package xyz.kbws.model.enums;

import lombok.Getter;

/**
 * @author kbws
 * @date 2024/4/24
 * @description: 靓号状态枚举
 */
@Getter
public enum BeautyAccountStatusEnum {

    NO_USE(0, "未使用"),
    USED(1, "已使用");


    private Integer status;
    private String desc;

    BeautyAccountStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }
}
