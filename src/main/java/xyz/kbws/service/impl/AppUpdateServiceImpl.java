package xyz.kbws.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import xyz.kbws.model.entity.AppUpdate;
import xyz.kbws.service.AppUpdateService;
import xyz.kbws.mapper.AppUpdateMapper;
import org.springframework.stereotype.Service;

/**
* @author hsy
* @description 针对表【app_update(app发布表)】的数据库操作Service实现
* @createDate 2024-06-25 13:54:12
*/
@Service
public class AppUpdateServiceImpl extends ServiceImpl<AppUpdateMapper, AppUpdate>
    implements AppUpdateService{

}




