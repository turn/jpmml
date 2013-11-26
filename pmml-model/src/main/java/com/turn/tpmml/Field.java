/*
 * Copyright (c) 2013 University of Tartu
 */
package com.turn.tpmml;

import javax.xml.bind.annotation.*;

@XmlTransient
abstract
public class Field extends PMMLObject implements HasName {

	abstract
	public FieldName getName();

	abstract
	public void setName(FieldName name);

	abstract
	public OpType getOptype();

	abstract
	public void setOptype(OpType opType);
}
