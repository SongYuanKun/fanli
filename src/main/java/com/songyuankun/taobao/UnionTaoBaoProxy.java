package com.songyuankun.taobao;

import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.request.TbkDgMaterialOptionalRequest;
import com.taobao.api.response.TbkDgMaterialOptionalResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * @author songyuankun
 */
@Service
@Slf4j
public class UnionTaoBaoProxy {

    @Value("${taobao.app_key}")
    private String appKey;
    @Value("${taobao.secret}")
    private String secret;

    @Value("${taobao.adzone_id}")
    private Long adzoneId;

    private static final String API_URL = "https://eco.taobao.com/router/rest";

    public String getCommand(String keyWord) {
        TaobaoClient client = new DefaultTaobaoClient(API_URL, appKey, secret);
        TbkDgMaterialOptionalRequest req = new TbkDgMaterialOptionalRequest();
        req.setAdzoneId(adzoneId);
        req.setQ(keyWord);
        TbkDgMaterialOptionalResponse rsp;
        try {
            rsp = client.execute(req);
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
        List<TbkDgMaterialOptionalResponse.MapData> resultList = rsp.getResultList();
        if (CollectionUtils.isEmpty(resultList)) {
            return "该商品不参与优惠";
        }
        TbkDgMaterialOptionalResponse.MapData mapData = resultList.get(0);
        return "商品名称：" + mapData.getTitle() + "\r\n" +
                "价格：" + mapData.getReservePrice() + "\r\n" +
                "返佣比例：" + mapData.getCommissionRate() + "‰\r\n" +
                "预计返佣：" +
                new BigDecimal(mapData.getReservePrice())
                        .multiply(new BigDecimal(mapData.getCommissionRate()))
                        .multiply(new BigDecimal("0.001"))
                        .setScale(2, RoundingMode.UP)
                + "\r\n" +
                "下单地址：" +
                StringUtils.defaultIfBlank(
                        mapData.getCouponShareUrl(),
                        mapData.getUrl()
                ) +
                "";
    }

}
