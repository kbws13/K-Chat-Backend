package xyz.kbws.model.dto.userContact;

import lombok.Data;

/**
 * @author kbws
 * @date 2024/6/8
 * @description:
 */
@Data
public class UserContactQueryDTO {

    private String userId;

    private String contactId;

    private Integer contactType;

    private Integer categoryType;

    private Boolean queryContactUserInfo;

    private Boolean queryGroupInfo;

    private String orderBy;

    private Boolean excludeMyGroup;

    /**
     * 状态 0:非好友 1:好友 2:已删除 3:拉黑
     */
    private Integer status;

    private Integer[] statusArray;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 更新时间
     */
    private String updateTime;

}
