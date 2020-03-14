package org.smooks.javabean.context;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.smooks.assertion.AssertArgument;
import org.smooks.javabean.repository.BeanId;

/**
 * Bean Id Store
 * <p/>
 * Represents a map of BeanId's. Every BeanId has it own unique index. The index
 * is incremental. The index starts with zero.
 * <p/>
 * Once a BeanId is registered it can never be unregistered.
 *
 * This object is thread safe.
 *
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class BeanIdStore {

	private volatile HashMap<String, BeanId>  beanIdMap = new  HashMap<String, BeanId>();

	/**
	 * registers a beanId name and returns the {@link BeanId} object.
	 * If the beanId name is already registered then belonging BeanId
	 * is returned.
	 *
	 * This method doesn't have a performance penalty anymore when then BeanId
	 * already exists.
	 *
	 * @return A new or existing BeanId.
	 */
	public  BeanId register(String beanIdName) {
		AssertArgument.isNotEmpty(beanIdName, "beanIdName");

		BeanId beanId = beanIdMap.get(beanIdName);
		if(beanId == null) {

			synchronized(this) {

				beanId = beanIdMap.get(beanIdName);
				if(beanId == null) {
					@SuppressWarnings("unchecked")
					HashMap<String, BeanId> newBeanIdMap = (HashMap<String, BeanId>) beanIdMap.clone();

					beanId = new BeanId(this, newBeanIdMap.size(), beanIdName);

					newBeanIdMap.put(beanIdName, beanId);

					beanIdMap = newBeanIdMap;
				}

			}
		}

		return beanId;
	}

	/**
	 * @return The BeanId or <code>null</code> if it is not registered;
	 *
	 */
	public BeanId getBeanId(String beanId) {
		return beanIdMap.get(beanId);
	}

	/**
	 * @return if the bean Id name is already registered.
	 *
	 */
	public boolean containsBeanId(String beanId) {
		return beanIdMap.containsKey(beanId);
	}

	/**
	 * Returns a copy of the internal bean id map
	 *
	 * @return An map where the key is the string based beanId and the value is the BeanId.
	 *
	 */
	public synchronized Map<String, BeanId> getBeanIdMap() {
		return Collections.unmodifiableMap(beanIdMap);
	}

	/**
	 * @return the current index size.
	 *
	 */
	public int size() {
		return beanIdMap.size();
	}
}
