package xyz.kbws.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import xyz.kbws.model.dto.userContact.UserContactQueryDTO;
import xyz.kbws.model.entity.UserContact;
import xyz.kbws.model.vo.UserContactVO;

import java.util.List;

/**
 * @author kbws
 * @date 2024/4/26
 * @description:
 */
public interface UserContactMapper extends BaseMapper<UserContact> {

    /**
     * 根据 userId 关联查询 user_contact 表跟 user 表
     * @param userId
     * @return
     */
    List<UserContactVO> listUsers(String userId);

    UserContact selectByUserIdAndContactId(String userId, String contactId);

    /**
     * 获取我的群组列表
     * @param query
     * @return
     */
    List<UserContactVO> myContact(@Param("query") UserContactQueryDTO query);

}
