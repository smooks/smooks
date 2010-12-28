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
package org.milyn.smooks.edi.unedifact;

import org.milyn.GenericReaderConfigurator;
import org.milyn.ReaderConfigurator;
import org.milyn.assertion.AssertArgument;
import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.smooks.edi.EDIReader;
import org.milyn.smooks.edi.ModelLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * UN/EDIFACT Reader configurator.
 * <p/>
 * Supports programmatic {@link org.milyn.smooks.edi.EDIReader} configuration on a {@link org.milyn.Smooks#setReaderConfig(org.milyn.ReaderConfigurator) Smooks} instance.
 *
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class UNEdifactReaderConfigurator implements ReaderConfigurator {

    private String mappingModel;
    private String targetProfile;

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

        // Add the ModelLoader config...
        SmooksResourceConfiguration modelLoaderResource = new SmooksResourceConfiguration();
        modelLoaderResource.setResource(ModelLoader.class.getName());
        modelLoaderResource.setParameter("mappingModel", mappingModel);
        configList.add(modelLoaderResource);

        // Add the reader config...
        GenericReaderConfigurator configurator = new GenericReaderConfigurator(UNEdifactReader.class);
        configurator.getParameters().setProperty("mappingModel", mappingModel);
        configurator.setTargetProfile(targetProfile);
        configList.addAll(configurator.toConfig());        

        return configList;
    }
}