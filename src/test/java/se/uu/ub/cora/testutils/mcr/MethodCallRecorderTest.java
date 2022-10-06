package se.uu.ub.cora.testutils.mcr;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.testutils.mrv.MethodReturnValues;

public class MethodCallRecorderTest {

	private static final long A_LONG_TO_BIG_FOR_INT = 3147483647L;
	MethodCallRecorder MCR;
	private Object objectParameter = new Object();
	private MethodCallRecorderForTest MCRforTest;

	@BeforeMethod
	public void beforeMethod() {
		MCR = new MethodCallRecorder();
		MCRforTest = new MethodCallRecorderForTest();
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

		assertTrue(MCR.methodWasCalled("addCall1"));
		assertTrue(MCR.methodWasCalled("addCall2"));
		assertFalse(MCR.methodWasCalled("addCall3"));

	}

	private void addCall1() {
		MCR.addCall("param1", 1, "param2", "2");
	}

	private void addCall2() {
		MCR.addCall("param2", 2, "param3", objectParameter);
	}

	private void addCall3() {
		MCR.addCall("param1", 1L, "param2", 3147483647L);
	}

	private void addCall4() {
		MCR.addCall("param1", "onlyOne");
	}

	@Test
	public void testAddCallWithError() throws Exception {
		MethodReturnValues MRV = new MethodReturnValues();
		RuntimeException exception = new RuntimeException();
		MRV.setAlwaysThrowException("testAddCallWithError", exception);

		MCR.useMRV(MRV);

		try {
			MCR.addCall();
			assertTrue(false);
		} catch (Exception e) {
			assertSame(e, exception);
		}
	}

	@Test
	public void testAddCallWithErrorParameter() throws Exception {
		MethodReturnValues MRV = new MethodReturnValues();
		RuntimeException exception = new RuntimeException();
		MRV.setThrowException("testAddCallWithErrorParameter", exception, "one");

		MCR.useMRV(MRV);

		try {
			MCR.addCall("one", "one");
			assertTrue(false);
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

		assertEquals(MCR.getNumberOfCallsToMethod("addCall1"), 2);
		assertEquals(MCR.getNumberOfCallsToMethod("addCall2"), 3);
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

		assertEquals(MCR.getParametersForMethodAndCallNumber("addCall1", 10).size(), 0);
	}

	@Test
	public void testAddCallWithParametersMatches() throws Exception {
		addCall2();
		addCall1();

		Map<String, Object> parametersCall1 = MCR.getParametersForMethodAndCallNumber("addCall1",
				0);

		assertEquals(parametersCall1.size(), 2);
		assertEquals(parametersCall1.get("param1"), 1);
		assertEquals(parametersCall1.get("param2"), "2");

		Map<String, Object> parametersCall2 = MCR.getParametersForMethodAndCallNumber("addCall2",
				0);

		assertEquals(parametersCall2.size(), 2);
		assertEquals(parametersCall2.get("param2"), 2);
		assertSame(parametersCall2.get("param3"), objectParameter);
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "MethodName not found for \\(methodName: methodName, callNumber: 0 and parameterName: parameterName\\)")
	public void testGetValueForMethodNameAndCallNumberAndParameterNameNoMatch() throws Exception {
		MCR.getValueForMethodNameAndCallNumberAndParameterName("methodName", 0, "parameterName");
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "MethodName not found for \\(methodName: addCall, callNumber: 10 and parameterName: parameterName\\)")
	public void testGetValueForMethodNameAndCallNumberAndParameterNameCallNumberNoMatch()
			throws Exception {
		addCall1();

		MCR.getValueForMethodNameAndCallNumberAndParameterName("addCall", 10, "parameterName");
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "MethodName not found for \\(methodName: addCall, callNumber: 0 and parameterName: parameterName\\)")
	public void testGetValueForMethodNameAndCallNumberAndParameterNameParameterNameNoMatch()
			throws Exception {
		addCall1();

		MCR.getValueForMethodNameAndCallNumberAndParameterName("addCall", 0, "parameterName");
	}

	@Test
	public void testNameOneCall() throws Exception {
		addCall1();

		Object valueCall1Param1 = MCR.getValueForMethodNameAndCallNumberAndParameterName("addCall1",
				0, "param1");
		Object valueCall1Param2 = MCR.getValueForMethodNameAndCallNumberAndParameterName("addCall1",
				0, "param2");

		assertEquals(valueCall1Param1, 1);
		assertEquals(valueCall1Param2, "2");
	}

	@Test
	public void testNameSeveralCalls() throws Exception {
		addCall1();
		addCall2();
		addCall1();
		addCall2();

		Object valueCall2Param1 = MCR.getValueForMethodNameAndCallNumberAndParameterName("addCall2",
				1, "param2");
		Object valueCall2Param2 = MCR.getValueForMethodNameAndCallNumberAndParameterName("addCall2",
				1, "param3");

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

		MCR.assertNumberOfCallsToMethod("addCall1", 10);
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
			MCR.assertNumberOfCallsToMethod("addCall1", 3);
			MCR.assertNumberOfCallsToMethod("addCall2", 2);
		} catch (Exception e) {
			errorThrown = true;
		}
		assertFalse(errorThrown);

	}

