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
package org.milyn.javabean.decoders;

import org.milyn.commons.javabean.DataDecodeException;
import org.milyn.commons.javabean.DataDecoder;
import org.milyn.commons.javabean.DecodeType;
import org.milyn.expression.MVELExpressionEvaluator;

/**
 * {@link MVELExpressionEvaluator} data decoder impl.
 *
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
@DecodeType(MVELExpressionEvaluator.class)
public class MVELExpressionEvaluatorDecoder implements DataDecoder {

    public Object decode(String data) throws DataDecodeException {
        MVELExpressionEvaluator expressionEvaluator = new MVELExpressionEvaluator();

        expressionEvaluator.setExpression(data);

        return expressionEvaluator;
    }
}
