package org.smooks.delivery;

import org.smooks.delivery.dom.DOMElementVisitor;
import org.smooks.delivery.dom.Phase;
import org.smooks.delivery.dom.VisitPhase;
import org.smooks.container.ExecutionContext;
import org.smooks.cdr.SmooksResourceConfiguration;
import org.smooks.cdr.SmooksConfigurationException;
import org.w3c.dom.Element;

/**
 * @author
 */
@Phase(VisitPhase.ASSEMBLY)
public class Assembly1 implements DOMElementVisitor {
    public void visitBefore(Element element, ExecutionContext executionContext) {
    }

    public void visitAfter(Element element, ExecutionContext executionContext) {
    }

    public void setConfiguration(SmooksResourceConfiguration resourceConfig) throws SmooksConfigurationException {
    }
}
