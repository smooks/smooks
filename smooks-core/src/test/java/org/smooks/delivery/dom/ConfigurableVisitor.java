package org.smooks.delivery.dom;

import org.smooks.container.ExecutionContext;
import org.smooks.cdr.SmooksResourceConfiguration;
import org.smooks.cdr.SmooksConfigurationException;
import org.w3c.dom.Element;

/**
 * @author
 */
public class ConfigurableVisitor implements DOMElementVisitor {
    public void visitBefore(Element element, ExecutionContext executionContext) {
        element.setAttribute("visitedby-" + getClass().getSimpleName(), "true");
    }

    public void visitAfter(Element element, ExecutionContext executionContext) {
    }

    public void setConfiguration(SmooksResourceConfiguration resourceConfig) throws SmooksConfigurationException {
    }
}
