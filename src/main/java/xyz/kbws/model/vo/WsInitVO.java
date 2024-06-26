package xyz.kbws.model.vo;

import lombok.Data;
import xyz.kbws.model.entity.ChatMessage;
import xyz.kbws.model.entity.ChatSessionUser;

import java.io.Serializable;
import java.util.List;

/**
 * @author kbws
 * @date 2024/6/26
 * @description: 初始化连接时返回的会话信息
 */
@Data
public class WsInitVO implements Serializable {

    private List<ChatSessionUser> chatSessionList;

    private List<ChatMessage> chatMessageList;

    private Integer applyCount;

    private static final long serialVersionUID = -1529454968441257413L;
}
