package xyz.kbws.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.kbws.annotation.AuthCheck;
import xyz.kbws.common.BaseResponse;
import xyz.kbws.common.DeleteRequest;
import xyz.kbws.common.ErrorCode;
import xyz.kbws.common.ResultUtils;
import xyz.kbws.constant.UserConstant;
import xyz.kbws.exception.BusinessException;
import xyz.kbws.model.dto.user.UserQueryRequest;
import xyz.kbws.model.dto.user.UserUpdateStatusRequest;
import xyz.kbws.model.entity.User;
import xyz.kbws.model.enums.UserRoleEnum;
import xyz.kbws.service.UserService;

import javax.annotation.Resource;

/**
 * @author kbws
 * @date 2024/6/22
 * @description: 管理员接口
 */
@Slf4j
@Api(tags = "管理员接口")
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Resource
    private UserService userService;

    @ApiOperation(value = "加载用户列表")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/loadUser")
    public BaseResponse<Page<User>> loadUser(@RequestBody UserQueryRequest userQueryRequest) {
        int current = userQueryRequest.getCurrent();
        int pageSize = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, pageSize), userService.getQueryWrapper(userQueryRequest));
        return ResultUtils.success(userPage);
    }

    @ApiOperation(value = "更新用户状态")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/updateUserStatus")
    public BaseResponse<String> updateUserStatus(@RequestBody UserUpdateStatusRequest userUpdateStatusRequest) {
        User user = userService.getById(userUpdateStatusRequest.getUserId());
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该用户不存在");
        }
        user.setStatus(userUpdateStatusRequest.getStatus());
        userService.updateById(user);
        return ResultUtils.success("修改成功");
    }

    @ApiOperation(value = "强制下线")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/forceOffLine")
    public BaseResponse<String> forceOffLine(@RequestBody DeleteRequest deleteRequest) {
        userService.forceOffLine(deleteRequest.getId());
        return ResultUtils.success("强制下线成功");
    }
}
