package xyz.kbws.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import xyz.kbws.mapper.UserContactMapper;
import xyz.kbws.model.dto.message.MessageSendDTO;
import xyz.kbws.model.entity.ChatSessionUser;
import xyz.kbws.model.entity.UserContact;
import xyz.kbws.model.enums.MessageTypeEnum;
import xyz.kbws.model.enums.UserContactStatusEnum;
import xyz.kbws.model.enums.UserContactTypeEnum;
import xyz.kbws.service.ChatSessionUserService;
import xyz.kbws.mapper.ChatSessionUserMapper;
import org.springframework.stereotype.Service;
import xyz.kbws.service.UserContactService;
import xyz.kbws.websocket.MessageHandler;

import javax.annotation.Resource;
import java.util.List;

/**
* @author hsy
* @description 针对表【chat_session_user(会话用户表)】的数据库操作Service实现
* @createDate 2024-06-26 13:54:42
*/
@Service
public class ChatSessionUserServiceImpl extends ServiceImpl<ChatSessionUserMapper, ChatSessionUser>
    implements ChatSessionUserService{

    @Resource
    private ChatSessionUserMapper chatSessionUserMapper;

    @Resource
    private UserContactMapper userContactMapper;

    @Resource
    private MessageHandler messageHandler;

    @Override
    public void updateRedundancyInfo(String contactName, String contactId) {
        ChatSessionUser chatSessionUser = chatSessionUserMapper.selectById(contactId);
        chatSessionUser.setContactName(contactName);
        chatSessionUserMapper.updateById(chatSessionUser);

        // 修改群昵称发送 WebSocket 信息
        UserContactTypeEnum contactTypeEnum = UserContactTypeEnum.getByPrefix(contactId);

        if (contactTypeEnum == UserContactTypeEnum.GROUP) {
            MessageSendDTO<String> messageSendDTO = new MessageSendDTO<>();
            messageSendDTO.setContactType(UserContactTypeEnum.getByPrefix(contactId).getType());
            messageSendDTO.setContactId(contactId);
            messageSendDTO.setExtentData(contactName);
            messageSendDTO.setMessageType(MessageTypeEnum.CONTACT_NAME_UPDATE.getType());
            messageHandler.sendMessage(messageSendDTO);
        }else {
            QueryWrapper<UserContact> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("contactType", UserContactTypeEnum.USER.getType());
            queryWrapper.eq("contactId", contactId);
            queryWrapper.eq("status", UserContactStatusEnum.FRIEND.getStatus());
            List<UserContact> userContactList = userContactMapper.selectList(queryWrapper);
            for (UserContact userContact : userContactList) {
                MessageSendDTO<String> messageSendDTO = new MessageSendDTO<>();
                messageSendDTO.setContactType(contactTypeEnum.getType());
                messageSendDTO.setContactId(userContact.getUserId());
                messageSendDTO.setExtentData(contactName);
                messageSendDTO.setMessageType(MessageTypeEnum.CONTACT_NAME_UPDATE.getType());
                messageSendDTO.setSendUserId(contactId);
                messageSendDTO.setSendUserNickName(contactName);
                messageHandler.sendMessage(messageSendDTO);
            }
        }

    }
}




