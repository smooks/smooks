package org.milyn.routing.util;

import org.milyn.container.MockExecutionContext;
import org.milyn.javabean.repository.BeanId;
import org.milyn.routing.jms.TestBean;

public final class RouterTestHelper
{
	private RouterTestHelper() {}

	public static MockExecutionContext createExecutionContext(
			final String beanIdName,
			final Object bean)
	{
        final MockExecutionContext executionContext = new MockExecutionContext();

        BeanId beanId = executionContext.getContext().getBeanIdStore().register(beanIdName);
        executionContext.getBeanContext().addBean(beanId, bean, null);
        return executionContext;
	}

	public static TestBean createBean()
	{
		final String name = "Daniel";
		final String address = "Fleminggatan";
		final String phoneNumber = "555-555-5555";

		final TestBean bean = new TestBean();
		bean.setAddress( address );
		bean.setName( name );
		bean.setPhoneNumber( phoneNumber );
		return bean;
	}

}
