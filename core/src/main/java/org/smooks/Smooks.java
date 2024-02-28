/*-
 * ========================LICENSE_START=================================
 * Core
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0, or
 * the GNU Lesser General Public License version 3.0 or later.
 * 
 * SPDX-License-Identifier: Apache-2.0 OR LGPL-3.0-or-later
 * 
 * ======================================================================
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ======================================================================
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * =========================LICENSE_END==================================
 */
package org.smooks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.api.ApplicationContext;
import org.smooks.api.ExecutionContext;
import org.smooks.api.Registry;
import org.smooks.api.SmooksException;
import org.smooks.api.TypedMap;
import org.smooks.api.bean.context.BeanContext;
import org.smooks.api.bean.lifecycle.BeanContextLifecycleObserver;
import org.smooks.api.delivery.ContentDeliveryConfig;
import org.smooks.api.delivery.ContentDeliveryRuntime;
import org.smooks.api.delivery.ContentHandler;
import org.smooks.api.delivery.ContentHandlerBinding;
import org.smooks.api.delivery.Filter;
import org.smooks.api.delivery.FilterBypass;
import org.smooks.api.delivery.VisitorAppender;
import org.smooks.api.delivery.event.ExecutionEventListener;
import org.smooks.api.lifecycle.FilterLifecycle;
import org.smooks.api.lifecycle.LifecycleManager;
import org.smooks.api.profile.Profile;
import org.smooks.api.profile.ProfileSet;
import org.smooks.api.profile.UnknownProfileMemberException;
import org.smooks.api.resource.config.ReaderConfigurator;
import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.api.resource.visitor.Visitor;
import org.smooks.api.resource.visitor.dom.DOMElementVisitor;
import org.smooks.assertion.AssertArgument;
import org.smooks.classpath.CascadingClassLoaderSet;
import org.smooks.engine.DefaultApplicationContextBuilder;
import org.smooks.engine.DefaultExecutionContext;
import org.smooks.engine.bean.context.preinstalled.Time;
import org.smooks.engine.bean.context.preinstalled.UniqueID;
import org.smooks.engine.delivery.DefaultContentHandlerBinding;
import org.smooks.engine.injector.Scope;
import org.smooks.engine.lifecycle.FilterFinishedLifecyclePhase;
import org.smooks.engine.lifecycle.FilterStartedLifecyclePhase;
import org.smooks.engine.lifecycle.PostConstructLifecyclePhase;
import org.smooks.engine.lookup.InstanceLookup;
import org.smooks.engine.lookup.LifecycleManagerLookup;
import org.smooks.engine.xml.NamespaceManager;
import org.smooks.io.payload.Exports;
import org.smooks.io.payload.FilterResult;
import org.smooks.io.payload.FilterSource;
import org.smooks.io.payload.JavaResult;
import org.smooks.resource.URIResourceLocator;
import org.smooks.support.SmooksUtil;
import org.smooks.support.URIUtil;
import org.xml.sax.SAXException;

