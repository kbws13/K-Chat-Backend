package xyz.kbws.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.system.UserInfo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import xyz.kbws.common.ErrorCode;
import xyz.kbws.constant.CommonConstant;
import xyz.kbws.exception.BusinessException;
import xyz.kbws.mapper.UserBeautyMapper;
import xyz.kbws.mapper.UserMapper;
import xyz.kbws.model.dto.userBeauty.UserBeautyAddDTO;
import xyz.kbws.model.dto.userBeauty.UserBeautyQuery;
import xyz.kbws.model.entity.User;
import xyz.kbws.model.entity.UserBeauty;
import xyz.kbws.model.enums.BeautyAccountStatusEnum;
import xyz.kbws.service.UserBeautyService;
import xyz.kbws.utils.SqlUtils;

import javax.annotation.Resource;

/**
 * @author hsy
 * @description 针对表【user_beauty(靓号表)】的数据库操作Service实现
 * @createDate 2024-04-24 14:40:17
 */
@Service
public class UserBeautyServiceImpl extends ServiceImpl<UserBeautyMapper, UserBeauty>
        implements UserBeautyService {

    @Resource
    private UserMapper userMapper;

    @Override
    public void saveUserBeauty(UserBeautyAddDTO beauty) {
        if (beauty.getId() != null) {
            UserBeauty dbInfo = this.getById(beauty.getId());
            if (BeautyAccountStatusEnum.USED.getStatus().equals(dbInfo.getStatus())) {
                //已经使用的不允许修改
                throw new BusinessException(ErrorCode.OPERATION_ERROR);
            }
        }
        //判断邮箱是否已经存在
        QueryWrapper<UserBeauty> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", beauty.getEmail());
        UserBeauty dbInfo = this.getOne(queryWrapper);
        if (beauty.getId() == null && dbInfo != null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "靓号邮箱已经存在");
        }

        if (beauty.getId() != null && dbInfo != null && dbInfo.getId() != null && !beauty.getId().equals(dbInfo.getId())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "靓号邮箱已经存在");
        }

        //判断靓号是否存在
        QueryWrapper<UserBeauty> query = new QueryWrapper<>();
        query.eq("userId", beauty.getUserId());
        dbInfo = this.getOne(query);
        if (beauty.getId() == null && dbInfo != null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "靓号已经存在");
        }

        if (beauty.getId() != null && dbInfo != null && dbInfo.getId() != null && !beauty.getId().equals(dbInfo.getId())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }

        //判断邮箱是否已经注册
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("email", beauty.getEmail());
        User userInfo = userMapper.selectOne(userQueryWrapper);
        if (userInfo != null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "靓号邮箱已经被注册");
        }

        userInfo = this.userMapper.selectById(beauty.getUserId());
        if (userInfo != null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "靓号已经被注册");
        }

        if (beauty.getId() != null) {
            UserBeauty userBeauty = new UserBeauty();
            BeanUtil.copyProperties(beauty, userBeauty);
            this.updateById(userBeauty);
        } else {
            UserBeauty userBeauty = new UserBeauty();
            BeanUtil.copyProperties(beauty, userBeauty);
            this.save(userBeauty);
        }
    }

    @Override
    public QueryWrapper<UserBeauty> getQueryWrapper(UserBeautyQuery userBeautyQuery) {
        if (userBeautyQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<UserBeauty> queryWrapper = new QueryWrapper<>();
        String email = userBeautyQuery.getEmail();
        String emailFuzzy = userBeautyQuery.getEmailFuzzy();
        String userId = userBeautyQuery.getUserId();
        String userIdFuzzy = userBeautyQuery.getUserIdFuzzy();
        Integer status = userBeautyQuery.getStatus();
        String sortField = userBeautyQuery.getSortField();
        String sortOrder = userBeautyQuery.getSortOrder();
        queryWrapper.eq(StringUtils.isNoneBlank(email), "email", email);
        queryWrapper.like(StringUtils.isNoneBlank(emailFuzzy), "email", emailFuzzy);
        queryWrapper.eq(StringUtils.isNoneBlank(userId), "userId", userId);
        queryWrapper.like(StringUtils.isNoneBlank(userIdFuzzy), "userId", userIdFuzzy);
        queryWrapper.eq(status != null, "status", status);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }
}




