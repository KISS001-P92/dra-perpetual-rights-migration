package com.sonymusic.carma.pp.persistence.repository;

import com.sonymusic.carma.pp.persistence.entity.ProtocolEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Transactional
public interface ProtocolRepository extends JpaRepository<ProtocolEntity, Long> {

	@Query(value = "SELECT NEXTVAL('dra_perpetual_rights_protocol_action_id_seq')", nativeQuery = true)
	Integer getNextActionId();

	@Transactional
	@Modifying
	@Query(value = "UPDATE dra_perpetual_rights_migration_protocol SET status_flag = 'D', mod_stamp = NOW(), mod_user = 99999998 WHERE status_flag='A' AND contract_id = :contractId", nativeQuery = true)
	void deleteAllByContractId(Integer contractId);

	@Query(value = "SELECT digital_rights_contract_us_id FROM dra_perpetual_rights_migration_protocol WHERE status_flag='A' AND dra_perpetual_rights_migration_input_id IN (:draPerpetualRightsMigrationInputIds)", nativeQuery = true)
	List<Integer> selectDigitalRightsContractUSIdByInputViewIds(Set<Integer> draPerpetualRightsMigrationInputIds);

	@Query(value = "SELECT digital_rights_contract_eu_id FROM dra_perpetual_rights_migration_protocol WHERE status_flag='A' AND dra_perpetual_rights_migration_input_id IN (:draPerpetualRightsMigrationInputIds)", nativeQuery = true)
	List<Integer> selectDigitalRightsContractEUIdByInputViewIds(Set<Integer> draPerpetualRightsMigrationInputIds);

	@Query(value = "SELECT contract_id FROM dra_perpetual_rights_migration_protocol WHERE status_flag='A' AND result_status='SUCCESS' AND (digital_rights_contract_eu_id is not null OR digital_rights_contract_us_id is not null) AND dra_perpetual_rights_migration_input_id IN (:draPerpetualRightsMigrationInputIds)", nativeQuery = true)
	Set<Integer> getAllSuccessedAndNewDigitalRights(Set<Integer> draPerpetualRightsMigrationInputIds);

	@Query(value = "SELECT dp.contract_id FROM dra_perpetual_rights_migration_protocol dp"
		+ " JOIN v_dra_perpetual_rights_migration_input vi ON dp.dra_perpetual_rights_migration_input_id = vi.id"
		+ " WHERE dp.status_flag='A' AND vi.status_flag='A' AND dp.result_status='SUCCESS' "
		+ " AND (dp.digital_rights_contract_eu_id is not null OR dp.digital_rights_contract_us_id is not null) "
		+ " AND vi.country_id IN (:countryIds)", nativeQuery = true)
	Set<Integer> getContractIdsWhereNewDigitalRightExistsByCountryIds(Set<String> countryIds);

	@Query(value = "SELECT dp.contract_id FROM dra_perpetual_rights_migration_protocol dp"
		+ " WHERE dp.status_flag='A' AND dp.result_status='SUCCESS' "
		+ " AND (dp.digital_rights_contract_eu_id is not null OR dp.digital_rights_contract_us_id is not null) "
		+ " AND dp.contract_id IN (:contractIds)", nativeQuery = true)
	Set<Integer> getContractIdsWhereNewDigitalRightExistsByContractIds(Set<Integer> contractIds);
}
