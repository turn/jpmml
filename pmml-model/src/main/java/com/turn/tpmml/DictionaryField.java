/*
 * Copyright (c) 2013 University of Tartu
 */
package com.turn.tpmml;

import javax.xml.bind.annotation.*;

@XmlTransient
abstract
public class DictionaryField extends Field {

	abstract
	public String getDisplayName();

	abstract
	public void setDisplayName(String displayName);

	abstract
	public DataType getDataType();

	abstract
	public void setDataType(DataType dataType);
}
