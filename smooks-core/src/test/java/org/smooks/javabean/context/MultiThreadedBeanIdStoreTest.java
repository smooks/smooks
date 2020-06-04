/*-
 * ========================LICENSE_START=================================
 * Smooks Core
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
package org.smooks.javabean.context;

import org.junit.Test;
import org.smooks.container.MockApplicationContext;
import org.smooks.container.MockExecutionContext;
import org.smooks.javabean.repository.BeanId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

public class MultiThreadedBeanIdStoreTest  {

	private static final Logger LOGGER = LoggerFactory.getLogger(MultiThreadedBeanIdStoreTest.class);

	@Test
	public void test_multi_threaded() throws InterruptedException {

		final int parties = 50;

		final AtomicBoolean exceptionsThrown = new AtomicBoolean(false);
		final MockApplicationContext appContext = new MockApplicationContext();
		final CyclicBarrier barrier = new CyclicBarrier(parties);
		final CountDownLatch countDownLatch = new CountDownLatch(parties);

		Runnable runnable = new Runnable() {

			public void run() {

				try {
					MockExecutionContext execContext = new MockExecutionContext();
					execContext.context = appContext;

					BeanContext beanContext = execContext.getBeanContext();

					barrier.await();
					for(int i = 0; i < 1000; i++) {

						Object bean = new Object();

						//log.info(threadName + " Add bean " + beanId);
						BeanId beanId = beanContext.getBeanId("beanId" + i);
						beanContext.addBean(beanId, bean, null);
						beanContext.addBean(beanId, bean, null);

						//log.info(threadName + " Get bean " + beanId);

						Object retrievedBean = beanContext.getBean(beanId);

						assertSame(bean, retrievedBean);

					}
				} catch (Exception e) {
					LOGGER.error("Exception thrown", e);

					exceptionsThrown.set(true);
				} finally {
					countDownLatch.countDown();
				}
			}
		};



		for(int i = 0; i < parties; i++) {
			Thread threads = new Thread(Thread.currentThread().getThreadGroup(), runnable, "BC Test Thread " + i);

			threads.start();
		}

		countDownLatch.await();

		assertFalse("Exceptions where thrown during the multi threaded test. See the log for the stacktraces.", exceptionsThrown.get());
	}

}
