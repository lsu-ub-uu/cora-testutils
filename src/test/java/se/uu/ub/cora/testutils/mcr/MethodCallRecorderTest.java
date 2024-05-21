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
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.testutils.mrv.MethodReturnValues;

public class MethodCallRecorderTest {

	private static final String ADD_CALL = "addCall";
	private static final String ADD_CALL1 = "addCall1";
	private static final String ADD_CALL2 = "addCall2";
	private static final String ADD_CALL3 = "addCall3";
	private static final String ADD_CALL4 = "addCall4";

	private static final String USE_MRV = "useMRV";
	private static final String RETURN1 = "return1";
	private static final String ITEM1 = "item1";
	private static final String ITEM2 = "item2";
	private static final String ONE = "one";
	private static final String ONLY_ONE = "onlyOne";

	private static final String PARAM1 = "param1";
	private static final String PARAM2 = "param2";
	private static final String PARAM3 = "param3";
	private static final String SOME_PARAMETER_NAME = "someParameterName";
	private static final String PARAMETER_NAME = "parameterName";
	private static final String PARAM_NAME = "paramName";
	private static final String PARAM_NAME2 = "paramName2";

	private static final String EXPECTED_VALUE = "expectedValue";
	private static final String SOME_VALUE = "someValue";
	private static final String SOME_VALUE1 = "someValue1";
	private static final String SOME_VALUE2 = "someValue2";
	private static final String SOME_VALUE3 = "someValue3";
	private static final String SOME_OTHER_VALUE = "someOtherValue";
	private static final String PARAM_VALUE1 = "paramValue1";
	private static final String PARAM_VALUE2 = "paramValue2";
	private static final String VALUE1 = "value1";
	private static final String VALUE2 = "value2";
	private static final String VALUE3 = "value3";
	private static final String SOME_METHOD = "someMethod";
	private static final long A_LONG_TO_BIG_FOR_INT = 3147483647L;
	MethodCallRecorder MCR;
	private Object objectParameter = new Object();
	private MethodCallRecorderOnlyForTestAssertValues MCRforTestAV;
	private MethodCallRecorderOnlyForTestAssertCalledParameters MCRforTestACP;

	@BeforeMethod
	public void beforeMethod() {
		MCR = new MethodCallRecorder();
		MCRforTestAV = new MethodCallRecorderOnlyForTestAssertValues();
		MCRforTestACP = new MethodCallRecorderOnlyForTestAssertCalledParameters();
	}

	@Test
	public void testMethodWasCalledDoesNotExist() throws Exception {
		assertFalse(MCR.methodWasCalled("MethodDoesNotExist"));
	}

	@Test
	public void testAddCallNoParameters() throws Exception {
		MCR.addCall();

		assertTrue(MCR.methodWasCalled("testAddCallNoParameters"));
	}

	@Test
	public void testAddCallNoParametersTwoCalls() throws Exception {
		addCall1();
		addCall2();

		assertTrue(MCR.methodWasCalled(ADD_CALL1));
		assertTrue(MCR.methodWasCalled(ADD_CALL2));
		assertFalse(MCR.methodWasCalled(ADD_CALL3));
	}

	private void addCall1() {
		MCR.addCall(PARAM1, 1, PARAM2, "2");
	}

	private void addCall2() {
		MCR.addCall(PARAM2, 2, PARAM3, objectParameter);
	}

	private void addCall3() {
		MCR.addCall(PARAM1, 1L, PARAM2, 3147483647L);
	}

	private void addCall4() {
		MCR.addCall(PARAM1, ONLY_ONE);
	}

	@Test
	public void testAddCallWithError() throws Exception {
		MethodReturnValues MRV = new MethodReturnValues();
		RuntimeException exception = new RuntimeException();
		MRV.setAlwaysThrowException("testAddCallWithError", exception);

		MCR.useMRV(MRV);

		try {
			MCR.addCall();
			fail("An exception should have been thrown");
		} catch (Exception e) {
			assertSame(e, exception);
		}
	}

	@Test
	public void testAddCallWithErrorParameter() throws Exception {
		MethodReturnValues MRV = new MethodReturnValues();
		RuntimeException exception = new RuntimeException();
		MRV.setThrowException("testAddCallWithErrorParameter", exception, ONE);

		MCR.useMRV(MRV);

		try {
			MCR.addCall(ONE, ONE);
			fail("An exception should have been thrown");
		} catch (Exception e) {
			assertSame(e, exception);
		}
	}

	@Test
	public void testGetNumberOfCallsToMethodNoMethodMatch() throws Exception {
		assertEquals(MCR.getNumberOfCallsToMethod(""), 0);
	}

