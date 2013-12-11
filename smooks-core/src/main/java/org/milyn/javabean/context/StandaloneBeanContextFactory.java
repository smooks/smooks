package org.milyn.javabean.context;

import org.milyn.container.ExecutionContext;
import org.milyn.javabean.repository.BeanId;
import org.milyn.payload.FilterResult;
import org.milyn.payload.FilterSource;
import org.milyn.payload.JavaResult;
import org.milyn.payload.JavaSource;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import java.util.HashMap;
import java.util.Map;

/**
 * The Bean Context Manager
 * <p/>
 * Creates {@link StandaloneBeanContext} that share the same {@link BeanIdStore}.
 *
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class StandaloneBeanContextFactory  {



	/* (non-Javadoc)
	 * @see org.milyn.javabean.context.BeanContextFactory#createBeanRepository(org.milyn.container.ExecutionContext)
	 */
	public static StandaloneBeanContext create(ExecutionContext executionContext) {
		StandaloneBeanContext beanContext;

		BeanIdStore beanIdStore = executionContext.getContext().getBeanIdStore();
		Map<String, Object> beanMap = createBeanMap(executionContext, beanIdStore);

		beanContext = new StandaloneBeanContext(executionContext, beanIdStore, beanMap);

		return beanContext;
	}


	/**
	 * Returns the BeanMap which must be used by the {@link BeanContext}. If
	 * a JavaResult or a JavaSource is used with the {@link ExecutionContext} then
	 * those are used in the creation of the Bean map.
	 *
	 * Bean's that are already in the JavaResult or JavaSource map are given
	 * a {@link BeanId} in the {@link BeanIdStore}.
	 *
	 * @param executionContext
	 * @param beanIdStore
	 * @return
	 */
	private static Map<String, Object> createBeanMap(ExecutionContext executionContext, BeanIdStore beanIdStore) {
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

		if(beanMap == null) {
			beanMap = new HashMap<String, Object>();
		} else {

			for(String beanId : beanMap.keySet()) {

				if(!beanIdStore.containsBeanId(beanId)) {
					beanIdStore.register(beanId);
				}

	        }

		}
		return beanMap;
	}

}
