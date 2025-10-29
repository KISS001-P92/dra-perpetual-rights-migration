package com.sonymusic.carma.pp.persistence.repository;

import com.sonymusic.carma.pp.persistence.entity.InstanceMappingEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.RepositoryDefinition;

import java.util.List;

@RepositoryDefinition(domainClass = InstanceMappingEntity.class, idClass = Integer.class)
public interface InstanceMappingRepository {

	@Query(value = "SELECT ni_rm.node_instance_id AS right_management_instance_id, ni_dr.node_instance_id AS digital_rights_instance_id\n"
		+ "FROM node_instance ni_rm\n"
		+ "JOIN node_instance ni_dr ON ni_rm.contract_id = ni_dr.contract_id AND ni_dr.\"path\" = REPLACE(ni_rm.\"path\", ' / Rights Management', ' / Digital Rights')\n"
		+ "WHERE ni_rm.node_instance_id IN ( :instanceIds)", nativeQuery = true)
	List<InstanceMappingEntity> findByInstanceIds(List<Integer> instanceIds);

	@Query(value = "SELECT ni_rm.node_instance_id AS right_management_instance_id, ni_dr.node_instance_id AS digital_rights_instance_id\n"
		+ "FROM node_instance ni_rm\n"
		+ "JOIN node_instance ni_dr ON ni_rm.contract_id = ni_dr.contract_id AND ni_dr.\"path\" = REPLACE(ni_rm.\"path\", ' / Rights Management', ' / Digital Rights')\n"
		+ "WHERE ni_rm.node_instance_id = :instanceId", nativeQuery = true)
	InstanceMappingEntity findByInstanceId(Integer instanceId);
}
