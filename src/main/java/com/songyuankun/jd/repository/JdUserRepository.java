package com.songyuankun.jd.repository;

import com.songyuankun.jd.repository.entity.JdUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * @author songyuankun
 */
@Repository
public interface JdUserRepository extends JpaRepository<JdUser, String>, JpaSpecificationExecutor<JdUser> {

}
