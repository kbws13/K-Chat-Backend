package xyz.kbws.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import xyz.kbws.annotation.AuthCheck;
import xyz.kbws.common.BaseResponse;
import xyz.kbws.common.ErrorCode;
import xyz.kbws.common.PageRequest;
import xyz.kbws.common.ResultUtils;
import xyz.kbws.exception.BusinessException;
import xyz.kbws.exception.ThrowUtils;
import xyz.kbws.model.dto.userContact.UserContactAddRequest;
import xyz.kbws.model.dto.userContactApply.UserContactApplyDealWithRequest;
import xyz.kbws.model.entity.UserContactApply;
import xyz.kbws.model.enums.UserContactTypeEnum;
import xyz.kbws.model.vo.UserContactApplyVO;
import xyz.kbws.model.vo.UserContactSearchResultVO;
import xyz.kbws.model.vo.UserContactVO;
import xyz.kbws.model.vo.UserVO;
import xyz.kbws.service.UserContactApplyService;
import xyz.kbws.service.UserContactService;
import xyz.kbws.service.UserService;
import xyz.kbws.utils.JwtUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author kbws
 * @date 2024/5/22
 * @description:
 */
@Api(tags = "联系人-群组接口")
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

    @ApiOperation(value = "申请")
    @PostMapping("/applyAdd")
    @AuthCheck
    public BaseResponse<Integer> add(HttpServletRequest request, @RequestBody UserContactAddRequest userContactAddRequest) {
        UserVO userVOByToken = jwtUtils.getUserVOByToken(request);
        Integer joinType = userContactService.apply(userVOByToken, userContactAddRequest.getContactId(), userContactAddRequest.getApplyMessage());
        return ResultUtils.success(joinType);
    }

    @ApiOperation(value = "加载申请列表")
    @PostMapping("/loadApply")
    @AuthCheck
    public BaseResponse<Page<UserContactApplyVO>> loadApply(HttpServletRequest request, @RequestBody PageRequest pageRequest) {
        if (pageRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int current = pageRequest.getCurrent();
        int pageSize = pageRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR);
        String receiveId = jwtUtils.getUserId(request);
        QueryWrapper<UserContactApply> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("receiveId", receiveId).orderByDesc("lastApplyTime");
        Page<UserContactApply> userContactApplyPage = userContactApplyService.page(new Page<>(current, pageSize), queryWrapper);
        Page<UserContactApplyVO> userContactApplyVOPage = new Page<>(current, pageSize, userContactApplyPage.getTotal());
        List<UserContactApplyVO> userContactApplyVO = userContactApplyService.getUserContactApplyVO(receiveId);
        userContactApplyVOPage.setRecords(userContactApplyVO);
        return ResultUtils.success(userContactApplyVOPage);
    }


    @ApiOperation(value = "处理申请")
    @PostMapping("/dealWithApply")
    public BaseResponse<String> dealWithApply(HttpServletRequest request, @RequestBody UserContactApplyDealWithRequest dealWithRequest) {
        ThrowUtils.throwIf(dealWithRequest == null, ErrorCode.PARAMS_ERROR);
        String userId = jwtUtils.getUserId(request);
        userContactApplyService.dealWithApply(userId, dealWithRequest.getApplyId(), dealWithRequest.getStatus());
        return ResultUtils.success("操作成功");
    }

    @ApiOperation(value = "获取联系人")
    @GetMapping("/myContact/{contactType}")
    @AuthCheck
    public BaseResponse<List<UserContactVO>> myContact(HttpServletRequest request, @PathVariable("contactType") String contactType) {
        UserContactTypeEnum contactTypeEnum = UserContactTypeEnum.getByName(contactType);
        if (contactTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserVO userVOByToken = jwtUtils.getUserVOByToken(request);

        return ResultUtils.success(null);
    }
}
