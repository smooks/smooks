/*-
 * ========================LICENSE_START=================================
 * Smooks Commons
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0 or,
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
package org.smooks.javabean.pojogen;

import org.smooks.assertion.AssertArgument;

import java.util.*;

/**
 * Method model.
 * @author bardl
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class JMethod {

    private JType returnType;
    private String methodName;
    private List<JNamedType> parameters = new ArrayList<JNamedType>();
    private Set<JType> exceptions = new LinkedHashSet<JType>();
    private StringBuilder bodyBuilder = new StringBuilder();

    public JMethod(String methodName) {
        AssertArgument.isNotNull(methodName, "methodName");
        this.returnType = new JType(void.class);
        this.methodName = methodName;
    }

    public JMethod(JType returnType, String methodName) {
        AssertArgument.isNotNull(returnType, "returnType");
        AssertArgument.isNotNull(methodName, "methodName");
        this.returnType = returnType;
        this.methodName = methodName;
    }

    public JType getReturnType() {
        return returnType;
    }

    public String getMethodName() {
        return methodName;
    }

    public JMethod addParameter(JType type, String parameterName) {
        return addParameter(new JNamedType(type, parameterName));
    }

    public JMethod addParameter(JNamedType parameter) {
        parameters.add(parameter);
        return this;
    }

    public List<JNamedType> getParameters() {
        return parameters;
    }

    public JMethod appendToBody(String codeString) {
        assertNotFinalized();
        bodyBuilder.append(codeString);
        return this;
    }

    public int bodyLength() {
        assertNotFinalized();
        return bodyBuilder.length();
    }

    public String getBody() {
        assertNotFinalized();
        return bodyBuilder.toString();
    }

    public StringBuilder getBodyBuilder() {
        assertNotFinalized();
        return bodyBuilder;
    }

    public Set<JType> getExceptions() {
        return exceptions;
    }

    public void finalizeMethod() {
        assertNotFinalized();
        bodyBuilder.setLength(0);
        bodyBuilder = null;
    }

    private void assertNotFinalized() {
        if(bodyBuilder == null) {
            throw new IllegalStateException("JMethod already finalized.");
        }
    }

    public String getParamSignature() {
        StringBuilder signature = new StringBuilder();

        signature.append("(");
        for(int i = 0; i < parameters.size(); i++) {
            if(i > 0) {
                signature.append(", ");
            }
            signature.append(parameters.get(i));
        }
        signature.append(")");

        if(!exceptions.isEmpty()) {
            signature.append(PojoGenUtil.getTypeDecl("throws", exceptions));
        }

        return signature.toString();
    }
}
