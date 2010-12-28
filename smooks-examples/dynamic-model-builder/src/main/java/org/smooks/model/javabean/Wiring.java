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
 * Wiring binding configuration.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class Wiring extends Binding {

    private String beanIdRef;
    private String wireOnElement;
    private String wireOnElementNS;

    public String getBeanIdRef() {
        return beanIdRef;
    }

    public void setBeanIdRef(String beanIdRef) {
        this.beanIdRef = beanIdRef;
    }

    public String getWireOnElement() {
        return wireOnElement;
    }

    public void setWireOnElement(String wireOnElement) {
        this.wireOnElement = wireOnElement;
    }

    public String getWireOnElementNS() {
        return wireOnElementNS;
    }

    public void setWireOnElementNS(String wireOnElementNS) {
        this.wireOnElementNS = wireOnElementNS;
    }
}
