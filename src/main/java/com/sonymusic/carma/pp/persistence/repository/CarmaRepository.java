package com.sonymusic.carma.pp.persistence.repository;

import com.sonymusic.carma.pp.persistence.entity.ContractIdUserExportedPair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CarmaRepository extends JpaRepository<CarmaEntityForNativeQueries, Integer> {
	@Query(nativeQuery = true, name = "getContractIdUserExportedPairByCountryIds")
	List<ContractIdUserExportedPair> getContractIdUserExportedPairByCountryIds(@Param("countryIds") List<String> countryIds);

	@Query(nativeQuery = true, name = "getContractIdUserExportedPairByContractIds")
	List<ContractIdUserExportedPair> getContractIdUserExportedPairByContractIds(@Param("contractIds") List<Integer> contractIds);
}
