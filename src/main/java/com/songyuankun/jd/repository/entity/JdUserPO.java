package com.songyuankun.jd.repository.entity;

import com.songyuankun.common.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Proxy;

import javax.persistence.*;

/**
 * @author songyuankun
 */
@Proxy(lazy = false)
@Entity
@Table(name = "jd_user")
@ToString
@Getter
@Setter
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class JdUserPO extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "wechat_user", nullable = false)
    private String wechatUser;

    @Column(name = "position_id", nullable = false)
    private String positionId;

    @Column(name = "default_user", nullable = false, columnDefinition = "bit default 0")
    private Boolean defaultUser;

}
