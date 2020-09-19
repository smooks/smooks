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
package org.smooks.cdr;

import org.smooks.profile.ProfileSet;

import java.util.Comparator;


/**
 * Sort Comparator for {@link org.smooks.cdr.SmooksResourceConfiguration} Objects based on their "specificity".
 * <p/>
 * Before reading this be sure to read the {@link org.smooks.cdr.SmooksResourceConfiguration} class Javadoc.
 * <p/>
 * As Smooks applies {@link org.smooks.delivery.ContentHandler}s ({@link org.smooks.delivery.dom.DOMElementVisitor DOMElementVisitors} and
 * {@link org.smooks.delivery.dom.serialize.SerializationUnit SerializationUnits}) it may discover that in a given case more than 1 {@link org.smooks.delivery.ContentHandler}
 * can be applied.  How does Smooks decide on the order in which these {@link org.smooks.delivery.ContentHandler}s are to be applied to the content?
 * <p/>
 * At the moment, Smooks uses this class to calculate a "specificity" rating for each Content Delivery Resource based on its 
 * {@link org.smooks.cdr.SmooksResourceConfiguration &lt;smooks-resource&gt;} configuration, and sorts them in decreasing order of specificity.
 * <p/>
 * The following outlines how this specificity value is calculated at present.
 * <!-- Just cut-n-paste from the code -->
 * <pre>
    // Get the combined specificity of all the profile targeting expressions.
	{@link org.smooks.cdr.ProfileTargetingExpression}[] profileTargetingExpressions = resourceConfig.{@link org.smooks.cdr.SmooksResourceConfiguration#getProfileTargetingExpressions() getProfileTargetingExpressions()};
	for(int i = 0; i < profileTargetingExpressions.length; i++) {
		specificity += profileTargetingExpressions[i].{@link org.smooks.cdr.ProfileTargetingExpression#getSpecificity(org.smooks.profile.ProfileSet) getSpecificity(profileSet)};
	}
	
	// Check the 'selector' attribute value.
	if(resourceConfig.{@link org.smooks.cdr.SmooksResourceConfiguration#isXmlDef() isXmlDef()}) {
		specificity += 10;
	} else if(resourceConfig.{@link org.smooks.cdr.SmooksResourceConfiguration#getSelector() getselector()}.equals("*")) {
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
	if(resourceConfig.{@link org.smooks.cdr.SmooksResourceConfiguration#getSelectorNamespaceURI() getSelectorNamespaceURI()} != null) {
		specificity += 10;
	}</pre>  
 * For more details on this please refer to the code in this class.
 * 
 * @author tfennelly
 */

public class SmooksResourceConfigurationSortComparator implements Comparator {

	/**
	 * Profile set.
	 */
	private final ProfileSet profileSet;

	/**
	 * Private constructor.
	 * @param profileSet Profile set used to evaluate specificity.
	 */
	public SmooksResourceConfigurationSortComparator(ProfileSet profileSet) {
		this.profileSet = profileSet;
	}
	
	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object configObj1, Object configObj2) {
		SmooksResourceConfiguration config1 = (SmooksResourceConfiguration)configObj1;
		SmooksResourceConfiguration config = (SmooksResourceConfiguration)configObj2;

		if(config1 == config) {
			return 0;
		}
		
		double config1Specificity = getSpecificity(config1);
		double config2Specificity = getSpecificity(config);				
		
		// They are ordered as follow (most specific first). 
		if(config1Specificity > config2Specificity) {
			return -1;
		} else if(config1Specificity < config2Specificity) {
			return 1;
		} else {
			return 0;
		}
	}
	
	/**
	 * Get the specificity of the SmooksResourceConfiguration.
	 * <p/>
	 * The "specificity" is evaluated based on the selector and target-profile values.
	 * @param resourceConfig Resource configuration.
	 * @return Configuration specificity.
	 */
	protected double getSpecificity(SmooksResourceConfiguration resourceConfig) {
		double specificity = 0;
		
		// If the following code is modified, please update the class Javadoc.

		// Get the combined specificity of all the profile targeting expressions.
		ProfileTargetingExpression[] profileTargetingExpressions = resourceConfig.getProfileTargetingExpressions();
		for(int i = 0; i < profileTargetingExpressions.length; i++) {
			specificity += profileTargetingExpressions[i].getSpecificity(profileSet);
		}
		
		// Check the 'selector' attribute value.
		if(resourceConfig.isXmlDef()) {
			specificity += 10;
		} else if(resourceConfig.getSelectorPath().getSelector().equals("*")) {
			specificity += 5;
		} else {
			// Explicit selector listed
			specificity += 100;
            
			// If the selector is contextual it's, therefore more specific so
			// account for that.  Subtract 1 because that "1" is already accounted
			// for by the addition of 100 - it's the extra we're accounting for here...
			if(resourceConfig.getSelectorPath().size() > 1) {
			    int contextSpecificity = resourceConfig.getSelectorPath().size();
			    specificity += (10 * (contextSpecificity - 1));
			}
		}
		
		// Check the 'namespace' attribute.
		if(resourceConfig.getSelectorPath().getSelectorNamespaceURI() != null) {
			specificity += 10;
		}
		
		return specificity;
	}
}