	@Test(expectedExceptions = AssertionError.class, expectedExceptionsMessageRegExp = ""
			+ "expected \\[true\\] but found \\[false\\]")
	public void testAssertMethodWasCalledMethodNotCalled() throws Exception {
		MCR.assertMethodWasCalled("someMethod");
	}

	@Test
	public void testAssertMethodWasCalled() throws Exception {
		addCall1();
		addCall2();

		MCR.assertMethodWasCalled("addCall1");
		MCR.assertMethodWasCalled("addCall2");
	}

	@Test(expectedExceptions = AssertionError.class, expectedExceptionsMessageRegExp = ""
			+ "expected \\[false\\] but found \\[true\\]")
	public void testAssertMethodWasNotCalledMethodCalled1() throws Exception {
		addCall1();

		MCR.assertMethodNotCalled("addCall1");
	}

	@Test(expectedExceptions = AssertionError.class, expectedExceptionsMessageRegExp = ""
			+ "expected \\[false\\] but found \\[true\\]")
	public void testAssertMethodWasNotCalledMethodCalled2() throws Exception {
		addCall2();

		MCR.assertMethodNotCalled("addCall2");
	}

	@Test
	public void testAssertMethodNotCalledMethodNotCalled() throws Exception {
		addCall1();
		addCall2();

		MCR.assertMethodNotCalled("addCall3");
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "MethodName not found for \\(methodName: someMethod, callNumber: 0 and parameterName: someParameterName\\)")
	public void testAssertParameterNotFoundMethodName() throws Exception {
		MCR.assertParameter("someMethod", 0, "someParameterName", "expectedValue");
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "MethodName not found for \\(methodName: someMethod, callNumber: 0 and parameterName: someParameterName\\)")
	public void testAssertParameterAsEqualNotFoundMethodName() throws Exception {
		MCR.assertParameterAsEqual("someMethod", 0, "someParameterName", "expectedValue");
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "CallNumber not found for \\(methodName: addCall1, callNumber: 10 and parameterName: someParameterName\\)")
	public void testAssertParameterNotFoundCallNumber() throws Exception {
		addCall1();

		MCR.assertParameter("addCall1", 10, "someParameterName", "expectedValue");
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "CallNumber not found for \\(methodName: addCall1, callNumber: 10 and parameterName: someParameterName\\)")
	public void testAssertParameterAsEqualNotFoundCallNumber() throws Exception {
		addCall1();

		MCR.assertParameterAsEqual("addCall1", 10, "someParameterName", "expectedValue");
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "ParameterName not found for \\(methodName: addCall1, callNumber: 0 and parameterName: someParameterName\\)")
	public void testAssertParameterNotFoundParamName() throws Exception {
		addCall1();

		MCR.assertParameter("addCall1", 0, "someParameterName", "expectedValue");
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "ParameterName not found for \\(methodName: addCall1, callNumber: 0 and parameterName: someParameterName\\)")
	public void testAssertParameterAsEqualNotFoundParamName() throws Exception {
		addCall1();

		MCR.assertParameterAsEqual("addCall1", 0, "someParameterName", "expectedValue");
	}

	@Test(expectedExceptions = AssertionError.class, expectedExceptionsMessageRegExp = ""
			+ "expected \\[100\\] but found \\[1\\]")
	public void testAssertParameterValueNotEqual() throws Exception {
		addCall1();

		MCR.assertParameter("addCall1", 0, "param1", 100);
	}

