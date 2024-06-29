package xyz.kbws.model.dto.chat;

import lombok.Data;

import java.io.Serializable;

/**
 * @author kbws
 * @date 2024/6/29
 * @description: 发送消息请求封装
 */
@Data
public class ChatSendMessageDTO implements Serializable {

    private String contactId;

    private String messageContent;

    private Integer messageType;

    private Long fileSize;

    private String fileName;

    private Integer fileType;

    private static final long serialVersionUID = -8821620043074317973L;
}
