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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;

/**
 * MethodReturnValues is a test helper class used to set return values to methods in spies and
 * similar test helping classes.
 * <p>
 * Spies and similar helper classes should create an internal public instance of this class and use
 * that instance to get return values for its methods using the {@link #getReturnValue(Object...)}
 * method. A spy normaly sets default return values for all methods using the
 * {@link #setDefaultReturnValuesSupplier(String, Supplier)} method.
 * <p>
 * Tests can then set specific return values for methods of the spy using the
 * {@link #setReturnValues(String, List, Object...)},
 * {@link #setSpecificReturnValuesSupplier(String, Supplier, Object...)}
 * <p>
 * This class is intended to be used in combination with {@link MethodCallRecorder}.
 */
public class MethodReturnValues {
	private static final int NUMBER_OF_CALLS_BACKWARD_TO_FIND_CALLING_METHOD = 3;
	private Map<NameValues, List<Object>> valuesToReturn = new HashMap<>();
	private Map<NameValues, Integer> noOfReturnedNameValues = new HashMap<>();
	private Map<NameValues, Supplier<?>> specificReturnSuppliers = new HashMap<>();
	private Map<String, Supplier<?>> defaultReturnSuppliers = new HashMap<>();
	private Map<NameValues, RuntimeException> exceptionToThrow = new HashMap<>();

	/**
	 * setReturnValues is expected to be used by tests to set desired return values for spies and
	 * similar test helper classes.
	 * <p>
	 * Values set by this method can later be fetched by using the
	 * {@link #getReturnValue(Object...)} method. Matching of which value to returned is done based
	 * on the set methodName and parameter values.
	 * <p>
	 * Ex: MRV.setReturnValues("methodName", List.of("return1", "return2"), "parameterValue")
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
		noOfReturnedNameValues.put(nameValues, 0);
	}

	/**
	 * setThrowException is expected to be used by tests to set a desired execption to throw for
	 * spies and similar test helper classes. *
	 * <p>
	 * Exceptions set by this method will be thrown when trying to fetch a value, by using the
	 * {@link #getReturnValue(Object...)} method. Matching of which error to throw is done based on
	 * the set methodName and parameter values.
	 * <p>
	 * Ex: MRV.setThrowException("methodName", new RuntimeException(), "parameter1Value")
	 * 
	 * @param methodName
	 *            A String with the method name
	 * @param returnException
	 *            A RuntimeException to throw
	 * @param parameterValues
	 *            An Object Varargs with the methods values.
	 */
	public void setThrowException(String methodName, RuntimeException returnException,
			Object... parameterValues) {
		NameValues nameValues = new NameValues(methodName, parameterValues);
		exceptionToThrow.put(nameValues, returnException);
	}

