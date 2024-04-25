package xyz.kbws.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.kbws.common.BaseResponse;
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
}
