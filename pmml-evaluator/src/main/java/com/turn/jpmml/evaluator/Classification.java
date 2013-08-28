/*
 * Copyright (c) 2013 University of Tartu
 */
package com.turn.jpmml.evaluator;

public interface Classification extends Computable<String> {

	Double getProbability(String value);
}