package com.sonymusic.carma.pp;

public class DraRuntimeException extends RuntimeException {

	private static final long serialVersionUID = -4840568821975359285L;

	public DraRuntimeException() {
		super();
	}

	public DraRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public DraRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public DraRuntimeException(String message) {
		super(message);
	}

	public DraRuntimeException(Throwable cause) {
		super(cause);
	}

}
