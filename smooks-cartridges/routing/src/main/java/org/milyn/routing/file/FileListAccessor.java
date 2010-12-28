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

package org.milyn.routing.file;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.milyn.assertion.AssertArgument;
import org.milyn.container.ExecutionContext;

/**
 * FileListAccessor is a utility class that retrieves list file names
 * from the Smooks {@link ExecutionContext}.
 * <p/>
 * 
 * @author <a href="mailto:daniel.bevenius@gmail.com">Daniel Bevenius</a>			
 *
 */
public class FileListAccessor
{
    /*
	 * 	Keys for the entry containing the file lists (used in ExecutionContexts attribute map )
     */
    private static final String ALL_LIST_FILE_NAME_CONTEXT_KEY = FileListAccessor.class.getName() + "#allListFileName";
    
	private FileListAccessor() { }
	
	/**
	 * 	Adds the passes in <code>listFileName</code> to the ExecutionContext. 
	 * 	<p/>
	 *  Note that the filename should be specified with a path. This is so that the same filename can be used 
	 *  in multiple directories.
	 * 
	 * @param fileName 	- list file name to add to the context
	 * @param execContext	- Smooks ExceutionContext
	 */
	public static void addFileName( final String fileName, final ExecutionContext execContext )
	{
		AssertArgument.isNotNullAndNotEmpty( fileName, "fileName" );
		
		@SuppressWarnings ("unchecked")
		List<String> allListFiles = (List<String>) execContext.getAttribute( ALL_LIST_FILE_NAME_CONTEXT_KEY );
		if ( allListFiles == null  )
		{
			allListFiles = new ArrayList<String>();
		}
		
		//	no need to have duplicates
		if ( !allListFiles.contains( fileName ))
		{
    		allListFiles.add( fileName );
		}
		
		execContext.setAttribute( ALL_LIST_FILE_NAME_CONTEXT_KEY , allListFiles );
	}
	
	/**
	 * 	Return the list of files contained in the passed in file "fromFile"
	 * 
	 * @param executionContext	- Smooks execution context
	 * @param fromFile			- path to list file 
	 * @return List<String>		- where String is the absolute path to a file.
	 * @throws IOException		- If the "fromFile" cannot be found or something else IO related goes wrong.
	 */
	public static List<String> getFileList( final ExecutionContext executionContext, String fromFile ) throws IOException
	{
		BufferedReader reader = null;
		try
		{
    		reader = new BufferedReader( new FileReader( fromFile ) );
    		List<String> files = new ArrayList<String>();
    		String line = null;
    		while ( (line = reader.readLine() ) != null )
    		{
    			files.add( line );
    		}
    		return files;
    	}
		finally
		{
			if ( reader != null )
			{
				reader.close();
			}
		}
	}

	@SuppressWarnings ( "unchecked" )
	public static List<String> getListFileNames( final ExecutionContext executionContext )
	{
		return (List<String>) executionContext.getAttribute( ALL_LIST_FILE_NAME_CONTEXT_KEY );
	}
	
	@SuppressWarnings ( "unchecked" )
	public static List<String> getListFileNames( final Map attributes )
	{
		return (List<String>) attributes.get( ALL_LIST_FILE_NAME_CONTEXT_KEY );
	}

}
