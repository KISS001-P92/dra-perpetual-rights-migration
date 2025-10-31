package com.sonymusic.carma.pp.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractIdUserExportedPair {
	@Id
	@Column(name = "contract_id")
	private Integer contractId;
	@Column(name = "is_user_exported")
	private String isUserExported;
}
