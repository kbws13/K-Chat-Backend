package xyz.kbws.model.enums;

import lombok.Getter;

/**
 * @author kbws
 * @date 2024/5/4
 * @description: 群组状态枚举
 */
@Getter
public enum GroupStatusEnum {

    NORMAL(1, "正常"),
    DISSOLUTION(0, "解散");

    private final Integer status;

    private final String desc;

    GroupStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }
}
