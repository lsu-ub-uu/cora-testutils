/*
 * Copyright 2021, 2022, 2024 Uppsala University Library
 * Copyright 2024 Olov McKie
 *
 * This file is part of Cora.
 *
 *     Cora is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Cora is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Cora.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.uu.ub.cora.testutils.mcr;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import se.uu.ub.cora.testutils.mrv.MethodReturnValues;

/**
 * MethodCallRecorder is a test helper class used to record and validate calls to methods in spies
 * and similar test helping classes.
 * <p>
 * Spies and similar helper classes should create an internal public instance of this class and then
 * record calls to its methods using the {@link #addCall(Object...)} method. And record answers to
 * the calls using the {@link #addReturned(Object)}method.
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
 * <p>
 * This class is intended to be used in combination with {@link MethodReturnValues}.
 */
public class MethodCallRecorder {
	private static final String CALL_NUMBER_TEXT = ", callNumber: ";
	private static final int NUMBER_OF_CALLS_BACKWARD_TO_FIND_CALLING_METHOD = 3;
	private static final int NO_OF_PARAMETERS_FOR_ONE_RECORDED_PARAMETER = 2;
	private Map<String, List<Map<String, Object>>> calledMethods = new HashMap<>();
	private Map<String, List<Object>> returnedValues = new HashMap<>();
	private MethodReturnValues MRV;

	/**
	 * addCall is expected to be used by spies and similar test helper classes to record calls made
	 * to their methods.
	 * <p>
	 * The calling methods name (from the spy or similar) is automatically added to the provided
	 * parameters, so it can be used when later using the assert and get methods in this class.
	 * <p>
	 * If there is a connected {@link MethodReturnValues} will this method throw errors set using
	 * set errors methods in MRV.
	 * 
	 * @param parameters
	 *            An Object Varargs with the respective methods and their values. For each parameter
	 *            should first the parameter name, and then the value be added to this call <br>
	 *            Ex: addCall("parameter1Name", parameter1Value, "parameter2Name", parameter2Value )
	 * 
	 */
	public void addCall(Object... parameters) {
		String methodName = getMethodNameFromCall();
		addCallForMethodNameAndParameters(methodName, parameters);
	}

	/**
	 * addCallForMethodNameAndParameters is the same method as {@link #addCall(Object...)} but you
	 * can manually specify the method name. This method is intended to build utilitity methods such
	 * as {@link MethodCallRecorder#addCallAndReturnFromMRV(Object...)} to reduce boilerplate code
	 */
	public void addCallForMethodNameAndParameters(String methodName, Object... parameters) {
		List<Map<String, Object>> list = possiblyAddMethodName(methodName);
		Map<String, Object> parameter = new LinkedHashMap<>();
		recordParameterNameAndValue(parameter, parameters);
		list.add(parameter);
		if (null != MRV) {
			Object[] parameterValues = extractValuesFromParameters(parameters);
			MRV.possiblyThrowErrorForMethodNameAndParameters(methodName, parameterValues);
		}
	}

	private void recordParameterNameAndValue(Map<String, Object> parameter, Object... parameters) {
		int position = 0;
		while (position < parameters.length) {
			parameter.put((String) parameters[position], parameters[position + 1]);
			position = position + NO_OF_PARAMETERS_FOR_ONE_RECORDED_PARAMETER;
		}
	}

	protected String getMethodNameFromCall() {
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
		addReturnedForMethodNameAndReturnValue(methodName, returnedValue);
	}

