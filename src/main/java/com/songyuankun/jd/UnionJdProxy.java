package com.songyuankun.jd;

import cn.hutool.http.*;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.jd.open.api.sdk.DefaultJdClient;
import com.jd.open.api.sdk.JdClient;
import com.jd.open.api.sdk.domain.kplunion.GoodsService.response.query.PromotionGoodsResp;
import com.jd.open.api.sdk.domain.kplunion.GoodsService.response.query.PromotionQueryResult;
import com.jd.open.api.sdk.domain.kplunion.promotioncommon.PromotionService.request.get.PromotionCodeReq;
import com.jd.open.api.sdk.domain.kplunion.promotioncommon.PromotionService.response.get.PromotionCodeResp;
import com.jd.open.api.sdk.request.kplunion.UnionOpenGoodsPromotiongoodsinfoQueryRequest;
import com.jd.open.api.sdk.request.kplunion.UnionOpenPromotionCommonGetRequest;
import com.jd.open.api.sdk.response.kplunion.UnionOpenGoodsPromotiongoodsinfoQueryResponse;
import com.jd.open.api.sdk.response.kplunion.UnionOpenPromotionCommonGetResponse;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    @Value("${jd.web_cookie}")
    private String webCookie;

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
            System.out.println(JSON.toJSONString(execute));
            PromotionCodeResp data = execute.getGetResult().getData();
            return StringUtils.defaultIfBlank(data.getJCommand(), data.getClickURL());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getGoodsInfo(String skuUrl, String positionId) {

        String url = getCommand(skuUrl, positionId);
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
            return "商品名称：" + datum.getGoodsName() + "\r\n" +
                    "价格：" + datum.getUnitPrice() + "\r\n" +
                    "返佣比例：" + datum.getCommisionRatioWl() + "%\r\n" +
                    "预计返佣：" +
                    BigDecimal.valueOf(datum.getUnitPrice())
                            .multiply(BigDecimal.valueOf(datum.getCommisionRatioWl()))
                            .multiply(new BigDecimal("0.01"))
                            .setScale(2, RoundingMode.DOWN) +
                    "\r\n" +
                    "下单地址：" + url +
                    "";

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Synchronized
    public String createPosition(String id) {
        String url = "https://api.m.jd.com/api";
        JSONObject param = new JSONObject();
        param.put("siteId", siteId);
        param.put("spaceName", id);
        param.put("type", 1);
        param.put("unionType", 1);

        JSONObject body = new JSONObject();
        body.put("funName", "savePromotionSite");
        JSONObject result = queryUnionJdWeb(url, param, body);
        if (Objects.equals(result.getInteger("code"), HttpStatus.HTTP_OK)) {
            return getPositionId();
        } else {
            throw new RuntimeException("用户创建失败");
        }
    }

    private String getPositionId() {
        String url = "https://api.m.jd.com/api";
        JSONObject param = new JSONObject();
        param.put("id", siteId);
        param.put("onTYpe", 2);
        param.put("promotionType", 1);
        param.put("pageNo", 1);
        param.put("pageSize", 1);

        JSONObject body = new JSONObject();
        body.put("funName", "listPromotionSite");
        JSONObject result = queryUnionJdWeb(url, param, body);
        return result.getJSONObject("data").getJSONArray("result").getJSONObject(0).getString("id");

    }

    private JSONObject queryUnionJdWeb(String url, JSONObject param, JSONObject body) {
        body.put("param", param);


        JSONObject jsonObject = new JSONObject();
        jsonObject.put("functionId", "unionPromotion");
        jsonObject.put("appid", "u");
        jsonObject.put("_", System.currentTimeMillis());
        jsonObject.put("loginType", 3);
        jsonObject.put("body", body);
        HttpRequest httpRequest = HttpUtil.createGet(url)
                .form(jsonObject)
                .header(Header.COOKIE, webCookie);
        try (HttpResponse execute = httpRequest.execute()) {
            return JSONObject.parseObject(execute.body());
        }
    }
}
