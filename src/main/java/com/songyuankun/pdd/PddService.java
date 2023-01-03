package com.songyuankun.pdd;

import com.pdd.pop.sdk.http.api.pop.response.PddDdkGoodsDetailResponse;
import com.songyuankun.EnableGetGoodInfo;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author songyuankun
 */
@Service
public class PddService implements EnableGetGoodInfo {
    private final PddProxy pddProxy;

    public PddService(PddProxy pddProxy) {
        this.pddProxy = pddProxy;
    }

    @Override
    public String getGoodInfo(String url, String fromUserId) {
//        PddDdkGoodsDetailResponse.GoodsDetailResponseGoodsDetailsItem pddGoodInfo = pddProxy.getPddGoodInfo(url);
//        if (ObjectUtils.isEmpty(pddGoodInfo)) {
//            return null;
//        }
        String urlGen = pddProxy.getPddDdkGoodsZsUnitUrlGen(url);

//        return "商品名称：" + pddGoodInfo.getGoodsName() + "\r\n" +
//            "价格：" + pddGoodInfo.getMinGroupPrice() + "\r\n" +
//            "返佣比例：" + pddGoodInfo.getActivityPromotionRate() + "%\r\n" +
//            "预计返佣：" +
//            BigDecimal.valueOf(pddGoodInfo.getActivityPromotionRate())
//                .multiply(BigDecimal.valueOf(pddGoodInfo.getMinGroupPrice()))
//                .multiply(new BigDecimal("0.001"))
//                .setScale(2, RoundingMode.DOWN) +
//            "\r\n" +
//            "下单地址：" + urlGen +
//            "";
        return null;
    }
}
