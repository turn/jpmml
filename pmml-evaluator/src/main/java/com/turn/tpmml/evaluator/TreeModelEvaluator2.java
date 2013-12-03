/*
 * Copyright (c) 2012 University of Tartu
 */
package com.turn.tpmml.evaluator;

import com.turn.tpmml.FieldName;
import com.turn.tpmml.NoTrueChildStrategyType;
import com.turn.tpmml.Node;
import com.turn.tpmml.PMML;
import com.turn.tpmml.Predicate;
import com.turn.tpmml.TreeModel;
import com.turn.tpmml.manager.IPMMLResult;
import com.turn.tpmml.manager.PMMLResult;
import com.turn.tpmml.manager.TreeModelManager;
import com.turn.tpmml.manager.TreePMMLResult;
import com.turn.tpmml.manager.UnsupportedFeatureException;

import java.util.List;
import java.util.Map;

public class TreeModelEvaluator2 extends TreeModelManager implements Evaluator {

	private static final long serialVersionUID = 1L;

	public TreeModelEvaluator2(PMML pmml) {
		super(pmml);
	}

	public TreeModelEvaluator2(PMML pmml, TreeModel treeModel) {
		super(pmml, treeModel);
	}

	public TreeModelEvaluator2(TreeModelManager parent) {
		this(parent.getPmml(), parent.getModel());
	}

	public Object prepare(FieldName name, Object value) {
		return ParameterUtil.prepare(getDataField(name), getMiningField(name), value);
	}

	/**
	 * @see #evaluateTree(EvaluationContext)
	 */
	public IPMMLResult evaluate(Map<FieldName, ?> parameters) {
		ModelManagerEvaluationContext context = new ModelManagerEvaluationContext(this, parameters);

		Node node = evaluateTree(context);

		NodeClassificationMap values = new NodeClassificationMap(node);

		// Map<FieldName, NodeClassificationMap> predictions =
		// Collections.singletonMap(getTarget(), values);

		TreePMMLResult res = new TreePMMLResult();
		res.put(getTarget(), values);
		PMMLResult tmpRes = OutputUtil.evaluate(res, context);
		res.absorb(tmpRes);
		// Sometimes we ends up with no currentNode.
		if (node != null) {
			res.setNodeId(node.getId());
		}

		return res;
	}

	public Node evaluateTree(EvaluationContext context) {
		Node root = getOrCreateRoot();

		Prediction prediction = findTrueChild(root, root, context); // XXX

		if (prediction.getLastTrueNode() != null && prediction.getTrueNode() != null &&
				!(prediction.getLastTrueNode()).equals(prediction.getTrueNode())) {
			return prediction.getTrueNode();
		} else {
			NoTrueChildStrategyType noTrueChildStrategy = getModel().getNoTrueChildStrategy();
			switch (noTrueChildStrategy) {
			case RETURN_NULL_PREDICTION:
				return null;
			case RETURN_LAST_PREDICTION:
				return prediction.getLastTrueNode();
			default:
				throw new UnsupportedFeatureException(noTrueChildStrategy);
			}
		}
	}

	private Prediction findTrueChild(Node lastNode, Node node, EvaluationContext context) {
		Boolean value = evaluateNode(node, context);

		if (value == null) {
			throw new EvaluationException();
		} // End if

		if (value.booleanValue()) {
			List<Node> children = node.getNodes();

			for (Node child : children) {
				Prediction childPrediction = findTrueChild(node, child, context);

				if (childPrediction.getTrueNode() != null) {
					return childPrediction;
				}
			}

			return new Prediction(lastNode, node);
		} else {
			return new Prediction(lastNode, null);
		}
	}

	private Boolean evaluateNode(Node node, EvaluationContext context) {
		Predicate predicate = node.getPredicate();
		if (predicate == null) {
			throw new EvaluationException();
		}

		return PredicateUtil.evaluate(predicate, context);
	}

	private static class Prediction {

		private Node lastTrueNode = null;

		private Node trueNode = null;

		public Prediction(Node lastTrueNode, Node trueNode) {
			this.lastTrueNode = lastTrueNode;
			this.trueNode = trueNode;
		}

		public Node getLastTrueNode() {
			return this.lastTrueNode;
		}

		public Node getTrueNode() {
			return this.trueNode;
		}
	}
}
