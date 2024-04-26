package xyz.kbws.aop;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import xyz.kbws.annotation.AuthCheck;
import xyz.kbws.common.ErrorCode;
import xyz.kbws.exception.BusinessException;
import xyz.kbws.exception.ThrowUtils;
import xyz.kbws.model.enums.UserRoleEnum;
import xyz.kbws.model.vo.UserVO;
import xyz.kbws.utils.JwtUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author kbws
 * @date 2024/4/26
 * @description: 权限校验 AOP
 */
@Aspect
@Component
@Slf4j
public class AuthInterceptor {

    @Resource
    private JwtUtils jwtUtils;

    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        UserVO userVOByToken;
        // 必须得登录才能通过
        if (authCheck.checkLogin() || StrUtil.isNotBlank(mustRole)) {
            userVOByToken = jwtUtils.getUserVOByToken(request);
            ThrowUtils.throwIf(userVOByToken == null, ErrorCode.NOT_LOGIN_ERROR);
        }
        // 必须有该权限才能通过
        if (StrUtil.isNotBlank(mustRole)) {
            UserRoleEnum mustUserRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
            if (mustUserRoleEnum == null) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
            String userRole = jwtUtils.getUserRole(request);
            // 必须有管理员权限
            if (UserRoleEnum.ADMIN.equals(mustUserRoleEnum)) {
                if (!mustRole.equals(userRole)) {
                    log.error("权限不足");
                    throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
                }
            }
        }
        return joinPoint.proceed();
    }
}
