package com.sonymusic.carma.pp.controller;

import com.sonymusic.carma.pp.model.MigrationConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;

import java.lang.reflect.Type;

@RestControllerAdvice
@RequiredArgsConstructor
public class MigrationConfigAdvice implements RequestBodyAdvice {

	private final RequestContext requestContext;

	@Override
	public boolean supports(@NonNull MethodParameter methodParameter, @NonNull Type targetType, @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
		return true;
	}

	@Override
	@NonNull
	public HttpInputMessage beforeBodyRead(@NonNull HttpInputMessage inputMessage, @NonNull MethodParameter parameter, @NonNull Type targetType,
		@NonNull Class<? extends HttpMessageConverter<?>> converterType) {
		return inputMessage;
	}

	@Override
	@NonNull
	public Object afterBodyRead(@NonNull Object body, @NonNull HttpInputMessage inputMessage, @NonNull MethodParameter parameter, @NonNull Type targetType,
		@NonNull Class<? extends HttpMessageConverter<?>> converterType) {
		if (body instanceof MigrationConfiguration) {
			requestContext.setConfiguration((MigrationConfiguration) body);
		}
		return body;
	}

	@Override
	public Object handleEmptyBody(Object body, @NonNull HttpInputMessage inputMessage, @NonNull MethodParameter parameter, @NonNull Type targetType,
		@NonNull Class<? extends HttpMessageConverter<?>> converterType) {
		return body;
	}
}
