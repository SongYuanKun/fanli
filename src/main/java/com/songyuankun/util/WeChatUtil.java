package com.songyuankun.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.songyuankun.util.enums.WeChatUrlEnum;

import com.songyuankun.wechat.WxMpMassNews;
import com.songyuankun.wechat.WxMpNewsArticle;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author songyuankun
 */
@Component
@Slf4j
public class WeChatUtil {
    @Value("${my.wechat.appid}")
    private String appId;
    @Value("${my.wechat.secret}")
    private String secret;

    private final RestTemplate restTemplate;
    private final RedisUtil redisUtil;

    public WeChatUtil(RedisUtil redisUtil, RestTemplate restTemplate) {
        this.redisUtil = redisUtil;
        this.restTemplate = restTemplate;
    }

    private String getAccessToken() {
        HashMap<String, Object> parameters = new HashMap<>(4);
        parameters.put("grant_type", "client_credential");
        parameters.put("appid", appId);
        parameters.put("secret", secret);
        String s = HttpUtil.get(WeChatUrlEnum.TOKEN.getUrl(), parameters);
        JSONObject jsonObject = JSON.parseObject(s);
        return jsonObject.getString("access_token");
    }

    /**
     * 去除src路径中的前后引号
     *
     * @param src 图片的src路径
     * @return src
     */
    private static String handleSrc(String src) {
        if (src != null) {
            if (src.startsWith("'") || src.startsWith("\"")) {
                return src.substring(1);
            }
            if (src.endsWith("'") || src.endsWith("\"")) {
                return src;
            }
        }
        return src;
    }

    private String getAccessTokenFromRedis() {
        String accessToken = redisUtil.get("WE_CHAT_TOKEN");
        if (accessToken == null) {
            accessToken = getAccessToken();
            if (accessToken != null) {
                redisUtil.setString("WE_CHAT_TOKEN", accessToken, 7200);
            }
        }
        return accessToken;
    }

    private String getWeChatUrl(WeChatUrlEnum weChatUrlEnum) {
        return weChatUrlEnum.getUrl() + "?access_token=" + getAccessTokenFromRedis();
    }

    public void sendWeChatArticles(WxMpMassNews articles) {
        String weChatUrl = getWeChatUrl(WeChatUrlEnum.UPLOAD_IMG);
        HttpUtil.post(weChatUrl, articles.toString());
    }


    public String addImageFromUrl(String url) {
        String weChatUrl = getWeChatUrl(WeChatUrlEnum.UPLOAD_IMG) + "&type=image";
        //设置请求头
        HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("multipart/form-data");
        headers.setContentType(type);
        //设置请求体，注意是LinkedMultiValueMap
        try {
            UrlResource fileUrlResource = new UrlResource(url);
            MultiValueMap<String, Object> form = new LinkedMultiValueMap<>(8);
            form.add("media", fileUrlResource);
            HttpEntity<MultiValueMap<String, Object>> files = new HttpEntity<>(form, headers);
            String s = restTemplate.postForObject(weChatUrl, files, String.class);
            return JSON.parseObject(s).getString("url");
        } catch (Exception e) {
            log.info("addImageFromUrl error", e);
        }
        return null;
    }

    /**
     * @param url url
     * @return mediaId
     */
    public String addThumbMedia(String url) {
        String weChatUrl = getWeChatUrl(WeChatUrlEnum.ADD_MATERIAL) + "&type=image";

        //设置请求头
        HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("multipart/form-data");
        headers.setContentType(type);
        //设置请求体，注意是LinkedMultiValueMap
        try {
            UrlResource fileUrlResource = new UrlResource(url);
            MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
            form.add("media", fileUrlResource);
            HttpEntity<MultiValueMap<String, Object>> files = new HttpEntity<>(form, headers);
            String s = restTemplate.postForObject(weChatUrl, files, String.class);
            JSONObject jsonObject = JSON.parseObject(s);
            return jsonObject.getString("media_id");
        } catch (Exception e) {
            log.info("addImageFromUrl error", e);
        }
        return null;
    }

    public String replaceUrl2WeChatUrl(String content) {
        List<String> urlList = new ArrayList<>();
        String img;
        Pattern pImage;
        Matcher mImage;
        String regExImg = "<img.*src\\s*=\\s*(.*?)[^>]*?>";
        String regExSrc = "src\\s*=\\s*\"?(.*?)(\"|>|\\s+)";

        pImage = Pattern.compile(regExImg, Pattern.CASE_INSENSITIVE);
        mImage = pImage.matcher(content);
        while (mImage.find()) {
            img = mImage.group();
            Matcher m = Pattern.compile(regExSrc).matcher(img);
            while (m.find()) {
                String path = m.group(1);
                String url = handleSrc(path);
                urlList.add(url);
            }
        }
        for (String url : urlList) {
            String weChatUrl = addImageFromUrl(url);
            content = content.replaceAll(url, weChatUrl).replaceAll("http://", "https://");
        }
        return content;

    }


}
