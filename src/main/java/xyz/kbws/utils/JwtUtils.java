package xyz.kbws.utils;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.signers.JWTSignerUtil;
import org.springframework.stereotype.Component;
import xyz.kbws.common.ErrorCode;
import xyz.kbws.constant.RedisConstant;
import xyz.kbws.exception.ThrowUtils;
import xyz.kbws.model.vo.UserVO;
import xyz.kbws.redis.RedisUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author kbws
 * @date 2024/4/25
 * @description: JWT工具类
 */
@Component
public class JwtUtils {

    @Resource
    private RedisUtils redisUtils;

    /**
     * 创建Token
     *
     * @param userId   用户id
     * @param userRole 用户角色
     * @return Token
     */
    public String createToken(String userId, String userRole) {
        return JWT.create()
                .setPayload("userId", userId)
                .setPayload("userRole", userRole)
                .setSigner(JWTSignerUtil.none())
                .sign();
    }

    public String getUserId(HttpServletRequest request) {
        String token = request.getHeader("token");
        verifyToken(token);
        JWT jwt = JWT.of(token);
        return (String) jwt.getPayload("userId");
    }

    public String getUserRole(HttpServletRequest request) {
        String token = request.getHeader("token");
        verifyToken(token);
        JWT jwt = JWT.of(token);
        return (String) jwt.getPayload("userRole");
    }

    public void verifyToken(String token) {
        boolean verify = JWT.of(token).verify();
        ThrowUtils.throwIf(!verify, ErrorCode.TOKEN_ERROR);
    }

    public UserVO getUserVOByToken(HttpServletRequest request) {
        String token = request.getHeader("token");
        return (UserVO) redisUtils.get(RedisConstant.WS_TOKEN + token);
    }
}
