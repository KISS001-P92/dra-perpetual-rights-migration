package com.sonymusic.carma.pp.persistence.entity;

import com.sonymusic.carma.pp.persistence.converter.ADConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "dra_perpetual_rights_migration_protocol")
public class ProtocolEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;
	@Column(name = "action_id")
	private Integer actionId;
	@Column(name = "document_id")
	private Integer documentId;
	@Column(name = "contract_id")
	private Integer contractId;
	@Column(name = "contract_number")
	private String contractNumber;
	@Column(name = "recording")
	private String recording;
	@Column(name = "dra_perpetual_rights_migration_input_id ")
	private Integer draPerpetualRightsMigrationInputId;
	@Column(name = "input_level_node_instance_id ")
	private Integer inputLevelNodeInstanceId;
	@Column(name = "digital_rights_contract_eu_id")
	private Integer digitalRightsContractEuId;
	@Column(name = "digital_rights_contract_us_id")
	private Integer digitalRightsContractUsId;
	@Column(name = "mod_user")
	private Integer modUser;
	@Column(name = "mod_stamp")
	private LocalDateTime modStamp;
	@Column(name = "result_status")
	private ResultStatus resultStatus;
	@Column(name = "result")
	private String result;
	@Column(name = "status_flag")
	@Convert(converter = ADConverter.class)
	private Boolean statusFlag;
}
