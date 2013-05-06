package org.jpmml.translator;

import java.util.HashMap;
import java.util.Map;

import org.dmg.pmml.DataDictionary;
import org.dmg.pmml.DataField;
import org.dmg.pmml.DataType;
import org.dmg.pmml.FieldName;
import org.jpmml.manager.ModelManager;

public class StandardVariableScopeKeeper implements IVariableScopeKeeper {
	HashMap<String, String> nameToVariable = new HashMap<String, String>();
	HashMap<String, String> nameToValue = new HashMap<String, String>();

	public StandardVariableScopeKeeper(DataDictionary dataDictionary, TranslationContext context, ModelManager<?> manager) throws TranslationException {
		for (DataField df : dataDictionary.getDataFields()) {
			if (df.getValues() == null || df.getValues().size() == 0 || !df.getName().getValue().startsWith("in_"))
				continue;

			nameToValue.put(df.getName().getValue(), expand(context, df.getValues().get(0).getValue().replaceAll("\\s", ""), manager));

			nameToVariable.put(df.getName().getValue(), df.getName().getValue());
		}
	}

	public void declareAllVariables(CodeFormatter cf,
			TranslationContext context, StringBuilder code) {
		for (Map.Entry<String, String> e : nameToValue.entrySet()) {
			cf.declareVariable(code, context, new Variable(DataType.DOUBLE, nameToVariable.get(e.getKey())), e.getValue());
		}
	}

	public String getValue(TranslationContext context, String variable) throws TranslationException {
		return nameToVariable.get(variable);
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

	private String expand(TranslationContext context, String expandMe, ModelManager<?> manager) throws TranslationException {
		int[] offset = new int[1];
		offset[0] = 0;
		return expand(context, expandMe, manager, offset);
	}

	private String expand(TranslationContext context, String expandMe, ModelManager<?> manager, int[] offset) throws TranslationException {
		if (expandMe.charAt(0) == '$') {
			return expandVariable(expandMe);
		}
		else if (expandMe.charAt(0) == '%') {
			++offset[0];
			return expandFunction(context, expandMe, manager, offset);
		}
		return expandMe;
	}

	
	private Boolean checkValidFunctionName(TranslationContext context, String functionName) {
		int indexOfDot = functionName.indexOf('.');
		if (indexOfDot < 0) {
			return false;
		}

		try {
			Class<?> cls = Class.forName(context.getBasePackageFunctions() + "." + functionName.substring(0, indexOfDot));
			UserDefinedFunction udf = cls.getAnnotation(UserDefinedFunction.class);

			for (String s : udf.methods()) {
				if (s.equals(functionName.substring(indexOfDot + 1))) {
					return true;
				}
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		
		
		return false;
	}
	
	/**
	 * Take a function call, and format it correctly. Expand variables if needed,
	 * and apply format variables to all arguments.
	 *
	 * @param context The translation context, useful for apply formatVariableName.
	 * @param functionCall The string formatted as: "%functionCall(arg1, arg2, ...)"
	 * where arg* can be a variable or a functionCall.
	 * @param manager Needed to format the arguments.
	 * @param offset The beginning of the string.
	 * @return a code that is correct.
	 * @throws TranslationException If functionCall is invalid.
	 */
	private String expandFunction(TranslationContext context, String functionCall, ModelManager<?> manager, int[] offset) throws TranslationException {
		StringBuilder result = new StringBuilder();

		int length = functionCall.length();
		int beginFunctionName = offset[0];
		int endFunctionName = offset[0];
		while (offset[0] < length && functionCall.charAt(offset[0]) != '(') {
			++offset[0];
		}

		endFunctionName = offset[0]++;

		String functionName = functionCall.substring(beginFunctionName, endFunctionName);
		if (!checkValidFunctionName(context, functionName)) {
			System.out.println("Fail.");
			throw new TranslationException(functionName + " not found.");
		}
		result.append(functionName).append("(");

		// Now we have to parse the args.
		// Eat the paren.
		// If there is no args, return empty parenthesis.
		if (functionCall.charAt(offset[0] + 1) == ')') {
			result.append(")");
			return result.toString();
		}

		do {
			int beginArg = offset[0];
			char cur = functionCall.charAt(offset[0]);
			if (cur == ',') {
				++offset[0];
				++beginArg;
				result.append(",");
			}
			if (cur == '$' || cur == '%') {
				result.append(expand(context, functionCall, manager, offset));
			}
			else {
				while (functionCall.charAt(offset[0]) != ',' && functionCall.charAt(offset[0]) != ')') {
					++offset[0];
				}
				result.append(context.formatVariableName(manager, new FieldName(functionCall.substring(beginArg, offset[0]))));
			}

		} while (functionCall.charAt(offset[0]) == ','); // While there is more args,
		// eat them.

		result.append(")");
		++offset[0];
		return result.toString();
	}
}
