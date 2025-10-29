package com.sonymusic.carma.pp.persistence.repository;

import com.sonymusic.carma.pp.persistence.entity.ContractIdUserExportedPair;
import jakarta.persistence.*;

@Entity
@NamedNativeQuery(name = "getContractIdUserExportedPairByCountryIds", query =
	"SELECT  c.contract_id,c.is_user_exported FROM contract c "
		+ "JOIN v_document vd ON c.document_id = vd.document_id "
		+ "WHERE vd.country_id IN :countryIds AND vd.template_id = 5056 "
		+ "AND c.status_flag ='A' ",
	resultClass = ContractIdUserExportedPair.class,
	resultSetMapping = "getContractIdUserExportedPair")

@NamedNativeQuery(name = "getContractIdUserExportedPairByContractIds", query =
	"SELECT  c.contract_id,c.is_user_exported FROM contract c "
		+ "JOIN v_document vd ON c.document_id = vd.document_id "
		+ "WHERE c.contract_id IN :contractIds AND vd.template_id = 5056 "
		+ "AND c.status_flag ='A' ",
	resultClass = ContractIdUserExportedPair.class,
	resultSetMapping = "getContractIdUserExportedPair")

@SqlResultSetMapping(name = "getContractIdUserExportedPair", classes = {
	@ConstructorResult(targetClass = ContractIdUserExportedPair.class, columns = {
		@ColumnResult(name = "contract_id", type = Integer.class),
		@ColumnResult(name = "is_user_exported", type = String.class)
	})
})

public class CarmaEntityForNativeQueries {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer dummyIdNecessaryForJpa;

	CarmaEntityForNativeQueries() {
	}

}
