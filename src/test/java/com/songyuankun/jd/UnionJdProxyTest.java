package com.songyuankun.jd;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class UnionJdProxyTest {

    @Autowired
    UnionJdProxy unionJdProxy;

    @Test
    void getCommand() {
        String command = unionJdProxy.getCommand("https://item.m.jd.com/product/10056670674874.html?&utm_source=iosapp&utm_medium=appshare&utm_campaign=t_335139774&utm_term=CopyURL&ad_od=share&utm_user=plusmember&gx=RnFsxDMNOWDazdTCET-j0isflP8");
        System.out.printf(command);
    }

    @Test
    void getGoodsInfo() {
        String goodsInfo = unionJdProxy.getGoodsInfo("https://item.m.jd.com/product/100018864897.html?&utm_source=iosapp&utm_medium=appshare&utm_campaign=t_335139774&utm_term=CopyURL&ad_od=share&utm_user=plusmember&gx=RnFsxDMNOWDazdTCET-j0isflP8");
        System.out.println(goodsInfo);

        goodsInfo = unionJdProxy.getGoodsInfo("https://item.m.jd.com/product/10056670674874.html?&utm_source=iosapp&utm_medium=appshare&utm_campaign=t_335139774&utm_term=CopyURL&ad_od=share&utm_user=plusmember&gx=RnFsxDMNOWDazdTCET-j0isflP8");
        System.out.println(goodsInfo);
    }
}