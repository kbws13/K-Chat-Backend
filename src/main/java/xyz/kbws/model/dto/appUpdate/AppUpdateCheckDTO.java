package xyz.kbws.model.dto.appUpdate;

import lombok.Data;

import java.io.Serializable;

/**
 * @author kbws
 * @date 2024/6/25
 * @description: 检测更新请求
 */
@Data
public class AppUpdateCheckDTO implements Serializable {

    private String uid;

    private String appVersion;

    private static final long serialVersionUID = -3164135485124712341L;
}
