package xyz.kbws.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 靓号表
 *
 * @TableName user_beauty
 */
@TableName(value = "user_beauty")
@Data
public class UserBeauty implements Serializable {
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    /**
     * id
     */
    @TableId
    private Integer id;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 用户id
     */
    private String userId;
    /**
     * 0:未使用 1:已使用
     */
    private Integer status;
}