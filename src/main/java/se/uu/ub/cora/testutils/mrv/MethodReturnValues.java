/*
 * Copyright 2022 Uppsala University Library
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

package se.uu.ub.cora.testutils.mrv;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;

/**
 * MethodReturnValues is a test helper class used to set return values to methods in spies and
 * similar test helping classes.
 * <p>
 * Spies and similar helper classes should create an internal public instance of this class and use
 * that instance to get return values for its methods using the {@link #getReturnValue(Object...)}
 * method.
 * <p>
 * Tests can then set return values for methods of the spy using the
 * {@link #setReturnValues(String, List, Object...)} method.
 * <p>
 * This class is intended to be used in combination with {@link MethodCallRecorder}.
 */
public class MethodReturnValues {
	private static final int NUMBER_OF_CALLS_BACKWARD_TO_FIND_CALLING_METHOD = 3;
	private Map<NameValues, List<Object>> valuesToReturn = new HashMap<>();
	private Map<NameValues, Integer> callsToMethodAndParameters = new HashMap<>();

	/**
	 * setReturnValues is expected to be used by tests to set desired return values for spies and
	 * similar test helper classes.
	 * <p>
	 * Values set by this method can later be fetched by using the
	 * {@link #getReturnValue(Object...)} method. Matching of which value to returned is done based
	 * on the set methodName and parameter values.
	 * 
	 * @param methodName
	 *            A String with the method name
	 * @param returnValues
	 *            a List of Object to use as return values, where the value returned will start from
	 *            the first and then continue down the list for each following request to
	 *            getReturnValue
	 * @param parameterValues
	 *            An Object Varargs with the methods values.
	 * 
	 */
	public void setReturnValues(String methodName, List<Object> returnValues,
			Object... parameterValues) {
		NameValues nameValues = new NameValues(methodName, parameterValues);

		valuesToReturn.put(nameValues, returnValues);
		callsToMethodAndParameters.put(nameValues, 0);
	}

	/**
	 * getReturnValue is expected to be used by spies and similar test helper classes to get return
	 * values to use for their methods.
	 * <p>
	 * It is expected that calls to this method is done from the spy method that is returning a
	 * value. The value returned is from values set by the
	 * {@link #setReturnValues(String, List, Object...)}method. The methods name is automatically
	 * collected from the calling method, so that only the methods parameterValues needs to be sent
	 * when calling this method.
	 * 
	 * @param parameterValues
	 *            An Object Varargs with the methods values.<br>
	 *            Ex: getReturnValue(parameter1Value, paramter2Value)
	 * @return An Object with the previously stored return value. If no return value is set for the
	 *         combination of this methods name and parameterValues, is an empty Object returned
	 *         instead.
	 */
	public Object getReturnValue(Object... parameterValues) {
		String methodName = getMethodNameFromCall();
		NameValues nameValues = new NameValues(methodName, parameterValues);
		if (noReturnValuesExistForCall(nameValues)) {
			return new Object();
		}
		Integer numberOfCalls = callsToMethodAndParameters.get(nameValues);
		callsToMethodAndParameters.put(nameValues, numberOfCalls + 1);
		return valuesToReturn.get(nameValues).get(numberOfCalls);
	}

	private String getMethodNameFromCall() {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		StackTraceElement stackTraceElement = stackTrace[NUMBER_OF_CALLS_BACKWARD_TO_FIND_CALLING_METHOD];
		return stackTraceElement.getMethodName();
	}

	private boolean noReturnValuesExistForCall(NameValues nameValues) {
		return !valuesToReturn.containsKey(nameValues);
	}
}
