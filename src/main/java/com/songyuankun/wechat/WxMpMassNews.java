package com.songyuankun.wechat;

import com.jd.open.api.sdk.internal.JSON.JSON;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@Data
public class WxMpMassNews implements Serializable {
    private static final long serialVersionUID = 565937155013581016L;

    private List<WxMpNewsArticle> articles = new ArrayList<>();

    public void addArticle(WxMpNewsArticle article) {
        this.articles.add(article);
    }

    public boolean isEmpty() {
        return this.articles == null || this.articles.isEmpty();
    }

    @Override
    public String toString() {
        return JSON.toString(this);
    }

}
