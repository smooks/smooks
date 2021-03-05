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
package org.smooks.engine.delivery.sax;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.api.SmooksException;
import org.smooks.api.delivery.Filter;
import org.smooks.engine.resource.config.ParameterAccessor;
import org.smooks.api.ExecutionContext;
import org.smooks.engine.delivery.AbstractFilter;
import org.smooks.engine.delivery.sax.terminate.TerminateException;
import org.smooks.io.Stream;
import org.smooks.io.payload.FilterResult;
import org.smooks.io.payload.FilterSource;
import org.smooks.io.payload.JavaSource;
import org.smooks.io.payload.StringSource;
import org.smooks.support.SAXUtil;
import org.smooks.support.XmlUtil;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.Writer;

/**
 * Smooks SAX Filter.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class SmooksSAXFilter extends AbstractFilter {

	private static final Logger LOGGER = LoggerFactory.getLogger(SmooksSAXFilter.class);
	
    protected final ExecutionContext executionContext;
    protected final SAXParser parser;
    protected final boolean closeSource;
    protected final boolean closeResult;

    public SmooksSAXFilter(ExecutionContext executionContext) {
        this.executionContext = executionContext;
        closeSource = Boolean.parseBoolean(ParameterAccessor.getParameterValue(Filter.CLOSE_SOURCE, String.class, "true", executionContext.getContentDeliveryRuntime().getContentDeliveryConfig()));
        closeResult = Boolean.parseBoolean(ParameterAccessor.getParameterValue(Filter.CLOSE_RESULT, String.class, "true", executionContext.getContentDeliveryRuntime().getContentDeliveryConfig()));
        parser = new SAXParser(executionContext);
    }

    public void doFilter() throws SmooksException {
        Source source = FilterSource.getSource(executionContext);
        Result result = FilterResult.getResult(executionContext, StreamResult.class);

        doFilter(source, result);
    }

    protected void doFilter(Source source, Result result) {
        if(source instanceof DOMSource) {
            String serializedDOM = XmlUtil.serialize(((DOMSource)source).getNode(), false, true);
            source = new StringSource(serializedDOM);
            if(LOGGER.isDebugEnabled()) {
                LOGGER.debug("DOMSource converted to a StringSource.");
            }
        }

        if (!(source instanceof StreamSource) && !(source instanceof JavaSource)) {
            throw new IllegalArgumentException(source.getClass().getName() + " Source types not yet supported by the SAX Filter. Only supports StreamSource and JavaSource at present.");
        }
        if (!(result instanceof FilterResult)) {
            if (!(result instanceof StreamResult) && result != null) {
                throw new IllegalArgumentException(result.getClass().getName() + " Result types not yet supported by the SAX Filter. Only supports StreamResult at present.");
            }
        }

        try {
            Writer writer = getWriter(result, executionContext);
            executionContext.put(Stream.STREAM_WRITER_TYPED_KEY, writer);
            parser.parse(source, executionContext);
            writer.flush();
        } catch (TerminateException e) {
            if(LOGGER.isDebugEnabled()) {
            	if(e.isTerminateBefore()) {
            		LOGGER.debug("Terminated filtering on visitBefore of element '" + SAXUtil.getXPath(e.getElement()) + "'.");
            	} else {
            		LOGGER.debug("Terminated filtering on visitAfter of element '" + SAXUtil.getXPath(e.getElement()) + "'.");            		
            	}
            }
        } catch (Exception e) {
            throw new SmooksException("Failed to filter source.", e);
        } finally {
            if(closeSource) {
                close(source);
            }
            if(closeResult) {
                close(result);
            }
        }
    }

    public void cleanup() {
        parser.cleanup();
    }
}
