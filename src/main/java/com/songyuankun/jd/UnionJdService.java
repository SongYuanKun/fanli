package com.songyuankun.jd;

import com.songyuankun.jd.repository.JdUserRepository;
import com.songyuankun.jd.repository.entity.JdUserPO;
import com.songyuankun.unionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

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
        return jdUserRepository.findFirstByWechatUser(id).orElseGet(jdUserRepository::findFirstByDefaultUserIsTrue);
    }

    @Override
    public String getGoodInfo(String skuUrl, String fromUserId) {
        String skuId = JdUtil.getSkuId(skuUrl);
        if (StringUtils.isBlank(skuId)) {
            return null;
        }
        JdUserPO jdUser = getJdUser(fromUserId);
        return unionJdProxy.getGoodsInfo(skuUrl, jdUser.getPositionId());
    }
}
