/*
 * Milyn - Copyright (C) 2006 - 2010
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License (version 2.1) as published
 * by the Free Software Foundation.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.
 * 
 * See the GNU Lesser General Public License for more details:
 * http://www.gnu.org/licenses/lgpl.txt
 */
package org.smooks.routing.jms;

import java.io.Serializable;

/**
 * 
 * @author <a href="mailto:daniel.bevenius@gmail.com">Daniel Bevenius</a>			
 *
 */
public class TestBean implements Serializable
{
    private String name;
    private String address;
    private String phoneNumber;
    
    public String getAddress() 
    {
        return address;
    }
    
    public void setAddress(String address) 
    {
        this.address = address;
    }
    
    public String getName() 
    {
        return name;
    }
    
    public void setName(String name) 
    {
        this.name = name;
    }
    
    public String getPhoneNumber() 
    {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) 
    {
        this.phoneNumber = phoneNumber;
    }
    
    public String toString()
    {
    	return "TestBean [name:" + name + ", address:" + address + ", phoneNumber:" + phoneNumber + "]";
    }

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((phoneNumber == null) ? 0 : phoneNumber.hashCode());
		return result;
	}

	@Override
	public boolean equals( Object obj )
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final TestBean other = (TestBean) obj;
		if (address == null)
		{
			if (other.address != null)
				return false;
		} else if (!address.equals( other.address ))
			return false;
		if (name == null)
		{
			if (other.name != null)
				return false;
		} else if (!name.equals( other.name ))
			return false;
		if (phoneNumber == null)
		{
			if (other.phoneNumber != null)
				return false;
		} else if (!phoneNumber.equals( other.phoneNumber ))
			return false;
		return true;
	}
    
    
}
