package com.songyuankun.wechat.reply.service;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.songyuankun.util.RedisUtil;
import com.songyuankun.wechat.reply.dto.QyWeChatMessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author songyuankun
 */
@Service
@Slf4j
public class AdminService {


    @Value("${my.wechat.admin.agent_id}")
    private Integer agentId;
    @Value("${my.wechat.admin.corp_id}")
    private String corpId;

    @Value("${my.wechat.admin.secret}")
    private String secret;

    private static final String SEND_MESSAGE_URL = "https://qyapi.weixin.qq.com/cgi-bin/message/send";

    private final RedisUtil redisUtil;

    public AdminService(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    public void sendMessageToAdmin(String content) {
        QyWeChatMessageDTO qyWeChatMessageDTO = new QyWeChatMessageDTO();
        qyWeChatMessageDTO.setToUser("@all");
        qyWeChatMessageDTO.setMsgType("text");
        qyWeChatMessageDTO.setAgentId(agentId);
        qyWeChatMessageDTO.setText(new QyWeChatMessageDTO.Text(content));
        qyWeChatMessageDTO.setSafe(0);
        String post = HttpUtil.post(SEND_MESSAGE_URL + "?access_token=" + getAccessTokenFromRedis(), JSON.toJSONString(qyWeChatMessageDTO));
        log.info("url:{},body:{},resp:{}", SEND_MESSAGE_URL + "?access_token=" + getAccessTokenFromRedis(), JSON.toJSONString(qyWeChatMessageDTO), post);
    }

    private String getAccessTokenFromRedis() {
        String accessToken = redisUtil.get("QY_WE_CHAT_TOKEN");
        if (accessToken == null) {
            accessToken = getAccessToken();
            if (accessToken != null) {
                redisUtil.setString("QY_WE_CHAT_TOKEN", accessToken, 7200);
            } else {
                log.error("get qy access token fail");
            }
        }
        return accessToken;
    }

    public String getAccessToken() {
        String url = "https://qyapi.weixin.qq.com/cgi-bin/gettoken";
        Map<String, Object> map = Maps.newHashMap();
        map.put("corpid", corpId);
        map.put("corpsecret", secret);
        String s = HttpUtil.get(url, map);
        JSONObject jsonObject = JSON.parseObject(s);
        return jsonObject.getString("access_token");
    }

}
