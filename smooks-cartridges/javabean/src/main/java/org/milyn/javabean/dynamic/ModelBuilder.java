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
package org.milyn.javabean.dynamic;

import java.io.*;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.milyn.FilterSettings;
import org.milyn.Smooks;
import org.milyn.SmooksException;
import org.milyn.assertion.AssertArgument;
import org.milyn.cdr.ParameterAccessor;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.Fragment;
import org.milyn.event.report.HtmlReportGenerator;
import org.milyn.javabean.BeanInstancePopulator;
import org.milyn.javabean.dynamic.serialize.BeanWriter;
import org.milyn.javabean.dynamic.visitor.NamespaceReaper;
import org.milyn.javabean.dynamic.visitor.UnknownElementDataReaper;
import org.milyn.javabean.lifecycle.BeanContextLifecycleEvent;
import org.milyn.javabean.lifecycle.BeanContextLifecycleObserver;
import org.milyn.javabean.lifecycle.BeanLifecycle;
import org.milyn.payload.JavaResult;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Dynamic Model Builder.
 * <p/>
 * Useful for constructing configuration model etc.  Allows you to build a config model
 * for a dynamic configuration namespace i.e. a config namespace that is evolving and being 
 * extended all the time.  New namespaces can be easily added or extended.  All that's required
 * is to define the new config XSD and the Smooks Java Binding config to bind the data in the 
 * config namespace into the target Java model.
 * <p/>
 * The namespaces all need to be configured in a "descriptor" .properties file located on the classpath.
 * Here's an example:
 * <pre>
 * mycomp.namespace=http://www.acme.com/xsd/mycomp.xsd
 * mycomp.schemaLocation=/META-INF/xsd/mycomp.xsd
 * mycomp.bindingConfigLocation=/META-INF/xsd/mycomp-binding.xml
 * </pre>
 * 
 * You should use a unique descriptor path for a given configuration model.  Of course there can be many instances
 * of this file on the classpath i.e. one per module/jar.  This allows you to easily add extensions and updates
 * to your configuration model, without having to define new Java model for the new namespaces (versions) etc.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class ModelBuilder {
	
	private static Log logger = LogFactory.getLog(ModelBuilder.class);
	private static DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
	static {
		documentBuilderFactory.setNamespaceAware(true);
	}

    private Descriptor descriptor;
	private boolean validate = true;
    private String reportPath;

    public ModelBuilder(Descriptor descriptor, boolean validate) throws SAXException, IOException {
		AssertArgument.isNotNull(descriptor, "descriptor");

        this.descriptor = descriptor;
        this.validate = validate;

        configure();
	}

    public ModelBuilder(String descriptorPath, boolean validate) throws SAXException, IOException {
		AssertArgument.isNotNullAndNotEmpty(descriptorPath, "descriptorPath");

        descriptor = new Descriptor(descriptorPath);
        this.validate = validate;

        configure();
	}
	
	public boolean isValidating() {
		return validate;
	}

    protected Descriptor getDescriptor() {
        return descriptor;
    }

    public void setReportPath(String reportPath) {
        this.reportPath = reportPath;
    }

    public <T> T readObject(InputStream message, Class<T> returnType) throws SAXException, IOException {
		return readObject(new InputStreamReader(message), returnType);
	}
	
	public <T> T readObject(Reader message, Class<T> returnType) throws SAXException, IOException {
		Model<JavaResult> model = readModel(message, JavaResult.class);
		return model.getModelRoot().getBean(returnType);
	}

    public <T> Model<T> readModel(InputStream message, Class<T> modelRoot) throws SAXException, IOException {
        return readModel(new InputStreamReader(message), modelRoot);
    }

    public <T> Model<T> readModel(Reader message, Class<T> modelRoot) throws SAXException, IOException {
        AssertArgument.isNotNull(message, "message");
        AssertArgument.isNotNull(modelRoot, "modelRoot");

		JavaResult result = new JavaResult();
		ExecutionContext executionContext = descriptor.getSmooks().createExecutionContext();
        Map<Class<?>, Map<String, BeanWriter>> beanWriters = descriptor.getBeanWriters();
		BeanTracker beanTracker = new BeanTracker(beanWriters);

        if(reportPath != null) {
            executionContext.setEventListener(new HtmlReportGenerator(reportPath));            
        }

		executionContext.getBeanContext().addObserver(beanTracker);
		
		if(validate && descriptor.getSchema() != null) {
			// Validate the message against the schemas...
			Document messageDoc = toDocument(message);

	        // Validate the document and then filter it through smooks...
	        descriptor.getSchema().newValidator().validate(new DOMSource(messageDoc));
			descriptor.getSmooks().filterSource(executionContext, new DOMSource(messageDoc), result);
		} else {
			descriptor.getSmooks().filterSource(executionContext, new StreamSource(message), result);
		}

        Model<T> model;

        if(modelRoot == JavaResult.class) {
            model = new Model<T>(modelRoot.cast(result), beanTracker.beans, beanWriters, NamespaceReaper.getNamespacePrefixMappings(executionContext));
        } else {
            model = new Model<T>(modelRoot.cast(result.getBean(modelRoot)), beanTracker.beans, beanWriters, NamespaceReaper.getNamespacePrefixMappings(executionContext));
        }

        return model;
	}

	private Document toDocument(Reader message) {
		DocumentBuilder docBuilder;
		
		try {
			docBuilder = documentBuilderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new SmooksException("Unable to parse message and dynamically bind into object model.  DOM Parser confguration exception.", e);
		}
		
		try {
			return docBuilder.parse(new InputSource(message));
		} catch (SAXException e) {
			throw new SmooksException("Unable to parse message and dynamically bind into object model.  Message format exception.", e);
		} catch (IOException e) {
			throw new SmooksException("Unable to parse message and dynamically bind into object model.  IO exception.", e);
		} finally {
			try {
				message.close();
			} catch (IOException e) {
				logger.error("Exception closing message reader.", e);
			}
		}
	}

	private void configure() {
        Smooks smooks = descriptor.getSmooks();

        smooks.addVisitor(new NamespaceReaper());
        //descriptor.getSmooks().addVisitor(new UnknownElementDataReaper(), "*");

        smooks.setFilterSettings(FilterSettings.newDOMSettings());
        ParameterAccessor.setParameter(BeanInstancePopulator.NOTIFY_POPULATE, "true", smooks);        

        // Create the execution context so as to force resolution of the config...
        smooks.createExecutionContext();
	}

    private class BeanTracker implements BeanContextLifecycleObserver {
		
		private List<BeanMetadata> beans = new ArrayList<BeanMetadata>();
        private Map<Class<?>, Map<String, BeanWriter>> beanWriterMap;

        public BeanTracker(Map<Class<?>, Map<String, BeanWriter>> beanWriterMap) {
            this.beanWriterMap = beanWriterMap;
        }

        public void onBeanLifecycleEvent(BeanContextLifecycleEvent event) {
			if(event.getLifecycle() == BeanLifecycle.ADD || event.getLifecycle() == BeanLifecycle.CHANGE) {
                Object bean = event.getBean();
                BeanMetadata beanMetadata = new BeanMetadata(bean);
                Map<String, BeanWriter> beanWriters = beanWriterMap.get(bean.getClass());
                Fragment source = event.getSource();

                if(source != null) {
                    String namespaceURI = source.getNamespaceURI();

                    beanMetadata.setNamespace(namespaceURI);
                    beanMetadata.setNamespacePrefix(source.getPrefix());
                    beanMetadata.setCreateSource(source);
                    beans.add(beanMetadata);

                    if(source.isDOMElement()) {
                        beanMetadata.setPreText(UnknownElementDataReaper.getPreText(source.getDOMElement(), beans, event));
                    } else {
                        // SAX pretext is gathered by an instance of the UnknownElementDataReaper
                    }

                    if(beanWriters != null) {
                        BeanWriter beanWriter = beanWriters.get(namespaceURI);

                        if(beanWriter != null) {
                            beanMetadata.setWriter(beanWriter);
                        } else if(logger.isDebugEnabled()) {
                            logger.debug("BeanWriters are configured for Object type '" + bean.getClass() + "', but not for namespace '" + namespaceURI + "'.");
                        }
                    } else if(logger.isDebugEnabled()) {
                        logger.debug("No BeanWriters configured for Object type '" + bean.getClass() + "'.");
                    }
                }
            } else if(event.getLifecycle() == BeanLifecycle.POPULATE) {
                BeanMetadata beanMetdata = findMetadata(event.getBean());

                beanMetdata.getPopulateSources().add(event.getSource());
            }
		}

        private BeanMetadata findMetadata(Object bean) {
            for(BeanMetadata metaData : beans) {
                if(metaData.getBean() == bean) {
                    return metaData;
                }
            }

            BeanRegistrationException.throwUnregisteredBeanInstanceException(bean);

            return null; // Satisfy compiler
        }
    }
}
