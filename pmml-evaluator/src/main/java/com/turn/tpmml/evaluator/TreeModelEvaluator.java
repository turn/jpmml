/*
 * Copyright (c) 2012 University of Tartu
 */
package com.turn.tpmml.evaluator;

import com.turn.tpmml.FieldName;
import com.turn.tpmml.Node;
import com.turn.tpmml.PMML;
import com.turn.tpmml.Predicate;
import com.turn.tpmml.TreeModel;
import com.turn.tpmml.manager.IPMMLResult;
import com.turn.tpmml.manager.ManagerException;
import com.turn.tpmml.manager.TreeModelManager;
import com.turn.tpmml.manager.TreePMMLResult;

import java.util.Map;

public class TreeModelEvaluator extends TreeModelManager implements Evaluator {

	private static final long serialVersionUID = 1L;

	public TreeModelEvaluator(PMML pmml) {
		super(pmml);
	}

	public TreeModelEvaluator(PMML pmml, TreeModel treeModel) {
		super(pmml, treeModel);
	}

	public TreeModelEvaluator(TreeModelManager parent) {
		this(parent.getPmml(), parent.getModel());
	}

	public Object prepare(FieldName name, Object value) {
		return ParameterUtil.prepare(getDataField(name), getMiningField(name), value);
	}

	public IPMMLResult evaluate(Map<FieldName, ?> parameters) {

		String result = null;
		Node currentNode = null;
		Node rootNode = getOrCreateRoot();

		ModelManagerEvaluationContext context = new ModelManagerEvaluationContext(this, parameters);
		Predicate rootPredicate = rootNode.getPredicate();
		Boolean predicateResult = PredicateUtil.evaluate(rootPredicate, context);

		if (predicateResult != null) {
			if (predicateResult.booleanValue()) {
				result = rootNode.getScore();
				currentNode = rootNode;
			}
		} else {
			// if root evaluates to "UNKNOWN" - we are done for all missing value
			// strategies, except default child

			switch (getModel().getMissingValueStrategy()) {
			case NONE:
			case LAST_PREDICTION:
			case NULL_PREDICTION:
				break;
			/* use default node if available */
			case DEFAULT_CHILD:

				String defaultChildId = rootNode.getDefaultChild();
				if (defaultChildId == null) {
					throw new EvaluationException("Default child is undefined");
				}
				Node defauldChild = null;
				for (Node child : rootNode.getNodes()) {
					if (child.getId() != null && child.getId().equals(defaultChildId)) {
						defauldChild = child;
						break;
					}
				}

				if (defauldChild != null) {
					// result = defauldChild.getScore();
					currentNode = defauldChild;
				} else {
					throw new EvaluationException("No default child");
				}
				break;
			default:
				throw new EvaluationException("Unsupported missing value strategy: " +
						getModel().getMissingValueStrategy());
			}
		}

		while (currentNode != null && currentNode.getNodes() != null &&
				!currentNode.getNodes().isEmpty()) {

			boolean pickedNextNode = false;
			for (Node node : currentNode.getNodes()) {

				Predicate predicate = node.getPredicate();
				predicateResult = PredicateUtil.evaluate(predicate, context);

				if (predicateResult != null) {
					if (predicateResult.booleanValue()) {
						result = node.getScore();
						currentNode = node;
						pickedNextNode = true;
						break;
					}
				} else {
					// UNKNOWN value from predicate evaluation

					switch (getModel().getMissingValueStrategy()) {
					/* same as FALSE for current predicate */
					case NONE:
						break;
					/* abort with current prediction */
					case LAST_PREDICTION:
						currentNode = null;
						break;
					/* abort with null prediction */
					case NULL_PREDICTION:
						result = null;
						currentNode = null;
						break;
					/* use default node if available */
					case DEFAULT_CHILD:

						String defaultChildId = node.getDefaultChild();
						if (defaultChildId == null) {
							throw new EvaluationException("Default child is undefined");
						}
						Node defauldChild = null;
						for (Node child : currentNode.getNodes()) {
							if (child.getId() != null && child.getId().equals(defaultChildId)) {
								defauldChild = child;
								break;
							}
						}

						if (defauldChild != null) {
							result = defauldChild.getScore();
							currentNode = defauldChild;
						} else {
							throw new EvaluationException("No default child");
						}
						break;
					default:
						throw new EvaluationException("Unsupported missing value strategy: " +
								getModel().getMissingValueStrategy());
					}
				}
			}

			// no abort yet and no node evaluated to TRUE
			if (currentNode != null && !pickedNextNode) {
				switch (getModel().getNoTrueChildStrategy()) {
				case RETURN_LAST_PREDICTION:
					currentNode = null;
					break;
				case RETURN_NULL_PREDICTION:
					currentNode = null;
					result = null;
					break;
				}
			}
		}

		TreePMMLResult res = new TreePMMLResult();
		try {
			res.put(getOutputField(this).getName(), result);
			// Sometimes we ends up with no currentNode.
			if (currentNode != null) {
				res.setNodeId(currentNode.getId());
			}
		} catch (ManagerException e) {
			throw new EvaluationException(e.getMessage());
		}

		return res;
	}
}
