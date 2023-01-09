package com.songyuankun.jd;

import com.songyuankun.jd.repository.JdUserRepository;
import com.songyuankun.jd.repository.entity.JdUserPO;
import com.songyuankun.unionService;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * @author songyuankun
 */
@Service
@Slf4j
public class UnionJdService implements unionService {

    private final JdUserRepository jdUserRepository;
    private final UnionJdProxy unionJdProxy;

    public UnionJdService(JdUserRepository jdUserRepository, UnionJdProxy unionJdProxy) {
        this.jdUserRepository = jdUserRepository;
        this.unionJdProxy = unionJdProxy;
    }

    public JdUserPO getJdUser(String id) {
        return jdUserRepository.findFirstByWechatUser(id).orElseGet(() -> createJdUser(id));
    }

    private JdUserPO createJdUser(String wechatUser) {
        if (StringUtils.isBlank(wechatUser)) {
            return null;
        }
        String position = unionJdProxy.createPosition(wechatUser);
        JdUserPO jdUserPO = new JdUserPO();
        jdUserPO.setWechatUser(wechatUser);
        jdUserPO.setPositionId(position);
        return jdUserRepository.save(jdUserPO);
    }

    @Override
    public String getGoodInfo(String skuUrl, String fromUserId) {
        String skuId = JdUtil.getSkuId(skuUrl);
        if (StringUtils.isBlank(skuId)) {
            return null;
        }
        JdUserPO jdUserPO = getJdUser(fromUserId);
        return unionJdProxy.getGoodsInfo(skuUrl, jdUserPO.getPositionId());
    }
}
