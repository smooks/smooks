/*-
 * ========================LICENSE_START=================================
 * Smooks Core
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0, or
 * the GNU Lesser General Public License version 3.0 or later.
 * 
 * SPDX-License-Identifier: Apache-2.0 OR LGPL-3.0-or-later
 * 
 * ======================================================================
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ======================================================================
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * =========================LICENSE_END==================================
 */
package org.smooks.delivery.sax.terminate;

import org.smooks.SmooksException;
import org.smooks.container.ExecutionContext;
import org.smooks.delivery.ordering.Producer;
import org.smooks.delivery.sax.SAXElement;
import org.smooks.delivery.sax.SAXVisitAfter;
import org.smooks.delivery.sax.SAXVisitBefore;
import org.smooks.util.CollectionsUtil;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Optional;
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
	@Inject
	public TerminateVisitor setTerminateBefore(Optional<Boolean> terminateBefore) {
		this.terminateBefore = terminateBefore.orElse(this.terminateBefore);
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