	@Test(expectedExceptions = AssertionError.class, expectedExceptionsMessageRegExp = ""
			+ "expected \\[100\\] but found \\[1\\]")
	public void testAssertParameterAsEqualValueNotEqual() throws Exception {
		addCall1();

		MCR.assertParameterAsEqual("addCall1", 0, "param1", 100);
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "expected value type is class java.lang.String but found class java.lang.Integer")
	public void testAssertParameterValueDifferentTypes() throws Exception {
		addCall1();

		MCR.assertParameter("addCall1", 0, "param1", "1");
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "expected value type is class java.lang.String but found class java.lang.Integer")
	public void testAssertParameterAsEqualValueDifferentTypes() throws Exception {
		addCall1();

		MCR.assertParameterAsEqual("addCall1", 0, "param1", "1");
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "expected value type is class java.lang.Integer but found class java.lang.String")
	public void testAssertParameterValueDifferentTypesDynamicErrorMessage() throws Exception {
		addCall1();
		MCR.assertParameter("addCall1", 0, "param2", 2);
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "expected value type is class java.lang.Integer but found class java.lang.String")
	public void testAssertParameterAsEqualValueDifferentTypesDynamicErrorMessage()
			throws Exception {
		addCall1();
		MCR.assertParameterAsEqual("addCall1", 0, "param2", 2);
	}

	@Test
	public void testAssertParameterEqualValues() throws Exception {
		addCall1();
		addCall2();
		addCall3();

		MCR.assertParameter("addCall1", 0, "param1", 1);
		MCR.assertParameter("addCall2", 0, "param3", objectParameter);
		MCR.assertParameter("addCall3", 0, "param1", 1L);

		assertParametersForPrimitivesMadeSureTheyAreDifferentObjectsXvalueOfCreatesSameObject();
	}

	private void assertParametersForPrimitivesMadeSureTheyAreDifferentObjectsXvalueOfCreatesSameObject() {
		MCR.assertParameter("addCall1", 0, "param1", Integer.valueOf(1));
		MCR.assertParameter("addCall3", 0, "param1", Long.valueOf(1));
		MCR.assertParameter("addCall1", 0, "param2", String.valueOf("2"));
		MCR.assertParameter("addCall3", 0, "param2", Long.valueOf(A_LONG_TO_BIG_FOR_INT));
	}

	@Test
	public void testAssertParameterAsEqualEqualValues() throws Exception {
		addCall1();
		addCall2();
		addCall3();

		MCR.assertParameterAsEqual("addCall1", 0, "param1", 1);
		MCR.assertParameterAsEqual("addCall2", 0, "param3", objectParameter);
		MCR.assertParameterAsEqual("addCall3", 0, "param1", 1L);

		assertParametersAsEqualForPrimitivesMadeSureTheyAreDifferentObjectsXvalueOfCreatesSameObject();
	}

	private void assertParametersAsEqualForPrimitivesMadeSureTheyAreDifferentObjectsXvalueOfCreatesSameObject() {
		MCR.assertParameterAsEqual("addCall1", 0, "param1", Integer.valueOf(1));
		MCR.assertParameterAsEqual("addCall3", 0, "param1", Long.valueOf(1));
		MCR.assertParameterAsEqual("addCall1", 0, "param2", String.valueOf("2"));
		MCR.assertParameterAsEqual("addCall3", 0, "param2", Long.valueOf(A_LONG_TO_BIG_FOR_INT));
	}

	@Test(expectedExceptions = AssertionError.class, expectedExceptionsMessageRegExp = ""
			+ "expected \\[45\\] but found \\[1\\]")
	public void testAssertParameterDifferentint() throws Exception {
		addCall1();
		MCR.assertParameter("addCall1", 0, "param1", 45);
	}

	@Test(expectedExceptions = AssertionError.class, expectedExceptionsMessageRegExp = ""
			+ "expected \\[45\\] but found \\[1\\]")
	public void testAssertParameterAsEqualDifferentint() throws Exception {
		addCall1();
		MCR.assertParameterAsEqual("addCall1", 0, "param1", 45);
	}

	@Test(expectedExceptions = AssertionError.class, expectedExceptionsMessageRegExp = ""
			+ "expected \\[45\\] but found \\[1\\]")
	public void testAssertParameterDifferentLong() throws Exception {
		addCall3();
		MCR.assertParameter("addCall3", 0, "param1", 45L);
	}

	@Test(expectedExceptions = AssertionError.class, expectedExceptionsMessageRegExp = ""
			+ "expected \\[45\\] but found \\[1\\]")
	public void testAssertParameterAsEqualDifferentLong() throws Exception {
		addCall3();
		MCR.assertParameterAsEqual("addCall3", 0, "param1", 45L);
	}

