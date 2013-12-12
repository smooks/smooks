package org.milyn.cartridge.javabean.performance.model;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 */

/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class Customer {
	
	public Person person;
	
	public List addresses = new ArrayList();
	
	public List orders = new ArrayList();

	/**
	 * @return the person
	 */
	public Person getPerson() {
		return person;
	}

	/**
	 * @param person the person to set
	 */
	public void setPerson(Person person) {
		this.person = person;
	}

	/**
	 * @return the addresses
	 */
	public List getAddresses() {
		return addresses;
	}

	/**
	 * @param addresses the addresses to set
	 */
	public void setAddresses(List addresses) {
		this.addresses = addresses;
	}

	/**
	 * @return the orders
	 */
	public List getOrders() {
		return orders;
	}

	/**
	 * @param orders the orders to set
	 */
	public void setOrders(List orders) {
		this.orders = orders;
	}
	
}
