package xyz.kbws.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import xyz.kbws.mapper.GroupInfoMapper;
import xyz.kbws.mapper.UserContactMapper;
import xyz.kbws.mapper.UserMapper;
import xyz.kbws.model.entity.GroupInfo;
import xyz.kbws.model.entity.User;
import xyz.kbws.model.entity.UserContact;
import xyz.kbws.model.enums.UserContactStatusEnum;
import xyz.kbws.model.enums.UserContactTypeEnum;
import xyz.kbws.model.vo.UserContactSearchResultVO;
import xyz.kbws.model.vo.UserContactVO;
import xyz.kbws.service.UserContactService;

import javax.annotation.Resource;
import java.util.List;

/**
* @author hsy
* @description 针对表【user_contact(联系人表)】的数据库操作Service实现
* @createDate 2024-04-26 14:51:55
*/
@Service
public class UserContactServiceImpl extends ServiceImpl<UserContactMapper, UserContact>
    implements UserContactService{

    @Resource
    private UserContactMapper userContactMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private GroupInfoMapper groupInfoMapper;

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
}




