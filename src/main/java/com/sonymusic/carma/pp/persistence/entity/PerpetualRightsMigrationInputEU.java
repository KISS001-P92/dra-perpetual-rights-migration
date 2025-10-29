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
@Table(name = "dra_perpetual_rights_migration_input_eu")
public class PerpetualRightsMigrationInputEU {

	@Id
	@Column(name = "id")
	private Integer id;
	@Column(name = "country_id")
	private String countryId;
	@Column(name = "Artist")
	private Integer artist;
	@Column(name = "Contract")
	private String contract;
	@Column(name = "Doc Type")
	private String docType;
	@Column(name = "Contracting Company")
	private String contractingCompany;
	@Column(name = "Project Id")
	private Integer projectId;
	@Column(name = "Project Number")
	private Integer projectNumber;
	@Column(name = "Project Name")
	private String projectName;
	@Column(name = "Product Family Id")
	private Integer productFamilyId;
	@Column(name = "Product Title")
	private String productTitle;
	@Column(name = "Only Products and/or Tracks")
	private String onlyProductsAndOrTracks;
	@Column(name = "Status of Term")
	private String statusOfTerm;
	@Column(name = "Approval Status")
	private String approvalStatus;
	@Column(name = "Period")
	private String period;
	@Column(name = "Recording")
	private String recording;
	@Column(name = "Territory")
	private String territory;
	@Column(name = "Term Related Provisions Exploitation Period End")
	private String termRelated;
	@Column(name = "Exploitation End")
	private String exploitationEnd;
	@Column(name = "Dates(s) cleaned - Automatic Date Calculation")
	private String datesCleaned;
	@Column(name = "Contract Master Clearance")
	private String contractMasterClearance;
	@Column(name = "Period Master Clearance")
	private String periodMasterClearance;
	@Column(name = "Recording Master Clearance")
	private String recordingMasterClearance;
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
