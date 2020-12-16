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
package org.smooks.delivery.sax.ng;

import org.smooks.container.ExecutionContext;
import org.smooks.container.TypedKey;
import org.smooks.delivery.SmooksContentHandler;
import org.smooks.delivery.Visitor;

import java.util.ArrayList;
import java.util.List;

public class DynamicSaxNgElementVisitorList {

    private static final TypedKey<DynamicSaxNgElementVisitorList> DYNAMIC_SAX_NG_ELEMENT_VISITOR_LIST_TYPED_KEY = new TypedKey<>();
    
    private final List<BeforeVisitor> beforeVisitors = new ArrayList<>();
    private final List<ChildrenVisitor> childrenVisitors = new ArrayList<>();
    private final List<AfterVisitor> afterVisitors = new ArrayList<>();

    public DynamicSaxNgElementVisitorList(ExecutionContext executionContext) {
        executionContext.put(DYNAMIC_SAX_NG_ELEMENT_VISITOR_LIST_TYPED_KEY, this);
    }

    public List<BeforeVisitor> getVisitBefores() {
        return beforeVisitors;
    }

    public List<ChildrenVisitor> getChildVisitors() {
        return childrenVisitors;
    }

    public List<AfterVisitor> getVisitAfters() {
        return afterVisitors;
    }
    
    public static DynamicSaxNgElementVisitorList getList(ExecutionContext executionContext) {
        return executionContext.get(DYNAMIC_SAX_NG_ELEMENT_VISITOR_LIST_TYPED_KEY);
    }

    public static void addDynamicVisitor(Visitor visitor, ExecutionContext executionContext) {
        SmooksContentHandler contentHandler = SmooksContentHandler.getHandler(executionContext);
        SmooksContentHandler nestedContentHandler = contentHandler.getNestedContentHandler();

        if(nestedContentHandler == null) {
            DynamicSaxNgElementVisitorList list = getList(executionContext);

            if(visitor instanceof BeforeVisitor) {
                list.beforeVisitors.add((BeforeVisitor) visitor);
            }
            if(visitor instanceof ChildrenVisitor) {
                list.childrenVisitors.add((ChildrenVisitor) visitor);
            }
            if(visitor instanceof AfterVisitor) {
                list.afterVisitors.add((AfterVisitor) visitor);
            }
        } else {
            addDynamicVisitor(visitor, nestedContentHandler.getExecutionContext());
        }
    }

    public static void propogateDynamicVisitors(ExecutionContext parentExecutionContext, ExecutionContext childExecutionContext) {
        DynamicSaxNgElementVisitorList parentList = getList(parentExecutionContext);

        if(parentList != null) {
            DynamicSaxNgElementVisitorList childList = getList(childExecutionContext);

            if(childList ==  null) {
                childList = new DynamicSaxNgElementVisitorList(childExecutionContext);
            }
            childList.beforeVisitors.addAll(parentList.beforeVisitors);
            childList.childrenVisitors.addAll(parentList.childrenVisitors);
            childList.afterVisitors.addAll(parentList.afterVisitors);
        }
    }

    public static void removeDynamicVisitor(Visitor visitor, ExecutionContext executionContext) {
        DynamicSaxNgElementVisitorList list = getList(executionContext);

        list.beforeVisitors.remove(visitor);
        list.childrenVisitors.remove(visitor);
        list.afterVisitors.remove(visitor);
    }
}
