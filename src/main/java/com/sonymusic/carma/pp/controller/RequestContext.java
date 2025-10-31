package com.sonymusic.carma.pp.controller;

import com.sonymusic.carma.pp.model.MigrationConfiguration;
import jakarta.annotation.ManagedBean;
import lombok.Data;
import org.springframework.web.context.annotation.RequestScope;

@ManagedBean
@RequestScope
@Data
public class RequestContext {
	private MigrationConfiguration configuration;
}
