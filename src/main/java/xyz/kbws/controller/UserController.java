package xyz.kbws.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.crypto.SecureUtil;
import com.wf.captcha.ArithmeticCaptcha;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import xyz.kbws.annotation.AuthCheck;
import xyz.kbws.common.BaseResponse;
import xyz.kbws.common.ErrorCode;
import xyz.kbws.common.ResultUtils;
import xyz.kbws.common.SysSetting;
import xyz.kbws.constant.RedisConstant;
import xyz.kbws.exception.BusinessException;
import xyz.kbws.exception.ThrowUtils;
import xyz.kbws.model.dto.user.UserLoginRequest;
import xyz.kbws.model.dto.user.UserRegisterRequest;
import xyz.kbws.model.dto.user.UserUpdateRequest;
import xyz.kbws.model.entity.User;
import xyz.kbws.model.vo.CheckCodeVO;
import xyz.kbws.model.vo.UserVO;
import xyz.kbws.redis.RedisComponent;
import xyz.kbws.redis.RedisUtils;
import xyz.kbws.service.UserService;
import xyz.kbws.utils.JwtUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

/**
 * @author kbws
 * @date 2024/4/24
 * @description: 用户接口
 */
@Api(tags = "用户接口")
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Resource
    private RedisUtils<String> redisUtils;

    @Resource
    private UserService userService;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private JwtUtils jwtUtils;

    @ApiOperation(value = "生成验证码")
    @GetMapping("/checkCode")
    public BaseResponse<CheckCodeVO> checkCode() {
        // 生成图片验证码
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(100, 43);
        String code = captcha.text();
        String checkCodeKey = UUID.randomUUID().toString();
        redisUtils.setex(RedisConstant.CHECK_CODE + checkCodeKey, code, RedisConstant.TIME_1MIN * 10);
        String checkCodeBase64 = captcha.toBase64();
        CheckCodeVO checkCodeVO = new CheckCodeVO();
        checkCodeVO.setCheckCode(checkCodeBase64);
        checkCodeVO.setCheckCodeKey(checkCodeKey);
        return ResultUtils.success(checkCodeVO);
    }

    @ApiOperation(value = "用户注册")
    @PostMapping("/register")
    public BaseResponse<String> userRegister(@Validated @RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        try {
            if (!userRegisterRequest.getCheckCode().equals(redisUtils.get(RedisConstant.CHECK_CODE + userRegisterRequest.getCheckCodeKey()))) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片验证码不正确");
            }
            return userService.userRegister(userRegisterRequest.getEmail(), userRegisterRequest.getNickName(), userRegisterRequest.getPassword());
        } finally {
            redisUtils.delete(RedisConstant.CHECK_CODE + userRegisterRequest.getCheckCodeKey());
        }
    }

    @ApiOperation(value = "用户登录")
    @PostMapping("/login")
    public BaseResponse<UserVO> userLogin(@Validated @RequestBody UserLoginRequest userLoginRequest) {
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        try {
            String checkCode = redisUtils.get(RedisConstant.CHECK_CODE + userLoginRequest.getCheckCodeKey());
            if (!userLoginRequest.getCheckCode().equals(checkCode) & userLoginRequest.getCheckCodeKey().equals("test")) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片验证码不正确");
            }
            UserVO userVO = userService.userLogin(userLoginRequest.getEmail(), userLoginRequest.getPassword());
            return ResultUtils.success(userVO);
        } finally {
            redisUtils.delete(RedisConstant.CHECK_CODE + userLoginRequest.getCheckCodeKey());
        }
    }

    @ApiOperation(value = "退出登录")
    @GetMapping("/logout")
    public BaseResponse<String> logout(HttpServletRequest request) {
        // TODO 退出登录 关闭 WebSocket 连接
        return ResultUtils.success("退出登录成功");
    }

    @ApiOperation(value = "修改用户信息")
    @PostMapping("/updateUser")
    @AuthCheck
    public BaseResponse<UserVO> updateUserInfo(@RequestBody UserUpdateRequest userUpdateRequest, HttpServletRequest request) {
        UserVO userVOByToken = jwtUtils.getUserVOByToken(request);
        userUpdateRequest.setUserId(userVOByToken.getUserId());
        userService.updateUserInfo(userUpdateRequest);
        return getCurrentUser(request);
    }

    @ApiOperation(value = "获取当前登录用户")
    @GetMapping("/getCurrentUser")
    @AuthCheck
    public BaseResponse<UserVO> getCurrentUser(HttpServletRequest request) {
        UserVO userVOByToken = jwtUtils.getUserVOByToken(request);
        User user = userService.getById(userVOByToken.getUserId());
        BeanUtil.copyProperties(user, userVOByToken);
        return ResultUtils.success(userVOByToken);
    }

    @ApiOperation(value = "修改用户密码")
    @PostMapping("/updatePwd")
    public BaseResponse<String> updatePassword(HttpServletRequest request, @RequestParam String password) {
        UserVO userVOByToken = jwtUtils.getUserVOByToken(request);
        User user = userService.getById(userVOByToken.getUserId());
        user.setPassword(SecureUtil.md5(password));
        userService.updateById(user);
        // TODO 强制退出，重新登录
        return ResultUtils.success("修改成功，请重新登录");
    }

    @ApiOperation(value = "获取系统设置")
    @GetMapping("/sysSetting")
    @AuthCheck
    public BaseResponse<SysSetting> getSysSetting() {
        SysSetting sysSetting = redisComponent.getSysSetting();
        return ResultUtils.success(sysSetting);
    }

}
