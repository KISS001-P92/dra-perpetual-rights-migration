package com.sonymusic.carma.pp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ErrorHandler;

@Service
@Slf4j
public class DraJmsErrorHandler implements ErrorHandler {

	@Override
	public void handleError(Throwable t) {
		log.error("Error in Jms", t);
	}

}
