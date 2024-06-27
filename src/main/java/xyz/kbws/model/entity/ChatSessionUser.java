package xyz.kbws.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 会话用户表
 * @TableName chat_session_user
 */
@TableName(value ="chat_session_user")
@Data
public class ChatSessionUser implements Serializable {
    /**
     * 用户id
     */
    @TableId
    private String userId;

    /**
     * 联系人id
     */
    private String contactId;

    /**
     * 会话id
     */
    private String sessionId;

    /**
     * 联系人名称
     */
    private String contactName;

    @TableField(exist = false)
    private String lastMessage;

    @TableField(exist = false)
    private Long lastReceiveTime;

    @TableField(exist = false)
    private Integer contactType;

    @TableField(exist = false)
    private Integer memberCount;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}