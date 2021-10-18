package se.uu.ub.cora.testutils.mcr;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
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
	private static final int NUMBER_OF_CALLS_BACKWARD_TO_FIND_CALLING_METHOD = 3;
	private static final int NO_OF_PARAMETERS_FOR_ONE_RECORDED_PARAMETER = 2;
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
		recordParameterNameAndValue(parameter, parameters);
		list.add(parameter);
	}

	private void recordParameterNameAndValue(Map<String, Object> parameter, Object... parameters) {
		int position = 0;
		while (position < parameters.length) {
			parameter.put((String) parameters[position], parameters[position + 1]);
			position = position + NO_OF_PARAMETERS_FOR_ONE_RECORDED_PARAMETER;
		}
	}

	private String getMethodNameFromCall() {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		StackTraceElement stackTraceElement = stackTrace[NUMBER_OF_CALLS_BACKWARD_TO_FIND_CALLING_METHOD];
		return stackTraceElement.getMethodName();
	}

	private List<Map<String, Object>> possiblyAddMethodName(String methodName) {
		return calledMethods.computeIfAbsent(methodName, key -> new ArrayList<>());
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
		String methodName = getMethodNameFromCall();
		List<Object> list = possiblyAddMethodNameToReturnedValues(methodName);
		list.add(returnedValue);
	}

	private List<Object> possiblyAddMethodNameToReturnedValues(String methodName) {
		return returnedValues.computeIfAbsent(methodName, key -> new ArrayList<>());
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
			throw new RuntimeException("MethodName not found for (methodName: " + methodName
					+ ", callNumber: " + callNumber + ")");
		} catch (IndexOutOfBoundsException ex) {
			throw new RuntimeException("CallNumber not found for (methodName: " + methodName
					+ ", callNumber: " + callNumber + ")");
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
	public void assertReturn(String methodName, int callNumber, Object expectedValue) {

		Object value = getReturnValue(methodName, callNumber);

		assertValuesAreEqual(expectedValue, value);
	}

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
	 * getParametersForMethodAndCallNumber is used to get the values for a specific call to the
	 * specified method. The parameters are returned as a map with the parameter name as key and the
	 * parameter value as value.
	 * <p>
	 * If a request for parameters for a callNumber that is larger than the number of calls made
	 * will a runtime exception will be thrown.
	 * 
	 * @param methodName
	 *            A String with the method name
	 * @param callNumber
	 *            An int with the order number of the call, starting on 0
	 * @return a Map with with the parameter name as key and the parameter value as value.
	 */
	public Map<String, Object> getParametersForMethodAndCallNumber(String methodName,
			int callNumber) {

		String messageEnd = createNotFoundMessageForMethodNameAndCallNumber(methodName, callNumber)
				+ ")";

		return getParametersOrThrowErrorForMethodNameAndCallNumber(methodName, callNumber,
				messageEnd);

	}

	private Map<String, Object> getParametersOrThrowErrorForMethodNameAndCallNumber(
			String methodName, int callNumber, String messageEnd) {
		throwErrorIfMethodNameNotRecorded(methodName, messageEnd);
		List<Map<String, Object>> methodCalls = calledMethods.get(methodName);

		throwErrorIfCallNumberNotRecorded(callNumber, messageEnd, methodCalls);
		return methodCalls.get(callNumber);
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

		String messageEnd = createNotFoundMessageForMethodNameAndCallNumberAndParameterName(
				methodName, callNumber, parameterName);

		Map<String, Object> parameters = getParametersOrThrowErrorForMethodNameAndCallNumber(
				methodName, callNumber, messageEnd);

		throwErrorIfParameterNameNotRecorded(parameterName, messageEnd, parameters);

		return parameters.get(parameterName);

	}

	private String createNotFoundMessageForMethodNameAndCallNumberAndParameterName(
			String methodName, int callNumber, String parameterName) {
		return createNotFoundMessageForMethodNameAndCallNumber(methodName, callNumber) + ""
				+ " and parameterName: " + parameterName + ")";
	}

	private String createNotFoundMessageForMethodNameAndCallNumber(String methodName,
			int callNumber) {
		return " not found for (methodName: " + methodName + ", callNumber: " + callNumber;
	}

	private void throwErrorIfParameterNameNotRecorded(String parameterName, String messageEnd,
			Map<String, Object> parameters) {
		if (!parameters.containsKey(parameterName)) {
			throw new RuntimeException("ParameterName" + messageEnd);
		}
	}

	private void throwErrorIfCallNumberNotRecorded(int callNumber, String messageEnd,
			List<Map<String, Object>> methodCalls) {
		if (methodCalls.size() <= callNumber) {
			throw new RuntimeException("CallNumber" + messageEnd);
		}
	}

	private void throwErrorIfMethodNameNotRecorded(String methodName, String messageEnd) {
		if (!calledMethods.containsKey(methodName)) {
			throw new RuntimeException("MethodName" + messageEnd);
		}
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
		Object[] inParameters = getInParametersAsArray(methodName, callNumber);

		try {
			assertAllParameters(inParameters, expectedValues);
		} catch (ArrayIndexOutOfBoundsException e) {
			String message = "Too many values to compare for (methodName: " + methodName
					+ ", callNumber: " + callNumber + ")";
			throw new RuntimeException(message);
		}
	}

	private void assertAllParameters(Object[] inParameters, Object... expectedValues) {
		int position = 0;
		for (Object expectedValue : expectedValues) {
			assertParameterForPosition(inParameters, position, expectedValue);
			position++;
		}
	}

	private void assertParameterForPosition(Object[] inParameters, int position,
			Object expectedValue) {
		Object value = inParameters[position];
		assertValuesAreEqual(expectedValue, value);
	}

	void assertValuesAreEqual(Object expectedValue, Object actualValue) {
		if (atLeastOneValueIsNull(expectedValue, actualValue)) {
			assertEquals(actualValue, expectedValue);
		} else {
			assertNonNullValues(expectedValue, actualValue);
		}
	}

	private void assertNonNullValues(Object expectedValue, Object actualValue) {
		throwExcpetionWhenDifferentTypes(expectedValue, actualValue);
		if (isStringOrNumber(expectedValue)) {
			assertEquals(actualValue, expectedValue);
		} else {
			assertSame(actualValue, expectedValue);
		}
	}

	private boolean atLeastOneValueIsNull(Object expectedValue, Object actualValue) {
		return null == expectedValue || null == actualValue;
	}

	private void throwExcpetionWhenDifferentTypes(Object expectedValue, Object value) {
		if (differentTypes(expectedValue, value)) {
			String message = "expected value type is " + expectedValue.getClass() + " but found "
					+ value.getClass();
			throw new RuntimeException(message);
		}
	}

	private boolean differentTypes(Object objectA, Object objectB) {
		Class<? extends Object> classA = objectA.getClass();
		Class<? extends Object> classB = objectB.getClass();
		return !classA.equals(classB);
	}

	private boolean isStringOrNumber(Object assertParameter) {
		return assertParameter instanceof String || isInt(assertParameter)
				|| isLong(assertParameter);
	}

	private boolean isInt(Object object) {
		return object instanceof Integer;
	}

	private boolean isLong(Object object) {
		return object instanceof Long;
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

		assertValuesAreEqual(expectedValue, value);
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
		assertEquals(getNumberOfCallsToMethod(methodName), calledNumberOfTimes);
	}

	private Object[] getInParametersAsArray(String methodName, int callNumber) {
		return getParametersForMethodAndCallNumber(methodName, callNumber).values().toArray();
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
