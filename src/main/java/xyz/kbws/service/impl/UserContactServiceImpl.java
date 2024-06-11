package xyz.kbws.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.kbws.common.ErrorCode;
import xyz.kbws.common.SysSetting;
import xyz.kbws.constant.UserConstant;
import xyz.kbws.exception.BusinessException;
import xyz.kbws.mapper.GroupInfoMapper;
import xyz.kbws.mapper.UserContactApplyMapper;
import xyz.kbws.mapper.UserContactMapper;
import xyz.kbws.mapper.UserMapper;
import xyz.kbws.model.dto.userContact.UserContactQueryDTO;
import xyz.kbws.model.entity.GroupInfo;
import xyz.kbws.model.entity.User;
import xyz.kbws.model.entity.UserContact;
import xyz.kbws.model.entity.UserContactApply;
import xyz.kbws.model.enums.*;
import xyz.kbws.model.vo.UserContactSearchResultVO;
import xyz.kbws.model.vo.UserContactVO;
import xyz.kbws.model.vo.UserVO;
import xyz.kbws.redis.RedisComponent;
import xyz.kbws.service.UserContactApplyService;
import xyz.kbws.service.UserContactService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author hsy
 * @description 针对表【user_contact(联系人表)】的数据库操作Service实现
 * @createDate 2024-04-26 14:51:55
 */
