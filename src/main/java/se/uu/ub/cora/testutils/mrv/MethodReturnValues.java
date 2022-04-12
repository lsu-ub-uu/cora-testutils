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

public class MethodReturnValues {
	private static final int NUMBER_OF_CALLS_BACKWARD_TO_FIND_CALLING_METHOD = 3;

	Map<String, Map<Object[], List<Object>>> valuesToReturn = new HashMap<>();
	Map<String, Map<Object[], Integer>> callsToMethodAndParameters = new HashMap<>();

	public void setReturnValues(String methodName, List<Object> returnValues,
			Object... parameterValues) {
		Map<Object[], List<Object>> valuesForParameter = new HashMap<>();
		valuesForParameter.put(parameterValues, returnValues);
		valuesToReturn.put(methodName, valuesForParameter);

		Map<Object[], Integer> numberOfCallsForParameters = new HashMap<>();
		numberOfCallsForParameters.put(parameterValues, 0);
		callsToMethodAndParameters.put(methodName, numberOfCallsForParameters);

	}

	public Object getReturnValue(Object[] values) {
		String methodNameFromCall = getMethodNameFromCall();

		if (!valuesToReturn.containsKey(methodNameFromCall)) {
			return new Object();
		}

		// if (valuesToReturn.get(methodNameFromCall).containsKey(values)) {
		// return new Object();
		// }

		Integer numberOfCalls = callsToMethodAndParameters.get(methodNameFromCall).get(values) + 1;
		callsToMethodAndParameters.get(methodNameFromCall).put(values, numberOfCalls);

		return valuesToReturn.get(methodNameFromCall).get(values).get(numberOfCalls - 1);

	}

	private String getMethodNameFromCall() {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		StackTraceElement stackTraceElement = stackTrace[NUMBER_OF_CALLS_BACKWARD_TO_FIND_CALLING_METHOD];
		return stackTraceElement.getMethodName();
	}

}
