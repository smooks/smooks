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

package org.milyn.javabean.dynamic.serialize.freemarker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.milyn.cdr.annotation.AppContext;
import org.milyn.cdr.annotation.ConfigParam;
import org.milyn.container.ApplicationContext;
import org.milyn.delivery.annotation.Initialize;
import org.milyn.commons.io.StreamUtils;
import org.milyn.javabean.dynamic.BeanMetadata;
import org.milyn.javabean.dynamic.BeanRegistrationException;
import org.milyn.javabean.dynamic.Model;
import org.milyn.javabean.dynamic.serialize.BeanWriter;
import org.milyn.commons.util.FreeMarkerTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * FreeMarker bean writer.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class FreeMarkerBeanWriter implements BeanWriter {

    private static Log logger = LogFactory.getLog(FreeMarkerBeanWriter.class);

    public static final String MODEL_CTX_KEY = "dyna_model_inst";

    @AppContext
    private ApplicationContext appContext;
    @ConfigParam(name = "template")
    private String templateConfig;

    private FreeMarkerTemplate template;

    private static final WriteNamespacesDirective writeNamespacesDirective = new WriteNamespacesDirective();
    private static final WriteBeanDirective writeBeanDirective = new WriteBeanDirective();
    private static final WriteBeanPreTextDirective writePreTextDirective = new WriteBeanPreTextDirective();
    private static final WriteAttribsDirective writeAttribsDirective = new WriteAttribsDirective();

    @Initialize
    public void intialize() {
        String trimmedTemplateConfig = templateConfig.trim();

        // Only attempt to load as a template resource URI if the configured 'template'
        // value is all on one line.  If it has line breaks then we know it's not an
        // external resource...
        if(trimmedTemplateConfig.trim().indexOf('\n') == -1) {
            try {
                InputStream templateStream = appContext.getResourceLocator().getResource(trimmedTemplateConfig);
                if(templateStream != null) {
                    templateConfig = StreamUtils.readStreamAsString(templateStream);                    
                }
            } catch (IOException e) {
                logger.debug("'template' configuration value '" + trimmedTemplateConfig + "' does not resolve to an external FreeMarker template.  Using configured value as the actual template.");
            }
        }

        // Create the template instance...
        template = new FreeMarkerTemplate(templateConfig);
    }

    public void write(Object bean, Writer writer, Model model) throws BeanRegistrationException, IOException {
        Map<String, Object> templateContext = new HashMap<String, Object>();
        BeanMetadata beanMetadata = model.getBeanMetadata(bean);

        if(beanMetadata == null) {
            BeanRegistrationException.throwUnregisteredBeanInstanceException(bean);
        }

        templateContext.put("bean", bean);
        templateContext.put(MODEL_CTX_KEY, model);
        templateContext.put("nsp", beanMetadata.getNamespacePrefix());

        templateContext.put("writeNamespaces", writeNamespacesDirective);
        templateContext.put("writeBean", writeBeanDirective);
        templateContext.put("writePreText", writePreTextDirective);
        templateContext.put("writeAttribs", writeAttribsDirective);

        template.apply(templateContext, writer);
    }
}
