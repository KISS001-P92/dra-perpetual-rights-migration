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
@Table(name = "dra_perpetual_rights_migration_input_us")
public class PerpetualRightsMigrationInputUS {

	@Id
	@Column(name = "id")
	private Integer id;
	@Column(name = "country_id")
	private String countryId;
	@Column(name = "Artist")
	private Integer artist;
	@Column(name = "Roster Status")
	private String rosterStatus;
	@Column(name = "Contract")
	private String contract;
	@Column(name = "Doc Type")
	private String docType;
	@Column(name = "Release Label")
	private String releaseLabel;
	@Column(name = "Project Id")
	private Integer projectId;
	@Column(name = "Project Number")
	private Integer projectNumber;
	@Column(name = "Project Name")
	private String projectName;
	@Column(name = "Signed Status")
	private String signedStatus;
	@Column(name = "Contract Status")
	private String contractStatus;
	@Column(name = "Period")
	private String period;
	@Column(name = "Recording")
	private String recording;
	@Column(name = "Territory")
	private String territory;
	@Column(name = "Contract Master Clearance")
	private String contractMasterClearance;
	@Column(name = "Period Master Clearance")
	private String periodMasterClearance;
	@Column(name = "Recording Master Clearance")
	private String recordingMasterClearance;
	@Column(name = "Terms of Exploitation")
	private String termsOfExploitation;
	@Column(name = "Ownership")
	private String ownership;
	@Column(name = "Perpetual Rights")
	private String perpetualRights;
	@Column(name = "Perpetual Rights Exception")
	private String perpetualRightsException;
	@Column(name = "status_flag")
	private Integer statusFlag;
	@Column(name = "mod_user")
	private Integer modUser;
	@Column(name = "mod_stamp")
	private LocalDateTime modStamp;

}
