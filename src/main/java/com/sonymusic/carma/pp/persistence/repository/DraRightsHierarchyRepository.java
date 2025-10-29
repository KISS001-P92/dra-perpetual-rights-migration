package com.sonymusic.carma.pp.persistence.repository;

import com.sonymusic.carma.pp.persistence.entity.DraRightsHierarchyEntity;
import org.springframework.data.repository.RepositoryDefinition;

import java.util.List;

@RepositoryDefinition(domainClass = DraRightsHierarchyEntity.class, idClass = Integer.class)
public interface DraRightsHierarchyRepository {

	List<DraRightsHierarchyEntity> findAll();
}
