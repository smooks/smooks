/*
 * Milyn - Copyright (C) 2006 - 2010
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License (version 2.1) as published by the Free Software
 *  Foundation.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 *  See the GNU Lesser General Public License for more details:
 *  http://www.gnu.org/licenses/lgpl.txt
 */

package org.smooks.model.javabean;

/**
 * Expression based binding configuration.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class Expression extends Binding {

    private String execOnElement;
    private String execOnElementNS;
    private String initVal;

    public String getExecOnElement() {
        return execOnElement;
    }

    public void setExecOnElement(String execOnElement) {
        this.execOnElement = execOnElement;
    }

    public String getExecOnElementNS() {
        return execOnElementNS;
    }

    public void setExecOnElementNS(String execOnElementNS) {
        this.execOnElementNS = execOnElementNS;
    }

    public String getInitVal() {
        return initVal;
    }

    public void setInitVal(String initVal) {
        this.initVal = initVal;
    }
}
