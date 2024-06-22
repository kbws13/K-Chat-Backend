package xyz.kbws.model.dto.user;

import lombok.Data;
import xyz.kbws.common.PageRequest;

import java.io.Serializable;

/**
 * @author kbws
 * @date 2024/6/22
 * @description: 用户分页查询请求类
 */
@Data
public class UserQueryRequest extends PageRequest implements Serializable {

    /**
     * 用户ID
     */
    private String userId;

    private String userIdFuzzy;

    /**
     * 邮箱
     */
    private String email;

    private String emailFuzzy;

    /**
     * 昵称
     */
    private String nickName;

    private String nickNameFuzzy;

    /**
     * 0:直接加入 1:同意后加好友
     */
    private Integer joinType;

    /**
     * 0:女 1:男
     */
    private Integer sex;

    /**
     * 密码
     */
    private String password;

    private String passwordFuzzy;

    /**
     * 个性签名
     */
    private String personalSignature;

    private String personalSignatureFuzzy;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 创建时间
     */
    private String createTime;

    private String createTimeStart;

    private String createTimeEnd;

    /**
     * 最后登录时间
     */
    private Long lastLoginTime;

    /**
     * 省份
     */
    private String areaName;

    private String areaNameFuzzy;

    /**
     * 城市
     */
    private String areaCode;

    private String areaCodeFuzzy;

    /**
     * 最后离开时间
     */
    private Long lastOffTime;

    private static final long serialVersionUID = -7496263403520148269L;
}
