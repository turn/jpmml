/*
 * Copyright (c) 2012 University of Tartu
 */
package com.turn.tpmml.manager;

/**
 * Generic exception when something happens within ModelManager.
 * 
 */
// FIXME: Specialize this exception, make it better.
public class ModelManagerException extends Exception {

	private static final long serialVersionUID = 1L;

	public ModelManagerException() {
	}

	public ModelManagerException(String message) {
		super(message);
	}
	
	public ModelManagerException(Throwable t) {
		super(t);
	}
}
