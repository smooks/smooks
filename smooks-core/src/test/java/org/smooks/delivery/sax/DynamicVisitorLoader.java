package org.smooks.delivery.sax;

import org.smooks.SmooksException;
import org.smooks.container.ExecutionContext;

import java.io.IOException;

/**
 * @author
 */
public class DynamicVisitorLoader implements SAXVisitBefore, SAXVisitAfter {

    public static DynamicVisitor visitor = new DynamicVisitor();

    public void visitBefore(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
        visitor.stuff.setLength(0);
        DynamicSAXElementVisitorList.addDynamicVisitor(visitor, executionContext);
    }

    public void visitAfter(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
        DynamicSAXElementVisitorList.removeDynamicVisitor(visitor, executionContext);
    }

    public static class DynamicVisitor implements SAXElementVisitor {

        public StringBuilder stuff = new StringBuilder();

        public void visitBefore(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
            stuff.append("<" + element.getName() + ">");
        }

        public void onChildText(SAXElement element, SAXText childText, ExecutionContext executionContext) throws SmooksException, IOException {
            stuff.append(childText.getText());
        }

        public void onChildElement(SAXElement element, SAXElement childElement, ExecutionContext executionContext) throws SmooksException, IOException {
        }

        public void visitAfter(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
            stuff.append("</" + element.getName() + ">");
        }
    }
}
