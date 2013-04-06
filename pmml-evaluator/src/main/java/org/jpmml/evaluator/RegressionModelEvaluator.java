/*
 * Copyright (c) 2012 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

public class RegressionModelEvaluator extends RegressionModelManager implements Evaluator {

	public RegressionModelEvaluator(PMML pmml){
		super(pmml);
	}

	public RegressionModelEvaluator(PMML pmml, RegressionModel regressionModel){
		super(pmml, regressionModel);
	}

	public RegressionModelEvaluator(RegressionModelManager parent){
		this(parent.getPmml(), parent.getModel());
	}

	/**
	 * @see #evaluateRegression(Map)
	 */
	public Double evaluate(Map<FieldName, ?> parameters){
		RegressionModel regressionModel = getModel();

		MiningFunctionType miningFunction = regressionModel.getFunctionName();
		switch(miningFunction){
			case REGRESSION:
				return evaluateRegression(parameters);
			default:
				throw new UnsupportedFeatureException(miningFunction);
		}
	}

	public Double evaluateRegression(Map<FieldName, ?> parameters){
		double result = 0D;

		result += getIntercept();

		List<NumericPredictor> numericPredictors = getNumericPredictors();
		for(NumericPredictor numericPredictor : numericPredictors){
			result += evaluateNumericPredictor(numericPredictor, parameters);
		}
		
		List<CategoricalPredictor> categoricalPredictors = getCategoricalPredictors();
		for (CategoricalPredictor categoricalPredictor : categoricalPredictors) {
			result += evaluateCategoricalPredictor(categoricalPredictor, parameters);
		}

		return Double.valueOf(result);
	}

	private double evaluateNumericPredictor(NumericPredictor numericPredictor, Map<FieldName, ?> parameters){
		Number value = (Number)ParameterUtil.getValue(parameters, numericPredictor.getName());

		return numericPredictor.getCoefficient() * value.doubleValue();
	}
	
	private double evaluateCategoricalPredictor(CategoricalPredictor categoricalPredictor, Map<FieldName, ?> parameters){
		String value = (String) ParameterUtil.getValue(parameters, categoricalPredictor.getName());
		
		
		return categoricalPredictor.getCoefficient() * (categoricalPredictor.getValue().equals(value) ? 1 : 0);
	}
}