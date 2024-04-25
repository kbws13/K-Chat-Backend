package xyz.kbws.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import xyz.kbws.constant.RedisConstant;

import java.io.Serializable;

/**
 * @author kbws
 * @date 2024/4/25
 * @description: 系统设置
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SysSetting implements Serializable {

    /**
     * 最大群组数
     */
    private Integer maxGroupCount = 5;

    /**
     * 群组最大人数
     */
    private Integer maxGroupMemberCount = 500;

    /**
     * 图片大小
     */
    private Integer maxImageSize = 2;

    /**
     * 视频大小
     */
    private Integer maxVideoSize = 5;

    /**
     * 文件大小
     */
    private Integer maxFileSize = 5;

    /**
     * 机器人ID
     */
    private String robotUid = RedisConstant.ROBOT_UID;

    /**
     * 机器人昵称
     */
    private String robotNickName = "K-Chat";

    /**
     * 欢迎语
     */
    private String robotWelcome = "欢迎使用K-Chat";

    private static final long serialVersionUID = -8972208573098240238L;
}
