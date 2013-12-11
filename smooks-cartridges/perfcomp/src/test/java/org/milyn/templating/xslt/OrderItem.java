package org.milyn.templating.xslt;

import org.w3c.dom.Element;
import org.milyn.commons.xml.DomUtils;

/**
 * order-item bean.
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class OrderItem {
    private String price;
    private String quantity;
    private String product;
    private String title;

    public void setPrice(String price) {
        this.price = price;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void addToElement(Element orderItem) {
        orderItem.setAttribute("price", price);
        orderItem.setAttribute("quantity", quantity);
        orderItem.setAttribute("product-id", product);
        DomUtils.addLiteral(orderItem, title);
    }
}