	@Test
	public void testGetNumberOfCallsToMethodMatchedMethodWithTwoCalls() throws Exception {
		addCall1();
		addCall1();

		addCall2();
		addCall2();
		addCall2();

		assertEquals(MCR.getNumberOfCallsToMethod(ADD_CALL1), 2);
		assertEquals(MCR.getNumberOfCallsToMethod(ADD_CALL2), 3);
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "MethodName not found for \\(methodName: NoMethod, callNumber: 0\\)")
	public void testAddCallWithParametersNoMatch() throws Exception {
		assertEquals(MCR.getParametersForMethodAndCallNumber("NoMethod", 0).size(), 0);
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "CallNumber not found for \\(methodName: addCall1, callNumber: 10\\)")
	public void testAddCallWithParametersMethodMatchWrongCallNumber() throws Exception {
		addCall1();

		assertEquals(MCR.getParametersForMethodAndCallNumber(ADD_CALL1, 10).size(), 0);
	}

	@Test
	public void testAddCallWithParametersMatches() throws Exception {
		addCall2();
		addCall1();

		Map<String, Object> parametersCall1 = MCR.getParametersForMethodAndCallNumber(ADD_CALL1, 0);

		assertEquals(parametersCall1.size(), 2);
		assertEquals(parametersCall1.get(PARAM1), 1);
		assertEquals(parametersCall1.get(PARAM2), "2");

		Map<String, Object> parametersCall2 = MCR.getParametersForMethodAndCallNumber(ADD_CALL2, 0);

		assertEquals(parametersCall2.size(), 2);
		assertEquals(parametersCall2.get(PARAM2), 2);
		assertSame(parametersCall2.get(PARAM3), objectParameter);
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "MethodName not found for \\(methodName: methodName, callNumber: 0 "
			+ "and parameterName: parameterName\\)")
	public void testGetValueForMethodNameAndCallNumberAndParameterNameNoMatch() throws Exception {
		MCR.getValueForMethodNameAndCallNumberAndParameterName("methodName", 0, PARAMETER_NAME);
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "MethodName not found for \\(methodName: addCall, callNumber: 10 "
			+ "and parameterName: parameterName\\)")
	public void testGetValueForMethodNameAndCallNumberAndParameterNameCallNumberNoMatch()
			throws Exception {
		addCall1();

		MCR.getValueForMethodNameAndCallNumberAndParameterName(ADD_CALL, 10, PARAMETER_NAME);
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "MethodName not found for \\(methodName: addCall, callNumber: 0 "
			+ "and parameterName: parameterName\\)")
	public void testGetValueForMethodNameAndCallNumberAndParameterNameParameterNameNoMatch()
			throws Exception {
		addCall1();

		MCR.getValueForMethodNameAndCallNumberAndParameterName(ADD_CALL, 0, PARAMETER_NAME);
	}

	@Test
	public void testNameOneCall() throws Exception {
		addCall1();

		Object valueCall1Param1 = MCR.getValueForMethodNameAndCallNumberAndParameterName(ADD_CALL1,
				0, PARAM1);
		Object valueCall1Param2 = MCR.getValueForMethodNameAndCallNumberAndParameterName(ADD_CALL1,
				0, PARAM2);

		assertEquals(valueCall1Param1, 1);
		assertEquals(valueCall1Param2, "2");
	}

	@Test
	public void testNameSeveralCalls() throws Exception {
		addCall1();
		addCall2();
		addCall1();
		addCall2();

		Object valueCall2Param1 = MCR.getValueForMethodNameAndCallNumberAndParameterName(ADD_CALL2,
				1, PARAM2);
		Object valueCall2Param2 = MCR.getValueForMethodNameAndCallNumberAndParameterName(ADD_CALL2,
				1, PARAM3);

		assertEquals(valueCall2Param1, 2);
		assertSame(valueCall2Param2, objectParameter);
	}

	@Test(expectedExceptions = AssertionError.class, expectedExceptionsMessageRegExp = ""
			+ "expected \\[10\\] but found \\[0\\]")
	public void testAssertNumberOfCallsToMethodNoMatch() throws Exception {

		MCR.assertNumberOfCallsToMethod("", 10);
	}

	@Test(expectedExceptions = AssertionError.class, expectedExceptionsMessageRegExp = ""
			+ "expected \\[10\\] but found \\[4\\]")
	public void testAssertNumberOfCallsToMethodWrongNumberOfCalls() throws Exception {
		addCall1();
		addCall1();
		addCall1();
		addCall1();

		MCR.assertNumberOfCallsToMethod(ADD_CALL1, 10);
	}

	@Test
	public void testAssertNumberOfCallsToMethod() throws Exception {
		addCall1();
		addCall1();
		addCall1();

		addCall2();
		addCall2();
		boolean errorThrown = false;
		try {
			MCR.assertNumberOfCallsToMethod(ADD_CALL1, 3);
			MCR.assertNumberOfCallsToMethod(ADD_CALL2, 2);
		} catch (Exception e) {
			errorThrown = true;
		}
		assertFalse(errorThrown);

	}

	@Test(expectedExceptions = AssertionError.class, expectedExceptionsMessageRegExp = ""
			+ "expected \\[true\\] but found \\[false\\]")
	public void testAssertMethodWasCalledMethodNotCalled() throws Exception {
		MCR.assertMethodWasCalled(SOME_METHOD);
	}

