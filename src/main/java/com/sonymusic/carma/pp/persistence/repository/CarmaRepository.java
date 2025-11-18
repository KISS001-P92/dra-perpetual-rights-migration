package com.sonymusic.carma.pp.persistence.repository;

import com.sonymusic.carma.pp.persistence.entity.ContractIdUserExportedPair;
import com.sonymusic.carma.pp.persistence.entity.DigitalRightsNodeInstanceAndClearanceData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface CarmaRepository extends JpaRepository<CarmaEntityForNativeQueries, Integer> {

	@Query(nativeQuery = true, name = "getContractIdUserExportedPairByContractIds")
	List<ContractIdUserExportedPair> getContractIdUserExportedPairByContractIds(@Param("contractIds") Set<Integer> contractIds);

	@Query(nativeQuery = true, name = "getExistingPerpetualDigitalRightsForUSContracts")
	List<DigitalRightsNodeInstanceAndClearanceData> getExistingPerpetualDigitalRightsForUSContracts(@Param("contractIds") Set<Integer> contractIds);

	@Query(nativeQuery = true, name = "getExistingPerpetualDigitalRightsForEUContracts")
	List<DigitalRightsNodeInstanceAndClearanceData> getExistingPerpetualDigitalRightsForEUContracts(@Param("contractIds") Set<Integer> contractIds);
}
