/*
 * Copyright (c) 2009 University of Tartu
 */
package org.jpmml.manager;

import java.util.*;

import org.dmg.pmml.*;

public class RegressionModelManager extends ModelManager<RegressionModel> {

	private RegressionModel regressionModel = null;

	private RegressionTable regressionTable = null;


	public RegressionModelManager(){
	}

	public RegressionModelManager(PMML pmml){
		this(pmml, find(pmml.getContent(), RegressionModel.class));
	}

	public RegressionModelManager(PMML pmml, RegressionModel regressionModel){
		super(pmml);

		this.regressionModel = regressionModel;
	}

	public String getSummary(){
		return "Regression";
	}

	@Override
	public RegressionModel getModel(){
		ensureNotNull(this.regressionModel);

		return this.regressionModel;
	}

	public RegressionModel createRegressionModel(){
		return createModel(MiningFunctionType.REGRESSION);
	}

	/**
	 * @throws ModelManagerException If the Model already exists
	 *
	 * @see #getModel()
	 */
	public RegressionModel createModel(MiningFunctionType miningFunction){
		ensureNull(this.regressionModel);

		this.regressionModel = new RegressionModel(new MiningSchema(), miningFunction);

		List<Model> content = getPmml().getContent();
		content.add(this.regressionModel);

		return this.regressionModel;
	}

	public FieldName getTarget(){
		RegressionModel regressionModel = getModel();

		return regressionModel.getTargetFieldName();
	}

	public RegressionModel setTarget(FieldName name){
		RegressionModel regressionModel = getModel();
		regressionModel.setTargetFieldName(name);

		return regressionModel;
	}

	public Double getIntercept(){
		RegressionTable regressionTable = getOrCreateRegressionTable();

		return Double.valueOf(regressionTable.getIntercept());
	}

	public RegressionTable setIntercept(Double intercept){
		RegressionTable regressionTable = getOrCreateRegressionTable();
		regressionTable.setIntercept(intercept.doubleValue());

		return regressionTable;
	}

	public List<NumericPredictor> getNumericPredictors(){
		RegressionTable regressionTable = getOrCreateRegressionTable();

		return regressionTable.getNumericPredictors();
	}

	public NumericPredictor getNumericPredictor(FieldName name){
		List<NumericPredictor> numericPredictors = getNumericPredictors();

		for(NumericPredictor numericPredictor : numericPredictors){

			if((numericPredictor.getName()).equals(name)){
				return numericPredictor;
			}
		}

		return null;
	}

	public NumericPredictor addNumericPredictor(FieldName name, Double coefficient){
		RegressionTable regressionTable = getOrCreateRegressionTable();

		NumericPredictor numericPredictor = new NumericPredictor(name, coefficient.doubleValue());
		regressionTable.getNumericPredictors().add(numericPredictor);

		return numericPredictor;
	}

	public List<CategoricalPredictor> getCategoricalPredictors(){
		RegressionTable regressionTable = getOrCreateRegressionTable();

		return regressionTable.getCategoricalPredictors();
	}

	public CategoricalPredictor getCategoricalPredictor(FieldName name){
		List<CategoricalPredictor> categoricalPredictors = getCategoricalPredictors();

		for(CategoricalPredictor categoricalPredictor : categoricalPredictors){

			if((categoricalPredictor.getName()).equals(name)){
				return categoricalPredictor;
			}
		}

		return null;
	}

	public CategoricalPredictor addCategoricalPredictor(FieldName name, String value, Double coefficient){
		RegressionTable regressionTable = getOrCreateRegressionTable();

		CategoricalPredictor categoricalPredictor = new CategoricalPredictor(name, value, coefficient);
		regressionTable.getCategoricalPredictors().add(categoricalPredictor);

		return categoricalPredictor;
	}
	
	public RegressionTable getOrCreateRegressionTable(){

		if(this.regressionTable == null){
			RegressionModel regressionModel = getModel();

			List<RegressionTable> regressionTables = regressionModel.getRegressionTables();
			if(regressionTables.isEmpty()){
				RegressionTable regressionTable = new RegressionTable(0d);

				regressionTables.add(regressionTable);
			}

			this.regressionTable = regressionTables.get(0);
		}

		return this.regressionTable;
	}
	
	public RegressionNormalizationMethodType getNormalizationMethodType() {
		
		return regressionModel.getNormalizationMethod();
	}
}