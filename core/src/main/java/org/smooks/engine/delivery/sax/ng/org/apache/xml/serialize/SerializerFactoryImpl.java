/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.smooks.engine.delivery.sax.ng.org.apache.xml.serialize;


import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.smooks.engine.delivery.sax.ng.org.apache.xerces.dom.DOMMessageFormatter;

/**
 * Default serializer factory can construct serializers for the three
 * markup serializers (XML, HTML, XHTML ).
 *
 * @deprecated This class was deprecated in Xerces 2.9.0. It is recommended 
 * that new applications use the DOM Level 3 LSSerializer or JAXP's Transformation 
 * API for XML (TrAX) for serializing XML and HTML. See the Xerces documentation for more 
 * information.
 * @version $Revision$ $Date$
 * @author <a href="mailto:Scott_Boag/CAM/Lotus@lotus.com">Scott Boag</a>
 * @author <a href="mailto:arkin@intalio.com">Assaf Arkin</a>
 */
final class SerializerFactoryImpl
    extends SerializerFactory
{


    private String _method;
    
    
    SerializerFactoryImpl( String method )
    {
        _method = method;
        if ( ! _method.equals( Method.XML ) &&
             ! _method.equals( Method.HTML ) &&
             ! _method.equals( Method.XHTML ) &&
             ! _method.equals( Method.TEXT ) ) {
            String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.SERIALIZER_DOMAIN, "MethodNotSupported", new Object[]{method});
            throw new IllegalArgumentException(msg);
        }
    }


    public Serializer makeSerializer(OutputFormat format )
    {
        Serializer serializer;
        
        serializer = getSerializer( format );
        serializer.setOutputFormat( format );
        return serializer;
    }
    
    
    
    public Serializer makeSerializer(Writer writer,
                                                              OutputFormat format )
    {
        Serializer serializer;
        
        serializer = getSerializer( format );
        serializer.setOutputCharStream( writer );
        return serializer;
    }
    
    
    public Serializer makeSerializer(OutputStream output,
                                                              OutputFormat format )
        throws UnsupportedEncodingException
    {
        Serializer serializer;
        
        serializer = getSerializer( format );
        serializer.setOutputByteStream( output );
        return serializer;
    }
    
    
    private Serializer getSerializer(OutputFormat format )
    {
        if ( _method.equals( Method.XML ) ) {
            return new XMLSerializer( format );
        } else if ( _method.equals( Method.HTML ) ) {
            return new HTMLSerializer( format );
        }  else if ( _method.equals( Method.XHTML ) ) {
            return new XHTMLSerializer( format );
        }  else if ( _method.equals( Method.TEXT ) ) {
            return new TextSerializer();
        } else {
            String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.SERIALIZER_DOMAIN, "MethodNotSupported", new Object[]{_method});
            throw new IllegalStateException(msg);
        }
    }
    
    
    protected String getSupportedMethod()
    {
        return _method;
    }


}

