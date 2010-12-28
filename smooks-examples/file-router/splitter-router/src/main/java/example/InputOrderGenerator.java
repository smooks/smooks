package example;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Simply generates an sample xml
 * </p>
 * Usage: <br>
 * InputOrderGenerator filename <nrOfOrderRows>
 * </br>
 * <pre>
 *Will generate a file that looks like this: 
 *&lt;order&gt;
 *	&lt;header&gt;
 *		&lt;date>Wed Nov 15 13:45:28 EST 2006&lt;/date&gt;
 *		&lt;customer number="123123"&gt;Joe&lt;/customer&gt;
 *	&lt;/header&gt;
 *	&lt;order-items&gt;
 *		&lt;order-item&gt;
 *			&lt;product&gt;111&lt;/product&gt;
 *			&lt;quantity&gt;2&lt;/quantity&gt;
 *			&lt;price&gt;8.90&lt;/price&gt;
 *			&lt;/order-item&gt;
 *			...
 *		&lt;/order-item&gt;
 *	&lt;/order-items&gt;
 *&lt;order&gt;
 * </pre>
 * The number of order-items is controlled by the 'nrOfOrderRows' argument.
 * 
 * @author <a href="mailto:daniel.bevenius@gmail.com">Daniel Bevenius</a>			
 *
 */
public class InputOrderGenerator
{
	private static final String LINE_SEP = System.getProperty( "line.separator" );
	
	public static void main( String[] args  ) throws IOException
	{
		if ( args.length != 2 )
		{
			printUsage();
			return;
		}
		
		String fileName = args[0];
		String nrOfOrderItemRows = args[1];
		int nrOfOrderItems = Integer.parseInt( nrOfOrderItemRows );
		
		File file = new File ( fileName );
		deleteFile( file );
		FileWriter writer = new FileWriter( file, true );
		writer.write( "<order id='332'>" + LINE_SEP );
		writer.write( "<header>" + LINE_SEP);
		writer.write( "<customer number=\"123\">Joe</customer>" + LINE_SEP );
		writer.write( "</header>" + LINE_SEP);
		writer.flush();
		writer.write( "<order-items>" + LINE_SEP );
		System.out.print("Generating " + file.getName() + "...");
		for ( int i = 1 ; i <= nrOfOrderItems ; i ++ )
		{
    		writer.write( "<order-item id='" + i + "'>" + LINE_SEP );
    		writer.write( "<product>" + i + "</product>" + LINE_SEP );
    		writer.write( "<quantity>2</quantity>" + LINE_SEP );
    		writer.write( "<price>8.80</price>" + LINE_SEP );
    		writer.write( "</order-item>" + LINE_SEP );
    		writer.flush();
		}
		writer.write( "</order-items>" + LINE_SEP);
		writer.write( "</order>" + LINE_SEP );
		writer.close();
		System.out.println();
		System.out.println("Generated " + file.getName() + " size = [" + getFileLength( file ) + "]" );
	}
	
	private static String getFileLength( final File file )
	{
		long fileLength = file.length();
		int digits = String.valueOf( fileLength ).length();
		if ( digits < 4 )
		{
    		return fileLength + " Byte(s)";
		}
		else if ( digits >= 4 && digits <= 6 )
		{
    		return fileLength / 1024 + " KB";
		}
		else 
		{
    		return fileLength / (1024 * 1024 ) + " MB";
		}
	}
	
	private static void deleteFile( final File file ) throws IOException
	{
		if ( file.exists() )
		{
			boolean delete = file.delete();
			if ( !delete )
			{
				throw new IOException ("Could not delete file [" + file.getAbsolutePath() + "]" );
			}
		}
	}
	
	public static void printUsage()
	{
		System.err.println( "Usage: InputOrderGenerator filename <nrOfOrderRows>");
	}

}
