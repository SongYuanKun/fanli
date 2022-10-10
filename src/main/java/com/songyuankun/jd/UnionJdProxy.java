package com.songyuankun.jd;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author songyuankun
 */
@Service
@Slf4j
public class UnionJdProxy {

    private static final String API_URL = "https://api.jd.com/routerjson";
    @Value("${jd.app_key}")
    private String appKey;
    @Value("${jd.secret_key}")
    private String secretKey;
    @Value("${jd.site_id}")
    private String siteId;

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

    public String getCommand(final String skuUrl) {

        String innerSkuUrl = null;

        String pattern = "https://item(.m|).jd.com/(product/|)\\d*.html";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(skuUrl);
        if (m.find()) {
            innerSkuUrl = m.group();
        }
        if (Objects.isNull(innerSkuUrl)) {
            return null;
        }

        String timestamp = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
        String version = "1.0";
        String method = "jd.union.open.promotion.common.get";
        JSONObject jsonObject = new JSONObject();
        JSONObject promotionCodeReq = new JSONObject();
        promotionCodeReq.put("materialId", innerSkuUrl);
        promotionCodeReq.put("siteId", siteId);
        jsonObject.put("promotionCodeReq", promotionCodeReq);
        String paramJson = jsonObject.toJSONString();
        String sign;
        try {
            sign = buildSign(timestamp, version, method, paramJson, appKey, secretKey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        String queryUrl = API_URL
                + "?timestamp=" + timestamp
                + "&v=" + version
                + "&sign_method=md5"
                + "&format=json"
                + "&method=" + method
                + "&360buy_param_json=" + paramJson
                + "&app_key=" + appKey
                + "&sign=" + sign;
        HttpResponse execute = HttpUtil.createGet(queryUrl).execute();
        String body = execute.body();
        JSONObject res = JSON.parseObject(body);
        JSONObject result = res.getJSONObject("jd_union_open_promotion_common_get_responce").getJSONObject("getResult");
        if (result.getJSONObject("data") != null) {
            return result.getJSONObject("data").getString("clickURL");
        } else {
            return result.getString("message");
        }
    }

    public String getGoodsInfo(String skuUrl) {

        String url = getCommand(skuUrl);

        String skuId = getSkuId(skuUrl);
        if (StringUtils.isBlank(skuId)) {
            return null;
        }

        String timestamp = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
        String version = "1.0";
        String method = "jd.union.open.goods.promotiongoodsinfo.query";
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("skuIds", skuId);
        String paramJson = jsonObject.toJSONString();
        String sign;
        try {
            sign = buildSign(timestamp, version, method, paramJson, appKey, secretKey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        String queryUrl = API_URL
                + "?timestamp=" + timestamp
                + "&v=" + version
                + "&sign_method=md5"
                + "&format=json"
                + "&method=" + method
                + "&360buy_param_json=" + paramJson
                + "&app_key=" + appKey
                + "&sign=" + sign;
        HttpResponse execute = HttpUtil.createGet(queryUrl).execute();
        String body = execute.body();
        JSONObject res = JSON.parseObject(body);
        JSONArray jsonArray = res.getJSONObject("jd_union_open_goods_promotiongoodsinfo_query_responce").getJSONObject("queryResult").getJSONArray("data");
        JSONObject goodsInfo;
        if (jsonArray.size() > 0) {
            goodsInfo = jsonArray.getJSONObject(0);
        } else {
            return "该商品不参与优惠";
        }
        if (goodsInfo == null || url == null) {
            return null;
        }
        return "商品名称：" + goodsInfo.getString("goodsName") + "\r\n" +
                "价格：" + goodsInfo.getString("unitPrice") + "\r\n" +
                "返佣比例：" + goodsInfo.getString("commisionRatioPc") + "%\r\n" +
                "预计返佣：" +
                new BigDecimal(goodsInfo.getInteger("unitPrice"))
                        .multiply(new BigDecimal(goodsInfo.getInteger("commisionRatioPc")))
                        .multiply(new BigDecimal("0.01"))
                        .setScale(2, RoundingMode.UP) +
                "\r\n" +
                "下单地址：" + url +
                "";

    }

    private String getSkuId(String skuUrl) {
        String pattern = "https://item(.m|).jd.com/(product/|)\\d*.html";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(skuUrl);
        if (!m.find()) {
            return "";
        }
        String url = m.group();
        return url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf(".html"));
    }

    private String buildSign(String timestamp, String version, String method, String paramJson, String appKey, String appSecret) throws Exception {
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

}
