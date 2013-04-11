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
	public Object evaluate(Map<FieldName, ?> parameters){
		RegressionModel regressionModel = getModel();

		MiningFunctionType miningFunction = regressionModel.getFunctionName();
		switch(miningFunction){
			case REGRESSION:
				return evaluateRegression(parameters);
			case CLASSIFICATION:
				return evaluateClassification(parameters);
			default:
				throw new UnsupportedFeatureException(miningFunction);
		}
	}

	private String evaluateClassification(Map<FieldName, ?> parameters) {
		TreeMap<String, Double> targetCategoryToScore = new TreeMap<String, Double>();
		
		for (RegressionTable rt : getOrCreateRegressionTables()) {
			Double rtValue = evaluateRegressionTable(rt, parameters);
			if (rtValue != null) {
				targetCategoryToScore.put(rt.getTargetCategory(), rtValue);
			}
		}
		String result = new String();
		if (targetCategoryToScore.isEmpty()) return null;
		
		TreeMap<Double, String> scoreToCategory = new TreeMap<Double, String>();
		switch (getNormalizationMethodType()) {
			case NONE:
				// pick the category with top score
				for (Map.Entry<String, Double> categoryScore : targetCategoryToScore.entrySet()) {
					scoreToCategory.put(categoryScore.getValue(), categoryScore.getKey());
				}
				result = scoreToCategory.lastEntry().getValue();
				break;
			case LOGIT:
				// pick the max of pj = 1 / ( 1 + exp( -yj ) )
				for (Map.Entry<String, Double> categoryScore : targetCategoryToScore.entrySet()) {
					double yj = categoryScore.getValue();
					double pj = 1.0/(1.0 + Math.exp(yj));

					scoreToCategory.put(pj, categoryScore.getKey());
				}
				result = scoreToCategory.lastEntry().getValue();
				break;
			case EXP:
				// pick the max of exp(yj) 
				for (Map.Entry<String, Double> categoryScore : targetCategoryToScore.entrySet()) {
					double yj = categoryScore.getValue();
					double pj = Math.exp(yj);
					scoreToCategory.put(pj, categoryScore.getKey());
				}
				result = scoreToCategory.lastEntry().getValue();
				break;
			case SOFTMAX:
				// pj = exp(yj) / (Sum[i = 1 to N](exp(yi) ) ) 
				double sum = 0.0;
				for (Map.Entry<String, Double> categoryScore : targetCategoryToScore.entrySet()) {
					double yj = categoryScore.getValue();
					sum += Math.exp(yj);
				}
				for (Map.Entry<String, Double> categoryScore : targetCategoryToScore.entrySet()) {
					double yj = categoryScore.getValue();
					double pj = Math.exp(yj) / sum;
					scoreToCategory.put(pj, categoryScore.getKey());
				}
				result = scoreToCategory.lastEntry().getValue();
				break;
			case CLOGLOG:
				// pick the max of pj = 1 - exp( -exp( yj ) ) 
				for (Map.Entry<String, Double> categoryScore : targetCategoryToScore.entrySet()) {
					double yj = categoryScore.getValue();
					double pj = 1 - Math.exp(-Math.exp(yj));
					scoreToCategory.put(pj, categoryScore.getKey());
				}
				result = scoreToCategory.lastEntry().getValue();
				break;
			case LOGLOG:
				// pick the max of pj = exp( -exp( -yj ) ) 
				for (Map.Entry<String, Double> categoryScore : targetCategoryToScore.entrySet()) {
					double yj = categoryScore.getValue();
					double pj = Math.exp(-Math.exp( -yj));
					scoreToCategory.put(pj, categoryScore.getKey());
				}
				result = scoreToCategory.lastEntry().getValue();
				break;
			default:
				
				result = null;					
		}

		return result;
	}

	private Double evaluateRegression(Map<FieldName, ?> parameters) {
		double result = evaluateRegressionTable(getOrCreateRegressionTable(), parameters);

		RegressionNormalizationMethodType normalizationMethod = getNormalizationMethodType();
		switch (normalizationMethod) {
			case NONE:
				// The same thing than: result = result;
				break;
			case SOFTMAX:
			case LOGIT:
				result = 1.0 / (1.0 + Math.exp(-result));
				break;
			case EXP:
				result = Math.exp(result);
				break;
			default:
				// We should never be here.
				assert false;
				break;
		}
				
		return result;
	}

	private double evaluateRegressionTable(RegressionTable rt, Map<FieldName, ?> parameters) {
		double result = 0D;

		result += getIntercept(rt);
		
		List<NumericPredictor> numericPredictors = rt.getNumericPredictors();
		for(NumericPredictor numericPredictor : numericPredictors) {
			result += evaluateNumericPredictor(numericPredictor, parameters);
		}

		List<CategoricalPredictor> categoricalPredictors = rt.getCategoricalPredictors();
		for (CategoricalPredictor categoricalPredictor : categoricalPredictors) {
			result += evaluateCategoricalPredictor(categoricalPredictor, parameters);
		}

		return Double.valueOf(result);
	}
	
	private double evaluateNumericPredictor(NumericPredictor numericPredictor, Map<FieldName, ?> parameters){
		Number value = (Number)ParameterUtil.getValue(parameters, numericPredictor.getName());

		return numericPredictor.getCoefficient()
				* Math.pow(value.doubleValue(), numericPredictor.getExponent().doubleValue());
	}
	
	private double evaluateCategoricalPredictor(CategoricalPredictor categoricalPredictor, Map<FieldName, ?> parameters) {
		Object blobValue =  ParameterUtil.getValue(parameters, categoricalPredictor.getName());
		boolean isEqual = false;
		List<DataField> ldf = getDataDictionary().getDataFields();

		for (DataField df : ldf) {
			if (df.getName().getValue().equals(categoricalPredictor.getName().getValue())) {
				switch (df.getDataType()) {
				case INTEGER:
					isEqual = (Integer) blobValue == Integer.parseInt(categoricalPredictor.getValue());
					break;
				case DOUBLE:
					isEqual = (Double) blobValue == Double.parseDouble(categoricalPredictor.getValue());
					break;
				case BOOLEAN:
					isEqual = (Boolean) blobValue == Boolean.parseBoolean(categoricalPredictor.getValue());
					break;
				case FLOAT:
					isEqual = (Float) blobValue == Float.parseFloat(categoricalPredictor.getValue());
					break;
				case STRING:
					isEqual = categoricalPredictor.getValue().equals((String)blobValue);
					break;
				default:
					throw new UnsupportedOperationException();
				}
			}
		}
		return categoricalPredictor.getCoefficient() * (isEqual ? 1 : 0);
	}
}