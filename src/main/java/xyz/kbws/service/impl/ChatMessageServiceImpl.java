package xyz.kbws.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import xyz.kbws.model.entity.ChatMessage;
import xyz.kbws.service.ChatMessageService;
import xyz.kbws.mapper.ChatMessageMapper;
import org.springframework.stereotype.Service;

/**
* @author hsy
* @description 针对表【chat_message(聊天消息表)】的数据库操作Service实现
* @createDate 2024-06-26 13:45:20
*/
@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage>
    implements ChatMessageService{

}