import javax.annotation.concurrent.ThreadSafe;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Smooks executor class.
 * <p/>
 * Additional configurations can be carried out on the {@link org.smooks.Smooks} instance
 * through the {@link SmooksUtil} class.
 * <p/>
 * The basic usage scenario for this class might be as follows:
 * <ol>
 * <li>Develop (or reuse) an implementation of {@link org.smooks.api.resource.visitor.sax.ng.ElementVisitor} to
 * perform some transformation/analysis operation on a message.  There are a number of prebuilt
 * and reuseable implemntations available as
 * "<a target="new" href="https://www.smooks.org#Smooks-smookscartridges">Smooks Cartridges</a>".</li>
 * <li>Write a {@link ResourceConfig resource configuration} to target the {@link org.smooks.api.resource.visitor.sax.ng.ElementVisitor}
 * implementation at the target fragment of the message being processed.</li>
 * <li>Apply the logic as follows:
 * <pre>
 * Smooks smooks = {@link #Smooks(String) new Smooks}("smooks-config.xml");
 * {@link ExecutionContext} execContext;
 *
 * execContext = smooks.{@link #createExecutionContext createExecutionContext}();
 * smooks.{@link #filterSource filter}(new {@link StreamSource}(...), new {@link StreamResult}(...), execContext);
 * </pre>
 * </li>
 * </ol>
 * Remember, you can implement and apply multiple {@link org.smooks.api.resource.visitor.sax.ng.ElementVisitor}
 * within the context of a single filtering operation.  You can also target
 * {@link DOMElementVisitor DOMElementVisitors}/{@link org.smooks.api.resource.visitor.sax.ng.ElementVisitor} based on target profiles, and so use a single
 * configuration to process multiple messages by sharing profiles across your message set.
 * <p/>
 * See <a target="new" href="http://milyn.codehaus.org/Tutorials">Smooks Tutorials</a>.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@ThreadSafe
public class Smooks implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Smooks.class);
    private final ApplicationContext applicationContext;

    /**
     * Manually added visitors.  In contract to those that are constructed and configured dynamically from
     * an XML configuration stream.
     */
    private final List<ContentHandlerBinding<Visitor>> visitorBindings;
    /**
     * Flag indicating whether or not the Smooks instance is configurable.  It becomes un-configurable
     * after the first execution context has been created.
     */
    private volatile boolean isConfigurable = true;

    /**
     * Public Default Constructor.
     * <p/>
     * Resource configurations can be added through calls to
     * {@link #addResourceConfigs(String)} or {@link #addResourceConfigs(String, java.io.InputStream)}.
     */
    public Smooks() {
        applicationContext = new DefaultApplicationContextBuilder().build();
        visitorBindings = new ArrayList<>();
    }

    /**
     * Public Default Constructor.
     * <p/>
     * Resource configurations can be added through calls to
     * {@link #addResourceConfigs(String)} or {@link #addResourceConfigs(String, java.io.InputStream)}.
     */
    public Smooks(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        visitorBindings = new ArrayList<>();
    }

    /**
     * Public constructor.
     * <p/>
     * Adds the set of {@link ResourceConfig resources} via the {@link #addResourceConfigs(String)} method,
     * which resolves the resourceURI parameter using a {@link org.smooks.resource.URIResourceLocator}.
     * <p/>
     * Additional resource configurations can be added through calls to
     * {@link #addResourceConfigs(String)} or {@link #addResourceConfigs(String, java.io.InputStream)}.
     *
     * @param resourceURI XML resource configuration stream URI.
     * @throws IOException  Error reading resource stream.
     * @throws SAXException Error parsing the resource stream.
     * @see ResourceConfig
     */
    public Smooks(String resourceURI) throws IOException, SAXException {
        URIResourceLocator resourceLocator = new URIResourceLocator();
        resourceLocator.setBaseURI(URIResourceLocator.extractBaseURI(resourceURI));
        applicationContext = new DefaultApplicationContextBuilder().setResourceLocator(resourceLocator).build();
        visitorBindings = new ArrayList<>();
        addResourceConfigs(resourceURI);
    }

    /**
     * Public constructor.
     * <p/>
     * Adds the set of {@link ResourceConfig resources} via the {@link #addResourceConfigs(java.io.InputStream)}.
     * <p/>
     * Additional resource configurations can be added through calls to
     * <code>addConfigurations</code> method set.
     *
     * @param resourceConfigStream XML resource configuration stream.
     * @throws IOException  Error reading resource stream.
     * @throws SAXException Error parsing the resource stream.
     * @see ResourceConfig
     */
    public Smooks(InputStream resourceConfigStream) throws IOException, SAXException {
        this();
        addResourceConfigs(resourceConfigStream);
    }

    /**
     * Set the filter settings for this Smooks instance.
     *
     * @param filterSettings The filter settings to be used.
     */
    public void setFilterSettings(FilterSettings filterSettings) {
        AssertArgument.isNotNull(filterSettings, "filterSettings");
        filterSettings.applySettings(this);
    }

    /**
     * Set the Exports for this Smooks instance.
     *
     * @param exports The exports that will be created by this Smooks instance.
     */
    public Smooks setExports(Exports exports) {
        AssertArgument.isNotNull(exports, "exports");
        applicationContext.getRegistry().deRegisterObject(Exports.class);
        applicationContext.getRegistry().registerObject(Exports.class, exports);

        return this;
    }

    /**
     * Set the configuration for the reader to be used on this Smooks instance.
     *
     * @param readerConfigurator {@link ReaderConfigurator} instance.
     */
    public void setReaderConfig(ReaderConfigurator readerConfigurator) {
        List<ResourceConfig> resourceConfigs = readerConfigurator.toConfig();

        for (ResourceConfig resourceConfig : resourceConfigs) {
            addResourceConfig(resourceConfig);
        }
    }

    /**
     * Set the namespace prefix-to-uri mappings to be used on this Smooks instance.
     *
     * @param namespaces The namespace prefix-to-uri mappings.
     */
    public void setNamespaces(Properties namespaces) {
        AssertArgument.isNotNull(namespaces, "namespaces");
        assertIsConfigurable();
        applicationContext.getRegistry().registerObject(NamespaceManager.class, namespaces);
    }

    /**
     * Add a visitor instance to <code>this</code> Smooks instance.
     * <p/>
     * This Visitor will be targeted at the root (#document) fragment.
     *
     * @param visitor The visitor implementation.
     */
    public ResourceConfig addVisitor(Visitor visitor) {
        return addVisitor(visitor, ResourceConfig.DOCUMENT_FRAGMENT_SELECTOR);
    }

    /**
     * Add a visitor instance to <code>this</code> Smooks instance.
     *
     * @param visitor        The visitor implementation.
     * @param targetSelector The message fragment target selector.
     */
    public ResourceConfig addVisitor(Visitor visitor, String targetSelector) {
        assertIsConfigurable();
        AssertArgument.isNotNull(visitor, "visitor");
        AssertArgument.isNotNull(targetSelector, "targetSelector");

        ContentHandlerBinding<Visitor> contentHandlerBinding = new DefaultContentHandlerBinding<>(visitor, targetSelector, applicationContext.getRegistry());
        visitorBindings.add(contentHandlerBinding);

        return contentHandlerBinding.getResourceConfig();
    }

    /**
     * Adds a {@link Visitor} to this <code>Smooks</code> via a {@link VisitorAppender}.
     *
     * @param visitorAppender the <code>VisitorAppender</code>
     */
    public void addVisitors(VisitorAppender visitorAppender) {
        getApplicationContext().getRegistry().lookup(new LifecycleManagerLookup()).applyPhase(visitorAppender, new PostConstructLifecyclePhase(new Scope(applicationContext.getRegistry())));

        for (ContentHandlerBinding<Visitor> visitorBinding : visitorAppender.addVisitors()) {
            getApplicationContext().getRegistry().lookup(new LifecycleManagerLookup()).applyPhase(visitorBinding.getContentHandler(), new PostConstructLifecyclePhase(new Scope(applicationContext.getRegistry(), visitorBinding.getResourceConfig(), visitorBinding.getContentHandler())));
            this.visitorBindings.add(visitorBinding);
        }
    }

    /**
     * Add a resource configuration to this Smooks instance.
     * <p/>
     * These configurations do not overwrite previously added configurations.
     * They are added to the list of configurations on this Smooks instance.
     *
     * @param resourceConfig The resource configuration to be added.
     */
    public void addResourceConfig(ResourceConfig resourceConfig) {
        AssertArgument.isNotNull(resourceConfig, "resourceConfig");
        assertIsConfigurable();
        applicationContext.getRegistry().registerResourceConfig(resourceConfig);
    }

    /**
     * Add a set of resource configurations to this Smooks instance.
     * <p/>
     * Uses the {@link org.smooks.resource.URIResourceLocator} class to load the resource.
     * <p/>
     * These configurations do not overwrite previously added configurations.
     * They are added to the list of configurations on this Smooks instance.
     *
     * @param resourceURI The URI string for the resource configuration list. See
     *                    {@link org.smooks.resource.URIResourceLocator}.
     * @throws IOException  Error reading resource stream.
     * @throws SAXException Error parsing the resource stream.
     */
    public void addResourceConfigs(String resourceURI) throws IOException, SAXException {
        AssertArgument.isNotNullAndNotEmpty(resourceURI, "resourceURI");

        InputStream resourceConfigStream;
        URIResourceLocator resourceLocator = new URIResourceLocator();

        resourceConfigStream = resourceLocator.getResource(resourceURI);
        try {
            URI resourceURIObj = new URI(resourceURI);
            addResourceConfigs(URIUtil.getParent(resourceURIObj).toString(), resourceConfigStream);
        } catch (URISyntaxException e) {
            LOGGER.error("Failed to load Smooks resource configuration '" + resourceURI + "'.", e);
        } finally {
            resourceConfigStream.close();
        }
    }

    /**
     * Add a set of resource configurations to this Smooks instance.
     * <p/>
     * These configurations do not overwrite previously added configurations.
     * They are added to the list of configurations on this Smooks instance.
     * <p/>
     * The base URI is required for resolving resource imports.  Just specify
     * the location of the resource file.
     *
     * @param baseURI              The base URI string for the resource configuration list. See
     *                             {@link org.smooks.resource.URIResourceLocator}.
     * @param resourceConfigStream The resource configuration stream.
     * @throws IOException  Error reading resource stream.
     * @throws SAXException Error parsing the resource stream.
     */
    public void addResourceConfigs(String baseURI, InputStream resourceConfigStream) throws SAXException, IOException {
        assertIsConfigurable();
        AssertArgument.isNotNullAndNotEmpty(baseURI, "baseURI");
        AssertArgument.isNotNull(resourceConfigStream, "resourceConfigStream");
        try {
            applicationContext.getRegistry().registerResources(baseURI, resourceConfigStream);
        } catch (URISyntaxException e) {
            throw new IOException("Failed to read resource configuration. Invalid 'baseURI'.");
        }
    }

    /**
     * Add a set of resource configurations to this Smooks instance.
     * <p/>
     * Calls {@link #addResourceConfigs(String, java.io.InputStream)} with a baseURI of "./",
     * which is the default base URI on all {@link org.smooks.resource.URIResourceLocator}
     * instances.
     *
     * @param resourceConfigStream The resource configuration stream.
     * @throws IOException  Error reading resource stream.
     * @throws SAXException Error parsing the resource stream.
     */
    public void addResourceConfigs(InputStream resourceConfigStream) throws SAXException, IOException {
        addResourceConfigs("./", resourceConfigStream);
    }

    /**
     * Create a {@link ExecutionContext} instance for use on this Smooks instance.
     * <p/>
     * The created context is profile agnostic and should be used where profile based targeting is not in use.
     * <p/>
     * The context returned from this method is used in subsequent calls to
     * {@link #filterSource(ExecutionContext, javax.xml.transform.Source, javax.xml.transform.Result...)}
     * It allows access to the execution context instance
     * before and after calls on this method.  This means the caller has an opportunity to set and get data
     * {@link TypedMap bound} to the execution context (before and after the calls), providing the
     * caller with a mechanism for interacting with the content {@link org.smooks.engine.delivery.dom.SmooksDOMFilter filtering} phases.
     *
     * @return Execution context instance.
     */
    public ExecutionContext createExecutionContext() {
        return createExecutionContext(Profile.DEFAULT_PROFILE);
    }

    /**
     * Create a {@link ExecutionContext} instance for use on this Smooks instance.
     * <p/>
     * The created context is profile aware and should be used where profile based targeting is in use. In this case,
     * the transfromation/analysis resources must be configured with profile targeting information.
     * <p/>
     * The context returned from this method is used in subsequent calls to
     * {@link #filterSource(ExecutionContext, javax.xml.transform.Source, javax.xml.transform.Result...)}.
     * It allows access to the execution context instance
     * before and after calls on this method.  This means the caller has an opportunity to set and get data
     * {@link TypedMap bound} to the execution context (before and after the calls), providing the
     * caller with a mechanism for interacting with the content {@link org.smooks.engine.delivery.dom.SmooksDOMFilter filtering} phases.
     *
     * @param targetProfile The target profile ({@link ProfileSet base profile}) on behalf of whom the filtering/serialisation
     *                      filter is to be executed.
     * @return Execution context instance.
     * @throws UnknownProfileMemberException Unknown target profile.
     */
    public ExecutionContext createExecutionContext(String targetProfile) throws UnknownProfileMemberException {
        if (applicationContext.getClassLoader() != null) {
            ClassLoader originalTCCL = Thread.currentThread().getContextClassLoader();
            CascadingClassLoaderSet newTCCL = new CascadingClassLoaderSet();

            newTCCL.addClassLoader(applicationContext.getClassLoader());
            newTCCL.addClassLoader(originalTCCL);

            Thread.currentThread().setContextClassLoader(newTCCL);
            try {
                if (isConfigurable) {
                    setNotConfigurable();
                }
                return new DefaultExecutionContext(targetProfile, applicationContext, visitorBindings);
            } finally {
                Thread.currentThread().setContextClassLoader(originalTCCL);
            }
        } else {
            if (isConfigurable) {
                setNotConfigurable();
            }
            return new DefaultExecutionContext(targetProfile, applicationContext, visitorBindings);
        }
    }

    private synchronized void setNotConfigurable() {
        if (!isConfigurable) {
            return;
        }
        isConfigurable = false;
    }

    /**
     * Filter the content in the supplied {@link Source} instance.
     * <p/>
     * Not producing a {@link Result}.
     *
     * @param source The content Source.
     * @throws SmooksException Failed to filter.
     */
    public void filterSource(Source source) throws SmooksException {
        filterSource(createExecutionContext(), source, null);
    }

    /**
     * Filter the content in the supplied {@link Source} instance, outputing data
     * to the supplied {@link Result} instances.
     *
     * @param source  The filter Source.
     * @param results The filter Results.
     * @throws SmooksException Failed to filter.
     */
    public void filterSource(Source source, Result... results) throws SmooksException {
        filterSource(createExecutionContext(), source, results);
    }

    /**
     * Filter the content in the supplied {@link Source} instance, outputing data
     * to the supplied {@link Result} instances.
     *
     * @param executionContext The {@link ExecutionContext} for this filter operation. See
     *                         {@link #createExecutionContext(String)}.
     * @param source           The filter Source.
     * @param results          The filter Results.
     * @throws SmooksException Failed to filter.
     */
    public void filterSource(ExecutionContext executionContext, Source source, Result... results) throws SmooksException {
        AssertArgument.isNotNull(source, "source");
        AssertArgument.isNotNull(executionContext, "executionContext");

        if (applicationContext.getClassLoader() != null) {
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(applicationContext.getClassLoader());
            try {
                _filter(executionContext, source, results);
            } finally {
                Thread.currentThread().setContextClassLoader(contextClassLoader);
            }
        } else {
            _filter(executionContext, source, results);
        }
    }

    private void _filter(ExecutionContext executionContext, Source source, Result... results) {
        ContentDeliveryRuntime contentDeliveryRuntime = executionContext.getContentDeliveryRuntime();
        Registry registry = applicationContext.getRegistry();
        LifecycleManager lifecycleManager = registry.lookup(new LifecycleManagerLookup());
        FilterStartedLifecyclePhase filterStartedLifecyclePhase = new FilterStartedLifecyclePhase(executionContext);
        for (FilterLifecycle filterLifecycle : registry.lookup(new InstanceLookup<>(FilterLifecycle.class)).values()) {
            lifecycleManager.applyPhase(filterLifecycle, filterStartedLifecyclePhase);
        }

        try {
            for (FilterLifecycle filterLifecycle : applicationContext.getRegistry().lookup(new InstanceLookup<>(FilterLifecycle.class)).values()) {
                filterLifecycle.onStarted(executionContext);
            }

            ContentDeliveryConfig contentDeliveryConfig = contentDeliveryRuntime.getContentDeliveryConfig();

            if (results != null && results.length == 1 && results[0] != null) {
                FilterBypass filterBypass = contentDeliveryConfig.getFilterBypass();
                if (filterBypass != null && filterBypass.bypass(executionContext, source, results[0])) {
                    // We're done... a filter bypass was applied...
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("FilterBypass {} applied.", filterBypass.getClass().getName());
                    }
                    return;
                }
            }

            Filter filter = contentDeliveryConfig.newFilter(executionContext);
            try {
                // Attach the source and results to the context...
                FilterSource.setSource(executionContext, source);
                FilterResult.setResults(executionContext, results);

                // Add pre installed beans + global BeanContext lifecycle observers...
                BeanContext beanContext = executionContext.getBeanContext();
                beanContext.addBean(Time.BEAN_ID, new Time());
                beanContext.addBean(UniqueID.BEAN_ID, new UniqueID());
                for (BeanContextLifecycleObserver observer : applicationContext.getBeanContextLifecycleObservers()) {
                    beanContext.addObserver(observer);
                }

                try {
                    contentDeliveryConfig.executeHandlerInit(executionContext);
                    filter.doFilter();
                } finally {
                    try {
                        // We want to make sure that all the beans from the BeanContext are available in the
                        // JavaResult, if one is supplied by the user...
                        JavaResult javaResult = (JavaResult) FilterResult.getResult(executionContext, JavaResult.class);
                        if (javaResult != null) {
                            javaResult.getResultMap().putAll(executionContext.getBeanContext().getBeanMap());
                        }

                        // Remove the pre-installed beans...
                        beanContext.removeBean(Time.BEAN_ID, null);
                        beanContext.removeBean(UniqueID.BEAN_ID, null);
                    } finally {
                        contentDeliveryConfig.executeHandlerCleanup(executionContext);
                    }
                }
            } catch (SmooksException e) {
                executionContext.setTerminationError(e);
                throw e;
            } catch (Throwable t) {
                executionContext.setTerminationError(t);
                throw new SmooksException("Smooks Filtering operation failed.", t);
            } finally {
                try {
                    filter.close();
                } catch (IOException e) {
                    throw new SmooksException(e);
                }
            }
        } finally {
            FilterFinishedLifecyclePhase filterFinishedLifecyclePhase = new FilterFinishedLifecyclePhase(executionContext);
            for (FilterLifecycle filterLifecycle : applicationContext.getRegistry().lookup(new InstanceLookup<>(FilterLifecycle.class)).values()) {
                lifecycleManager.applyPhase(filterLifecycle, filterFinishedLifecyclePhase);
            }
        }
    }

    /**
     * Get the Smooks {@link ApplicationContext} associated with
     * this Smooks instance.
     *
     * @return The Smooks {@link ApplicationContext}.
     */
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * Close this Smooks instance and all associated resources.
     * <p/>
     * Should result in the {@link jakarta.annotation.PostConstruct uninitialization}
     * of all allocated {@link ContentHandler} instances.
     */
    @Override
    public void close() {
        applicationContext.getRegistry().close();
    }

    /**
     * Assert that the instance is configurable, throwing an exception if it is not.
     */
    private void assertIsConfigurable() {
        if (!isConfigurable) {
            throw new UnsupportedOperationException("Unsupported call to Smooks instance configuration method after Smooks instance has created an ExecutionContext.");
        }
    }
}
