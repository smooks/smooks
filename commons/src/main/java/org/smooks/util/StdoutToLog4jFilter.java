/*-
 * ========================LICENSE_START=================================
 * Smooks Commons
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
package org.smooks.util;

import org.slf4j.Logger;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * StoutToLog4jFilter is just a simple FileWriter implementation that 
 * supresses output unless the log4j priority is set to debug.
 * This class in indended to be used with hsql embedded server and to 
 * be used like this:
 * <pre>{@code
 * 
 * Log targetLogger = LogFactory.getLog("org.hsqldb");
 * server.setLogWriter(new PrintWriter(new StdoutToLog4jFilter(server.getLogWriter(), targetLogger)));
 * 
 * }</pre>
 * 
 * @author <a href="mailto:dbevenius@redhat.com">Daniel Bevenius</a>
 */
public class StdoutToLog4jFilter extends FilterWriter
{
	/**
	 * Log to log check logging level.
	 */
    protected Logger logger;
    
    /**
     * string "buffer"
     */
    protected final StringBuilder sb = new StringBuilder(); 
    
    /**
     * Carrage Return
     */
    private static final char CR = '\r';
    /**
     * Line Feed
     */
    private static final char LF = '\n';

    public StdoutToLog4jFilter(Writer writer)
    {
    	super( writer );
    }

    public StdoutToLog4jFilter(Writer writer, Logger logger)
    { 
		this( writer );
		this.logger = logger;
    } 

    @Override
	public synchronized void write(int c) throws IOException 
    { 
        sb.append(c); 
    } 

    @Override
	public synchronized void  write(char[] cbuf, int off, int len) throws IOException 
    { 
        sb.append( cbuf, off, len ); 
    } 

    @Override
	public synchronized void  write(String str, int off, int len) throws IOException 
    { 
        sb.append(str, off, off+len); 
    } 

    @Override
	public synchronized void flush() throws IOException 
    { 
        log(); 
    } 

    @Override
	public synchronized void close() throws IOException 
    { 
        if ( sb.length() != 0) 
        {
        	log(); 
        }
    } 

    @Override
	protected void finalize() throws Throwable 
    { 
        if (sb.length() != 0) 
        { 
        	log(); 
    	} 
        super.finalize(); 
    } 

    protected void log() 
    { 
        stripExtraNewLine();
        logBuffer(sb);
        sb.delete(0,sb.length()); 
    } 
    
    private void logBuffer(final StringBuilder sb)
    {
    	if (logger == null )
    	{
    		return;
    	}
    	
        if (logger.isDebugEnabled())
        {
        	logger.debug(sb.toString());
    	}
    }
   
    protected final void stripExtraNewLine() 
    { 
        int length = sb.length(); 
        
        if (length == 0)
        {
        	return;
        }
        
        if (length == 1)
        {
            char last = sb.charAt(0); 
            if (last == CR || last == LF)
            {
                sb.deleteCharAt(0); 
            }
        }
        else
        {
        	int lastPosition = length-1;
        	char secondLast = sb.charAt(lastPosition); 
            if (secondLast == CR) 
            {
                sb.deleteCharAt( lastPosition ); 
            }
            else if (secondLast == LF) 
            { 
	        	int secondlastPosition = length-2;
                sb.deleteCharAt(lastPosition); 
                if (sb.charAt(secondlastPosition) == CR ) 
                {
                    sb.deleteCharAt(secondlastPosition); 
                }
            } 
        }
    } 
}
