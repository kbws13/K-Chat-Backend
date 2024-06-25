package xyz.kbws.model.dto.group;

import lombok.Data;
import xyz.kbws.common.PageRequest;

import java.io.Serializable;

/**
 * @author kbws
 * @date 2024/6/25
 * @description: 群组查找封装请求
 */
@Data
public class GroupInfoQueryDTO extends PageRequest implements Serializable {

    /**
     * 群ID
     */
    private String groupId;

    private String groupIdFuzzy;

    /**
     * 群组名
     */
    private String groupName;

    private String groupNameFuzzy;

    /**
     * 群主id
     */
    private String groupOwnerId;

    private String groupOwnerIdFuzzy;

    /**
     * 群公告
     */
    private String groupNotice;

    private String groupNoticeFuzzy;

    /**
     * 0:直接加入 1:管理员同意后加入
     */
    private Integer joinType;

    /**
     * 状态 1:正常 0:解散
     */
    private Integer status;

    private Boolean queryGroupOwnerName;

    private Boolean queryMemberCount;

    private static final long serialVersionUID = 8213645382600481595L;
}
