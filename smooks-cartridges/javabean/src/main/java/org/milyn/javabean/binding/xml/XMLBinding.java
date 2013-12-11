/*
	Milyn - Copyright (C) 2006 - 2011

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
package org.milyn.javabean.binding.xml;

import org.milyn.Smooks;
import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.cdr.SmooksResourceConfigurationList;
import org.milyn.cdr.xpath.SelectorStep;
import org.milyn.cdr.xpath.SelectorStepBuilder;
import org.milyn.commons.assertion.AssertArgument;
import org.milyn.commons.cdr.SmooksConfigurationException;
import org.milyn.commons.javabean.DataDecoder;
import org.milyn.commons.javabean.DataEncoder;
import org.milyn.javabean.BeanInstanceCreator;
import org.milyn.javabean.BeanInstancePopulator;
import org.milyn.javabean.binding.AbstractBinding;
import org.milyn.javabean.binding.BeanSerializationException;
import org.milyn.javabean.binding.SerializationContext;
import org.milyn.javabean.binding.model.Bean;
import org.milyn.javabean.binding.model.Binding;
import org.milyn.javabean.binding.model.DataBinding;
import org.milyn.javabean.binding.model.ModelSet;
import org.milyn.javabean.binding.model.WiredBinding;
import org.milyn.javabean.binding.model.get.ConstantGetter;
import org.milyn.javabean.binding.model.get.GetterGraph;
import org.milyn.payload.StringSource;
import org.milyn.xml.NamespaceMappings;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * XML Binding class.
 * <p/>
 * This class is designed specifically for reading and writing XML data (does not work for other data formats)
 * to and from Java Object models using nothing more than standard &lt;jb:bean&gt; configurations i.e.
 * no need to write a template for serializing the Java Objects to an output character based format,
 * as with Smooks v1.4 and before.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 * @since 1.5
 */
public class XMLBinding extends AbstractBinding {

    private ModelSet beanModelSet;
    private List<XMLElementSerializationNode> graphs;
    private Set<QName> rootElementNames = new HashSet<QName>();
    private Map<Class, RootNodeSerializer> serializers = new LinkedHashMap<Class, RootNodeSerializer>();
    private boolean omitXMLDeclaration = false;

    /**
     * Public constructor.
     * <p/>
     * Must be followed by calls to the {@link #add(java.io.InputStream)} (or {@link #add(String)}) method
     * and then the {@link #intiailize()} method.
     */
    public XMLBinding() {
        super();
    }

    /**
     * Public constructor.
     * <p/>
     * Create an instance using a pre-configured Smooks instance.
     *
     * @param smooks The pre-configured Smooks instance.
     */
    public XMLBinding(Smooks smooks) {
        super(smooks);
    }

    @Override
    public XMLBinding add(String smooksConfigURI) throws IOException, SAXException {
        super.add(smooksConfigURI);
        return this;
    }

    @Override
    public XMLBinding add(InputStream smooksConfigStream) throws IOException, SAXException {
        return (XMLBinding) super.add(smooksConfigStream);
    }

    @Override
    public XMLBinding setReportPath(String reportPath) {
        super.setReportPath(reportPath);
        return this;
    }

    @Override
    public XMLBinding intiailize() {
        super.intiailize();

        beanModelSet = ModelSet.get(getSmooks().getApplicationContext());
        graphs = createExpandedXMLOutputGraphs(getUserDefinedResourceList());
        createRootSerializers(graphs);
        mergeBeanModelsIntoXMLGraphs();

        return this;
    }

    /**
     * Turn on/off outputting of the XML declaration when executing the {@link #toXML(Object, java.io.Writer)} method.
     *
     * @param omitXMLDeclaration True if the order is to be omitted, otherwise false.
     * @return <code>this</code> instance.
     */
    public XMLBinding setOmitXMLDeclaration(boolean omitXMLDeclaration) {
        this.omitXMLDeclaration = omitXMLDeclaration;
        return this;
    }

    /**
     * Bind from the XML into the Java Object model.
     *
     * @param inputSource The XML input.
     * @param toType      The Java type to which the XML data is to be bound.
     * @param <T>         The Java type to which the XML data is to be bound.
     * @return The populated Java instance.
     */
    public <T> T fromXML(String inputSource, Class<T> toType) {
        try {
            return bind(new StringSource(inputSource), toType);
        } catch (IOException e) {
            throw new IllegalStateException("Unexpected IOException from a String input.", e);
        }
    }

    /**
     * Bind from the XML into the Java Object model.
     *
     * @param inputSource The XML input.
     * @param toType      The Java type to which the XML data is to be bound.
     * @param <T>         The Java type to which the XML data is to be bound.
     * @return The populated Java instance.
     */
    public <T> T fromXML(Source inputSource, Class<T> toType) throws IOException {
        return bind(inputSource, toType);
    }

