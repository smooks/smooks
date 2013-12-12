// Copyright 2007 Fusionsoft, Inc. All rights reserved.
// Use is subject to license terms.
package org.milyn.commons.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/** The standard implementation for the annotated class.
 *
 * Note: This class is a modified version of the original Fusionsoft Annotation
 * library. See: {@link http://www.fusionsoft-online.com/articles-java-annotations.php}
 *
 * @author Vladimir Ovchinnikov
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 * @version 1.0
 */
class AnnotatedClassImpl implements AnnotatedClass {
	private final Class<?> theClass;
	private Map<Class<?>, Annotation> classToAnnotationMap = null;
	private Map<Method, AnnotatedMethod> methodToAnnotatedMap = null;
	private Annotation[] annotations = null;
	private AnnotatedMethod[] annotatedMethods = null;

	AnnotatedClassImpl (final Class<?> theClass){
		super();
		this.theClass = theClass;
	}

	/**
	 * @return the cached map of classes to annotations
	 */
	private Map<Class<?>, Annotation> getAllAnnotationMap(){
		if (classToAnnotationMap == null) {
			classToAnnotationMap = getAllAnnotationMapCalculated();
		}
		return classToAnnotationMap;
	}

	/**
	 * @return the calculated map of classes to annotations
	 */
	private Map<Class<?>, Annotation> getAllAnnotationMapCalculated(){
		final HashMap<Class<?>, Annotation> result = new HashMap<Class<?>, Annotation>();

		final Class<?> superClass = getTheClass().getSuperclass();
		// Get the superclass's annotations
		if (superClass != null) {
			fillAnnotationsForOneClass(result, superClass);
		}

		// Get the superinterfaces' annotations
		for (final Class<?> c : getTheClass().getInterfaces()) {
			fillAnnotationsForOneClass(result, c);
		}

		// Get its own annotations. They have preferece to inherited annotations.
		for (final Annotation annotation : getTheClass().getDeclaredAnnotations()) {
			result.put(annotation.getClass().getInterfaces()[0], annotation);
		}

		return result;
	}

	/**
	 * @param result map of classes to annotations
	 * @param baseClass is the superclass or one of the superinterfaces.
	 */
	private void fillAnnotationsForOneClass(final HashMap<Class<?>, Annotation> result,
			final Class<?> baseClass) {

		addAnnotations(result, AnnotationManager.getAnnotatedClass(baseClass).getAllAnnotations());
	}

	/**
	 * @param result map of classes to annotations
	 * @param annotations to add to the result
	 */
	private void addAnnotations(final HashMap<Class<?>, Annotation> result,
			final Annotation[] annotations) {

		for (final Annotation annotation : annotations){

			if (annotation != null) {

				if (result.containsKey(annotation.getClass().getInterfaces()[0])) {
					result.put(annotation.getClass().getInterfaces()[0], null /*it means not to take the annotation at all*/);
				} else {
					result.put(annotation.getClass().getInterfaces()[0], annotation);
				}

			}

		}
	}

	public Class<?> getTheClass() {
		return theClass;
	}

	public Annotation[] getAllAnnotations() {
		if (annotations == null) {
			annotations = getAllAnnotationsCalculated();
		}
		return annotations;
	}

	private Annotation[] getAllAnnotationsCalculated() {
		return getAllAnnotationMap().values().toArray(new Annotation[0]);
	}

	@SuppressWarnings("unchecked")
	public <T extends Annotation> T getAnnotation(final Class<T> annotationClass) {
		return (T) getAllAnnotationMap().get(annotationClass);
	}

	private Map<Method, AnnotatedMethod> getMethodMap(){
		if (methodToAnnotatedMap == null) {
			methodToAnnotatedMap = getMethodMapCalculated();
		}
		return methodToAnnotatedMap;
	}

	private Map<Method, AnnotatedMethod> getMethodMapCalculated(){
		final HashMap<Method, AnnotatedMethod> result = new HashMap<Method, AnnotatedMethod>();

		for (final Method method : getTheClass().getMethods()) {
			result.put(method, new AnnotatedMethodImpl(this, method));
		}

		return result;
	}

	public AnnotatedMethod getAnnotatedMethod(final Method method) {
		return getMethodMap().get(method);
	}

	public AnnotatedMethod[] getAnnotatedMethods() {
		if (annotatedMethods == null) {
			annotatedMethods = getAnnotatedMethodsCalculated();
		}
		return annotatedMethods;
	}

	private AnnotatedMethod[] getAnnotatedMethodsCalculated() {
		final Collection<AnnotatedMethod> values = getMethodMap().values();
		return values.toArray(new AnnotatedMethod[0]);
	}


	public AnnotatedMethod getAnnotatedMethod(final String name, final Class<?>[] parameterType) {
		try {
			return getAnnotatedMethod(getTheClass().getMethod(name, parameterType));
		} catch (final SecurityException e) {
			throw new RuntimeException(e);
		} catch (final NoSuchMethodException e) {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see com.fusionsoft.annotation.AnnotatedClass#isAnnotationPresent(java.lang.Class)
	 */
	public boolean isAnnotationPresent(
			final Class<? extends Annotation> annotationClass) {

		return getAnnotation(annotationClass) != null;
	}
}