	/**
	 * addReturnedForMethodNameAndReturnValue is the same method as {@link #addReturned(Object)} but
	 * you can manually specify the method name. This method is intended to build utilitity methods
	 * such as {@link MethodCallRecorder#addCallAndReturnFromMRV(Object...)} to reduce boilerplate
	 * code
	 */
	public void addReturnedForMethodNameAndReturnValue(String methodName, Object returnedValue) {
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
			throw new RuntimeException("MethodName not found for (methodName: %s, callNumber: %s)"
					.formatted(methodName, callNumber));
		} catch (IndexOutOfBoundsException ex) {
			throw new RuntimeException("CallNumber not found for (methodName: %s, callNumber: %s)"
					.formatted(methodName, callNumber));
		}
	}

	/**
	 * getReturnValues is used to get a list of the return values for a specific method
	 * 
	 * @param methodName
	 *            A String with the method name
	 * @return A List with Objects recorded as returned from the method in the order they where
	 *         returned
	 */
	public Collection<Object> getReturnValues(String methodName) {
		if (!returnedValues.containsKey(methodName)) {
			throw new RuntimeException(
					"MethodName not found for (methodName: %s)".formatted(methodName));
		}
		return returnedValues.get(methodName);
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
		return " not found for (methodName: " + methodName + CALL_NUMBER_TEXT + callNumber;
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
					+ CALL_NUMBER_TEXT + callNumber + ")";
			throw new RuntimeException(message);
		}
	}

	/**
	 * assertMethodCalledWithParametersReturnFirstCall is used to validate calls to spies and
	 * similar test helpers. If the specified method has been called with the specified values, is
	 * the return value for the first matching call returned as an Optional, the Optional is emtpy
	 * if the return type for the method is void.
	 * 
	 * @param methodName
	 *            A String with the methodName to check parameters for
	 * @param expectedValues
	 *            A Varargs Object with the expected parameter values in the order they are used in
	 *            the method.
	 * @return An Optional<Object> with the recorded return value
	 */
	public Optional<Object> assertMethodCalledWithParametersReturnFirstCall(String methodName,
			Object... expectedValues) {
		int position = getPositionOfFirstMatchingCallOrThrowErrorIfNone(methodName, expectedValues);
		return getReturnValueAsOptionalEmptyForVoidMethods(methodName, position);
	}

	private int getPositionOfFirstMatchingCallOrThrowErrorIfNone(String methodName,
			Object... expectedValues) {
		int size = calledMethods.get(methodName).size();
		for (int i = 0; i < size; i++) {
			try {
				assertParameters(methodName, i, expectedValues);
				return i;
			} catch (AssertionError e) {
				// Try to match next recorded call
			}
		}
		String message = "Method %s not called with values: %s".formatted(methodName,
				Arrays.toString(expectedValues));
		throw new AssertionError(message);
	}

	private Optional<Object> getReturnValueAsOptionalEmptyForVoidMethods(String methodName, int i) {
		try {
			return Optional.of(getReturnValue(methodName, i));
		} catch (Exception e) {
			return Optional.empty();
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
		throwExcepetionWhenDifferentTypes(expectedValue, actualValue);
		if (isStringOrNumber(expectedValue)) {
			assertEquals(actualValue, expectedValue);
		} else {
			assertSame(actualValue, expectedValue);
		}
	}

	private boolean atLeastOneValueIsNull(Object expectedValue, Object actualValue) {
		return null == expectedValue || null == actualValue;
	}

	private void throwExcepetionWhenDifferentTypes(Object expectedValue, Object value) {
		if (differentTypes(expectedValue, value)) {
			String message = "expected value type is %s but found %s"
					.formatted(expectedValue.getClass(), value.getClass());
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
	public void assertParameterAsEqual(String methodName, int callNumber, String parameterName,
			Object expectedValue) {
		Object value = getValueForMethodNameAndCallNumberAndParameterName(methodName, callNumber,
				parameterName);
		throwExcepetionWhenDifferentTypes(expectedValue, value);
		assertEquals(value, expectedValue);
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

	/**
	 * useMRV makes this MethodCallRecorder use the supplied MethodReturnValues, to enable the use
	 * of addCallReturnFromMRV to reduce boilerplate code in spies and similar test classes.
	 * 
	 * @param MRV
	 *            A {@link MethodReturnValues} to use to get return values from
	 */
	public void useMRV(MethodReturnValues MRV) {
		this.MRV = MRV;
	}

	/**
	 * addCallAndReturnFromMRV is a utilityMethod to reduce boilerplate code in classes that use
	 * {@link MethodCallRecorder}. It is the same as manually calling the following methods:
	 * <ol>
	 * <li>{@link MethodCallRecorder#addCall(Object...)}</li>
	 * <li>{@link MethodReturnValues#getReturnValue(Object...)}</li>
	 * <li>{@link MethodCallRecorder#addReturned(Object)}</li>
	 * <li>and then return the returnValue</li>
	 * </ol>
	 * reducing boilerplate in most spy methods to a simple call:
	 * <p>
	 * Ex: return MCR.addCallAndReturnFromMRV("parameter1",parameter1,"parameter2",parameter2);
	 * 
	 * 
	 * @param parameters
	 * @return
	 */
	public Object addCallAndReturnFromMRV(Object... parameters) {
		String methodName = getMethodNameFromCall();
		throwErrorIfNoMRV();

		addCallForMethodNameAndParameters(methodName, parameters);
		Object returnValue = getReturnValueForMethodNameAndParameters(methodName, parameters);
		addReturnedForMethodNameAndReturnValue(methodName, returnValue);

		return returnValue;
	}

	private void throwErrorIfNoMRV() {
		if (null == MRV) {
			throw new RuntimeException("Method addCallAndReturnFromMRV can not be used before "
					+ "a MVR has been set using the method useMRV");
		}
	}

	private Object getReturnValueForMethodNameAndParameters(String methodName,
			Object... parameters) {
		Object[] parameterValues = extractValuesFromParameters(parameters);
		return MRV.getReturnValueForMethodNameAndParameters(methodName, parameterValues);
	}

	private Object[] extractValuesFromParameters(Object... parameters) {
		int position = 0;
		List<Object> parameterValues = new ArrayList<>();
		while (position < parameters.length) {
			parameterValues.add(parameters[position + 1]);
			position = position + NO_OF_PARAMETERS_FOR_ONE_RECORDED_PARAMETER;
		}
		return parameterValues.toArray();
	}

	public Object onlyForTestGetMRV() {
		return MRV;
	}

}
