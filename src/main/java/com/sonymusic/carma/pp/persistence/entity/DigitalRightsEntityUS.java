package com.sonymusic.carma.pp.persistence.entity;

import com.sonymusic.carma.pp.persistence.converter.ADConverter;
import com.sonymusic.carma.pp.persistence.converter.YesNoConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "digital_rights_contract_us")
public class DigitalRightsEntityUS {

	private static final String SEQ = "digital_rights_contract_us_seq";

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQ)
	@SequenceGenerator(name = SEQ, sequenceName = SEQ, allocationSize = 1)
	@Column(name = "digital_rights_contract_us_id")
	private Integer id;
	@Column(name = "node_instance_id")
	private Integer nodeInstanceId;
	@Column(name = "dra_rights_hierarchy_id")
	private Integer draRightsHierarchyId;
	@Column(name = "cleared")
	@Convert(converter = YesNoConverter.class)
	private Boolean cleared;
	@Column(name = "territory")
	private String territory;
	@Column(name = "reversion_date")
	private LocalDateTime reversionDate;
	@Column(name = "origin_of_language")
	private Integer originOfLanguage;
	@Column(name = "status_flag")
	@Convert(converter = ADConverter.class)
	private Boolean statusFlag;
	@Column(name = "mod_user")
	private Integer modUser;
	@Column(name = "mod_stamp")
	private LocalDateTime modStamp;
}
