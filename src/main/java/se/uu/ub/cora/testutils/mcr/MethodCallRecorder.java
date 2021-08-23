package se.uu.ub.cora.testutils.mcr;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MethodCallRecorder is a test helper class used to record and validate calls to methods in spies
 * and similar test helping classes.
 * <p>
 * Spies and similar helper classes should create an internal instance of this class and then record
 * calls to its methods using the {@link #addCall(Object...)} method. And record answers to the
 * calls using the {@link #addReturned(Object)}method.
 * <p>
 * Tests can then validate that correct calls have been made using the
 * {@link #assertParameters(String, int, Object...)} method or the
 * {@link #assertParameter(String, int, String, Object)} method and check the number of calls using
 * the {@link #assertNumberOfCallsToMethod(String, int)} or that a method has been called using the
 * {@link #assertMethodWasCalled(String)} method.
 * <p>
 * Returned values can be accessed using the {@link #getReturnValue(String, int)} method.
 * <p>
 * Or get values for a specific call using the {@link #getInParametersAsArray(String, int)} method
 * to use in external asserts and check the number of calls using the
 * {@link #getNumberOfCallsToMethod(String)} method or that a method has been called using the
 * {@link #methodWasCalled(String)} method.
 */
public class MethodCallRecorder {
	private Map<String, List<Map<String, Object>>> calledMethods = new HashMap<>();
	private Map<String, List<Object>> returnedValues = new HashMap<>();

	/**
	 * addCall is expected to be used by spies and similar test helper classes to record calls made
	 * to their methods.
	 * <p>
	 * The calling methods name (from the spy or similar) is automatically added to the provided
	 * parameters, so it can be used when later using the assert and get methods in this class.
	 * 
	 * @param parameters
	 *            An Object Varargs with the respective methods and their values. For each parameter
	 *            should first the parameter name, and then the value be added to this call <br>
	 *            Ex: addCall("parameter1Name", parameter1Value, "parameter2Name", parameter2Value )
	 * 
	 */
	public void addCall(Object... parameters) {
		String methodName = getMethodNameFromCall();
		List<Map<String, Object>> list = possiblyAddMethodName(methodName);
		Map<String, Object> parameter = new LinkedHashMap<>();
		int position = 0;
		while (position < parameters.length) {
			parameter.put((String) parameters[position], parameters[position + 1]);
			position = position + 2;
		}
		list.add(parameter);
	}

	private String getMethodNameFromCall() {
		int numberOfCallsBackwardToFindCallingMethod = 3;
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		StackTraceElement stackTraceElement = stackTrace[numberOfCallsBackwardToFindCallingMethod];
		return stackTraceElement.getMethodName();
	}

	private List<Map<String, Object>> possiblyAddMethodName(String methodName) {
		return calledMethods.computeIfAbsent(methodName, key -> {
			return new ArrayList<>();
		});
	}

	/**
	 * addReturned is expected to be used by spies and similar test helper classes to record return
	 * values sent from their methods.
	 * <p>
	 * The calling methods name (from the spy or similar) is automatically added to the provided
	 * parameters, so it can be used when later using the assert and get methods in this class.
	 * 
	 * @param returnedValue
	 *            The value returned from the method
	 */
	public void addReturned(Object returnedValue) {
		String methodName2 = getMethodNameFromCall();
		List<Object> list = possiblyAddMethodNameToReturnedValues(methodName2);
		list.add(returnedValue);
	}

	private List<Object> possiblyAddMethodNameToReturnedValues(String methodName) {
		if (!returnedValues.containsKey(methodName)) {
			returnedValues.put(methodName, new ArrayList<>());
		}
		return returnedValues.get(methodName);
	}

	/**
	 * getReturnValue is used to get the return value for a specific method call and call number
	 * 
	 * @param methodName
	 *            A String with the method name
	 * @param callNumber
	 *            An int with the order number of the call, starting on 0
	 * @return An Object with the recorded return value
	 */
	public Object getReturnValue(String methodName, int callNumber) {
		try {
			List<Object> returnedValuesForMethod = returnedValues.get(methodName);
			return returnedValuesForMethod.get(callNumber);
		} catch (NullPointerException ex) {
			throw new RuntimeException(
					"MethodName not found for (methodName: someMethod, callNumber: 0)");
		}
	}

	/**
	 * assertReturn is used to validate calls to spies and similar test helpers.
	 * <p>
	 * Strings and Ints are compared using assertEquals
	 * <p>
	 * All other types are compared using assertSame
	 * 
	 * @param methodName
	 *            A String with the methodName to check parameters for
	 * @param callNumber
	 *            An int with the order number of the call, starting on 0
	 * @param expectedValue
	 *            An Object with the expected parameter value
	 */
	// public void assertReturn(String methodName, int callNumber, Object expectedValue) {
	//
	// Object value = getReturnValue(methodName, callNumber);
	//
	// assertParameter(expectedValue, value);
	// }

	/**
	 * getNumberOfCallsToMethod is used to get the number of calls made to a method
	 * 
	 * @param methodName
	 *            A String with the method name
	 * @return An int with the number of calls made
	 */
	public int getNumberOfCallsToMethod(String methodName) {
		if (null == calledMethods.get(methodName)) {
			return 0;
		}
		return calledMethods.get(methodName).size();
	}

	/**
	 * getValueForMethodNameAndCallNumberAndParameterName is used to get the value for a specific
	 * method calls specified parameter
	 * <p>
	 * If no value is recorded for the specified method, callNumber and parameterName will a runtime
	 * exception be thrown.
	 * 
	 * @param methodName
	 *            A String with the method name
	 * @param callNumber
	 *            An int with the order number of the call, starting on 0
	 * @param parameterName
	 *            A String with the parameter name to get the value for
	 * @return An Object with the recorded value
	 */
	public Object getValueForMethodNameAndCallNumberAndParameterName(String methodName,
			int callNumber, String parameterName) {

		// Original solution
		// List<Map<String, Object>> methodCalls = calledMethods.get(methodName);
		// Map<String, Object> parameters = methodCalls.get(callNumber);
		// return parameters.get(parameterName);

		// Another solution
		String errorMessageStartsWith = "MethodName";
		if (calledMethods.containsKey(methodName)) {
			List<Map<String, Object>> methodCalls = calledMethods.get(methodName);

			errorMessageStartsWith = "CallNumber";
			if (methodCalls.size() > callNumber) {
				Map<String, Object> parameters = methodCalls.get(callNumber);

				errorMessageStartsWith = "ParameterName";
				if (parameters.containsKey(parameterName)) {
					return parameters.get(parameterName);
				}
			}
		}
		throw new RuntimeException(errorMessageStartsWith + " not found for (methodName: "
				+ methodName + ", callNumber: " + callNumber + "" + " and parameterName: "
				+ parameterName + ")");

	}

	/**
	 * methodWasCalled returns if a method has been called or not
	 * 
	 * @param methodName
	 *            A String with the methodName to get if it has been called or not
	 * @return A boolean, true if the method has been called else false
	 */
	public boolean methodWasCalled(String methodName) {
		return calledMethods.containsKey(methodName);
	}

	/**
	 * getParametersForMethodAndCallNumber is used to get the values for a specific call to the
	 * specified method. The parameters are returned as a map with the parameter name as key and the
	 * parameter value as value.
	 * <p>
	 * If a request for parameters for a callNumber that is larger than the number of calls made
	 * will an empty map be returned.
	 * 
	 * @param methodName
	 *            A String with the method name
	 * @param callNumber
	 *            An int with the order number of the call, starting on 0
	 * @return a Map with with the parameter name as key and the parameter value as value.
	 */
	public Map<String, Object> getParametersForMethodAndCallNumber(String methodName,
			int callNumber) {
		List<Map<String, Object>> methodCalls = calledMethods.get(methodName);
		if (null != methodCalls && methodCalls.size() >= callNumber) {
			return methodCalls.get(callNumber);
		}
		return Collections.emptyMap();
	}

	/**
	 * assertParameters is used to validate calls to spies and similar test helpers.
	 * <p>
	 * Strings and Ints are compared using assertEquals
	 * <p>
	 * All other types are compared using assertSame
	 * 
	 * @param methodName
	 *            A String with the methodName to check parameters for
	 * @param callNumber
	 *            An int with the order number of the call, starting on 0
	 * @param expectedValues
	 *            A Varargs Object with the expected parameter values in the order they are used in
	 *            the method.
	 */
	public void assertParameters(String methodName, int callNumber, Object... expectedValues) {
		try {
			Object[] inParameters = getInParametersAsArray(methodName, callNumber);

			int position = 0;
			for (Object expectedValue : expectedValues) {
				Object value = inParameters[position];
				assertParameter(expectedValue, value);
				position++;
			}
		} catch (Exception e) {
			assertTrue(false);
		}
	}

	void assertParameter(Object expectedValue, Object actualValue) {
		throwExcpetionWhenDifferentTypes(expectedValue, actualValue);
		if (isStringOrInt(expectedValue)) {
			assertEquals(actualValue, expectedValue);
		} else {
			assertSame(actualValue, expectedValue);
		}
	}

	private void throwExcpetionWhenDifferentTypes(Object expectedValue, Object value) {
		if (differentTypes(expectedValue, value)) {
			String message = "expected value type is " + expectedValue.getClass() + " but found "
					+ value.getClass();
			throw new RuntimeException(message);
		}
	}

	private boolean differentTypes(Object objectA, Object objectB) {
		return !objectA.getClass().equals(objectB.getClass());
	}

	/**
	 * assertParameter is used to validate calls to spies and similar test helpers.
	 * 
	 * @param methodName
	 *            A String with the methodName to check parameters for
	 * @param callNumber
	 *            An int with the order number of the call, starting on 0
	 * @param parameterName
	 *            A String with the parameter name to check the value of
	 * @param expectedValue
	 *            An Object with the expected parameter value
	 */
	public void assertParameter(String methodName, int callNumber, String parameterName,
			Object expectedValue) {

		Object value = getValueForMethodNameAndCallNumberAndParameterName(methodName, callNumber,
				parameterName);

		assertParameter(expectedValue, value);
	}

	/**
	 * assertNumberOfCallsToMethod asserts the number of times a method has been called.
	 * 
	 * @param methodName
	 *            Name of the method to assert
	 * @param calledNumberOfTimes
	 *            Expected number of times that the method has been called.
	 */
	public void assertNumberOfCallsToMethod(String methodName, int calledNumberOfTimes) {
		// coraAssert.assertEquals(getNumberOfCallsToMethod(methodName), calledNumberOfTimes);
		assertEquals(getNumberOfCallsToMethod(methodName), calledNumberOfTimes);
	}

	private Object[] getInParametersAsArray(String methodName, int callNumber) {
		Object[] inParameters = getParametersForMethodAndCallNumber(methodName, callNumber).values()
				.toArray();
		return inParameters;
	}

	private boolean isStringOrInt(Object assertParameter) {
		return assertParameter instanceof String || isInt(assertParameter);
	}

	private boolean isInt(Object object) {
		try {
			Integer.parseInt((String) object);
			return true;
		} catch (Exception e) {
			return false;
		}

	}

	/**
	 * assertMethodWasCalled is used to assert that a method has been called
	 * 
	 * @param methodName
	 *            A String with the methodName to assert that it has been called
	 */
	public void assertMethodWasCalled(String methodName) {
		assertTrue(methodWasCalled(methodName));
	}

	/**
	 * assertMethodNotCalled is used to assert that a method has NOT been called
	 * 
	 * @param methodName
	 *            A String with the methodName to assert that it has NOT been called
	 */
	public void assertMethodNotCalled(String methodName) {
		assertFalse(methodWasCalled(methodName));
	}

}
