package xyz.kbws.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import xyz.kbws.annotation.AuthCheck;
import xyz.kbws.common.BaseResponse;
import xyz.kbws.common.ResultUtils;
import xyz.kbws.common.SysSetting;
import xyz.kbws.constant.UserConstant;
import xyz.kbws.redis.RedisComponent;

import javax.annotation.Resource;

/**
 * @author kbws
 * @date 2024/6/25
 * @description: 系统设置接口
 */
@Slf4j
@Api(tags = "系统设置接口")
@RestController
@RequestMapping("/system")
public class SystemController {

    @Resource
    private RedisComponent redisComponent;

    @ApiOperation(value = "获取系统设置")
    @GetMapping("/get")
    @AuthCheck
    public BaseResponse<SysSetting> getSystemSetting() {
        SysSetting sysSetting = redisComponent.getSysSetting();
        return ResultUtils.success(sysSetting);
    }

    @ApiOperation(value = "保存系统设置")
    @PostMapping("/save")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<String> saveSystemSetting(@RequestBody SysSetting sysSetting) {
        redisComponent.saveSysSetting(sysSetting);
        return ResultUtils.success("保存系统设置成功");
    }
}
