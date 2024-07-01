package xyz.kbws.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import xyz.kbws.common.ErrorCode;
import xyz.kbws.common.SysSetting;
import xyz.kbws.config.AppConfig;
import xyz.kbws.constant.FileConstant;
import xyz.kbws.constant.RedisConstant;
import xyz.kbws.exception.BusinessException;
import xyz.kbws.mapper.ChatMessageMapper;
import xyz.kbws.mapper.ChatSessionMapper;
import xyz.kbws.model.dto.message.MessageSendDTO;
import xyz.kbws.model.entity.ChatMessage;
import xyz.kbws.model.entity.ChatSession;
import xyz.kbws.model.entity.UserContact;
import xyz.kbws.model.enums.*;
import xyz.kbws.model.vo.UserVO;
import xyz.kbws.redis.RedisComponent;
import xyz.kbws.service.ChatMessageService;
import xyz.kbws.service.UserContactService;
import xyz.kbws.utils.StringUtil;
import xyz.kbws.websocket.MessageHandler;

import javax.annotation.Resource;
import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * @author hsy
 * @description 针对表【chat_message(聊天消息表)】的数据库操作Service实现
 * @createDate 2024-06-26 13:45:20
 */
@Slf4j
@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage>
        implements ChatMessageService {

    @Resource
    private UserContactService userContactService;

    @Resource
    private ChatMessageMapper chatMessageMapper;

    @Resource
    private ChatSessionMapper chatSessionMapper;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private MessageHandler messageHandler;

    @Resource
    private AppConfig appConfig;

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

    @Override
    public void saveMessageFile(String userId, Long messageId, MultipartFile file, MultipartFile cover) {
        ChatMessage chatMessage = chatMessageMapper.selectById(messageId);
        if (chatMessage == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (!chatMessage.getSendUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        SysSetting sysSetting = redisComponent.getSysSetting();
        String fileSuffix = FileNameUtil.mainName(file.getOriginalFilename());
        if (!StrUtil.isEmpty(fileSuffix) && ArrayUtil.contains(FileConstant.IMAGE_SUFFIX_LIST, fileSuffix.toLowerCase())
                && file.getSize() > FileConstant.FILE_SIZE_MB * sysSetting.getMaxVideoSize()) {
            return;
        }
        String fileName = file.getOriginalFilename();
        String fileExtName = FileNameUtil.extName(fileName);
        String fileRealName = messageId + fileExtName;
        String month = DateUtil.format(new Date(chatMessage.getSendTime()), DateTimePatternEnum.YYYYMM.getPattern());
        File folder = new File(appConfig.getProjectFolder() + FileConstant.FILE_FOLDER + month);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File uploadFile = new File(folder.getPath() + "/" + fileRealName);
        try {
            file.transferTo(uploadFile);
            if (cover != null) {
                cover.transferTo(new File(uploadFile.getPath() + FileConstant.COVER_IMAGE_SUFFIX));
            }
        } catch (Exception e) {
            log.error("上传文件失败，", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "文件上传失败");
        }
        chatMessage.setStatus(MessageStatusEnum.SENDED.getStatus());
        chatMessageMapper.updateById(chatMessage);

        MessageSendDTO messageSendDTO = new MessageSendDTO();
        messageSendDTO.setStatus(MessageStatusEnum.SENDED.getStatus());
        messageSendDTO.setMessageId(Long.valueOf(chatMessage.getId()));
        messageSendDTO.setMessageType(MessageTypeEnum.FILE_UPLOAD.getType());
        messageSendDTO.setContactId(chatMessage.getContactId());
        messageHandler.sendMessage(messageSendDTO);
    }

    @Override
    public File downloadFile(UserVO userVO, Long messageId, Boolean cover) {
        ChatMessage chatMessage = chatMessageMapper.selectById(messageId);
        String contactId = chatMessage.getContactId();
        UserContactTypeEnum contactTypeEnum = UserContactTypeEnum.getByPrefix(contactId);
        if (UserContactTypeEnum.USER.equals(contactTypeEnum) && !userVO.getUserId().equals(chatMessage.getContactId())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        if (UserContactTypeEnum.GROUP.equals(contactTypeEnum)) {
            QueryWrapper<UserContact> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userId", userVO.getUserId());
            queryWrapper.eq("contactType", UserContactTypeEnum.GROUP.getType());
            queryWrapper.eq("status", UserContactStatusEnum.FRIEND.getStatus());
            Integer contactCount = Math.toIntExact(userContactService.count(queryWrapper));
            if (contactCount == 0) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR);
            }
        }
        String month = DateUtil.format(new Date(chatMessage.getSendTime()), DateTimePatternEnum.YYYYMM.getPattern());
        File folder = new File(appConfig.getProjectFolder() + FileConstant.FILE_FOLDER + month);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String fileName = chatMessage.getFileName();
        String fileExtName = FileNameUtil.getSuffix(fileName);
        String fileRealName = messageId + fileExtName;

        if (cover != null && cover) {
            fileRealName = fileRealName + FileConstant.COVER_IMAGE_SUFFIX;
        }
        File file = new File(folder.getPath() + "/" + fileRealName);
        if (!file.exists()) {
            log.info("文件不存在");
            throw new BusinessException(ErrorCode.FILE_DOES_NOT_EXIST);
        }
        return file;
    }
}




