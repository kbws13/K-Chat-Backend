package xyz.kbws.controller;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import xyz.kbws.annotation.AuthCheck;
import xyz.kbws.common.BaseResponse;
import xyz.kbws.common.ErrorCode;
import xyz.kbws.common.ResultUtils;
import xyz.kbws.config.AppConfig;
import xyz.kbws.constant.FileConstant;
import xyz.kbws.exception.BusinessException;
import xyz.kbws.model.dto.chat.ChatSendMessageDTO;
import xyz.kbws.model.dto.chat.DownloadFileDTO;
import xyz.kbws.model.dto.message.MessageSendDTO;
import xyz.kbws.model.entity.ChatMessage;
import xyz.kbws.model.enums.MessageTypeEnum;
import xyz.kbws.model.vo.UserVO;
import xyz.kbws.service.ChatMessageService;
import xyz.kbws.service.ChatSessionUserService;
import xyz.kbws.utils.JwtUtils;
import xyz.kbws.utils.StringUtil;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

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
    public BaseResponse<MessageSendDTO> sendMessage(@RequestBody ChatSendMessageDTO chatSendMessageDTO, HttpServletRequest request) {
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

    @ApiOperation(value = "上传文件")
    @PostMapping("/uploadFile")
    @AuthCheck
    public BaseResponse<String> uploadFile(HttpServletRequest request, Long messageId, MultipartFile file, MultipartFile cover) {
        UserVO userVOByToken = jwtUtils.getUserVOByToken(request);
        chatMessageService.saveMessageFile(userVOByToken.getUserId(), messageId, file, cover);
        return ResultUtils.success("上传成功");
    }

    @ApiOperation(value = "下载文件")
    @PostMapping("/downloadFile")
    @AuthCheck
    public void downloadFile(@RequestBody DownloadFileDTO downloadFileDTO, HttpServletRequest request, HttpServletResponse response) {
        UserVO userVOByToken = jwtUtils.getUserVOByToken(request);
        OutputStream out = null;
        FileInputStream in = null;
        try {
            File file = null;
            if (StringUtil.isNumber(downloadFileDTO.getFileId())) {
                String avatarFolderName = FileConstant.FILE_FOLDER + FileConstant.FILE_FOLDER_AVATAR_NAME;
                String avatarPath = appConfig.getProjectFolder() + avatarFolderName + downloadFileDTO.getFileId() + FileConstant.IMAGE_SUFFIX;
                if (downloadFileDTO.getShowCover()) {
                    avatarPath = avatarPath + FileConstant.COVER_IMAGE_SUFFIX;
                }
                file = new File(avatarPath);
                if (!file.exists()) {
                    throw new BusinessException(ErrorCode.FILE_DOES_NOT_EXIST);
                }
            } else {
                file = chatMessageService.downloadFile(userVOByToken, Long.parseLong(downloadFileDTO.getFileId()), downloadFileDTO.getShowCover());
            }
            response.setContentType("application/x-msdownload; charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;");
            response.setContentLengthLong(file.length());
            in = new FileInputStream(file);
            byte[] byteData = new byte[1024];
            out = response.getOutputStream();
            int len = 0;
            while ((len = in.read(byteData)) != -1) {
                out.write(byteData, 0, len);
            }
            out.flush();
        } catch (Exception e) {
            log.error("下载文件失败");
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    log.error("IO异常", e);
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    log.error("IO异常", e);
                }
            }
        }
    }

}
