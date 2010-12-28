package org.milyn.javabean.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Result;
import javax.xml.transform.Source;

import org.milyn.assertion.AssertArgument;
import org.milyn.container.ExecutionContext;
import org.milyn.javabean.lifecycle.BeanLifecycle;
import org.milyn.payload.FilterResult;
import org.milyn.payload.FilterSource;
import org.milyn.payload.JavaResult;
import org.milyn.payload.JavaSource;

@SuppressWarnings("deprecation")
public class OldBeanAccessor {
	private static final String CONTEXT_KEY = OldBeanAccessor.class.getName() + "#CONTEXT_KEY";

    private final ExecutionContext executionContext;

    private final Map<String, Object> beans;

    private final Map<String, List<String>> lifecycleAssociations = new HashMap<String, List<String>>();

    /**
     * Public default constructor.
     */
    public OldBeanAccessor(ExecutionContext executionContext) {
    	this(executionContext, new LinkedHashMap<String, Object>());
    }

    /**
     * Public constructor.
     * <p/>
     * Creates an accessor based on the supplied result Map.
     *
     * @param resultMap The result Map.
     */
    public OldBeanAccessor(ExecutionContext executionContext, Map<String, Object> resultMap) {
    	this.executionContext = executionContext;
        beans = resultMap;
    }

    /**
     * Get the current bean, specified by the supplied beanId, from the supplied request.
     * <p/>
     * If the specified beanId refers to a bean instance list, this method returns the
     * last (current) bean from the list.
     * @param beanId Bean Identifier.
     * @param executionContext The request on which the bean instance is stored.
     * @return The bean instance, or null if no such bean instance exists on the supplied
     * request.
     * @deprecated use the {@link #getBean(ExecutionContext, String)}
     */
    @Deprecated
    public static Object getBean(String beanId, ExecutionContext executionContext) {
        return getBean(executionContext, beanId);
    }

    /**
     * Get the current bean, specified by the supplied beanId, from the supplied request.
     * <p/>
     * If the specified beanId refers to a bean instance list, this method returns the
     * last (current) bean from the list.
     * @param beanId Bean Identifier.
     * @param executionContext The request on which the bean instance is stored.
     * @return The bean instance, or null if no such bean instance exists on the supplied
     * request.
     */
    public static Object getBean(ExecutionContext executionContext, String beanId) {
        AssertArgument.isNotNullAndNotEmpty(beanId, "beanId");
        AssertArgument.isNotNull(executionContext, "executionContext");

        Map<String, Object> beans = getBeanMap(executionContext);
        Object bean = beans.get(beanId);

        return bean;
    }

    /**
     * Get the bean map associated with the supplied request instance.
     * @param executionContext The execution context.
     * @return The bean map associated with the supplied request.
     * @deprecated Use {@link #getBeanMap(org.milyn.container.ExecutionContext)}.
     */
    @Deprecated
	public static HashMap<String, Object> getBeans(ExecutionContext executionContext) {
        return (HashMap<String, Object>) getBeanMap(executionContext);
    }

    /**
     * Get the bean map associated with the supplied request instance.
     * @param executionContext The execution context.
     * @return The bean map associated with the supplied request.
     */
    public static Map<String, Object> getBeanMap(ExecutionContext executionContext) {
        if(executionContext == null) {
            throw new IllegalArgumentException("null 'request' arg in method call.");
        }

        OldBeanAccessor accessor = getAccessor(executionContext);

        return accessor.beans;
    }

    /**
     * Associates the lifeCycle of the childBean with the parentBean. When the parentBean gets overwritten via the
     * addBean method then the associated child beans will get removed from the bean map.
     *
     * @param executionContext The execution context within which the beans are located.
     * @param parentBean The bean that controlles the lifecycle of its childs
     * @param childBean The bean that will be associated to the parent
     *
     */
    public static void associateLifecycles(ExecutionContext executionContext, String parentBean, String childBean) {
    	AssertArgument.isNotNull(executionContext, "executionContext");
    	AssertArgument.isNotNullAndNotEmpty(parentBean, "parentBean");
    	AssertArgument.isNotNullAndNotEmpty(childBean, "childBean");

    	OldBeanAccessor accessor = getAccessor(executionContext);

    	accessor.associateLifecycles(parentBean, childBean);

    }

    /**
     * Associates the lifeCycle of the childBean with the parentBean. When the parentBean gets overwritten via the
     * addBean method then the associated child beans will get removed from the bean map.
     *
     * @param executionContext The execution context within which the beans are located.
     * @param parentBean The bean that controlles the lifecycle of its childs
     * @param childBean The bean that will be associated to the parent
     * @param addToList Is the child added to a bean list.
     * @deprecated Because of the new bean binding system, adding to a list this way is deprecated.
     * 			   Use the {@link #associateLifecycles(ExecutionContext, String, String)} method.
     */
    @Deprecated
    public static void associateLifecycles(ExecutionContext executionContext, String parentBean, String childBean, boolean addToList) {
    	AssertArgument.isNotNull(executionContext, "executionContext");
    	AssertArgument.isNotNullAndNotEmpty(parentBean, "parentBean");
    	AssertArgument.isNotNullAndNotEmpty(childBean, "childBean");


    	if(addToList) {
            childBean += "List";
        }

        associateLifecycles(executionContext, parentBean, childBean);
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
    	return beans.toString();
    }

    private static OldBeanAccessor getAccessor(ExecutionContext executionContext) {
    	OldBeanAccessor accessor = (OldBeanAccessor) executionContext.getAttribute(CONTEXT_KEY);

        if(accessor == null) {
            Result result = FilterResult.getResult(executionContext, JavaResult.class);
            Source source = FilterSource.getSource(executionContext);
            Map<String, Object> beanMap = null;

            if(result != null) {
                JavaResult javaResult = (JavaResult) result;
                beanMap = javaResult.getResultMap();
            }
            if(source instanceof JavaSource) {
                JavaSource javaSource = (JavaSource) source;
                Map<String, Object> sourceBeans = javaSource.getBeans();

                if(sourceBeans != null) {
                    if(beanMap != null) {
                        beanMap.putAll(sourceBeans);
                    } else {
                        beanMap = sourceBeans;
                    }
                }
            }

            if(beanMap != null) {
                accessor = new OldBeanAccessor(executionContext, beanMap);
            } else {
                accessor = new OldBeanAccessor(executionContext);
            }

            executionContext.setAttribute(CONTEXT_KEY, accessor);
        }

        return accessor;
    }

    private void cleanAssociatedLifecycleBeans(String parentBean) {

    	List<String> associations = lifecycleAssociations.get(parentBean);

        if(associations != null) {
            for (String association : associations) {
            	removeBean(association);
            }
            lifecycleAssociations.remove(parentBean);
        }

    }

    private void removeBean(String beanId) {
    	cleanAssociatedLifecycleBeans(beanId);


    	beans.remove(beanId);
    }

    private void associateLifecycles(String parentBean, String childBean) {
    	AssertArgument.isNotNullAndNotEmpty(parentBean, "parentBean");
    	AssertArgument.isNotNullAndNotEmpty(childBean, "childBean");

    	List<String> associations = lifecycleAssociations.get(parentBean);

        if(associations != null) {
            if(!associations.contains(childBean)) {
                associations.add(childBean);
            }
        } else {
            associations = new ArrayList<String>(1);
            associations.add(childBean);
            lifecycleAssociations.put(parentBean, associations);
        }
    }
}
