package xyz.kbws.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import xyz.kbws.common.ErrorCode;
import xyz.kbws.common.SysSetting;
import xyz.kbws.config.AppConfig;
import xyz.kbws.constant.CommonConstant;
import xyz.kbws.constant.FileConstant;
import xyz.kbws.exception.BusinessException;
import xyz.kbws.mapper.GroupInfoMapper;
import xyz.kbws.mapper.UserContactMapper;
import xyz.kbws.model.dto.group.GroupInfoQueryDTO;
import xyz.kbws.model.entity.GroupInfo;
import xyz.kbws.model.entity.User;
import xyz.kbws.model.entity.UserContact;
import xyz.kbws.model.enums.GroupStatusEnum;
import xyz.kbws.model.enums.UserContactStatusEnum;
import xyz.kbws.model.enums.UserContactTypeEnum;
import xyz.kbws.redis.RedisComponent;
import xyz.kbws.service.GroupInfoService;
import xyz.kbws.service.UserContactService;
import xyz.kbws.service.UserService;
import xyz.kbws.utils.SqlUtils;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
* @author hsy
* @description 针对表【group_info(群组表)】的数据库操作Service实现
* @createDate 2024-04-26 14:51:47
*/
@Service
public class GroupInfoServiceImpl extends ServiceImpl<GroupInfoMapper, GroupInfo>
    implements GroupInfoService{

    @Resource
    private UserService userService;

    @Resource
    private UserContactService userContactService;

    @Resource
    private GroupInfoMapper groupInfoMapper;

    @Resource
    private UserContactMapper userContactMapper;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private AppConfig appConfig;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void save(GroupInfo groupInfo, MultipartFile avatarFile, MultipartFile avatarCover) throws IOException {
        Date date = DateUtil.date();
        // 新增
        if (StrUtil.isEmpty(groupInfo.getId())) {
            QueryWrapper<GroupInfo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("ownerId", groupInfo.getOwnerId());
            int count = Math.toIntExact(groupInfoMapper.selectCount(queryWrapper));
            SysSetting sysSetting = redisComponent.getSysSetting();
            if (count >= sysSetting.getMaxGroupCount()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "最多只能创建" + sysSetting.getMaxGroupCount() + "个群聊");
            }
            if (avatarFile == null) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR);
            }
            groupInfo.setCreateTime(date);
            groupInfo.setId(UserContactTypeEnum.GROUP.getPrefix() + RandomUtil.randomNumbers(CommonConstant.LENGTH_11));
            groupInfoMapper.insert(groupInfo);
            // 将群组添加到自己的联系人
            UserContact userContact = new UserContact();
            userContact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            userContact.setContactType(UserContactTypeEnum.GROUP.getType());
            userContact.setContactId(groupInfo.getId());
            userContact.setUserId(groupInfo.getOwnerId());
            userContact.setCreateTime(date);
            userContactMapper.insert(userContact);
            // TODO 创建会话
            // TODO 发送消息
        } else {
            GroupInfo dbInfo = groupInfoMapper.selectById(groupInfo.getId());
            if (!dbInfo.getOwnerId().equals(groupInfo.getOwnerId())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
            groupInfoMapper.updateById(groupInfo);
            // TODO 更新相关表的冗余信息

            // TODO 修改群昵称发送 WebSocket 信息
        }
        if (avatarFile == null) {
            return;
        }
        String baseFolder = appConfig.getProjectFolder() + FileConstant.FILE_FOLDER;
        File targetFileFolder = new File(baseFolder + FileConstant.AVATAR);
        if (!targetFileFolder.exists()) {
            targetFileFolder.mkdirs();
        }
        String filePath = targetFileFolder.getPath() + "/" + groupInfo.getId() + FileConstant.IMAGE_SUFFIX;
        avatarFile.transferTo(new File(filePath));
        avatarCover.transferTo(new File(targetFileFolder.getPath() + "/" + groupInfo.getId() + FileConstant.COVER_IMAGE_SUFFIX));
    }

    @Override
    public Page<GroupInfo> getGroupInfoByPage(GroupInfoQueryDTO groupInfoQueryDTO) {
        Boolean queryGroupOwnerName = groupInfoQueryDTO.getQueryGroupOwnerName();
        Boolean queryMemberCount = groupInfoQueryDTO.getQueryMemberCount();
        Page<GroupInfo> page = this.page(new Page<>(groupInfoQueryDTO.getCurrent(), groupInfoQueryDTO.getPageSize()), getQueryWrapper(groupInfoQueryDTO));
        List<GroupInfo> records = page.getRecords();
        if (queryGroupOwnerName) {
            for (GroupInfo record : records) {
                User user = userService.getById(record.getOwnerId());
                record.setGroupOwnerNickName(user.getNickName());
            }
        }
        if (queryMemberCount) {
            for (GroupInfo record : records) {
                QueryWrapper<UserContact> query = new QueryWrapper<>();
                query.eq("contactId", record.getId());
                List<UserContact> userContacts = userContactMapper.selectList(query);
                record.setMemberCount(userContacts.size());
            }
        }
        page.setRecords(records);
        return page;
    }

    @Override
    public QueryWrapper<GroupInfo> getQueryWrapper(GroupInfoQueryDTO groupInfoQueryDTO) {
        QueryWrapper<GroupInfo> queryWrapper = new QueryWrapper<>();
        String groupId = groupInfoQueryDTO.getGroupId();
        String groupIdFuzzy = groupInfoQueryDTO.getGroupIdFuzzy();
        String groupName = groupInfoQueryDTO.getGroupName();
        String groupNameFuzzy = groupInfoQueryDTO.getGroupNameFuzzy();
        String groupOwnerId = groupInfoQueryDTO.getGroupOwnerId();
        String groupOwnerIdFuzzy = groupInfoQueryDTO.getGroupOwnerIdFuzzy();
        String groupNotice = groupInfoQueryDTO.getGroupNotice();
        String groupNoticeFuzzy = groupInfoQueryDTO.getGroupNoticeFuzzy();
        Integer joinType = groupInfoQueryDTO.getJoinType();
        Integer status = groupInfoQueryDTO.getStatus();

        String sortField = groupInfoQueryDTO.getSortField();
        String sortOrder = groupInfoQueryDTO.getSortOrder();
        queryWrapper.eq(StringUtils.isNoneBlank(groupId), "id", groupId);
        queryWrapper.like(StringUtils.isNoneBlank(groupIdFuzzy), "id", groupId);
        queryWrapper.eq(StringUtils.isNoneBlank(groupName), "name", groupName);
        queryWrapper.like(StringUtils.isNoneBlank(groupNameFuzzy), "name", groupNameFuzzy);
        queryWrapper.eq(StringUtils.isNoneBlank(groupOwnerId), "ownerId", groupOwnerId);
        queryWrapper.like(StringUtils.isNoneBlank(groupOwnerIdFuzzy), "ownerId", groupOwnerIdFuzzy);
        queryWrapper.eq(StringUtils.isNoneBlank(groupNotice), "notice", groupNotice);
        queryWrapper.like(StringUtils.isNoneBlank(groupNoticeFuzzy), "notice", groupNoticeFuzzy);
        queryWrapper.eq(joinType != null, "joinType", joinType);
        queryWrapper.eq(status != null, "status", status);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return null;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void dissolutionGroup(String ownerId, String groupId) {
        GroupInfo dbInfo = this.getById(groupId);
        if (dbInfo == null || !dbInfo.getOwnerId().equals(ownerId)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        // 删除群组
        dbInfo.setStatus(GroupStatusEnum.DISSOLUTION.getStatus());
        this.updateById(dbInfo);

        // 更新联系人信息
        QueryWrapper<UserContact> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("contactId", groupId);
        List<UserContact> userContacts = userContactMapper.selectList(queryWrapper);
        for (UserContact userContact : userContacts) {
            userContact.setStatus(UserContactStatusEnum.DEL.getStatus());
        }
        userContactService.updateBatchById(userContacts);

        // TODO 移除相关群员的联系人缓存

        // TODO 发消息 1.更新会话消息 2.记录群消息 3.发生解散通知消息
    }
}




