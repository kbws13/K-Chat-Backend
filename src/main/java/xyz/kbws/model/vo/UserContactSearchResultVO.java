package xyz.kbws.model.vo;

import lombok.Data;
import xyz.kbws.model.enums.UserContactStatusEnum;

import java.io.Serializable;

/**
 * @author kbws
 * @date 2024/5/22
 * @description: 联系人/群组搜索结果
 */
@Data
public class UserContactSearchResultVO implements Serializable {

    private String contactId;

    private String contactType;

    private String nickName;

    private Integer status;

    private String statusName;

    private Integer sex;

    private String areaName;

    public String getStatusName() {
        UserContactStatusEnum statusEnum = UserContactStatusEnum.getByStatus(status);
        return statusEnum == null ? null : statusEnum.getDesc();
    }

    private static final long serialVersionUID = 2671283111677262148L;
}
