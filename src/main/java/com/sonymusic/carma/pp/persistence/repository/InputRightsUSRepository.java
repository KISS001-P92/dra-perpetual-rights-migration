package com.sonymusic.carma.pp.persistence.repository;

import com.sonymusic.carma.pp.persistence.entity.PerpetualRightsMigrationInputUS;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.RepositoryDefinition;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@RepositoryDefinition(domainClass = PerpetualRightsMigrationInputUS.class, idClass = Integer.class)
public interface InputRightsUSRepository {

	@Transactional
	@Modifying
	@Query(value = "UPDATE dra_perpetual_rights_migration_input_us SET status_flag = 'A', mod_stamp = NOW(), mod_user = 99999998 "
		+ "WHERE  id = :id", nativeQuery = true)
	void updateProcessed(Integer id);

	@Transactional
	@Modifying
	@Query(value = "UPDATE dra_perpetual_rights_migration_input_us SET status_flag = 'D', mod_stamp = NOW(), mod_user = 99999998 "
		+ "WHERE status_flag='A' AND \"Contract\" IN :contractNumbers", nativeQuery = true)
	void updateCleared(Set<String> contractNumbers);
}
