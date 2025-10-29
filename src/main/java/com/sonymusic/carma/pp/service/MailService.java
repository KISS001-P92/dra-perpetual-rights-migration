package com.sonymusic.carma.pp.service;

import com.sonymusic.carma.pp.model.MigrationConfiguration;
import com.sonymusic.carma.pp.model.MigrationStatistics;
import com.sonymusic.carma.pp.model.SendingDraStatistics;
import com.sonymusic.carma.sapi.preference.PreferenceRepository;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

	private static final String EMAIL_SUBJECT_PREFIX = "EMAIL_SUBJECT_PREFIX";
	private static final String EMAIL_CARMA_HOTLINE = "EMAIL_CARMA_HOTLINE";
	private static final String PREFERENCE_DRA_DEVELOPERS = "dra.developers.email";

	private final JavaMailSender mailSender;
	private final Configuration configuration;
	private final PreferenceRepository preferenceRepository;

	public void sendErrorMail(MigrationConfiguration config, Exception e) {
		createErrorBody(config, e).ifPresent(body -> sendMail("EU Migration Error Mail", body));
	}

	public void sendStatusMail(MigrationStatistics statistics) {
		createStatusBody(statistics).ifPresent(body -> sendMail("Internal Rights Migration Status Mail", body));
	}

	public void sendStatusMailForSending(SendingDraStatistics statistics) {
		createStatusBodyForSending(statistics).ifPresent(body -> sendMail("Sending contracts to DRA Status Mail", body));
	}

	private void sendMail(String subject, String body) {
		String subjectPrefix = preferenceRepository.getValueByName(EMAIL_SUBJECT_PREFIX, "");

		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message);
			helper.addTo(preferenceRepository.getValueByName(EMAIL_CARMA_HOTLINE));
			InternetAddress[] ccList = InternetAddress.parse(preferenceRepository.getValueByName(PREFERENCE_DRA_DEVELOPERS));
			for (InternetAddress internetAddress : ccList) {
				helper.addCc(internetAddress);
			}
			helper.setSubject(String.join(" ", subjectPrefix, subject));
			helper.setText(body);
			mailSender.send(message);
		} catch (MessagingException e) {
			log.error("An error occurred when attempting to send status mail", e);
		}
	}

	private Optional<String> createStatusBody(MigrationStatistics statistics) {
		Map<String, Object> model = new HashMap<>();
		model.put("statistics", statistics);
		return createBody("statusMail.ftlh", model);
	}

	private Optional<String> createStatusBodyForSending(SendingDraStatistics statistics) {
		Map<String, Object> model = new HashMap<>();
		model.put("statistics", statistics);
		return createBody("statusMailSendingDra.ftlh", model);
	}

	private Optional<String> createErrorBody(MigrationConfiguration config, Exception e) {
		Map<String, Object> model = new HashMap<>();
		model.put("configuration", config);
		model.put("exception", createErrorText(e));
		return createBody("errorMail.ftlh", model);
	}

	private Optional<String> createBody(String template, Map<String, Object> model) {
		try {
			StringWriter stringWriter = new StringWriter();
			configuration.getTemplate(template).process(model, stringWriter);
			return Optional.of(stringWriter.getBuffer().toString());
		} catch (TemplateException | IOException e) {
			log.error("An error occurred when attempting to create mail", e);
		}
		return Optional.empty();
	}

	private String createErrorText(Exception e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}
}
