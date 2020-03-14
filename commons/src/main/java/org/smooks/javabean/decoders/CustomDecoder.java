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
package org.smooks.javabean.decoders;

import org.smooks.javabean.DataDecoder;
import org.smooks.javabean.DataDecodeException;
import org.smooks.config.Configurable;
import org.smooks.cdr.SmooksConfigurationException;

import java.util.Properties;

/**
 * Custom decoder.
 * <p/>
 * Specify a delegate decoder class in the properties.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class CustomDecoder implements DataDecoder, Configurable {

    public static final String CLASS_PROPERTY_NAME = "decoderClass";
    private Properties configuration;
    private DataDecoder delegateDecoder;

    public void setConfiguration(Properties config) throws SmooksConfigurationException {
        String className = config.getProperty(CLASS_PROPERTY_NAME);

        if(className == null) {
            throw new SmooksConfigurationException("Mandatory property '" + CLASS_PROPERTY_NAME + "' not specified.");
        }

        delegateDecoder = DataDecoder.Factory.create(className);

        //Set configuration in delegateDecoder.
        if (delegateDecoder instanceof Configurable) {
            ((Configurable)delegateDecoder).setConfiguration(config);
        }

        this.configuration = config;
    }

    public Properties getConfiguration() {
        return configuration;
    }

    public Object decode(String data) throws DataDecodeException {
        return delegateDecoder.decode(data);
    }

    public DataDecoder getDelegateDecoder() {
        return delegateDecoder;
    }
}