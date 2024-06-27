package xyz.kbws.websocket;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import xyz.kbws.constant.CommonConstant;
import xyz.kbws.mapper.ChatMessageMapper;
import xyz.kbws.mapper.UserMapper;
import xyz.kbws.model.dto.message.MessageSendDTO;
import xyz.kbws.model.entity.ChatMessage;
import xyz.kbws.model.entity.ChatSessionUser;
import xyz.kbws.model.entity.User;
import xyz.kbws.model.entity.UserContactApply;
import xyz.kbws.model.enums.MessageTypeEnum;
import xyz.kbws.model.enums.UserContactApplyStatusEnum;
import xyz.kbws.model.enums.UserContactTypeEnum;
import xyz.kbws.model.vo.WsInitVO;
import xyz.kbws.redis.RedisComponent;
import xyz.kbws.service.ChatSessionUserService;
import xyz.kbws.service.UserContactApplyService;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author kbws
 * @date 2024/6/26
 * @description: Channel 上下文工具类
 */
@Slf4j
@Component
public class ChannelContext {

    private static final ConcurrentHashMap<String, Channel> USER_CONTEXT_MAP = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<String, ChannelGroup> GROUP_CONTEXT_MAP = new ConcurrentHashMap<>();

    @Resource
    private ChatSessionUserService chatSessionUserService;

    @Resource
    private ChatMessageMapper chatMessageMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserContactApplyService userContactApplyService;

    @Resource
    private RedisComponent redisComponent;

    public void addContext(String userId, Channel channel) {
        String channelId = channel.id().toString();
        log.info("channelId：{}", channelId);
        AttributeKey attributeKey = null;
        if (!AttributeKey.exists(channelId)) {
            attributeKey = AttributeKey.newInstance(channelId);
        }else {
            attributeKey = AttributeKey.valueOf(channelId);
        }
        channel.attr(attributeKey).set(userId);
        List<String> contactIdList = redisComponent.getUserContactList(userId);
        for (String groupId : contactIdList) {
            if (groupId.startsWith(UserContactTypeEnum.GROUP.getPrefix())) {
                addToGroup(groupId, channel);
            }
        }
        USER_CONTEXT_MAP.put(userId, channel);
        redisComponent.saveUserHeartBeat(userId);
        // 更新用户最后登录时间
        User user = userMapper.selectById(userId);
        user.setLastLoginTime(new Date());
        userMapper.updateById(user);

        // 给用户发送消息
        Long sourceLastOfTime = user.getLastOffTime();
        Long lastOfTime = sourceLastOfTime;
        if (sourceLastOfTime != null && System.currentTimeMillis() - CommonConstant.MILLISECOND_3DAYS_AGO > sourceLastOfTime) {
            lastOfTime = CommonConstant.MILLISECOND_3DAYS_AGO;
        }
        // 1.查询会话信息 查询用户所有的会话信息 保证换了设备会话同步
        QueryWrapper<ChatSessionUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        queryWrapper.orderByDesc("lastReceiveTime");
        List<ChatSessionUser> chatSessionUserList = chatSessionUserService.list(queryWrapper);

        WsInitVO wsInitVO = new WsInitVO();
        wsInitVO.setChatSessionList(chatSessionUserList);

        // 2.查询聊天消息
        // 查询所有联系人
        List<String> groupIdList = contactIdList.stream().filter(item -> item.startsWith(UserContactTypeEnum.GROUP.getPrefix())).collect(Collectors.toList());
        groupIdList.add(userId);
        QueryWrapper<ChatMessage> query = new QueryWrapper<>();
        query.in("contactId", groupIdList);
        query.ge("sendTime", lastOfTime);
        List<ChatMessage> chatMessageList = chatMessageMapper.selectList(query);
        wsInitVO.setChatMessageList(chatMessageList);

        // 3.查询好友申请
        QueryWrapper<UserContactApply> applyQuery = new QueryWrapper<>();
        applyQuery.eq("receiveId", userId);
        applyQuery.eq("status", UserContactApplyStatusEnum.INIT.getStatus());
        applyQuery.ge("lastApplyTime", lastOfTime);
        Integer count = Math.toIntExact(userContactApplyService.count(applyQuery));
        wsInitVO.setApplyCount(count);
        // 发送消息
        MessageSendDTO messageSendDTO = new MessageSendDTO();
        messageSendDTO.setMessageType(MessageTypeEnum.INIT.getType());
        messageSendDTO.setContactId(userId);
        messageSendDTO.setExtentData(wsInitVO);
        sendMessage(messageSendDTO, userId);
    }

    /**
     * 发送消息
     */
    public void sendMessage(MessageSendDTO messageSendDTO, String receiveId) {
        if (receiveId == null) {
            return;
        }
        Channel sendChannel = USER_CONTEXT_MAP.get(receiveId);
        if (sendChannel == null) {
            return;
        }
        // 相对于客户端而言，联系人就是发送人，这里要转一下再发送
        messageSendDTO.setContactId(messageSendDTO.getSendUserId());
        messageSendDTO.setContactName(messageSendDTO.getSendUserNickName());
        sendChannel.writeAndFlush(new TextWebSocketFrame(JSONUtil.toJsonStr(messageSendDTO)));
    }

    private void addToGroup(String groupId, Channel channel) {
        ChannelGroup group = GROUP_CONTEXT_MAP.get(groupId);
        if (group == null) {
            group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
            GROUP_CONTEXT_MAP.put(groupId, group);
        }
        if (channel == null) {
            return;
        }
        group.add(channel);
    }

    public void removeContext(Channel channel) {
        Attribute<String> attributeKey = channel.attr(AttributeKey.valueOf(channel.id().toString()));
        String userId = attributeKey.get();
        if (StrUtil.isEmpty(userId)) {
            USER_CONTEXT_MAP.remove(userId);
        }
        redisComponent.removeUserHeartBeat(userId);
        // 更新用户最后离线时间
        User user = userMapper.selectById(userId);
        user.setLastOffTime(System.currentTimeMillis());
        userMapper.updateById(user);
    }

}