	@Test
	public void testAssertMethodWasCalled() throws Exception {
		addCall1();
		addCall2();

		MCR.assertMethodWasCalled(ADD_CALL1);
		MCR.assertMethodWasCalled(ADD_CALL2);
	}

	@Test(expectedExceptions = AssertionError.class, expectedExceptionsMessageRegExp = ""
			+ "expected \\[false\\] but found \\[true\\]")
	public void testAssertMethodWasNotCalledMethodCalled1() throws Exception {
		addCall1();

		MCR.assertMethodNotCalled(ADD_CALL1);
	}

	@Test(expectedExceptions = AssertionError.class, expectedExceptionsMessageRegExp = ""
			+ "expected \\[false\\] but found \\[true\\]")
	public void testAssertMethodWasNotCalledMethodCalled2() throws Exception {
		addCall2();

		MCR.assertMethodNotCalled(ADD_CALL2);
	}

	@Test
	public void testAssertMethodNotCalledMethodNotCalled() throws Exception {
		addCall1();
		addCall2();

		MCR.assertMethodNotCalled(ADD_CALL3);
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "MethodName not found for \\(methodName: someMethod, callNumber: 0 "
			+ "and parameterName: someParameterName\\)")
	public void testAssertParameterNotFoundMethodName() throws Exception {
		MCR.assertParameter(SOME_METHOD, 0, SOME_PARAMETER_NAME, EXPECTED_VALUE);
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "MethodName not found for \\(methodName: someMethod, callNumber: 0 "
			+ "and parameterName: someParameterName\\)")
	public void testAssertParameterAsEqualNotFoundMethodName() throws Exception {
		MCR.assertParameterAsEqual(SOME_METHOD, 0, SOME_PARAMETER_NAME, EXPECTED_VALUE);
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "CallNumber not found for \\(methodName: addCall1, callNumber: 10 "
			+ "and parameterName: someParameterName\\)")
	public void testAssertParameterNotFoundCallNumber() throws Exception {
		addCall1();

		MCR.assertParameter(ADD_CALL1, 10, SOME_PARAMETER_NAME, EXPECTED_VALUE);
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "CallNumber not found for \\(methodName: addCall1, callNumber: 10 "
			+ "and parameterName: someParameterName\\)")
	public void testAssertParameterAsEqualNotFoundCallNumber() throws Exception {
		addCall1();

		MCR.assertParameterAsEqual(ADD_CALL1, 10, SOME_PARAMETER_NAME, EXPECTED_VALUE);
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "ParameterName not found for \\(methodName: addCall1, callNumber: 0 "
			+ "and parameterName: someParameterName\\)")
	public void testAssertParameterNotFoundParamName() throws Exception {
		addCall1();

		MCR.assertParameter(ADD_CALL1, 0, SOME_PARAMETER_NAME, EXPECTED_VALUE);
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "ParameterName not found for \\(methodName: addCall1, callNumber: 0 "
			+ "and parameterName: someParameterName\\)")
	public void testAssertParameterAsEqualNotFoundParamName() throws Exception {
		addCall1();

		MCR.assertParameterAsEqual(ADD_CALL1, 0, SOME_PARAMETER_NAME, EXPECTED_VALUE);
	}

	@Test(expectedExceptions = AssertionError.class, expectedExceptionsMessageRegExp = ""
			+ "expected \\[100\\] but found \\[1\\]")
	public void testAssertParameterValueNotEqual() throws Exception {
		addCall1();

		MCR.assertParameter(ADD_CALL1, 0, PARAM1, 100);
	}

	@Test(expectedExceptions = AssertionError.class, expectedExceptionsMessageRegExp = ""
			+ "expected \\[100\\] but found \\[1\\]")
	public void testAssertParameterAsEqualValueNotEqual() throws Exception {
		addCall1();

		MCR.assertParameterAsEqual(ADD_CALL1, 0, PARAM1, 100);
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "expected value type is class java.lang.String but found class java.lang.Integer")
	public void testAssertParameterValueDifferentTypes() throws Exception {
		addCall1();

		MCR.assertParameter(ADD_CALL1, 0, PARAM1, "1");
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "expected value type is class java.lang.String but found class java.lang.Integer")
	public void testAssertParameterAsEqualValueDifferentTypes() throws Exception {
		addCall1();

		MCR.assertParameterAsEqual(ADD_CALL1, 0, PARAM1, "1");
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "expected value type is class java.lang.Integer but found class java.lang.String")
	public void testAssertParameterValueDifferentTypesDynamicErrorMessage() throws Exception {
		addCall1();
		MCR.assertParameter(ADD_CALL1, 0, PARAM2, 2);
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "expected value type is class java.lang.Integer but found class java.lang.String")
	public void testAssertParameterAsEqualValueDifferentTypesDynamicErrorMessage()
			throws Exception {
		addCall1();
		MCR.assertParameterAsEqual(ADD_CALL1, 0, PARAM2, 2);
	}

