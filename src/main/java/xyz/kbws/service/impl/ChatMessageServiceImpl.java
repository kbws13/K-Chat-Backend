package xyz.kbws.service.impl;

import cn.hutool.core.util.ArrayUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import xyz.kbws.common.ErrorCode;
import xyz.kbws.common.SysSetting;
import xyz.kbws.constant.RedisConstant;
import xyz.kbws.exception.BusinessException;
import xyz.kbws.mapper.ChatMessageMapper;
import xyz.kbws.mapper.ChatSessionMapper;
import xyz.kbws.model.dto.message.MessageSendDTO;
import xyz.kbws.model.entity.ChatMessage;
import xyz.kbws.model.entity.ChatSession;
import xyz.kbws.model.enums.MessageStatusEnum;
import xyz.kbws.model.enums.MessageTypeEnum;
import xyz.kbws.model.enums.UserContactTypeEnum;
import xyz.kbws.model.vo.UserVO;
import xyz.kbws.redis.RedisComponent;
import xyz.kbws.service.ChatMessageService;
import xyz.kbws.utils.StringUtil;
import xyz.kbws.websocket.MessageHandler;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author hsy
 * @description 针对表【chat_message(聊天消息表)】的数据库操作Service实现
 * @createDate 2024-06-26 13:45:20
 */
@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage>
        implements ChatMessageService {

    @Resource
    private ChatMessageMapper chatMessageMapper;

    @Resource
    private ChatSessionMapper chatSessionMapper;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private MessageHandler messageHandler;

    @Override
    public MessageSendDTO saveMessage(ChatMessage chatMessage, UserVO userVO) {
        // 不是机器人好友，判断好友状态
        if (!RedisConstant.ROBOT_UID.equals(userVO.getUserId())) {
            List<String> userContactList = redisComponent.getUserContactList(userVO.getUserId());
            if (!userContactList.contains(chatMessage.getContactId())) {
                UserContactTypeEnum userContactTypeEnum = UserContactTypeEnum.getByPrefix(chatMessage.getContactId());
                if (userContactTypeEnum == UserContactTypeEnum.USER) {
                    throw new BusinessException(ErrorCode.NON_FRIEND);
                } else {
                    throw new BusinessException(ErrorCode.NOT_IN_GROUP_CHAT);
                }
            }
        }
        String sessionId = null;
        String sendUserId = userVO.getUserId();
        String contactId = chatMessage.getContactId();
        Long curTime = System.currentTimeMillis();
        UserContactTypeEnum contactTypeEnum = UserContactTypeEnum.getByPrefix(contactId);
        MessageTypeEnum messageTypeEnum = MessageTypeEnum.getByType(chatMessage.getType());
        String lastMessage = chatMessage.getContent();
        String messageContent = StringUtil.resetMessageContent(chatMessage.getContent());
        chatMessage.setContent(messageContent);
        Integer status = MessageTypeEnum.MEDIA_CHAT == messageTypeEnum ? MessageStatusEnum.SENDING.getStatus() : MessageStatusEnum.SENDED.getStatus();
        if (ArrayUtil.contains(new Integer[]{
                MessageTypeEnum.CHAT.getType(),
                MessageTypeEnum.GROUP_CREATE.getType(),
                MessageTypeEnum.ADD_FRIEND.getType(),
                MessageTypeEnum.MEDIA_CHAT.getType()
        }, messageTypeEnum.getType())) {
            if (contactTypeEnum == UserContactTypeEnum.USER) {
                sessionId = StringUtil.getChatSessionForUser(new String[]{sendUserId, contactId});
            } else {
                sessionId = StringUtil.getChatSessionForGroup(contactId);
            }
            // 更新会话信息
            ChatSession chatSession = new ChatSession();
            chatSession.setLastMessage(messageContent);
            if (UserContactTypeEnum.GROUP == contactTypeEnum && !MessageTypeEnum.GROUP_CREATE.getType().equals(messageTypeEnum.getType())) {
                chatSession.setLastMessage(userVO.getNickName() + "：" + messageContent);
            }
            lastMessage = chatSession.getLastMessage();
            // 如果是媒体文件
            chatSession.setLastReceiveTime(curTime);
            chatSessionMapper.updateById(chatSession);
            //记录消息消息表
            chatMessage.setSessionId(sessionId);
            chatMessage.setSendUserId(sendUserId);
            chatMessage.setSendUserNickName(userVO.getNickName());
            chatMessage.setSendTime(curTime);
            chatMessage.setContactType(contactTypeEnum.getType());
            chatMessage.setStatus(status);
            chatMessageMapper.insert(chatMessage);
        }
        MessageSendDTO messageSendDTO = new MessageSendDTO();
        messageSendDTO.setSessionId(chatMessage.getSessionId());
        messageSendDTO.setSendUserId(chatMessage.getSendUserId());
        messageSendDTO.setSendUserNickName(chatMessage.getSendUserNickName());
        messageSendDTO.setContactId(chatMessage.getContactId());
        messageSendDTO.setMessageContent(chatMessage.getContent());
        messageSendDTO.setMessageType(chatMessage.getType());
        messageSendDTO.setSendTime(chatMessage.getSendTime());
        messageSendDTO.setContactType(chatMessage.getContactType());
        messageSendDTO.setStatus(chatMessage.getStatus());
        messageSendDTO.setFileSize(chatMessage.getFileSize());
        messageSendDTO.setFileName(chatMessage.getFileName());
        messageSendDTO.setFileType(chatMessage.getFileType());
        if (RedisConstant.ROBOT_UID.equals(contactId)) {
            SysSetting sysSetting = redisComponent.getSysSetting();
            UserVO robot = new UserVO();
            robot.setUserId(sysSetting.getRobotUid());
            robot.setNickName(sysSetting.getRobotNickName());
            ChatMessage robotMessage = new ChatMessage();
            robotMessage.setContactId(sendUserId);
            //这里可以对接Ai 根据输入的信息做出回答
            robotMessage.setContent("我只是一个机器人无法识别你的消息");
            robotMessage.setType(MessageTypeEnum.CHAT.getType());
            saveMessage(robotMessage, robot);
        } else {
            messageHandler.sendMessage(messageSendDTO);
        }
        return messageSendDTO;
    }
}




