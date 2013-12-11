/*
 * Copyright (c) 2010 University of Tartu
 */
package com.turn.tpmml.evaluator;

public class EvaluationException extends Exception {

	private static final long serialVersionUID = 1L;

	public EvaluationException() {
	}

	public EvaluationException(String message) {
		super(message);
	}
	
	public EvaluationException(Exception e) {
		super(e);
	}
}
