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

package org.milyn.servlet.http;

import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.delivery.ContentHandler;

/**
 * The HeaderAction class performs a HTTP header operation on the
 * content delivery response. 
 * @author tfennelly
 */
public class HeaderAction implements ContentHandler {
	/**
	 * Add action param value def.
	 */
	public static final int ACTION_ADD = 0;
	/**
	 * Remove action param value def.
	 */
	public static final int ACTION_REMOVE = 1;
	/**
	 * Action for this instance.
	 */
    private int action;
	/**
	 * Action target header name.
	 */
	private String headerName;
	/**
	 * Action target header value.
	 */
	private String headerValue;
	
	/**
	 * Public constructor.
	 * @param resourceConfig action smooks-resource instance.
	 */
	public void setConfiguration(SmooksResourceConfiguration resourceConfig) {
		if(resourceConfig == null) {
			IllegalStateException state = new IllegalStateException("Bad HeaderAction defintion.");
			state.initCause(new IllegalArgumentException("null 'unitDef' arg in constructor call."));
			throw state;
		}
		
		String actionParam = resourceConfig.getStringParameter("action");
		if(actionParam == null) {
			throw new IllegalStateException("'action' param not defined on HeaderAction SmooksResourceConfiguration.");
		}
		actionParam = actionParam.trim().toLowerCase();
		if(actionParam.equals("add")) {
			action = ACTION_ADD;
		} else if(actionParam.equals("remove")) {
			action = ACTION_REMOVE;
		} else {
			throw new IllegalStateException("Unsupported header action '" + actionParam + "' defined on HeaderAction SmooksResourceConfiguration.");
		}
		
		headerName = resourceConfig.getStringParameter("header-name");
		if(headerName == null) {
			throw new IllegalStateException("'header-name' param not defined on HeaderAction SmooksResourceConfiguration.");
		}
		
		headerValue = resourceConfig.getStringParameter("header-value");
		if(action == ACTION_ADD && headerValue == null) {
			throw new IllegalStateException("'header-value' param not defined on 'add' HeaderAction SmooksResourceConfiguration.");
		}
	}	
	
	/**
	 * Get the header action.
	 * @return {@link HeaderAction#ACTION_ADD} or {@link HeaderAction#ACTION_REMOVE}.
	 */
	public int getAction() {
		return action;
	}
	
	/**
	 * Get the name of the header which is the target of this action.
	 * @return The headerName.
	 */
	public String getHeaderName() {
		return headerName;
	}
	
	/**
	 * Get the header value for this action if the action is {@link HeaderAction#ACTION_ADD}.
	 * @return The header value for this action if the action is {@link HeaderAction#ACTION_ADD}.
	 * @throws IllegalStateException Call to getHeaderValue() when the action is not
	 * {@link HeaderAction#ACTION_ADD}.
	 */
	public String getHeaderValue() {
		if(action != ACTION_ADD) {
			throw new IllegalStateException("Illegal call to getHeaderValue() when action not 'add'.");
		}
		
		return headerValue;
	}	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if(obj == null) {
			return false;
		} else if(obj == this) {
			return true;
		} else if(obj instanceof HeaderAction) {
			HeaderAction actionObj = (HeaderAction)obj;
			if(actionObj.action == action && actionObj.headerName.equals(headerName) && actionObj.headerValue.equals(headerValue)) {
				return true;
			}
		} else if(obj instanceof String) {
			String stringObj = (String)obj;
			if(stringObj.equals(headerName)) {
				return true;
			}
		}
		
		return false;
	}	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "HeaderAction: " + action + ", " + headerName + ", " + headerValue;
	}
}
 