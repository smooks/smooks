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
package example;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.milyn.Smooks;
import org.milyn.commons.SmooksException;
import org.milyn.container.ExecutionContext;
import org.milyn.routing.file.FileListAccessor;
import org.xml.sax.SAXException;

/**
 * This is a simple example that demonstrates how Smooks can be 
 * configured to "route" the output of a transform to file(s).
 * 
 * @author <a href="mailto:daniel.bevenius@gmail.com">Daniel Bevenius</a>
 *
 */
public class Main
{
	private static final String LINE_SEP = System.getProperty( "line.separator" );
	
    protected void runSmooksTransform() throws IOException, SAXException, ClassNotFoundException
    {
        final Smooks smooks = new Smooks( "smooks-config.xml" );

        try {
            final ExecutionContext executionContext = smooks.createExecutionContext();

            //	create the source and result
            final StreamSource source = new StreamSource( new FileInputStream( "target/input-message.xml" ) );
            final StreamResult result = null;

            //executionContext.setEventListener(new HtmlReportGenerator("target/report.html"));

            //	perform the transform
            smooks.filterSource(executionContext, source, result);

            //	display the output from the transform
            System.out.println( LINE_SEP );
            System.out.println( "List file : [" + FileListAccessor.getListFileNames( executionContext ) + "]" );

            //	uncomment to print the files
            printFiles( executionContext );
        } finally {
            smooks.close();
        }
    }

    public static void main(String[] args) throws IOException, SAXException, SmooksException, InterruptedException, ClassNotFoundException
    {
		String fileName = "target/input-message.xml";
		System.out.println(fileName);
		System.out.println();
		String nrofLineItems = pause("Please specify number of order-items to generate in the input message > ");
		InputOrderGenerator.main( new String[] { fileName, nrofLineItems } );
    	
        final Main smooksMain = new Main();

        System.out.println( LINE_SEP );
        System.out.println("input-message.xml needs to be transformed and appended to a file");
        System.out.println( LINE_SEP );
        pause("Press 'enter' to display the transformed message...");
        smooksMain.runSmooksTransform();
        System.out.println( LINE_SEP );
        pause("That's it ");
    }
    
    /*
     * Can be used to print the list of files and their contents.
     * Beware that this can cause memory issues as the whole list file will be
     * read into memory. This method should only be used with smaller transforms.
     */
    @SuppressWarnings ("unused")
    private void printFiles( ExecutionContext executionContext ) throws IOException, ClassNotFoundException
    {
        List<String> allListFiles = FileListAccessor.getListFileNames( executionContext );
        for (String listFile : allListFiles)
		{
            List<String> fileNames = (List<String>) FileListAccessor.getFileList( executionContext, listFile );
            System.out.println( "Contains [" + fileNames.size() + "] files");
            for (String fileName : fileNames)
    		{
                System.out.println( "fileName :  [" + fileName + "]" );
    		}
		}
    }

    private static String pause(String message) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("> " + message);
            return in.readLine();
        } 
        catch (IOException e) 
        {
        	e.printStackTrace();
        }
        System.out.println( LINE_SEP );
		return null;
    }

}