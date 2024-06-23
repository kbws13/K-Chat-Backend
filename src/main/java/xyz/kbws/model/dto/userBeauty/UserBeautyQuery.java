package xyz.kbws.model.dto.userBeauty;

import lombok.Data;
import xyz.kbws.common.PageRequest;

import java.io.Serializable;

/**
 * @author kbws
 * @date 2024/6/23
 * @description: 靓号查询分页查询
 */
@Data
public class UserBeautyQuery extends PageRequest implements Serializable {

    /**
     * 邮箱
     */
    private String email;

    private String emailFuzzy;

    /**
     * 用户ID
     */
    private String userId;

    private String userIdFuzzy;

    /**
     * 状态0:未使用 1:已使用
     */
    private Integer status;

    private static final long serialVersionUID = -2433263824183421430L;
}
