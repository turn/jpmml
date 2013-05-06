package org.jpmml.translator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface UserDefinedFunction {
	public String name();
	public String[] methods();
}
