package com.songyuankun.wechat.reply.dto;


import lombok.Data;
import lombok.ToString;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Data
@ToString
@XmlRootElement(name = "xml")
@XmlAccessorType(XmlAccessType.FIELD)
public class MessageDTO {

    /**
     * 开发者微信号
     */
    @XmlElement(name = "ToUserName")
    private String toUserName;
    /**
     * 发送方帐号（一个OpenID）
     */
    @XmlElement(name = "FromUserName")
    private String fromUserName;
    /**
     * 消息创建时间 （整型）
     */
    @XmlElement(name = "CreateTime")
    private Long createTime;
    /**
     * 消息类型，文本为text
     */
    @XmlElement(name = "MsgType")
    private String msgType;
    /**
     * 文本消息内容
     */
    @XmlElement(name = "Content")
    private String content;

    public MessageDTO replay(String content) {
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setContent(content);
        messageDTO.setCreateTime(System.currentTimeMillis() / 1000);
        messageDTO.setFromUserName(this.getToUserName());
        messageDTO.setToUserName(this.getFromUserName());
        messageDTO.setMsgType("text");
        return messageDTO;
    }

}
