/*
	Milyn - Copyright (C) 2006 - 2010

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License (version 2.1) as published by the Free Software
	Foundation.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

	See the GNU Lesser General Public License for more details:
	http://www.gnu.org/licenses/lgpl.txt
*/
package example.model;

import java.util.Date;
import java.math.BigDecimal;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class Header {
    private String orderId;
    private long orderStatus;
    private BigDecimal netAmount;
    private BigDecimal totalAmount;
    private BigDecimal tax;
    private Date date;
    private Customer customer;
    private Address billingAddress;
    private Address shippingAddress;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public long getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(long orderStatus) {
        this.orderStatus = orderStatus;
    }

    public BigDecimal getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(BigDecimal netAmount) {
        this.netAmount = netAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
    public Address getBillingAddress() {
		return billingAddress;
	}

	public void setBillingAddress(Address billingAddress) {
		this.billingAddress = billingAddress;
	}

	public Address getShippingAddress() {
		return shippingAddress;
	}

	public void setShippingAddress(Address shippingAddress) {
		this.shippingAddress = shippingAddress;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Header[\n\t\torderId = ");
		builder.append(orderId);
		builder.append(",\n\t\torderStatus = ");
		builder.append(orderStatus);
		builder.append(",\n\t\tdate = ");
		builder.append(date);
		builder.append(",\n\t\tnetAmount = ");
		builder.append(netAmount);
		builder.append(",\n\t\ttotalAmount = ");
		builder.append(totalAmount);
		builder.append(",\n\t\ttax =" );
		builder.append(tax);
		builder.append(",\n\t\tcustomer = ");
		builder.append(customer);
		builder.append(",\n\t\tbillingAddress = ");
		builder.append(billingAddress);
		builder.append(",\n\t\tshippingAddress = ");
		builder.append(shippingAddress);
		builder.append("\n\t]");
		return builder.toString();
	}

}
