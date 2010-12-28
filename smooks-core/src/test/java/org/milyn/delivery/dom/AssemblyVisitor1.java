package org.milyn.delivery.dom;

import org.milyn.delivery.dom.DOMElementVisitor;
import org.milyn.delivery.dom.Phase;
import org.milyn.delivery.dom.VisitPhase;
import org.milyn.container.ExecutionContext;
import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.cdr.SmooksConfigurationException;
import org.w3c.dom.Element;

/**
 * @author
 */
@Phase(VisitPhase.ASSEMBLY)
public class AssemblyVisitor1 implements DOMElementVisitor {
    public void visitBefore(Element element, ExecutionContext executionContext) {
        element.setAttribute("visitedby-" + getClass().getSimpleName(), "true");
    }

    public void visitAfter(Element element, ExecutionContext executionContext) {
    }

    public void setConfiguration(SmooksResourceConfiguration resourceConfig) throws SmooksConfigurationException {
    }
}
