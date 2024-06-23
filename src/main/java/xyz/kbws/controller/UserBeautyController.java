package xyz.kbws.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
import xyz.kbws.common.ResultUtils;
import xyz.kbws.constant.UserConstant;
import xyz.kbws.model.dto.userBeauty.UserBeautyQuery;
import xyz.kbws.model.entity.UserBeauty;
import xyz.kbws.service.UserBeautyService;
import xyz.kbws.utils.JwtUtils;

import javax.annotation.Resource;

/**
 * @author kbws
 * @date 2024/6/23
 * @description: 靓号管理
 */
@Api(tags = "靓号管理")
@Slf4j
@RestController
@RequestMapping("/userBeauty")
public class UserBeautyController {

    @Resource
    private UserBeautyService userBeautyService;

    @Resource
    private JwtUtils jwtUtils;

    @ApiOperation(value = "加载靓号列表")
    @PostMapping("/loadBeautyAccountList")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserBeauty>> loadBeautyAccountList(@RequestBody UserBeautyQuery userBeautyQuery) {
        int current = userBeautyQuery.getCurrent();
        int pageSize = userBeautyQuery.getPageSize();
        QueryWrapper<UserBeauty> queryWrapper = userBeautyService.getQueryWrapper(userBeautyQuery);
        Page<UserBeauty> page = userBeautyService.page(new Page<>(current, pageSize), queryWrapper);
        return ResultUtils.success(page);
    }
}
