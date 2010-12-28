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
package org.milyn.smooks.edi.unedifact.model.r41.types;

import java.io.Serializable;

/**
 * Identifier.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public abstract class Identifier implements Serializable {

	private static final long serialVersionUID = 1L;

	private String id;
	private String versionNum;
	private String releaseNum;

	public String getId() {
		return id;
	}

    public void setId(String id) {
		this.id = id;
	}

    public String getVersionNum() {
		return versionNum;
	}

    public void setVersionNum(String versionNum) {
		this.versionNum = versionNum;
	}

    public String getReleaseNum() {
		return releaseNum;
	}

    public void setReleaseNum(String releaseNum) {
		this.releaseNum = releaseNum;
	}
}
