package xyz.kbws.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import xyz.kbws.model.dto.appUpdate.AppUpdateQueryDTO;
import xyz.kbws.model.entity.AppUpdate;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author hsy
* @description 针对表【app_update(app发布表)】的数据库操作Service
* @createDate 2024-06-25 13:54:12
*/
public interface AppUpdateService extends IService<AppUpdate> {

    void postUpdate(Integer id, Integer status, String grayscaleUid);

    QueryWrapper<AppUpdate> getQueryWrapper(AppUpdateQueryDTO appUpdateQueryDTO);
}
