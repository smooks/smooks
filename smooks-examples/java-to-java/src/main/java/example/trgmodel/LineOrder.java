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
package example.trgmodel;

import java.util.Arrays;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class LineOrder {
    private String customerId;
    private String customerName;

    private LineOrderPriority priority;

    private LineItem[] lineItems;

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public LineItem[] getLineItems() {
        return lineItems;
    }

    public void setLineItems(LineItem[] lineItems) {
        this.lineItems = lineItems;
    }

	/**
	 * @return the priority
	 */
	public LineOrderPriority getPriority() {
		return priority;
	}

	/**
	 * @param priority the priority to set
	 */
	public void setPriority(LineOrderPriority priority) {
		this.priority = priority;
	}

    @Override
	public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Class: " + getClass().getName() + "\n");
        stringBuilder.append("\tcustomerId: " + customerId + "\n");
        stringBuilder.append("\tcustomerName: " + customerName + "\n");
        stringBuilder.append("\tpriority: " + getPriority() + "\n");
        if(lineItems != null) {
            stringBuilder.append("\tlineItems: " + Arrays.asList(lineItems));
        }

        return stringBuilder.toString();
    }


}