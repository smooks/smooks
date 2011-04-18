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
package example;

public class Customer {
	
	private String Firstname;
    private String Lastname;
    private Gender Gender;
    private int Age;
	private String Country;
	
    public String getCountry() {
		return Country;
	}
	public void setCountry(String country) {
		Country = country;
	}
    public String getFirstname() {
		return Firstname;
	}
	public void setFirstname(String firstName) {
		Firstname = firstName;
	}
	public String getLastname() {
		return Lastname;
	}
	public void setLastname(String lastName) {
		Lastname = lastName;
	}
	public Gender getGender() {
		return Gender;
	}
	public void setGender(Gender gender) {
		Gender = gender;
	}
	public int getAge() {
		return Age;
	}
	public void setAge(int age) {
		Age = age;
	}

    public String toString() {
        return "[" + Firstname + ", " + Lastname + ", " + Gender + ", " + Age + ", " + Country + "]";
    }
}
