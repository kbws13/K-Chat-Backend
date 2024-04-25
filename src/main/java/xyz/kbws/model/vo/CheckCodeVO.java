package xyz.kbws.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author kbws
 * @date 2024/4/24
 * @description: 生成图片验证码
 */
@Data
public class CheckCodeVO implements Serializable {

    private static final long serialVersionUID = 4081290697544567030L;
    @ApiModelProperty(value = "验证码图片(Base64格式)")
    private String checkCode;
    @ApiModelProperty(value = "验证码Key")
    private String checkCodeKey;
}
