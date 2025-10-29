package com.sonymusic.carma.pp.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstanceMappingEntity implements Serializable {
	private static final long serialVersionUID = 8770117405852682907L;
	@Id
	@Column(name = "input_level_node_instance_id")
	private Integer inputLevelNodeInstanceId;

	@Column(name = "digital_rights_instance_id")
	private Integer digitalRightsInstanceId;
}
