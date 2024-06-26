package xyz.kbws.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

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
    @TableId
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
    private static final long serialVersionUID = 1L;
}