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

package org.milyn.cartridge.javabean.dynamic;

import org.milyn.commons.SmooksException;

/**
 * Bean Registration Exception.
 * <p/>
 * <b>See factory methods.</b>.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class BeanRegistrationException extends SmooksException {

    /**
     * Private constructor.
     * @param message Exception message.
     */
    private BeanRegistrationException(String message) {
        super(message);
    }

    /**
     * Throw a {@link BeanRegistrationException} exception for the specified "unregistered" bean instance.
     * <p/>
     * This exception is thrown when one of the root bean instances for a namespace used within
     * a model doesn't have registered {@link BeanMetadata}
     * (via the {@link Model#registerBean(Object)}).
     *
     * @param bean The unknown bean instance.
     * @throws BeanRegistrationException The exception.
     */
    public static void throwUnregisteredBeanInstanceException(Object bean) throws BeanRegistrationException {
        throw new BeanRegistrationException("No BeanMetaData is registered for the specified bean instance.  Bean type '" + bean.getClass().getName() + "'.  All namespace 'root' bean instances in the Model must have registered BeanMetaData via the 'Model.registerBean(Object)' method.");
    }

    /**
     * Throw a {@link BeanRegistrationException} exception for the specified bean instance that
     * is already registered.
     *
     * @param bean The bean instance.
     * @throws BeanRegistrationException The exception.
     */
    public static void throwBeanInstanceAlreadyRegisteredException(Object bean) throws BeanRegistrationException {
        throw new BeanRegistrationException("The specified bean instance is already registered with the model.  Bean type '" + bean.getClass().getName() + "'.");
    }

    /**
     * Throw a {@link BeanRegistrationException} exception for a bean that is not annotated with the
     * {@link org.milyn.cartridge.javabean.dynamic.serialize.DefaultNamespace} annotation.
     * <p/>
     * All namespace root bean types must be annotated with the {@link org.milyn.cartridge.javabean.dynamic.serialize.DefaultNamespace}
     * annotation.
     *
     * @param bean The bean instance.
     * @throws BeanRegistrationException The exception.
     */
    public static void throwBeanNotAnnotatedWithDefaultNamespace(Object bean) throws BeanRegistrationException {
        throw new BeanRegistrationException("Bean type '" + bean.getClass().getName() + "' cannot be registered via Model.registerBean(Object) because it's not annotated with the @DefaultNamespace annotation.");
    }
}
