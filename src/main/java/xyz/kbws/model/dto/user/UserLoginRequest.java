package xyz.kbws.model.dto.user;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

/**
 * @author kbws
 * @date 2024/4/25
 * @description: 用户登录请求
 */
@Data
public class UserLoginRequest {

    @ApiModelProperty(value = "邮箱")
    @Email
    @NotEmpty(message = "email不能为空")
    private String email;

    @ApiModelProperty(value = "密码")
    @NotEmpty(message = "password不能为空")
    private String password;

    @ApiModelProperty(value = "验证码")
    @NotEmpty(message = "checkCode不能为空")
    private String checkCode;

    @ApiModelProperty(value = "验证码Key")
    @NotEmpty(message = "checkCodeKey不能为空")
    private String checkCodeKey;
}
