package xyz.kbws.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import xyz.kbws.annotation.AuthCheck;
import xyz.kbws.common.BaseResponse;
import xyz.kbws.common.ResultUtils;
import xyz.kbws.model.vo.UserVO;
import xyz.kbws.service.GroupInfoService;
import xyz.kbws.utils.JwtUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author kbws
 * @date 2024/4/26
 * @description: 群组接口
 */
@Api(tags = "群组接口")
@RestController
@RequestMapping("/group")
@Slf4j
public class GroupInfoController {

    @Resource
    private GroupInfoService groupInfoService;

    @Resource
    private JwtUtils jwtUtils;

    @ApiOperation(value = "添加群组")
    @PostMapping("/add")
    @AuthCheck
    public BaseResponse<String> addGroup(HttpServletRequest request, String id,
                                         @NotEmpty String name,
                                         String notice,
                                         @NotNull Integer joinType,
                                         MultipartFile avatarFile,
                                         MultipartFile avatarCover) {
        UserVO userVOByToken = jwtUtils.getUserVOByToken(request);
        return ResultUtils.success("");
    }
}
