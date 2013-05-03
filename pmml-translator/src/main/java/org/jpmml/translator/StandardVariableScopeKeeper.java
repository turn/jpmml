package org.jpmml.translator;

import java.util.HashMap;
import java.util.Map;

import org.dmg.pmml.DataDictionary;
import org.dmg.pmml.DataField;
import org.dmg.pmml.DataType;

public class StandardVariableScopeKeeper implements IVariableScopeKeeper {
	HashMap<String, String> nameToVariable = new HashMap<String, String>();
	HashMap<String, String> nameToValue = new HashMap<String, String>();


	public StandardVariableScopeKeeper(DataDictionary dataDictionary, TranslationContext context) throws TranslationException {
		for (DataField df : dataDictionary.getDataFields()) {
			if (df.getValues() == null || df.getValues().size() == 0 || df.getName().getValue().charAt(0) != '$')
				continue;

			nameToValue.put(df.getName().getValue(), expand(context, df.getValues().get(0).getValue()));

			nameToVariable.put(df.getName().getValue(), df.getName().getValue().substring(2, df.getName().getValue().length() - 1));
		}
	}

	public void declareAllVariables(CodeFormatter cf,
			TranslationContext context, StringBuilder code) {
		for (Map.Entry<String, String> e : nameToValue.entrySet()) {
			cf.declareVariable(code, context, new Variable(DataType.DOUBLE, nameToVariable.get(e.getKey())), e.getValue());
		}
	}

	public String getValue(TranslationContext context, String variable) throws TranslationException {
		return expand(context, variable);
	}

	/**
	 * Take a variable and expand it. i.e: ${variableName}.
	 *
	 * @param variable The variable to expand into the variable that contains
	 * the name of the variable that tracks the result at run time. Must start by '${'
	 * and end with '}'. The variable name is everything between these braces.
	 * It looks in the currently declared variable.
	 *
	 * @return The variable expanded.
	 * @throws TranslationException If the variable is ill-formed or not
	 * declared.
	 */
	private String expandVariable(String variable) throws TranslationException {
		// Extract the real name.
		if (variable.charAt(0) != '$' || variable.charAt(1) != '{'
				|| variable.charAt(variable.length() - 1) != '}') {
			throw new TranslationException(variable + " is ill-formed.");
		}

		// Simple way of doing this thing considering there is no nesting.
		// Nesting doesn't make sense. So it might be enough.
		String name = variable.substring(2, variable.length() - 1);

		String result = "";
		if (nameToVariable.containsKey(name)) {
			result = nameToVariable.get(name);
		}
		else {
			throw new TranslationException(name + " is not declared.");
		}

		return result;
	}

	private String expand(TranslationContext context, String expandMe) throws TranslationException {
		return expand(context, expandMe, 0);
	}

	private String expand(TranslationContext context, String expandMe, int offset) throws TranslationException {
		if (expandMe.charAt(0) == '$') {
			return expandVariable(expandMe);
		}
		else if (expandMe.charAt(0) == '%') {
			return expandFunction(context, expandMe, offset + 1);
		}
		return expandMe;
	}

	/**
	 * Take a function call, and format it correctly. Expand variables if needed,
	 * and apply format variables to all arguments.
	 *
	 * @param context The translation context, useful for apply formatVariableName.
	 * @param functionCall The string formatted as: "&functionCall(arg1, arg2, ...)"
	 * where arg* can be a variable or a functionCall.
	 * @return a code that is correct.
	 * @throws TranslationException If functionCall is invalid.
	 */
	// FIXME: Not nested at this time.
	// FIXME: Apply formatVariableName.
//	private String expandFunction(TranslationContext context, String functionCall) throws TranslationException {
//		// Remove all the whitespaces to make the code simpler.
//		String noWhitespaceFunctionCall = functionCall.replaceAll("\\s","");
//		if (noWhitespaceFunctionCall.charAt(0) != '&') {
//			throw new TranslationException(functionCall + " is not a valid function call. Missing &.");
//		}
//		return expandFunction(context, noWhitespaceFunctionCall, 1);
//	}

	private String expandFunction(TranslationContext context, String functionCall, int offset) throws TranslationException {
		StringBuilder result = new StringBuilder();

		int length = functionCall.length();
		int beginFunctionName = offset;
		int endFunctionName = offset;
		while (offset < length && functionCall.charAt(offset) != '(') {
			++offset;
		}

		endFunctionName = offset++;

		result.append(functionCall.substring(beginFunctionName, endFunctionName)).append("(");

		// Now we have to parse the args.
		// Eat the paren.
		// If there is no args, return empty parenthesis.
		if (functionCall.charAt(offset + 1) == ')') {
			result.append(")");
			return result.toString();
		}

		do {
			int beginArg = offset;
			char cur = functionCall.charAt(offset);
			if (cur == ',') {
				++offset;
				result.append(",");
			}
			if (cur == '$' || cur == '%') {
				result.append(expand(context, functionCall, offset));
			}
			else {
				while (functionCall.charAt(offset) != ',' && functionCall.charAt(offset) != ')') {
					++offset;
				}
				result.append(functionCall.substring(beginArg, offset));
			}

		} while (functionCall.charAt(offset) == ','); // While there is more args,
		// eat them.

		result.append(")");
		return result.toString();
	}

}