	@Test
	public void testAssertParameterEqualValues() throws Exception {
		addCall1();
		addCall2();
		addCall3();

		MCR.assertParameter(ADD_CALL1, 0, PARAM1, 1);
		MCR.assertParameter(ADD_CALL2, 0, PARAM3, objectParameter);
		MCR.assertParameter(ADD_CALL3, 0, PARAM1, 1L);

		assertParametersForPrimitivesMadeSureTheyAreDifferentObjectsXvalueOfCreatesSameObject();
	}

	private void assertParametersForPrimitivesMadeSureTheyAreDifferentObjectsXvalueOfCreatesSameObject() {
		MCR.assertParameter(ADD_CALL1, 0, PARAM1, Integer.valueOf(1));
		MCR.assertParameter(ADD_CALL3, 0, PARAM1, Long.valueOf(1));
		MCR.assertParameter(ADD_CALL1, 0, PARAM2, String.valueOf("2"));
		MCR.assertParameter(ADD_CALL3, 0, PARAM2, Long.valueOf(A_LONG_TO_BIG_FOR_INT));
	}

	@Test
	public void testAssertParameterAsEqualEqualValues() throws Exception {
		addCall1();
		addCall2();
		addCall3();

		MCR.assertParameterAsEqual(ADD_CALL1, 0, PARAM1, 1);
		MCR.assertParameterAsEqual(ADD_CALL2, 0, PARAM3, objectParameter);
		MCR.assertParameterAsEqual(ADD_CALL3, 0, PARAM1, 1L);

		assertParametersAsEqualForPrimitivesMadeSureTheyAreDifferentObjectsXvalueOfCreatesSameObject();
	}

	private void assertParametersAsEqualForPrimitivesMadeSureTheyAreDifferentObjectsXvalueOfCreatesSameObject() {
		MCR.assertParameterAsEqual(ADD_CALL1, 0, PARAM1, Integer.valueOf(1));
		MCR.assertParameterAsEqual(ADD_CALL3, 0, PARAM1, Long.valueOf(1));
		MCR.assertParameterAsEqual(ADD_CALL1, 0, PARAM2, String.valueOf("2"));
		MCR.assertParameterAsEqual(ADD_CALL3, 0, PARAM2, Long.valueOf(A_LONG_TO_BIG_FOR_INT));
	}

	@Test(expectedExceptions = AssertionError.class, expectedExceptionsMessageRegExp = ""
			+ "expected \\[45\\] but found \\[1\\]")
	public void testAssertParameterDifferentint() throws Exception {
		addCall1();
		MCR.assertParameter(ADD_CALL1, 0, PARAM1, 45);
	}

	@Test(expectedExceptions = AssertionError.class, expectedExceptionsMessageRegExp = ""
			+ "expected \\[45\\] but found \\[1\\]")
	public void testAssertParameterAsEqualDifferentint() throws Exception {
		addCall1();
		MCR.assertParameterAsEqual(ADD_CALL1, 0, PARAM1, 45);
	}

	@Test(expectedExceptions = AssertionError.class, expectedExceptionsMessageRegExp = ""
			+ "expected \\[45\\] but found \\[1\\]")
	public void testAssertParameterDifferentLong() throws Exception {
		addCall3();
		MCR.assertParameter(ADD_CALL3, 0, PARAM1, 45L);
	}

	@Test(expectedExceptions = AssertionError.class, expectedExceptionsMessageRegExp = ""
			+ "expected \\[45\\] but found \\[1\\]")
	public void testAssertParameterAsEqualDifferentLong() throws Exception {
		addCall3();
		MCR.assertParameterAsEqual(ADD_CALL3, 0, PARAM1, 45L);
	}

	@Test(expectedExceptions = AssertionError.class)
	public void testAssertParameterDifferentObject() throws Exception {
		addCall2();

		MCR.assertParameter(ADD_CALL2, 0, PARAM3, new Object());
	}

	@Test(expectedExceptions = AssertionError.class)
	public void testAssertParameterAsEqualDifferentObject() throws Exception {
		addCall2();

		MCR.assertParameterAsEqual(ADD_CALL2, 0, PARAM3, new Object());
	}

	@Test(expectedExceptions = AssertionError.class, expectedExceptionsMessageRegExp = ""
			+ "expected \\[ObjectOnlyForTest: 2\\] but found \\[ObjectOnlyForTest: 1\\]")
	public void testAssertParameterDifferentObjectButEqualsIsTrueShouldThrowException()
			throws Exception {
		addCall5();

		MCR.assertParameter("addCall5", 0, PARAM1, new ObjectOnlyForTest(2));
	}

	private void addCall5() {
		MCR.addCall(PARAM1, new ObjectOnlyForTest(1));
	}

	@Test
	public void testAssertParameterAsEqualDifferentObjectButEqualsIsTrue() throws Exception {
		addObjectOnlyForTestCall();

		MCR.assertParameterAsEqual("addObjectOnlyForTestCall", 0, PARAM1, new ObjectOnlyForTest(2));
	}

	private void addObjectOnlyForTestCall() {
		MCR.addCall(PARAM1, new ObjectOnlyForTest(1));
	}

