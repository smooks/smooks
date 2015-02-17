package org.milyn.javabean.performance.model;
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
	
	public List<Address> addresses = new ArrayList<Address>();
	
	public List<Order> orders = new ArrayList<Order>();

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
	public void setAddresses(List<Address> addresses) {
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
	public void setOrders(List<Order> orders) {
		this.orders = orders;
	}
	
}
