package com.songyuankun.util.enums;

import lombok.Getter;

@Getter
public enum WeChatUrlEnum {

    /**
     * 添加永久素材
     */
    ADD_NEWS("https://api.weixin.qq.com/cgi-bin/material/add_news"),
    ADD_MATERIAL("https://api.weixin.qq.com/cgi-bin/material/add_material"),
    UPLOAD_IMG("https://api.weixin.qq.com/cgi-bin/media/uploadimg"),
    TOKEN("https://api.weixin.qq.com/cgi-bin/token");

    private final String url;

    WeChatUrlEnum(String url) {
        this.url = url;
    }
}
