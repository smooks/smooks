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
package org.milyn.delivery.sax.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.milyn.delivery.sax.SAXElement;
import org.milyn.delivery.sax.SAXVisitor;

/**
 * Fragment Text Consumer {@link SAXVisitor} Annotation.
 * <p/>
 * Because SAX is a streaming model, you need to tell Smooks to accumulate text events
 * for a specific fragment.  For performance reasons, Smooks would never accumulate these
 * events by default, because that could result in a significant memory/performance overhead.
 * <p/> 
 * This annotation results in {@link SAXElement#accumulateText() text accumulation}
 * being turned on for the fragment targeted by the annotated {@link SAXVisitor} instance.
 * The text event data can then be accessed through the parent {@link SAXElement} instance.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface TextConsumer {
}
