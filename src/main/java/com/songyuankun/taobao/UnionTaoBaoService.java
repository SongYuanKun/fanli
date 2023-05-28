package com.songyuankun.taobao;

import com.songyuankun.unionService;
import com.taobao.api.response.TbkDgMaterialOptionalResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * @author songyuankun
 */
@Service
public class UnionTaoBaoService implements unionService {
    private final UnionTaoBaoProxy unionTaoBaoProxy;

    public UnionTaoBaoService(UnionTaoBaoProxy unionTaoBaoProxy) {
        this.unionTaoBaoProxy = unionTaoBaoProxy;
    }


    @Override
    public String getGoodInfo(String url, String fromUserId) {
        List<TbkDgMaterialOptionalResponse.MapData> mapDataList = unionTaoBaoProxy.getCommand(url);
        if (CollectionUtils.isEmpty(mapDataList)) {
            return null;
        }
        TbkDgMaterialOptionalResponse.MapData mapData = mapDataList.get(0);
        return "商品名称：" + mapData.getTitle() + "\r\n" +
                "价格：" + mapData.getReservePrice() + "\r\n" +
                "返佣比例：" + mapData.getCommissionRate() + "‰\r\n" +
                "预计返佣：" +
                new BigDecimal(mapData.getReservePrice())
                        .multiply(new BigDecimal(mapData.getCommissionRate()))
                        .multiply(new BigDecimal("0.001"))
                        .setScale(2, RoundingMode.UP)
                + "\r\n" +
                "下单地址：https:" +
                StringUtils.defaultIfBlank(
                        mapData.getCouponShareUrl(),
                        mapData.getUrl()
                );
    }
}
