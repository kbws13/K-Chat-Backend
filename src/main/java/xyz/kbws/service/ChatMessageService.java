package xyz.kbws.service;

import org.springframework.web.multipart.MultipartFile;
import xyz.kbws.model.dto.message.MessageSendDTO;
import xyz.kbws.model.entity.ChatMessage;
import com.baomidou.mybatisplus.extension.service.IService;
import xyz.kbws.model.vo.UserVO;

import java.io.File;

/**
* @author hsy
* @description 针对表【chat_message(聊天消息表)】的数据库操作Service
* @createDate 2024-06-26 13:45:20
*/
public interface ChatMessageService extends IService<ChatMessage> {
    MessageSendDTO saveMessage(ChatMessage chatMessage, UserVO userVO);

    void saveMessageFile(String userId, Long messageId, MultipartFile file, MultipartFile cover);

    File downloadFile(UserVO userVO, Long messageId, Boolean cover);
}
