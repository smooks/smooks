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

package org.milyn.container.standalone;


import java.io.IOException;

import org.milyn.Smooks;
import org.milyn.SmooksUtil;
import org.milyn.profile.DefaultProfileSet;
import org.xml.sax.SAXException;

public class PreconfiguredSmooks extends Smooks {

	/**
	 * Public Constructor.
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public PreconfiguredSmooks() throws SAXException, IOException {
        SmooksUtil.registerProfileSet(DefaultProfileSet.create("msie6w", new String[] {"msie6", "html4", "html"}), this);
        SmooksUtil.registerProfileSet(DefaultProfileSet.create("msie6m", new String[] {"msie6", "html4", "html"}), this);
        SmooksUtil.registerProfileSet(DefaultProfileSet.create("msie6", new String[] {"html4", "html"}), this);
        SmooksUtil.registerProfileSet(DefaultProfileSet.create("firefox", new String[] {"html4", "html"}), this);

        addConfigurations("/cdr/parameters.cdrl", getClass().getResourceAsStream("/cdr/parameters.cdrl"));
        addConfigurations("/cdr/test.cdrl", getClass().getResourceAsStream("/cdr/test.cdrl"));
	}

}
