package xyz.kbws.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @author kbws
 * @date 2024/6/20
 * @description: 用户更新信息请求封装类
 */
@Data
public class UserUpdateRequest implements Serializable {

    /**
     * 用户id
     */
    private String userId;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 昵称
     */
    private String nickName;
    /**
     * 0:直接加入 1:同意后加好友
     */
    private Integer joinType;
    /**
     * 性别 0:男 1:女
     */
    private Integer sex;
    /**
     * 个性签名
     */
    private String personalSignature;
    /**
     * 地区
     */
    private String areaName;
    /**
     * 地区编号
     */
    private String areaCode;

    private static final long serialVersionUID = -8227236412637645687L;
}
