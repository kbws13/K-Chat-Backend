package xyz.kbws.utils;

import cn.hutool.crypto.SecureUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

import static xyz.kbws.service.impl.UserContactServiceImpl.cleanHtmlTag;

/**
 * @author kbws
 * @date 2024/6/26
 * @description:
 */
public class StringUtil {
    public static final String getChatSessionForUser(String[] userIds) {
        Arrays.sort(userIds);
        return SecureUtil.md5(StringUtils.join(userIds, ""));
    }

    public static final String getChatSessionForGroup(String groupId) {
        return SecureUtil.md5(groupId);
    }

    public static String resetMessageContent(String content) {
        content = cleanHtmlTag(content);
        return content;
    }

    public static boolean isNumber(String str) {
        String checkNumber = "^[0-9]+$";
        if (null == str) {
            return false;
        }
        if (!str.matches(checkNumber)) {
            return false;
        }

        return true;
    }
}
