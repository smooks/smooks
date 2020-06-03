/*-
 * ========================LICENSE_START=================================
 * Smooks Core
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
package org.smooks.expression;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.smooks.cdr.SmooksConfigurationException;
import org.mvel2.DataConversion;
import org.mvel2.MVEL;
import org.mvel2.integration.impl.MapVariableResolverFactory;

/**
 * <a href="http://mvel.codehaus.org/">MVEL</a> expression evaluator.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 * @author <a href="mailto:maurice@zeijen.net">maurice@zeijen.net</a>
 */
public class MVELExpressionEvaluator implements ExpressionEvaluator {

    private static final String MVEL_VARIABLES_VARIABLE_NAME = "VARS";

	private String expression;

    private Serializable compiled;

	private boolean containsVariablesVariable;

    private Class<?> toType;

    public MVELExpressionEvaluator() {
	}

    public MVELExpressionEvaluator(String expression) {
    	setExpression(expression);
    }

    public ExpressionEvaluator setExpression(String expression) throws SmooksConfigurationException {
        this.expression = expression.trim();
        compiled = MVEL.compileExpression(this.expression);

        containsVariablesVariable = this.expression.contains(MVEL_VARIABLES_VARIABLE_NAME);

        return this;
    }

    public String getExpression() {
        return expression;
    }

	public void setToType(Class<?> toType) {
		this.toType = toType;
	}

	public boolean eval(Object contextObject) throws ExpressionEvaluationException {
        return (Boolean) exec(contextObject);
    }

	public Object exec(final Object contextObject, Map<String, Object> variableMap) throws ExpressionEvaluationException {
		 try {

	        	if(containsVariablesVariable && contextObject instanceof Map<?, ?>) {

	        		// We use the root ResolverFactories so that variables created in MVEL Scripts are put in the empty HashMap
	        		// of the second VariableResolverFactory and not in the contextObject Map.
		        	MapVariableResolverFactory rootResolverFactory = new MapVariableResolverFactory(variableMap);

	        		MapVariableResolverFactory contextVariableResolverFactory = new MapVariableResolverFactory((Map<?, ?>) contextObject);
	        		rootResolverFactory.setNextFactory(contextVariableResolverFactory);

		        	// The VARS variable contains the MVELVariables object which get access to root ResolverFactory to be able to
		        	// do look in the variables of the resolver factory
		        	rootResolverFactory.createVariable(MVEL_VARIABLES_VARIABLE_NAME, new MVELVariables(rootResolverFactory));

		        	if(toType != null) {
		        		return  DataConversion.convert(MVEL.executeExpression(compiled, rootResolverFactory), toType);
		        	} else {
		        		return  MVEL.executeExpression(compiled, rootResolverFactory);
		        	}
	        	} else {
		        	if(toType != null) {
		        		return DataConversion.convert(MVEL.executeExpression(compiled, contextObject, new MapVariableResolverFactory(variableMap)), toType);
		        	} else {
		        		return MVEL.executeExpression(compiled, contextObject, new MapVariableResolverFactory(variableMap));
		        	}
	        	}

	        } catch(Exception e) {
	        	String msg = "Error evaluating MVEL expression '" + expression + "' against object type '" + contextObject.getClass().getName() + "'. " +
	            				"Common issues include:" +
	            				"\n\t\t1. Referencing a variable that is not bound into the context." +
	            				" In this case use the 'isdef' operator to check if the variable is bound in the context." +
	            				"\n\t\t2. Invalid expression reference to a List/Array based variable token.  Example List/Array referencing expression token: 'order.orderItems[0].productId'.";

	            throw new ExpressionEvaluationException(msg, e);
	        }
	}

	public Object exec(final Object contextObject) throws ExpressionEvaluationException {
        return exec(contextObject, new HashMap<String, Object>());
    }

	public Object getValue(final Object contextObject) throws ExpressionEvaluationException {
    	return exec(contextObject);
    }

    /**
	 * @return the compiled
	 */
	public Serializable getCompiled() {
		return compiled;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this)
					.append("expression", expression)
					.append("toType", toType)
					.toString();
	}
}
