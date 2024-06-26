package xyz.kbws.utils;

import cn.hutool.crypto.SecureUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * @author kbws
 * @date 2024/6/26
 * @description:
 */
public class StringUtil {
    public static final String getChatSession(String[] userIds) {
        Arrays.sort(userIds);
        return SecureUtil.md5(StringUtils.join(userIds, ""));
    }
}
