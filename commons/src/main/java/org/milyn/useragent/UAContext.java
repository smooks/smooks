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

package org.milyn.useragent;

import java.io.Serializable;

import org.milyn.profile.ProfileSet;

/**
 * Useragnet device context. <p/> Provides access to information such as the
 * useragent name and the useragent ProfileSet.
 * 
 * @author tfennelly
 */
public interface UAContext extends Serializable {

	/**
	 * Get the useragent common name.
	 * 
	 * @return The useragent common name.
	 */
	public abstract String getCommonName();

	/**
	 * Get the ProfileSet for the device.
	 * 
	 * @return The ProfileSet
	 */
	public abstract ProfileSet getProfileSet();
}