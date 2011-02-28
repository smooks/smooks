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
package org.milyn.templating;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.milyn.SmooksException;
import org.milyn.delivery.Fragment;
import org.milyn.delivery.sax.SAXUtil;
import org.milyn.util.CollectionsUtil;
import org.milyn.assertion.AssertArgument;
import org.milyn.cdr.SmooksConfigurationException;
import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.cdr.annotation.AppContext;
import org.milyn.cdr.annotation.ConfigParam;
import org.milyn.cdr.annotation.Config;
import org.milyn.container.ApplicationContext;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.dom.DOMElementVisitor;
import org.milyn.delivery.dom.serialize.ContextObjectSerializationUnit;
import org.milyn.delivery.dom.serialize.TextSerializationUnit;
import org.milyn.delivery.annotation.Initialize;
import org.milyn.delivery.ordering.Producer;
import org.milyn.io.AbstractOutputStreamResource;
import org.milyn.javabean.DataDecodeException;
import org.milyn.javabean.DataDecoder;
import org.milyn.javabean.repository.BeanId;
import org.milyn.javabean.repository.BeanRepositoryManager;
import org.milyn.xml.DomUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.transform.TransformerConfigurationException;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Set;

/**
 * Abstract template processing unit.
 * <p/>
 * Defines abstract methods for loading the template in question, as well as convienience methods for
 * processing the template action against the templating result (replace, addto, insertbefore and insertafter).
 * <p/>
 * See implementations.
 * @author tfennelly
 */
public abstract class AbstractTemplateProcessor implements DOMElementVisitor, Producer {

    /**
     * Template split point processing instruction.
     */
    public static final String TEMPLATE_SPLIT_PI = "<\\?TEMPLATE-SPLIT-PI\\?>";

    private final Log logger = LogFactory.getLog(getClass());
    private static boolean legactVisitBeforeParamWarn = false;

    protected enum Action {
        REPLACE,
        ADDTO,
        INSERT_BEFORE,
        INSERT_AFTER,
        BIND_TO,
    }

    private TemplatingConfiguration templatingConfiguration;

    @ConfigParam(defaultVal = "false")
    private boolean applyTemplateBefore;

    @ConfigParam(name = "action", defaultVal = "replace", choice = {"replace", "addto", "insertbefore", "insertafter", "bindto"}, decoder = ActionDecoder.class)
    private Action action;

    @ConfigParam(defaultVal = "UTF-8")
    private Charset encoding = Charset.forName("UTF-8");

    @ConfigParam(use = ConfigParam.Use.OPTIONAL)
    private String bindId;

    @ConfigParam(use = ConfigParam.Use.OPTIONAL)
    private String outputStreamResource;

    @Config
    private SmooksResourceConfiguration smooksConfig;

    @AppContext
    private ApplicationContext applicationContext;

    private BeanId bindBeanId;

    @Initialize
    public void initialize() {
        if(templatingConfiguration != null) {
            SmooksResourceConfiguration config = new SmooksResourceConfiguration();

            config.setResource(templatingConfiguration.getTemplate());

            Usage resultUsage = templatingConfiguration.getUsage();
            if(resultUsage == Inline.ADDTO) {
                action = Action.ADDTO;
            } else if(resultUsage == Inline.REPLACE) {
                action = Action.REPLACE;
            } else if(resultUsage == Inline.INSERT_BEFORE) {
                action = Action.INSERT_BEFORE;
            } else if(resultUsage == Inline.INSERT_AFTER) {
                action = Action.INSERT_AFTER;
            } else if(resultUsage instanceof BindTo) {
                action = Action.BIND_TO;
                bindId = ((BindTo)resultUsage).getBeanId();
                bindBeanId = applicationContext.getBeanIdStore().register(bindId);
            } else if(resultUsage instanceof OutputTo) {
                outputStreamResource = ((OutputTo)resultUsage).getOutputStreamResource();
            }

            try {
                loadTemplate(config);
            } catch (Exception e) {
                throw new SmooksConfigurationException("Error loading Templating resource: " + config, e);
            }
        } else if(smooksConfig != null) {
            if(smooksConfig.getResource() == null) {
                throw new SmooksConfigurationException("Templating resource undefined in resource configuration: " + smooksConfig);
            }

            try {
                loadTemplate(smooksConfig);
            } catch (Exception e) {
                throw new SmooksConfigurationException("Error loading Templating resource: " + smooksConfig, e);
            }
            String visitBefore = smooksConfig.getStringParameter("visitBefore");
            if(visitBefore != null) {
                if(!legactVisitBeforeParamWarn) {
                    logger.warn("Templating <param> 'visitBefore' deprecated.  Use 'applyTemplateBefore'.");
                    legactVisitBeforeParamWarn = true;
                }
                this.applyTemplateBefore = visitBefore.equalsIgnoreCase("true");
            }

            if(action == Action.BIND_TO) {
                if(bindId == null) {
                    throw new SmooksConfigurationException("'bindto' templating action configurations must also specify a 'bindId' configuration for the Id under which the result is bound to the ExecutionContext");
                } else {
                    bindBeanId = applicationContext.getBeanIdStore().register(bindId);
                }
            }
        } else {
            throw new SmooksConfigurationException(getClass().getSimpleName() + " not configured.");
        }
    }

    protected void setTemplatingConfiguration(TemplatingConfiguration templatingConfiguration) {
        AssertArgument.isNotNull(templatingConfiguration, "templatingConfiguration");
        this.templatingConfiguration = templatingConfiguration;
    }

    protected abstract void loadTemplate(SmooksResourceConfiguration config) throws IOException, TransformerConfigurationException;

    public boolean applyTemplateBefore() {
        return applyTemplateBefore;
    }

