/*
 * Copyright (c) 2012 University of Tartu
 */
package org.jpmml.manager;

import java.util.*;

import org.dmg.pmml.*;

public class MiningModelManager extends ModelManager<MiningModel> {

	private MiningModel miningModel = null;


	public MiningModelManager(){
	}

	public MiningModelManager(PMML pmml){
		this(pmml, find(pmml.getContent(), MiningModel.class));
	}

	public MiningModelManager(PMML pmml, MiningModel miningModel){
		super(pmml);

		this.miningModel = miningModel;
	}

	@Override
	public MiningModel getModel(){
		ensureNotNull(this.miningModel);

		return this.miningModel;
	}

	public String getSummary() {
		return "MiningModel";
	}

	/**
	 * @throws ModelManagerException If the Model already exists
	 *
	 * @see #getModel()
	 */
	public MiningModel createModel(MiningFunctionType miningFunction){
		ensureNull(this.miningModel);

		this.miningModel = new MiningModel(new MiningSchema(), miningFunction);

		List<Model> content = getPmml().getContent();
		content.add(this.miningModel);

		return this.miningModel;
	}

	public List<Segment> getSegment() {
		return miningModel.getSegmentation().getSegments();
	}

	public MiningFunctionType getFunctionType() {
		return miningModel.getFunctionName();
	}

	public Segmentation getSegmentation() {
		return miningModel.getSegmentation();
	}

	public MultipleModelMethodType  getMultipleMethodModel() {
		return miningModel.getSegmentation().getMultipleModelMethod();
	}
}