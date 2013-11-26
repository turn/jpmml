/*
 * Copyright (c) 2013 University of Tartu
 */
package com.turn.tpmml.evaluator;

import org.junit.*;

import com.turn.tpmml.evaluator.Computable;
import com.turn.tpmml.evaluator.EvaluatorUtil;

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