    /**
     * Write the supplied Object instance to XML.
     *
     * @param object       The Object instance.
     * @param outputWriter The output writer.
     * @param <W>          The Writer type.
     * @return The supplied {@link Writer} instance}.
     * @throws BeanSerializationException Error serializing the bean.
     * @throws IOException                Error writing to the supplied Writer instance.
     */
    public <W extends Writer> W toXML(Object object, W outputWriter) throws BeanSerializationException, IOException {
        AssertArgument.isNotNull(object, "object");
        assertInitialized();

        Class<? extends Object> objectClass = object.getClass();
        RootNodeSerializer rootNodeSerializer = serializers.get(objectClass);
        if (rootNodeSerializer == null) {
            throw new BeanSerializationException("No serializer for Java type '" + objectClass.getName() + "'.");
        }
        if (!omitXMLDeclaration) {
            outputWriter.write("<?xml version=\"1.0\"?>\n");
        }

        XMLElementSerializationNode serializer = rootNodeSerializer.serializer;
        serializer.serialize(outputWriter, new SerializationContext(object, rootNodeSerializer.beanId));
        outputWriter.flush();

        return outputWriter;
    }

    /**
     * Write the supplied Object instance to XML.
     * <p/>
     * This is a simple wrapper on the {@link #toXML(Object, java.io.Writer)} method.
     *
     * @param object The Object instance.
     * @return The XML as a String.
     * @throws BeanSerializationException Error serializing the bean.
     * @throws IOException                Error writing to the supplied Writer instance.
     */
    public String toXML(Object object) throws BeanSerializationException {
        StringWriter writer = new StringWriter();
        try {
            toXML(object, writer);
            return writer.toString();
        } catch (IOException e) {
            throw new IllegalStateException("Unexpected IOException writing to a StringWriter.", e);
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                throw new IllegalStateException("Unexpected IOException closing a StringWriter.", e);
            }
        }
    }

    private void mergeBeanModelsIntoXMLGraphs() {
        Set<Map.Entry<Class, RootNodeSerializer>> serializerSet = serializers.entrySet();

        for (Map.Entry<Class, RootNodeSerializer> rootNodeSerializer : serializerSet) {
            Bean model = beanModelSet.getModel(rootNodeSerializer.getKey());
            if (model == null) {
                throw new IllegalStateException("Unexpected error.  No Bean model for type '" + rootNodeSerializer.getKey().getName() + "'.");
            }
            merge(rootNodeSerializer.getValue().serializer, model);
        }
    }

    private void merge(XMLElementSerializationNode serializer, Bean bean) {
        boolean isCollection = bean.isCollection();

        for (Binding binding : bean.getBindings()) {
            BeanInstancePopulator populator = binding.getPopulator();

            if (!isCollection && binding instanceof DataBinding) {
                XMLSerializationNode node = serializer.findNode(populator.getConfig().getSelectorSteps());
                if (node != null) {
                    node.setGetter(constructContextualGetter((DataBinding) binding));
                    DataDecoder bindingDecoder = binding.getPopulator().getDecoder(getSmooks().createExecutionContext().getDeliveryConfig());
                    if (bindingDecoder instanceof DataEncoder) {
                        node.setEncoder((DataEncoder) bindingDecoder);
                    }
                }
            } else if (binding instanceof WiredBinding) {
                Bean wiredBean = ((WiredBinding) binding).getWiredBean();
                XMLElementSerializationNode node = (XMLElementSerializationNode) serializer.findNode(wiredBean.getCreator().getConfig().getSelectorSteps());

                if (node != null) {
                    if (isCollection) {
                        // Mark the node that creates the wiredBean as being a collection item node...
                        Bean collectionBean = wiredBean.getWiredInto();
                        GetterGraph getter = constructContextualGetter(collectionBean);

                        node.setIsCollection(true);
                        node.setCollectionGetter(wiredBean.getBeanId(), getter);
                    } else {
                        node.setGetter(constructContextualGetter(wiredBean));
                    }
                }

                merge(serializer, wiredBean);
            }
        }
    }

    private void createRootSerializers(List<XMLElementSerializationNode> graphs) {
        Collection<Bean> beanModels = beanModelSet.getModels().values();

        for (Bean model : beanModels) {
            BeanInstanceCreator creator = model.getCreator();
            SelectorStep[] selectorSteps = creator.getConfig().getSelectorSteps();
            XMLElementSerializationNode createNode = (XMLElementSerializationNode) findNode(graphs, selectorSteps);

            // Only create serializers for routed elements...
            if (rootElementNames.contains(createNode.getQName())) {
                createNode = ((XMLElementSerializationNode) createNode.clone());
                createNode.setParent(null);

                Class<?> beanClass = creator.getBeanRuntimeInfo().getPopulateType();
                if (!Collection.class.isAssignableFrom(beanClass)) {
                    // Ignore Collections... don't allow them to be serialized.... not enough type info.
                    serializers.put(beanClass, new RootNodeSerializer(creator.getBeanId(), createNode));
                    addNamespaceAttributes(createNode);
                }
            }
        }
    }

    private void addNamespaceAttributes(XMLElementSerializationNode serializer) {
        Properties namespaces = NamespaceMappings.getMappings(getSmooks().getApplicationContext());
        if (namespaces != null) {
            Enumeration<String> namespacePrefixes = (Enumeration<String>) namespaces.propertyNames();
            while (namespacePrefixes.hasMoreElements()) {
                String prefix = namespacePrefixes.nextElement();
                String namespace = namespaces.getProperty(prefix);
                QName nsAttributeName = new QName(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, prefix, XMLConstants.XMLNS_ATTRIBUTE);
                XMLAttributeSerializationNode nsAttribute = new XMLAttributeSerializationNode(nsAttributeName);

                serializer.getAttributes().add(nsAttribute);
                nsAttribute.setGetter(new ConstantGetter(namespace));
            }
        }
    }

    private List<XMLElementSerializationNode> createExpandedXMLOutputGraphs(SmooksResourceConfigurationList userConfigList) {
        List<XMLElementSerializationNode> graphRoots = new ArrayList<XMLElementSerializationNode>();

        for (int i = 0; i < userConfigList.size(); i++) {
            SmooksResourceConfiguration config = userConfigList.get(i);
            Object javaResource = config.getJavaResourceObject();

            if (javaResource instanceof BeanInstanceCreator) {
                assertSelectorOK(config);
                constructNodePath(config.getSelectorSteps(), graphRoots);
            } else if (javaResource instanceof BeanInstancePopulator) {
                assertSelectorOK(config);
                constructNodePath(config.getSelectorSteps(), graphRoots);
            }
        }

        return graphRoots;
    }

    private XMLSerializationNode constructNodePath(SelectorStep[] selectorSteps, List<XMLElementSerializationNode> graphRoots) {
        if (selectorSteps == null || selectorSteps.length == 0) {
            throw new IllegalStateException("Invalid binding configuration.  All <jb:bean> configuration elements must specify fully qualified selector paths (createOnElement, data, executeOnElement attributes etc.).");
        }

        SelectorStep rootSelectorStep = selectorSteps[0];
        XMLElementSerializationNode root = XMLElementSerializationNode.getElement(rootSelectorStep, graphRoots, true);

        if (selectorSteps.length > 1) {
            return root.getPathNode(selectorSteps, 1, true);
        } else if (rootSelectorStep.getTargetAttribute() != null) {
            // It's an attribute node...
            return XMLElementSerializationNode.addAttributeNode(root, rootSelectorStep, true);
        } else {
            return root;
        }
    }

    private XMLSerializationNode findNode(List<XMLElementSerializationNode> graphs, SelectorStep[] selectorSteps) {
        XMLElementSerializationNode root = XMLElementSerializationNode.getElement(selectorSteps[0], graphs, false);
        XMLSerializationNode node = root;

        if (selectorSteps.length > 1) {
            node = root.getPathNode(selectorSteps, 1, false);
        }

        if (node == null) {
            throw new IllegalStateException("Unexpected exception.  Failed to locate the node '" + SelectorStepBuilder.toString(selectorSteps) + "'.");
        }

        return node;
    }

    private void assertSelectorOK(SmooksResourceConfiguration config) {
        String selector = config.getSelector();

        if (selector != null) {
            if (selector.contains(SmooksResourceConfiguration.DOCUMENT_FRAGMENT_SELECTOR) || selector.contains(SmooksResourceConfiguration.LEGACY_DOCUMENT_FRAGMENT_SELECTOR)) {
                throw new SmooksConfigurationException("Cannot use the document selector with the XMLBinding class.  Must use an absolute path.  Selector value '" + selector + "'.");
            }
            if (!selector.startsWith("/") && !selector.startsWith("${") && !selector.startsWith("#")) {
                throw new SmooksConfigurationException("Invalid selector value '" + selector + "'.  Selector paths must be absolute.");
            }
            rootElementNames.add(config.getSelectorSteps()[0].getTargetElement());
        }
    }

    private class RootNodeSerializer {
        private String beanId;
        private XMLElementSerializationNode serializer;

        private RootNodeSerializer(String beanId, XMLElementSerializationNode serializer) {
            this.beanId = beanId;
            this.serializer = serializer;
        }
    }
}
