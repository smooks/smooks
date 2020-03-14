package org.smooks.general;

import org.smooks.delivery.dom.DOMVisitAfter;
import org.smooks.container.ExecutionContext;
import org.smooks.SmooksException;
import org.w3c.dom.Element;

/**
 * @author
 */
public class DOMVisitor implements DOMVisitAfter {
    
    public void visitAfter(Element element, ExecutionContext executionContext) throws SmooksException {
    }
}
