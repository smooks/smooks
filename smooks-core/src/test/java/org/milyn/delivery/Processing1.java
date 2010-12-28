package org.milyn.delivery;

import org.milyn.delivery.dom.DOMElementVisitor;
import org.milyn.container.ExecutionContext;
import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.cdr.SmooksConfigurationException;
import org.w3c.dom.Element;

/**
 * @author
 */
public class Processing1  implements DOMElementVisitor {
    public void visitBefore(Element element, ExecutionContext executionContext) {
    }

    public void visitAfter(Element element, ExecutionContext executionContext) {
    }

    public void setConfiguration(SmooksResourceConfiguration resourceConfig) throws SmooksConfigurationException {
    }
}
