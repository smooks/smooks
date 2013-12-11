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

import java.io.IOException;
import java.io.Writer;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.milyn.commons.assertion.AssertArgument;
import org.milyn.javabean.dynamic.serialize.BeanWriter;
import org.milyn.javabean.dynamic.serialize.DefaultNamespace;
import org.milyn.payload.JavaResult;

/**
 * Model container.
 * <p/>
 * Contains the {@link #getModelRoot() modelRoot} object instance, as well
 * as {@link #getModelMetadata() modelMetadata} associated with the
 * objects wired into the object graph routed on the {@link #getModelRoot() modelRoot}.
 * The {@link #getModelMetadata() modelMetadata} can contain information for, among other
 * things, serializing the object graph routed at {@link #getModelRoot() modelRoot}.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class Model<T> {

    private static Log logger = LogFactory.getLog(Model.class);

	private T modelRoot;
	private List<BeanMetadata> modelMetadata;
    private Map<Class<?>, Map<String, BeanWriter>> beanWriters;
    private Set<String> knownNamespaces;
    private Map<String, String> namespacePrefixMappings;

    /**
	 * Public constructor.
	 * @param modelRoot The model root object.
     * @param builder Associated model builder instance.
	 */
	public Model(T modelRoot, ModelBuilder builder) {
        AssertArgument.isNotNull(modelRoot, "modelRoot");
        AssertArgument.isNotNull(builder, "builder");

		this.modelRoot = modelRoot;
		this.modelMetadata = new ArrayList<BeanMetadata>();
        this.beanWriters = builder.getDescriptor().getBeanWriters();
        this.namespacePrefixMappings = new LinkedHashMap<String, String>();

        resolveKnownNamespaces();

        // Register the model root bean...
        registerBean(modelRoot);
	}

	/**
	 * Protected constructor.
     * <p/>
     * Used by the {@link ModelBuilder}.
     *
	 * @param modelRoot The model root object.
	 * @param modelMetadata Model metadata.
	 */
	protected Model(T modelRoot, List<BeanMetadata> modelMetadata, Map<Class<?>, Map<String, BeanWriter>> beanWriters, Map<String, String> namespacePrefixMappings) {
        AssertArgument.isNotNull(modelRoot, "modelRoot");
        AssertArgument.isNotNull(modelMetadata, "modelMetadata");
        AssertArgument.isNotNull(beanWriters, "beanWriters");
        AssertArgument.isNotNull(namespacePrefixMappings, "namespacePrefixMappings");

		this.modelRoot = modelRoot;
		this.modelMetadata = modelMetadata;
        this.beanWriters = beanWriters;
        this.namespacePrefixMappings = namespacePrefixMappings;

        resolveKnownNamespaces();
	}

	/**
	 * Get the model root object instance.
	 * @return the model root object instance.
	 */
	public T getModelRoot() {
		return modelRoot;
	}

    /**
     * Resolve the set of known namespaces.
     */
    private void resolveKnownNamespaces() {
        // Extract the set of known namespaces based on the set of bean writers we have...
        knownNamespaces = new HashSet<String>();
        Collection<Map<String, BeanWriter>> namespacedBeanWritersI = beanWriters.values();
        for(Map<String, BeanWriter>  namespacedBeanWritersII : namespacedBeanWritersI) {
            knownNamespaces.addAll(namespacedBeanWritersII.keySet());
        }
    }

    /**
     * Register the specified bean instance.
     * <p/>
     * All "namespace root" bean instances within a model must be registered via this method.
     * All "namespace root" bean types must be annotated with the {@link org.milyn.javabean.dynamic.serialize.DefaultNamespace @DefaultNamespace}
     * annotation.
     * <p/>
     * Note that not all beans in the model object graph need to be registered with the model.  Only the
     * namepsace "root" beans need to be registered.  The root bean of the model's object graph is often the only
     * namespace "root" bean in the model.  This is only ever the case when the configuration is composed of a single
     * namespace.  Many configurations are composed of multiple configuration namespaces, such as the Smooks
     * configurations, with the <i>smooks-core</i>, <i>javabean</i>, <i>validation</i> etc configuration namespaces.
     * In these cases, the constructed java object model for this multi-namespace configuration will have object
     * instance "away from" the root of the object model (down the object graph) that are associated with different
     * configuration namespaces.  These object instances are what we call the "namespace root" beans and they need to
     * be registered with the model via this method.  If not registered, the serialization process is likely to
     * fail when it attempts to locate a {@link BeanWriter} for the bean (and it's associated configuration namespace).
     *
     * @return Model metadata.
     * @throws BeanRegistrationException Bean instance
     * {@link BeanRegistrationException#throwBeanInstanceAlreadyRegisteredException(Object) already registered}, or
     * bean type
     * {@link BeanRegistrationException#throwBeanNotAnnotatedWithDefaultNamespace(Object) not annotated with the
     * DefaultNamespace annotation}.
     */
    public BeanMetadata registerBean(Object beanInstance) throws BeanRegistrationException {
        AssertArgument.isNotNull(beanInstance, "beanInstance");

        if(getBeanMetadata(beanInstance) != null) {
            BeanRegistrationException.throwBeanInstanceAlreadyRegisteredException(beanInstance);
        }

        BeanMetadata metadata = new BeanMetadata(beanInstance);
        modelMetadata.add(metadata);

        // Set the namespace to the default namespace for that bean.  Can be
        // changed later, if needed...
        DefaultNamespace defaultNs = beanInstance.getClass().getAnnotation(DefaultNamespace.class);
        if(defaultNs == null) {
            BeanRegistrationException.throwBeanNotAnnotatedWithDefaultNamespace(beanInstance);
        }
        metadata.setNamespace(defaultNs.uri());
        metadata.setNamespacePrefix(defaultNs.prefix());

        return metadata;
    }

    /**
	 * Get the model metadata.
	 * <p/>
	 * A {@link BeanMetadata} list containing metadata about objects
	 * wired into the object graph, routed at the {@link #getModelRoot() model root}.
	 *
	 * @return Model metadata.
	 */
	public List<BeanMetadata> getModelMetadata() {
		return modelMetadata;
	}

    /**
	 * Get the model metadata.
	 * <p/>
	 * A {@link BeanMetadata} list containing metadata about objects
	 * wired into the object graph, routed at the {@link #getModelRoot() model root}.
	 *
	 * @return Model metadata.
	 */
	public BeanMetadata getBeanMetadata(Object beanInstance) {
		AssertArgument.isNotNull(beanInstance, "beanInstance");
		for(BeanMetadata beanMetadata: modelMetadata) {
			if(beanMetadata.getBean() == beanInstance) {
				return beanMetadata;
			}
		}
		return null;
	}

    /**
     * Write the bean model to the specified {@link Writer} instance.
     * @param writer The writer instance.
     * @throws BeanRegistrationException One of the "namespace root" beans in the model is not {@link #registerBean(Object) registered}.
     * @throws IOException Error while writing the model to the supplied {@link Writer} instance.
     */
    public synchronized void writeModel(Writer writer) throws BeanRegistrationException, IOException {
        AssertArgument.isNotNull(writer, "writer");

        Object rootBean;

        if(modelRoot instanceof JavaResult) {
            JavaResult javaResult = (JavaResult) modelRoot;
            Map<String, Object> beanMap = javaResult.getResultMap();

            if(beanMap.isEmpty()) {
                throw new IOException("Unable to serialize empty JavaResult Model.");
            } else if(beanMap.size() > 1) {
                throw new IOException("Unable to serialize JavaResult Model that contains more than 1 bean instance.");
            }
            rootBean = beanMap.values().iterator().next();
        } else {
            rootBean = modelRoot;
        }

        resolveModelNamespaces();
        resolveUnmappedBeanWriters();

        BeanWriter beanWriter = getBeanWriter(rootBean);
        beanWriter.write(rootBean, writer, this);
    }

    /**
     * Get the current namespace prefix mappings for this Model instance.
     * @return The current namespace prefix mappings for this Model instance.
     */
    public Map<String, String> getNamespacePrefixMappings() {
        return namespacePrefixMappings;
    }

    /**
     * Get the {@link BeanWriter} instance for the specified bean, if one exists.
     * @param bean The bean.
     * @return The {@link BeanWriter} instance for the specified bean, if one exists, otherwise null.
     * @throws BeanRegistrationException No {@link BeanMetadata} for specified bean instance.
     */
    public BeanWriter getBeanWriter(Object bean) throws BeanRegistrationException {
        BeanMetadata beanMetadata = getBeanMetadata(bean);

        if(beanMetadata == null) {
            BeanRegistrationException.throwUnregisteredBeanInstanceException(bean);
        }

        return beanMetadata.getWriter();
    }

    /**
     * Resolve all the namespaces in the model.
     */
    private void resolveModelNamespaces() {
        removeKnownNamespaceMappings();
        updateMetadataPrefixes();
        addMissingNamespaceMappings();
    }

    /**
     * Iterate through all the bean metadata and make sure the
     * namespace prefixes match those declared in the prefix mappings.
     */
    private void updateMetadataPrefixes() {
        for(BeanMetadata metaData : modelMetadata) {
            String declaredPrefix = namespacePrefixMappings.get(metaData.getNamespace());
            if(declaredPrefix != null) {
                metaData.setNamespacePrefix(declaredPrefix);
            }
        }
    }

    /**
     * Filter out all "known" namespace-to-prefix mappings for which there are no
     * registered beans in the model.
     */
    private void removeKnownNamespaceMappings() {
        // Need to create a clone so as to avoid concurrent mod exceptions...
        Set<String> namespaceUris = new HashSet<String>(namespacePrefixMappings.keySet());

        for(String namespaceUri : namespaceUris) {
            if(knownNamespaces.contains(namespaceUri) && !isNamespaceInModel(namespaceUri)) {
                namespacePrefixMappings.remove(namespaceUri);
            }
        }
    }

    /**
     * Iterate through all the bean metadata and add any missing
     * namespace-to-prefix mappings.
     */
    private void addMissingNamespaceMappings() {
        for(BeanMetadata metaData : modelMetadata) {
            String uri = metaData.getNamespace();
            if(!namespacePrefixMappings.containsKey(uri)) {
                namespacePrefixMappings.put(uri, metaData.getNamespacePrefix());
            }
        }
    }

    private boolean isNamespaceInModel(String namespaceUri) {
        for(BeanMetadata metaData : modelMetadata) {
            if(namespaceUri.equals(metaData.getNamespace())) {
                return true;
            }
        }

        return false;
    }

    private void resolveUnmappedBeanWriters() throws IOException {
        for(BeanMetadata metaData : modelMetadata) {
            if(metaData.getWriter() == null) {
                // Install the writer for the configured namespace...
                Map<String, BeanWriter> classBeanWriters = beanWriters.get(metaData.getBean().getClass());

                if(classBeanWriters != null) {
                    BeanWriter beanWriter = classBeanWriters.get(metaData.getNamespace());

                    if(beanWriter == null) {
                        throw new IOException("BeanWriters are configured for Object type '" + metaData.getBean().getClass() + "', but not for namespace '" + metaData.getNamespace() + "'.");
                    }

                    metaData.setWriter(beanWriter);
                }
            }
        }
    }
}
