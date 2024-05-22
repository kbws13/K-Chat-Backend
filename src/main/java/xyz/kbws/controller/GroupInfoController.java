package xyz.kbws.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import xyz.kbws.annotation.AuthCheck;
import xyz.kbws.common.BaseResponse;
import xyz.kbws.common.ErrorCode;
import xyz.kbws.common.ResultUtils;
import xyz.kbws.exception.BusinessException;
import xyz.kbws.model.entity.GroupInfo;
import xyz.kbws.model.entity.UserContact;
import xyz.kbws.model.enums.GroupStatusEnum;
import xyz.kbws.model.enums.UserContactStatusEnum;
import xyz.kbws.model.enums.UserContactTypeEnum;
import xyz.kbws.model.vo.GroupInfoVO;
import xyz.kbws.model.vo.UserContactVO;
import xyz.kbws.model.vo.UserVO;
import xyz.kbws.service.GroupInfoService;
import xyz.kbws.service.UserContactService;
import xyz.kbws.utils.JwtUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;

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
    private UserContactService userContactService;

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
                                         MultipartFile avatarCover) throws IOException {
        UserVO userVOByToken = jwtUtils.getUserVOByToken(request);
        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setId(id);
        groupInfo.setName(name);
        groupInfo.setOwnerId(userVOByToken.getUserId());
        groupInfo.setNotice(notice);
        groupInfo.setJoinType(joinType);
        groupInfoService.save(groupInfo, avatarFile, avatarCover);
        return ResultUtils.success("添加成功");
    }

    @ApiOperation(value = "获取我创建的群组")
    @GetMapping("/getMyGroup")
    @AuthCheck
    public BaseResponse<List<GroupInfo>> getMyGroup(HttpServletRequest request) {
        UserVO userVOByToken = jwtUtils.getUserVOByToken(request);
        QueryWrapper<GroupInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ownerId", userVOByToken.getUserId()).orderByDesc("createTime");
        List<GroupInfo> res = groupInfoService.list(queryWrapper);
        return ResultUtils.success(res);
    }

    @ApiOperation(value = "获取群组详情")
    @PostMapping("/getGroupInfo")
    @AuthCheck
    public BaseResponse<GroupInfo> getGroupInfo(HttpServletRequest request, @NotEmpty String groupId) {
        GroupInfo groupInfo = getGroupDetailCommon(request, groupId);
        QueryWrapper<UserContact> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("contactId", groupId);
        Integer memberCount = Math.toIntExact(userContactService.count(queryWrapper));
        groupInfo.setMemberCount(memberCount);
        return ResultUtils.success(groupInfo);
    }

    private GroupInfo getGroupDetailCommon(HttpServletRequest request, String groupId) {
        UserVO userVOByToken = jwtUtils.getUserVOByToken(request);
        QueryWrapper<UserContact> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userVOByToken.getUserId()).eq("contactId", groupId);
        UserContact userContact = userContactService.getOne(queryWrapper);
        if (userContact == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "你不在群聊或者群聊不存在或已解散");
        }
        GroupInfo groupInfo = groupInfoService.getById(groupId);
        if (groupInfo == null || !GroupStatusEnum.NORMAL.getStatus().equals(groupInfo.getStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "群聊不存在或已解散");
        }
        return groupInfo;
    }

    @ApiOperation(value = "获取聊天会话群聊详情")
    @GetMapping("/getGroupInfo4Chat/{groupId}")
    @AuthCheck
    public BaseResponse<GroupInfoVO> getGroupInfo4Chat(HttpServletRequest request, @PathVariable("groupId") String groupId) {
        GroupInfo groupInfo = getGroupDetailCommon(request, groupId);
        List<UserContactVO> userContactList = userContactService.listUsers(groupId);
        GroupInfoVO groupInfoVO = new GroupInfoVO();
        groupInfoVO.setGroupInfo(groupInfo);
        groupInfoVO.setUserContactList(userContactList);
        return ResultUtils.success(groupInfoVO);
    }
}
