package com.booknest.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

	private static final Logger log = LoggerFactory.getLogger(EmailService.class);
	private final JavaMailSender mailSender;

	@Value("${notification.email.enabled}")
	private boolean emailEnabled;

	@Value("${spring.mail.username:noreply@booknest.com}")
	private String fromEmail;

	public void sendEmail(String to, String subject, String body) {
		if (!emailEnabled || to == null || to.isBlank()) {
			log.debug("Email skipped: enabled={}, to={}", emailEnabled, to);
			return;
		}
		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom(fromEmail);
			message.setTo(to);
			message.setSubject(subject);
			message.setText(body);
			mailSender.send(message);
			log.info("Email sent to {}: {}", to, subject);
		} catch (Exception e) {
			log.error("Failed to send email to {}: {}", to, e.getMessage());
		}
	}
}
