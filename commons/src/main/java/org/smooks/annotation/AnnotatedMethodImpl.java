// Copyright 2007 Fusionsoft, Inc. All rights reserved.
// Use is subject to license terms.
package org.smooks.annotation;

/** The standard implementation for the annotated method.
 *
 * Note: This class is a modified version of the original Fusionsoft Annotation
 * library. See: {@link http://www.fusionsoft-online.com/articles-java-annotations.php}
 *
 * @author Vladimir Ovchinnikov
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 * @version 1.1
 */
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

class AnnotatedMethodImpl implements AnnotatedMethod {

	private final AnnotatedClass annotatedClass;

	private final Method method;

	private Map<Class<?>, Annotation> classToAnnotationMap;

	private Annotation[] annotations;

	private Annotation[][] parameterAnnotations;

	AnnotatedMethodImpl(final AnnotatedClass annotatedClass, final Method method) {
		super();
		this.annotatedClass = annotatedClass;
		this.method = method;
	}

	private Map<Class<?>, Annotation> getAllAnnotationMap(){
		if (classToAnnotationMap == null) {
			classToAnnotationMap = getAllAnnotationMapCalculated();
		}
		return classToAnnotationMap;
	}

	private Map<Class<?>, Annotation> getAllAnnotationMapCalculated(){
		final HashMap<Class<?>, Annotation> result = new HashMap<Class<?>, Annotation>();

		final Class<?> superClass = getAnnotatedClass().getTheClass().getSuperclass();
		// Get the superclass's overriden method annotations
		if (superClass != null) {
			fillAnnotationsForOneMethod(result,
					AnnotationManager.getAnnotatedClass(
							superClass)
							.getAnnotatedMethod(getMethod().getName(),
									getMethod().getParameterTypes()));
		}

		// Get the superinterfaces' overriden method annotations
		for (final Class<?> c : getAnnotatedClass().getTheClass().getInterfaces()){
			fillAnnotationsForOneMethod(result,
					AnnotationManager.getAnnotatedClass(c)
					.getAnnotatedMethod(getMethod().getName(),
							getMethod().getParameterTypes()));
		}

		// Get its own annotations. They have preference to inherited annotations.
		for (final Annotation annotation : getMethod().getDeclaredAnnotations()) {
			result.put(annotation.getClass().getInterfaces()[0], annotation);
		}

		return result;
	}

	/**
	 * @param result is the map of classes to annotations to fill
	 * @param annotatedMethod the method to get annotations. Does nothing
	 * if the annotated method is null.
	 */
	private void fillAnnotationsForOneMethod(final HashMap<Class<?>, Annotation> result,
			final AnnotatedMethod annotatedMethod) {

		if (annotatedMethod == null) {
			return;
		}

		addAnnotations(result, annotatedMethod.getAllAnnotations());
	}

	/**
	 * @param result map of classes to annotations
	 * @param annotations to add to the result
	 */
	private void addAnnotations(final Map<Class<?>, Annotation> result,
			final Annotation[] annotations) {

		for (final Annotation annotation : annotations) {
			if (annotation == null) {
				continue;
			}

			result.put(annotation.getClass().getInterfaces()[0], annotation); /*It means to take the last annotation*/

//			if (result.containsKey(annotation.getClass().getInterfaces()[0]))
//				result.put(annotation.getClass().getInterfaces()[0],
//						null /*it means not to take the annotation at all*/);
//			else
//				result.put(annotation.getClass().getInterfaces()[0], annotation);

		}
	}

	@Override
	public Annotation[] getAllAnnotations() {
		if (annotations == null) {
			annotations = getAllAnnotationsCalculated();
		}
		return annotations;
	}

	private Annotation[] getAllAnnotationsCalculated() {
		final Collection<Annotation> values = getAllAnnotationMap().values();
		return values.toArray(new Annotation[0]);
	}


	@Override
	public AnnotatedClass getAnnotatedClass() {
		return annotatedClass;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Annotation> T getAnnotation(final Class<T> annotationClass) {
		return (T) getAllAnnotationMap().get(annotationClass);
	}

	@Override
	public Method getMethod() {
		return method;
	}

	/*
	 * (non-Javadoc)
	 * @see com.fusionsoft.annotation.AnnotatedMethod#isAnnotationPresent(java.lang.Class)
	 */
	@Override
	public boolean isAnnotationPresent(
			final Class<? extends Annotation> annotationClass) {

		return getAnnotation(annotationClass) != null;
	}

	/**
	 * @param result is the map of classes to annotations to fill
	 * @param annotatedMethod the method to get annotations. Does nothing
	 * if the annotated method is null.
	 */
	@SuppressWarnings("unused")
	private void fillAnnotationsForParameters(final Map<Class<?>, Annotation>[] result,
			final Annotation[][] paramAnnotations) {

		for (int i = 0; i < paramAnnotations.length; i++) {
			final Annotation[] annontations = paramAnnotations[i];

			Map<Class<?>, Annotation> map = result[i];
			if (map == null) {
				map = new HashMap<>();
				result[i] = map;
			}

			addAnnotations(map, annontations);
		}
	}


	/**
	 * Process the parameter annotations
	 *
	 * @return
	 */
	private Annotation[][] getParameterAnnotationsCalculated(){

		@SuppressWarnings("unchecked")
		final Map<Class<?>, Annotation>[] mapResult = new Map[method.getParameterAnnotations().length];

		final Class<?> superClass = getAnnotatedClass().getTheClass().getSuperclass();
		if (superClass != null) {

			final AnnotatedClass aClass = AnnotationManager.getAnnotatedClass(superClass);
			final AnnotatedMethod aMethod = aClass.getAnnotatedMethod(getMethod().getName(), getMethod().getParameterTypes());

			if(aMethod != null) {
				final Annotation[][] paramAnnotations = aMethod.getParameterAnnotations();

				fillAnnotationsForParameters(mapResult, paramAnnotations);
			}

		}

		// Get the superinterfaces' overriden method annotations
		for (final Class<?> c : getAnnotatedClass().getTheClass().getInterfaces()){

			final AnnotatedClass aClass = AnnotationManager.getAnnotatedClass(c);
			final AnnotatedMethod aMethod = aClass.getAnnotatedMethod(getMethod().getName(), getMethod().getParameterTypes());

			if(aMethod != null) {
				final Annotation[][] paramAnnotations = aMethod.getParameterAnnotations();

				fillAnnotationsForParameters(mapResult, paramAnnotations);
			}
		}

		// Get its own annotations. They have preference to inherited annotations.
		fillAnnotationsForParameters(mapResult, method.getParameterAnnotations());

		final Annotation[][] result = new Annotation[mapResult.length][];
		final Annotation[] arrayTemplate = new Annotation[0];
		for(int i = 0; i < mapResult.length; i++) {

			result[i] = mapResult[i].values().toArray(arrayTemplate);

		}

		return result;
	}

	/* (non-Javadoc)
	 * @see com.fusionsoft.annotation.AnnotatedMethod#getParameterAnnotations()
	 */
	public Annotation[][] getParameterAnnotations() {
		if(parameterAnnotations == null) {
			parameterAnnotations = getParameterAnnotationsCalculated();
		}
		return parameterAnnotations;
	}



}
