package com.songyuankun.jd;

import com.songyuankun.jd.repository.JdUserRepository;
import com.songyuankun.jd.repository.entity.JdUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @author songyuankun
 */
@Service
@Slf4j
public class UnionJdService {

    private final JdUserRepository jdUserRepository;
    private final UnionJdProxy unionJdProxy;

    public UnionJdService(JdUserRepository jdUserRepository, UnionJdProxy unionJdProxy) {
        this.jdUserRepository = jdUserRepository;
        this.unionJdProxy = unionJdProxy;
    }

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
        String skuId = JdUtil.getSkuId(skuUrl);
        if (StringUtils.isBlank(skuId)) {
            return null;
        }
        JdUser jdUser = getJdUser(fromUserId);
        return unionJdProxy.getGoodsInfo(skuUrl, jdUser.getPositionId());
    }
}