	@Test
	public void testAssertParameterAsEqualList() throws Exception {
		addListCall();

		MCR.assertParameterAsEqual("addListCall", 0, PARAM1, List.of(ITEM1, ITEM2));
	}

	private void addListCall() {
		MCR.addCall(PARAM1, List.of(ITEM1, ITEM2));
	}

	class ObjectOnlyForTest {
		int thisNo;

		public ObjectOnlyForTest(int number) {
			thisNo = number;
		}

		@Override
		public boolean equals(Object obj) {
			return true;
		}

		@Override
		public String toString() {
			return "ObjectOnlyForTest: " + thisNo;
		}
	}

	@Test
	public void testMakeSureAssertParametersCallsAssertParameterForEachExpectedValue()
			throws Exception {
		addCallForTest();

		MCRforTestAV.assertParameters("addCallForTest", 0, VALUE1, VALUE2);

		assertEquals(MCRforTestAV.assertParameterCallsCounter, 2);
		assertEquals(MCRforTestAV.expectedValue.get(0), VALUE1);
		assertEquals(MCRforTestAV.actualValue.get(0), VALUE1);
		assertEquals(MCRforTestAV.expectedValue.get(1), VALUE2);
		assertEquals(MCRforTestAV.actualValue.get(1), VALUE2);

	}

	private void addCallForTest() {
		MCRforTestAV.addCall(PARAM1, VALUE1, PARAM2, VALUE2, PARAM3, VALUE3);
	}

	class MethodCallRecorderOnlyForTestAssertValues extends MethodCallRecorder {
		int assertParameterCallsCounter = 0;
		private List<Object> expectedValue = new ArrayList<>();
		private List<Object> actualValue = new ArrayList<>();;

		@Override
		void assertValuesAreEqual(Object expectedValue, Object actualValue) {
			this.expectedValue.add(expectedValue);
			this.actualValue.add(actualValue);
			assertParameterCallsCounter++;
		}
	}

	@Test
	public void testAssertCalledParametersCallsAssertParametersForAllAnswersThreeCalls()
			throws Exception {
		String methodName = "addCallForTest2";
		addCallForTest2();
		addCallForTest2();
		addCallForTest2();
		MCRforTestACP.setThrowsError(List.of("none", "none", "none"));

		try {
			MCRforTestACP.assertCalledParameters(methodName, VALUE1, VALUE2, VALUE3);
		} catch (Exception e) {
			fail();
		}

		String[] value = new String[] { VALUE1, VALUE2, VALUE3 };
		assertEquals(MCRforTestACP.assertParametersCallsCounter, 1);

		assertEquals(MCRforTestACP.methodNames.get(0), methodName);
		assertEquals(MCRforTestACP.callNumbers.get(0), 0);
		assertEquals(MCRforTestACP.expectedValues.get(0), value);
	}

	@Test
	public void testAssertCalledParametersCallsAssertParametersForAllAnswersThreeCallsOneAndTwoFails()
			throws Exception {
		String methodName = "addCallForTest2";
		addCallForTest2();
		addCallForTest2();
		addCallForTest2();
		MCRforTestACP.setThrowsError(List.of("assertion", "runtime", "none"));

		try {
			MCRforTestACP.assertCalledParameters(methodName, VALUE1, VALUE2, VALUE3);
		} catch (Exception e) {
			fail();
		}

		String[] value = new String[] { VALUE1, VALUE2, VALUE3 };
		assertEquals(MCRforTestACP.assertParametersCallsCounter, 3);

		assertEquals(MCRforTestACP.methodNames.get(0), methodName);
		assertEquals(MCRforTestACP.callNumbers.get(0), 0);
		assertEquals(MCRforTestACP.expectedValues.get(0), value);

		assertEquals(MCRforTestACP.methodNames.get(1), methodName);
		assertEquals(MCRforTestACP.callNumbers.get(1), 1);
		assertEquals(MCRforTestACP.expectedValues.get(1), value);

		assertEquals(MCRforTestACP.methodNames.get(2), methodName);
		assertEquals(MCRforTestACP.callNumbers.get(2), 2);
		assertEquals(MCRforTestACP.expectedValues.get(2), value);
	}

	@Test
	public void testAssertCalledParametersCallsAssertParametersForAllAnswersThreeCallsAllFails()
			throws Exception {
		String methodName = "addCallForTest2";
		addCallForTest2();
		addCallForTest2();
		addCallForTest2();
		MCRforTestACP.setThrowsError(List.of("assertion", "runtime", "runtime"));

		try {
			MCRforTestACP.assertCalledParameters(methodName, VALUE1, VALUE2, VALUE3);
			fail();
		} catch (Error e) {
			assertTrue(e instanceof AssertionError);
			assertEquals(e.getMessage(),
					"Method: " + methodName + " not called with values: [value1, value2, value3]");
		}

		String[] value = new String[] { VALUE1, VALUE2, VALUE3 };
		assertEquals(MCRforTestACP.assertParametersCallsCounter, 3);

		assertEquals(MCRforTestACP.methodNames.get(0), methodName);
		assertEquals(MCRforTestACP.callNumbers.get(0), 0);
		assertEquals(MCRforTestACP.expectedValues.get(0), value);

		assertEquals(MCRforTestACP.methodNames.get(1), methodName);
		assertEquals(MCRforTestACP.callNumbers.get(1), 1);
		assertEquals(MCRforTestACP.expectedValues.get(1), value);

		assertEquals(MCRforTestACP.methodNames.get(2), methodName);
		assertEquals(MCRforTestACP.callNumbers.get(2), 2);
		assertEquals(MCRforTestACP.expectedValues.get(2), value);
	}

