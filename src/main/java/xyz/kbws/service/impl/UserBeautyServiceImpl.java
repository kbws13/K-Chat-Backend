package xyz.kbws.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import xyz.kbws.common.ErrorCode;
import xyz.kbws.constant.CommonConstant;
import xyz.kbws.exception.BusinessException;
import xyz.kbws.mapper.UserBeautyMapper;
import xyz.kbws.model.dto.userBeauty.UserBeautyQuery;
import xyz.kbws.model.entity.UserBeauty;
import xyz.kbws.service.UserBeautyService;
import xyz.kbws.utils.SqlUtils;

/**
 * @author hsy
 * @description 针对表【user_beauty(靓号表)】的数据库操作Service实现
 * @createDate 2024-04-24 14:40:17
 */
@Service
public class UserBeautyServiceImpl extends ServiceImpl<UserBeautyMapper, UserBeauty>
        implements UserBeautyService {

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




