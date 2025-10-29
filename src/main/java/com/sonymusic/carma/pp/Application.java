package com.sonymusic.carma.pp;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.Destination;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.jndi.JndiTemplate;
import org.springframework.scheduling.annotation.AsyncConfigurer;

import javax.naming.NamingException;

@SpringBootApplication(exclude = { org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class })
@EnableJms
@ComponentScan(basePackages = { "com.sonymusic.carma.pp", "com.sonymusic.carma.sapi.preference" })
public class Application implements AsyncConfigurer {

	@Autowired
	private ConnectionFactory connectionFactory;

	@Autowired
	private DraJmsErrorHandler draJmsErrorHandler;

	@Autowired
	private ObjectMapper objectMapper;

	//@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(Application.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class);
	}

	@Override
	public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
		return new SimpleAsyncUncaughtExceptionHandler();
	}

	@Bean
	public JmsTemplate jmsTemplate() throws NamingException {
		JmsTemplate template = new JmsTemplate();
		template.setConnectionFactory(connectionFactory);
		return template;
	}

	@Bean
	public Destination destination() throws NamingException {
		JndiTemplate jndiTemplate = new JndiTemplate();
		return jndiTemplate.lookup("java:/jms/DRAExportQueue", Destination.class);
	}

	@Bean
	public DefaultJmsListenerContainerFactory jmsListenerContainerFactory() {
		DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory);
		factory.setConcurrency("1-1");
		factory.setErrorHandler(draJmsErrorHandler);
		return factory;
	}

	@Bean
	public ObjectMapper proxyObjectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		objectMapper.registerModule(new JavaTimeModule());
		return objectMapper;
	}

	@Bean
	public ConnectionFactory draConnectionFactory() throws IllegalArgumentException, NamingException {
		JndiObjectFactoryBean jndi = new JndiObjectFactoryBean();
		jndi.setJndiName("java:/jms/CarmaJMSConnectionFactory");
		jndi.setLookupOnStartup(true);
		jndi.setProxyInterface(ConnectionFactory.class);
		jndi.afterPropertiesSet();
		return (ConnectionFactory) jndi.getObject();
	}

}
