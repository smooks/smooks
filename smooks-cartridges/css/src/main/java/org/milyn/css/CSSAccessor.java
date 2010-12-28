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

package org.milyn.css;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.milyn.css.StyleSheetStore.StoreEntry;
import org.milyn.container.ExecutionContext;
import org.milyn.magger.CSSParser;
import org.milyn.magger.CSSProperty;
import org.milyn.magger.CSSRule;
import org.milyn.magger.CSSStylesheet;
import org.milyn.profile.ProfileSet;
import org.w3c.css.sac.SACMediaList;
import org.w3c.dom.Element;

/**
 * Page CSS accessor class.
 * <p/>
 * Transformation Units use this class to access CSS information for the 
 * current page.  The CSS info is "pre-gathered" by the {@link org.milyn.css.CSSStyleScraper}
 * Assembly Unit, if configured for the requesting device.
 * <p/>
 * This class is instanciated via the {@link #getInstance(ExecutionContext)} factory method.
 * @author tfennelly
 */
public class CSSAccessor {

	/**
	 * Logger.
	 */
	private static Log logger = LogFactory.getLog(CSSAccessor.class);
	/**
	 * Request stylesheet store.
	 */
	private StyleSheetStore stylesheetStore;
	/**
	 * Associated container request.
	 */
	private ExecutionContext request;
	/**
	 * CSSAccessor request key.
	 */
	private static final String REQUEST_REQUESTKEY = CSSAccessor.class + "#request";
	/**
	 * Element level style cache key.
	 */
	private static final String ELEMENT_STYLE_DECL_REQUESTKEY = CSSAccessor.class + "#elementStyleDecls";
	
	/**
	 * Public constructor.
	 * @param request The container request associated with the current page 
	 * being delivered.
	 */
	private CSSAccessor(ExecutionContext request) {
		if(request == null) {
			throw new IllegalArgumentException("null 'request' arg in constructor call.");
		}
		this.request = request;
		stylesheetStore = StyleSheetStore.getStore(request);
	}
	
	/**
	 * CSSAccessor Factory method.
	 * @param request The container request associated with the current page 
	 * being delivered.
	 * @return The CSSAccessor associated with the supplied request.
	 */
	public static CSSAccessor getInstance(ExecutionContext request) {
		CSSAccessor accessor = (CSSAccessor)request.getAttribute(REQUEST_REQUESTKEY);
		
		if(accessor == null) {
			accessor = new CSSAccessor(request);
			request.setAttribute(REQUEST_REQUESTKEY, accessor);
		}
		
		return accessor;
	}

	/**
	 * Get the named CSS property for the supplied DOM element, if one exists.
	 * <p/>
	 * Cross references the rules media list (if specified) against the
	 * requesting browser/device name and profile.
	 * @param domElement The DOM element.
	 * @param propertyName The required CSS property.
	 * @return The {@link CSSProperty} associated with the named CSS property for the supplied element,
	 * if a value for this property is defined for this element, otherwise null. 
	 */
	public CSSProperty getProperty(Element domElement, String propertyName) {
		String internPropertyName = propertyName.intern();
		List elementStyleRules = getElementStyleRules(domElement);
		
		// if the element defines the property, return this value.
		if(elementStyleRules != null) {
			for(int i = 0; i < elementStyleRules.size(); i++) {
				CSSRule elementRule = (CSSRule)elementStyleRules.get(i);
				if(elementRule.getProperty().getName() == internPropertyName) {
					return elementRule.getProperty();
				}
			}
		}
		
		// Iterate over the StyleSheets
		Iterator ssIterator = stylesheetStore.iterator();
		CSSRule matchingRule = null;
		while(ssIterator.hasNext()) {
			StoreEntry storeEntry = (StoreEntry)ssIterator.next();
			CSSStylesheet styleSheet = storeEntry.getStylesheet();
			List rules = styleSheet.getRules();
			int ruleCount = rules.size();
			
			// Iterate over the rules
			for(int i = 0; i < ruleCount; i++) {
				CSSRule nextRule = (CSSRule)rules.get(i);
				CSSProperty nextProperty = nextRule.getProperty();
				
				// If it's an instance of the property we're looking for and it's
				// a match for the element in question....
				//
				// Note we're not performing a String.equals because magger
				// interns all the property names, therefore allowing us to
				// perform a direct ref comparison.
				if(nextProperty.getName() == internPropertyName && 
						nextRule.getSelector().match(domElement, null) &&
						isForDevice(nextRule)) {
					
					if(matchingRule == null) {
						matchingRule = nextRule;
					} else if(nextRule.getSelector().getSpecificity() >= matchingRule.getSelector().getSpecificity()) {
						// nextRule's selector is more specific
						matchingRule = nextRule;
					}
				}
			}
		}
		
		return (matchingRule != null?matchingRule.getProperty():null);
	}

	/**
	 * Is the supplied rule targeted at the requesting device.
	 * <p/>
	 * Performs a check of the rules media list against the devices
	 * name or profileset. 
	 * @param rule CSS Rule.
	 * @return True if the rule is targeted at the device, otherwise
	 * false.
	 */
	private boolean isForDevice(CSSRule rule) {
		SACMediaList targetMediaList = rule.getMediaList();
		
		if(targetMediaList == null || targetMediaList.getLength() == 0) {
			return true;
		}
		
		ProfileSet profileSet = request.getTargetProfiles();
		int listLen = targetMediaList.getLength();
		for(int i = 0; i < listLen; i++) {
			String media = targetMediaList.item(i);
			if(profileSet.isMember(media)) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Parse and return the elements style declaration.
	 * @param domElement The element.
	 * @return The elements style in a StyleDeclaration, or null if no style defined.
	 */
	private List getElementStyleRules(Element domElement) {
		List rules = null;
		String styleAttrib = domElement.getAttribute("style");
		
		if(styleAttrib != null) {
			Hashtable elementStyleDecls = getElementStyleDeclCache(request);
			
			rules = (List)elementStyleDecls.get(domElement);
			if(rules == null) {
				CSSParser cssParser = new CSSParser(request.getContext().getResourceLocator());
				
				try {
					String styleToParse = domElement.getTagName() + " {" + styleAttrib + "}";
					CSSStylesheet styleSheet = cssParser.parse(styleToParse, request.getDocumentSource(), null);
					rules = styleSheet.getRules();
					// and parsed rules to the cache
					elementStyleDecls.put(domElement, rules);
				} catch(Throwable throwable) {
					logger.warn("Unable to parse element CSS: " + styleAttrib, throwable);
				}
			}
		}
		return rules;
	}

	private Hashtable getElementStyleDeclCache(ExecutionContext request2) {
		Hashtable elementStyleDecls = (Hashtable)request.getAttribute(ELEMENT_STYLE_DECL_REQUESTKEY);
		if(elementStyleDecls == null) {
			elementStyleDecls = new Hashtable();
			request.setAttribute(ELEMENT_STYLE_DECL_REQUESTKEY, elementStyleDecls);
		}
		return elementStyleDecls;
	}
}
