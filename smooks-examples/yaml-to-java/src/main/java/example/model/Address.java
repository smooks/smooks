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
package example.model;

/**
 *
 * @author <a href="mailto:maurice@zeijen.net">maurice@zeijen.net</a>
 *
 */
public class Address {

	private String street;

	private String city;

	private String zipcode;

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getZipcode() {
		return zipcode;
	}

	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Address [\n\t\t\tstreet = ");
		builder.append(street);
		builder.append(",\n\t\t\tcity = ");
		builder.append(city);
		builder.append(",\n\t\t\tzipcode = ");
		builder.append(zipcode);
		builder.append("\n\t\t]");
		return builder.toString();
	}

}
