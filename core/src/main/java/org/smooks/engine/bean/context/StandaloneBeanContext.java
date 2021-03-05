/*-
 * ========================LICENSE_START=================================
 * Core
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0, or
 * the GNU Lesser General Public License version 3.0 or later.
 * 
 * SPDX-License-Identifier: Apache-2.0 OR LGPL-3.0-or-later
 * 
 * ======================================================================
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ======================================================================
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * =========================LICENSE_END==================================
 */
package org.smooks.engine.bean.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.api.bean.context.BeanContext;
import org.smooks.api.bean.context.BeanIdStore;
import org.smooks.assertion.AssertArgument;
import org.smooks.api.ExecutionContext;
import org.smooks.api.delivery.fragment.Fragment;
import org.smooks.api.bean.lifecycle.BeanContextLifecycleEvent;
import org.smooks.api.bean.lifecycle.BeanContextLifecycleObserver;
import org.smooks.api.bean.lifecycle.BeanLifecycle;
import org.smooks.engine.bean.lifecycle.DefaultBeanContextLifecycleEvent;
import org.smooks.api.bean.repository.BeanId;
import org.smooks.support.MultiLineToStringBuilder;

import java.util.*;
import java.util.Map.Entry;

public class StandaloneBeanContext implements BeanContext {

	private static final Logger LOGGER = LoggerFactory.getLogger(StandaloneBeanContext.class);

	private final ExecutionContext executionContext;

	private final Map<String, Object> beanMap;

	private final ArrayList<ContextEntry> entries;

	private final BeanIdStore beanIdStore;

	private BeanContextMapAdapter repositoryBeanMapAdapter = new BeanContextMapAdapter();

	private List<BeanContextLifecycleObserver> lifecycleObservers = new ArrayList<BeanContextLifecycleObserver>();
	private List<BeanContextLifecycleObserver> addObserversQueue = new ArrayList<BeanContextLifecycleObserver>();
	private List<BeanContextLifecycleObserver> removeObserversQueue = new ArrayList<BeanContextLifecycleObserver>();
	private List<BeanContextLifecycleEvent> notifyObserverEventQueue = new ArrayList<BeanContextLifecycleEvent>();

	/**
	 * Create the StandAloneBeanContext
	 *
	 * @param executionContext
	 *            The {@link ExecutionContext} to which this object is bound to.
	 * @param beanIdStore
	 *            The {@link BeanIdStore} to which this object is bound to.
	 * @param beanMap
	 *            The {@link Map} in which the bean's will be set. It is
	 *            important not to modify this map outside of the
	 *            BeanRepository! It is only provided as constructor parameter
	 *            because in some situations we need to control which
	 *            {@link Map} is used.
	 */
	public StandaloneBeanContext(ExecutionContext executionContext,
			BeanIdStore beanIdStore, Map<String, Object> beanMap) {
		this.executionContext = executionContext;
		this.beanIdStore = beanIdStore;
		this.beanMap = beanMap;

		entries = new ArrayList<ContextEntry>(beanIdStore.size());

		updateBeanMap();
	}

    private StandaloneBeanContext(ExecutionContext executionContext, StandaloneBeanContext parentContext) {
        this.executionContext = executionContext;
        this.beanIdStore = parentContext.beanIdStore;
        this.beanMap = parentContext.beanMap;
        this.entries = parentContext.entries;
        this.repositoryBeanMapAdapter = parentContext.repositoryBeanMapAdapter;
        this.lifecycleObservers = parentContext.lifecycleObservers;
        this.addObserversQueue = parentContext.addObserversQueue;
        this.removeObserversQueue = parentContext.removeObserversQueue;
        this.notifyObserverEventQueue = parentContext.notifyObserverEventQueue;
    }

    public void addBean(BeanId beanId, Object bean) {
        addBean(beanId, bean, null);
    }

