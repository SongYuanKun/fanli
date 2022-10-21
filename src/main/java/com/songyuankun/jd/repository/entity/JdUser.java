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

@Proxy(lazy = false)
@Entity
@Table(name = "jd_user")
@ToString
@Getter
@Setter
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class JdUser extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private String id;

}
