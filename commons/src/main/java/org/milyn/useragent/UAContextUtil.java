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

/**
 * UAContext utility methods.
 * 
 * @author tfennelly
 */
public abstract class UAContextUtil {

	/**
	 * Does the name parameter match device named in the deviceContext, or on of
	 * the deviceContext profiles.
	 * 
	 * @param name
	 *            The name.
	 * @param deviceContext
	 *            The deviceContext.
	 * @return True if the name parameter is the requesting device's "Common
	 *         Name" or the name name of one of it's profiles.
	 */
	public static boolean isDeviceOrProfile(String name, UAContext deviceContext) {
		if (name.equalsIgnoreCase(deviceContext.getCommonName())) {
			return true;
		} else if (deviceContext.getProfileSet().isMember(name)) {
			return true;
		}

		return false;
	}
}
