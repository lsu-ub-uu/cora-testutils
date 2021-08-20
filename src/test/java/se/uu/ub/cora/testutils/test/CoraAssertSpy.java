package se.uu.ub.cora.testutils.test;

public class CoraAssertSpy implements CoraAssert {

	public int assertEqualsActual = 0;
	public int assertEqualsExpected = 0;

	@Override
	public void assertEquals(int actual, int expected) {
		assertEqualsActual = actual;
		assertEqualsExpected = expected;
	}

}
