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
package org.milyn.javabean.expressionbinding;

import java.util.Date;
import java.util.Map;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class Message {

    private Date date;
    private Date datePlus1Year;
    private Map message2;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getDatePlus1Year() {
        return datePlus1Year;
    }

    public void setDatePlus1Year(Date datePlus1Year) {
        this.datePlus1Year = datePlus1Year;
    }

    public Map getMessage2() {
        return message2;
    }

    public void setMessage2(Map message2) {
        this.message2 = message2;
    }
}
