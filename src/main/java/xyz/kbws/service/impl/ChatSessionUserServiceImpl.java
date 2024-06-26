package xyz.kbws.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import xyz.kbws.model.entity.ChatSessionUser;
import xyz.kbws.service.ChatSessionUserService;
import xyz.kbws.mapper.ChatSessionUserMapper;
import org.springframework.stereotype.Service;

/**
* @author hsy
* @description 针对表【chat_session_user(会话用户表)】的数据库操作Service实现
* @createDate 2024-06-26 13:54:42
*/
@Service
public class ChatSessionUserServiceImpl extends ServiceImpl<ChatSessionUserMapper, ChatSessionUser>
    implements ChatSessionUserService{

}




