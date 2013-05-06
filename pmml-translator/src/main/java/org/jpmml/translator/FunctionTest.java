package org.jpmml.translator;

public class FunctionTest {
	static public Double identity(Double input) {
		return input;
	}

	static public Double add(Double v1, Double v2) {
		return v1 + v2;
	}
}
