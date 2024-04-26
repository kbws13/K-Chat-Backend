package xyz.kbws.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import xyz.kbws.exception.ThrowUtils;
import xyz.kbws.mapper.UserContactApplyMapper;
import xyz.kbws.model.entity.UserContactApply;
import xyz.kbws.service.UserContactApplyService;
import org.springframework.stereotype.Service;

/**
* @author hsy
* @description 针对表【user_contact_apply(联系人申请表)】的数据库操作Service实现
* @createDate 2024-04-26 14:51:59
*/
@Service
public class UserContactApplyServiceImpl extends ServiceImpl<UserContactApplyMapper, UserContactApply>
    implements UserContactApplyService{

}




