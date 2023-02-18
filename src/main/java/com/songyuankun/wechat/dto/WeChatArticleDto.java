package com.songyuankun.wechat.dto;

import com.alibaba.fastjson.annotation.JSONField;

import lombok.Data;

/**
 * @author songyuankun
 */
@Data
public class WeChatArticleDto {


    private String title;


    @JSONField(name = "thumb_media_id")
    private String thumbMediaId;
    private String author;
    private String digest;
    @JSONField(name = "show_cover_pic")
    private Integer showCoverPic;
    private String content;
    @JSONField(name = "content_source_url")
    private String contentSourceUrl;
    @JSONField(name = "need_open_comment")
    private String needOpenComment;
    @JSONField(name = "only_fans_can_comment")
    private String onlyFansCanComment;


}
