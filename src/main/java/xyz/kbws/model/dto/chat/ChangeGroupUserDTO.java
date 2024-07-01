package xyz.kbws.model.dto.chat;

import lombok.Data;

import java.io.Serializable;

/**
 * @author kbws
 * @date 2024/7/1
 * @description: 添加或者移除群组人员
 */
@Data
public class ChangeGroupUserDTO implements Serializable {

    private String groupId;

    private String selectContacts;

    private Integer opType;

    private static final long serialVersionUID = -3306863600382060009L;
}