	@Test
	public void testAssertCalledParametersReturnCallsAssertParametersForAllAnswersThreeCalls()
			throws Exception {
		String methodName = "addCallForTest2";
		addCallForTest2();
		addCallForTest2();
		addCallForTest2();
		MCRforTestACP.setThrowsError(List.of("none", "none", "none"));

		try {
			Object returnValue = MCRforTestACP.assertCalledParametersReturn(methodName, VALUE1,
					VALUE2, VALUE3);
			assertSame(MCRforTestACP.getReturnValue(methodName, 0), returnValue);
		} catch (Exception e) {
			fail();
		}

		String[] value = new String[] { VALUE1, VALUE2, VALUE3 };
		assertEquals(MCRforTestACP.assertParametersCallsCounter, 1);

		assertEquals(MCRforTestACP.methodNames.get(0), methodName);
		assertEquals(MCRforTestACP.callNumbers.get(0), 0);
		assertEquals(MCRforTestACP.expectedValues.get(0), value);
	}

	@Test
	public void testAssertCalledParametersReturnCallsAssertParametersForAllAnswersThreeCallsNoReturnValue()
			throws Exception {
		String methodName = "addCallForTest2NoReturnValue";
		addCallForTest2NoReturnValue();
		addCallForTest2NoReturnValue();
		addCallForTest2NoReturnValue();
		MCRforTestACP.setThrowsError(List.of("none", "none", "none"));

		try {
			MCRforTestACP.assertCalledParametersReturn(methodName, VALUE1, VALUE2, VALUE3);
			fail();
		} catch (Exception e) {
			// fail();
			assertTrue(e instanceof RuntimeException);
			assertEquals(e.getMessage(), "No return value found for method: " + methodName
					+ " called with values: [value1, value2, value3]");
		}

		String[] value = new String[] { VALUE1, VALUE2, VALUE3 };
		assertEquals(MCRforTestACP.assertParametersCallsCounter, 1);

		assertEquals(MCRforTestACP.methodNames.get(0), methodName);
		assertEquals(MCRforTestACP.callNumbers.get(0), 0);
		assertEquals(MCRforTestACP.expectedValues.get(0), value);
	}

	@Test
	public void testAssertCalledParametersReturnCallsAssertParametersForAllAnswersThreeCallsOneAndTwoFails()
			throws Exception {
		String methodName = "addCallForTest2";
		addCallForTest2();
		addCallForTest2();
		addCallForTest2();
		MCRforTestACP.setThrowsError(List.of("assertion", "runtime", "none"));

		try {
			Object returnValue = MCRforTestACP.assertCalledParametersReturn(methodName, VALUE1,
					VALUE2, VALUE3);
			assertSame(MCRforTestACP.getReturnValue(methodName, 0), returnValue);
		} catch (Exception e) {
			fail();
		}

		String[] value = new String[] { VALUE1, VALUE2, VALUE3 };
		assertEquals(MCRforTestACP.assertParametersCallsCounter, 3);

		assertEquals(MCRforTestACP.methodNames.get(0), methodName);
		assertEquals(MCRforTestACP.callNumbers.get(0), 0);
		assertEquals(MCRforTestACP.expectedValues.get(0), value);

		assertEquals(MCRforTestACP.methodNames.get(1), methodName);
		assertEquals(MCRforTestACP.callNumbers.get(1), 1);
		assertEquals(MCRforTestACP.expectedValues.get(1), value);

		assertEquals(MCRforTestACP.methodNames.get(2), methodName);
		assertEquals(MCRforTestACP.callNumbers.get(2), 2);
		assertEquals(MCRforTestACP.expectedValues.get(2), value);
	}

	@Test
	public void testAssertCalledParametersReturnCallsAssertParametersForAllAnswersThreeCallsAllFails()
			throws Exception {
		String methodName = "addCallForTest2";
		addCallForTest2();
		addCallForTest2();
		addCallForTest2();
		MCRforTestACP.setThrowsError(List.of("assertion", "runtime", "runtime"));

		try {
			MCRforTestACP.assertCalledParametersReturn(methodName, VALUE1, VALUE2, VALUE3);
			fail();
		} catch (Error e) {
			assertTrue(e instanceof AssertionError);
			assertEquals(e.getMessage(),
					"Method: " + methodName + " not called with values: [value1, value2, value3]");
		}

		String[] value = new String[] { VALUE1, VALUE2, VALUE3 };
		assertEquals(MCRforTestACP.assertParametersCallsCounter, 3);

		assertEquals(MCRforTestACP.methodNames.get(0), methodName);
		assertEquals(MCRforTestACP.callNumbers.get(0), 0);
		assertEquals(MCRforTestACP.expectedValues.get(0), value);

		assertEquals(MCRforTestACP.methodNames.get(1), methodName);
		assertEquals(MCRforTestACP.callNumbers.get(1), 1);
		assertEquals(MCRforTestACP.expectedValues.get(1), value);

		assertEquals(MCRforTestACP.methodNames.get(2), methodName);
		assertEquals(MCRforTestACP.callNumbers.get(2), 2);
		assertEquals(MCRforTestACP.expectedValues.get(2), value);
	}

