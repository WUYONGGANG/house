/** 
 * Project Name: hzf_smart
 * File Name: TradeOrderRepository.java 
 * Package Name: com.huifenqi.usercomm.dao.huizhaofang 
 * Date: 2018年1月17日下午5:31:53
 * Copyright (c) 2017, www.huizhaofang.com All Rights Reserved. 
 * 
 */
package com.huifenqi.usercomm.dao;

import com.huifenqi.usercomm.domain.TradeOrder;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 * ClassName: TradeOrderRepository date: 2018年1月17日 下午5:31:53 Description:
 * 
 * @author arison
 * @version
 * @since JDK 1.8
 */
public interface TradeOrderRepository extends CrudRepository<TradeOrder, Long> {
    Page<TradeOrder> findByUserIdOrderByCreateTimeDesc(long userId, Pageable Pageable);
    
    @Query("select t from TradeOrder t where t.userId = ?1 and t.orderStatus <> ?2  order by t.createTime desc  ")
	Page<TradeOrder> findChargeList(long userId, int orderStatus,Pageable Pageable);
    
	Page<TradeOrder> findByBusinessNoAndProductTypeNoAndProductNo(String businessNo, String productTypeNo, String productNo, Pageable Pageable);
	TradeOrder findByOrderNo(String orderNo);
}
