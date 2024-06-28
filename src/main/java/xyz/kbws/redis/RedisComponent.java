package xyz.kbws.redis;

import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Component;
import xyz.kbws.common.SysSetting;
import xyz.kbws.constant.RedisConstant;
import xyz.kbws.model.vo.UserVO;

import javax.annotation.Resource;
import java.util.List;

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
     * 删除用户心跳
     * @param userId
     */
    public void removeUserHeartBeat(String userId) {
        redisUtils.delete(RedisConstant.WS_USER_HEART_BEAT + userId);
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

    public UserVO getTokenUserVOByUserId(String userId) {
        String token = (String) redisUtils.get(RedisConstant.WS_TOKEN_USERID + userId);
        return getTokenUserVO(token);
    }

    public void clearUserTokenByUserId(String userId) {
        String token = (String) redisUtils.get(RedisConstant.WS_TOKEN_USERID + userId);
        if (StrUtil.isEmpty(token)) {
            return;
        }
        redisUtils.delete(RedisConstant.WS_TOKEN);
    }

    public SysSetting getSysSetting() {
        SysSetting sysSetting = (SysSetting) redisUtils.get(RedisConstant.SYS_SETTING);
        sysSetting = sysSetting == null ? new SysSetting() : sysSetting;
        return sysSetting;
    }

    /**
     * 保存系统设置
     * @param sysSetting
     */
    public void saveSysSetting(SysSetting sysSetting) {
        redisUtils.set(RedisConstant.SYS_SETTING, sysSetting);
    }

    /**
     * 获取用户联系人
     * @param userId
     * @return
     */
    public List<String> getUserContactList(String userId) {
        return redisUtils.getQueueList(RedisConstant.USER_CONTACT + userId);
    }

    /**
     * 添加用户联系人
     * @param userId
     * @param contactId
     */
    public void addUserContact(String userId, String contactId) {
        List<String> contactList = redisUtils.getQueueList(RedisConstant.USER_CONTACT + userId);
        if (!contactList.contains(contactId)) {
            redisUtils.listPush(RedisConstant.USER_CONTACT + userId, contactId, RedisConstant.TIME_1DAY * 2);
        }
    }

    /**
     * 清空用户联系人
     * @param userId
     */
    public void cleanUserContact(String userId) {
        redisUtils.delete(RedisConstant.USER_CONTACT + userId);
    }

    /**
     * 删除用户联系人
     * @param userId
     * @param contactId
     */
    public void removeUserContact(String userId, String contactId) {
        redisUtils.remove(RedisConstant.USER_CONTACT + userId, contactId);
    }

    /**
     * 批量添加用户联系人
     * @param userId
     * @param contactIdList
     */
    public void addUserContactBatch(String userId, List<String> contactIdList) {
        redisUtils.listPushAll(RedisConstant.USER_CONTACT + userId, contactIdList, RedisConstant.TIME_1DAY * 2);
    }
}
