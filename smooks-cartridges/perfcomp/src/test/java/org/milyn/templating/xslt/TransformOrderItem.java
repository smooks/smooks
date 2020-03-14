package org.smooks.templating.xslt;

import org.smooks.delivery.dom.DOMElementVisitor;
import org.smooks.container.ExecutionContext;
import org.smooks.cdr.annotation.ConfigParam;
import org.smooks.xml.DomUtils;
import org.smooks.SmooksException;
import org.w3c.dom.Element;

import java.util.List;
import java.util.ArrayList;

/**
 * Transform the order-item node using the current populated OrderItem bean.
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class TransformOrderItem implements DOMElementVisitor {

    @ConfigParam(defaultVal = "false")
    private boolean removeItem;


    public void visitBefore(Element element, ExecutionContext executionContext) throws SmooksException {
    }

    public void visitAfter(Element orderItemElement, ExecutionContext executionContext) throws SmooksException {
        OrderItem orderItem = (OrderItem) executionContext.getBeanContext().getBean("order-item");

        // Remove the order items child content
        DomUtils.removeChildren(orderItemElement);
        // Readd the order item data in the form required by the target
        orderItem.addToElement(orderItemElement);
        if(removeItem) {
            // Remove the order item from the message
            DomUtils.removeElement(orderItemElement, false);
            // cache the order item - readded to the OrderLines at the end.
            cacheOrderItem(orderItemElement, executionContext);
        }
    }

    private static final String KEY = TransformOrderItem.class.getName() + "#order-items";

    private void cacheOrderItem(Element orderItemElement, ExecutionContext containerRequest) {
        List<Element> orderItems = getOrderItems(containerRequest);
        orderItems.add(orderItemElement);
    }

    public static List<Element> getOrderItems(ExecutionContext containerRequest) {
        List<Element> orderItems = (List<Element>) containerRequest.getAttribute(KEY);

        if(orderItems == null) {
            orderItems = new ArrayList<Element>();
            containerRequest.setAttribute(KEY, orderItems);
        }
        return orderItems;
    }
}
