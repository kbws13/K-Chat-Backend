package xyz.kbws.model.enums;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;

/**
 * @author kbws
 * @date 2024/4/25
 * @description: 加好友方式
 */
@Getter
public enum JoinTypeEnum {

    JOIN(0, "直接加入"),
    APPLY(1, "需要审核");

    private Integer type;
    private String desc;

    JoinTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public static JoinTypeEnum getByName(String name) {
        try {
            if (StrUtil.isEmpty(name)) {
                return null;
            }
            return JoinTypeEnum.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static JoinTypeEnum getByType(Integer joinType) {
        for (JoinTypeEnum joinTypeEnum : JoinTypeEnum.values()) {
            if (joinTypeEnum.getType().equals(joinType)) {
                return joinTypeEnum;
            }
        }
        return null;
    }
}
