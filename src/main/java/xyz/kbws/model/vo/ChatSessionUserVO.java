package xyz.kbws.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author kbws
 * @date 2024/6/26
 * @description:
 */
@Data
public class ChatSessionUserVO implements Serializable {

    private String userId;

    private String contactId;

    /**
     * 会话id
     */
    private String sessionId;

    /**
     * 联系人名称
     */
    private String contactName;

    private String lastMessage;

    private Long lastReceiveTime;

    private Integer memberCount;

    private Integer contactType;

    private static final long serialVersionUID = -4878891743443654360L;
}
