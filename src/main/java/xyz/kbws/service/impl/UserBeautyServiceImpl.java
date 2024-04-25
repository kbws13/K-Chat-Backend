package xyz.kbws.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import xyz.kbws.mapper.UserBeautyMapper;
import xyz.kbws.model.entity.UserBeauty;
import xyz.kbws.service.UserBeautyService;

/**
 * @author hsy
 * @description 针对表【user_beauty(靓号表)】的数据库操作Service实现
 * @createDate 2024-04-24 14:40:17
 */
@Service
public class UserBeautyServiceImpl extends ServiceImpl<UserBeautyMapper, UserBeauty>
        implements UserBeautyService {

}




