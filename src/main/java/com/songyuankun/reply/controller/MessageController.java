package com.songyuankun.reply.controller;

import com.songyuankun.jd.UnionJdProxy;
import com.songyuankun.jd.repository.JdUserRepository;
import com.songyuankun.jd.repository.entity.JdUserPO;
import com.songyuankun.reply.dto.MessageDTO;
import com.songyuankun.reply.service.WeChatService;
import com.songyuankun.unionService;
import com.songyuankun.util.WeChatUtil;
import com.songyuankun.wechat.WxMpMassNews;
import com.songyuankun.wechat.WxMpNewsArticle;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * @author songyuankun
 */
@RestController
@Slf4j
@RequestMapping("weixin")
public class MessageController {

    private final WeChatService weChatService;
    private final WeChatUtil weChatUtil;
    private final List<unionService> unionServiceList;
    private final JdUserRepository jdUserRepository;

    @Autowired
    private UnionJdProxy unionJdProxy;

    public MessageController(WeChatService weChatService, WeChatUtil weChatUtil, List<unionService> unionServiceList, JdUserRepository jdUserRepository) {
        this.weChatService = weChatService;
        this.weChatUtil = weChatUtil;
        this.unionServiceList = unionServiceList;
        this.jdUserRepository = jdUserRepository;
    }

    @PostMapping(value = "auto-reply", consumes = "text/xml", produces = "text/xml")
    public MessageDTO autoReplay(@RequestBody MessageDTO messageDTO) {
        log.info("messageDTO:{}", messageDTO);
        String command = null;
        String content = messageDTO.getContent();
        String fromUserName = messageDTO.getFromUserName();
        if (!jdUserRepository.findFirstByWechatUser(fromUserName).isPresent() && content.contains("用户注册码：")) {
            JdUserPO jdUserPO = new JdUserPO();
            jdUserPO.setWechatUser(fromUserName);
            jdUserPO.setPositionId(content.replace("用户注册码：", ""));
            jdUserRepository.save(jdUserPO);
        }
        try {
            for (unionService unionService : unionServiceList) {
                command = unionService.getGoodInfo(content, fromUserName);
                if (StringUtils.isNotBlank(command)) {
                    break;
                }
            }
            if (StringUtils.isBlank(command)) {
                command = "仅支持淘宝、京东、拼多多的链接。或该商品不参与优惠";

            }
        } catch (Exception e) {
            e.printStackTrace();
            return messageDTO.replay(e.getMessage());
        }
        return messageDTO.replay(command);
    }

    @GetMapping("auto-reply")
    public String autoReplay(@RequestParam String signature, @RequestParam String timestamp, @RequestParam String nonce, @RequestParam(value = "echoStr") String echoStr) {
        if (weChatService.checkSignature(signature, timestamp, nonce)) {
            return echoStr;
        } else {
            return "";
        }
    }

    @GetMapping(produces = "text/html;charset=utf-8")
    public void test() {
        String jingFen = unionJdProxy.getJingFen("3100139794", 153);
        WxMpMassNews wxMpMassNews = new WxMpMassNews();
        WxMpNewsArticle wxMpNewsArticle = new WxMpNewsArticle();
        wxMpNewsArticle.setTitle("每日推荐");
        wxMpNewsArticle.setContent(jingFen);
        wxMpMassNews.setArticles(Collections.singletonList(wxMpNewsArticle));
        weChatUtil.sendWeChatArticles(wxMpMassNews);
    }

}
