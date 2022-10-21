package com.songyuankun.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.MappedSuperclass;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@MappedSuperclass
@DynamicInsert
@DynamicUpdate
public class BaseEntity {

}