	class MethodCallRecorderOnlyForTestAssertCalledParameters extends MethodCallRecorder {
		int assertParametersCallsCounter = 0;
		List<String> methodNames = new ArrayList<>();
		private List<Integer> callNumbers = new ArrayList<>();;
		private List<Object> expectedValues = new ArrayList<>();
		private List<String> throwErrors = List.of("none");

		@Override
		public void assertParameters(String methodName, int callNumber, Object... expectedValues) {
			this.methodNames.add(methodName);
			this.callNumbers.add(callNumber);
			this.expectedValues.add(expectedValues);
			assertParametersCallsCounter++;
			String typeToThrow = throwErrors.get(callNumber);
			if (typeToThrow.equals("assertion")) {
				throw new AssertionError("Error from MethodCallRecorderForTest2");
			}
			if (typeToThrow.equals("runtime")) {
				throw new RuntimeException("Error from MethodCallRecorderForTest2");
			}
		}

		public void setThrowsError(List<String> throwErrors) {
			this.throwErrors = throwErrors;
		}
	}

	private void addCallForTest2() {
		MCRforTestACP.addCall(PARAM1, VALUE1, PARAM2, VALUE2, PARAM3, VALUE3);
		MCRforTestACP.addReturned("someReturnValue");
	}

	private void addCallForTest2NoReturnValue() {
		MCRforTestACP.addCall(PARAM1, VALUE1, PARAM2, VALUE2, PARAM3, VALUE3);
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "Too many values to compare for \\(methodName: addCall1, callNumber: 0\\)")
	public void testMakeSureAssertParametersDoesNotCallsAssertParameterMoreTimesThanEnteredParams()
			throws Exception {
		addCall1();

		MCR.assertParameters(ADD_CALL1, 0, 1, "2", "valueNotRecorded");

	}

	@Test
	public void testAssertParametersPositiveAssertDifferentTypes() throws Exception {
		addCall1();
		addCall2();

		MCR.assertParameters(ADD_CALL1, 0, 1, "2");
		MCR.assertParameters(ADD_CALL2, 0, 2, objectParameter);

	}

	@Test(expectedExceptions = AssertionError.class, expectedExceptionsMessageRegExp = ""
			+ "expected \\[3\\] but found \\[2\\]")
	public void testAssertParametersWrongValue() throws Exception {
		addCall1();
		addCall2();

		MCR.assertParameters(ADD_CALL1, 0, 1, "3");
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "CallNumber not found for \\(methodName: addCall4, callNumber: 1\\)")
	public void testAssertParametersOnlyOneParameter() throws Exception {
		addCall4();

		MCR.assertParameters(ADD_CALL4, 1, ONLY_ONE);
	}

	@Test
	public void testStoredValueIsNullCheckedNull() throws Exception {
		MCR.addCall(PARAM1, null);

		String testValue = null;
		MCR.assertParameters("testStoredValueIsNullCheckedNull", 0, testValue);
	}

	@Test(expectedExceptions = AssertionError.class, expectedExceptionsMessageRegExp = ""
			+ "expected \\[not null\\] but found \\[null\\]")
	public void testStoredValueIsNullCheckedNotNull() throws Exception {
		MCR.addCall(PARAM1, null);

		String testValue = "not null";
		MCR.assertParameters("testStoredValueIsNullCheckedNotNull", 0, testValue);
	}

	@Test(expectedExceptions = AssertionError.class, expectedExceptionsMessageRegExp = ""
			+ "expected \\[null\\] but found \\[not null\\]")
	public void testStoredValueIsNotNullCheckedNull() throws Exception {
		MCR.addCall(PARAM1, "not null");

		String testValue = null;
		MCR.assertParameters("testStoredValueIsNotNullCheckedNull", 0, testValue);
	}

