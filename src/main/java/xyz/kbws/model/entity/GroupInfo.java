package xyz.kbws.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * 群组表
 * @TableName group_info
 */
@TableName(value ="group_info")
@Data
public class GroupInfo implements Serializable {
    /**
     * 群组id
     */
    @TableId
    private String id;

    /**
     * 群组名
     */
    private String name;

    /**
     * 群主id
     */
    private String ownerId;

    /**
     * 创建时间
     */
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 群公告
     */
    private String notice;

    /**
     * 0:直接加入 1:管理员同意后加入
     */
    private Integer joinType;

    /**
     * 1:正常 0:解散
     */
    private Integer status;

    /**
     * 群组成员数量
     */
    @TableField(exist = false)
    private Integer memberCount;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}