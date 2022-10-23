package com.songyuankun.jd.repository;

import com.songyuankun.jd.repository.entity.JdUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author songyuankun
 */
@Repository
public interface JdUserRepository extends JpaRepository<JdUser, Integer>, JpaSpecificationExecutor<JdUser> {

    Optional<JdUser> findFirstByWechatUser(String wechatUser);

}