	@Test
	public void testAddReturn() throws Exception {
		String valueToReturn = SOME_VALUE;
		MCR.addReturned(valueToReturn);

		assertEquals(MCR.getReturnValue("testAddReturn", 0), SOME_VALUE);

	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "MethodName not found for \\(methodName: someMethod, callNumber: 0\\)")
	public void testAddReturnNoMethodFound() throws Exception {
		String valueToReturn = SOME_VALUE;
		MCR.addReturned(valueToReturn);

		MCR.getReturnValue(SOME_METHOD, 0);
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "MethodName not found for \\(methodName: someMethod2, callNumber: 3\\)")
	public void testAddReturnNoMethodFound2() throws Exception {
		String valueToReturn = SOME_VALUE3;
		MCR.addReturned(valueToReturn);

		MCR.getReturnValue("someMethod2", 3);
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "CallNumber not found for \\(methodName: testAddReturnCallNumberNotFound, callNumber: 10\\)")
	public void testAddReturnCallNumberNotFound() throws Exception {
		String valueToReturn = SOME_VALUE;
		MCR.addReturned(valueToReturn);

		MCR.getReturnValue("testAddReturnCallNumberNotFound", 10);
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "CallNumber not found for \\(methodName: testAddReturnCallNumberNotFound2, callNumber: 11\\)")
	public void testAddReturnCallNumberNotFound2() throws Exception {
		String valueToReturn = SOME_VALUE2;
		MCR.addReturned(valueToReturn);

		MCR.getReturnValue("testAddReturnCallNumberNotFound2", 11);
	}

	@Test
	public void testAddGetReturnValues() throws Exception {
		MCR.addReturned(SOME_VALUE);

		assertEquals(MCR.getReturnValues("testAddGetReturnValues"), List.of(SOME_VALUE));
	}

	@Test
	public void testAddGetReturnValuesMoreValues() throws Exception {
		MCR.addReturned(SOME_VALUE);
		MCR.addReturned(SOME_VALUE2);
		MCR.addReturned(SOME_VALUE3);

		assertEquals(MCR.getReturnValues("testAddGetReturnValuesMoreValues"),
				List.of(SOME_VALUE, SOME_VALUE2, SOME_VALUE3));
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "MethodName not found for \\(methodName: someMethod\\)")
	public void testAddGetReturnValuesNoMethodFound() throws Exception {
		String valueToReturn = SOME_VALUE;
		MCR.addReturned(valueToReturn);

		MCR.getReturnValues(SOME_METHOD);
	}

	@Test
	public void testAssertReturnPositiveAssertion() throws Exception {
		String valueToReturn = SOME_VALUE;
		MCR.addReturned(valueToReturn);

		MCR.assertReturn("testAssertReturnPositiveAssertion", 0, valueToReturn);
	}

	@Test(expectedExceptions = AssertionError.class, expectedExceptionsMessageRegExp = ""
			+ "expected \\[someOtherValue\\] but found \\[someValue\\]")
	public void testAssertReturnNegativeAssertion() throws Exception {
		MCR.addReturned(SOME_VALUE);

		MCR.assertReturn("testAssertReturnNegativeAssertion", 0, SOME_OTHER_VALUE);
	}

	@Test
	public void testAddReturnedSeveralCalls() throws Exception {
		MCR.addReturned(SOME_VALUE1);
		MCR.addReturned(SOME_VALUE2);

		MCR.assertReturn("testAddReturnedSeveralCalls", 0, SOME_VALUE1);
		MCR.assertReturn("testAddReturnedSeveralCalls", 1, SOME_VALUE2);
	}

	@Test
	public void testNoMRVsetThrowsError() throws Exception {
		Exception caughtException = null;
		try {
			MCR.addCallAndReturnFromMRV(PARAM_VALUE1);
			fail("An exception should have been thrown");
		} catch (Exception e) {
			caughtException = e;
		}
		assertNotNull(caughtException);
		assertEquals(caughtException.getMessage(), "Method addCallAndReturnFromMRV can not be used "
				+ "before a MVR has been set using the method useMRV");
	}

	@Test
	public void useMRV() throws Exception {
		MethodReturnValues MRV = new MethodReturnValues();
		MRV.setReturnValues(USE_MRV, List.of(RETURN1), PARAM_VALUE1);

		MCR.useMRV(MRV);
		Object returnValue = MCR.addCallAndReturnFromMRV(PARAM_NAME, PARAM_VALUE1);

		assertEquals(returnValue, RETURN1);
		MCR.assertMethodWasCalled(USE_MRV);
		MCR.assertReturn(USE_MRV, 0, RETURN1);
		assertEquals(returnValue, RETURN1);
	}

	@Test
	public void useMRVWithTwoParameters() throws Exception {
		MethodReturnValues MRV = new MethodReturnValues();
		MRV.setReturnValues("useMRVWithTwoParameters", List.of(RETURN1), PARAM_VALUE1,
				PARAM_VALUE2);

		MCR.useMRV(MRV);
		Object returnValue = MCR.addCallAndReturnFromMRV(PARAM_NAME, PARAM_VALUE1, PARAM_NAME2,
				PARAM_VALUE2);

		assertEquals(returnValue, RETURN1);
		MCR.assertMethodWasCalled("useMRVWithTwoParameters");
		MCR.assertReturn("useMRVWithTwoParameters", 0, RETURN1);
		assertEquals(returnValue, RETURN1);
	}

	@Test
	public void testOnlyForTestGetMRV() throws Exception {
		MethodReturnValues MRV = new MethodReturnValues();
		MCR.useMRV(MRV);

		assertSame(MCR.onlyForTestGetMRV(), MRV);
	}
}
