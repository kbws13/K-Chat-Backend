package xyz.kbws.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ArrayUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import xyz.kbws.annotation.AuthCheck;
import xyz.kbws.common.*;
import xyz.kbws.exception.BusinessException;
import xyz.kbws.exception.ThrowUtils;
import xyz.kbws.model.dto.userContact.UserContactAddRequest;
import xyz.kbws.model.dto.userContact.UserContactQueryDTO;
import xyz.kbws.model.dto.userContactApply.UserContactApplyDealWithRequest;
import xyz.kbws.model.entity.User;
import xyz.kbws.model.entity.UserContact;
import xyz.kbws.model.entity.UserContactApply;
import xyz.kbws.model.enums.UserContactStatusEnum;
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

    @ApiOperation(value = "获取联系人列表")
    @GetMapping("/myContact/{contactType}")
    @AuthCheck
    public BaseResponse<List<UserContactVO>> myContact(HttpServletRequest request, @PathVariable("contactType") String contactType) {
        UserContactTypeEnum contactTypeEnum = UserContactTypeEnum.getByName(contactType);
        if (contactTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserVO userVOByToken = jwtUtils.getUserVOByToken(request);
        UserContactQueryDTO userContactQueryDTO = new UserContactQueryDTO();
        userContactQueryDTO.setUserId(userVOByToken.getUserId());
        userContactQueryDTO.setCategoryType(contactTypeEnum.getType());
        if (UserContactTypeEnum.USER == contactTypeEnum) {
            userContactQueryDTO.setQueryContactUserInfo(true);
        } else if (UserContactTypeEnum.GROUP == contactTypeEnum) {
            userContactQueryDTO.setQueryGroupInfo(true);
            userContactQueryDTO.setExcludeMyGroup(true);
        }
        userContactQueryDTO.setOrderBy("updateTime desc");
        userContactQueryDTO.setStatusArray(new Integer[]{
                UserContactStatusEnum.FRIEND.getStatus(),
                UserContactStatusEnum.DEL_BE.getStatus(),
                UserContactStatusEnum.BLACKLIST_BE.getStatus(),
        });
        List<UserContactVO> userContactVOS = userContactService.listByParam(userContactQueryDTO);
        return ResultUtils.success(userContactVOS);
    }

    @ApiOperation(value = "获取其他用户信息")
    @GetMapping("/getUserInfo/{contactId}")
    @AuthCheck
    public BaseResponse<UserVO> getUserInfo(@PathVariable("contactId") String contactId, HttpServletRequest request) {
        UserVO userVOByToken = jwtUtils.getUserVOByToken(request);
        User user = userService.getById(contactId);
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        userVO.setContactStatus(UserContactStatusEnum.NOT_FRIEND.getStatus());

        QueryWrapper<UserContact> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userVOByToken.getUserId()).eq("contactId", contactId);
        UserContact userContact = userContactService.getOne(queryWrapper);
        if (userContact != null) {
            userVO.setContactStatus(UserContactStatusEnum.FRIEND.getStatus());
        }
        return ResultUtils.success(userVO);
    }

    @ApiOperation(value = "获取联系人信息")
    @GetMapping("/getContactInfo/{contactId}")
    @AuthCheck
    public BaseResponse<UserVO> getContactInfo(@PathVariable("contactId") String contactId, HttpServletRequest request) {
        UserVO userVOByToken = jwtUtils.getUserVOByToken(request);
        QueryWrapper<UserContact> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userVOByToken.getUserId()).eq("contactId", contactId);
        UserContact userContact = userContactService.getOne(queryWrapper);
        if (userContact == null || !ArrayUtil.contains(new Integer[] {
                UserContactStatusEnum.FRIEND.getStatus(),
                UserContactStatusEnum.DEL_BE.getStatus(),
                UserContactStatusEnum.BLACKLIST_BE.getStatus()
        }, userContact.getStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }

        User user = userService.getById(contactId);
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);

        return ResultUtils.success(userVO);
    }

    @ApiOperation(value = "删除联系人")
    @PostMapping("/deleteContact")
    @AuthCheck
    public BaseResponse<String> deleteContact(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        UserVO userVOByToken = jwtUtils.getUserVOByToken(request);
        userContactService.updateContactStatus(userVOByToken.getUserId(), deleteRequest.getId(), UserContactStatusEnum.DEL);
        return ResultUtils.success("删除成功");
    }

    @ApiOperation(value = "拉黑联系人")
    @PostMapping("/blackContact")
    @AuthCheck
    public BaseResponse<String> blackContact(@RequestBody DeleteRequest blackRequest, HttpServletRequest request) {
        UserVO userVOByToken = jwtUtils.getUserVOByToken(request);
        userContactService.updateContactStatus(userVOByToken.getUserId(), blackRequest.getId(), UserContactStatusEnum.BLACKLIST);
        return ResultUtils.success("拉黑成功");
    }
}
