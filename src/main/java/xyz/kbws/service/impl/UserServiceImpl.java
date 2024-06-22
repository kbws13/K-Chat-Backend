package xyz.kbws.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.kbws.common.BaseResponse;
import xyz.kbws.common.ErrorCode;
import xyz.kbws.common.ResultUtils;
import xyz.kbws.constant.CommonConstant;
import xyz.kbws.exception.BusinessException;
import xyz.kbws.mapper.UserBeautyMapper;
import xyz.kbws.mapper.UserMapper;
import xyz.kbws.model.dto.user.UserQueryRequest;
import xyz.kbws.model.dto.user.UserUpdateRequest;
import xyz.kbws.model.entity.User;
import xyz.kbws.model.entity.UserBeauty;
import xyz.kbws.model.enums.BeautyAccountStatusEnum;
import xyz.kbws.model.enums.JoinTypeEnum;
import xyz.kbws.model.enums.UserContactTypeEnum;
import xyz.kbws.model.enums.UserStatusEnum;
import xyz.kbws.model.vo.UserVO;
import xyz.kbws.redis.RedisComponent;
import xyz.kbws.service.UserService;
import xyz.kbws.utils.JwtUtils;
import xyz.kbws.utils.SqlUtils;

import javax.annotation.Resource;

/**
 * @author hsy
 * @description 针对表【user(用户表)】的数据库操作Service实现
 * @createDate 2024-04-24 14:39:58
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserBeautyMapper userBeautyMapper;

    @Resource
    private JwtUtils jwtUtils;

    @Resource
    private RedisComponent redisComponent;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResponse<String> userRegister(String email, String nickName, String password) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("email", email);
        User one = this.getOne(queryWrapper);
        if (one != null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "邮箱已存在");
        }
        User user = new User();
        user.setUserId(UserContactTypeEnum.USER.getPrefix() + RandomUtil.randomNumbers(CommonConstant.LENGTH_11));
        QueryWrapper<UserBeauty> query = new QueryWrapper<>();
        query.like("email", email);
        UserBeauty beautyAccount = userBeautyMapper.selectOne(query);
        boolean useBeautyAccount = beautyAccount != null && BeautyAccountStatusEnum.NO_USE.getStatus().equals(beautyAccount.getStatus());
        if (useBeautyAccount) {
            user.setUserId(UserContactTypeEnum.USER.getPrefix() + beautyAccount.getUserId());
        }
        user.setEmail(email);
        user.setNickName(nickName);
        // 将密码 md5 加密
        user.setPassword(SecureUtil.md5(password));
        user.setStatus(UserStatusEnum.ENABLE.getStatus());
        user.setJoinType(JoinTypeEnum.APPLY.getType());
        user.setLastOffTime(DateUtil.current());
        int insert = userMapper.insert(user);
        // 如果靓号存在，更新靓号状态为已使用
        if (useBeautyAccount) {
            beautyAccount.setStatus(BeautyAccountStatusEnum.USED.getStatus());
            userBeautyMapper.updateById(beautyAccount);
        }
        // TODO 创建机器人好友
        if (insert != 0) {
            return ResultUtils.success("注册成功");
        } else {
            return ResultUtils.error(ErrorCode.OPERATION_ERROR, "注册失败");
        }
    }

    @Override
    public UserVO userLogin(String email, String password) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        User user = userMapper.selectOne(queryWrapper);
        if (user == null || !user.getPassword().equals(password)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "账号或密码错误");
        }
        if (!user.getStatus().equals(UserStatusEnum.ENABLE.getStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "账号已禁用");
        }
        // 判断用户是否已经登录
        Long lastHeartBeat = redisComponent.getUserHeartBeat(user.getUserId());
        if (lastHeartBeat != null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "此账号已经在别处登录，请退出后再登录");
        }
        // TODO 查询我的群组和我的联系人
        // TODO 查询我的联系人
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        // 生成Token
        String token = jwtUtils.createToken(userVO.getUserId(), userVO.getUserRole());
        userVO.setToken(token);
        // 保存登录信息到Redis中
        redisComponent.saveTokenUserVO(userVO);
        return userVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserInfo(UserUpdateRequest userUpdateRequest) {
        User dbUserInfo = userMapper.selectById(userUpdateRequest.getUserId());
        User updateUser = new User();
        BeanUtil.copyProperties(userUpdateRequest, updateUser);
        userMapper.updateById(updateUser);
        String contactNameUpdate = null;
        if (!dbUserInfo.getNickName().equals(userUpdateRequest.getNickName())) {
            contactNameUpdate = updateUser.getNickName();
        }
        // TODO 更新会话信息中的昵称信息
    }

    @Override
    public void forceOffLine(String userId) {
        // TODO  强制下线
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        String userId = userQueryRequest.getUserId();
        String userIdFuzzy = userQueryRequest.getUserIdFuzzy();
        String email = userQueryRequest.getEmail();
        String emailFuzzy = userQueryRequest.getEmailFuzzy();
        String nickName = userQueryRequest.getNickName();
        String nickNameFuzzy = userQueryRequest.getNickNameFuzzy();
        Integer joinType = userQueryRequest.getJoinType();
        Integer sex = userQueryRequest.getSex();
        Integer status = userQueryRequest.getStatus();
        String createTime = userQueryRequest.getCreateTime();
        Long lastLoginTime = userQueryRequest.getLastLoginTime();
        String areaName = userQueryRequest.getAreaName();
        String areaNameFuzzy = userQueryRequest.getAreaNameFuzzy();
        String areaCode = userQueryRequest.getAreaCode();
        String areaCodeFuzzy = userQueryRequest.getAreaCodeFuzzy();
        Long lastOffTime = userQueryRequest.getLastOffTime();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(StringUtils.isNoneBlank(userId), "userId", userId);
        queryWrapper.like(StringUtils.isNoneBlank(userIdFuzzy), "userId", userId);
        queryWrapper.eq(StringUtils.isNoneBlank(email), "email", email);
        queryWrapper.like(StringUtils.isNoneBlank(emailFuzzy), "email", emailFuzzy);
        queryWrapper.eq(StringUtils.isNoneBlank(nickName), "nickName", nickName);
        queryWrapper.like(StringUtils.isNoneBlank(nickNameFuzzy), "nickName", nickNameFuzzy);
        queryWrapper.eq(joinType != null, "joinType", joinType);
        queryWrapper.eq(sex != null, "sex", sex);
        queryWrapper.eq(status != null, "status", status);
        queryWrapper.eq(StringUtils.isNoneBlank(createTime), "createTime", createTime);
        queryWrapper.eq(lastLoginTime != null, "lastLoginTime", lastLoginTime);
        queryWrapper.eq(StringUtils.isNoneBlank(areaName), "areaName", areaName);
        queryWrapper.like(StringUtils.isNoneBlank(areaNameFuzzy), "areaNameFuzzy", areaNameFuzzy);
        queryWrapper.eq(StringUtils.isNoneBlank(areaCode), "areaCode", areaCode);
        queryWrapper.like(StringUtils.isNoneBlank(areaCodeFuzzy), "areaCodeFuzzy", areaCodeFuzzy);
        queryWrapper.eq(lastOffTime != null, "lastOffTime", lastOffTime);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);

        return queryWrapper;
    }
}




