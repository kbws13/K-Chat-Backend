package xyz.kbws.model.dto.userContact;

import lombok.Data;

import java.io.Serializable;

/**
 * @author kbws
 * @date 2024/5/23
 * @description:
 */
@Data
public class UserContactAddRequest implements Serializable {

    private String contactId;

    private String applyMessage;

    private static final long serialVersionUID = 7296484051837635200L;
}
