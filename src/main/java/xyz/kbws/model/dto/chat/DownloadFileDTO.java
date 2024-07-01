package xyz.kbws.model.dto.chat;

import lombok.Data;

import java.io.Serializable;

/**
 * @author kbws
 * @date 2024/7/1
 * @description: 下载文件请求封装
 */
@Data
public class DownloadFileDTO implements Serializable {

    private String fileId;

    private Boolean showCover;

    private static final long serialVersionUID = 2701340923231856418L;
}
