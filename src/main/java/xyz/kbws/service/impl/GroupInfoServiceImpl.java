package xyz.kbws.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
import xyz.kbws.model.entity.GroupInfo;
import xyz.kbws.model.entity.UserContact;
import xyz.kbws.model.enums.UserContactStatusEnum;
import xyz.kbws.model.enums.UserContactTypeEnum;
import xyz.kbws.redis.RedisComponent;
import xyz.kbws.service.GroupInfoService;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
* @author hsy
* @description 针对表【group_info(群组表)】的数据库操作Service实现
* @createDate 2024-04-26 14:51:47
*/
@Service
public class GroupInfoServiceImpl extends ServiceImpl<GroupInfoMapper, GroupInfo>
    implements GroupInfoService{

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
}




