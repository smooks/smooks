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
package org.smooks.templating.freemarker;

import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.cache.URLTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.smooks.SmooksException;
import org.smooks.cdr.SmooksResourceConfiguration;
import org.smooks.cdr.annotation.ConfigParam;
import org.smooks.container.ExecutionContext;
import org.smooks.delivery.Filter;
import org.smooks.delivery.Fragment;
import org.smooks.delivery.dom.serialize.TextSerializationUnit;
import org.smooks.delivery.ordering.Consumer;
import org.smooks.delivery.sax.*;
import org.smooks.event.report.annotation.VisitAfterReport;
import org.smooks.event.report.annotation.VisitBeforeReport;
import org.smooks.io.AbstractOutputStreamResource;
import org.smooks.io.NullWriter;
import org.smooks.templating.AbstractTemplateProcessor;
import org.smooks.templating.TemplatingConfiguration;
import org.smooks.util.FreeMarkerTemplate;
import org.smooks.util.FreeMarkerUtils;
import org.smooks.xml.DomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Map;

/**
 * <a href="http://freemarker.org/">FreeMarker</a> template application ProcessingUnit.
 * <p/>
 * See {@link org.smooks.templating.freemarker.FreeMarkerContentHandlerFactory}.
 * <p/>
 * <b>NOTE</b> that this visitor supports the extra "<b>useNodeModel</b>" parameter when
 * using DOM based filtering.  When set to true (default=false), the targeted
 * DOM element will be attached to the model that is passed to the FreeMarker
 * templating engine.  This allows the DOM model to be referenced from within
 * the FreeMarker template, with the targeted element name being the "root"
 * name when forming expressions.  See <a href="http://freemarker.org">freemarker.org</a>
 * for more info.
 *
 * @author tfennelly
 */
