package com.sonymusic.carma.pp.persistence.entity;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DigitalRightsNodeInstanceAndClearanceData {
	@Column(name = "node_instance_id")
	private Integer nodeInstanceId;
	@Column(name = "dra_rights_hierarchy_id")
	private Integer draRightsHierarchyId;
	@Column(name = "clearance")
	private String clearance;

}