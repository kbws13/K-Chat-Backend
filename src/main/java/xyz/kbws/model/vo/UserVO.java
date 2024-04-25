package xyz.kbws.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author kbws
 * @date 2024/4/25
 * @description: 登录用户封装类
 */
@Data
public class UserVO implements Serializable {

    private static final long serialVersionUID = 3676406746132868131L;
    /**
     * 用户id
     */
    private String userId;
    /**
     * 昵称
     */
    private String nickName;
    /**
     * 性别：0：女 1：男
     */
    private Integer sex;
    /**
     * 0:直接加入 1:同意后加好友
     */
    private Integer joinType;
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
    /**
     * 状态
     */
    private Integer status;
    /**
     * token
     */
    private String token;
    private Integer contactStatus;
    /**
     * 用户身份
     */
    private String userRole;
}