@Slf4j
@Service
public class UserContactServiceImpl extends ServiceImpl<UserContactMapper, UserContact>
        implements UserContactService {

    @Lazy
    @Resource
    private UserContactApplyService userContactApplyService;

    @Resource
    private UserContactMapper userContactMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private GroupInfoMapper groupInfoMapper;

    @Resource
    private UserContactApplyMapper userContactApplyMapper;

    @Resource
    private RedisComponent redisComponent;

    @Override
    public List<UserContactVO> listUsers(String userId) {
        return userContactMapper.listUsers(userId);
    }

    @Override
    public UserContactSearchResultVO searchContact(String userId, String contactId) {
        UserContactTypeEnum userContactTypeEnum = UserContactTypeEnum.getByPrefix(contactId);
        if (userContactTypeEnum == null) {
            return null;
        }
        UserContactSearchResultVO resultVO = new UserContactSearchResultVO();
        switch (userContactTypeEnum) {
            case USER:
                User user = userMapper.selectById(contactId);
                if (user == null) {
                    return null;
                }
                BeanUtil.copyProperties(user, resultVO);
                break;
            case GROUP:
                GroupInfo groupInfo = groupInfoMapper.selectById(contactId);
                resultVO.setNickName(groupInfo.getName());
                break;
        }
        resultVO.setContactType(userContactTypeEnum.toString());
        resultVO.setContactId(contactId);
        if (userId.equals(contactId)) {
            resultVO.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            return resultVO;
        }
        // 查询是否是好友
        QueryWrapper<UserContact> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId).eq("contactId", contactId);
        UserContact userContact = userContactMapper.selectOne(queryWrapper);
        resultVO.setStatus(userContact == null ? null : userContact.getStatus());
        return resultVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer apply(UserVO userVO, String contactId, String applyMessage) {
        UserContactTypeEnum contactTypeEnum = UserContactTypeEnum.getByPrefix(contactId);
        if (contactTypeEnum == null) {
            log.error("该联系人类型不存在");
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        // 申请人
        String applyUserId = userVO.getUserId();
        // 如果申请信息为空，则填充为默认的申请信息
        if (StrUtil.isEmpty(applyMessage)) {
            applyMessage = String.format(UserConstant.APPLY_MESSAGE_TEMPLATE, userVO.getNickName());
        }

        Long currentTime = System.currentTimeMillis();

        Integer joinType;
        String receiveUserId = contactId;

        // 查询对方是否已经被添加为好友，如果被对方拉黑则无法添加
        UserContact userContact = userContactMapper.selectByUserIdAndContactId(receiveUserId, contactId);
        if (userContact != null && ArrayUtil.contains(new Integer[]{
                UserContactStatusEnum.BLACKLIST_BE.getStatus(),
                UserContactStatusEnum.BLACKLIST_BE_FIRST.getStatus()
        }, userContact.getStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "对方已将你拉黑，无法添加");
        }
        // 添加群组
        if (UserContactTypeEnum.GROUP == contactTypeEnum) {
            GroupInfo groupInfo = groupInfoMapper.selectById(contactId);
            if (groupInfo == null || GroupStatusEnum.DISSOLUTION.getStatus().equals(groupInfo.getStatus())) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "群聊不存在或已解散");
            }
            receiveUserId = groupInfo.getOwnerId();
            joinType = userVO.getJoinType();
        } else {
            User user = userMapper.selectById(contactId);
            if (user == null) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR);
            }
            joinType = user.getJoinType();
        }
        // 直接加入不用记录申请记录
        if (JoinTypeEnum.JOIN.getType().equals(joinType)) {
            // 添加联系人
            this.addUserContact(applyUserId, receiveUserId, contactId, contactTypeEnum.getType(), applyMessage);
            return joinType;
        }
        UserContactApply userContactApply = userContactApplyMapper.selectByPrimaryKey(applyUserId, receiveUserId, contactId);
        UserContactApply contactApply = new UserContactApply();
        if (userContactApply == null) {
            contactApply.setApplyId(applyUserId);
            contactApply.setReceiveId(receiveUserId);
            contactApply.setContactType(contactTypeEnum.getType());
            contactApply.setContactId(contactId);
            contactApply.setLastApplyTime(currentTime);
            contactApply.setStatus(UserContactApplyStatusEnum.INIT.getStatus());
            contactApply.setApplyInfo(applyMessage);
            userContactApplyMapper.insert(contactApply);
        } else {
            // 更新状态
            contactApply.setStatus(UserContactApplyStatusEnum.INIT.getStatus());
            contactApply.setLastApplyTime(currentTime);
            contactApply.setApplyInfo(applyMessage);
            userContactApplyMapper.updateById(contactApply);
        }

        if (userContactApply == null || !UserContactApplyStatusEnum.INIT.getStatus().equals(userContactApply.getStatus())) {
            // TODO 发送 ws 消息
        }
        return joinType;
    }

    @Override
    public void addUserContact(String applyUserId, String receiveUserId, String contactId, Integer contactType, String applyMessage) {
        // 群聊人数
        if (UserContactTypeEnum.GROUP.getType().equals(contactType)) {
            QueryWrapper<UserContact> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("contactId", contactId);
            int count = Math.toIntExact(userContactMapper.selectCount(queryWrapper));
            SysSetting sysSetting = redisComponent.getSysSetting();
            if (count >= sysSetting.getMaxGroupCount()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "成员已满，无法加入");
            }
        }
        // 同意双方添加好友
        List<UserContact> contactList = new ArrayList<>();
        // 申请人添加对方
        UserContact userContact = new UserContact();
        userContact.setUserId(applyUserId);
        userContact.setContactId(contactId);
        userContact.setContactType(contactType);
        userContact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
        contactList.add(userContact);
        // 如果是申请好友，接收人添加申请人，群组不用添加对方为好友
        if (UserContactTypeEnum.GROUP.getType().equals(contactType)) {
            userContact = new UserContact();
            userContact.setUserId(receiveUserId);
            userContact.setContactId(contactId);
            userContact.setContactType(contactType);
            userContact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            contactList.add(userContact);
        }
        // 批量插入（使用 AOP 获取当前类的代理对象，然后调用方法，防止事务失效）
        UserContactService userContactService = (UserContactService) AopContext.currentProxy();
        userContactService.saveOrUpdateBatch(contactList);
        // TODO 如果是好友，接收人也添加申请人为好友 添加缓存
        // TODO 创建会话 发送消息
    }

    @Override
    public List<UserContactVO> listByParam(UserContactQueryDTO userContactQueryDTO) {
        return userContactMapper.myContact(userContactQueryDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateContactStatus(String userId, String contactId, UserContactStatusEnum statusEnum) {
        // 移除好友
        UserContact userContact = new UserContact();
        userContact.setStatus(statusEnum.getStatus());
        QueryWrapper<UserContact> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId).eq("contactId", contactId);
        userContactMapper.update(userContact, queryWrapper);

        // 从好友那里移除自己
        UserContact friend = new UserContact();
        if (UserContactStatusEnum.DEL == statusEnum) {
            friend.setStatus(UserContactStatusEnum.DEL_BE.getStatus());
        } else if (UserContactStatusEnum.BLACKLIST == statusEnum) {
            friend.setStatus(UserContactStatusEnum.BLACKLIST_BE.getStatus());
        }
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", contactId).eq("contactId", userContact);
        userContactMapper.update(friend, queryWrapper);
        // TODO 从我的好友列表缓存中删除
        // TODO 从好友列表缓存中删除我
        return true;
    }
}




