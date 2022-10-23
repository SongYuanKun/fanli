package com.songyuankun.reply.controller;

import com.songyuankun.jd.UnionJdService;
import com.songyuankun.reply.dto.MessageDTO;
import com.songyuankun.reply.service.WeChatService;
import com.songyuankun.taobao.UnionTaoBaoProxy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * @author songyuankun
 */
@RestController
@Slf4j
@RequestMapping("weixin")
public class MessageController {

    private final WeChatService weChatService;
    private final UnionJdService unionJdProxy;
    private final UnionTaoBaoProxy unionTaoBaoProxy;

    public MessageController(WeChatService weChatService, UnionJdService unionJdProxy, UnionTaoBaoProxy unionTaoBaoProxy) {
        this.weChatService = weChatService;
        this.unionJdProxy = unionJdProxy;
        this.unionTaoBaoProxy = unionTaoBaoProxy;
    }

    @PostMapping(value = "auto-reply", consumes = "text/xml", produces = "text/xml")
    public MessageDTO autoReplay(@RequestBody MessageDTO messageDTO) {
        log.info("messageDTO:{}", messageDTO);
        String command;
        try {
            command = unionJdProxy.getGoodsInfo(messageDTO.getContent(), messageDTO.getFromUserName());
            if (StringUtils.isBlank(command)) {
                command = unionTaoBaoProxy.getCommand(messageDTO.getContent());
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
