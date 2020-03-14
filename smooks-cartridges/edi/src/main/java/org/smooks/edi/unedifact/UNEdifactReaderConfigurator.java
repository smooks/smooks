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
package org.smooks.edi.unedifact;

import java.util.ArrayList;
import java.util.List;

import org.smooks.GenericReaderConfigurator;
import org.smooks.ReaderConfigurator;
import org.smooks.assertion.AssertArgument;
import org.smooks.cdr.SmooksResourceConfiguration;
import org.smooks.edi.EDIReader;

/**
 * UN/EDIFACT Reader configurator.
 * <p/>
 * Supports programmatic {@link EDIReader} configuration on a {@link org.smooks.Smooks#setReaderConfig(org.smooks.ReaderConfigurator) Smooks} instance.
 *
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class UNEdifactReaderConfigurator implements ReaderConfigurator {

    private String mappingModel;
    private String targetProfile;
    
    /**
     * Configure UNEdifactReader to dynamically look-up mapping
     * models in the classpath
     * 
     * @param mappingModel
     */
    public UNEdifactReaderConfigurator() {
        this.mappingModel = "";
    }

    
    
    /**
     * Specific mapping models
     * 
     * @param mappingModel
     */
    public UNEdifactReaderConfigurator(String mappingModel) {
        AssertArgument.isNotNullAndNotEmpty(mappingModel, "mappingModel");
        this.mappingModel = mappingModel;
    }

    public UNEdifactReaderConfigurator setTargetProfile(String targetProfile) {
        AssertArgument.isNotNullAndNotEmpty(targetProfile, "targetProfile");
        this.targetProfile = targetProfile;
        return this;
    }

    public List<SmooksResourceConfiguration> toConfig() {
        List<SmooksResourceConfiguration> configList = new ArrayList<SmooksResourceConfiguration>();

        // Add the reader config...
        GenericReaderConfigurator configurator = new GenericReaderConfigurator(UNEdifactReader.class);
        configurator.getParameters().setProperty("mappingModel", mappingModel);
        configurator.setTargetProfile(targetProfile);
        configList.addAll(configurator.toConfig());        

        return configList;
    }
}