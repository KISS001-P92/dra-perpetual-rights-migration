package com.sonymusic.carma.pp.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "dra_rights_hierarchy")
public class DraRightsHierarchyEntity {
	@Id
	@Column(name = "dra_rights_hierarchy_id")
	private Integer id;
	@Column(name = "dra_rights_hierarchy_name")
	private String name;
	@Column(name = "dra_hierarchy_level")
	private Integer level;
	@Column(name = "dra_level_type_code")
	private String levelTypeCode;
	@Column(name = "dra_parent_level_type_code")
	private String parentLevelTypeCode;
	@Column(name = "dra_parent_id")
	private Integer parentId;
	@Column(name = "display_order")
	private Integer displayOrder;
	@Column(name = "status_flag")
	private String statusFlag;
}
