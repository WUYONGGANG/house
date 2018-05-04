package com.huifenqi.hzf_platform.dao.repository.house;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.huifenqi.hzf_platform.context.entity.phone.CallInfoRecord;


public interface CallInfoRecordRepository extends JpaRepository<CallInfoRecord, Long>{
	
	
	@Query("select a from CallInfoRecord a where a.callDuration > 0 and a.voiceRecordUrl = '' and a.callId != '' and a.createTime < ?1")
	Page<CallInfoRecord> findValidCallInfoRecord(Pageable pageable , Date startTime);
	
	@Query("select count(1) from CallInfoRecord c where c.outId in (select b.outId from BindRecord b where b.agencyPhone = ?1 ) ")
	int selectAgencyPhoneCallInfoTotalCount(String agencyPhone);
	
	@Query("select count(1) from CallInfoRecord c where c.outId in (select b.outId from BindRecord b where b.agencyPhone = ?1 ) and c.callDuration >0 ")
	int selectAgencyPhoneCallInfoValidCount(String agencyPhone);
	
	
	/**
	 * 根据房源ID查询房源所属中介公司的经纪人电话
	 */
	@Query("select DISTINCT b.agencyPhone from HouseBase b where b.agencyPhone != '' and b.companyId = (select s.companyId from HouseBase s where s.sellId = ?1 ) ")
	List<String> selectAgencyPhoneBySellId(String sellId);
}
