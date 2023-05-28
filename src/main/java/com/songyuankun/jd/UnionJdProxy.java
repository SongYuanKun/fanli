package com.songyuankun.jd;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.jd.open.api.sdk.DefaultJdClient;
import com.jd.open.api.sdk.JdClient;
import com.jd.open.api.sdk.domain.kplunion.GoodsService.request.query.JFGoodsReq;
import com.jd.open.api.sdk.domain.kplunion.GoodsService.response.query.*;
import com.jd.open.api.sdk.domain.kplunion.OrderService.request.query.OrderRowReq;
import com.jd.open.api.sdk.domain.kplunion.OrderService.response.query.OrderRowQueryResult;
import com.jd.open.api.sdk.domain.kplunion.OrderService.response.query.OrderRowResp;
import com.jd.open.api.sdk.domain.kplunion.promotioncommon.PromotionService.request.get.PromotionCodeReq;
import com.jd.open.api.sdk.domain.kplunion.promotioncommon.PromotionService.response.get.PromotionCodeResp;
import com.jd.open.api.sdk.request.kplunion.UnionOpenGoodsJingfenQueryRequest;
import com.jd.open.api.sdk.request.kplunion.UnionOpenGoodsPromotiongoodsinfoQueryRequest;
import com.jd.open.api.sdk.request.kplunion.UnionOpenOrderRowQueryRequest;
import com.jd.open.api.sdk.request.kplunion.UnionOpenPromotionCommonGetRequest;
import com.jd.open.api.sdk.response.kplunion.UnionOpenGoodsJingfenQueryResponse;
import com.jd.open.api.sdk.response.kplunion.UnionOpenGoodsPromotiongoodsinfoQueryResponse;
import com.jd.open.api.sdk.response.kplunion.UnionOpenOrderRowQueryResponse;
import com.jd.open.api.sdk.response.kplunion.UnionOpenPromotionCommonGetResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author songyuankun
 */
@Service
@Slf4j
public class UnionJdProxy {

    private static final String API_URL = "https://api.jd.com/routerjson";
    @Value("${jd.app_key}")
    private String appKey;
    @Value("${jd.secret_key}")
    private String secretKey;
    @Value("${jd.site_id}")
    private Long siteId;

    public String getCommand(final String skuUrl, String positionId) {
        String materialId = null;
        String pattern = "https://item(.m|).jd.com/(product/|)\\d*.html";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(skuUrl);
        if (m.find()) {
            materialId = m.group();
        }
        if (Objects.isNull(materialId)) {
            return null;
        }
        return getGoodInfoByMaterialId(positionId, materialId);
    }

