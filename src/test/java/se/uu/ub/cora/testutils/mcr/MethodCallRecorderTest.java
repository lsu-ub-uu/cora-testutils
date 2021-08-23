package se.uu.ub.cora.testutils.mcr;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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

	@Test
	public void testAddCallWithParametersNoMatch() throws Exception {
		assertEquals(MCR.getParametersForMethodAndCallNumber("NoMethod", 0).size(), 0);
	}

	@Test
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
			+ "CallNumber not found for \\(methodName: addCall1, callNumber: 10 and parameterName: someParameterName\\)")
	public void testAssertParameterNotFoundCallNumber() throws Exception {
		addCall1();

		MCR.assertParameter("addCall1", 10, "someParameterName", "expectedValue");
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "ParameterName not found for \\(methodName: addCall1, callNumber: 0 and parameterName: someParameterName\\)")
	public void testAssertParameterNotFoundParamName() throws Exception {
		addCall1();

		MCR.assertParameter("addCall1", 0, "someParameterName", "expectedValue");
	}

	@Test(expectedExceptions = AssertionError.class, expectedExceptionsMessageRegExp = ""
			+ "expected \\[100\\] but found \\[1\\]")
	public void testAssertParameterValueNotEqual() throws Exception {
		addCall1();

		MCR.assertParameter("addCall1", 0, "param1", 100);
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "expected value type is class java.lang.String but found class java.lang.Integer")
	public void testAssertParameterValueDifferentTypes() throws Exception {
		addCall1();

		MCR.assertParameter("addCall1", 0, "param1", "1");
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "expected value type is class java.lang.Integer but found class java.lang.String")
	public void testAssertParameterValueDifferentTypesDynamicErrorMessage() throws Exception {
		addCall1();

		MCR.assertParameter("addCall1", 0, "param2", 2);
	}

	@Test
	public void testAssertParameterEqualValues() throws Exception {
		addCall1();
		addCall2();
		addCall3();

		MCR.assertParameter("addCall1", 0, "param1", 1);
		assertParametersForPrimitivesMadeSureTheyAreDifferentObjectsXvalueOfCreatesSameObject();
		MCR.assertParameter("addCall2", 0, "param3", objectParameter);
		MCR.assertParameter("addCall3", 0, "param1", 1L);
	}

	private void assertParametersForPrimitivesMadeSureTheyAreDifferentObjectsXvalueOfCreatesSameObject() {
		MCR.assertParameter("addCall1", 0, "param1", new Integer(1));
		MCR.assertParameter("addCall3", 0, "param1", new Long(1));
		MCR.assertParameter("addCall1", 0, "param2", new String("2"));
		MCR.assertParameter("addCall3", 0, "param2", new Long(A_LONG_TO_BIG_FOR_INT));
	}

	// @Test
	// public void testName() throws Exception {
	// Object o1 = 1;
	// // Object o2 = Integer.valueOf(1);
	// Object o2 = new Integer(1);
	//
	// assertSame(o1, o2, "");
	// }

	@Test(expectedExceptions = AssertionError.class, expectedExceptionsMessageRegExp = ""
			+ "expected \\[45\\] but found \\[1\\]")
	public void testAssertParameterDifferentint() throws Exception {
		addCall1();
		MCR.assertParameter("addCall1", 0, "param1", 45);
	}

	@Test(expectedExceptions = AssertionError.class, expectedExceptionsMessageRegExp = ""
			+ "expected \\[45\\] but found \\[1\\]")
	public void testAssertParameterDifferentLong() throws Exception {
		addCall3();
		MCR.assertParameter("addCall3", 0, "param1", 45L);
	}

	@Test(expectedExceptions = AssertionError.class)
	public void testAssertParameterDifferentObject() throws Exception {
		addCall2();

		MCR.assertParameter("addCall2", 0, "param3", new Object());
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
			+ "Too many values to compare for \\(methodName: addCallForTest, callNumber: 0\\)")
	public void testMakeSureAssertParametersDoesNotCallsAssertParameterMoreTimesThanEnteredParams()
			throws Exception {
		addCall1();

		MCR.assertParameters("addCallForTest", 0, "value1", "value2", "value3", "value4");

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
			+ "CallNumber not found for \\(methodName: someMethod, callNumber: 0\\)")
	public void testAddReturnCallNumberNotFound() throws Exception {
		String valueToReturn = "someValue";
		MCR.addReturned(valueToReturn);

		assertEquals(MCR.getReturnValue("testAddReturnCallNumberNotFound", 10), valueToReturn);

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
}
