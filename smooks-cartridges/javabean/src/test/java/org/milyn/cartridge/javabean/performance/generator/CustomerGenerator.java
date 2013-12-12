package org.milyn.cartridge.javabean.performance.generator;


import org.milyn.cartridge.javabean.performance.model.Address;
import org.milyn.cartridge.javabean.performance.model.Article;
import org.milyn.cartridge.javabean.performance.model.Customer;
import org.milyn.cartridge.javabean.performance.model.Order;
import org.milyn.cartridge.javabean.performance.model.Person;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateSequenceModel;

public class CustomerGenerator implements TemplateSequenceModel {

		private final int size;
		
		/**
		 * 
		 */
		public CustomerGenerator(int size) {
			this.size = size;
		}
		
		/* (non-Javadoc)
		 * @see freemarker.template.TemplateSequenceModel#get(int)
		 */
		public TemplateModel get(int nmb) throws TemplateModelException {
			
			Customer c = new Customer();
			
			c.person = new Person();
			c.person.surname = "surname-" + nmb;
			c.person.firstname = "firstname-" + nmb;
			c.person.gender = (nmb % 2 == 0)? "m" : "f";
			c.person.phonenumber = "1000" + nmb;
			
			Address address = new Address();
			address.type = "billing";
			address.street = "street-" + nmb;
			address.housenumber = Integer.toString(nmb);
			address.zipcode = "12345";
			address.city = "City " + nmb;
			c.addresses.add(address);
			
			address = new Address();
			address.type = "delivery";
			address.street = "street-del-" + nmb;
			address.housenumber = Integer.toString(nmb);
			address.zipcode = "54321";
			address.city = "City " + nmb;
			c.addresses.add(address);
			
			Order order = new Order();
			order.number = nmb + "-" + 1;
			order.price = 10.20;
			order.size = 4;
			
			order.article = new Article();
			order.article.id = "1235467";
			order.article.name = "article" + nmb;
			order.article.price = 2.30;
			
			c.orders.add(order);
			
			order = new Order();
			order.number = nmb + "-" + 2;
			order.price = 10.20;
			order.size = 4;
			
			order.article = new Article();
			order.article.id = "1235467";
			order.article.name = "article" + nmb;
			order.article.price = 2.30;
			
			c.orders.add(order);
			
			return BeansWrapper.getDefaultInstance().wrap(c);
		}

		/* (non-Javadoc)
		 * @see freemarker.template.TemplateSequenceModel#size()
		 */
		public int size() throws TemplateModelException {
			return size;
		}

		
		
		
	}