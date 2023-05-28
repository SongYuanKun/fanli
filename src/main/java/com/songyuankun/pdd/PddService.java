package com.songyuankun.pdd;

import com.pdd.pop.sdk.http.api.pop.response.PddDdkGoodsDetailResponse;
import com.pdd.pop.sdk.http.api.pop.response.PddDdkGoodsZsUnitUrlGenResponse;
import com.songyuankun.unionService;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author songyuankun
 */
@Service
public class PddService implements unionService {
    private final PddProxy pddProxy;

    public PddService(PddProxy pddProxy) {
        this.pddProxy = pddProxy;
    }

    @Override
    public String getGoodInfo(String url, String fromUserId) {
        PddDdkGoodsZsUnitUrlGenResponse.GoodsZsUnitGenerateResponse pddDdkGoodsZsUnitUrlGen = pddProxy.getPddDdkGoodsZsUnitUrlGen(url);
        String pddDdkGoodsZsUnitUrlGenUrl = pddDdkGoodsZsUnitUrlGen.getUrl();
        if (StringUtils.isBlank(pddDdkGoodsZsUnitUrlGenUrl)) {
            return null;
        }
        PddDdkGoodsDetailResponse.GoodsDetailResponseGoodsDetailsItem pddGoodInfo = pddProxy.getPddGoodInfo(pddDdkGoodsZsUnitUrlGenUrl);
        if (ObjectUtils.isEmpty(pddDdkGoodsZsUnitUrlGen)) {
            return null;
        }

        return "商品名称：" + pddGoodInfo.getGoodsName() + "\r\n" +
                "价格：" + BigDecimal.valueOf(pddGoodInfo.getMinGroupPrice())
                .multiply(new BigDecimal("0.01"))
                .setScale(2, RoundingMode.DOWN) + "\r\n" +
                "返佣比例：" +
                BigDecimal.valueOf(pddGoodInfo.getPromotionRate())
                        .multiply(new BigDecimal("0.1"))
                        .setScale(1, RoundingMode.DOWN) + "%\r\n" +
                "预计返佣：" +
                BigDecimal.valueOf(pddGoodInfo.getMinGroupPrice())
                        .multiply(BigDecimal.valueOf(pddGoodInfo.getPromotionRate()))
                        .multiply(new BigDecimal("0.00001"))
                        .setScale(2, RoundingMode.DOWN) +
                "\r\n" +
                "下单地址：" + pddDdkGoodsZsUnitUrlGen.getMultiGroupMobileShortUrl();
    }
}
