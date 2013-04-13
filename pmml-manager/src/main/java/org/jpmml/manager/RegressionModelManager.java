/*
 * Copyright (c) 2009 University of Tartu
 */
package org.jpmml.manager;

import java.util.List;

import org.dmg.pmml.CategoricalPredictor;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.MiningFunctionType;
import org.dmg.pmml.MiningSchema;
import org.dmg.pmml.Model;
import org.dmg.pmml.NumericPredictor;
import org.dmg.pmml.PMML;
import org.dmg.pmml.RegressionModel;
import org.dmg.pmml.RegressionNormalizationMethodType;
import org.dmg.pmml.RegressionTable;

/**
 * Provide an interface to the regressionModel class.
 * 
 * The regression functions are used to determine the relationship between the
 * dependent variable (target field) and one or more independent variables. The
 * dependent variable is the one whose values you want to predict, whereas the
 * independent variables are the variables that you base your prediction on.
 * While the term regression usually refers to the prediction of numeric values,
 * the PMML element RegressionModel can also be used for classification. This is
 * due to the fact that multiple regression equations can be combined in order
 * to predict categorical values.
 * 
 * 
 * @author tbadie
 * 
 */
public class RegressionModelManager extends ModelManager<RegressionModel> {

	private RegressionModel regressionModel = null;

	private RegressionTable regressionTable = null;
	private List<RegressionTable> regressionTables = null;

	public RegressionModelManager() {
	}

	public RegressionModelManager(PMML pmml) {
		this(pmml, find(pmml.getContent(), RegressionModel.class));
	}

	public RegressionModelManager(PMML pmml, RegressionModel regressionModel) {
		super(pmml);

		this.regressionModel = regressionModel;
	}

	public String getSummary() {
		return "Regression";
	}

	@Override
	public RegressionModel getModel() {
		ensureNotNull(this.regressionModel);

		return this.regressionModel;
	}

	public RegressionModel createRegressionModel() {
		return createModel(MiningFunctionType.REGRESSION);
	}

	/**
	 * @throws ModelManagerException
	 *             If the Model already exists
	 * 
	 * @see #getModel()
	 */
	public RegressionModel createModel(MiningFunctionType miningFunction) {
		ensureNull(this.regressionModel);

		this.regressionModel = new RegressionModel(new MiningSchema(),
				miningFunction);

		List<Model> content = getPmml().getContent();
		content.add(this.regressionModel);

		return this.regressionModel;
	}

	public FieldName getTarget() {
		RegressionModel regressionModel = getModel();

		return regressionModel.getTargetFieldName();
	}

	public RegressionModel setTarget(FieldName name) {
		RegressionModel regressionModel = getModel();
		regressionModel.setTargetFieldName(name);

		return regressionModel;
	}

	/**
	 * Get the intercept for the first regressionTable.
	 * 
	 * @return the intercept.
	 */
	public Double getIntercept() {
		RegressionTable regressionTable = getOrCreateRegressionTable();

		return Double.valueOf(regressionTable.getIntercept());
	}

	/**
	 * Get the intercept of the regressionTable given in argument.
	 * 
	 * @param rt The regressionTable used.
	 * @return The rt's intercept.
	 */
	public Double getIntercept(RegressionTable rt) {
		return Double.valueOf(rt.getIntercept());
	}

	/** 
	 * Set the intercept of the first table.
	 * 
	 * @param intercept The new value.
	 * @return The modified regression table.
	 */
	public RegressionTable setIntercept(Double intercept) {
		RegressionTable regressionTable = getOrCreateRegressionTable();
		regressionTable.setIntercept(intercept.doubleValue());

		return regressionTable;
	}

	/**
	 * Get the numeric predictors of a particular regression table.
	 * 
	 * @param rt The regressionTable used.
	 * @return The numeric predictors.
	 */
	public List<NumericPredictor> getNumericPredictors(RegressionTable rt) {
		return rt.getNumericPredictors();
	}

