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

package org.milyn.javabean.dynamic;

import org.milyn.Smooks;
import org.milyn.commons.SmooksException;
import org.milyn.commons.assertion.AssertArgument;
import org.milyn.commons.cdr.SmooksConfigurationException;
import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.cdr.SmooksResourceConfigurationList;
import org.milyn.cdr.XMLConfigDigester;
import org.milyn.cdr.xpath.SelectorStep;
import org.milyn.javabean.dynamic.ext.BeanWriterFactory;
import org.milyn.javabean.dynamic.resolvers.AbstractResolver;
import org.milyn.javabean.dynamic.resolvers.DefaultBindingConfigResolver;
import org.milyn.javabean.dynamic.resolvers.DefaultSchemaResolver;
import org.milyn.javabean.dynamic.serialize.BeanWriter;
import org.milyn.commons.util.ClassUtil;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

/**
 * Model Descriptor.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class Descriptor {

    public static final String DESCRIPTOR_NAMESPACE_POSTFIX = ".namespace";
    public static final String DESCRIPTOR_SCHEMA_LOCATION_POSTFIX = ".schemaLocation";
    public static final String DESCRIPTOR_BINDING_CONFIG_LOCATION_POSTFIX = ".bindingConfigLocation";
    public static final String DESCRIPTOR_ORDER_POSTFIX = ".order";

    private Smooks smooks;
    private Schema schema;
    private ClassLoader classloader = Descriptor.class.getClassLoader();

    public Descriptor(List<Properties> descriptors) throws SAXException, IOException {
        AssertArgument.isNotNullAndNotEmpty(descriptors, "descriptors");

        intialize(descriptors, new DefaultSchemaResolver(descriptors), new DefaultBindingConfigResolver(descriptors));
    }

    public Descriptor(String descriptorPath) throws SAXException, IOException {
		AssertArgument.isNotNullAndNotEmpty(descriptorPath, "descriptorPath");

		List<Properties> descriptors = loadDescriptors(descriptorPath, getClass().getClassLoader());
		intialize(descriptors, new DefaultSchemaResolver(descriptors), new DefaultBindingConfigResolver(descriptors));
	}

	public Descriptor(String descriptorPath, EntityResolver schemaResolver, EntityResolver bindingResolver, ClassLoader classloader) throws SAXException, IOException {
		AssertArgument.isNotNullAndNotEmpty(descriptorPath, "descriptorPath");
		AssertArgument.isNotNull(bindingResolver, "bindingResolver");
        AssertArgument.isNotNull(classloader, "classloader");

        this.classloader = classloader;

		List<Properties> descriptors = loadDescriptors(descriptorPath, classloader);
		intialize(descriptors, schemaResolver, bindingResolver);
	}

    public Descriptor(List<Properties> descriptors, EntityResolver schemaResolver, EntityResolver bindingResolver, ClassLoader classloader) throws SAXException, IOException {
        AssertArgument.isNotNullAndNotEmpty(descriptors, "descriptors");
        AssertArgument.isNotNull(bindingResolver, "bindingResolver");
        AssertArgument.isNotNull(classloader, "classloader");

        this.classloader = classloader;

        intialize(descriptors, schemaResolver, bindingResolver);
    }

    public Smooks getSmooks() {
        return smooks;
    }

    public Schema getSchema() {
        return schema;
    }

    public Map<Class<?>, Map<String, BeanWriter>> getBeanWriters() {
        return BeanWriterFactory.getBeanWriters(smooks.getApplicationContext());
    }

    public static List<Properties> loadDescriptors(String descriptorPath, ClassLoader classLoader) {
        List<Properties> descriptorFiles = new ArrayList<Properties>();

        try {
            List<URL> resources = ClassUtil.getResources(descriptorPath, classLoader);

            if(resources.isEmpty()) {
                throw new IllegalStateException("Failed to locate any model descriptor file by the name '" + descriptorPath + "' on the classpath.");
            }

            for(URL resource : resources) {
                InputStream resStream = resource.openStream();
                descriptorFiles.add(loadDescriptor(resStream));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unexpected IO Exception when reading Dynamic Namespace Descriptor files from classpath.", e);
        }

        return descriptorFiles;
    }

    public static Properties loadDescriptor(InputStream descriptorStream) throws IOException {
        AssertArgument.isNotNull(descriptorStream, "descriptorStream");
        try {
            Properties descriptor = new Properties();
            descriptor.load(descriptorStream);
            return descriptor;
        } finally {
            descriptorStream.close();
        }
    }

    private void intialize(List<Properties> descriptors, EntityResolver schemaResolver, EntityResolver bindingResolver) throws SAXException, IOException {

        if(schemaResolver instanceof AbstractResolver) {
            if(((AbstractResolver)schemaResolver).getClassLoader() != classloader) {
                throw new SmooksException("Schema EntityResolver '" + schemaResolver.getClass().getName() + "' not using the same ClassLoader as this Descriptor instance.");
            }
        }
        if(bindingResolver instanceof AbstractResolver) {
            if(((AbstractResolver)bindingResolver).getClassLoader() != classloader) {
                throw new SmooksException("Binding EntityResolver '" + bindingResolver.getClass().getName() + "' not using the same ClassLoader as this Descriptor instance.");
            }
        }

        if(schemaResolver != null) {
            this.schema = newSchemaInstance(descriptors, schemaResolver);
        }
		this.smooks = newSmooksInstance(descriptors, bindingResolver);
	}

    private Schema newSchemaInstance(List<Properties> descriptors, EntityResolver schemaResolver) throws SAXException, IOException {
        List<Source> schemas = getSchemas(descriptors, schemaResolver);

        try {
            // Create the merged Schema instance and from that, create the Validator instance...
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            return schemaFactory.newSchema(schemas.toArray(new Source[schemas.size()]));
        } finally {
            for(Source schemaSource : schemas) {
                if(schemaSource instanceof StreamSource) {
                    StreamSource streamSource = (StreamSource)schemaSource;
                    if(streamSource.getInputStream() != null) {
                        streamSource.getInputStream().close();
                    } else if(streamSource.getReader() != null) {
                        streamSource.getReader().close();
                    }
                }
            }
        }
    }

    private List<Source> getSchemas(List<Properties> descriptors, EntityResolver schemaResolver) throws SAXException, IOException {
        Set<Namespace> namespaces = resolveNamespaces(descriptors);
        List<Source> xsdSources = new ArrayList<Source>();

        for (Namespace namespace : namespaces) {
            InputSource schemaSource = schemaResolver.resolveEntity(namespace.uri, namespace.uri);

            if(schemaSource != null) {
                if(schemaSource.getByteStream() != null) {
                    xsdSources.add(new StreamSource(schemaSource.getByteStream()));
                } else if(schemaSource.getCharacterStream() != null) {
                    xsdSources.add(new StreamSource(schemaSource.getCharacterStream()));
                } else {
                    throw new SAXException("Schema resolver '" + schemaResolver.getClass().getName() + "' failed to resolve schema for namespace '" + namespace + "'.  Resolver must return a Reader or InputStream in the InputSource.");
                }
            }
        }

        return xsdSources;
    }

    private Smooks newSmooksInstance(List<Properties> descriptors, EntityResolver bindingResolver) throws SAXException, IOException, SmooksConfigurationException {
        AssertArgument.isNotNullAndNotEmpty(descriptors, "descriptors");
        AssertArgument.isNotNull(bindingResolver, "bindingResolver");

        Set<Namespace> namespaces = resolveNamespaces(descriptors);
        Map<String, Smooks> extendedConfigDigesters = new HashMap<String, Smooks>();

        // Now create a Smooks instance for processing configurations for these namespaces...
        Smooks smooks = new Smooks();

        smooks.setClassLoader(classloader);
        
        for (Namespace namespace : namespaces) {
            InputSource bindingSource = bindingResolver.resolveEntity(namespace.uri, namespace.uri);

            if(bindingSource != null) {
                if(bindingSource.getByteStream() != null) {
                    SmooksResourceConfigurationList configList;

                    try {
                        configList = XMLConfigDigester.digestConfig(bindingSource.getByteStream(), "./", extendedConfigDigesters, classloader);
                        for(int i = 0; i < configList.size(); i++) {
                            SmooksResourceConfiguration config = configList.get(i);
                            
                            if(config.getSelectorNamespaceURI() == null) {
                                SelectorStep selectorStep = config.getSelectorStep();

                                // And if there isn't a namespace prefix specified on the element (unresolved at this point),
                                // then assign the binding config namespace...
                                if(selectorStep.getTargetElement().getPrefix().equals(XMLConstants.DEFAULT_NS_PREFIX)) {
                                    config.setSelectorNamespaceURI(namespace.uri);
                                }
                            }
                        }
                    } catch (URISyntaxException e) {
                        throw new SmooksConfigurationException("Unexpected configuration digest exception.", e);
                    }

                    smooks.getApplicationContext().getStore().addSmooksResourceConfigurationList(configList);
                } else {
                    throw new SAXException("Binding configuration resolver '" + bindingResolver.getClass().getName() + "' failed to resolve binding configuration for namespace '" + namespace + "'.  Resolver must return an InputStream in the InputSource.");
                }
            }
        }

        return smooks;
    }

    private static Set<Namespace> resolveNamespaces(List<Properties> descriptors) {
        List<Namespace> namespaces = new ArrayList<Namespace>();

        for(Properties descriptor : descriptors) {
            extractNamespaceDecls(descriptor, namespaces);
        }

        Comparator<Namespace> namspaceSorter = new Comparator<Namespace>() {
            public int compare(Namespace o1, Namespace o2) {
                return o1.order - o2.order;
            }
        };

        Namespace[] namespaceArray = new Namespace[namespaces.size()];
        namespaces.toArray(namespaceArray);
        Arrays.sort(namespaceArray, namspaceSorter);

        Set<Namespace> orderedNamespaceSet = new LinkedHashSet<Namespace>();
        orderedNamespaceSet.addAll(Arrays.asList(namespaceArray));

        return orderedNamespaceSet;
    }

    private static List<Namespace> extractNamespaceDecls(Properties descriptor, List<Namespace> namespaces) {
        Set<Map.Entry<Object, Object>> properties = descriptor.entrySet();
        for(Map.Entry<Object, Object> property: properties) {
            String key = ((String) property.getKey()).trim();
            if(key.endsWith(DESCRIPTOR_NAMESPACE_POSTFIX)) {
                Namespace namespace = new Namespace();
                String namespaceUri = (String) property.getValue();
                String namespaceId = getNamespaceId(namespaceUri, descriptor);

                if(namespaceId == null) {
                    throw new SmooksConfigurationException("Unable to resolve namespace ID for namespace URI '" + namespaceUri + "'.");
                }

                String namespaceOrder = descriptor.getProperty(namespaceId + DESCRIPTOR_ORDER_POSTFIX, Integer.toString(Integer.MAX_VALUE)).trim();

                namespace.uri = namespaceUri;
                try {
                    namespace.order = Integer.parseInt(namespaceOrder);
                } catch(NumberFormatException e) {
                    throw new SmooksConfigurationException("Invalid value for descriptor config value '" + namespaceId + DESCRIPTOR_ORDER_POSTFIX + "'.  Must be a valid Integer value.");
                }

                namespaces.add(namespace);
            }
        }

        return namespaces;
    }

    public static String getNamespaceId(String namespaceURI, List<Properties> descriptors) {
        for(Properties descriptor : descriptors) {
            String id = getNamespaceId(namespaceURI, descriptor);
            if(id != null) {
                return id;
            }
        }
        return null;
    }

    private static String getNamespaceId(String namespaceURI, Properties descriptor) {
        Set<Map.Entry<Object, Object>> properties = descriptor.entrySet();
        for(Map.Entry<Object, Object> property: properties) {
            String key = ((String) property.getKey()).trim();
            String value = ((String) property.getValue()).trim();
            if(key.endsWith(DESCRIPTOR_NAMESPACE_POSTFIX) && value.equals(namespaceURI)) {
                return key.substring(0, (key.length() - DESCRIPTOR_NAMESPACE_POSTFIX.length()));
            }
        }
        return null;
    }

    public static String getSchemaLocation(String namespaceId, List<Properties> descriptors) {
        return getDescriptorValue(namespaceId + DESCRIPTOR_SCHEMA_LOCATION_POSTFIX, descriptors);
    }

    public static String getBindingConfigLocation(String namespaceId, List<Properties> descriptors) {
        return getDescriptorValue(namespaceId + DESCRIPTOR_BINDING_CONFIG_LOCATION_POSTFIX, descriptors);
    }

    private static String getDescriptorValue(String name, List<Properties> descriptors) {
        for(Properties descriptor : descriptors) {
            String value = descriptor.getProperty(name);
            if(value != null) {
                return value;
            }
        }

        return null;
    }

    private static class Namespace {
        private String uri;
        private int order;
    }
}
