/*
 * Copyright (c) 2013 University of Tartu
 */
package com.turn.jpmml.evaluator;

import org.junit.*;

import com.turn.jpmml.evaluator.Computable;
import com.turn.jpmml.evaluator.EvaluatorUtil;

import static org.junit.Assert.*;

public class EvaluatorUtilTest {

	@Test
	public void decode(){
		Computable<String> value = new Computable<String>(){

			public String getResult(){
				return "value";
			}
		};

		assertEquals("value", EvaluatorUtil.decode(value));
	}
}