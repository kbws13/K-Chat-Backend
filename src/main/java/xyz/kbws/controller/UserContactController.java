package xyz.kbws.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xyz.kbws.annotation.AuthCheck;
import xyz.kbws.common.BaseResponse;
import xyz.kbws.common.ResultUtils;
import xyz.kbws.model.vo.UserContactSearchResultVO;
import xyz.kbws.service.UserContactApplyService;
import xyz.kbws.service.UserContactService;
import xyz.kbws.service.UserService;
import xyz.kbws.utils.JwtUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author kbws
 * @date 2024/5/22
 * @description:
 */
@Api(tags = "联系人/群组接口")
@Slf4j
@RestController
@RequestMapping("/contact")
public class UserContactController {

    @Resource
    private UserContactService userContactService;

    @Resource
    private UserService userService;

    @Resource
    private UserContactApplyService userContactApplyService;

    @Resource
    private JwtUtils jwtUtils;

    @ApiOperation(value = "搜索联系人/群组")
    @PostMapping("/search")
    @AuthCheck
    public BaseResponse<UserContactSearchResultVO> search(HttpServletRequest request, @RequestParam String contactId) {
        String userId = jwtUtils.getUserId(request);
        UserContactSearchResultVO userContactSearchResultVO = userContactService.searchContact(userId, contactId);
        return ResultUtils.success(userContactSearchResultVO);
    }

}
