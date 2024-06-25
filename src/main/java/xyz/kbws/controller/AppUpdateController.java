package xyz.kbws.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import xyz.kbws.annotation.AuthCheck;
import xyz.kbws.common.BaseResponse;
import xyz.kbws.common.DeleteRequest;
import xyz.kbws.common.ErrorCode;
import xyz.kbws.common.ResultUtils;
import xyz.kbws.constant.AppUpdateConstant;
import xyz.kbws.constant.UserConstant;
import xyz.kbws.exception.BusinessException;
import xyz.kbws.model.dto.appUpdate.*;
import xyz.kbws.model.entity.AppUpdate;
import xyz.kbws.model.enums.AppUpdateFileTypeEnum;
import xyz.kbws.model.vo.AppUpdateVO;
import xyz.kbws.service.AppUpdateService;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author kbws
 * @date 2024/6/25
 * @description: APP更新接口
 */
@Slf4j
@Api(tags = "APP更新接口")
@RestController
@RequestMapping("/appUpdate")
public class AppUpdateController {

    @Resource
    private AppUpdateService appUpdateService;

    @ApiOperation(value = "检测版本")
    @PostMapping("/check")
    @AuthCheck
    public BaseResponse<AppUpdateVO> checkVersion(@RequestBody AppUpdateCheckDTO appUpdateCheckDTO) {
        if (StrUtil.isEmpty(appUpdateCheckDTO.getAppVersion())) {
            return ResultUtils.success(null);
        }
        QueryWrapper<AppUpdate> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id");
        AppUpdate appUpdate = appUpdateService.list(queryWrapper).get(0);
        if (appUpdate == null) {
            return ResultUtils.success(null);
        }
        AppUpdateVO appUpdateVO = new AppUpdateVO();
        BeanUtil.copyProperties(appUpdate, appUpdateVO);
        String fileName = AppUpdateConstant.APP_NAME + appUpdate.getVersion() + AppUpdateConstant.APP_EXE_SUFFIX;
        appUpdateVO.setFileName(fileName);
        return ResultUtils.success(appUpdateVO);
    }

    @ApiOperation(value = "获取更新列表")
    @GetMapping("/loadUpdateList")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<AppUpdate>> loadUpdateList(@RequestBody AppUpdateQueryDTO appUpdateQueryDTO) {
        int current = appUpdateQueryDTO.getCurrent();
        int pageSize = appUpdateQueryDTO.getPageSize();
        QueryWrapper<AppUpdate> queryWrapper = appUpdateService.getQueryWrapper(appUpdateQueryDTO);
        Page<AppUpdate> result = appUpdateService.page(new Page<>(current, pageSize), queryWrapper);
        return ResultUtils.success(result);
    }

    @ApiOperation(value = "添加更新")
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<String> addAppUpdate(@RequestBody AppUpdateAddDTO appUpdateAddDTO) {
        AppUpdateFileTypeEnum fileTypeEnum = AppUpdateFileTypeEnum.getByType(appUpdateAddDTO.getFileType());
        if (fileTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        checkVersion(appUpdateAddDTO.getVersion());
        AppUpdate appUpdate = new AppUpdate();
        BeanUtil.copyProperties(appUpdateAddDTO, appUpdate);
        appUpdateService.save(appUpdate);
        return ResultUtils.success("添加成功");
    }

    @ApiOperation(value = "修改更新")
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<String> modifyAppUpdate(@RequestBody AppUpdateUpdateDTO appUpdateUpdateDTO) {
        checkVersion(appUpdateUpdateDTO.getVersion());
        AppUpdate appUpdate = appUpdateService.getById(appUpdateUpdateDTO.getId());
        BeanUtil.copyProperties(appUpdateUpdateDTO, appUpdate);
        appUpdateService.updateById(appUpdate);
        return ResultUtils.success("修改成功");
    }

    @ApiOperation(value = "删除更新")
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<String> deleteAppUpdate(@RequestBody DeleteRequest deleteRequest) {
        AppUpdate appUpdate = appUpdateService.getById(Integer.parseInt(deleteRequest.getId()));
        if (appUpdate == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        appUpdateService.removeById(deleteRequest.getId());
        return ResultUtils.success("删除成功");
    }

    @ApiOperation(value = "发布更新")
    @PostMapping("/post")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<String> postAppUpdate(@RequestBody AppUpdatePostDTO appUpdatePostDTO) {
        appUpdateService.postUpdate(appUpdatePostDTO.getId(), appUpdatePostDTO.getStatus(), appUpdatePostDTO.getGrayscaleUid());
        return ResultUtils.success("发布成功");
    }


    private void checkVersion(String version) {
        QueryWrapper<AppUpdate> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id");
        List<AppUpdate> list = appUpdateService.list(queryWrapper);
        if (!list.isEmpty()) {
            AppUpdate appUpdate = list.get(0);
            Long dbVersion = Long.parseLong(appUpdate.getVersion().replace(".", ""));
            Long currentVersion = Long.parseLong(version.replace(".", ""));
            if (currentVersion < dbVersion) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "当前版本必须大于历史版本");
            }
            QueryWrapper<AppUpdate> query = new QueryWrapper<>();
            query.eq("version", version);
            AppUpdate one = appUpdateService.getOne(query);
            if (one != null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "版本号已存在");
            }
        }
    }
}
