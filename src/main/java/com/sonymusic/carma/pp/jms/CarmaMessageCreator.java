package com.sonymusic.carma.pp.jms;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.ObjectMessage;
import jakarta.jms.Session;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.MessageCreator;

import java.io.Serializable;

@Getter
@Slf4j
public class CarmaMessageCreator implements MessageCreator {

	Serializable messageContent;

	@Override
	public Message createMessage(Session session) {
		ObjectMessage message = null;
		try {
			message = session.createObjectMessage(messageContent);
		} catch (JMSException e) {
			log.error(e.getMessage(), e);
		}
		return message;
	}

	public void setMessageContent(Serializable messageContent) {
		this.messageContent = messageContent;
	}

}
