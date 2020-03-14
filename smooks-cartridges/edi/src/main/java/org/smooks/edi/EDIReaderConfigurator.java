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
package org.smooks.edi;

import java.util.List;

import org.smooks.GenericReaderConfigurator;
import org.smooks.ReaderConfigurator;
import org.smooks.assertion.AssertArgument;
import org.smooks.cdr.SmooksResourceConfiguration;

/**
 * EDI Reader configurator.
 * <p/>
 * Supports programmatic {@link EDIReader} configuration on a {@link org.smooks.Smooks#setReaderConfig(org.smooks.ReaderConfigurator) Smooks} instance.
 *
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class EDIReaderConfigurator implements ReaderConfigurator {

    private String mappingModel;
    private String targetProfile;

    public EDIReaderConfigurator(String mappingModel) {
        AssertArgument.isNotNullAndNotEmpty(mappingModel, "mappingModel");
        this.mappingModel = mappingModel;
    }

    public EDIReaderConfigurator setTargetProfile(String targetProfile) {
        AssertArgument.isNotNullAndNotEmpty(targetProfile, "targetProfile");
        this.targetProfile = targetProfile;
        return this;
    }

    public List<SmooksResourceConfiguration> toConfig() {
        GenericReaderConfigurator configurator = new GenericReaderConfigurator(EDIReader.class);

        configurator.getParameters().setProperty(EDIReader.MODEL_CONFIG_KEY, mappingModel);
        configurator.setTargetProfile(targetProfile);

        return configurator.toConfig();
    }
}