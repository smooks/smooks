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
package org.smooks.fixedlength.MILYN_427;

/**
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class Data {

	private String number;
	private String truncated;
	private String lastname;
	private String firstname;
	private String mail;

	public void setNumber(final String lineNr) {
		this.number = lineNr;
	}

	public String getNumber() {
		return number;
	}

	public void setTruncated(final String truncated) {
		this.truncated = truncated;
	}

	public String getTruncated() {
		return truncated;
	}

	public void setLastname(final String lastname) {
		this.lastname = lastname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setFirstname(final String firstname) {
		this.firstname = firstname;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setMail(final String mail) {
		this.mail = mail;
	}

	public String getMail() {
		return mail;
	}

}
