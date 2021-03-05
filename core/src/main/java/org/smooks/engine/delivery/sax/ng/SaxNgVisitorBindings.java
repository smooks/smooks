/*-
 * ========================LICENSE_START=================================
 * Core
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
package org.smooks.engine.delivery.sax.ng;

import org.smooks.api.delivery.ContentHandlerBinding;
import org.smooks.api.resource.visitor.Visitor;
import org.smooks.api.resource.visitor.sax.ng.AfterVisitor;
import org.smooks.api.resource.visitor.sax.ng.BeforeVisitor;
import org.smooks.api.resource.visitor.sax.ng.ChildrenVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SaxNgVisitorBindings {

    private List<ContentHandlerBinding<BeforeVisitor>> beforeVisitors;
    private List<ContentHandlerBinding<ChildrenVisitor>> childVisitors;
    private List<ContentHandlerBinding<AfterVisitor>> afterVisitors;

    public List<ContentHandlerBinding<BeforeVisitor>> getBeforeVisitors() {
        return beforeVisitors;
    }

    public void setBeforeVisitors(List<ContentHandlerBinding<BeforeVisitor>> beforeVisitors) {
        this.beforeVisitors = beforeVisitors;
    }

    public List<ContentHandlerBinding<ChildrenVisitor>> getChildVisitors() {
        return childVisitors;
    }

    public void setChildVisitors(List<ContentHandlerBinding<ChildrenVisitor>> childVisitors) {
        this.childVisitors = childVisitors;
    }

    public List<ContentHandlerBinding<AfterVisitor>> getAfterVisitors() {
        return afterVisitors;
    }

    public void setAfterVisitors(List<ContentHandlerBinding<AfterVisitor>> afterVisitors) {
        this.afterVisitors = afterVisitors;
    }

    public List<ContentHandlerBinding<? extends Visitor>> getVisitorBindings() {
        List<ContentHandlerBinding<? extends Visitor>> visitors = new ArrayList<>();
        if (beforeVisitors != null) {
            visitors.addAll(beforeVisitors);
        }
        if (afterVisitors != null) {
            visitors.addAll(afterVisitors);
        }
        if (childVisitors != null) {
            visitors.addAll(childVisitors);
        }
        
        return visitors.stream().distinct().collect(Collectors.toList());
    }
    
    public SaxNgVisitorBindings merge(SaxNgVisitorBindings map) {
    	if(map == null) {
    		// No need to merge...
    		return this;
    	}    	
    	
    	SaxNgVisitorBindings merge = new SaxNgVisitorBindings();
    	
        merge.beforeVisitors = new ArrayList<>();
        merge.childVisitors = new ArrayList<>();
        merge.afterVisitors = new ArrayList<>();
        
        merge.beforeVisitors.addAll(beforeVisitors);
        merge.beforeVisitors.addAll(map.beforeVisitors);
        merge.childVisitors.addAll(childVisitors);
        merge.childVisitors.addAll(map.childVisitors);
        merge.afterVisitors.addAll(afterVisitors);
        merge.afterVisitors.addAll(map.afterVisitors);

        return merge;
    }
}