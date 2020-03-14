package org.smooks.templating.xslt;

import org.smooks.delivery.dom.DOMElementVisitor;
import org.smooks.container.ExecutionContext;
import org.smooks.xml.DomUtils;
import org.smooks.SmooksException;
import org.w3c.dom.Element;

import java.util.List;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class TransformOrderItems implements DOMElementVisitor {
    

    public void visitBefore(Element element, ExecutionContext executionContext) throws SmooksException {
    }

    public void visitAfter(Element order, ExecutionContext executionContext) throws SmooksException {
        List<Element> orderItems = TransformOrderItem.getOrderItems(executionContext);
        Element orderLines = order.getOwnerDocument().createElement("OrderLines");

        // Add the OrderLines element
        order.appendChild(orderLines);
        // Readd the cached order items to the new OrderLines element
        DomUtils.appendList(orderLines, orderItems);

        /*
        Node firstOrderItem = orderItems.get(0);
        Document doc = order.getOwnerDocument();

        DomUtils.insertBefore(doc.createTextNode("<OrderLines>"), firstOrderItem);
        order.appendChild(doc.createTextNode("</OrderLines>"));
        */
    }
}
