package com.sonymusic.carma.pp.jms;

import jakarta.jms.Destination;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Setter
@Log4j2
@Component
@AllArgsConstructor
public class JMSMessageSender {
	private JmsTemplate jmsTemplate;
	private Destination destination;

	public void sendMessage(final Serializable messageContent) {
		CarmaMessageCreator creator = new CarmaMessageCreator();
		creator.setMessageContent(messageContent);
		jmsTemplate.send(destination, creator);
		log.info("message sent for " + messageContent);
	}

}