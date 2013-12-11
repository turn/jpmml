package com.turn.tpmml.translator;

/**
 * Generic translation exception
 *
 * @author asvirsky
 *
 */
public class TranslationException extends Exception {

	private static final long serialVersionUID = 1L;

	public TranslationException(String message) {
		super(message);
	}

	public TranslationException(Throwable t) {
		super(t);
	}
}
