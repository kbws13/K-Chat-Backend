package xyz.kbws.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * app发布表
 * @TableName app_update
 */
@TableName(value ="app_update")
@Data
public class AppUpdate implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 版本号
     */
    private String version;

    /**
     * 更新信息
     */
    private String updateDesc;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 0:未发布 1:灰度发布 2:全部发布
     */
    private Integer status;

    /**
     * 灰度uid
     */
    private String grayscaleUid;

    /**
     * 文件类型 0:本地文件 1:外链
     */
    private Integer fileType;

    /**
     * 外链地址
     */
    private String outerLink;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}