    public String getGoodInfoByMaterialId(String positionId, String materialId) {
        JdClient client = new DefaultJdClient(API_URL, null, appKey, secretKey);
        UnionOpenPromotionCommonGetRequest request = new UnionOpenPromotionCommonGetRequest();
        PromotionCodeReq promotionCodeReq = new PromotionCodeReq();
        promotionCodeReq.setSiteId(String.valueOf(siteId));
        promotionCodeReq.setPositionId(Long.parseLong(positionId));
        promotionCodeReq.setMaterialId(materialId);

        request.setPromotionCodeReq(promotionCodeReq);
        request.setVersion("1.0");

        try {
            UnionOpenPromotionCommonGetResponse execute = client.execute(request);
            PromotionCodeResp data = execute.getGetResult().getData();
            return ObjectUtils.isEmpty(data) ? null : StringUtils.defaultIfBlank(data.getJCommand(), data.getClickURL());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getGoodsInfo(String skuUrl, String positionId) {
        String url = getCommand(skuUrl, positionId);
        if (StringUtils.isBlank(url)) {
            return "该商品不参与优惠";
        }
        String skuId = JdUtil.getSkuId(skuUrl);
        if (StringUtils.isBlank(skuId)) {
            return null;
        }

        JdClient client = new DefaultJdClient(API_URL, null, appKey, secretKey);
        UnionOpenGoodsPromotiongoodsinfoQueryRequest request = new UnionOpenGoodsPromotiongoodsinfoQueryRequest();
        request.setSkuIds(skuId);
        request.setVersion("1.0");
        try {
            UnionOpenGoodsPromotiongoodsinfoQueryResponse execute = client.execute(request);
            PromotionQueryResult queryResult = execute.getQueryResult();
            PromotionGoodsResp[] data = queryResult.getData();
            if (ArrayUtils.isEmpty(data)) {
                return "该商品不参与优惠";
            }
            PromotionGoodsResp datum = data[0];
            return "商品名称：" + datum.getGoodsName() + "\r\n" + "价格：" + datum.getUnitPrice() + "\r\n" + "返佣比例：" + datum.getCommisionRatioWl() + "%\r\n" + "预计返佣：" + BigDecimal.valueOf(datum.getUnitPrice()).multiply(BigDecimal.valueOf(datum.getCommisionRatioWl())).multiply(new BigDecimal("0.01")).setScale(2, RoundingMode.DOWN) + "\r\n" + "下单地址：" + url;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public String getGoodsUrl(String skuUrl, String positionId) {
        String url = getCommand(skuUrl, positionId);
        if (StringUtils.isBlank(url)) {
            return "该商品不参与优惠";
        }
        String skuId = JdUtil.getSkuId(skuUrl);
        if (StringUtils.isBlank(skuId)) {
            return null;
        }

        JdClient client = new DefaultJdClient(API_URL, null, appKey, secretKey);
        UnionOpenGoodsPromotiongoodsinfoQueryRequest request = new UnionOpenGoodsPromotiongoodsinfoQueryRequest();
        request.setSkuIds(skuId);
        request.setVersion("1.0");
        try {
            UnionOpenGoodsPromotiongoodsinfoQueryResponse execute = client.execute(request);
            PromotionQueryResult queryResult = execute.getQueryResult();
            PromotionGoodsResp[] data = queryResult.getData();
            if (ArrayUtils.isEmpty(data)) {
                return "该商品不参与优惠";
            }
            PromotionGoodsResp datum = data[0];
            return url;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public String getJingFen(String positionId, Integer eliteId) {
        StringBuilder shareText = new StringBuilder();
        JdClient client = new DefaultJdClient(API_URL, null, appKey, secretKey);
        UnionOpenGoodsJingfenQueryRequest request = new UnionOpenGoodsJingfenQueryRequest();
        request.setVersion("1.0");
        JFGoodsReq jfGoodsReq = new JFGoodsReq();
        jfGoodsReq.setEliteId(eliteId);
        request.setGoodsReq(jfGoodsReq);
        try {
            UnionOpenGoodsJingfenQueryResponse execute = client.execute(request);
            JingfenQueryResult queryResult = execute.getQueryResult();
            JFGoodsResp[] data = queryResult.getData();
            for (JFGoodsResp jfGoodsResp : data) {
                String skuName = jfGoodsResp.getSkuName();
                String materialUrl = "https://" + jfGoodsResp.getMaterialUrl();
                CouponInfo couponInfos = jfGoodsResp.getCouponInfo();
                PriceInfo priceInfo = jfGoodsResp.getPriceInfo();
                SeckillInfo seckillInfo = jfGoodsResp.getSeckillInfo();
                Integer lowestPriceType = priceInfo.getLowestPriceType();
                Coupon[] couponList = couponInfos.getCouponList();
                if (ArrayUtils.isEmpty(couponList)) {
                    if (lowestPriceType == 3) {
                        shareText.append(String.format("【秒杀】%s\n——————————\n  【原价】¥%s\n 【秒杀价】¥%s\n抢购地址：%s\n", skuName, seckillInfo.getSeckillOriPrice(), seckillInfo.getSeckillPrice(), getGoodsUrl(materialUrl, positionId)));
                    } else if (lowestPriceType == 2) {
                        shareText.append(String.format("【拼购】%s\n——————————\n  【原价】¥%s\n 【拼购价】¥%s\n抢购地址：%s\n", skuName, priceInfo.getPrice(), priceInfo.getLowestPrice(), getGoodsUrl(materialUrl, positionId)));
                    } else {
                        shareText.append(String.format("【京东】%s\n——————————\n 【爆款价】¥%s\n抢购地址：%s\n", skuName, priceInfo.getPrice(), getGoodsUrl(materialUrl, positionId)));
                    }
                } else {
                    double price = priceInfo.getPrice();
                    double lowestCouponPrice = priceInfo.getLowestCouponPrice();
                    double lowestPrice = priceInfo.getLowestPrice();
                    if (lowestPriceType == 3) {
                        shareText.append(String.format("【秒杀】%s\n——————————\n  【原价】¥%s\n 【券后秒杀价】¥%s\n抢购地址：%s\n", skuName, seckillInfo.getSeckillOriPrice(), lowestPrice, getGoodsUrl(materialUrl, positionId)));
                    } else if (lowestPriceType == 2) {
                        shareText.append(String.format("【拼购】%s\n——————————\n  【原价】¥%s\n 【券后拼购价】¥%s\n抢购地址：%s\n", skuName, price, lowestPrice, getGoodsUrl(materialUrl, positionId)));
                    } else {
                        shareText.append(String.format("【京东】%s\n——————————\n  【爆款价】¥%s\n 【用卷价】¥%s\n抢购地址：%s\n", skuName, price, lowestCouponPrice, getGoodsUrl(materialUrl, positionId)));
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return shareText.toString();
    }

    public List<OrderRowResp> getOrderInfo(String positionId) {
        JdClient client = new DefaultJdClient(API_URL, null, appKey, secretKey);
        UnionOpenOrderRowQueryRequest request = new UnionOpenOrderRowQueryRequest();
        OrderRowReq orderReq = new OrderRowReq();
        orderReq.setPageIndex(1);
        orderReq.setPageIndex(1000);
        orderReq.setType(3);
        orderReq.setStartTime(LocalDateTimeUtil.format(LocalDateTimeUtil.now().minusMinutes(-30), "yyyy-MM-dd HH:mm:ss"));
        orderReq.setEndTime(LocalDateTimeUtil.format(LocalDateTimeUtil.now(), "yyyy-MM-dd HH:mm:ss"));

        request.setOrderReq(orderReq);
        request.setVersion("1.0");
        try {
            UnionOpenOrderRowQueryResponse response = client.execute(request);
            OrderRowQueryResult queryResult = response.getQueryResult();
            return Arrays.stream(queryResult.getData()).filter(orderRowResp -> Objects.equals(orderRowResp.getPositionId(), Long.parseLong(positionId))).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
