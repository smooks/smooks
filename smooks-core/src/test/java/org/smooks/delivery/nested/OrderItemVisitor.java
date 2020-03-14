package org.smooks.delivery.nested;

import org.smooks.SmooksException;
import org.smooks.cdr.annotation.ConfigParam;
import org.smooks.container.ExecutionContext;
import org.smooks.delivery.sax.SAXElement;
import org.smooks.delivery.sax.SAXVisitBefore;

import java.io.IOException;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class OrderItemVisitor implements SAXVisitBefore {

    @ConfigParam
    private String beanId;

    public void visitBefore(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
        Object bean = executionContext.getBeanContext().getBean(beanId);

        if(bean != null) {
            executionContext.getBeanContext().addBean(beanId, bean + "-" + beanId);
        } else {
            executionContext.getBeanContext().addBean(beanId, beanId);
        }
    }
}
