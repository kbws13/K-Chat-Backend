package xyz.kbws.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author kbws
 * @date 2024/6/25
 * @description: 应用更新信息封装类
 */
@Data
public class AppUpdateVO implements Serializable {

    private Integer id;

    /**
     * 版本号
     */
    private String version;

    /**
     * 更新描述
     */
    private List<String> updateList;

    private Long size;

    private String fileName;

    private Integer fileType;

    private String outerLink;

    private static final long serialVersionUID = -8446226488332859259L;
}
