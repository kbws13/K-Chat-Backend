package xyz.kbws.model.dto.userContactApply;

import lombok.Data;

import java.io.Serializable;

/**
 * @author kbws
 * @date 2024/5/25
 * @description:
 */
@Data
public class UserContactApplyDealWithRequest implements Serializable {

    private Integer applyId;

    private Integer status;

    private static final long serialVersionUID = 751648389029506616L;
}
