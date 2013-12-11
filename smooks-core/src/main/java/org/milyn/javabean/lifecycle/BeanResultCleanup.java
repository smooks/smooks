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
package org.milyn.javabean.lifecycle;

import org.milyn.cdr.annotation.ConfigParam;
import org.milyn.commons.util.CollectionsUtil;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.ExecutionLifecycleCleanable;
import org.milyn.delivery.annotation.Initialize;
import org.milyn.javabean.context.BeanContext;

import java.util.Map.Entry;
import java.util.Set;

/**
 * Bean Result Cleanup resource.
 * <p/>
 * Execution Lifecycle Cleanable resource that performs Java result cleaup.
 *
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class BeanResultCleanup implements ExecutionLifecycleCleanable {

    @ConfigParam
    private String[] beanIDs;
    private Set<String> beanIDSet;

    @Initialize
    public void initialize() {
        beanIDSet = CollectionsUtil.toSet(beanIDs);
    }

    /**
     * Execute the cleanup.
     *
     * @param executionContext The execution context.
     */
    public void executeExecutionLifecycleCleanup(ExecutionContext executionContext) {
        BeanContext beanContext = executionContext.getBeanContext();
        Set<Entry<String, Object>> beanSet = beanContext.getBeanMap().entrySet();

        for (Entry<String, Object> beanEntry : beanSet) {
            String beanID = beanEntry.getKey();
            if (!beanIDSet.contains(beanID)) {
                beanContext.removeBean(beanID, null);
            }
        }
    }
}