@VisitBeforeReport(summary = "FreeMarker Template - See Detail.", detailTemplate = "reporting/FreeMarkerTemplateProcessor_before.html")
@VisitAfterReport(summary = "FreeMarker Template - See Detail.", detailTemplate = "reporting/FreeMarkerTemplateProcessor_After.html")
public class FreeMarkerTemplateProcessor extends AbstractTemplateProcessor implements SAXElementVisitor, Consumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(FreeMarkerTemplateProcessor.class);

    @ConfigParam(name = Filter.ENTITIES_REWRITE, defaultVal = "true")
    private boolean rewriteEntities;
    @ConfigParam(name = "templating.freemarker.defaultNumberFormat", defaultVal = FreeMarkerTemplate.DEFAULT_MACHINE_READABLE_NUMBER_FORMAT)
    private String defaultNumberFormat;

    private Template defaultTemplate;
    private Template templateBefore;
    private Template templateAfter;
    private SmooksResourceConfiguration config;
    private DefaultSAXElementSerializer targetWriter;

    /**
     * Default constructor.
     */
    protected FreeMarkerTemplateProcessor() {
    }

    /**
     * Programmatically configure the FreeMarker Templating Visitor.
     * @param templatingConfiguration The templating configuration.
     * @return This Visitor instance.
     */
    public FreeMarkerTemplateProcessor(TemplatingConfiguration templatingConfiguration) {
        super.setTemplatingConfiguration(templatingConfiguration);
    }

    @Override
	protected void loadTemplate(SmooksResourceConfiguration config) throws IOException {
        this.config = config;

        Configuration configuration = new Configuration(Configuration.VERSION_2_3_21);

        configuration.setSharedVariable("serialize", new NodeModelSerializer());
        configuration.setNumberFormat(defaultNumberFormat);

        if (config.isInline()) {
            byte[] templateBytes = config.getBytes();
            String[] templates = (new String(templateBytes)).split(AbstractTemplateProcessor.TEMPLATE_SPLIT_PI);

            if(templates.length == 1) {
                if(applyTemplateBefore()) {
                    defaultTemplate = new Template("free-marker-template", new StringReader(templates[0]), configuration);
                } else {
                    defaultTemplate = new Template("free-marker-template", new StringReader(templates[0]), configuration);
                }
            } else if(templates.length == 2) {
                if(getAction() != Action.REPLACE) {
                    throw new UnsupportedOperationException("Split templates only supported on the REPLACE action.");
                }
                templateBefore = new Template("free-marker-template-before", new StringReader(templates[0]), configuration);
                templateAfter = new Template("free-marker-template-after", new StringReader(templates[1]), configuration);
            } else {
                throw new IOException("Invalid FreeMarker template config.  Zero split tokens.");
            }
        } else {
            TemplateLoader[] loaders = new TemplateLoader[]{new FileTemplateLoader(), new ContextClassLoaderTemplateLoader()};
            MultiTemplateLoader multiLoader = new MultiTemplateLoader(loaders);

            configuration.setTemplateLoader(multiLoader);
            if(applyTemplateBefore()) {
                defaultTemplate = configuration.getTemplate(config.getResource());
            } else {
                defaultTemplate = configuration.getTemplate(config.getResource());
            }
        }

        // We'll use the DefaultSAXElementSerializer to write out the targeted element
        // where the action is not "replace" or "bindto".
        targetWriter = new DefaultSAXElementSerializer();
        targetWriter.setWriterOwner(this);
        targetWriter.setRewriteEntities(rewriteEntities);
    }

    public boolean consumes(Object object) {
        if(defaultTemplate != null && defaultTemplate.toString().indexOf(object.toString()) != -1) {
            return true;
        } else if(templateBefore != null && templateBefore.toString().indexOf(object.toString()) != -1) {
            return true;
        } else if(templateAfter != null && templateAfter.toString().indexOf(object.toString()) != -1) {
            return true;
        }

        return false;
    }

    public void visitBefore(Element element, ExecutionContext executionContext) throws SmooksException {
        if(defaultTemplate != null) {
            if(applyTemplateBefore()) {
                applyTemplate(defaultTemplate, element, executionContext);
            }
        } else {
            // Must be a split template...
            throw new UnsupportedOperationException("Split templates not supported for DOM based filtering.");
        }
    }

    public void visitAfter(Element element, ExecutionContext executionContext) throws SmooksException {
        if(defaultTemplate != null) {
            if(!applyTemplateBefore()) {
                applyTemplate(defaultTemplate, element, executionContext);
            }
        } else {
            // Must be a split template...
            throw new UnsupportedOperationException("Split templates not supported for DOM based filtering.");
        }
    }

    /**
     * Apply the template for DOM.
     *
     * @param element          The targeted DOM Element.
     * @param executionContext The Smooks execution context.
     * @throws org.smooks.SmooksException Failed to apply template. See cause.
     */
    @Override
	protected void visit(Element element, ExecutionContext executionContext) throws SmooksException {
        // Not used in this implementation.
        throw new UnsupportedOperationException("This method should not be called on this implementation.");
    }

	private void applyTemplate(Template template, Element element, ExecutionContext executionContext) throws SmooksException {
        // Apply the template...
        String templatingResult;
        try {
            Writer writer = new StringWriter();
            Map<String, Object> model = FreeMarkerUtils.getMergedModel(executionContext);

            template.process(model, writer);
            writer.flush();
            templatingResult = writer.toString();
        } catch (TemplateException e) {
            throw new SmooksException("Failed to apply FreeMarker template to fragment '" + DomUtils.getXPath(element) + "'.  Resource: " + config, e);
        } catch (IOException e) {
            throw new SmooksException("Failed to apply FreeMarker template to fragment '" + DomUtils.getXPath(element) + "'.  Resource: " + config, e);
        }

        // Create the replacement DOM text node containing the applied template...
        Node resultNode = TextSerializationUnit.createTextElement(element, templatingResult);

        // Process the templating action, supplying the templating result...
        processTemplateAction(element, resultNode, executionContext);
    }

    /* ------------------------------------------------------------------------------------------------------------------------------------------
    SAX Processing methods.
    ------------------------------------------------------------------------------------------------------------------------------------------ */

    public void visitBefore(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
        String outputStreamResourceName = getOutputStreamResource();
        if(outputStreamResourceName != null) {
            if(applyTemplateBefore()) {
                applyTemplateToOutputStream(defaultTemplate, element, outputStreamResourceName, executionContext);
            }
        } else {
            if (getAction() == Action.INSERT_BEFORE) {
                // apply the template...
                applyTemplate(defaultTemplate, element, executionContext);
                // write the start of the element...
                if (executionContext.getDeliveryConfig().isDefaultSerializationOn()) {
                    targetWriter.visitBefore(element, executionContext);
                }
            } else if (getAction() == Action.REPLACE) {
                Writer currentWriter = element.getWriter(this);

                if(templateBefore != null) {
                    applyTemplate(templateBefore, element, executionContext);
                } else if(executionContext.isDefaultSerializationOn()) {
                    // If Default Serialization is on, we want to block output to the
                    // output stream...
                    element.setWriter(new NullWriter(currentWriter), this);
                }
            } else if (getAction() != Action.REPLACE && getAction() != Action.BIND_TO) {
                // write the start of the element...
                if (executionContext.getDeliveryConfig().isDefaultSerializationOn()) {
                    targetWriter.visitBefore(element, executionContext);
                }
            } else {
                // Just acquire ownership of the writer, but only do so if the action is not a BIND_TO
                // and default serialization is on.  BIND_TO will not use the writer, so no need to
                // acquire it for that action...
                if (getAction() != Action.BIND_TO && executionContext.getDeliveryConfig().isDefaultSerializationOn()) {
                    element.getWriter(this);
                }
            }
        }
    }

    public void onChildText(SAXElement element, SAXText childText, ExecutionContext executionContext) throws SmooksException, IOException {
        if(getOutputStreamResource() == null) {
            if (getAction() != Action.REPLACE && getAction() != Action.BIND_TO) {
                if (executionContext.getDeliveryConfig().isDefaultSerializationOn()) {
                    targetWriter.onChildText(element, childText, executionContext);
                }
            }
        }
    }

    public void onChildElement(SAXElement element, SAXElement childElement, ExecutionContext executionContext) throws SmooksException, IOException {
        if(getOutputStreamResource() == null) {
            if (getAction() != Action.REPLACE && getAction() != Action.BIND_TO) {
                if (executionContext.getDeliveryConfig().isDefaultSerializationOn()) {
                    targetWriter.onChildElement(element, childElement, executionContext);
                }
            }
        }
    }

    public void visitAfter(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
        String outputStreamResourceName = getOutputStreamResource();
        if(outputStreamResourceName != null) {
            if(!applyTemplateBefore()) {
                applyTemplateToOutputStream(defaultTemplate, element, outputStreamResourceName, executionContext);
            }
        } else {
            if (getAction() == Action.ADDTO) {
                if (!targetWriter.isStartWritten(element)) {
                    if (executionContext.getDeliveryConfig().isDefaultSerializationOn()) {
                        targetWriter.writeStartElement(element);
                    }
                }
                // apply the template...
                applyTemplate(defaultTemplate, element, executionContext);
                // write the end of the element...
                if (executionContext.getDeliveryConfig().isDefaultSerializationOn()) {
                    targetWriter.visitAfter(element, executionContext);
                }
            } else if (getAction() == Action.INSERT_BEFORE) {
                // write the end of the element...
                if (executionContext.getDeliveryConfig().isDefaultSerializationOn()) {
                    targetWriter.visitAfter(element, executionContext);
                }
            } else if (getAction() == Action.INSERT_AFTER) {
                // write the end of the element...
                if (executionContext.getDeliveryConfig().isDefaultSerializationOn()) {
                    targetWriter.visitAfter(element, executionContext);
                }
                // apply the template...
                applyTemplate(defaultTemplate, element, executionContext);
            } else if (getAction() == Action.REPLACE) {
                // Reset the writer and then apply the template...
                Writer writer = element.getWriter(this);

                if(writer instanceof NullWriter) {
                    element.setWriter(((NullWriter)writer).getParentWriter(), this);
                }

                if(templateAfter != null) {
                    applyTemplate(templateAfter, element, executionContext);
                } else {
                    applyTemplate(defaultTemplate, element, executionContext);
                }
            } else if (getAction() == Action.BIND_TO) {
                // just apply the template...
                applyTemplate(defaultTemplate, element, executionContext);
            }
        }
    }

    private void applyTemplateToOutputStream(Template template, SAXElement element, String outputStreamResourceName, ExecutionContext executionContext) {
        Writer writer = AbstractOutputStreamResource.getOutputWriter(outputStreamResourceName, executionContext);
        applyTemplate(template, element, executionContext, writer);
    }

    private void applyTemplate(Template template, SAXElement element, ExecutionContext executionContext) throws SmooksException {
        if (getAction() == Action.BIND_TO) {
            Writer writer = new StringWriter();
            applyTemplate(template, element, executionContext, writer);

            executionContext.getBeanContext().addBean(getBindBeanId(), writer.toString(), new Fragment(element));
        } else {
            Writer writer = element.getWriter(this);
            applyTemplate(template, element, executionContext, writer);
        }
    }

    private void applyTemplate(Template template, SAXElement element, ExecutionContext executionContext, Writer writer) throws SmooksException {
        try {
            Map<String, Object> model = FreeMarkerUtils.getMergedModel(executionContext);
            template.process(model, writer);
            writer.flush();
        } catch (TemplateException e) {
            throw new SmooksException("Failed to apply FreeMarker template to fragment '" + SAXUtil.getXPath(element) + "'.  Resource: " + config, e);
        } catch (IOException e) {
            throw new SmooksException("Failed to apply FreeMarker template to fragment '" + SAXUtil.getXPath(element) + "'.  Resource: " + config, e);
        }
    }

    private static class ContextClassLoaderTemplateLoader extends URLTemplateLoader {

        @Override
		protected URL getURL(String name) {
            return Thread.currentThread().getContextClassLoader().getResource(name);
        }

    }
}
