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

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.Before;
import org.junit.Test;
import org.smooks.Smooks;
import org.smooks.api.ExecutionContext;
import org.smooks.io.StreamUtils;
import org.xml.sax.SAXException;

/**
 * Unit test for SmooksSAXFilter
 * 
 * @author <a href="mailto:daniel.bevenius@gmail.com">Daniel Bevenius</a>			
 *
 */
public class SmooksSAXFilterTestCase
{
	private byte[] input;
	private ExecutionContext context;
	private SmooksSAXFilter saxFilter;
	
	@Test
	public void doFilter_verify_that_flush_is_called() throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		StreamResult result = new StreamResult( baos );
		
		saxFilter.doFilter( new StreamSource( new ByteArrayInputStream( input ) ), result );
		
		OutputStream outputStream = result.getOutputStream();
		assertTrue( outputStream instanceof ByteArrayOutputStream );
		
		byte[] byteArray = ((ByteArrayOutputStream)outputStream).toByteArray();
		assertTrue ( byteArray.length > 0 );
	}
	
	@Before
	public void setup() throws IOException, SAXException
	{
        Smooks smooks = new Smooks(getClass().getResourceAsStream("smooks-config-01.xml"));
        context = smooks.createExecutionContext();
		input = StreamUtils.readStream( getClass().getResourceAsStream( "test-01.xml") );
		saxFilter = new SmooksSAXFilter( context );
	}

}
