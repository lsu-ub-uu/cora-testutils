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
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MethodReturnValuesTest {

	String methodName = "testName";
	List<Object> returnValues;
	Object[] parameterValues = { new Object() };
	Object[] otherParameters = { new Object() };
	private MethodReturnValues MRV;

	@BeforeMethod
	public void beforeMethod() {
		MRV = new MethodReturnValues();
	}

	@Test
	public void testReturnValueForThisMethod() throws Exception {
		String expectedReturnValue = "returnValue";
		returnValues = List.of(expectedReturnValue);
		MRV.setReturnValues("testReturnValueForThisMethod", returnValues, parameterValues);

		Object returnValue = MRV.getReturnValue(parameterValues);

		assertEquals(returnValue, expectedReturnValue);

	}

	@Test
	public void testReturnOtherValueForThisMethod() throws Exception {
		String expectedReturnValue = "otherValue";
		returnValues = List.of(expectedReturnValue);
		MRV.setReturnValues("testReturnOtherValueForThisMethod", returnValues, parameterValues);

		Object returnValue = MRV.getReturnValue(parameterValues);

		assertEquals(returnValue, expectedReturnValue);

	}

	@Test
	public void testReturnSeveralValueForThisMethod() throws Exception {
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
		String expectedReturnValue = "otherValue";
		returnValues = List.of(expectedReturnValue);
		MRV.setReturnValues("methodThatDoesNotExist", returnValues, parameterValues);

		Object returnValue = MRV.getReturnValue(parameterValues);

		assertNotNull(returnValue);
	}

	@Test
	public void testParametersNotFound() throws Exception {
		String expectedReturnValue = "otherValue";
		returnValues = List.of(expectedReturnValue);
		MRV.setReturnValues("testParametersNotFound", returnValues, parameterValues);

		Object returnValue = MRV.getReturnValue(otherParameters);

		assertNotNull(returnValue);
	}

	@Test
	public void testStringValues() throws Exception {
		MRV.setReturnValues("testStringValues", List.of(1, 2, 3), "one", "two");

		var return1 = MRV.getReturnValue("one", "two");
		var return2 = MRV.getReturnValue("one", "two");
		var return3 = MRV.getReturnValue("one", "two");

		assertEquals(return1, 1);
		assertEquals(return2, 2);
		assertEquals(return3, 3);
	}

	@Test
	public void testIntValues() throws Exception {
		MRV.setReturnValues("testIntValues", List.of(1, 2, 3), 1, 2);

		var return1 = MRV.getReturnValue(1, 2);
		var return2 = MRV.getReturnValue(1, 2);
		var return3 = MRV.getReturnValue(1, 2);

		assertEquals(return1, 1);
		assertEquals(return2, 2);
		assertEquals(return3, 3);
	}

	@Test
	public void testBoolanReturnValues() throws Exception {
		MRV.setReturnValues("testBoolanReturnValues", List.of(true, false, true), 1);

		boolean return1 = (boolean) MRV.getReturnValue(1);
		boolean return2 = (boolean) MRV.getReturnValue(1);
		boolean return3 = (boolean) MRV.getReturnValue(1);

		assertTrue(return1);
		assertFalse(return2);
		assertTrue(return3);
	}

	@Test
	public void testNoParameterValues() throws Exception {
		MRV.setReturnValues("testNoParameterValues", List.of(1, 2, 3));

		var return1 = MRV.getReturnValue();
		assertEquals(return1, 1);
	}

	// @Test
	// public void testNameValues() throws Exception {
	// MRV.setReturnValues("testNameValues", List.of(1, 2, 3, 4, 5, 6, 7, 8, 9), "one", "two",
	// "three");
	//
	// var return1 = MRV.getReturnValue("two", "one");
	// assertEquals(return1, 1);
	// }

	@Test
	public void testDifferentReturnValuesForDifferentParameterValues() throws Exception {
		MRV.setReturnValues("testDifferentReturnValuesForDifferentParameterValues", List.of("one"),
				"one");
		MRV.setReturnValues("testDifferentReturnValuesForDifferentParameterValues", List.of("two"),
				"two");

		var return1 = MRV.getReturnValue("one");
		var return2 = MRV.getReturnValue("two");

		assertEquals(return1, "one");
		assertEquals(return2, "two");
	}
	// MRV.setReturnValue("containsChildWithNameInData", List.of(true), "paramAValue",
	// "paramBValue");
	// MRV.setReturnValue("containsChildWithNameInData", List.of(true), "plainTextPassword");

	// MethodMockMeNow
	// MethodReturnValues
	// var returnValue = MRV.getReturnValue(nameInData);

}
