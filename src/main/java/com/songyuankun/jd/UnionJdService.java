package com.songyuankun.jd;

import com.songyuankun.jd.repository.JdUserRepository;
import com.songyuankun.jd.repository.entity.JdUser;
import lombok.extern.slf4j.Slf4j;
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
        return jdUserRepository.findById(id).orElseGet(() -> createJdUser(id));
    }

    private JdUser createJdUser(String id) {
        unionJdProxy.createPosition(id);
        JdUser jdUser = new JdUser();
        jdUser.setId(id);
        return jdUserRepository.save(jdUser);
    }


}
