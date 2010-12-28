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

package org.milyn.cdr;

import java.util.Comparator;

import org.milyn.profile.ProfileSet;


/**
 * Sort Comparator for {@link org.milyn.cdr.SmooksResourceConfiguration} Objects based on their "specificity".
 * <p/>
 * Before reading this be sure to read the {@link org.milyn.cdr.SmooksResourceConfiguration} class Javadoc.
 * <p/>
 * As Smooks applies {@link org.milyn.delivery.ContentHandler}s ({@link org.milyn.delivery.dom.DOMElementVisitor DOMElementVisitors} and
 * {@link org.milyn.delivery.dom.serialize.SerializationUnit SerializationUnits}) it may discover that in a given case more than 1 {@link org.milyn.delivery.ContentHandler}
 * can be applied.  How does Smooks decide on the order in which these {@link org.milyn.delivery.ContentHandler}s are to be applied to the content?
 * <p/>
 * At the moment, Smooks uses this class to calculate a "specificity" rating for each Content Delivery Resource based on its 
 * {@link org.milyn.cdr.SmooksResourceConfiguration &lt;smooks-resource&gt;} configuration, and sorts them in decreasing order of specificity.
 * <p/>
 * The following outlines how this specificity value is calculated at present.
 * <!-- Just cut-n-paste from the code -->
 * <pre>
    // Get the combined specificity of all the profile targeting expressions.
	{@link org.milyn.cdr.ProfileTargetingExpression}[] profileTargetingExpressions = resourceConfig.{@link org.milyn.cdr.SmooksResourceConfiguration#getProfileTargetingExpressions() getProfileTargetingExpressions()};
	for(int i = 0; i < profileTargetingExpressions.length; i++) {
		specificity += profileTargetingExpressions[i].{@link org.milyn.cdr.ProfileTargetingExpression#getSpecificity(org.milyn.profile.ProfileSet) getSpecificity(profileSet)};
	}
	
	// Check the 'selector' attribute value.
	if(resourceConfig.{@link org.milyn.cdr.SmooksResourceConfiguration#isXmlDef() isXmlDef()}) {
		specificity += 10;
	} else if(resourceConfig.{@link org.milyn.cdr.SmooksResourceConfiguration#getSelector() getselector()}.equals("*")) {
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
	if(resourceConfig.{@link org.milyn.cdr.SmooksResourceConfiguration#getSelectorNamespaceURI() getSelectorNamespaceURI()} != null) {
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
	private ProfileSet profileSet;

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
		} else if(resourceConfig.getSelector().equals("*")) {
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
		if(resourceConfig.getSelectorNamespaceURI() != null) {
			specificity += 10;
		}
		
		return specificity;
	}
}