package xyz.kbws.websocket.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import xyz.kbws.config.AppConfig;
import xyz.kbws.websocket.netty.handler.HeartBeatHandler;
import xyz.kbws.websocket.netty.handler.WebSocketHandler;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author kbws
 * @date 2024/6/25
 * @description: Netty 访问启动入口
 */
@Slf4j
@Component
public class NettyWebSocketStarter {

    // 定义两个线程组
    // 处理连接的线程组
    private static EventLoopGroup boosGroup = new NioEventLoopGroup(1);
    // 处理消息的线程组
    private static EventLoopGroup workGroup = new NioEventLoopGroup();

    @Resource
    private WebSocketHandler webSocketHandler;

    @Resource
    private AppConfig appConfig;

    @PreDestroy
    public void close() {
        boosGroup.shutdownGracefully();
        workGroup.shutdownGracefully();
    }

    @Async
    public void startNetty() {
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(boosGroup, workGroup);
            serverBootstrap.channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.DEBUG))
                    .childHandler(new ChannelInitializer() {
                        // 设置处理器
                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            ChannelPipeline pipeline = channel.pipeline();
                            // 对 HTTP 协议的支持，使用 HTTP 的编码器、解码器
                            pipeline.addLast(new HttpServerCodec());
                            // 聚合解码 HttpRequest/HttpContent/lastHttpContent 到 fullHttpRequest
                            // 保证接收到的 HTTP 请求的完整性
                            pipeline.addLast(new HttpObjectAggregator(64 * 1024));
                            // 心跳检测 readerIdleTime(读超时时间) writeIdleTime(写超时时间) allIdleTime(所有类型的超时时间)
                            pipeline.addLast(new IdleStateHandler(60,  0, 0, TimeUnit.SECONDS));
                            pipeline.addLast(new HeartBeatHandler());
                            // 将 HTTP 协议升级为 WebSocket 协议，对 WebSocket 支持
                            pipeline.addLast(new WebSocketServerProtocolHandler("/ws", null, true, 64*1024, true, true, 10000L));
                            pipeline.addLast(webSocketHandler);
                        }
                    });
            ChannelFuture channelFuture = serverBootstrap.bind(appConfig.getWsPort()).sync();
            log.info("Netty 服务启动成功，端口：{}", appConfig.getWsPort());
            channelFuture.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            log.error("启动 Netty 失败");
            throw new RuntimeException(e);
        }finally {
            boosGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }
}
