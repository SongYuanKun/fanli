package com.songyuankun.jd;

import com.songyuankun.jd.repository.JdUserRepository;
import com.songyuankun.jd.repository.entity.JdUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author songyuankun
 */
@Service
@Slf4j
public class UnionJdService {


    @Autowired
    private JdUserRepository jdUserRepository;
    @Autowired
    private  UnionJdProxy unionJdProxy;

    public JdUser getJdUser(String id) {
        return jdUserRepository.findFirstByWechatUser(id).orElseGet(() -> createJdUser(id));
    }

    private JdUser createJdUser(String wechatUser) {
        if (StringUtils.isBlank(wechatUser)) {
            return null;
        }
        String position = unionJdProxy.createPosition(wechatUser);
        JdUser jdUser = new JdUser();
        jdUser.setWechatUser(wechatUser);
        jdUser.setPositionId(position);
        return jdUserRepository.save(jdUser);
    }

    public String getGoodsInfo(String skuUrl, String fromUserId) {
        JdUser jdUser = getJdUser(fromUserId);
        String skuId = JdUtil.getSkuId(skuUrl);
        if (StringUtils.isBlank(skuId)) {
            return null;
        }
        return unionJdProxy.getGoodsInfo(skuUrl, jdUser.getPositionId());
    }
}