	/**
	 * getReturnValue is expected to be used by spies and similar test helper classes to get return
	 * values to use for their methods.
	 * <p>
	 * It is expected that calls to this method is done from the spy method that is returning a
	 * value. The value returned is from values set by the
	 * {@link #setReturnValues(String, List, Object...)} method. The methods name is automatically
	 * collected from the calling method, so that only the methods parameterValues needs to be sent
	 * when calling this method.
	 * <p>
	 * Values/errors are returned from those set in the following order:
	 * <ol>
	 * <li>Return values set with {@link #setReturnValues(String, List, Object...)} as long as not
	 * all values from the list have been returned</li>
	 * <li>Return values from supplier set with
	 * {@link #setSpecificReturnValuesSupplier(String, Supplier, Object...)}</li>
	 * <li>Error to thrown set with {@link #setThrowException(String, RuntimeException, Object...)}
	 * </li>
	 * <li>Return values from supplier set with
	 * {@link #setDefaultReturnValuesSupplier(String, Supplier)}</li>
	 * <li>As a last resort is a runtime error thrown explaingin that nothing can be found to return
	 * for the specified method and parameter values.</li>
	 * </ol>
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
		return getReturnValueForMethodNameAndParameters(methodName, parameterValues);
	}

	/**
	 * getReturnValueForMethodNameAndParameters is the same method as
	 * {@link #getMethodNameFromCall()} but you can manually specify the method name. This method is
	 * intended to build utilitity methods such as
	 * {@link MethodCallRecorder#addCallAndReturnFromMRV(Object...)} to reduce boilerplate code
	 */
	public Object getReturnValueForMethodNameAndParameters(String methodName,
			Object... parameterValues) {
		NameValues nameValues = new NameValues(methodName, parameterValues);
		if (specificNotUsedReturnValuesExist(nameValues)) {
			Integer numberOfCalls = noOfReturnedNameValues.get(nameValues);
			noOfReturnedNameValues.put(nameValues, numberOfCalls + 1);
			return valuesToReturn.get(nameValues).get(numberOfCalls);
		}
		if (specificReturnSuppliers.containsKey(nameValues)) {
			return specificReturnSuppliers.get(nameValues).get();
		}
		if (exceptionToThrow.containsKey(nameValues)) {
			throw exceptionToThrow.get(nameValues);
		}
		if (defaultReturnSuppliers.containsKey(methodName)) {
			return defaultReturnSuppliers.get(methodName).get();
		}
		List<String> par = createListFromValues(parameterValues);
		throw new RuntimeException("No return value found for methodName: " + methodName
				+ " and parameterValues:" + String.join(", ", par));
	}

	private String getMethodNameFromCall() {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		StackTraceElement stackTraceElement = stackTrace[NUMBER_OF_CALLS_BACKWARD_TO_FIND_CALLING_METHOD];
		return stackTraceElement.getMethodName();
	}

	private boolean specificNotUsedReturnValuesExist(NameValues nameValues) {
		if (returnValuesExistForCall(nameValues)) {
			Integer numberOfCalls = noOfReturnedNameValues.get(nameValues);
			return valuesToReturn.get(nameValues).size() > numberOfCalls;
		}
		return false;
	}

	private boolean returnValuesExistForCall(NameValues nameValues) {
		return valuesToReturn.containsKey(nameValues);
	}

	private List<String> createListFromValues(Object... parameterValues) {
		List<String> par = new ArrayList<>();
		for (Object object : parameterValues) {
			par.add(object.toString());
		}
		return par;
	}

	/**
	 * setSpecificReturnValuesSupplier is expected to be used by tests, to set a Supplier for return
	 * values for specified parameterValues in spies and similar test helper classes.
	 * <p>
	 * Values set by this method can later be fetched by using the
	 * {@link #getReturnValue(Object...)} method. Matching of which value to returned is done based
	 * on the set methodName and parameter values.
	 * <p>
	 * Ex: MRV.setSpecificReturnValuesSupplier("methodName", mySpy::new, "parameterValue")
	 * 
	 * @param methodName
	 *            A String with the method name
	 * @param supplier
	 *            A Supplier that can supply instances to return
	 * @param parameterValues
	 *            An Object Varargs with the methods values.
	 */
	public void setSpecificReturnValuesSupplier(String methodName, Supplier<?> supplier,
			Object... parameterValues) {
		NameValues nameValues = new NameValues(methodName, parameterValues);
		specificReturnSuppliers.put(nameValues, supplier);
	}

	/**
	 * setDefaultReturnValuesSupplier is expected to be used by tests, to set a default Supplier for
	 * return values in spies and similar test helper classes.
	 * <p>
	 * Values set by this method can later be fetched by using the
	 * {@link #getReturnValue(Object...)} method. Matching of which value to returned is done based
	 * on the set methodName.
	 * <p>
	 * Ex: MRV.setDefaultReturnValuesSupplier("methodName", mySpy::new)
	 * 
	 * @param methodName
	 *            A String with the method name
	 * @param supplier
	 *            A Supplier that can supply instances to return
	 */
	public void setDefaultReturnValuesSupplier(String methodName, Supplier<?> supplier) {
		defaultReturnSuppliers.put(methodName, supplier);
	}

}
