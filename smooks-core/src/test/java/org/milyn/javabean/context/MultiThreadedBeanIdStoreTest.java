package org.milyn.javabean.context;

import org.junit.Test;
import org.milyn.container.MockApplicationContext;
import org.milyn.container.MockExecutionContext;
import org.milyn.javabean.repository.BeanId;
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
