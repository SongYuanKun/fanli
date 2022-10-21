package com.songyuankun.jd;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jd.open.api.sdk.DefaultJdClient;
import com.jd.open.api.sdk.JdClient;
import com.jd.open.api.sdk.domain.kplunion.PositionService.request.create.PositionReq;
import com.jd.open.api.sdk.request.kplunion.UnionOpenPositionCreateRequest;
import com.jd.open.api.sdk.response.kplunion.UnionOpenPositionCreateResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
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


    public String getCommand(final String skuUrl) {

        String innerSkuUrl = null;

        String pattern = "https://item(.m|).jd.com/(product/|)\\d*.html";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(skuUrl);
        if (m.find()) {
            innerSkuUrl = m.group();
        }
        if (Objects.isNull(innerSkuUrl)) {
            return null;
        }

        String timestamp = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
        String version = "1.0";
        String method = "jd.union.open.promotion.common.get";
        JSONObject jsonObject = new JSONObject();
        JSONObject promotionCodeReq = new JSONObject();
        promotionCodeReq.put("materialId", innerSkuUrl);
        promotionCodeReq.put("siteId", siteId);
        jsonObject.put("promotionCodeReq", promotionCodeReq);
        String paramJson = jsonObject.toJSONString();
        String sign;
        try {
            sign = JdUtil.buildSign(timestamp, version, method, paramJson, appKey, secretKey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        String queryUrl = API_URL
                + "?timestamp=" + timestamp
                + "&v=" + version
                + "&sign_method=md5"
                + "&format=json"
                + "&method=" + method
                + "&360buy_param_json=" + paramJson
                + "&app_key=" + appKey
                + "&sign=" + sign;
        String body;
        try (HttpResponse execute = HttpUtil.createGet(queryUrl).execute()) {
            body = execute.body();
        }
        JSONObject res = JSON.parseObject(body);
        JSONObject result = res.getJSONObject("jd_union_open_promotion_common_get_responce").getJSONObject("getResult");
        if (result.getJSONObject("data") != null) {
            return result.getJSONObject("data").getString("clickURL");
        } else {
            return result.getString("message");
        }
    }

    public String getGoodsInfo(String skuUrl) {

        String url = getCommand(skuUrl);

        String skuId = JdUtil.getSkuId(skuUrl);
        if (StringUtils.isBlank(skuId)) {
            return null;
        }

        String timestamp = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
        String version = "1.0";
        String method = "jd.union.open.goods.promotiongoodsinfo.query";
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("skuIds", skuId);
        String paramJson = jsonObject.toJSONString();
        String sign;
        try {
            sign = JdUtil.buildSign(timestamp, version, method, paramJson, appKey, secretKey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        String queryUrl = API_URL
                + "?timestamp=" + timestamp
                + "&v=" + version
                + "&sign_method=md5"
                + "&format=json"
                + "&method=" + method
                + "&360buy_param_json=" + paramJson
                + "&app_key=" + appKey
                + "&sign=" + sign;
        String body;
        try (HttpResponse execute = HttpUtil.createGet(queryUrl).execute()) {
            body = execute.body();
        }
        JSONObject res = JSON.parseObject(body);
        JSONArray jsonArray = res.getJSONObject("jd_union_open_goods_promotiongoodsinfo_query_responce").getJSONObject("queryResult").getJSONArray("data");
        JSONObject goodsInfo;
        if (jsonArray.size() > 0) {
            goodsInfo = jsonArray.getJSONObject(0);
        } else {
            return "该商品不参与优惠";
        }
        if (goodsInfo == null || url == null) {
            return null;
        }
        return "商品名称：" + goodsInfo.getString("goodsName") + "\r\n" +
                "价格：" + goodsInfo.getString("unitPrice") + "\r\n" +
                "返佣比例：" + goodsInfo.getString("commisionRatioPc") + "%\r\n" +
                "预计返佣：" +
                new BigDecimal(goodsInfo.getInteger("unitPrice"))
                        .multiply(new BigDecimal(goodsInfo.getInteger("commisionRatioPc")))
                        .multiply(new BigDecimal("0.01"))
                        .setScale(2, RoundingMode.UP) +
                "\r\n" +
                "下单地址：" + url +
                "";

    }


    public String createPosition(String id) throws Exception {

        JdClient client = new DefaultJdClient("SERVER_URL", null, appKey, secretKey);
        UnionOpenPositionCreateRequest request = new UnionOpenPositionCreateRequest();
        PositionReq positionReq = new PositionReq();
        positionReq.setUnionId(2025465528);
//        positionReq.setKey();
//        positionReq.setUnionType();
//        positionReq.setType();
//        positionReq.setSpaceNameList();
//        positionReq.setSiteId();

        request.setPositionReq(positionReq);
        request.setVersion("1.0");
        UnionOpenPositionCreateResponse response = client.execute(request);
        return "";
    }
}