    public Set<String> getProducts() {
        if(outputStreamResource != null) {
            return CollectionsUtil.toSet(outputStreamResource);
        } else {
            return CollectionsUtil.toSet();
        }
    }

    protected Action getAction() {
        return action;
    }

    public Charset getEncoding() {
        return encoding;
    }

    public String getBindId() {
        return bindId;
    }

    public String getOutputStreamResource() {
        return outputStreamResource;
    }

    protected void processTemplateAction(Element element, Node templatingResult, ExecutionContext executionContext) {
		// REPLACE needs to be handled explicitly...
		if(getOutputStreamResource() == null && action == Action.REPLACE) {
            DomUtils.replaceNode(templatingResult, element);
        } else {
    		_processTemplateAction(element, templatingResult, action, executionContext);
        }
	}

	protected void processTemplateAction(Element element, NodeList templatingResultNodeList, ExecutionContext executionContext) {
		// If we're at the root element
		if(element.getParentNode() instanceof Document) {
			int count = templatingResultNodeList.getLength();

			// Iterate over the NodeList and filter the action using the
			// first element node we encounter as the transformation result...
			for(int i = 0; i < count; i++) {
				Node node = templatingResultNodeList.item(0);
				if(node.getNodeType() == Node.ELEMENT_NODE) {
					processTemplateAction(element, node, executionContext);
					break;
				}
			}
		} else if(action == Action.REPLACE) {
			// When we're not at the root element, REPLACE needs to be handled explicitly
			// by performing a series of insert-befores, followed by a remove of the
			// target element...
			processTemplateAction(element, templatingResultNodeList, Action.INSERT_BEFORE, executionContext);
			element.getParentNode().removeChild(element);
        } else {
			processTemplateAction(element, templatingResultNodeList, action, executionContext);
        }
	}

	private void processTemplateAction(Element element, NodeList templatingResultNodeList, Action action, ExecutionContext executionContext) {
		int count = templatingResultNodeList.getLength();

		// Iterate over the NodeList and filter each Node against the action.
		for(int i = 0; i < count; i++) {
			// We iterate over the list in this way because the nodes are auto removed from the
			// the list as they are added/inserted elsewhere.
			_processTemplateAction(element, templatingResultNodeList.item(0), action, executionContext);
		}
	}

	private void _processTemplateAction(Element element, Node node, Action action, ExecutionContext executionContext) {
        Node parent = element.getParentNode();

        // Can't insert before or after the root element...
        if(parent instanceof Document && (action == Action.INSERT_BEFORE || action == Action.INSERT_AFTER)) {
            logger.debug("Insert before/after root element not allowed.  Consider using the replace action!!");
            return;
        }

        String outputStreamResourceName = getOutputStreamResource();
        if(outputStreamResourceName != null) {
            Writer writer = AbstractOutputStreamResource.getOutputWriter(outputStreamResourceName, executionContext);
            String text = extractTextContent(node, executionContext);
            try {
                writer.write(text);
            } catch (IOException e) {
                throw new SmooksException("Failed to write to output stream resource '" + outputStreamResourceName + "'.", e);
            }
        } else {
            if(action == Action.ADDTO) {
                element.appendChild(node);
            } else if(action == Action.INSERT_BEFORE) {
                DomUtils.insertBefore(node, element);
            } else if(action == Action.INSERT_AFTER) {
                Node nextSibling = element.getNextSibling();

                if(nextSibling == null) {
                    // "element" is the last child of "parent" so just add to "parent".
                    parent.appendChild(node);
                } else {
                    // insert before the "nextSibling" - Node doesn't have an "insertAfter" operation!
                    DomUtils.insertBefore(node, nextSibling);
                }
            } else if(action == Action.BIND_TO) {
            	String text = extractTextContent(node, executionContext);

            	executionContext.getBeanContext().addBean(bindBeanId, text, new Fragment(element));
            } else if(action == Action.REPLACE) {
                // Don't perform any "replace" actions here!
            }
        }
    }

    private String extractTextContent(Node node, ExecutionContext executionContext) {
        if(node.getNodeType() == Node.TEXT_NODE) {
            return node.getTextContent();
        } else if(node.getNodeType() == Node.ELEMENT_NODE && ContextObjectSerializationUnit.isContextObjectElement((Element) node)) {
            String contextKey = ContextObjectSerializationUnit.getContextKey((Element) node);
            return (String) executionContext.getAttribute(contextKey);
        } else if(node.getNodeType() == Node.ELEMENT_NODE && TextSerializationUnit.isTextElement((Element) node)) {
            return TextSerializationUnit.getText((Element) node);
        } else {
            throw new SmooksException("Unsupported 'bindTo' or toOutStream templating action.  The bind data must be attached to a DOM Text node, or already bound to a <context-object> element.");
        }
    }

    public void visitBefore(Element element, ExecutionContext executionContext) throws SmooksException {
        if(applyTemplateBefore) {
            visit(element, executionContext);
        }
    }

    public void visitAfter(Element element, ExecutionContext executionContext) throws SmooksException {
        if(!applyTemplateBefore) {
            visit(element, executionContext);
        }
    }

    protected abstract void visit(Element element, ExecutionContext executionContext) throws SmooksException;

    public static class ActionDecoder implements DataDecoder {
        public Object decode(String data) throws DataDecodeException {
            if("addto".equals(data)) {
                return Action.ADDTO;
            } else if("insertbefore".equals(data)) {
                return Action.INSERT_BEFORE;
            } else if("insertafter".equals(data)) {
                return Action.INSERT_AFTER;
            } else if("bindto".equals(data)) {
                return Action.BIND_TO;
            } else {
                return Action.REPLACE;
            }
        }
    }

	/**
	 * @return the bindBeanId
	 */
	public BeanId getBindBeanId() {
		return bindBeanId;
	}
}
