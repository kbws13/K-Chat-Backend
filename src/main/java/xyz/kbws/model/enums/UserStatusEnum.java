package xyz.kbws.model.enums;

import lombok.Getter;

/**
 * @author kbws
 * @date 2024/4/25
 * @description: 用户状态枚举
 */
@Getter
public enum UserStatusEnum {

    DISABLE(0, "禁用"),
    ENABLE(1, "启用");

    private Integer status;
    private String desc;

    UserStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public static UserStatusEnum getByStatus(Integer status) {
        for (UserStatusEnum item : UserStatusEnum.values()) {
            if (item.getStatus().equals(status)) {
                return item;
            }
        }
        return null;
    }
}
