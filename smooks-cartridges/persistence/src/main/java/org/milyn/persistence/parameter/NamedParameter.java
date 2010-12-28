/*
	Milyn - Copyright (C) 2006 - 2010

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License (version 2.1) as published by the Free Software
	Foundation.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

	See the GNU Lesser General Public License for more details:
	http://www.gnu.org/licenses/lgpl.txt
*/
package org.milyn.persistence.parameter;

import org.apache.commons.lang.builder.HashCodeBuilder;



/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class NamedParameter implements Parameter<NamedParameterIndex> {

	private final int index;

	private final String name;

	private final NamedParameterIndex containerIndex;

	private int hashCode;

	protected NamedParameter(NamedParameterIndex repositoryIndex, int index, String name) {
		this.containerIndex = repositoryIndex;
		this.index = index;
		this.name = name;

		hashCode = new HashCodeBuilder()
						.append(index)
						.append(containerIndex)
						.toHashCode();
	}

	public int getIndex() {
		return index;
	}

	public String getName() {
		return name;
	}


	public NamedParameterIndex getContainerIndex() {
		return containerIndex;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return hashCode;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}

		if(obj instanceof NamedParameter == false) {
			return false;
		}
		NamedParameter rhs = (NamedParameter) obj;
		if(hashCode != hashCode) {
			return false;
		} else if(index != rhs.index) {
			return false;
		} else if(containerIndex.equals(rhs.containerIndex)) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return name;
	}

}
