package com.sonymusic.carma.pp.persistence.repository;

import com.sonymusic.carma.pp.persistence.entity.InputPerpetualRightsViewEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.RepositoryDefinition;

import java.util.List;

@RepositoryDefinition(domainClass = InputPerpetualRightsViewEntity.class, idClass = Integer.class)
public interface InputRightsViewRepository {

	List<InputPerpetualRightsViewEntity> findByContractIdInAndStatusFlagIsNull(List<Integer> contractIds);

	List<InputPerpetualRightsViewEntity> findByCountryIdInAndStatusFlagIsNull(List<String> countryIds);

	@Query(value = "SELECT * FROM v_dra_perpetual_rights_migration_input WHERE status_flag='A' AND contract_id IN (:contractIds)", nativeQuery = true)
	List<InputPerpetualRightsViewEntity> findByContractIdInAndStatusFlagIsA(List<Integer> contractIds);

	@Query(value = "SELECT * FROM v_dra_perpetual_rights_migration_input WHERE status_flag='A' AND country_id IN (:countryIds)", nativeQuery = true)
	List<InputPerpetualRightsViewEntity> findByCountryIdInAndStatusFlagIsA(List<String> countryIds);

}
