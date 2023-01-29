package com.songyuankun.taobao;

import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.request.TbkDgMaterialOptionalRequest;
import com.taobao.api.response.TbkDgMaterialOptionalResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author songyuankun
 */
@Service
@Slf4j
public class UnionTaoBaoProxy {

    private static final String API_URL = "https://eco.taobao.com/router/rest";
    @Value("${taobao.app_key}")
    private String appKey;
    @Value("${taobao.secret}")
    private String secret;
    @Value("${taobao.adzone_id}")
    private Long adzoneId;

    public List<TbkDgMaterialOptionalResponse.MapData> getCommand(String keyWord) {
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
        return resultList;
    }

}
