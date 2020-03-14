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
package org.smooks.delivery.sax.terminate;

import org.smooks.SmooksException;
import org.smooks.cdr.annotation.ConfigParam;
import org.smooks.cdr.annotation.ConfigParam.Use;
import org.smooks.container.ExecutionContext;
import org.smooks.delivery.ordering.Producer;
import org.smooks.delivery.sax.SAXElement;
import org.smooks.delivery.sax.SAXVisitAfter;
import org.smooks.delivery.sax.SAXVisitBefore;
import org.smooks.util.CollectionsUtil;

import java.io.IOException;
import java.util.Set;

/**
 * Terminate Visitor.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class TerminateVisitor implements SAXVisitBefore, SAXVisitAfter, Producer {

	private boolean terminateBefore = false;

	/* (non-Javadoc)
	 * @see org.smooks.delivery.sax.SAXVisitBefore#visitBefore(org.smooks.delivery.sax.SAXElement, org.smooks.container.ExecutionContext)
	 */
	public void visitBefore(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
		if(terminateBefore) {
			throw new TerminateException(element, true);
		}
	}

	/* (non-Javadoc)
	 * @see org.smooks.delivery.sax.SAXVisitAfter#visitAfter(org.smooks.delivery.sax.SAXElement, org.smooks.container.ExecutionContext)
	 */
	public void visitAfter(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
		if(!terminateBefore) {
			throw new TerminateException(element, false);
		}
	}

	/**
	 * @param terminateBefore the terminateBefore to set
	 */
	@SuppressWarnings("WeakerAccess")
	@ConfigParam(use = Use.OPTIONAL)
	public TerminateVisitor setTerminateBefore(boolean terminateBefore) {
		this.terminateBefore = terminateBefore;
		return this;
	}

	/* (non-Javadoc)
	 * @see org.smooks.delivery.ordering.Producer#getProducts()
	 */
	@SuppressWarnings("unchecked")
	public Set<?> getProducts() {
		// Doesn't actually produce anything.  Just using the Producer/Consumer mechanism to
		// force this vistor to the top of the visitor apply list.
		return CollectionsUtil.toSet();
	}
}
