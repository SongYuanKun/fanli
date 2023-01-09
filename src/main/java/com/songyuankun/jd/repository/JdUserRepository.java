package com.songyuankun.jd.repository;

import com.songyuankun.jd.repository.entity.JdUserPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author songyuankun
 */
@Repository
public interface JdUserRepository extends JpaRepository<JdUserPO, Integer>, JpaSpecificationExecutor<JdUserPO> {

    Optional<JdUserPO> findFirstByWechatUser(String wechatUser);

}
