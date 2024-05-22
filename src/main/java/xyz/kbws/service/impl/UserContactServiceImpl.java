package xyz.kbws.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import xyz.kbws.exception.ThrowUtils;
import xyz.kbws.mapper.UserContactMapper;
import xyz.kbws.model.entity.UserContact;
import xyz.kbws.model.vo.UserContactVO;
import xyz.kbws.service.UserContactService;
import org.springframework.stereotype.Service;

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

    @Override
    public List<UserContactVO> listUsers(String userId) {
        return userContactMapper.listUsers(userId);
    }
}




