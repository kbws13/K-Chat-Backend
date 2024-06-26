package xyz.kbws.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.kbws.model.dto.userContact.UserContactQueryDTO;
import xyz.kbws.model.entity.UserContact;
import xyz.kbws.model.enums.UserContactStatusEnum;
import xyz.kbws.model.vo.UserContactSearchResultVO;
import xyz.kbws.model.vo.UserContactVO;
import xyz.kbws.model.vo.UserVO;

import java.util.List;

/**
* @author hsy
* @description 针对表【user_contact(联系人表)】的数据库操作Service
* @createDate 2024-04-26 14:51:55
*/
public interface UserContactService extends IService<UserContact> {

    List<UserContactVO> listUsers(String userId);

    /**
     * 查询联系人或者群组
     * @param userId
     * @param contactId
     * @return
     */
    UserContactSearchResultVO searchContact(String userId, String contactId);

    /**
     * 申请添加联系人或者群组
     * @return
     */
    Integer apply(UserVO userVO, String contactId, String applyMessage);

    /**
     * 根据参数获取联系人信息
     * @param userContactQueryDTO 查询参数
     * @return
     */
    List<UserContactVO> listByParam(UserContactQueryDTO userContactQueryDTO);

    /**
     * 添加联系人
     * @param applyUserId 申请人id
     * @param receiveUserId 接收人id
     * @param contactId 申请记录id
     * @param contactType 联系人类型
     * @param applyMessage 申请信息
     */
    void addUserContact(String applyUserId, String receiveUserId, String contactId, Integer contactType, String applyMessage);

    Boolean updateContactStatus(String userId, String contactId, UserContactStatusEnum statusEnum);

    void addContactRobot(String userId);
}
