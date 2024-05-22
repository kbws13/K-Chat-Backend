package xyz.kbws.config;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author kbws
 * @date 2024/4/25
 * @description: 全局应用配置
 */
@Data
@Component
public class AppConfig {

    /**
     * WebSocket 端口
     */
    @Value("${ws.port:}")
    private Integer wsPort;

    /**
     * 文件目录
     */
    @Value("${project.folder:}")
    private String projectFolder;

    @Value("${project.baseUrl}")
    private String baseUrl;

    public String getProjectFolder() {
        if (StrUtil.isEmpty(projectFolder) && !projectFolder.endsWith("/")) {
            projectFolder = projectFolder + "/";
        }
        return projectFolder;
    }
}
