package com.songyuankun.jd;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JdUtil {

    public static String getSkuId(String skuUrl) {
        String pattern = "https://item(.m|).jd.com/(product/|)\\d*.html";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(skuUrl);
        if (!m.find()) {
            return "";
        }
        String url = m.group();
        return url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf(".html"));
    }

    public static String buildSign(String timestamp, String version, String method, String paramJson, String appKey, String appSecret) throws Exception {
        Map<String, String> map = new TreeMap<>();
        map.put("timestamp", timestamp);
        map.put("v", version);
        map.put("sign_method", "md5");
        map.put("format", "json");
        map.put("method", method);
        map.put("360buy_param_json", paramJson);
        map.put("app_key", appKey);
        StringBuilder sb = new StringBuilder(appSecret);
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();
            //检测参数是否为空
            if (areNotEmpty(new String[]{name, value})) {
                sb.append(name).append(value);
            }
        }
        sb.append(appSecret);
        //MD5
        return md5(sb.toString());
    }


    public static String md5(String source) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] bytes = md.digest(source.getBytes(StandardCharsets.UTF_8));
        return byte2hex(bytes);

    }

    private static String byte2hex(byte[] bytes) {
        StringBuilder sign = new StringBuilder();
        for (byte aByte : bytes) {
            String hex = Integer.toHexString(aByte & 0xFF);
            if (hex.length() == 1) {
                sign.append("0");
            }
            sign.append(hex.toUpperCase());
        }
        return sign.toString();

    }

    public static boolean areNotEmpty(String[] values) {
        boolean result = true;
        if ((values == null) || (values.length == 0)) {
            result = false;
        } else {
            for (String value : values) {
                result &= !isEmpty(value);
            }
        }
        return result;

    }

    public static boolean isEmpty(String value) {
        int strLen;
        if ((value == null) || ((strLen = value.length()) == 0)) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(value.charAt(i))) {
                return false;
            }
        }
        return true;

    }

}
