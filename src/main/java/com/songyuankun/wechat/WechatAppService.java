package com.songyuankun.wechat;

import com.songyuankun.util.WeChatUtil;

import org.springframework.stereotype.Service;

@Service
public class WechatAppService {

    private final WeChatUtil weChatUtil;

    public WechatAppService(WeChatUtil weChatUtil) {
        this.weChatUtil = weChatUtil;
    }

    public String addNews() {
        weChatUtil.sendWeChatArticles("");
        return "";
    }

}
