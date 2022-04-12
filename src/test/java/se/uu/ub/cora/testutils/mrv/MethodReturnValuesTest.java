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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.util.List;

import org.testng.annotations.Test;

public class MethodReturnValuesTest {

	String methodName = "testName";
	List<Object> returnValues;
	Object[] parameterValues;
	Object[] otherParameters;

	@Test
	public void testReturnValueForThisMethod() throws Exception {
		MethodReturnValues MRV = new MethodReturnValues();
		String expectedReturnValue = "returnValue";
		returnValues = List.of(expectedReturnValue);
		MRV.setReturnValues("testReturnValueForThisMethod", returnValues, parameterValues);

		Object returnValue = MRV.getReturnValue(parameterValues);

		assertEquals(returnValue, expectedReturnValue);

	}

	@Test
	public void testReturnOtherValueForThisMethod() throws Exception {
		MethodReturnValues MRV = new MethodReturnValues();
		String expectedReturnValue = "otherValue";
		returnValues = List.of(expectedReturnValue);
		MRV.setReturnValues("testReturnOtherValueForThisMethod", returnValues, parameterValues);

		Object returnValue = MRV.getReturnValue(parameterValues);

		assertEquals(returnValue, expectedReturnValue);

	}

	@Test
	public void testReturnSeveralValueForThisMethod() throws Exception {
		MethodReturnValues MRV = new MethodReturnValues();
		returnValues = List.of("firstValue", "secondValue", "thirdValue");
		// String expectedReturnValue = "otherValue";
		MRV.setReturnValues("testReturnSeveralValueForThisMethod", returnValues, parameterValues);

		Object firstValue = MRV.getReturnValue(parameterValues);
		Object secondValue = MRV.getReturnValue(parameterValues);
		Object thirdValue = MRV.getReturnValue(parameterValues);

		assertEquals(firstValue, "firstValue");
		assertEquals(secondValue, "secondValue");
		assertEquals(thirdValue, "thirdValue");

	}

	@Test
	public void testMethodNameNotFound() throws Exception {
		MethodReturnValues MRV = new MethodReturnValues();
		String expectedReturnValue = "otherValue";
		returnValues = List.of(expectedReturnValue);
		MRV.setReturnValues("methodThatDoesNotExist", returnValues, parameterValues);

		Object value = MRV.getReturnValue(parameterValues);

		assertNotNull(value);
	}

	@Test
	public void testParametersNotFound() throws Exception {
		MethodReturnValues MRV = new MethodReturnValues();
		String expectedReturnValue = "otherValue";
		returnValues = List.of(expectedReturnValue);
		MRV.setReturnValues("testParametersNotFound", returnValues, parameterValues);

		Object value = MRV.getReturnValue(otherParameters);

		assertNotNull(value);
	}
	// MRV.setReturnValue("containsChildWithNameInData", List.of(true), "paramAValue",
	// "paramBValue");
	// MRV.setReturnValue("containsChildWithNameInData", List.of(true), "plainTextPassword");

	// MethodMockMeNow
	// MethodReturnValues
	// var returnValue = MRV.getReturnValue(nameInData);

}
