/** 
* Project Name: hzf_platform 
* File Name: HousePictureRepository.java 
* Package Name: com.huifenqi.hzf_platform.dao.repository 
* Date: 2016年4月26日下午2:35:55 
* Copyright (c) 2016, www.huizhaofang.com All Rights Reserved. 
* 
*/
package com.huifenqi.hzf_platform.dao.repository.house;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.huifenqi.hzf_platform.context.Constants;
import com.huifenqi.hzf_platform.context.entity.phone.BlackPhone;

/**
 * ClassName: BlackPhoneRepository date: 2018年04月02日
 * 
 * @author changmingwei
 * @version
 * @since JDK 1.8
 */
public interface BlackPhoneRepository extends JpaRepository<BlackPhone, Long> {
    
	
    @Query("select count(1) from BlackPhone a" + " where a.phone=?1 and a.status="+Constants.BlackPhoneUtil.STATUS_YES)
    int findBlackByPhone(String phone);
    
}
