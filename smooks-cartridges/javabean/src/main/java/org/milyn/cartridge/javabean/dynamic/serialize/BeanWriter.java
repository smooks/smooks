/*
 * Milyn - Copyright (C) 2006 - 2010
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License (version 2.1) as published by the Free Software
 *  Foundation.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 *  See the GNU Lesser General Public License for more details:
 *  http://www.gnu.org/licenses/lgpl.txt
 */

package org.milyn.cartridge.javabean.dynamic.serialize;

import org.milyn.cartridge.javabean.dynamic.BeanRegistrationException;
import org.milyn.cartridge.javabean.dynamic.Model;

import java.io.IOException;
import java.io.Writer;

/**
 * Bean Serializer.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public interface BeanWriter {

    /**
     * Write the specified bean to the supplied writer.
     * @param bean The bean instance.
     * @param writer The target writer.
     * @param model The {@link Model} instance that "owns" the bean.
     * @throws org.milyn.cartridge.javabean.dynamic.BeanRegistrationException Unknown bean instance.
     * @throws IOException Error writing bean to {@link Writer writer}.
     */
    void write(Object bean, Writer writer, Model model) throws BeanRegistrationException, IOException;
}