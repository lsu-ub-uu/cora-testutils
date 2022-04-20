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
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class NameValuesTest {
	private NameValues nameValues;
	private NameValues nameValues2;
	private NameValues nameValues3;
	private NameValues nameValues4;
	private NameValues nameValues5;

	@BeforeMethod
	public void beforeMethod() {
		nameValues = new NameValues("someMethod", "one", "two");
		nameValues2 = new NameValues("someMethod", "one", "two");
		nameValues3 = new NameValues("someOtherMethod", "one", "two");
		nameValues4 = new NameValues("someMethod", "two", "one");
		nameValues5 = new NameValues("someMethod", "one", "two", "two");
	}

	@Test
	public void testHashCode() throws Exception {
		assertEquals(nameValues.hashCode(), nameValues2.hashCode());
	}

	@Test
	public void testEquals() throws Exception {
		assertFalse(nameValues.equals(null));
		assertFalse(nameValues.equals(new Object()));
		assertFalse(nameValues.equals(nameValues3));
		assertFalse(nameValues.equals(nameValues4));
		assertFalse(nameValues.equals(nameValues5));
		assertTrue(nameValues.equals(nameValues));
		assertTrue(nameValues.equals(nameValues2));
	}
}
