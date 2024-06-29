package xyz.kbws.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import xyz.kbws.mapper.ChatSessionMapper;
import xyz.kbws.model.entity.ChatSession;
import xyz.kbws.service.ChatSessionService;

/**
* @author hsy
* @description 针对表【chat_session(会话信息表)】的数据库操作Service实现
* @createDate 2024-06-26 13:32:39
*/
@Service
public class ChatSessionServiceImpl extends ServiceImpl<ChatSessionMapper, ChatSession>
    implements ChatSessionService{

}




