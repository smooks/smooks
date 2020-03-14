package org.smooks.javabean;

/**
 * @author
 */
@OrderItemAnnotation
public class OrderItem {
    private long productId;
    private Integer quantity;
    private double price;
    private Order order;

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

    public String toString() {
        return "{productId: " + productId + ", quantity: " + quantity + ", price: " + price + "}";
    }
}
