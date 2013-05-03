package org.jpmml.translator;

public interface IVariableScopeKeeper {
	/**
	 * Declare all the variables of the local transformations.
	 *
	 * Call it in the beginning of the code generation process.
	 *
	 * @param cf The code formatter.
	 * @param context The translation context.
	 * @param code The code where we will append.
	 */
	public void declareAllVariables(CodeFormatter cf, TranslationContext context,
			StringBuilder code);

	/**
	 * Given a string representing a variable: "${variableName}", return
	 * the value associated to it.
	 *
	 * @param variable The name of the variable. '${' and '}' are part of the string.
	 * there might be nested variables.
	 * @return The value associated to this variable.
	 * @throws TranslationException If the variable or function does not exist.
	 */
	public String getValue(TranslationContext context, String variable) throws TranslationException;
}
