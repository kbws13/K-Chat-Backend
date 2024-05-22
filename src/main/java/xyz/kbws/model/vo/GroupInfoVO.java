package xyz.kbws.model.vo;

import lombok.Data;
import xyz.kbws.model.entity.GroupInfo;

import java.util.List;

/**
 * @author kbws
 * @date 2024/5/4
 * @description:
 */
@Data
public class GroupInfoVO {

    private GroupInfo groupInfo;

    private List<UserContactVO> userContactList;
}
