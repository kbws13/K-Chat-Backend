package xyz.kbws.constant;

import xyz.kbws.model.enums.UserContactTypeEnum;

/**
 * @author kbws
 * @date 2024/4/24
 * @description: Redis 常量
 */
public interface RedisConstant {

    String CHECK_CODE = "kchat:checkcode:";

    String WS_USER_HEART_BEAT = "kchat:ws:user:heartbeat:";

    String WS_TOKEN = "kchat:ws:token:";

    String WS_TOKEN_USERID = "kchat:ws:token:userid:";

    String USER_CONTACT = "easychat:ws:user:contact:";

    String ROBOT_UID = UserContactTypeEnum.USER.getPrefix() + "robot";

    String SYS_SETTING = "kchat:syssetting:";

    Integer TIME_1MIN = 60;

    Integer TIME_1DAY = TIME_1MIN * 60 * 24;

    Integer EXPIRES_HEART_BEAT = 6;
}
