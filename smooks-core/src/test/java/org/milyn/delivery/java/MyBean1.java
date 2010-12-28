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
package org.milyn.delivery.java;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class MyBean1 {
    private boolean prop1 = true;
    private String prop2 = "hello";
    private long prop3 = 1111L;
    private MyBean2 mybean2 = new MyBean2();

    public boolean isProp1() {
        return prop1;
    }

    public void setProp1(boolean prop1) {
        this.prop1 = prop1;
    }

    public String getProp2() {
        return prop2;
    }

    public void setProp2(String prop2) {
        this.prop2 = prop2;
    }

    public long getProp3() {
        return prop3;
    }

    public void setProp3(long prop3) {
        this.prop3 = prop3;
    }

    public MyBean2 getMybean2() {
        return mybean2;
    }

    public void setMybean2(MyBean2 mybean2) {
        this.mybean2 = mybean2;
    }
}
