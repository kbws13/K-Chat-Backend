package xyz.kbws.model.dto.userBeauty;

import lombok.Data;

import java.io.Serializable;

/**
 * @author kbws
 * @date 2024/6/24
 * @description: 靓号添加请求
 */
@Data
public class UserBeautyAddDTO implements Serializable {

    private Integer id;

    /**
     * 邮箱
     */
    private String email;
    /**
     * 用户id
     */
    private String userId;

    private static final long serialVersionUID = 4957072804586054780L;
}
