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
                .header(Header.COOKIE, "shshshfpb=cAYXTTeRd83x8SbRDRU-C5A; shshshfpa=b50522f7-1351-f8f1-663a-ba1debf5795c-1653792131; whwswswws=; warehistory=\"10056670674874,10056670674874,10056670674874,10056670674874,10056670674874,10056670674874,10056670674874,10050064328710,100022193732,100001621535,100014384352,\"; autoOpenApp_downCloseDate_jd_homePage=1665140250682_1; jcap_dvzw_fp=tVLtMZvmNwN96JdfzFSRAGA-IIDwY6FawHPMxnC_ky_PFaGjfJlEhcfJA8YLcIwEAq4xYQ==; __jdv=76161171|direct|-|none|-|1666416713302; __jdu=16664167133011822377983; areaId=1; ipLoc-djd=1-2901-0-0; PCSYCityID=CN_110000_110100_110114; shshshfp=c10d629c39a7673012cfe47cf00f22eb; user-key=0e0c1b0c-68df-4d14-b249-bfa54eb25bda; 3AB9D23F7A4B3C9B=RJKW6NTITPSEIDL7TQZHRNFL3FS5XU2BQIDCBOO2BCSW7ASLI3XUBFYC6RKVJH4P3JPRTWZBA7TW2OQIFP7YXHZECA; TrackID=13-R3ku1X-SP3sLsX7Nz0C6Ttet5cKC05cSgpDOScRjknU1WRQw9K-5Os92sr3M3tR9qHOITSwtzci9dfS0hnsytsMROJdy_wvgbvXKu5y0A; pinId=lyNEjbpwGPQJ3BsDd-BBl7V9-x-f3wj7; pin=jd_5861e418610ca; unick=jd_182011lyr; ceshi3.com=203; _tp=Lqz99JyKiqlWLWkf9WpF9j4uwwduOKTO26OwS3pC9Iw%3D; _pst=jd_5861e418610ca; __jdc=209449046; __jda=209449046.16664167133011822377983.1666416713.1666491011.1666499161.3; thor=0B5674FC93F071F28C986463AF01DE42F5794F2A312B1D47FC79BFD045D9E7C9E4C4914EB96B883F9204B9BB811BCA703E0EE085856E1283379CA3F86E016E1175D33EF269BDF8865BDE95EA4DF7F65CC7DB9CE55C93191F6CC00C09A629EDBC71AEBC06AAB3937A50E745EB47920127D5F5F442AC5E010C498056C4B4F0C7999E9ADCA5B3466A54247F7B657FCFAC2B018C2101365F744B7296BCE147BC4F63; __jdb=209449046.27.16664167133011822377983|3.1666499161; RT=\"z=1&dm=jd.com&si=6jepue0pqdh&ss=l9kv0sdf&sl=j&tt=1cj&ld=e2kl&nu=e40add4e0e00b1080626db93d2f6e517&cl=e02d\"");
        try (HttpResponse execute = httpRequest.execute()) {
            return JSONObject.parseObject(execute.body());
        }
    }
}
