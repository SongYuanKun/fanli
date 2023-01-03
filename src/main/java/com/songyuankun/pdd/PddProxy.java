package com.songyuankun.pdd;

import com.alibaba.fastjson2.JSON;
import com.pdd.pop.sdk.http.PopClient;
import com.pdd.pop.sdk.http.PopHttpClient;
import com.pdd.pop.sdk.http.api.pop.request.PddDdkGoodsDetailRequest;
import com.pdd.pop.sdk.http.api.pop.request.PddDdkGoodsZsUnitUrlGenRequest;
import com.pdd.pop.sdk.http.api.pop.response.PddDdkGoodsDetailResponse;
import com.pdd.pop.sdk.http.api.pop.response.PddDdkGoodsZsUnitUrlGenResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author songyuankun
 */
@Service
public class PddProxy {
    @Value("${pdd.pid}")
    private String pid;
    @Value("${pdd.client_id}")
    private String clientId;
    @Value("${pdd.client_secret}")
    private String clientSecret;

    public PddDdkGoodsZsUnitUrlGenResponse.GoodsZsUnitGenerateResponse getPddDdkGoodsZsUnitUrlGen(String url) {
        PopClient client = new PopHttpClient(clientId, clientSecret);
        PddDdkGoodsZsUnitUrlGenRequest request = new PddDdkGoodsZsUnitUrlGenRequest();
        request.setPid(pid);
        request.setSourceUrl(url);
        PddDdkGoodsZsUnitUrlGenResponse response;
        try {
            response = client.syncInvoke(request);
            return response.getGoodsZsUnitGenerateResponse();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public PddDdkGoodsDetailResponse.GoodsDetailResponseGoodsDetailsItem getPddGoodInfo(String url) {
        String goodsSign = getGoodsSign(url);
        PopClient client = new PopHttpClient(clientId, clientSecret);
        PddDdkGoodsDetailRequest request = new PddDdkGoodsDetailRequest();
        request.setPid(pid);
        request.setGoodsSign(goodsSign);
        try {
            PddDdkGoodsDetailResponse response = client.syncInvoke(request);
            System.out.println(JSON.toJSONString(response));
            return response.getGoodsDetailResponse().getGoodsDetails().get(0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static String getGoodsSign(String skuUrl) {
        String pattern = "goods_sign=\\w*";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(skuUrl);
        if (!m.find()) {
            return "";
        }
        String url = m.group();
        return url.substring(url.lastIndexOf("goods_sign=") + 11);
    }
}
