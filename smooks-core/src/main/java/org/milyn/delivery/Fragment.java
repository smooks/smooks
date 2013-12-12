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

package org.milyn.delivery;

import org.milyn.delivery.sax.SAXElement;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Fragment.
 * <p/>
 * Wrapper class for a DOM or SAX Fragment.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class Fragment {

    private Element domFragment;
    private SAXElement saxFragment;

    public Fragment(Element fragmentElement) {
        this.domFragment = fragmentElement;
    }

    public Fragment(SAXElement fragmentElement) {
        this.saxFragment = fragmentElement;
    }

    public Element getDOMElement() {
        return domFragment;
    }

    public SAXElement getSAXElement() {
        return saxFragment;
    }

    public Object getElement() {
        if(saxFragment != null) {
            return saxFragment;
        } else {
            return domFragment;
        }
    }

    public boolean isDOMElement() {
        return (domFragment != null);
    }

    public boolean isSAXElement() {
        return (saxFragment != null);
    }

    public String getNamespaceURI() {
        if(isSAXElement()) {
            return saxFragment.getName().getNamespaceURI();
        } else if(isDOMElement()) {
            return domFragment.getNamespaceURI();
        }
        return null;
    }

    public String getPrefix() {
        if(isSAXElement()) {
            return saxFragment.getName().getPrefix();
        } else if(isDOMElement()) {
            return domFragment.getPrefix();
        }
        return null;
    }

    public boolean isParentFragment(Fragment fragment) {
        if(fragment.isDOMElement() && isDOMElement()) {
            Node parent = fragment.domFragment.getParentNode();
            while(parent != null) {
                if(parent == domFragment) {
                    return true;
                }
                parent = parent.getParentNode();
            }
        } else if(fragment.isSAXElement() && isSAXElement()) {
            SAXElement parent = fragment.saxFragment.getParent();
            while(parent != null) {
                if(parent == saxFragment) {
                    return true;
                }
                parent = parent.getParent();
            }
        }

        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Fragment) {
            Fragment fragObj = (Fragment) obj;
            return (this.domFragment == fragObj.domFragment && this.saxFragment == fragObj.saxFragment);
        }

        return false;
    }
}
