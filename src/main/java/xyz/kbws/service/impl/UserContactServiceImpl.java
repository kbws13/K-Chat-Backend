package xyz.kbws.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.kbws.common.ErrorCode;
import xyz.kbws.constant.UserConstant;
import xyz.kbws.exception.BusinessException;
import xyz.kbws.mapper.GroupInfoMapper;
import xyz.kbws.mapper.UserContactApplyMapper;
import xyz.kbws.mapper.UserContactMapper;
import xyz.kbws.mapper.UserMapper;
import xyz.kbws.model.entity.GroupInfo;
import xyz.kbws.model.entity.User;
import xyz.kbws.model.entity.UserContact;
import xyz.kbws.model.entity.UserContactApply;
import xyz.kbws.model.enums.*;
import xyz.kbws.model.vo.UserContactSearchResultVO;
import xyz.kbws.model.vo.UserContactVO;
import xyz.kbws.model.vo.UserVO;
import xyz.kbws.service.UserContactService;

import javax.annotation.Resource;
import java.util.List;

/**
* @author hsy
* @description 针对表【user_contact(联系人表)】的数据库操作Service实现
* @createDate 2024-04-26 14:51:55
*/
@Slf4j
@Service
public class UserContactServiceImpl extends ServiceImpl<UserContactMapper, UserContact>
    implements UserContactService{

    @Resource
    private UserContactMapper userContactMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private GroupInfoMapper groupInfoMapper;

    @Resource
    private UserContactApplyMapper userContactApplyMapper;

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
        switch (userContactTypeEnum){
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
        if (userContact != null && UserContactStatusEnum.BLACKLIST_BE.getStatus().equals(userContact.getStatus())) {
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
            // TODO 添加联系人

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
}




