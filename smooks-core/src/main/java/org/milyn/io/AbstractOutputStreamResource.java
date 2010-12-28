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

package org.milyn.io;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.milyn.SmooksException;
import org.milyn.assertion.AssertArgument;
import org.milyn.cdr.annotation.ConfigParam;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.ExecutionLifecycleCleanable;
import org.milyn.delivery.Fragment;
import org.milyn.delivery.VisitLifecycleCleanable;
import org.milyn.delivery.ordering.Consumer;
import org.milyn.delivery.dom.DOMVisitBefore;
import org.milyn.delivery.sax.SAXElement;
import org.milyn.delivery.sax.SAXVisitBefore;
import org.w3c.dom.Element;

import java.io.*;
import java.nio.charset.Charset;

/**
 * AbstractOuputStreamResource is the base class for handling output stream
 * resources in Smooks.
 * <p/>
 * Note that a {@link Writer} can also be opened on a stream resource.  If a {@link Writer}
 * has been opened on a resource, an {@link OutputStream} cannot also be opened (and visa versa).
 * <p/>
 * Example configuration:
 * <pre>
 * &lt;resource-config selector="#document"&gt;
 *    &lt;resource&gt;org.milyn.io.ConcreateImpl&lt;/resource&gt;
 *    &lt;param name="resourceName"&gt;resourceName&lt;/param&gt;
 *    &lt;param name="writerEncoding"&gt;UTF-8&lt;/param&gt; &lt;!-- Optional --&gt;
 * &lt;/resource-config&gt;
 * </pre>
 *
 * Description of configuration properties:
 * <ul>
 * <li><code>resource</code>: should be a concreate implementation of this class</li>
 * <li><code>resourceName</code>: the name of this resouce. Will be used to identify this resource</li>
 * <li><code>writerEncoding</code>: (Optional) the encoding to be used by any writers opened on this resource (Default is "UTF-8")</li>
 * </ul>
 *
 * @author <a href="mailto:daniel.bevenius@gmail.com">Daniel Bevenius</a>
 *
 */
public abstract class AbstractOutputStreamResource implements SAXVisitBefore, DOMVisitBefore, Consumer, VisitLifecycleCleanable, ExecutionLifecycleCleanable
{
	Log log = LogFactory.getLog( AbstractOutputStreamResource.class );

    protected static final String RESOURCE_CONTEXT_KEY_PREFIX = AbstractOutputStreamResource.class.getName() + "#outputresource:";

    private static final String OUTPUTSTREAM_CONTEXT_KEY_PREFIX = AbstractOutputStreamResource.class.getName() + "#outputstream:";

    @ConfigParam
    private String resourceName;

    @ConfigParam(defaultVal = "UTF-8")
    private Charset writerEncoding = Charset.forName("UTF-8");

    //	public

	/**
	 * Retrieve/create an output stream that is appropriate for the concreate implementation
	 *
	 * @param executionContext Execution Context.
	 * @return OutputStream specific to the concreate implementation
	 */
	public abstract OutputStream getOutputStream( final ExecutionContext executionContext ) throws IOException;

    /**
	 * Get the name of this resource
	 *
	 * @return The name of the resource
	 */
	public String getResourceName() {
        return resourceName;
    }

    public boolean consumes(Object object) {
        if(object.equals(resourceName)) {
            return true;
        }
        return false;
    }

    /**
	 * Set the name of this resource
	 *
	 * @param resourceName The name of the resource
	 */
    public AbstractOutputStreamResource setResourceName(String resourceName) {
        AssertArgument.isNotNullAndNotEmpty(resourceName, "resourceName");
        this.resourceName = resourceName;
        return this;
    }

    public AbstractOutputStreamResource setWriterEncoding(Charset writerEncoding) {
        AssertArgument.isNotNull(writerEncoding, "writerEncoding");
        this.writerEncoding = writerEncoding;
        return this;
    }

    public Charset getWriterEncoding() {
        return writerEncoding;
    }

    public void visitBefore( final SAXElement element, final ExecutionContext executionContext ) throws SmooksException, IOException
	{
		bind ( executionContext );
	}

    public void visitBefore( final Element element, final ExecutionContext executionContext ) throws SmooksException
    {
        bind ( executionContext );
    }

    public void executeVisitLifecycleCleanup(Fragment fragment, ExecutionContext executionContext) {
        if(closeCondition( executionContext )) {
            closeResource( executionContext );
        }
    }

	public void executeExecutionLifecycleCleanup( ExecutionContext executionContext )
	{
		closeResource( executionContext );
	}

	protected boolean closeCondition(ExecutionContext executionContext) {
		return true;
	}

