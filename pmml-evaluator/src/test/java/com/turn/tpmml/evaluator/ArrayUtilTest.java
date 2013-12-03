/*
 * Copyright (c) 2012 University of Tartu
 */
package com.turn.tpmml.evaluator;

import com.turn.tpmml.Array;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class ArrayUtilTest {

	@Test
	public void tokenizeIntArray() {
		assertEquals(Arrays.asList("1", "2", "3"), tokenizeIntArray("1 2 3"));
	}

	@Test
	public void tokenizeStringArray() {
		assertEquals(Arrays.asList("a", "b", "c"), tokenizeStringArray("a b c"));
		assertEquals(Arrays.asList("a", "b", "c"), tokenizeStringArray("\"a\" \"b\" \"c\""));

		assertEquals(Arrays.asList("a b c"), tokenizeStringArray("\"a b c\""));

		assertEquals(Arrays.asList("\"a b c"), tokenizeStringArray("\"a b c"));
		assertEquals(Arrays.asList("\\a", "\\b\\", "c\\"), tokenizeStringArray("\\a \\b\\ c\\"));

		assertEquals(Arrays.asList("a \"b\" c"), tokenizeStringArray("\"a \\\"b\\\" c\""));
		assertEquals(Arrays.asList("\"a b c\""), tokenizeStringArray("\"\\\"a b c\\\"\""));
	}

	private static List<String> tokenizeIntArray(String content) {
		return ArrayUtil.tokenize(new Array(content, Array.Type.INT));
	}

	private static List<String> tokenizeStringArray(String content) {
		return ArrayUtil.tokenize(new Array(content, Array.Type.STRING));
	}
}
