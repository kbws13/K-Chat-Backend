package xyz.kbws.model.enums;

import lombok.Getter;

/**
 * @author kbws
 * @date 2024/6/25
 * @description: 应用更新发布状态
 */
@Getter
public enum AppUpdateSatusEnum {
    INIT(0, "未发布"),
    GRAYSCALE(1, "灰度发布"),
    ALL(2, "全网发布");

    private final Integer status;
    private final String description;

    AppUpdateSatusEnum(int status, String description) {
        this.status = status;
        this.description = description;
    }

    public static AppUpdateSatusEnum getByStatus(Integer status) {
        for (AppUpdateSatusEnum at : AppUpdateSatusEnum.values()) {
            if (at.status.equals(status)) {
                return at;
            }
        }
        return null;
    }
}
