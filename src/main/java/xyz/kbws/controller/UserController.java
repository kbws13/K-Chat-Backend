package xyz.kbws.controller;

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
import xyz.kbws.model.vo.CheckCodeVO;
import xyz.kbws.model.vo.UserVO;
import xyz.kbws.redis.RedisComponent;
import xyz.kbws.redis.RedisUtils;
import xyz.kbws.service.UserService;

import javax.annotation.Resource;
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

    @ApiOperation(value = "获取系统设置")
    @GetMapping("/sysSetting")
    @AuthCheck
    public BaseResponse<SysSetting> getSysSetting() {
        SysSetting sysSetting = redisComponent.getSysSetting();
        return ResultUtils.success(sysSetting);
    }

}
