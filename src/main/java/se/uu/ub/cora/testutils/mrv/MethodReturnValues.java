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
import java.util.Set;

public class MethodReturnValues {
	private static final int NUMBER_OF_CALLS_BACKWARD_TO_FIND_CALLING_METHOD = 3;

	private Map<String, Map<Object[], List<Object>>> valuesToReturn = new HashMap<>();
	private Map<String, Map<Object[], Integer>> callsToMethodAndParameters = new HashMap<>();

	public void setReturnValues(String methodName, List<Object> returnValues,
			Object... parameterValues) {
		Map<Object[], List<Object>> valuesForParameter = new HashMap<>();
		valuesForParameter.put(parameterValues, returnValues);
		valuesToReturn.put(methodName, valuesForParameter);

		Map<Object[], Integer> numberOfCallsForParameters = new HashMap<>();
		numberOfCallsForParameters.put(parameterValues, 0);
		callsToMethodAndParameters.put(methodName, numberOfCallsForParameters);

	}

	public Object getReturnValue(Object... parameterValues) {
		String methodNameFromCall = getMethodNameFromCall();

		if (noStoredReturnValueForMethodAndParameters(methodNameFromCall, parameterValues)) {
			return new Object();
		}
		// TODO: need same looping check as others...
		Integer numberOfCalls = callsToMethodAndParameters.get(methodNameFromCall)
				.get(parameterValues) + 1;
		callsToMethodAndParameters.get(methodNameFromCall).put(parameterValues, numberOfCalls);

		// return valuesToReturn.get(methodNameFromCall).get(parameterValues).get(numberOfCalls -
		// 1);
		return fetchStoredReturnValueForMethodAndParameters(methodNameFromCall, parameterValues)
				.get(numberOfCalls - 1);
	}

	private String getMethodNameFromCall() {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		StackTraceElement stackTraceElement = stackTrace[NUMBER_OF_CALLS_BACKWARD_TO_FIND_CALLING_METHOD];
		return stackTraceElement.getMethodName();
	}

	private boolean noStoredReturnValueForMethodAndParameters(String methodNameFromCall,
			Object... parameterValues) {
		if (!valuesToReturn.containsKey(methodNameFromCall)) {
			return true;
		}
		// !valuesToReturn.get(methodNameFromCall).containsKey(parameterValues);
		Map<Object[], List<Object>> valuesToReturnForThisMethod = valuesToReturn
				.get(methodNameFromCall);
		Set<Object[]> candiateParameterValueSets = valuesToReturnForThisMethod.keySet();
		for (Object[] candiateParameterValues : candiateParameterValueSets) {
			boolean allTheSameValue = true;
			int no = 0;
			for (Object candiateParameterValue : candiateParameterValues) {
				if (!candiateParameterValue.equals(parameterValues[no])) {
					allTheSameValue = false;
				}
				no++;
			}
			if (allTheSameValue) {
				// match
				return false;
			}
		}
		// return !valuesToReturnForThisMethod.containsKey(parameterValues);
		return true;
	}

	private List<Object> fetchStoredReturnValueForMethodAndParameters(String methodNameFromCall,
			Object... parameterValues) {
		// if (!valuesToReturn.containsKey(methodNameFromCall)) {
		// return null;
		// }
		Map<Object[], List<Object>> valuesToReturnForThisMethod = valuesToReturn
				.get(methodNameFromCall);
		Set<Object[]> candiateParameterValueSets = valuesToReturnForThisMethod.keySet();
		for (Object[] candiateParameterValues : candiateParameterValueSets) {
			boolean allTheSameValue = true;
			int no = 0;
			for (Object candiateParameterValue : candiateParameterValues) {
				if (!candiateParameterValue.equals(parameterValues[no])) {
					allTheSameValue = false;
				}
				no++;
			}
			if (allTheSameValue) {
				// match
				return valuesToReturnForThisMethod.get(candiateParameterValues);
			}
		}
		// should never get here
		return null;
	}

}
