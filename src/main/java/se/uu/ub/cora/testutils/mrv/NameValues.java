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

import java.util.Objects;

class NameValues {
	private String methodName;
	private Object[] parameterValues;

	public NameValues(String methodName, Object... parameterValues) {
		this.methodName = methodName;
		this.parameterValues = parameterValues;
	}

	@Override
	public boolean equals(Object obj) {
		if (null == obj) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		NameValues nameValues = (NameValues) obj;
		return Objects.equals(methodName, nameValues.methodName)
				&& parametersTheSame(nameValues.parameterValues);
	}

	private boolean parametersTheSame(Object[] pValues) {
		if (parameterValues.length != pValues.length) {
			return false;
		}
		int no = 0;
		for (Object parameterValue : parameterValues) {
			if (!parameterValue.equals(pValues[no])) {
				return false;
			}
			no++;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int parametersHash = 0;
		for (Object parameterValue : parameterValues) {
			parametersHash += Objects.hash(parameterValue);
		}
		return Objects.hash(methodName, parametersHash);
	}
}
