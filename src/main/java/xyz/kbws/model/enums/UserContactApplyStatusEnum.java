package xyz.kbws.model.enums;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;

/**
 * @author kbws
 * @date 2024/5/23
 * @description: 联系人申请状态枚举
 */
@Getter
public enum UserContactApplyStatusEnum {
    INIT(0, "待处理"),
    PASS(1, "已同意"),
    REJECT(2, "已拒绝"),
    BLACKLIST(3, "已拉黑");

    private final Integer status;

    private final String desc;

    UserContactApplyStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public static UserContactApplyStatusEnum getByStatus(String status) {
        try {
            if (StrUtil.isEmpty(status)) {
                return null;
            }
            return UserContactApplyStatusEnum.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static UserContactApplyStatusEnum getByStatus(Integer status) {
        for (UserContactApplyStatusEnum item : UserContactApplyStatusEnum.values()) {
            if (item.getStatus().equals(status)) {
                return item;
            }
        }
        return null;
    }
}
