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

package org.milyn.cartridge.javabean.dynamic.ext;

import org.milyn.commons.SmooksException;
import org.milyn.commons.cdr.SmooksConfigurationException;
import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.cdr.extension.ExtensionContext;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.dom.DOMVisitBefore;
import org.milyn.cartridge.javabean.ext.BeanConfigUtil;
import org.w3c.dom.Element;

/**
 * Bean class lookup visitor.
 * <p/>
 * Used during processing of the <dmb:writer> extended DMB configuration
 * for looking up the actual bean runtime Class from the beanId
 * specified on the on the <dmb:writer>.
 *
 * <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class BeanClassLookup implements DOMVisitBefore {

    public void visitBefore(Element element, ExecutionContext executionContext) throws SmooksException {
        // The current config on the stack must be <dmb:writer>...
        ExtensionContext extensionContext = ExtensionContext.getExtensionContext(executionContext);
        SmooksResourceConfiguration dmbWriterConfig = extensionContext.getResourceStack().peek();
        if(dmbWriterConfig.getStringParameter("beanClass") == null) {
            String beanId = dmbWriterConfig.getStringParameter("beanId");

            if(beanId == null) {
                throw new SmooksConfigurationException("One of the 'beanClass' or 'beanId' attributes must be configured on the <dmb:writer> configuration.");                
            }

            SmooksResourceConfiguration beanCreatorConfig = BeanConfigUtil.findBeanCreatorConfig(beanId, executionContext);
            if(beanCreatorConfig == null) {
                throw new SmooksConfigurationException("Cannot find <jb:bean> configuration for beanId '" + beanId + "' for <dmb:writer>.  Reordered <dmb:writer> after <jb:bean> config.");
            }

            String beanClass = beanCreatorConfig.getStringParameter(BeanConfigUtil.BEAN_CLASS_CONFIG);
            if(beanClass == null) {
                throw new SmooksConfigurationException("Cannot create find BeanWriter for beanId '" + beanId + "'.  The associated <jb:bean> configuration does not define a bean Class name.");
            }

            dmbWriterConfig.setParameter(BeanConfigUtil.BEAN_CLASS_CONFIG, beanClass);
        }
    }
}