    /**
     * Get an {@link OutputStream} to the named Resource.
     *
     * @param resourceName The resource name.
     * @param executionContext The current ExececutionContext.
     * @return An {@link OutputStream} to the named Resource.
     * @throws SmooksException Unable to access OutputStream.
     */
    public static OutputStream getOutputStream(
    		final String resourceName,
            final ExecutionContext executionContext) throws SmooksException
    {
        String resourceKey = OUTPUTSTREAM_CONTEXT_KEY_PREFIX + resourceName;
        Object resourceIOObj = executionContext.getAttribute( resourceKey );

        if( resourceIOObj == null )
        {
            AbstractOutputStreamResource resource = (AbstractOutputStreamResource) executionContext.getAttribute( RESOURCE_CONTEXT_KEY_PREFIX + resourceName );
            OutputStream outputStream = openOutputStream(resource, resourceName, executionContext);

            executionContext.setAttribute( resourceKey, outputStream );
            return outputStream;
        } else {
            if(resourceIOObj instanceof OutputStream) {
                return (OutputStream) resourceIOObj;
            } else if(resourceIOObj instanceof Writer) {
                throw new SmooksException("An Writer to the '" + resourceName + "' resource is already open.  Cannot open an OutputStream to this resource now!");
            } else {
                throw new RuntimeException("Invalid runtime ExecutionContext state. Value stored under context key '" + resourceKey + "' must be either and OutputStream or Writer.  Is '" + resourceIOObj.getClass().getName() + "'.");
            }
        }
    }

    /**
     * Get a {@link Writer} to the named {@link OutputStream} Resource.
     * <p/>
     * Wraps the {@link OutputStream} in a {@link Writer}.  Uses the "writerEncoding"
     * param to set the encoding on the {@link Writer}.
     *
     * @param resourceName The resource name.
     * @param executionContext The current ExececutionContext.
     * @return A {@link Writer} to the named {@link OutputStream} Resource.
     * @throws SmooksException Unable to access OutputStream.
     */
    public static Writer getOutputWriter(final String resourceName, final ExecutionContext executionContext) throws SmooksException {
        String resourceKey = OUTPUTSTREAM_CONTEXT_KEY_PREFIX + resourceName;
        Object resourceIOObj = executionContext.getAttribute( resourceKey );

        if( resourceIOObj == null ) {
            AbstractOutputStreamResource resource = (AbstractOutputStreamResource) executionContext.getAttribute( RESOURCE_CONTEXT_KEY_PREFIX + resourceName );
            OutputStream outputStream = openOutputStream(resource, resourceName, executionContext);
            Writer outputStreamWriter = new OutputStreamWriter(outputStream, resource.getWriterEncoding());

            executionContext.setAttribute( resourceKey, outputStreamWriter );
            return outputStreamWriter;
        } else {
            if(resourceIOObj instanceof Writer) {
                return (Writer) resourceIOObj;
            } else if(resourceIOObj instanceof OutputStream) {
                throw new SmooksException("An OutputStream to the '" + resourceName + "' resource is already open.  Cannot open a Writer to this resource now!");
            } else {
                throw new RuntimeException("Invalid runtime ExecutionContext state. Value stored under context key '" + resourceKey + "' must be either and OutputStream or Writer.  Is '" + resourceIOObj.getClass().getName() + "'.");
            }
        }
    }

    private static OutputStream openOutputStream(AbstractOutputStreamResource resource, String resourceName, ExecutionContext executionContext) {
        if( resource == null )
        {
            throw new SmooksException( "OutputResource '" + resourceName + "' not bound to context.  Configure an '" + AbstractOutputStreamResource.class.getName() +  "' implementation, or change resource ordering." );
        }

        try
        {
            return resource.getOutputStream( executionContext );
        }
        catch ( IOException e )
        {
            throw new SmooksException( "Unable to set outputstream for '" + resource.getResourceName() + "'.", e );
        }
    }

    /**
	 * Close the resource output stream.
     * <p/>
     * Classes overriding this method must call super on this method. This will
     * probably need to be done before performing any aditional cleanup.
	 *
	 * @param executionContext Smooks ExecutionContext
	 */
    protected void closeResource( final ExecutionContext executionContext )
	{
		try
		{
            Closeable output = (Closeable) executionContext.getAttribute( OUTPUTSTREAM_CONTEXT_KEY_PREFIX + getResourceName() );
            close( output );
		}
		finally
		{
            executionContext.removeAttribute( OUTPUTSTREAM_CONTEXT_KEY_PREFIX + getResourceName() );
            executionContext.removeAttribute( RESOURCE_CONTEXT_KEY_PREFIX + getResourceName() );
		}
	}

	private void bind( final ExecutionContext executionContext )
	{
        executionContext.setAttribute( RESOURCE_CONTEXT_KEY_PREFIX + getResourceName(), this );
	}

	private void close( final Closeable closeable)
	{
		if ( closeable == null )
		{
			return;
		}

        if(closeable instanceof Flushable) {
            try
            {
                ((Flushable) closeable).flush();
            }
            catch (IOException e)
            {
                log.error( "IOException while trying to flush output resource '" + resourceName + "': ", e );
            }
        }

        try
		{
			closeable.close();
		}
		catch (IOException e)
		{
			log.error( "IOException while trying to close output resource '" + resourceName + "': ", e );
		}
	}

}
