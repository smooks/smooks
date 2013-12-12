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
package org.milyn.cartridge.javabean.dynamic;

import org.milyn.cartridge.javabean.dynamic.serialize.BeanWriter;
import org.milyn.commons.assertion.AssertArgument;
import org.milyn.delivery.Fragment;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Bean metadata.
 * <p/>
 * This class is used to hold additional data about a model bean instance.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class BeanMetadata {
	
	private Object bean;
	private String namespace;
    private String namespacePrefix;
    private Fragment createSource;
    private List<Fragment> populateSources = new ArrayList<Fragment>();
    private String preText;
    private Map<Object, Object> properties;
    private BeanWriter writer;

    /**
     * Public constructor.
     * @param bean The bean instance.
     */
    public BeanMetadata(Object bean) {
		AssertArgument.isNotNull(bean, "bean");
		this.bean = bean;
	}

    /**
     * Get the bean instance with which this metadata instance is associated.
     * @return The bean instance.
     */
	public Object getBean() {
		return bean;
	}

    /**
     * Get the XML namespace with which the associated bean instance is associated.
     *
     * @return The XML namespace with which the bean instance is associated.
     * @see #setNamespace(String)
     * @see #setNamespacePrefix(String)
     * @see #getNamespacePrefix()
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Set the XML namespace with which the associated bean instance is associated.
     * <p/>
     * If the bean instance was created from a source XML message, the namespace will
     * be set from the namespace of the source XML.  If the bean instance was created and
     * {@link Model#registerBean(Object) registered manually on the Model}, the
     * {@link org.milyn.cartridge.javabean.dynamic.serialize.DefaultNamespace default namepsace} is
     * set (can be reset).
     *
     * @param namespace The XML namespace with which the bean instance is associated.
     * @return <code>this</code> BeanMetadata instance.
     * @see #getNamespace()
     * @see #setNamespacePrefix(String)
     * @see #getNamespacePrefix()
     */
	public BeanMetadata setNamespace(String namespace) {
		this.namespace = namespace;
		return this;
	}

    /**
     * Get the XML namespace prefix.
     * @return The XML namespace prefix.
     * @see #setNamespace(String)
     * @see #getNamespace()
     * @see #setNamespacePrefix(String)
     */
    public String getNamespacePrefix() {
        return namespacePrefix;
    }

    /**
     * Set the XML namespace prefix.
     * <p/>
     * If the bean instance was created from a source XML message, the prefix will
     * be set from the prefix of the source XML.  If the bean instance was created and
     * {@link Model#registerBean(Object) registered manually on the Model}, the
     * {@link org.milyn.cartridge.javabean.dynamic.serialize.DefaultNamespace default namepsace prefix} is
     * set (can be reset).
     *
     * @param namespacePrefix The XML namespace prefix.
     * @return <code>this</code> BeanMetadata instance.
     * @see #setNamespace(String)
     * @see #getNamespace()
     * @see #getNamespacePrefix()
     */
    public BeanMetadata setNamespacePrefix(String namespacePrefix) {
        this.namespacePrefix = namespacePrefix;
        return this;
    }

    /**
     * Get the "pre text" associated with the bean.
     * <p/>
     * When a {@link ModelBuilder#readModel(java.io.Reader, Class) Model is created/read from a source XML message},
     * the different fragments in the source message are responsible for triggering creation of beans that go into
     * the {@link Model} instance.  The "pre text" of a bean is the XML whitespace, comments, CDATA and non contributing
     * XML that precedes the XML fragment that created the bean.  This is an ad-hoc mechanism for maintaining user comments
     * etc.
     *
     * @return The "pre text".
     */
    public String getPreText() {
        return preText;
    }

    /**
     * Set the "pre text" associated with the bean.
     * <p/>
     * When a {@link ModelBuilder#readModel(java.io.Reader, Class) Model is created/read from a source XML message},
     * the different fragments in the source message are responsible for triggering creation of beans that go into
     * the {@link Model} instance.  The "pre text" of a bean is the XML whitespace, comments, CDATA and non contributing
     * XML that precedes the XML fragment that created the bean.  This is an ad-hoc mechanism for maintaining user comments
     * etc.
     *
     * @param preText The "pre text".
     * @return <code>this</code> BeanMetadata instance.
     */
    public BeanMetadata setPreText(String preText) {
        this.preText = preText;
        return this;
    }

    /**
     * Get the {@link BeanWriter} instance to be used to serialize the bean instance.
     *
     * @return The {@link BeanWriter} instance to be used to serialize the bean instance, or null if
     * no {@link BeanWriter} instance is configured for the bean type and namespace combination.
     */
    public BeanWriter getWriter() {
        return writer;
    }

    /**
     * Set the {@link BeanWriter} instance to be used to serialize the bean instance.
     *
     * @param writer The {@link BeanWriter} instance to be used to serialize the bean instance.
     * @return <code>this</code> BeanMetadata instance.
     */
    public BeanMetadata setWriter(BeanWriter writer) {
        this.writer = writer;
        return this;
    }

    /**
     * Set the source {@link Fragment} that created the bean instance.
     * @param createSource The source fragment.
     * @return <code>this</code> BeanMetadata instance.
     */
    public BeanMetadata setCreateSource(Fragment createSource) {
        this.createSource = createSource;
        return this;
    }

    /**
     * Get the source {@link Fragment} that created the bean instance.
     * @return The source fragment, or null if the bean was not created from a source fragment
     * (may have been manually constructed and added to the model).
     */
    public Fragment getCreateSource() {
        return createSource;
    }

    /**
     * Get the "population" {@link Fragment fragments} that contributed data to the bean instance
     * (set data on it's properties).
     * 
     * @return The list of "population" fragments.  An empty List is returned if no "population"
     * fragments were recorded.
     */
    public List<Fragment> getPopulateSources() {
        return populateSources;
    }

    /**
     * Get user properties/metadata associated with the bean.
     * <p/>
     * A mechanism for associating additional "user defined" metadata.
     *
     * @return The user properties/metadata associated with the bean.
     */
    public Map<Object, Object> getProperties() {
        if(properties == null) {
            properties = new LinkedHashMap<Object, Object>();
        }
        return properties;
    }
}