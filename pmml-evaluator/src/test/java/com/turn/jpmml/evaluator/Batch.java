/*
 * Copyright (c) 2013 University of Tartu
 */
package com.turn.jpmml.evaluator;

import java.io.*;

import com.turn.jpmml.evaluator.Evaluator;

public interface Batch {

	/**
	 * Model's description in PMML data format
	 */
	InputStream getModel();

	/**
	 * Model input in CSV data format.
	 *
	 * @see Evaluator#getActiveFields()
	 */
	InputStream getInput();

	/**
	 * Model output in CSV data format.
	 *
	 * @see Evaluator#getPredictedFields()
	 * @see Evaluator#getOutputFields()
	 */
	InputStream getOutput();
}