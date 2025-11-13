package com.sonymusic.carma.pp.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Table(name = "v_dra_perpetual_rights_migration_input")
public class InputPerpetualRightsViewEntity {
	@Id
	@Column(name = "id")
	private Integer id;
	@Column(name = "country_id")
	private String countryId;
	@Column(name = "documentId")
	private Integer documentId;
	@Column(name = "contract_id")
	private Integer contractId;
	@Column(name = "contract_number")
	private String contractNumber;
	@Column(name = "recording")
	private String recording;
	@Column(name = "contract_dr_node_instance_id")
	private Integer contractDRNodeInstanceId;
	@Column(name = "contract_dr_territory_expression")
	private String contractDRTerritoryExpression;
	@Column(name = "contract_dr_territory")
	private String contractDRTerritory;
	@Column(name = "period_dr_node_instance_id")
	private Integer periodDRNodeInstanceId;
	@Column(name = "period_dr_territory_expression")
	private String periodDRTerritoryExpression;
	@Column(name = "period_dr_territory")
	private String periodDRTerritory;
	@Column(name = "recording_dr_node_instance_id")
	private Integer recordingDRNodeInstanceId;
	@Column(name = "recording_dr_territory_expression")
	private String recordingDRTerritoryExpression;
	@Column(name = "recording_dr_territory")
	private String recordingDRTerritory;
	@Column(name = "territory_on_territory")
	private String territory;
	@Column(name = "contract_master_clearance")
	private String contractMasterClearance;
	@Column(name = "period_master_clearance")
	private String periodMasterClearance;
	@Column(name = "recording_master_clearance")
	private String recordingMasterClearance;
	@Column(name = "perpetual_rights")
	private String perpetualRights;
	@Column(name = "perpetual_rights_exception")
	private String perpetualRightsException;
	@Column(name = "inhereted_clearance")
	private String inheretedClearance;
	@Column(name = "intended_clearance")
	private String intendedClearance;
	@Column(name = "status_flag")
	private String statusFlag;
	@Column(name = "mod_user")
	private Integer modUser;
	@Column(name = "mod_stamp")
	private LocalDateTime modStamp;

	public boolean isUs() {
		return id < 10000000;
	}
}
