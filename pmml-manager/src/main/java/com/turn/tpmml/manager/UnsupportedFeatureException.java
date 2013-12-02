/*
 * Copyright (c) 2012 University of Tartu
 */
package com.turn.tpmml.manager;

import com.turn.tpmml.*;

public class UnsupportedFeatureException extends ModelManagerException {


	private static final long serialVersionUID = 1L;

	public UnsupportedFeatureException(){
	}

	public UnsupportedFeatureException(String message){
		super(message);
	}

	public UnsupportedFeatureException(PMMLObject element){
		this((element.getClass()).getName());
	}

	public UnsupportedFeatureException(Enum<?> attribute){
		this((attribute.getClass()).getName() + "#" + attribute.name());
	}
}
