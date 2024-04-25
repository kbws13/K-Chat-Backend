package xyz.kbws.model.dto.user;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * @author kbws
 * @date 2024/4/24
 * @description: 用户注册请求
 */
@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = -6284538547202624163L;
    @Email
    @NotEmpty(message = "email不能为空")
    private String email;
    @NotEmpty(message = "nickName不能为空")
    private String nickName;
    @NotEmpty(message = "password不能为空")
    private String password;
    @NotEmpty(message = "checkCode不能为空")
    private String checkCode;
    @NotEmpty(message = "checkCodeKey不能为空")
    private String checkCodeKey;
}
