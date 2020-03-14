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

package org.smooks.delivery.dom;

import org.smooks.cdr.SmooksResourceConfiguration;
import org.smooks.delivery.ContentHandlerConfigMap;

import java.util.ArrayList;
import java.util.List;

/**
 * Processing set.
 * <p/>
 * The set of ProcessingUnit to be applied to an Element.
 * @author tfennelly
 */
@SuppressWarnings("unused")
public class ProcessingSet {

	/**
	 * ProcessingUnit instances.
	 */
	private List<ContentHandlerConfigMap> processingUnits = new ArrayList<ContentHandlerConfigMap>();

	/**
	 * Add to the ProcessingSet.
	 * @param processingUnit The Processing Unit to be added.
	 * @param resourceConfig Corresponding resource config.
	 */
	@SuppressWarnings("unchecked")
	public void addProcessingUnit(DOMElementVisitor processingUnit, SmooksResourceConfiguration resourceConfig) {
        ContentHandlerConfigMap mapInst =
            new ContentHandlerConfigMap(processingUnit, resourceConfig);

        processingUnits.add(mapInst);
	}

	/**
	 * Get the list of ProcessingUnit instances to be applied.
	 * @return List of ProcessingUnit instances.
	 */
	public List<ContentHandlerConfigMap> getProcessingUnits() {
		return processingUnits;
	}
}
