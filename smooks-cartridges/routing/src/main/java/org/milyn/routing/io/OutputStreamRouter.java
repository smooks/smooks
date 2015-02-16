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
package org.milyn.routing.io;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.milyn.SmooksException;
import org.milyn.cdr.SmooksConfigurationException;
import org.milyn.cdr.annotation.AppContext;
import org.milyn.cdr.annotation.ConfigParam;
import org.milyn.container.ApplicationContext;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.annotation.Initialize;
import org.milyn.delivery.annotation.VisitAfterIf;
import org.milyn.delivery.annotation.VisitBeforeIf;
import org.milyn.delivery.dom.DOMElementVisitor;
import org.milyn.delivery.sax.SAXElement;
import org.milyn.delivery.sax.SAXVisitAfter;
import org.milyn.delivery.sax.SAXVisitBefore;
import org.milyn.delivery.ordering.Consumer;
import org.milyn.io.AbstractOutputStreamResource;
import org.milyn.javabean.context.BeanContext;
import org.milyn.javabean.repository.BeanId;
import org.w3c.dom.Element;

/**
 * OutputStreamRouter is a fragment Visitor (DOM/SAX) that can be used to route
 * context beans ({@link BeanContext} beans) an OutputStream.
 * </p>
 * An OutputStreamRouter is used in combination with a concreate implementation of
 * {@link AbstractOutputStreamResource}, for example a {@link org.milyn.routing.file.FileOutputStreamResource}.
 *
 *Example configuration:
 *<pre>
 *&lt;resource-config selector="orderItem"&gt;
 *    &lt;resource&gt;org.milyn.routing.io.OutputStreamRouter&lt;/resource&gt;
 *    &lt;param name="resourceName"&gt;refToResource&lt;/param&gt;
 *    &lt;param name="beanId"&gt;orderItem&lt;/param&gt;
 *&lt;/resource-config&gt;
 *</pre>
 *
 * Description of configuration properties:
 * <ul>
 * <li><code>beanId </code> is key used search the execution context for the content to be written the OutputStream
 * <li><code>resourceName </code> is a reference to a previously configured {@link AbstractOutputStreamResource}
 * <li><code>encoding </code> is the encoding used when writing a characters to file
 * </ul>
 *
 * @author <a href="mailto:daniel.bevenius@gmail.com">Daniel Bevenius</a>
 * @since 1.0
 */
@VisitAfterIf(	condition = "!parameters.containsKey('visitBefore') || parameters.visitBefore.value != 'true'")
@VisitBeforeIf(	condition = "!parameters.containsKey('visitAfter') || parameters.visitAfter.value != 'true'")
public class OutputStreamRouter implements DOMElementVisitor, SAXVisitBefore, SAXVisitAfter, Consumer
{
	@ConfigParam
	private String resourceName;

    /*
     *	Character encoding to be used when writing character output
     */
    @ConfigParam( use = ConfigParam.Use.OPTIONAL, defaultVal = "UTF-8" )
	private String encoding;

	/*
	 * 	beanId is a key that is used to look up a bean in the execution context
	 */
    @ConfigParam( name = "beanId", use = ConfigParam.Use.REQUIRED )
    private String beanIdName;

    private BeanId beanId;

    @AppContext
    private ApplicationContext applicationContext;

    @Initialize
    public void initialize() throws SmooksConfigurationException {

    	beanId = applicationContext.getBeanIdStore().getBeanId(beanIdName);

    }

    //	public

    public boolean consumes(Object object) {
        if(object.equals(resourceName)) {
            return true;
        } else if(object.toString().startsWith(beanIdName)) {
            // We use startsWith (Vs equals) so as to catch bean populations e.g. "address.street".
            return true;
        }

        return false;
    }

    public void visitBefore( Element element, ExecutionContext executionContext ) throws SmooksException
	{
		write( executionContext );
	}

	public void visitAfter( Element element, ExecutionContext executionContext ) throws SmooksException
	{
		write( executionContext );
	}

	public void visitBefore( SAXElement element, ExecutionContext executionContext ) throws SmooksException, IOException
	{
		write( executionContext );
	}

	public void visitAfter( SAXElement element, ExecutionContext executionContext ) throws SmooksException, IOException
	{
		write( executionContext );
	}

	public String getResourceName()
	{
		return resourceName;
	}

	//	private

	private void write( final ExecutionContext executionContext )
	{
		Object bean = executionContext.getBeanContext().getBean( beanId );
        if ( bean == null )
        {
        	throw new SmooksException( "A bean with id [" + beanId + "] was not found in the executionContext");
        }

        OutputStream out = AbstractOutputStreamResource.getOutputStream( resourceName, executionContext );
		try
		{
			if ( bean instanceof String )
			{
        		out.write( ( (String)bean).getBytes(encoding ) );
			}
			else if ( bean instanceof byte[] )
			{
        		out.write( new String( (byte[]) bean, encoding ).getBytes() ) ;
			}
			else
			{
        		out = new ObjectOutputStream( out );
        		((ObjectOutputStream)out).writeObject( bean );
			}

			out.flush();

		}
		catch (IOException e)
		{
    		final String errorMsg = "IOException while trying to append to file";
    		throw new SmooksException( errorMsg, e );
		}
	}

}
