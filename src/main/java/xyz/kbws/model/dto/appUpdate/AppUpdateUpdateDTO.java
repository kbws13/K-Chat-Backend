package xyz.kbws.model.dto.appUpdate;

import lombok.Data;

import java.io.Serializable;

/**
 * @author kbws
 * @date 2024/6/25
 * @description: 更新信息修改请求
 */
@Data
public class AppUpdateUpdateDTO implements Serializable {

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

    private static final long serialVersionUID = 96686045040065858L;
}
