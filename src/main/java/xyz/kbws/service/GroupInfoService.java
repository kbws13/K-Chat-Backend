package xyz.kbws.service;

import org.springframework.web.multipart.MultipartFile;
import xyz.kbws.model.entity.GroupInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.io.IOException;

/**
* @author hsy
* @description 针对表【group_info(群组表)】的数据库操作Service
* @createDate 2024-04-26 14:51:47
*/
public interface GroupInfoService extends IService<GroupInfo> {
    void save(GroupInfo groupInfo, MultipartFile avatarFile, MultipartFile avatarCover) throws IOException;
}