	@Test(expectedExceptions = AssertionError.class)
	public void testAssertParameterDifferentObject() throws Exception {
		addCall2();

		MCR.assertParameter("addCall2", 0, "param3", new Object());
	}

	@Test(expectedExceptions = AssertionError.class)
	public void testAssertParameterAsEqualDifferentObject() throws Exception {
		addCall2();

		MCR.assertParameterAsEqual("addCall2", 0, "param3", new Object());
	}

	@Test(expectedExceptions = AssertionError.class, expectedExceptionsMessageRegExp = ""
			+ "expected \\[ObjectOnlyForTest: 2\\] but found \\[ObjectOnlyForTest: 1\\]")
	public void testAssertParameterDifferentObjectButEqualsIsTrueShouldThrowException()
			throws Exception {
		addCall5();

		MCR.assertParameter("addCall5", 0, "param1", new ObjectOnlyForTest(2));
	}

	private void addCall5() {
		MCR.addCall("param1", new ObjectOnlyForTest(1));
	}

	@Test
	public void testAssertParameterAsEqualDifferentObjectButEqualsIsTrue() throws Exception {
		addObjectOnlyForTestCall();

		MCR.assertParameterAsEqual("addObjectOnlyForTestCall", 0, "param1",
				new ObjectOnlyForTest(2));
	}

	private void addObjectOnlyForTestCall() {
		MCR.addCall("param1", new ObjectOnlyForTest(1));
	}

	@Test
	public void testAssertParameterAsEqualList() throws Exception {
		addListCall();

		MCR.assertParameterAsEqual("addListCall", 0, "param1", List.of("item1", "item2"));
	}

	private void addListCall() {
		MCR.addCall("param1", List.of("item1", "item2"));
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

		MCRforTest.assertParameters("addCallForTest", 0, "value1", "value2");

		assertEquals(MCRforTest.assertParameterCallsCounter, 2);
		assertEquals(MCRforTest.expectedValue.get(0), "value1");
		assertEquals(MCRforTest.actualValue.get(0), "value1");
		assertEquals(MCRforTest.expectedValue.get(1), "value2");
		assertEquals(MCRforTest.actualValue.get(1), "value2");

	}

	private void addCallForTest() {
		MCRforTest.addCall("param1", "value1", "param2", "value2", "param3", "value3");
	}

	class MethodCallRecorderForTest extends MethodCallRecorder {

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

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "Too many values to compare for \\(methodName: addCall1, callNumber: 0\\)")
	public void testMakeSureAssertParametersDoesNotCallsAssertParameterMoreTimesThanEnteredParams()
			throws Exception {
		addCall1();

		MCR.assertParameters("addCall1", 0, 1, "2", "valueNotRecorded");

	}

	@Test
	public void testAssertParametersPositiveAssertDifferentTypes() throws Exception {
		addCall1();
		addCall2();

		MCR.assertParameters("addCall1", 0, 1, "2");
		MCR.assertParameters("addCall2", 0, 2, objectParameter);

	}

	@Test(expectedExceptions = AssertionError.class, expectedExceptionsMessageRegExp = ""
			+ "expected \\[3\\] but found \\[2\\]")
	public void testAssertParametersWrongValue() throws Exception {
		addCall1();
		addCall2();

		MCR.assertParameters("addCall1", 0, 1, "3");
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "CallNumber not found for \\(methodName: addCall4, callNumber: 1\\)")
	public void testAssertParametersOnlyOneParameter() throws Exception {
		addCall4();

		MCR.assertParameters("addCall4", 1, "onlyOne");
	}

	@Test
	public void testStoredValueIsNullCheckedNull() throws Exception {
		MCR.addCall("param1", null);

		String testValue = null;
		MCR.assertParameters("testStoredValueIsNullCheckedNull", 0, testValue);
	}

	@Test(expectedExceptions = AssertionError.class, expectedExceptionsMessageRegExp = ""
			+ "expected \\[not null\\] but found \\[null\\]")
	public void testStoredValueIsNullCheckedNotNull() throws Exception {
		MCR.addCall("param1", null);

		String testValue = "not null";
		MCR.assertParameters("testStoredValueIsNullCheckedNotNull", 0, testValue);
	}

