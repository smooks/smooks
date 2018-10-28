package org.milyn.javabean.context;

import java.util.Map;

import org.milyn.container.ExecutionContext;
import org.milyn.delivery.Fragment;
import org.milyn.javabean.lifecycle.BeanContextLifecycleEvent;
import org.milyn.javabean.lifecycle.BeanContextLifecycleObserver;
import org.milyn.javabean.lifecycle.BeanLifecycle;
import org.milyn.javabean.repository.BeanId;

import javax.xml.namespace.QName;

/**
 * Bean Context
 * <p/>
 * This class represents a context of bean's and the means to get and
 * set there instances.
 * <p/>
 * This class uses a {@link BeanIdStore} to optimize the access performance. If
 * all the {@link BeanId} objects are registered with the BeanIdStore before this object
 * is created then you get 'direct access' performance.
 * <p/>
 * For performance reasons it is best to register all BeanId objects up front. Because
 * if new BeanId objects are registered after the BeanContext is created then the BeanContext
 * needs to do  synchronize with the BeanIdStore,
 * <p/>
 * It is possible to get the bean by it's bean id String name. However this isn't as
 * fast as using the BeanId objects.
 *
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public interface BeanContext {

	/**
	 * Add a bean instance under the specified {@link BeanId}.
	 *
	 * @param beanId The {@link org.milyn.javabean.repository.BeanId} under which the bean is to be stored.
     * @param bean The bean instance to be stored.
     */
	public abstract void addBean(BeanId beanId, Object bean);

    /**
     * Add a bean instance under the specified beanId string.
     * <p/>
     * Generates a {@link BeanId} in the background.
     *
     * @param beanId The {@link org.milyn.javabean.repository.BeanId} under which the bean is to be stored.
     * @param bean The bean instance to be stored.
     * @param source Source fragment.
     */
    public abstract void addBean(BeanId beanId, Object bean, Fragment source);

	/**
	 * Add a bean instance under the specified beanId.
	 * <p/>
	 * If performance is important, you should get (and cache) a {@link BeanId} instance
	 * for the beanId String and then use the {@link #addBean(BeanId, Object, Fragment)} method.
	 *
	 * @param beanId The beanId under which the bean is to be stored.
     * @param bean The bean instance to be stored.
     */
	public abstract void addBean(String beanId, Object bean);

    /**
     * Add a bean instance under the specified beanId.
     * <p/>
     * If performance is important, you should get (and cache) a {@link BeanId} instance
     * for the beanId String and then use the {@link #addBean(BeanId, Object, Fragment)} method.
     *
     * @param beanId The beanId under which the bean is to be stored.
     * @param bean The bean instance to be stored.
     * @param source Source fragment.
     */
    public abstract void addBean(String beanId, Object bean, Fragment source);

	/**
	 * Get the {@link BeanId} instance for the specified beanId String.
	 * <p/>
	 * Regsiters the beanId if it's not already registered.
	 *
	 * @param beanId The beanId String.
	 * @return The associated {@link BeanId} instance.
	 */
	public abstract BeanId getBeanId(String beanId);

	/**
	 * Looks if a bean instance is set under the {@link BeanId}
	 *
	 * @param beanId The {@link BeanId} under which is looked.
	 */
	public abstract boolean containsBean(BeanId beanId);

	/**
	 * Get the current bean, specified by the supplied {@link BeanId}.
	 * <p/>
	 * @param beanId The {@link BeanId} of the bean to be returned.
	 * @return The bean instance, otherwise <code>null</code>.
	 */
	public abstract Object getBean(BeanId beanId);

	/**
	 * Returns the bean by it's beanId name.
	 * <p/>
	 * Returns the first bean of the specified type from the BeanContext instance.
	 *
	 * @param beanId The type of the bean to be returned.
	 * @return The bean instance, otherwise <code>null</code>.
	 */
	public abstract Object getBean(String beanId);

	/**
	 * Returns the bean by it's beanId name.
	 * <p/>
	 * Returns the first bean of the specified type from the BeanContext instance.
	 *
	 * @param beanType The type of the bean to be returned.
	 * @return The bean instance, otherwise <code>null</code>.
	 */
	public abstract <T> T getBean(Class<T> beanType);

	/**
	 * Changes a bean instance of the given {@link BeanId}. The difference to {@link #addBean(BeanId, Object)}
	 * is that the bean must exist, the associated beans aren't removed and the observers of the
	 * {@link BeanLifecycle#CHANGE} event are notified.
	 *
	 * @param beanId The {@link org.milyn.javabean.repository.BeanId} under which the bean instance is to be stored.
     * @param bean The bean instance to be stored.
     * @param source Source fragment.
     */
	public abstract void changeBean(BeanId beanId, Object bean, Fragment source);

	/**
	 * Removes a bean and all its associated lifecycle beans from the bean map
	 *
	 * @param beanId The beanId to remove the beans from.
     * @param source Source fragment.
     */
	public abstract Object removeBean(BeanId beanId, Fragment source);

	/**
	 * Removes a bean and all its associated lifecycle beans from the bean map
	 *
	 * @param beanId The beanId to remove the beans from.
     * @param source Source fragment.
     */
	public abstract Object removeBean(String beanId, Fragment source);

	/**
	 * Removes all the beans from the bean map
	 */
	public abstract void clear();

	/**
	 * Registers a bean context observer.
	 *
	 * @param observer The actual BeanObserver instance.
	 */
	public abstract void addObserver(BeanContextLifecycleObserver observer);

	/**
	 * Notify all observers of a specific bean lifecycle event.
	 *
	 * @param event The event.
	 */
	public abstract void notifyObservers(BeanContextLifecycleEvent event);

	/**
	 * Unregisters a bean observer.
	 *
	 * @param observer The actual BeanObserver instance.
	 */
	public abstract void removeObserver(BeanContextLifecycleObserver observer);

	/**
	 * This returns a map which is backed by this repository. Changes made in the map
	 * are reflected back into the repository.
	 * There are some important side notes:
	 *
	 * <ul>
	 *   <li> The write performance of the map isn't as good as the write performance of the
	 *     	  BeanRepository because it needs to find or register the BeanId every time.
	 *        The read performance are as good as any normal Map.</li>
	 *   <li> The entrySet() method returns an UnmodifiableSet </li>
	 *   <li> When a bean gets removed from the BeanRepository then only the value of the
	 *        map entry is set to null. This means that null values should be regarded as
	 *        deleted beans. That is also why the size() of the bean map isn't accurate. It
	 *        also counts the null value entries.
	 * </ul>
	 *
	 * Only use the Map if you absolutely needed it else you should use the BeanRepository.
	 */
	public abstract Map<String, Object> getBeanMap();

	/**
	 * Mark the bean as being in context.
	 * <p/>
	 * This is "set" when we enter the fragment around which the bean is created and unset
	 * when we exit.
	 *
	 * @param beanId The bean ID.
	 * @param inContext True if the bean is in context, otherwise false.
	 */
	public abstract void setBeanInContext(BeanId beanId, boolean inContext);

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public abstract String toString();

    /**
     * Create a sub-{@link BeanContext} of this {@link BeanContext}, associated
     * with the supplied {@link org.milyn.container.ExecutionContext}.
     * @param executionContext The Associated {@link org.milyn.container.ExecutionContext}.
     * @return The new sub-{@link BeanContext}.
     */
    BeanContext newSubContext(ExecutionContext executionContext);
}
