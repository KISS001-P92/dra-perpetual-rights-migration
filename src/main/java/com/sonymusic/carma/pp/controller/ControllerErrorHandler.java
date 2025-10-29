package com.sonymusic.carma.pp.controller;

import com.sonymusic.carma.pp.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class ControllerErrorHandler {

	private final MailService mailService;
	private final RequestContext requestContext;

	@ExceptionHandler(Throwable.class)
	@ResponseBody
	public ResponseEntity<String> handleException(Exception e) {
		log.error("Controller Exception: {}", e.getMessage(), e);
		mailService.sendErrorMail(requestContext.getConfiguration(), e);
		return ResponseEntity.internalServerError().body(e.getMessage());
	}
}
