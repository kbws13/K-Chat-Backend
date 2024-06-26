package xyz.kbws.model.enums;

import lombok.Getter;

/**
 * @author kbws
 * @date 2024/6/26
 * @description: 消息状态枚举
 */
@Getter
public enum MessageStatusEnum {
    SENDING(0, "发送中"),
    SENDED(1, "已发送");


    private final Integer status;
    private final String desc;

    MessageStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public static MessageStatusEnum getByStatus(Integer status) {
        for (MessageStatusEnum item : MessageStatusEnum.values()) {
            if (item.getStatus().equals(status)) {
                return item;
            }
        }
        return null;
    }
}
