package xyz.kbws.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.kbws.model.entity.UserContact;
import xyz.kbws.model.vo.UserContactSearchResultVO;
import xyz.kbws.model.vo.UserContactVO;

import java.util.List;

/**
* @author hsy
* @description 针对表【user_contact(联系人表)】的数据库操作Service
* @createDate 2024-04-26 14:51:55
*/
public interface UserContactService extends IService<UserContact> {

    List<UserContactVO> listUsers(String userId);

    UserContactSearchResultVO searchContact(String userId, String contactId);
}
