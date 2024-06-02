package xyz.kbws.model.vo;

import lombok.Data;
import xyz.kbws.model.enums.UserContactApplyStatusEnum;

import java.io.Serializable;

/**
 * @author kbws
 * @date 2024/5/25
 * @description:
 */
@Data
public class UserContactApplyVO implements Serializable {

    /**
     * 自增id
     */
    private Integer id;

    /**
     * 申请人id
     */
    private String applyId;

    /**
     * 接收人id
     */
    private String receiveId;

    /**
     * 联系人类型 0:好友 1:群组
     */
    private Integer contactType;

    /**
     * 联系人或者群组id
     */
    private String contactId;

    /**
     * 最后申请时间
     */
    private Long lastApplyTime;

    /**
     * 状态 0:待处理 1:已同意 2:已拒绝 3:已拉黑
     */
    private Integer status;

    /**
     * 申请信息
     */
    private String applyInfo;

    /**
     * 申请人/群名
     */
    private String contactName;

    /**
     * 状态名字
     */
    private String statusName;

    public String getStatusName() {
        UserContactApplyStatusEnum statusEnum = UserContactApplyStatusEnum.getByStatus(status);
        return statusEnum == null ? null : statusEnum.getDesc();
    }

    private static final long serialVersionUID = -1307085639931116354L;
}
