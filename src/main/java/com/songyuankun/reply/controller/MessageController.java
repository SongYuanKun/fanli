package com.songyuankun.reply.controller;

import com.songyuankun.reply.dto.MessageDTO;
import com.songyuankun.reply.service.WeChatService;
import com.songyuankun.unionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author songyuankun
 */
@RestController
@Slf4j
@RequestMapping("weixin")
public class MessageController {

    private final WeChatService weChatService;
    private final List<unionService> unionServiceList;

    public MessageController(WeChatService weChatService, List<unionService> unionServiceList) {
        this.weChatService = weChatService;
        this.unionServiceList = unionServiceList;
    }

    @PostMapping(value = "auto-reply", consumes = "text/xml", produces = "text/xml")
    public MessageDTO autoReplay(@RequestBody MessageDTO messageDTO) {
        log.info("messageDTO:{}", messageDTO);
        String command = null;
        try {
            for (unionService unionService : unionServiceList) {
                command = unionService.getGoodInfo(messageDTO.getContent(), messageDTO.getFromUserName());
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

}
