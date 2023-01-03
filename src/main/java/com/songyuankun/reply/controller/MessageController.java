package com.songyuankun.reply.controller;

import com.songyuankun.EnableGetGoodInfo;
import com.songyuankun.reply.dto.MessageDTO;
import com.songyuankun.reply.service.WeChatService;
import com.songyuankun.taobao.UnionTaoBaoProxy;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * @author songyuankun
 */
@RestController
@Slf4j
@RequestMapping("weixin")
public class MessageController {

    private final WeChatService weChatService;
    private final List<EnableGetGoodInfo> enableGetGoodInfoList;
    private final UnionTaoBaoProxy unionTaoBaoProxy;

    public MessageController(WeChatService weChatService, List<EnableGetGoodInfo> enableGetGoodInfoList, UnionTaoBaoProxy unionTaoBaoProxy) {
        this.weChatService = weChatService;
        this.enableGetGoodInfoList = enableGetGoodInfoList;
        this.unionTaoBaoProxy = unionTaoBaoProxy;
    }

    @PostMapping(value = "auto-reply", consumes = "text/xml", produces = "text/xml")
    public MessageDTO autoReplay(@RequestBody MessageDTO messageDTO) {
        log.info("messageDTO:{}", messageDTO);
        String command = null;
        try {
            for (EnableGetGoodInfo enableGetGoodInfo : enableGetGoodInfoList) {
                command = enableGetGoodInfo.getGoodInfo(messageDTO.getContent(), messageDTO.getFromUserName());
                if (StringUtils.isNotBlank(command)) {
                    break;
                }
            }
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
