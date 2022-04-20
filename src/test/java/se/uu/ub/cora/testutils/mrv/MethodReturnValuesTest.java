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
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertSame;
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

		Exception caughtException = null;
		try {
			MRV.getReturnValue("one");
			assertTrue(false);
		} catch (Exception e) {
			caughtException = e;
		}
		assertNotNull(caughtException);
		assertEquals(caughtException.getMessage(),
				"No return value found for methodName: testMethodNameNotFound and parameterValues:one");
	}

	@Test
	public void testParametersNotFound() throws Exception {
		String expectedReturnValue = "otherValue";
		returnValues = List.of(expectedReturnValue);
		MRV.setReturnValues("testParametersNotFound", returnValues, parameterValues);

		Exception caughtException = null;
		try {
			MRV.getReturnValue("one", "two");
			assertTrue(false);
		} catch (Exception e) {
			caughtException = e;
		}
		assertNotNull(caughtException);
		assertEquals(caughtException.getMessage(),
				"No return value found for methodName: testParametersNotFound and parameterValues:one, two");
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

	@Test
	public void testDefaultReturnValues() throws Exception {
		MRV.setDefaultReturnValuesSupplier("testDefaultReturnValues", String::new);

		var return1 = MRV.getReturnValue();
		assertTrue(return1 instanceof String);
	}

	@Test
	public void testDefaultReturnValuesWithSpy() throws Exception {
		MRV.setDefaultReturnValuesSupplier("testDefaultReturnValuesWithSpy", DummySpy::new);

		var return1 = MRV.getReturnValue();
		var return2 = MRV.getReturnValue();
		assertTrue(return1 instanceof DummySpy);
		assertNotSame(return1, return2);
	}

	@Test
	public void testDefaultWithOneSetSpecific() throws Exception {
		Object toReturn1 = new Object();
		MRV.setReturnValues("testDefaultWithOneSetSpecific", List.of(toReturn1), "one");
		MRV.setDefaultReturnValuesSupplier("testDefaultWithOneSetSpecific", DummySpy::new);

		var return1 = MRV.getReturnValue("one");
		var return2 = MRV.getReturnValue("one");
		assertSame(return1, toReturn1);
		assertTrue(return2 instanceof DummySpy);
		assertNotSame(return1, return2);
	}

	@Test
	public void testSpecificDefaultReturnValues() throws Exception {
		MRV.setSpecificReturnValuesSupplier("testSpecificDefaultReturnValues", DummySpy::new,
				"one");

		var return1 = MRV.getReturnValue("one");
		assertTrue(return1 instanceof DummySpy);
	}

	@Test
	public void testReturnOrder() throws Exception {
		Object toReturn1 = new Object();
		MRV.setReturnValues("testReturnOrder", List.of(toReturn1), "one");
		MRV.setSpecificReturnValuesSupplier("testReturnOrder", DummySpy::new, "one");
		MRV.setDefaultReturnValuesSupplier("testReturnOrder", String::new);

		var return1 = MRV.getReturnValue("one");
		var return2 = MRV.getReturnValue("one");
		var return3 = MRV.getReturnValue("two");
		assertSame(return1, toReturn1);
		assertTrue(return2 instanceof DummySpy);
		assertTrue(return3 instanceof String);
	}

	@Test
	public void testThrowException() throws Exception {
		RuntimeException returnException = new RuntimeException();
		MRV.setThrowException("testThrowException", returnException, "one");
		Exception caughtException = null;
		try {
			MRV.getReturnValue("one");
			assertTrue(false);
		} catch (Exception e) {
			caughtException = e;
		}
		assertNotNull(caughtException);
		assertSame(caughtException, returnException);
	}

	@Test
	public void testThrowOrder() throws Exception {
		Object toReturn1 = new Object();
		Object toReturn2 = new Object();
		MRV.setReturnValues("testThrowOrder", List.of(toReturn1), "one");
		MRV.setSpecificReturnValuesSupplier("testThrowOrder", DummySpy::new, "one");
		MRV.setReturnValues("testThrowOrder", List.of(toReturn1), "one");

		MRV.setReturnValues("testThrowOrder", List.of(toReturn2), "two");
		MRV.setThrowException("testThrowOrder", new RuntimeException(), "two");
		MRV.setDefaultReturnValuesSupplier("testThrowOrder", String::new);

		MRV.getReturnValue("one");
		MRV.getReturnValue("one");
		MRV.getReturnValue("one");

		MRV.getReturnValue("two");
		Exception caughtException = null;
		try {
			MRV.getReturnValue("two");
			assertTrue(false);
		} catch (Exception e) {
			caughtException = e;
		}
		assertNotNull(caughtException);
	}
	// -make it possible to set error to throw
	// -make it possible to set default for some value
	// -see if we can set a MVR in MCR, to reduce boilerplate code
	// -think about if MCR could combine addCall and addReturn, with MVR to reduce boilerplate
	// further
	// -possibly get a value with type to get to make MRV do the casting...

	// MRV.setReturnValue("containsChildWithNameInData", List.of(true), "paramAValue",
	// "paramBValue");
	// MRV.setReturnValue("containsChildWithNameInData", List.of(true), "plainTextPassword");

	// MethodMockMeNow
	// MethodReturnValues
	// var returnValue = MRV.getReturnValue(nameInData);

}
