package xyz.kbws.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 会话信息表
 * @TableName chat_session
 */
@TableName(value ="chat_session")
@Data
public class ChatSession implements Serializable {
    /**
     * 会话id
     */
    @TableId
    private String sessionId;

    /**
     * 最后接收的消息
     */
    private String lastMessage;

    /**
     * 最后接收消息时间(毫秒)
     */
    private Long lastReceiveTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}