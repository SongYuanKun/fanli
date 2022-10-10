package com.songyuankun.reply.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class QyWeChatMessageDTO {
    @JSONField(name = "touser")
    private String toUser;
    @JSONField(name = "msgtype")
    private String msgType;
    @JSONField(name = "agentid")
    private Integer agentId;
    private Text text;
    private Integer safe;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Text {
        private String content;
    }
}
