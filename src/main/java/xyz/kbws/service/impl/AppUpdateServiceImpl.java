package xyz.kbws.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import xyz.kbws.common.ErrorCode;
import xyz.kbws.constant.CommonConstant;
import xyz.kbws.exception.BusinessException;
import xyz.kbws.model.dto.appUpdate.AppUpdateQueryDTO;
import xyz.kbws.model.entity.AppUpdate;
import xyz.kbws.model.enums.AppUpdateSatusEnum;
import xyz.kbws.service.AppUpdateService;
import xyz.kbws.mapper.AppUpdateMapper;
import org.springframework.stereotype.Service;
import xyz.kbws.utils.SqlUtils;

/**
* @author hsy
* @description 针对表【app_update(app发布表)】的数据库操作Service实现
* @createDate 2024-06-25 13:54:12
*/
@Service
public class AppUpdateServiceImpl extends ServiceImpl<AppUpdateMapper, AppUpdate>
    implements AppUpdateService{

    @Override
    public void postUpdate(Integer id, Integer status, String grayscaleUid) {
        AppUpdateSatusEnum statusEnum = AppUpdateSatusEnum.getByStatus(status);
        if (status == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        if (AppUpdateSatusEnum.GRAYSCALE == statusEnum && StrUtil.isEmpty(grayscaleUid)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        if (AppUpdateSatusEnum.GRAYSCALE != statusEnum) {
            grayscaleUid = "";
        }
        AppUpdate update = this.getById(id);
        update.setStatus(status);
        update.setGrayscaleUid(grayscaleUid);
        this.updateById(update);
    }

    @Override
    public QueryWrapper<AppUpdate> getQueryWrapper(AppUpdateQueryDTO appUpdateQueryDTO) {
        QueryWrapper<AppUpdate> queryWrapper = new QueryWrapper<>();
        String version = appUpdateQueryDTO.getVersion();
        String updateDesc = appUpdateQueryDTO.getUpdateDesc();
        Integer status = appUpdateQueryDTO.getStatus();
        String grayscaleUid = appUpdateQueryDTO.getGrayscaleUid();
        Integer fileType = appUpdateQueryDTO.getFileType();
        String outerLink = appUpdateQueryDTO.getOuterLink();
        String sortField = appUpdateQueryDTO.getSortField();
        String sortOrder = appUpdateQueryDTO.getSortOrder();
        queryWrapper.like(StringUtils.isNoneBlank(version), "version", version);
        queryWrapper.like(StringUtils.isNoneBlank(updateDesc), "updateDesc", updateDesc);
        queryWrapper.eq(status != null, "status", status);
        queryWrapper.like(StringUtils.isNoneBlank(grayscaleUid), "grayscaleUid", grayscaleUid);
        queryWrapper.eq(fileType != null, "fileType", fileType);
        queryWrapper.like(StringUtils.isNoneBlank(outerLink), "outerLink", outerLink);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }
}




