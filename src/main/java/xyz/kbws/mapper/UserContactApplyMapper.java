package xyz.kbws.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import xyz.kbws.model.entity.UserContactApply;
import xyz.kbws.model.vo.UserContactApplyVO;

import java.util.List;

/**
 * @author kbws
 * @date 2024/4/26
 * @description:
 */
public interface UserContactApplyMapper extends BaseMapper<UserContactApply> {

    UserContactApply selectByPrimaryKey(String applyId, String receiveId, String contactId);

    List<UserContactApplyVO> selectApplyVO(String receiveId);
}
