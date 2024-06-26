package xyz.kbws.redis;

import org.springframework.stereotype.Component;
import xyz.kbws.common.SysSetting;
import xyz.kbws.constant.RedisConstant;
import xyz.kbws.model.vo.UserVO;

import javax.annotation.Resource;

/**
 * @author kbws
 * @date 2024/4/25
 * @description:
 */
@Component
public class RedisComponent {

    @Resource
    private RedisUtils redisUtils;

    /**
     * 获取心跳
     *
     * @param userId 用户id
     * @return 时间戳
     */
    public Long getUserHeartBeat(String userId) {
        return (Long) redisUtils.get(RedisConstant.WS_USER_HEART_BEAT + userId);
    }

    /**
     * 保存最后的心跳时间
     * @param userId
     */
    public void saveUserHeartBeat(String userId) {
        redisUtils.setex(RedisConstant.WS_USER_HEART_BEAT + userId, System.currentTimeMillis(), RedisConstant.EXPIRES_HEART_BEAT);
    }

    /**
     * 保存登录信息
     *
     * @param userVO 用户登录封装类
     */
    public void saveTokenUserVO(UserVO userVO) {
        redisUtils.setex(RedisConstant.WS_TOKEN + userVO.getToken(), userVO, RedisConstant.TIME_1DAY * 2);
        redisUtils.setex(RedisConstant.WS_TOKEN_USERID + userVO.getUserId(), userVO.getToken(), RedisConstant.TIME_1DAY * 2);
    }

    public UserVO getTokenUserVO(String token) {
        return (UserVO) redisUtils.get(RedisConstant.WS_TOKEN + token);
    }

    public SysSetting getSysSetting() {
        SysSetting sysSetting = (SysSetting) redisUtils.get(RedisConstant.SYS_SETTING);
        sysSetting = sysSetting == null ? new SysSetting() : sysSetting;
        return sysSetting;
    }

    public void saveSysSetting(SysSetting sysSetting) {
        redisUtils.set(RedisConstant.SYS_SETTING, sysSetting);
    }
}
