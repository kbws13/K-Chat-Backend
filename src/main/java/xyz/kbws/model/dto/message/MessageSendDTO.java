package xyz.kbws.model.dto.message;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;

/**
 * @author kbws
 * @date 2024/6/26
 * @description:
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageSendDTO<T> implements Serializable {

    /**
     * 消息id
     */
    private Long messageId;

    /**
     * 会话id
     */
    private String sessionId;

    /**
     * 发送人
     */
    private String sendUserId;

    /**
     * 发送人昵称
     */
    private String sendUserNickName;

    /**
     * 联系人id
     */
    private String contactId;

    /**
     * 联系人名称
     */
    private String contactName;

    /**
     * 消息内容
     */
    private String messageContent;

    /**
     * 最后的消息
     */
    private String lastMessage;

    /**
     * 消息类型
     */
    private Integer messageType;

    /**
     * 发送时间
     */
    private Long sendTime;

    /**
     * 联系人类型
     */
    private Integer contactType;

    /**
     * 扩展信息
     */
    private T extentData;

    /**
     * 消息状态
     * 0：发送中 1：已发送
     * 对于文件是异步上传用状态处理
     */
    private Integer status;

    /**
     * 文件信息
     */
    private Long fileSize;

    private Long fileName;

    private Integer fileType;

    //群员
    private Integer memberCount;

    private static final long serialVersionUID = -7263093001338914406L;

    public String getLastMessage() {
        if (StrUtil.isEmpty(lastMessage)) {
            return messageContent;
        }
        return lastMessage;
    }
}
