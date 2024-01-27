/*-
 * ========================LICENSE_START=================================
 * API
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
package org.smooks.api.resource.config;

import org.smooks.api.delivery.ContentHandler;
import org.smooks.api.resource.visitor.dom.DOMElementVisitor;
import org.smooks.api.resource.visitor.SerializerVisitor;

import java.util.Comparator;


/**
 * Sort Comparator for {@link ResourceConfig} Objects based on their "specificity".
 * <p/>
 * Before reading this be sure to read the {@link ResourceConfig} class Javadoc.
 * <p/>
 * As Smooks applies {@link ContentHandler}s ({@link DOMElementVisitor DOMElementVisitors} and
 * {@link SerializerVisitor SerializationUnits}) it may discover that in a given case more than 1 {@link ContentHandler}
 * can be applied.  How does Smooks decide on the order in which these {@link ContentHandler}s are to be applied to the content?
 * <p/>
 * At the moment, Smooks uses this class to calculate a "specificity" rating for each Content Delivery Resource based on its 
 * {@link ResourceConfig &lt;smooks-resource&gt;} configuration, and sorts them in decreasing order of specificity.
 * <p/>
 * The following outlines how this specificity value is calculated at present.
 * <!-- Just cut-n-paste from the code -->
 * <pre>
    // Get the combined specificity of all the profile targeting expressions.
	{@link ProfileTargetingExpression}[] profileTargetingExpressions = resourceConfig.{@link ResourceConfig#getProfileTargetingExpressions() getProfileTargetingExpressions()};
	for(int i = 0; i < profileTargetingExpressions.length; i++) {
		specificity += profileTargetingExpressions[i].{@link ProfileTargetingExpression#getSpecificity(org.smooks.api.profile.ProfileSet) getSpecificity(profileSet)};
	}
	
	// Check the 'selector' attribute value.
	if (resourceConfig.{@link ResourceConfig#getSelector() getselector()}.equals("*")) {
		specificity += 5;
	} else {
		// Explicit selector listed
		specificity += 100;
            
        // If the selector is contextual it's, therefore more specific so
        // account for that.  Subtract 1 because that "1" is already accounted
        // for by the addition of 100 - it's the extra we're accounting for here...
        if(resourceConfig.isSelectorContextual()) {
            int contextSpecificity = resourceConfig.getContextualSelector().length;
            specificity += (10 * (contextSpecificity - 1));
        }
	}
		
	// Check the 'namespace' attribute.
	if(resourceConfig.{@link ResourceConfig#getSelectorNamespaceURI() getSelectorNamespaceURI()} != null) {
		specificity += 10;
	}</pre>  
 * For more details on this please refer to the code in this class.
 * 
 * @author tfennelly
 */

public interface ResourceConfigSortComparator extends Comparator<ResourceConfig> {

	
}
