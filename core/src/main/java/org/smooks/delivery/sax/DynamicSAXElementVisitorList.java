/*-
 * ========================LICENSE_START=================================
 * Smooks Core
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0, or
 * the GNU Lesser General Public License version 3.0 or later.
 * 
 * SPDX-License-Identifier: Apache-2.0 OR LGPL-3.0-or-later
 * 
 * ======================================================================
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ======================================================================
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * =========================LICENSE_END==================================
 */
package org.smooks.delivery.sax;

import org.smooks.container.ExecutionContext;
import org.smooks.container.TypedKey;
import org.smooks.delivery.SmooksContentHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Dynamic SAX Element Visitor list.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class DynamicSAXElementVisitorList {

    private static final TypedKey<DynamicSAXElementVisitorList> DYNAMIC_SAX_ELEMENT_VISITOR_LIST_TYPED_KEY = new TypedKey<>();
    
    private final List<SAXVisitBefore> visitBefores = new ArrayList<SAXVisitBefore>();
    private final List<SAXVisitChildren> childVisitors = new ArrayList<SAXVisitChildren>();
    private final List<SAXVisitAfter> visitAfters = new ArrayList<SAXVisitAfter>();

    public DynamicSAXElementVisitorList(ExecutionContext executionContext) {
        executionContext.put(DYNAMIC_SAX_ELEMENT_VISITOR_LIST_TYPED_KEY, this);
    }

    public List<SAXVisitBefore> getVisitBefores() {
        return visitBefores;
    }

    public List<SAXVisitChildren> getChildVisitors() {
        return childVisitors;
    }

    public List<SAXVisitAfter> getVisitAfters() {
        return visitAfters;
    }
    
    public static DynamicSAXElementVisitorList getList(ExecutionContext executionContext) {
        return executionContext.get(DYNAMIC_SAX_ELEMENT_VISITOR_LIST_TYPED_KEY);
    }

    public static void addDynamicVisitor(SAXVisitor visitor, ExecutionContext executionContext) {
        SmooksContentHandler contentHandler = SmooksContentHandler.getHandler(executionContext);
        SmooksContentHandler nestedContentHandler = contentHandler.getNestedContentHandler();

        if(nestedContentHandler == null) {
            DynamicSAXElementVisitorList list = getList(executionContext);

            if(visitor instanceof SAXVisitBefore) {
                list.visitBefores.add((SAXVisitBefore) visitor);
            }
            if(visitor instanceof SAXVisitChildren) {
                list.childVisitors.add((SAXVisitChildren) visitor);
            }
            if(visitor instanceof SAXVisitAfter) {
                list.visitAfters.add((SAXVisitAfter) visitor);
            }
        } else {
            addDynamicVisitor(visitor, nestedContentHandler.getExecutionContext());
        }
    }

    public static void propogateDynamicVisitors(ExecutionContext parentExecutionContext, ExecutionContext childExecutionContext) {
        DynamicSAXElementVisitorList parentList = getList(parentExecutionContext);

        if(parentList != null) {
            DynamicSAXElementVisitorList childList = getList(childExecutionContext);

            if(childList ==  null) {
                childList = new DynamicSAXElementVisitorList(childExecutionContext);
            }
            childList.visitBefores.addAll(parentList.visitBefores);
            childList.childVisitors.addAll(parentList.childVisitors);
            childList.visitAfters.addAll(parentList.visitAfters);
        }
    }

    public static void removeDynamicVisitor(SAXVisitor visitor, ExecutionContext executionContext) {
        DynamicSAXElementVisitorList list = getList(executionContext);

        list.visitBefores.remove(visitor);
        list.childVisitors.remove(visitor);
        list.visitAfters.remove(visitor);
    }
}