	public void addBean(BeanId beanId, Object bean, Fragment source) {
		AssertArgument.isNotNull(beanId, "beanId");
		AssertArgument.isNotNull(bean, "bean");

		// If there's already an instance of this bean, notify observers of it's
		// removal (removal by being overwritten)...
		Object currentInstance = getBean(beanId);
		if (currentInstance != null) {
			notifyObservers(new DefaultBeanContextLifecycleEvent(executionContext,
					source, BeanLifecycle.REMOVE, beanId, currentInstance));
		}

		// Check if the BeanIdList has new BeanIds and if so then
		// add those new entries to the Map. This ensures we always
		// have an up to date Map.
		checkUpdatedBeanIdList();

		int index = beanId.getIndex();
		ContextEntry repoEntry = entries.get(index);

		clean(index);
		repoEntry.setValue(bean);

		// Add the bean to the context...
		notifyObservers(new DefaultBeanContextLifecycleEvent(executionContext, source,
				BeanLifecycle.ADD, beanId, bean));
	}

    public void addBean(String beanId, Object bean) {
        AssertArgument.isNotNull(beanId, "beanId");

        addBean(getBeanId(beanId), bean, null);
    }

	public void addBean(String beanId, Object bean, Fragment source) {
		AssertArgument.isNotNull(beanId, "beanId");

		addBean(getBeanId(beanId), bean, source);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.smooks.engine.javabean.context.BeanContext#containsBean(org.smooks.engine.javabean
	 * .repository.BeanId)
	 */
	public boolean containsBean(BeanId beanId) {
		AssertArgument.isNotNull(beanId, "beanId");

		int index = beanId.getIndex();

		return entries.size() > index && entries.get(index).getValue() != null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.smooks.engine.javabean.context.BeanContext#getBeanId(java.lang.String)
	 */
	public BeanId getBeanId(String beanId) {
		AssertArgument.isNotNull(beanId, "beanId");
		BeanId beanIdObj = beanIdStore.getBeanId(beanId);

		if (beanIdObj == null) {
			beanIdObj = beanIdStore.register(beanId);
		}

		return beanIdObj;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.smooks.engine.javabean.context.BeanContext#getBean(org.smooks.engine.javabean.repository
	 * .BeanId)
	 */
	public Object getBean(BeanId beanId) {
		AssertArgument.isNotNull(beanId, "beanId");

		int index = beanId.getIndex();

		if (entries.size() <= index) {
			return null;
		}

		return entries.get(index).getValue();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.smooks.engine.javabean.context.BeanContext#getBean(java.lang.String)
	 */
	public Object getBean(String beanId) {
		return beanMap.get(beanId);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.smooks.engine.javabean.context.BeanContext#getBean(java.lang.Class)
	 */
	public <T> T getBean(Class<T> beanType) {
		return getBean(beanType, beanMap);
	}

	public static <T> T getBean(Class<T> beanType, Map<String, Object> beanMap) {
		if (beanMap == null) {
			return null;
		}

		for (Object bean : beanMap.values()) {
			if (beanType.isInstance(bean)) {
				return beanType.cast(bean);
			}
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.smooks.engine.javabean.context.BeanContext#changeBean(org.smooks.engine.javabean.
	 * repository.BeanId, java.lang.Object)
	 */
	public void changeBean(BeanId beanId, Object bean, Fragment source) {
		AssertArgument.isNotNull(beanId, "beanId");
		AssertArgument.isNotNull(bean, "bean");

		int index = beanId.getIndex();

		if (entries.size() > index && entries.get(index).getValue() != null) {
			entries.get(index).setValue(bean);

			notifyObservers(new DefaultBeanContextLifecycleEvent(executionContext,
					source, BeanLifecycle.CHANGE, beanId, bean));
		} else {
			throw new IllegalStateException("The bean '" + beanId
					+ "' can't be changed because it isn't in the repository.");
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.smooks.engine.javabean.context.BeanContext#removeBean(org.smooks.engine.javabean.
	 * repository.BeanId)
	 */
	public Object removeBean(BeanId beanId, Fragment source) {
		AssertArgument.isNotNull(beanId, "beanId");

		ContextEntry repositoryEntry = entries.get(beanId.getIndex());
		Object old = repositoryEntry.getValue();

		repositoryEntry.clean();
		repositoryEntry.setValue(null);

		notifyObservers(new DefaultBeanContextLifecycleEvent(executionContext, source,
				BeanLifecycle.REMOVE, beanId, getBean(beanId)));

		return old;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.smooks.engine.javabean.context.BeanContext#removeBean(java.lang.String)
	 */
	public Object removeBean(String beanId, Fragment source) {
		BeanId beanIDObj = getBeanId(beanId);

		if (beanIDObj != null) {
			return removeBean(beanIDObj, source);
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.smooks.engine.javabean.context.BeanContext#clear()
	 */
	public void clear() {

		for (ContextEntry entry : entries) {
			entry.setValue(null);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.smooks.engine.javabean.context.BeanContext#getBeanMap()
	 */
	public Map<String, Object> getBeanMap() {
		return repositoryBeanMapAdapter;
	}

	/**
	 * Checks if the repository is still in sync with then {@link BeanIdStore}.
	 */
	private void checkUpdatedBeanIdList() {

		// We only check if the size is difference because it
		// is not possible to remove BeanIds from the BeanIdList
		if (entries.size() != beanIdStore.size()) {

			updateBeanMap();

		}
	}

	/**
	 * Sync's the BeanRepositories bean map with the bean map from the
	 * {@link BeanIdStore}. All missing keys that are in the BeanIdList's map
	 * are added to the BeanRepositories map.
	 */
	private void updateBeanMap() {

		Map<String, BeanId> beanIdMap = beanIdStore.getBeanIdMap();

		int largestBeanIdIndex = -1;
		for (Entry<String, BeanId> beanIdEntry : beanIdMap.entrySet()) {
			String beanIdName = beanIdEntry.getKey();
			BeanId beanId = beanIdEntry.getValue();
			if (!beanMap.containsKey(beanIdName)) {
				beanMap.put(beanIdName, null);
			}
			if (largestBeanIdIndex < beanId.getIndex()) {
				largestBeanIdIndex = beanId.getIndex();
			}
		}
		if(largestBeanIdIndex >= 0) {
			int newEntries = (largestBeanIdIndex - entries.size()) + 1;
			entries.addAll(Collections.nCopies(newEntries, null));

			for (Entry<String, Object> beanMapEntry : beanMap.entrySet()) {

				BeanId beanId = beanIdMap.get(beanMapEntry.getKey());

				int index = beanId.getIndex();
				if (entries.get(index) == null) {

					entries.set(index, new ContextEntry(beanId, beanMapEntry));
				}
			}
		}
	}

	/**
	 * Remove all bean instances of the associating BeanId's of the parent bean
	 * id. The integer index is directly used for performance reasons.
	 *
	 * @param beanId
	 *            The index of the parent BeanId.
	 */
	private void clean(int beanId) {
		ContextEntry entry = entries.get(beanId);
		if (entry != null) {
			entry.clean();
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.smooks.engine.javabean.context.BeanContext#setBeanInContext(org.smooks.engine.javabean
	 * .repository.BeanId, boolean)
	 */
	public void setBeanInContext(BeanId beanId, boolean inContext) {
		ContextEntry repositoryEntry = entries.get(beanId.getIndex());
		if (repositoryEntry != null) {
			repositoryEntry.setBeanInContext(inContext);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	/*
	 * (non-Javadoc)
	 *
	 * @see org.smooks.engine.javabean.context.BeanContext#toString()
	 */
	@Override
	public String toString() {
		return MultiLineToStringBuilder.toString(getBeanMap());
	}

    public BeanContext newSubContext(ExecutionContext executionContext) {
        return new StandaloneBeanContext(executionContext, this);
    }

    /**
	 * Repository Entry
	 * <p/>
	 * Represents an entry of a BeanId and provides an platform of all the
	 * objects that needed for that entry
	 *
	 * @author <a
	 *         href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com
	 *         </a>
	 *
	 */
	private class ContextEntry {

		private final BeanId beanId;

		private final Entry<String, Object> entry;

		private final List<Integer> lifecycleAssociation = new ArrayList<Integer>();

		private boolean cleaning = false;

		private boolean beanInContext = true;

		/**
		 * @param entry
		 */
		public ContextEntry(BeanId beanId, Entry<String, Object> entry) {
			this.beanId = beanId;
			this.entry = entry;
		}

		public Object getValue() {
			return entry.getValue();
		}

		public void setValue(Object value) {
			if (value == null) {
				value = null;
			}
			entry.setValue(value);
		}

		public void clean() {
			clean(false);
		}

		private void clean(boolean nullifyValue) {
			// Clean the repo entry if it's not already cleaning and the bean is
			// not
			// in context...
			if (cleaning || beanInContext) {
				return;
			}

			setCleaning(true);
			try {
				if (lifecycleAssociation.size() > 0) {
					for (Integer associationId : lifecycleAssociation) {
						ContextEntry association = entries.get(associationId);

						association.clean(true);
					}
					lifecycleAssociation.clear();
				}
			} finally {
				if (nullifyValue) {
					setValue(null);
				}
				setCleaning(false);
			}
		}

		/**
		 * Is this repo entry being cleaned.
		 *
		 * @return True if the entry is being cleaned, otherwise false.
		 */
		public boolean isCleaning() {
			return cleaning;
		}

		/**
		 * Mark this repo entry as being cleaned.
		 *
		 * @param cleaning
		 *            True if the entry is being cleaned, otherwise false.
		 */
		public void setCleaning(boolean cleaning) {
			this.cleaning = cleaning;
		}

		public boolean isBeanInContext() {
			return beanInContext;
		}

		public void setBeanInContext(boolean beanInContext) {
			this.beanInContext = beanInContext;
		}

		public String toString() {
			return ContextEntry.class.getSimpleName() + ": Idx ("
					+ beanId.getIndex() + "), Name (" + beanId.getName()
					+ "), Num Associations (" + lifecycleAssociation.size()
					+ ").";
		}
	}

	/**
	 * This Map Adapter enables that the bean context can be used as a normal
	 * map. There are some important side notes:
	 *
	 * <ul>
	 * <li>The write performance of the map isn't as good as the write
	 * performance of the BeanRepository because it needs to find or register
	 * the BeanId every time. The read performance are as good as any normal
	 * Map.</li>
	 * <li>The {@link #entrySet()} method returns an UnmodifiableSet</li>
	 * <li>When a bean gets removed from the BeanRepository then only the value
	 * of the map entry is set to null. This means that null values should be
	 * regarded as deleted beans. That is also why the size() of the bean map
	 * isn't accurate. It also counts the null value entries.
	 * </ul>
	 *
	 * Only use the Map if you absolutely needed it else you should use the
	 * BeanContext.
	 *
	 * @author <a
	 *         href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com
	 *         </a>
	 *
	 */
	private class BeanContextMapAdapter implements Map<String, Object> {

		/*
		 * (non-Javadoc)
		 *
		 * @see java.util.Map#clear()
		 */
		public void clear() {
			StandaloneBeanContext.this.clear();
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.util.Map#containsKey(java.lang.Object)
		 */
		public boolean containsKey(Object key) {
			return beanMap.containsKey(key);
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.util.Map#containsValue(java.lang.Object)
		 */
		public boolean containsValue(Object value) {
			return beanMap.containsValue(value);
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.util.Map#entrySet()
		 */
		public Set<java.util.Map.Entry<String, Object>> entrySet() {
			return Collections.unmodifiableSet(beanMap.entrySet());
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.util.Map#get(java.lang.Object)
		 */
		public Object get(Object key) {
			return beanMap.get(key);
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.util.Map#isEmpty()
		 */
		public boolean isEmpty() {
			return beanMap.isEmpty();
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.util.Map#keySet()
		 */
		public Set<String> keySet() {
			return beanMap.keySet();
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.util.Map#put(java.lang.Object, java.lang.Object)
		 */
		public Object put(String key, Object value) {
			AssertArgument.isNotNull(key, "key");

			BeanId beanId = beanIdStore.getBeanId(key);

			Object old = null;
			if (beanId == null) {
				beanId = beanIdStore.register(key);
			} else {
				old = getBean(beanId);
			}

			addBean(beanId, value, null);

			return old;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.util.Map#putAll(java.util.Map)
		 */
		public void putAll(Map<? extends String, ?> map) {
			AssertArgument.isNotNull(map, "map");

			for (Entry<? extends String, ?> entry : map
					.entrySet()) {

				addBean(entry.getKey(), entry.getValue(), null);

			}
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.util.Map#remove(java.lang.Object)
		 */
		public Object remove(Object key) {
			AssertArgument.isNotNull(key, "key");

			if (!(key instanceof String)) {
				return null;
			}
			BeanId beanId = beanIdStore.getBeanId((String) key);

			return beanId == null ? null : removeBean(beanId, null);
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.util.Map#size()
		 */
		public int size() {
			return beanMap.size();
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.util.Map#values()
		 */
		public Collection<Object> values() {
			return beanMap.values();
		}

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.smooks.engine.javabean.context.BeanContext#addObserver(org.smooks.engine.javabean
	 * .lifecycle.BeanContextLifecycleObserver)
	 */
	public void addObserver(BeanContextLifecycleObserver observer) {
		if (lifecycleObservers != null) {
			lifecycleObservers.add(observer);
		} else {
			// Will be sync'd up during next notify...
			addObserversQueue.add(observer);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.smooks.engine.javabean.context.BeanContext#notifyObservers(org.smooks.engine.javabean
	 * .lifecycle.BeanContextLifecycleEvent)
	 */
	public void notifyObservers(BeanContextLifecycleEvent event) {
		if (lifecycleObservers != null) {
			List<BeanContextLifecycleObserver> localObserverListCopy = lifecycleObservers;

			// Null the global List object reference while we're iterating it...
			lifecycleObservers = null;
			try {
				int observerCount = localObserverListCopy.size();
				for (int i = 0; i < observerCount; i++) {
					localObserverListCopy.get(i).onBeanLifecycleEvent(event);
				}
			} finally {
				// Reinstate the global List ref so it can be used again...
				lifecycleObservers = localObserverListCopy;

				// Synchronize the global observer list... there may be
				// observers queued
				// for addition or removal. This can happen if a request to add
				// or remove
				// an observer was triggered during the above iteration of the
				// localObserverListCopy list...
				syncObserverList();
			}

			// Handle nested events i.e. events triggered during the above
			// iteration of the
			// localObserverListCopy list...
			if (!notifyObserverEventQueue.isEmpty()) {
				List<BeanContextLifecycleEvent> notifyObserverEventQueueCopy = notifyObserverEventQueue;

				// Create a new queue for nested notification events created by
				// these events...
				notifyObserverEventQueue = new ArrayList<BeanContextLifecycleEvent>();

				// Fire the nested events from the notify queue copy...
				for (BeanContextLifecycleEvent nestedEvent : notifyObserverEventQueueCopy) {
					notifyObservers(nestedEvent);
				}
			}
		} else {
			notifyObserverEventQueue.add(event);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.smooks.engine.javabean.context.BeanContext#removeObserver(org.smooks.engine.javabean
	 * .lifecycle.BeanContextLifecycleObserver)
	 */
	public void removeObserver(BeanContextLifecycleObserver observer) {
		if (lifecycleObservers != null) {
			lifecycleObservers.remove(observer);
		} else {
			// Will be sync'd up during next notify...
			removeObserversQueue.add(observer);
		}
	}

	private void syncObserverList() {
		int addObserverCount = addObserversQueue.size();
		if (addObserverCount > 0) {
			for (int i = 0; i < addObserverCount; i++) {
				lifecycleObservers.add(addObserversQueue.get(i));
			}
			addObserversQueue.clear();
		}

		int removeObserverCount = removeObserversQueue.size();
		if (removeObserverCount > 0) {
			for (int i = 0; i < removeObserverCount; i++) {
				lifecycleObservers.remove(removeObserversQueue.get(i));
			}
			removeObserversQueue.clear();
		}
	}
}
