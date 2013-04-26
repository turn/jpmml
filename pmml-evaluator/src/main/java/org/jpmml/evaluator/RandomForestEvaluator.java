/*
 * Copyright (c) 2012 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

public class RandomForestEvaluator extends RandomForestManager implements Evaluator {

	public RandomForestEvaluator(PMML pmml){
		super(pmml);
	}

	public RandomForestEvaluator(PMML pmml, MiningModel miningModel){
		super(pmml, miningModel);
	}

	public RandomForestEvaluator(RandomForestManager parent){
		this(parent.getPmml(), parent.getModel());
	}

	/**
	 * @see #evaluateRegression(Map)
	 */
	public IPMMLResult evaluate(Map<FieldName, ?> parameters){
		MiningModel model = getModel();

		MiningFunctionType miningFunction = model.getFunctionName();
		switch(miningFunction){
			case REGRESSION:
				return evaluateRegression(parameters);
			default:
				throw new UnsupportedFeatureException(miningFunction);
		}
	}

	public IPMMLResult evaluateRegression(Map<FieldName, ?> parameters){
		Segmentation segmentation = getSegmentation();

		double sum = 0;
		double weightedSum = 0;

		int count = 0;

		List<Segment> segments = getSegments();
		for(Segment segment : segments){
			Predicate predicate = segment.getPredicate();

			Boolean selectable = PredicateUtil.evaluatePredicate(predicate, parameters);
			if(selectable == null){
				throw new EvaluationException();
			} // End if

			if(!selectable.booleanValue()){
				continue;
			}

			TreeModel treeModel = (TreeModel)segment.getModel();
			if(treeModel == null){
				throw new EvaluationException();
			}

			TreeModelEvaluator treeModelEvaluator = new TreeModelEvaluator(getPmml(), treeModel);

			IPMMLResult res = treeModelEvaluator.evaluate(parameters);
			String score;
			try {
				score = (String) res.getValue(getOutputField(treeModelEvaluator).getName());
			} catch (NoSuchElementException e) {
				throw new EvaluationException();
			} catch (Exception e) {
				throw new EvaluationException();
			}
			if(score == null){
				throw new EvaluationException();
			}

			Double value = Double.valueOf(score);

			sum += value.doubleValue();
			weightedSum += (segment.getWeight() * value.doubleValue());

			count++;
		}

		MultipleModelMethodType multipleModelMethod = segmentation.getMultipleModelMethod();
		switch(multipleModelMethod){
			case SUM:
				// Sum already contains the good result.
				break;
			case AVERAGE:
				sum = (sum / count);
				break;
			case WEIGHTED_AVERAGE:
				sum = (weightedSum / count); // XXX
				break;
			default:
				throw new UnsupportedFeatureException(multipleModelMethod);
		}

		PMMLResult res = new PMMLResult();
		try {
			res.put(getOutputField(this).getName(), sum);
		} catch (Exception e) {
			throw new EvaluationException(e.getMessage());
		}

		return res;
	}
}