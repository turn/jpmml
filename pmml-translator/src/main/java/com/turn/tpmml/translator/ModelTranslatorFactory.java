package com.turn.tpmml.translator;

import com.turn.tpmml.MiningModel;
import com.turn.tpmml.Model;
import com.turn.tpmml.PMML;
import com.turn.tpmml.RegressionModel;
import com.turn.tpmml.Scorecard;
import com.turn.tpmml.TreeModel;
import com.turn.tpmml.manager.ModelManager;
import com.turn.tpmml.manager.ModelManagerException;
import com.turn.tpmml.manager.ModelManagerFactory;
import com.turn.tpmml.manager.UnsupportedFeatureException;

public class ModelTranslatorFactory extends ModelManagerFactory {

	protected ModelTranslatorFactory() {
	}

	@Override
	public ModelManager<? extends Model> getModelManager(PMML pmml, Model model)
			throws ModelManagerException {

		if (model instanceof TreeModel) {
			return new TreeModelTranslator(pmml, (TreeModel) model);
		} else

		if (model instanceof Scorecard) {
			return new ScorecardTranslator(pmml, (Scorecard) model);
		} else

		if (model instanceof RegressionModel) {
			return new RegressionModelTranslator(pmml, (RegressionModel) model);
		} else

		if (model instanceof MiningModel) {
			return new MiningModelTranslator(pmml, (MiningModel) model);
		} else {
			throw new ModelManagerException(new UnsupportedFeatureException(model));
		}
	}

	public static ModelTranslatorFactory getInstance() {
		return new ModelTranslatorFactory();
	}
}
