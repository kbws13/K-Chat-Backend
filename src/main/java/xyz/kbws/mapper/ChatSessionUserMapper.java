package xyz.kbws.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import xyz.kbws.model.entity.ChatSessionUser;
import xyz.kbws.model.vo.ChatSessionUserVO;

import java.util.List;

/**
* @author hsy
* @description 针对表【chat_session_user(会话用户表)】的数据库操作Mapper
* @createDate 2024-06-26 13:54:42
* @Entity xyz.kbws.model.entity.ChatSessionUser
*/
public interface ChatSessionUserMapper extends BaseMapper<ChatSessionUser> {
    List<ChatSessionUserVO> selectVO(@Param("query") ChatSessionUser chatSessionUser);
}




