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

package example;

import org.milyn.javabean.dynamic.BeanMetadata;
import org.milyn.javabean.dynamic.Model;
import org.milyn.javabean.dynamic.ModelBuilder;
import org.smooks.model.core.SmooksModel;
import org.smooks.model.javabean.Bean;
import org.xml.sax.SAXException;

import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Main example class.
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class Main {

    public static void main(String[] args) throws IOException, SAXException {

        ModelBuilder modelBuilder = new ModelBuilder("META-INF/org/smooks/model/descriptor.properties", false);
        Model<SmooksModel> model;
        SmooksModel smooksModel;

        // Read an instance of the model...
        model = modelBuilder.readModel(new FileReader("smooks-config.xml"), SmooksModel.class);
        smooksModel = model.getModelRoot();

        // Make modifications to the smooksModel instance etc....
        List<Bean> beans = smooksModel.getBeans();
        for(Bean bean : beans) {
            BeanMetadata beanMetadata = model.getBeanMetadata(bean);
            // etc ...
        }

        // Serialize the model back out...
        model.writeModel(new PrintWriter(System.out));
    }
}
