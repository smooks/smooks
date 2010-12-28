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
package org.milyn.ect;

import org.milyn.edisax.model.internal.Edimap;

/**
 * EdimapConfiguration
 * @author bardl
 */
public class EdimapConfiguration {
    private Edimap edimap;
    private String filename;

    public EdimapConfiguration(Edimap edimap, String filename) {
        this.edimap = edimap;
        this.filename = filename;
    }

    public Edimap getEdimap() {
        return edimap;
    }

    public void setEdimap(Edimap edimap) {
        this.edimap = edimap;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
