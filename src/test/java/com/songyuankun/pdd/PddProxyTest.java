package com.songyuankun.pdd;

import com.alibaba.fastjson2.JSON;
import com.pdd.pop.sdk.http.PopClient;
import com.pdd.pop.sdk.http.PopHttpClient;
import com.pdd.pop.sdk.http.api.pop.request.PddDdkGoodsZsUnitUrlGenRequest;
import com.pdd.pop.sdk.http.api.pop.response.PddDdkGoodsZsUnitUrlGenResponse;

class PddProxyTest {

    public static String clientId = "ccc741f4a39343ad845df0d866d0c6e7";
    public static String clientSecret = "4f41b0d23b3fcb09875908ddf321de632d452843";
    public static String pid = "36287817_261269005";
    public static String url = "https://mobile.yangkeduo.com/goods1.html?_wvx=10&refer_share_uin=SECJ2YUB4RPLPD7QSY35J33UWU_GEXDA&refer_share_id=4qHS88qHVt3ajx8dVsSnrIELccYfD0uL&share_uin=SECJ2YUB4RPLPD7QSY35J33UWU_GEXDA&page_from=26&_wv=41729&refer_share_channel=message&pxq_secret_key=CJJRDV6XOZ3NGG2OCDOAKEGLJK6JSS6NTO3MHYGQF7BH6BZD4YNQ&goods_id=419769859115#pushState";

        public static void main(String[] args) {
        PopClient client = new PopHttpClient(clientId, clientSecret);

        PddDdkGoodsZsUnitUrlGenRequest request = new PddDdkGoodsZsUnitUrlGenRequest();
        request.setPid(pid);
        request.setSourceUrl(url);
        PddDdkGoodsZsUnitUrlGenResponse response;
        try {
            response = client.syncInvoke(request);
            System.out.println(JSON.toJSONString(response));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}