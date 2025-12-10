package com.sonymusic.carma.pp.persistence.repository;

import com.sonymusic.carma.pp.persistence.entity.DigitalRightsEntityUS;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface DigitalRightsUsRepository extends JpaRepository<DigitalRightsEntityUS, Long> {

	@Modifying
	@Query(value = "UPDATE digital_rights_contract_us "
		+ "SET status_flag = 'D', mod_stamp = NOW(), mod_user = 99999998 "
		+ "WHERE status_flag='A' AND digital_rights_contract_us_id IN :ids", nativeQuery = true)
	void deleteByIds(List<Integer> ids);

}
