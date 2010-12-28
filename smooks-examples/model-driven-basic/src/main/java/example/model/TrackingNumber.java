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

/**
 * Shipping Tracking Number bean.
 * 
 * @author tfennelly
 */
public class TrackingNumber {

    private String shipperID;
    private String shipmentNumber;

    public String getShipperID() {
        return shipperID;
    }
    public void setShipperID(String shipperID) {
        this.shipperID = shipperID.trim();
    }
    public String getShipmentNumber() {
        return shipmentNumber;
    }
    public void setShipmentNumber(String shipmentNumber) {
        this.shipmentNumber = shipmentNumber.trim();
    }

    public String toString() {
        return shipperID + ":" + shipmentNumber;
    }
}
