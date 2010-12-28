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
package org.milyn.ejc;

import org.milyn.assertion.AssertArgument;
import org.milyn.util.FreeMarkerTemplate;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.*;

/**
 * BindingWriter generates a bindingfile based on classstructure found in ClassModel.
 * @author bardl
 */
public class BindingWriter {

    private ClassModel classModel;
    private FreeMarkerTemplate template = new FreeMarkerTemplate("templates/bindingConfig.ftl.xml", BindingWriter.class);

    public BindingWriter(ClassModel classModel) throws ClassNotFoundException {
        AssertArgument.isNotNull(classModel, "classModel");

        this.classModel = classModel;
    }


    public void generate(String bindingfile) throws IOException {

        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(bindingfile));
            writeBindingConfig(writer);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

	public static void writeBindingConfig(ClassModel classModel, Writer writer) throws IOException, ClassNotFoundException {
		(new BindingWriter(classModel)).writeBindingConfig(writer);
        writer.flush();
	}
	
	public void writeBindingConfig(Writer writer) throws IOException {
		Map<String, Object> templatingContextObject = new HashMap<String, Object>();
		List<BindingConfig> beanConfigs = new ArrayList<BindingConfig>();

		flattenBeanConfigGraph(beanConfigs, classModel.getRootBeanConfig());

		templatingContextObject.put("beanConfigs", beanConfigs);
		templatingContextObject.put("classPackage", classModel.getRootBeanConfig().getBeanClass().getPackageName().replace('.', '/'));
		writer.write(template.apply(templatingContextObject));
	}

    private void flattenBeanConfigGraph(List<BindingConfig> beanConfigs, BindingConfig beanConfig) {
        beanConfigs.add(beanConfig);
        for(BindingConfig wiredConfig : beanConfig.getWireBindings()) {
            flattenBeanConfigGraph(beanConfigs, wiredConfig);
        }
    }

    private List<String> parsePackages(String packagesString) {
        String[] packages = packagesString.split(";");
        List<String> packagesSet = new ArrayList<String>();

        for(String aPackage : packages) {
            packagesSet.add(aPackage.trim());
        }

        return packagesSet;
    }
}

