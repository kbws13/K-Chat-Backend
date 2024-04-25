package xyz.kbws;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import xyz.kbws.redis.RedisUtils;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * @author kbws
 * @date 2024/4/25
 * @description:
 */
@Slf4j
@Component
public class InitRun implements ApplicationRunner {

    @Resource
    private DataSource dataSource;

    @Resource
    private RedisUtils redisUtils;

    @Override
    public void run(ApplicationArguments args){
        try {
            dataSource.getConnection();
            redisUtils.get("test");
            log.info("服务启动成功，可以开始开发了");
        } catch (SQLException e) {
            log.error("数据库配置错误，请检查数据库配置");
        } catch (Exception e) {
            log.error("服务启动失败", e);
        }
    }
}
