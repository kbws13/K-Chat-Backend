package xyz.kbws.controller;

import cn.hutool.core.util.ArrayUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.kbws.annotation.AuthCheck;
import xyz.kbws.common.BaseResponse;
import xyz.kbws.common.ErrorCode;
import xyz.kbws.common.ResultUtils;
import xyz.kbws.config.AppConfig;
import xyz.kbws.exception.BusinessException;
import xyz.kbws.model.dto.chat.ChatSendMessageDTO;
import xyz.kbws.model.dto.message.MessageSendDTO;
import xyz.kbws.model.entity.ChatMessage;
import xyz.kbws.model.enums.MessageTypeEnum;
import xyz.kbws.model.vo.UserVO;
import xyz.kbws.service.ChatMessageService;
import xyz.kbws.service.ChatSessionUserService;
import xyz.kbws.utils.JwtUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author kbws
 * @date 2024/6/29
 * @description:
 */
@Slf4j
@Api(tags = "聊天接口")
@RestController
@RequestMapping("/chat")
public class ChatController {

    @Resource
    private ChatMessageService chatMessageService;

    @Resource
    private ChatSessionUserService chatSessionUserService;

    @Resource
    private AppConfig appConfig;

    @Resource
    private JwtUtils jwtUtils;

    @ApiOperation(value = "发送信息接口")
    @PostMapping("/sendMessage")
    @AuthCheck
    public BaseResponse sendMessage(@RequestBody ChatSendMessageDTO chatSendMessageDTO, HttpServletRequest request) {
        MessageTypeEnum messageTypeEnum = MessageTypeEnum.getByType(chatSendMessageDTO.getMessageType());
        if (messageTypeEnum == null || !ArrayUtil.contains(new Integer[]{MessageTypeEnum.CHAT.getType(), MessageTypeEnum.MEDIA_CHAT.getType()}, chatSendMessageDTO.getMessageType())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserVO userVOByToken = jwtUtils.getUserVOByToken(request);
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContactId(chatSendMessageDTO.getContactId());
        chatMessage.setContent(chatSendMessageDTO.getMessageContent());
        chatMessage.setFileSize(chatSendMessageDTO.getFileSize());
        chatMessage.setFileName(chatSendMessageDTO.getFileName());
        chatMessage.setFileType(chatSendMessageDTO.getFileType());
        chatMessage.setType(chatSendMessageDTO.getMessageType());
        MessageSendDTO messageSendDTO = chatMessageService.saveMessage(chatMessage, userVOByToken);
        return ResultUtils.success(messageSendDTO);
    }

}
