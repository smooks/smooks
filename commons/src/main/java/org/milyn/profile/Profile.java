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

package org.milyn.profile;

import java.io.Serializable;

/**
 * Profile.
 * 
 * @author tfennelly
 */
public interface Profile extends Serializable {
    
    /**
     * The default profile name.
     */
    String DEFAULT_PROFILE = Profile.class.getName() + "#default_profile";

    /**
	 * Get the profile name.
	 * 
	 * @return The profile name.
	 */
	public abstract String getName();
}
