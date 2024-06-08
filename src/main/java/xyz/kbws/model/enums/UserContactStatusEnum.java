package xyz.kbws.model.enums;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;

/**
 * @author kbws
 * @date 2024/4/27
 * @description: 联系人状态枚举
 */
@Getter
public enum UserContactStatusEnum {

    NOT_FRIEND(0, "非好友"),
    FRIEND(1, "好友"),
    DEL(2, "已删除好友"),
    DEL_BE(3, "被删除"),
    BLACKLIST(4, "已拉黑好友"),
    BLACKLIST_BE(5, "被好友拉黑");

    private final Integer status;

    private final String desc;

    UserContactStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public static UserContactStatusEnum getByName(String name) {
        try {
            if (StrUtil.isEmpty(name)) {
                return null;
            }
            return UserContactStatusEnum.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static UserContactStatusEnum getByStatus(Integer status) {
        for (UserContactStatusEnum item : UserContactStatusEnum.values()) {
            if (item.getStatus().equals(status)) {
                return item;
            }
        }
        return null;
    }
}
