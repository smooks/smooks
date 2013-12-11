/*
 * Milyn - Copyright (C) 2006 - 2010
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License (version 2.1) as published by the Free Software
 * Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU Lesser General Public License for more details:
 * http://www.gnu.org/licenses/lgpl.txt
 */

package org.milyn.flatfile.variablefield;

import org.milyn.GenericReaderConfigurator;
import org.milyn.commons.cdr.SmooksConfigurationException;
import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.flatfile.Binding;
import org.milyn.flatfile.BindingType;
import org.milyn.flatfile.FlatFileReader;

import java.util.List;

/**
 * Abstract Variable Field Record Parser configurator.
 *
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public abstract class VariableFieldRecordParserConfigurator extends GenericReaderConfigurator {

    private Class<? extends VariableFieldRecordParserFactory> factoryParserClass;
    private boolean indent = false;
    private boolean strict = true;
    private boolean fieldsInMessage = false;
    private Binding binding;

    public VariableFieldRecordParserConfigurator(Class<? extends VariableFieldRecordParserFactory> factoryParserClass) {
        super(FlatFileReader.class);
        this.factoryParserClass = factoryParserClass;
    }

    public VariableFieldRecordParserConfigurator setIndent(boolean indent) {
        this.indent = indent;
        return this;
    }

    public VariableFieldRecordParserConfigurator setStrict(boolean strict) {
        this.strict = strict;
        return this;
    }

    public VariableFieldRecordParserConfigurator setFieldsInMessage(boolean fieldsInMessage) {
        this.fieldsInMessage = fieldsInMessage;
        return this;
    }

    public VariableFieldRecordParserConfigurator setBinding(Binding binding) {
        this.binding = binding;
        return this;
    }

    public List<SmooksResourceConfiguration> toConfig() {
        getParameters().setProperty("parserFactory", factoryParserClass.getName());
        getParameters().setProperty("indent", Boolean.toString(indent));
        getParameters().setProperty("strict", Boolean.toString(strict));
        getParameters().setProperty("fields-in-message", Boolean.toString(fieldsInMessage));

        if(binding != null) {
            getParameters().setProperty("bindBeanId", binding.getBeanId());
            getParameters().setProperty("bindBeanClass", binding.getBeanClass().getName());
            getParameters().setProperty("bindingType", binding.getBindingType().toString());
            if(binding.getBindingType() == BindingType.MAP) {
                if(binding.getKeyField() == null) {
                    throw new SmooksConfigurationException("CSV 'MAP' Binding must specify a 'keyField' property on the binding configuration.");
                }
                getParameters().setProperty("bindMapKeyField", binding.getKeyField());
            }
        }

        return super.toConfig();
    }
}
