package xyz.kbws.websocket;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import xyz.kbws.model.dto.message.MessageSendDTO;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * @author kbws
 * @date 2024/6/27
 * @description: 消息处理器
 */
@Slf4j
@Component
public class MessageHandler {

    private static final String MESSAGE_TOPIC = "message.topic";

    @Resource
    private RedissonClient redissonClient;

    @Lazy
    @Resource
    private ChannelContext channelContext;

    @PostConstruct
    public void listMessage() {
        RTopic rTopic = redissonClient.getTopic(MESSAGE_TOPIC);
        rTopic.addListener(MessageSendDTO.class, (MessageSendDTO, sendDTO) -> {
            log.info("收到广播消息：{}", JSONUtil.toJsonStr(sendDTO));
            channelContext.sendMessage(sendDTO);
        });
    }

    public void sendMessage(MessageSendDTO messageSendDTO) {
        RTopic rTopic = redissonClient.getTopic(MESSAGE_TOPIC);
        rTopic.publish(messageSendDTO);
    }
}
