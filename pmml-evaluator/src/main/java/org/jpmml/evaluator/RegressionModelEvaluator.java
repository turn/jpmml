/*
 * Copyright (c) 2012 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

/**
 * This class evaluates the variables on the model. It reads the pmml object
 * to return a result.
 * For information about the regression model, see {@link RegressionModelManager}.
 *
 * @author tbadie
 *
 */
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
	 * Evaluate the model with the given parameters.
	 *
	 * @see #evaluateRegression(Map), @see #evaluateClassification(Map).
	 */
	public IPMMLResult evaluate(Map<FieldName, ?> parameters){
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

	/**
	 * Evaluate a classification.
	 *
	 * @param parameters The set of parameters.
	 * @return The name of the chosen category.
	 */
	private IPMMLResult evaluateClassification(Map<FieldName, ?> parameters) {
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
					double pj = 1.0/(1.0 + Math.exp(-yj));

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

		PMMLResult res = new PMMLResult();
		try {
			res.put(getOutputField(this).getName(), result);
		} catch (Exception e) {
			throw new EvaluationException(e.getMessage());
		}

		return res;
	}

	private IPMMLResult evaluateRegression(Map<FieldName, ?> parameters) {
		// When it's a simple regression, there is only one table. So we just
		// evaluate it, normalize the result and return it.
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

		PMMLResult res = new PMMLResult();
		try {
			res.put(getOutputField(this).getName(), result);
		} catch (Exception e) {
			throw new EvaluationException(e.getMessage());
		}

		return res;
	}

	/**
	 * Evaluate a regression table.
	 *
	 * @param rt The regression table.
	 * @param parameters The set of parameters.
	 * @return The evaluation.
	 */
	private double evaluateRegressionTable(RegressionTable rt, Map<FieldName, ?> parameters) {
		// Evaluating a regression table is only evaluate all the numeric predictors,
		// and all the categorical predictors.
		double result = 0D;

		result += getIntercept(rt);

		// If a value is missing for a numeric predictors, it's an error.
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

	/**
	 * Evaluate a numeric predictor on a set of parameters.
	 *
	 * @param numericPredictor The numeric predictor.
	 * @param parameters The set of parameters.
	 * @return
	 */
	private double evaluateNumericPredictor(NumericPredictor numericPredictor, Map<FieldName, ?> parameters){
		Number value = (Number)ParameterUtil.getValue(parameters, numericPredictor.getName());

		return numericPredictor.getCoefficient()
				* Math.pow(value.doubleValue(), numericPredictor.getExponent().doubleValue());
	}

	/**
	 * Evaluate a categorical predictor on a set of parameters.
	 *
	 * @param categoricalPredictor The predictor.
	 * @param parameters The parameters.
	 * @return The result of the evaluation.
	 */
	private double evaluateCategoricalPredictor(CategoricalPredictor categoricalPredictor, Map<FieldName, ?> parameters) {
		// The concept of the categorical predictor is: if a variable has a
		// certain value, we return the coefficient. Otherwise we return 0.
		// The problem is, the value can be a string, a double, an integer...
		// And the equality is not done the same way. So we have to look the
		// type of the variable used to know how to compare them. Because 0.0 != "0".
		// This is why there is this ugly switch below. It's unfortunate, but it's the
		// only way that works I have found.
		Object blobValue = null;
		try {
			blobValue = ParameterUtil.getValue(parameters, categoricalPredictor.getName());
		} catch (EvaluationException e) {
			return 0.0;
		}
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