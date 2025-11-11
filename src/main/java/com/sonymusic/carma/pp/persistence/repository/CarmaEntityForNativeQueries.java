package com.sonymusic.carma.pp.persistence.repository;

import com.sonymusic.carma.pp.persistence.entity.ContractIdUserExportedPair;
import com.sonymusic.carma.pp.persistence.entity.DigitalRightsNodeInstanceAndClearanceData;
import jakarta.persistence.*;

@Entity
@NamedNativeQuery(name = "getContractIdUserExportedPairByCountryIds", query =
	"SELECT  c.contract_id, "
		+ "case when vd.template_id =9000 then 'Y' "
		+ "else c.is_user_exported end as is_user_exported "
		+ "FROM contract c "
		+ "JOIN v_document vd ON c.document_id = vd.document_id "
		+ "WHERE vd.country_id IN :countryIds AND vd.template_id in (5056,9000)"
		+ "AND c.status_flag ='A' ",
	resultClass = ContractIdUserExportedPair.class,
	resultSetMapping = "getContractIdUserExportedPair")

@NamedNativeQuery(name = "getContractIdUserExportedPairByContractIds", query =
	"SELECT  c.contract_id, "
		+ "case when vd.template_id =9000 then 'Y' "
		+ "else c.is_user_exported end as is_user_exported "
		+ "FROM contract c "
		+ "JOIN v_document vd ON c.document_id = vd.document_id "
		+ "WHERE c.contract_id IN :contractIds AND vd.template_id in (5056,9000) "
		+ "AND c.status_flag ='A' ",
	resultClass = ContractIdUserExportedPair.class,
	resultSetMapping = "getContractIdUserExportedPair")

@SqlResultSetMapping(name = "getContractIdUserExportedPair", classes = {
	@ConstructorResult(targetClass = ContractIdUserExportedPair.class, columns = {
		@ColumnResult(name = "contract_id", type = Integer.class),
		@ColumnResult(name = "is_user_exported", type = String.class)
	})

})

@NamedNativeQuery(name = "getExistingPerpetualDigitalRightsForUSContracts", query =
	"SELECT drcu.node_instance_id, drcu.dra_rights_hierarchy_id, drcu.cleared as clearance "
		+ "FROM digital_rights_contract_us drcu "
		+ "JOIN node_instance ni ON drcu.node_instance_id = ni.node_instance_id and ni.status_flag ='A' "
		+ "WHERE drcu.status_flag ='A' and ni.contract_id in (:contractIds) and drcu.dra_rights_hierarchy_id in (21,22,23) ",
	resultClass = DigitalRightsNodeInstanceAndClearanceData.class,
	resultSetMapping = "getExistingPerpetualDigitalRightsForContracts")

@NamedNativeQuery(name = "getExistingPerpetualDigitalRightsForEUContracts", query =
	"SELECT drcu.node_instance_id, drcu.dra_rights_hierarchy_id, drcu.approval_term as clearance "
		+ "FROM digital_rights_contract_eu drcu "
		+ "JOIN node_instance ni ON drcu.node_instance_id = ni.node_instance_id and ni.status_flag ='A' "
		+ " WHERE drcu.status_flag ='A' and ni.contract_id in (:contractIds) and drcu.dra_rights_hierarchy_id in (21,22,23) ",
	resultClass = DigitalRightsNodeInstanceAndClearanceData.class,
	resultSetMapping = "getExistingPerpetualDigitalRightsForContracts")

@SqlResultSetMapping(name = "getExistingPerpetualDigitalRightsForContracts", classes = {
	@ConstructorResult(targetClass = DigitalRightsNodeInstanceAndClearanceData.class, columns = {
		@ColumnResult(name = "node_instance_id", type = Integer.class),
		@ColumnResult(name = "dra_rights_hierarchy_id", type = Integer.class),
		@ColumnResult(name = "clearance", type = String.class)
	})
})

public class CarmaEntityForNativeQueries {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer dummyIdNecessaryForJpa;

	CarmaEntityForNativeQueries() {
	}

}
