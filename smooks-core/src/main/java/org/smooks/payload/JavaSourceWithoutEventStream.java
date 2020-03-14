/*
 * Milyn - Copyright (C) 2006 - 2010
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License (version 2.1) as published
 * by the Free Software Foundation.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.
 * 
 * See the GNU Lesser General Public License for more details:
 * http://www.gnu.org/licenses/lgpl.txt
 */
package org.smooks.payload;

import java.util.List;


/**
 * JavaSourceWithEventStream is a wrapper around a {@link JavaSource}
 * which has disabled eventStreaming ({@link JavaSource#isEventStreamRequired()})
 * by default.
 * <p/>
 * Note that even though event streaming is disabled by default when an instance
 * of this class is created, it might later get changed as this class simply
 * extends JavaSource.
 * 
 * @author Daniel Bevenius
 *
 */
public class JavaSourceWithoutEventStream extends JavaSource
{
	JavaSource delegate;

	public JavaSourceWithoutEventStream(List<Object> sourceObjects)
	{
		super(sourceObjects);
		disableEventStream();
	}

	public JavaSourceWithoutEventStream(Object sourceObject)
	{
		super(sourceObject);
		disableEventStream();
	}

	public JavaSourceWithoutEventStream(String objectName, Object sourceObject)
	{
		super(objectName, sourceObject);
		disableEventStream();
	}
	
	private void disableEventStream()
	{
		setEventStreamRequired(false);
	}

}
