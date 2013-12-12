package org.milyn.cartridge.javabean.performance.generator;

import org.milyn.cartridge.javabean.performance.model.Customer;
import org.milyn.cartridge.javabean.performance.model.Person;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateSequenceModel;

public class SimpleGenerator implements TemplateSequenceModel {

		private final int size;
		
		/**
		 * 
		 */
		public SimpleGenerator(int size) {
			this.size = size;
		}
		
		/* (non-Javadoc)
		 * @see freemarker.template.TemplateSequenceModel#get(int)
		 */
		public TemplateModel get(int nmb) throws TemplateModelException {
			
			Customer c = new Customer();
			
			c.person = new Person();
			c.person.surname = "surname-" + nmb;
			
			return BeansWrapper.getDefaultInstance().wrap(c);
		}

		/* (non-Javadoc)
		 * @see freemarker.template.TemplateSequenceModel#size()
		 */
		public int size() throws TemplateModelException {
			return size;
		}

		
		
		
	}