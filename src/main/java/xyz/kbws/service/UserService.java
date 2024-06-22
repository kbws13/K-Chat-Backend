package xyz.kbws.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import xyz.kbws.common.BaseResponse;
import xyz.kbws.model.dto.user.UserQueryRequest;
import xyz.kbws.model.dto.user.UserUpdateRequest;
import xyz.kbws.model.entity.User;
import xyz.kbws.model.vo.UserVO;

/**
 * @author hsy
 * @description 针对表【user(用户表)】的数据库操作Service
 * @createDate 2024-04-24 14:39:58
 */
public interface UserService extends IService<User> {
    /**
     * 用户注册
     *
     * @param email    邮箱
     * @param nickName 昵称
     * @param password 密码
     * @return 返回结果
     */
    BaseResponse<String> userRegister(String email, String nickName, String password);

    /**
     * 用户登录
     *
     * @param email    邮箱
     * @param password 密码
     * @return 返回结果
     */
    UserVO userLogin(String email, String password);

    /**
     * 更新用户信息
     *
     * @param userUpdateRequest 用户更新请求
     */
    void updateUserInfo(UserUpdateRequest userUpdateRequest);

    /**
     * 强制下线
     * @param userId 用户id
     */
    void forceOffLine(String userId);

    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);
}