	/**
	 * Get a particular numeric predictor.
	 * 
	 * @param rt The regressionTable used.
	 * @param name The variable we want.
	 * @return The numeric predictor if found, null otherwise.
	 */
	public NumericPredictor getNumericPredictor(RegressionTable rt,
			FieldName name) {
		List<NumericPredictor> numericPredictors = getNumericPredictors(rt);

		for (NumericPredictor numericPredictor : numericPredictors) {

			if ((numericPredictor.getName()).equals(name)) {
				return numericPredictor;
			}
		}

		return null;
	}

	/**
	 * Add a new numeric predictor to the first regressionTable.
	 * 
	 * @param name The name of the variable.
	 * @param coefficient The corresponding coefficient.
	 * @return The numeric predictor.
	 */
	public NumericPredictor addNumericPredictor(FieldName name,
			Double coefficient) {
		RegressionTable regressionTable = getOrCreateRegressionTable();

		NumericPredictor numericPredictor = new NumericPredictor(name,
				coefficient.doubleValue());
		regressionTable.getNumericPredictors().add(numericPredictor);

		return numericPredictor;
	}

	/**
	 * Get all the categoricalPredictors of a particular regressionTable.
	 * 
	 * @param rt The regressionTable used.
	 * @return Its categoricalPredictors.
	 */
	public List<CategoricalPredictor> getCategoricalPredictors(
			RegressionTable rt) {
		return rt.getCategoricalPredictors();
	}

	/**
	 * Get a particular categoricalPredictor.
	 * 
	 * @param rt The regressionTable used.
	 * @param name The name of the categoricalPredictor wanted.
	 * @return The categorical predictor wanted if found, null otherwise.
	 */
	public CategoricalPredictor getCategoricalPredictor(RegressionTable rt,
			FieldName name) {
		List<CategoricalPredictor> categoricalPredictors = getCategoricalPredictors(rt);

		for (CategoricalPredictor categoricalPredictor : categoricalPredictors) {

			if ((categoricalPredictor.getName()).equals(name)) {
				return categoricalPredictor;
			}
		}

		return null;
	}

	/**
	 * Add a new categorical predictor to the first regressionTable.
	 *
	 * @param name The name of the variable.
	 * @param coefficient The corresponding coefficient.
	 * @return The categorical predictor.
	 */
	public CategoricalPredictor addCategoricalPredictor(RegressionTable rt,
			FieldName name, String value, Double coefficient) {
		CategoricalPredictor categoricalPredictor = new CategoricalPredictor(
				name, value, coefficient);
		rt.getCategoricalPredictors().add(categoricalPredictor);

		return categoricalPredictor;
	}

	/**
	 * Get the first regression table if it exists. Otherwise, create a new one.
	 * 
	 * @return The first regression table.
	 */
	public RegressionTable getOrCreateRegressionTable() {

		if (this.regressionTable == null) {
			RegressionModel regressionModel = getModel();

			List<RegressionTable> regressionTables = regressionModel
					.getRegressionTables();
			if (regressionTables.isEmpty()) {
				RegressionTable regressionTable = new RegressionTable(0d);

				regressionTables.add(regressionTable);
			}

			this.regressionTable = regressionTables.get(0);
		}

		return this.regressionTable;
	}

	
	/**
	 * Get all the regression tables if they exist. Otherwise, create a new one.
	 * 
	 * @return The regression tables.
	 */
	public List<RegressionTable> getOrCreateRegressionTables() {
		if (regressionTables == null) {
			regressionTables = getModel().getRegressionTables();

			if (regressionTables.isEmpty()) {
				regressionTables.add(new RegressionTable(0d));
			}
		}

		return regressionTables;
	}

	/**
	 * Get the type of normalization of the model.
	 * 
	 * @return The type of normalization.
	 */
	public RegressionNormalizationMethodType getNormalizationMethodType() {

		return regressionModel.getNormalizationMethod();
	}

	public MiningFunctionType getFunctionName() {
		return regressionModel.getFunctionName();
	}
}