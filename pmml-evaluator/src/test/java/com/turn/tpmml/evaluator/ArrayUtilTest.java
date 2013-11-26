/*
 * Copyright (c) 2012 University of Tartu
 */
package com.turn.tpmml.evaluator;

import java.util.*;

import com.turn.tpmml.*;

import org.junit.*;

import com.turn.tpmml.evaluator.ArrayUtil;

import static org.junit.Assert.*;

public class ArrayUtilTest {

	@Test
	public void tokenizeIntArray(){
		assertEquals(Arrays.asList("1", "2", "3"), tokenizeIntArray("1 2 3"));
	}

	@Test
	public void tokenizeStringArray(){
		assertEquals(Arrays.asList("a", "b", "c"), tokenizeStringArray("a b c"));
		assertEquals(Arrays.asList("a", "b", "c"), tokenizeStringArray("\"a\" \"b\" \"c\""));

		assertEquals(Arrays.asList("a b c"), tokenizeStringArray("\"a b c\""));

		assertEquals(Arrays.asList("\"a b c"), tokenizeStringArray("\"a b c"));
		assertEquals(Arrays.asList("\\a", "\\b\\", "c\\"), tokenizeStringArray("\\a \\b\\ c\\"));

		assertEquals(Arrays.asList("a \"b\" c"), tokenizeStringArray("\"a \\\"b\\\" c\""));
		assertEquals(Arrays.asList("\"a b c\""), tokenizeStringArray("\"\\\"a b c\\\"\""));
	}

	static
	private List<String> tokenizeIntArray(String content){
		return ArrayUtil.tokenize(new Array(content, Array.Type.INT));
	}

	static
	private List<String> tokenizeStringArray(String content){
		return ArrayUtil.tokenize(new Array(content, Array.Type.STRING));
	}
}
