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
package org.smooks.delivery.sax.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.xml.transform.stream.StreamResult;

import org.smooks.delivery.sax.SAXElement;
import org.smooks.delivery.sax.SAXVisitor;

/**
 * Fragment {@link StreamResult} Writer SAX Visitor Annotation.
 * <p/>
 * Used to flag a {@link SAXVisitor} implementation as being a writer
 * to any defined {@link StreamResult}.  This annotation results in the
 * Stream Writer (for the StreamResult) being acquired on behalf of the
 * SAXVistor implementation declaring the annotation.  See {@link SAXElement}.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
@Inherited
public @interface StreamResultWriter {
}
