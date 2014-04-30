package org.milyn.smooks.scripting.groovy;

import groovy.xml.XmlUtil;
import groovy.xml.dom.DOMCategory;
import groovy.xml.DOMBuilder;

import org.milyn.container.ExecutionContext
import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.SmooksException;
import org.milyn.javabean.repository.BeanRepository;
import org.milyn.javabean.context.BeanContext;

import org.milyn.delivery.DomModelCreator
import org.milyn.delivery.DOMModel
import org.milyn.delivery.dom.DOMVisitBefore
import org.milyn.delivery.dom.DOMVisitAfter
import org.milyn.delivery.dom.serialize.Serializer
import org.milyn.xml.*;
import org.milyn.io.NullWriter;

import org.milyn.delivery.sax.SAXVisitBefore;
import org.milyn.delivery.sax.SAXVisitAfter;
import org.milyn.delivery.sax.SAXElement;

import java.io.IOException;
import org.w3c.dom.*;
import java.util.Map;

${imports}

<#if visitBefore>
class ${visitorName} implements DOMVisitBefore, SAXVisitBefore {

    private SmooksResourceConfiguration config;

	public void setConfiguration(SmooksResourceConfiguration config) {
		this.config = config;
	}

    public void visitBefore(Element element, ExecutionContext executionContext) {
        Document document = element.getOwnerDocument();
        Map nodeModels = DOMModel.getModel(executionContext).getModels();

        def getBean = { beanId ->
            executionContext.getBeanContext().getBean(beanId);
        }

        ${visitorScript}
    }

    public void visitBefore(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
        Map nodeModels = DOMModel.getModel(executionContext).getModels();

        def getBean = { beanId ->
            executionContext.getBeanContext().getBean(beanId);
        }

        ${visitorScript}
    }
}
<#else>
class ${visitorName} implements DOMVisitAfter, SAXVisitBefore, SAXVisitAfter {

    private SmooksResourceConfiguration config;
    private DomModelCreator modelCreator;
    private boolean format = false;
    private boolean isWritingFragment = false;

	public void setConfiguration(SmooksResourceConfiguration config) {
		this.config = config;

		if(config.getBoolParameter("createDOMFragment", true)) {
		    modelCreator = new DomModelCreator();
		}
		format = config.getBoolParameter("format", false);
		isWritingFragment = config.getBoolParameter("writeFragment", false);
	}

    public void visitAfter(Element element, ExecutionContext executionContext) {
        visitAfter(element, executionContext, null);
    }

    public void visitAfter(Element element, ExecutionContext executionContext, Writer writer) {
        Document document = element.getOwnerDocument();
        Map nodeModels = DOMModel.getModel(executionContext).getModels();

        def getBean = { beanId ->
            executionContext.getBeanContext().getBean(beanId);
        }
        def writeFragment = { outNode ->
            if(outNode.getNodeType() == Node.ELEMENT_NODE) {
                Serializer.recursiveDOMWrite((Element) outNode, writer);
            } else if(outNode.getNodeType() == Node.DOCUMENT_NODE) {
                Serializer.recursiveDOMWrite(outNode.getDocumentElement(), writer);
            } else {
                throw new SmooksException("Call to 'writeFragment' with a non Document/Element Node.  Node type: " + outNode.getClass().getName());
            }
        }

        ${visitorScript}
    }

    // visitBefore is required purely for setting up the model creator...
    public void visitBefore(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
        if(modelCreator != null) {
            if(isWritingFragment) {
                Writer currentWriter = element.getWriter(this);
                // If fragment writing is on, we want to block output to the
                // output stream...
                element.setWriter(new NullWriter(currentWriter), this);
            }

            modelCreator.visitBefore(element, executionContext);
        }
    }

    public void visitAfter(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
        if(modelCreator != null) {
            Document fragmentDoc = modelCreator.popCreator(executionContext);
            Element fragmentElement = fragmentDoc.getDocumentElement();

            if(isWritingFragment) {
                Writer writer = element.getWriter(this);
                if(writer instanceof NullWriter) {
                    // Reset the writer...
                    writer = ((NullWriter)writer).getParentWriter();
                    element.setWriter(writer, this);
                }

                visitAfter(fragmentElement, executionContext, writer);
            } else {
                visitAfter(fragmentElement, executionContext);
            }
        } else {
            Map nodeModels = DOMModel.getModel(executionContext).getModels();

            def getBean = { beanId ->
                executionContext.getBeanContext().getBean(beanId);
            }

            ${visitorScript}
        }
    }
}
</#if>