	@Test(expectedExceptions = AssertionError.class, expectedExceptionsMessageRegExp = ""
			+ "expected \\[null\\] but found \\[not null\\]")
	public void testStoredValueIsNotNullCheckedNull() throws Exception {
		MCR.addCall("param1", "not null");

		String testValue = null;
		MCR.assertParameters("testStoredValueIsNotNullCheckedNull", 0, testValue);
	}

	@Test
	public void testAddReturn() throws Exception {
		String valueToReturn = "someValue";
		MCR.addReturned(valueToReturn);

		assertEquals(MCR.getReturnValue("testAddReturn", 0), "someValue");

	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "MethodName not found for \\(methodName: someMethod, callNumber: 0\\)")
	public void testAddReturnNoMethodFound() throws Exception {
		String valueToReturn = "someValue";
		MCR.addReturned(valueToReturn);

		assertEquals(MCR.getReturnValue("someMethod", 0), "someValue");
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "MethodName not found for \\(methodName: someMethod2, callNumber: 3\\)")
	public void testAddReturnNoMethodFound2() throws Exception {
		String valueToReturn = "someValue3";
		MCR.addReturned(valueToReturn);

		assertEquals(MCR.getReturnValue("someMethod2", 3), "someValue3");
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "CallNumber not found for \\(methodName: testAddReturnCallNumberNotFound, callNumber: 10\\)")
	public void testAddReturnCallNumberNotFound() throws Exception {
		String valueToReturn = "someValue";
		MCR.addReturned(valueToReturn);

		assertEquals(MCR.getReturnValue("testAddReturnCallNumberNotFound", 10), valueToReturn);
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "CallNumber not found for \\(methodName: testAddReturnCallNumberNotFound2, callNumber: 11\\)")
	public void testAddReturnCallNumberNotFound2() throws Exception {
		String valueToReturn = "someValue2";
		MCR.addReturned(valueToReturn);

		assertEquals(MCR.getReturnValue("testAddReturnCallNumberNotFound2", 11), valueToReturn);
	}

	@Test
	public void testAssertReturnPosiveAssertion() throws Exception {
		String valueToReturn = "someValue";
		MCR.addReturned(valueToReturn);

		MCR.assertReturn("testAssertReturnPosiveAssertion", 0, valueToReturn);
	}

	@Test(expectedExceptions = AssertionError.class, expectedExceptionsMessageRegExp = ""
			+ "expected \\[someOtherValue\\] but found \\[someValue\\]")
	public void testAssertReturnNegativeAssertion() throws Exception {
		MCR.addReturned("someValue");

		MCR.assertReturn("testAssertReturnNegativeAssertion", 0, "someOtherValue");
	}

	@Test
	public void testAddReturnedSeveralCalls() throws Exception {
		MCR.addReturned("someValue1");
		MCR.addReturned("someValue2");

		MCR.assertReturn("testAddReturnedSeveralCalls", 0, "someValue1");
		MCR.assertReturn("testAddReturnedSeveralCalls", 1, "someValue2");
	}

	@Test
	public void testNoMRVsetThrowsError() throws Exception {
		Exception caughtException = null;
		try {
			MCR.addCallAndReturnFromMRV("paramValue1");
			assertTrue(false);
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
		MRV.setReturnValues("useMRV", List.of("return1"), "paramValue1");

		MCR.useMRV(MRV);
		Object returnValue = MCR.addCallAndReturnFromMRV("paramName", "paramValue1");

		assertEquals(returnValue, "return1");
		MCR.assertMethodWasCalled("useMRV");
		MCR.assertReturn("useMRV", 0, "return1");
		assertEquals(returnValue, "return1");
	}

	@Test
	public void useMRVWithTwoParameters() throws Exception {
		MethodReturnValues MRV = new MethodReturnValues();
		MRV.setReturnValues("useMRVWithTwoParameters", List.of("return1"), "paramValue1",
				"paramValue2");

		MCR.useMRV(MRV);
		Object returnValue = MCR.addCallAndReturnFromMRV("paramName", "paramValue1", "paramName2",
				"paramValue2");

		assertEquals(returnValue, "return1");
		MCR.assertMethodWasCalled("useMRVWithTwoParameters");
		MCR.assertReturn("useMRVWithTwoParameters", 0, "return1");
		assertEquals(returnValue, "return1");
	}

	@Test
	public void testOnlyForTestGetMRV() throws Exception {
		MethodReturnValues MRV = new MethodReturnValues();
		MCR.useMRV(MRV);

		assertSame(MCR.onlyForTestGetMRV(), MRV);
	}
}
