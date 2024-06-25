package xyz.kbws.model.dto.appUpdate;

import lombok.Data;

import java.io.Serializable;

/**
 * @author kbws
 * @date 2024/6/25
 * @description:
 */
@Data
public class AppUpdatePostDTO implements Serializable {

    private Integer id;

    private Integer status;

    private String grayscaleUid;

    private static final long serialVersionUID = 768791416836540117L;
}
