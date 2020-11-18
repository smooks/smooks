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
package org.smooks.delivery.interceptor;

import org.smooks.container.ApplicationContext;
import org.smooks.delivery.ContentHandlerBinding;
import org.smooks.delivery.Visitor;

import java.lang.reflect.ParameterizedType;

public abstract class AbstractInterceptorVisitor implements InterceptorVisitor {
    
    protected ContentHandlerBinding<Visitor> visitorBinding;
    protected ApplicationContext applicationContext;
    
    @Override
    public void setVisitorBinding(final ContentHandlerBinding<Visitor> visitorBinding) {
        this.visitorBinding = visitorBinding;
    }

    @Override
    public ContentHandlerBinding<Visitor> getVisitorBinding() {
        return visitorBinding;
    }

    @Override
    public ContentHandlerBinding<Visitor> getTarget() {
        ContentHandlerBinding<Visitor> nextVisitorBinding = visitorBinding;
        while (nextVisitorBinding.getContentHandler() instanceof InterceptorVisitor) {
            nextVisitorBinding = ((InterceptorVisitor) visitorBinding.getContentHandler()).getTarget();
        }

        return nextVisitorBinding;    
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    protected <T extends Visitor> Object intercept(Invocation<T> invocation) {
        Class<T> targetClass = (Class<T>) ((ParameterizedType) invocation.getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0];
        ContentHandlerBinding<Visitor> nextVisitorBinding = visitorBinding;
        while (nextVisitorBinding != null) {
            if (targetClass.isInstance(nextVisitorBinding.getContentHandler())) {
                return invocation.invoke((T) nextVisitorBinding.getContentHandler());
            } else {
                if (nextVisitorBinding.getContentHandler() instanceof InterceptorVisitor) {
                    nextVisitorBinding = ((InterceptorVisitor) nextVisitorBinding.getContentHandler()).getVisitorBinding();
                } else {
                    nextVisitorBinding = null;
                }
            }
        }

        return null;
    }

    public interface Invocation<T extends Visitor> {
        Object invoke(T visitor);

        Class<T> getTarget();
    }
}