package xyz.kbws.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.multipart.MultipartFile;
import xyz.kbws.model.dto.group.GroupInfoQueryDTO;
import xyz.kbws.model.entity.GroupInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import xyz.kbws.model.enums.MessageTypeEnum;
import xyz.kbws.model.vo.UserVO;

import java.io.IOException;

/**
* @author hsy
* @description 针对表【group_info(群组表)】的数据库操作Service
* @createDate 2024-04-26 14:51:47
*/
public interface GroupInfoService extends IService<GroupInfo> {

    /**
     * 保存群组
     * @param groupInfo
     * @param avatarFile
     * @param avatarCover
     * @throws IOException
     */
    void save(GroupInfo groupInfo, MultipartFile avatarFile, MultipartFile avatarCover) throws IOException;

    /**
     * 分页查询
     * @param groupInfoQueryDTO
     * @return
     */
    Page<GroupInfo> getGroupInfoByPage(GroupInfoQueryDTO groupInfoQueryDTO);

    /**
     * 获取查询器
     * @param groupInfoQueryDTO
     * @return
     */
    QueryWrapper<GroupInfo> getQueryWrapper(GroupInfoQueryDTO groupInfoQueryDTO);

    /**
     * 退群
     * @param userId
     * @param groupId
     * @param messageTypeEnum
     */
    void leaveGroup(String userId, String groupId, MessageTypeEnum messageTypeEnum);

    /**
     * 解散群聊
     * @param ownerId
     * @param groupId
     */
    void dissolutionGroup(String ownerId, String groupId);

    /**
     * 添加或移除群组人员
     * @param userVO
     * @param groupId
     * @param contactIds
     * @param opType
     */
    void addOrRemoveGroupUser(UserVO userVO, String groupId, String contactIds, Integer opType);
}
