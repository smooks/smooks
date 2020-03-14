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
package org.smooks.delivery;

import javax.xml.transform.Result;
import javax.xml.transform.Source;

import org.smooks.SmooksException;
import org.smooks.container.ExecutionContext;

/**
 * Filter bypass interface.
 * <p/>
 * In some cases, the Smooks fragment filtering process (SAX/DOM) can be bypassed
 * if there is just a single visitor resource applied to the <i>#document</i>
 * fragment.  This interface allows a visitor to mark itself as such a visitor.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public interface FilterBypass {

	/**
	 * Bypass the Smooks Filter process.
	 * <p/>
	 * If the Filter bypass was not applied, the normal Smooks Fragment Filtering
	 * process will be proceed.
	 * 
	 * @param executionContext Smooks execution context.
	 * @param source Filter Source.
	 * @param result Filter Result.
	 * @return True of the bypass was applied, otherwise false.
	 * @throws SmooksException An error occurred while apply the bypass transform.
	 */
	boolean bypass(ExecutionContext executionContext, Source source, Result result) throws SmooksException;
}
