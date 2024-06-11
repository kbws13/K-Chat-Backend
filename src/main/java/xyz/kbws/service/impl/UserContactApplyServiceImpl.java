package xyz.kbws.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import xyz.kbws.common.ErrorCode;
import xyz.kbws.exception.BusinessException;
import xyz.kbws.mapper.UserContactApplyMapper;
import xyz.kbws.mapper.UserContactMapper;
import xyz.kbws.model.entity.UserContact;
import xyz.kbws.model.entity.UserContactApply;
import xyz.kbws.model.enums.UserContactApplyStatusEnum;
import xyz.kbws.model.enums.UserContactStatusEnum;
import xyz.kbws.model.vo.UserContactApplyVO;
import xyz.kbws.service.UserContactApplyService;
import xyz.kbws.service.UserContactService;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
* @author hsy
* @description 针对表【user_contact_apply(联系人申请表)】的数据库操作Service实现
* @createDate 2024-04-26 14:51:59
*/
@Service
public class UserContactApplyServiceImpl extends ServiceImpl<UserContactApplyMapper, UserContactApply>
    implements UserContactApplyService{

    @Resource
    private UserContactService userContactService;

    @Resource
    private UserContactApplyMapper userContactApplyMapper;

    @Resource
    private UserContactMapper userContactMapper;


    @Override
    public List<UserContactApplyVO> getUserContactApplyVO(String receiveId) {
        return userContactApplyMapper.selectApplyVO(receiveId);
    }

    @Override
    public void dealWithApply(String userId, Integer applyId, Integer status) {
        UserContactApplyStatusEnum statusEnum = UserContactApplyStatusEnum.getByStatus(status);
        if (statusEnum == null || UserContactApplyStatusEnum.INIT == statusEnum) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserContactApply apply = userContactApplyMapper.selectById(applyId);
        if (apply == null || !userId.equals(apply.getReceiveId())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        apply.setStatus(statusEnum.getStatus());
        apply.setLastApplyTime(System.currentTimeMillis());
        QueryWrapper<UserContactApply> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", applyId).eq("status", UserContactApplyStatusEnum.INIT.getStatus());
        int count = userContactApplyMapper.update(apply, queryWrapper);
        if (count == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "处理申请失败");
        }

        if (UserContactApplyStatusEnum.PASS.getStatus().equals(status)) {
            // 添加联系人
            userContactService.addUserContact(apply.getApplyId(), apply.getReceiveId(), apply.getContactId(), apply.getContactType(), apply.getApplyInfo());
            return;
        }

        if (UserContactApplyStatusEnum.BLACKLIST == statusEnum) {
            Date curDate = new Date();
            QueryWrapper<UserContact> query = new QueryWrapper<>();
            query.eq("userId", apply.getApplyId()).eq("contactId", apply.getContactId());
            UserContact userContact = userContactMapper.selectOne(query);
            if (userContact == null) {
                userContact = new UserContact();
                userContact.setUserId(apply.getApplyId());
                userContact.setContactId(apply.getContactId());
                userContact.setContactType(apply.getContactType());
                userContact.setCreateTime(curDate);
                userContact.setStatus(UserContactStatusEnum.BLACKLIST_BE_FIRST.getStatus());
                userContact.setUpdateTime(curDate);
                userContactMapper.insert(userContact);
            } else {
                userContact.setStatus(UserContactStatusEnum.BLACKLIST_BE_FIRST.getStatus());
                userContact.setUpdateTime(curDate);
                userContactMapper.update(userContact, query);
            }
        }
    }

}




