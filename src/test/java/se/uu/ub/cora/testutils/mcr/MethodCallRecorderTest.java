package se.uu.ub.cora.testutils.mcr;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MethodCallRecorderTest {

	MethodCallRecorder MCR;
	private Object objectParameter = new Object();

	@BeforeMethod
	public void beforeMethod() {
		MCR = new MethodCallRecorder();
		Assert as;
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
			+ "Value not found for methodName: methodName, callNumber: 0 and parameterName: parameterName")
	public void testGetValueForMethodNameAndCallNumberAndParameterNameNoMatch() throws Exception {
		MCR.getValueForMethodNameAndCallNumberAndParameterName("methodName", 0, "parameterName");
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "Value not found for methodName: addCall, callNumber: 10 and parameterName: parameterName")
	public void testGetValueForMethodNameAndCallNumberAndParameterNameCallNumberNoMatch()
			throws Exception {
		addCall1();

		MCR.getValueForMethodNameAndCallNumberAndParameterName("addCall", 10, "parameterName");
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "Value not found for methodName: addCall, callNumber: 0 and parameterName: parameterName")
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
}
