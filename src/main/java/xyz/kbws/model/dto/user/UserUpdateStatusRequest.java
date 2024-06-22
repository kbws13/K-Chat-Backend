package xyz.kbws.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @author kbws
 * @date 2024/6/22
 * @description: 更新用户状态请求
 */
@Data
public class UserUpdateStatusRequest implements Serializable {

    private String userId;

    private Integer status;

    private static final long serialVersionUID = -6662676614623776459L;
}
