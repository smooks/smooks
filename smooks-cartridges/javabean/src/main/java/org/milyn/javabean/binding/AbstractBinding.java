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
package org.milyn.javabean.binding;

import org.milyn.FilterSettings;
import org.milyn.Smooks;
import org.milyn.assertion.AssertArgument;
import org.milyn.cdr.SmooksResourceConfigurationList;
import org.milyn.container.ExecutionContext;
import org.milyn.event.report.HtmlReportGenerator;
import org.milyn.javabean.binding.model.Bean;
import org.milyn.javabean.binding.model.DataBinding;
import org.milyn.javabean.binding.model.WiredBinding;
import org.milyn.javabean.binding.model.get.GetterGraph;
import org.milyn.payload.JavaResult;
import org.xml.sax.SAXException;

import javax.xml.transform.Source;
import java.io.IOException;
import java.io.InputStream;

/**
 * Abstract Binding class.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class AbstractBinding {

    /**
     * Smooks instance.
     */
    private Smooks smooks;
    /**
     * Execution report path.
     */
    private String reportPath;
    /**
     * Initialized flag.
     */
    private boolean initialized = false;

    /**
     * Constructor.
     *
     * @throws IOException  Error reading resource stream.
     * @throws SAXException Error parsing the resource stream.
     */
    protected AbstractBinding() throws IOException, SAXException {
        smooks = new Smooks();
    }

    /**
     * Add Smooks binding configurations to the binding instance.
     *
     * @param smooksConfigURI Smooks configuration.
     * @throws IOException  Error reading resource stream.
     * @throws SAXException Error parsing the resource stream.
     */
    public AbstractBinding add(String smooksConfigURI) throws IOException, SAXException {
        assertNotInitialized();
        smooks.addConfigurations(smooksConfigURI);
        return this;
    }

    /**
     * Add Smooks binding configurations to the binding instance.
     *
     * @param smooksConfigStream Smooks configuration.
     * @throws IOException  Error reading resource stream.
     * @throws SAXException Error parsing the resource stream.
     */
    public AbstractBinding add(InputStream smooksConfigStream) throws IOException, SAXException {
        assertNotInitialized();
        smooks.addConfigurations(smooksConfigStream);
        return this;
    }

    /**
     * Initialize the binding instance.
     */
    public AbstractBinding intiailize() {
        assertNotInitialized();
        smooks.createExecutionContext();
        this.initialized = true;
        return this;
    }

    /**
     * Get the underlying {@link Smooks} instance.
     *
     * @return The underlying {@link Smooks} instance.
     */
    public Smooks getSmooks() {
        return smooks;
    }

    /**
     * Set the execution report output path.
     *
     * @param reportPath The execution report output path.
     */
    public void setReportPath(String reportPath) {
        this.reportPath = reportPath;
    }

    /**
     * Bind the input source to the specified type.
     * <p/>
     * In order to make a cleaner API, implementing classes should create a more
     * appropriately named method based on the target binding format, that just
     * delegates to this method e.g. {@link org.milyn.javabean.binding.xml.XMLBinding#fromXML(javax.xml.transform.Source, Class)}
     * and {@link org.milyn.javabean.binding.xml.XMLBinding#toXML(Object, java.io.Writer)}.
     *
     * @param inputSource The input source.
     * @param toType      The target type.
     * @return The target binding type instance.
     * @throws IOException Error binding source to target type.
     */
    protected <T> T bind(Source inputSource, Class<T> toType) throws IOException {
        AssertArgument.isNotNull(inputSource, "inputSource");
        AssertArgument.isNotNull(toType, "toType");

        assertInitialized();

        JavaResult javaResult = new JavaResult();
        ExecutionContext executionContext = smooks.createExecutionContext();

        if (reportPath != null) {
            executionContext.setEventListener(new HtmlReportGenerator(reportPath));
        }

        smooks.filterSource(executionContext, inputSource, javaResult);

        return javaResult.getBean(toType);
    }

    protected SmooksResourceConfigurationList getUserDefinedResourceList() {
        return smooks.getApplicationContext().getStore().getUserDefinedResourceList();
    }

    protected GetterGraph constructContextualGetter(DataBinding binding) {
        GetterGraph contextualGetter = new GetterGraph();

        contextualGetter.add(binding);
        addToContextualGetter(contextualGetter, binding.getParentBean());

        return contextualGetter;
    }

    protected GetterGraph constructContextualGetter(Bean bean) {
        return addToContextualGetter(new GetterGraph(), bean);
    }

    private GetterGraph addToContextualGetter(GetterGraph contextualGetter, Bean bean) {
        while(bean != null) {
            Bean parentBean = bean.getWiredInto();

            if(parentBean != null) {
                if(parentBean.isCollection()){
                    // Contextual selectors stop once they hit a parent Collection bean...
                    if(parentBean.getWiredInto() != null) {
                        // Use the collection item's beanId as the context object name
                        // because collection items don't have property names...
                        contextualGetter.setContextObjectName(bean.getBeanId());
                    }
                    break;
                }

                WiredBinding binding = parentBean.getWiredBinding(bean);

                if(binding == null) {
                    throw new IllegalStateException("Failed to locate a wiring of bean '" + bean + "' on bean '" + parentBean + "'.");
                }

                contextualGetter.add(parentBean, binding.getProperty());
            }

            bean = parentBean;
        }

        return contextualGetter;
    }

    protected void assertInitialized() {
        if(!initialized) {
            throw new IllegalStateException("Illegal call to method before instance is initialized.  Must call the 'initialize' method first.");
        }
    }

    protected void assertNotInitialized() {
        if(initialized) {
            throw new IllegalStateException("Illegal call to method after instance is initialized.");
        }
    }
}
