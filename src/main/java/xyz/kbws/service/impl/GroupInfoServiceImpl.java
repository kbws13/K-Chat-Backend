package xyz.kbws.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import xyz.kbws.exception.ThrowUtils;
import xyz.kbws.mapper.GroupInfoMapper;
import xyz.kbws.model.entity.GroupInfo;
import xyz.kbws.service.GroupInfoService;
import org.springframework.stereotype.Service;

/**
* @author hsy
* @description 针对表【group_info(群组表)】的数据库操作Service实现
* @createDate 2024-04-26 14:51:47
*/
@Service
public class GroupInfoServiceImpl extends ServiceImpl<GroupInfoMapper, GroupInfo>
    implements GroupInfoService{

}




