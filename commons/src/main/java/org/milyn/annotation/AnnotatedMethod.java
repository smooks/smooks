// Copyright 2007 Fusionsoft, Inc. All rights reserved.
// Use is subject to license terms.
package org.milyn.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/** The annotated method is a wrapping for some method
 * providing inheritance of all annotations of the method
 * being overridden by this one. If the same method has
 * different annotations in different superclasses or superinterface,
 * the last annotation met is taken. So you better maintain the same
 * annotations in this case.
 *
 * Note: This class is a modified version of the original Fusionsoft Annotation
 * library. See: {@link http://www.fusionsoft-online.com/articles-java-annotations.php}
 *
 * @author Vladimir Ovchinnikov
 * @version 1.0
 */
public interface AnnotatedMethod {
	/**
	 * @return the annotated class where the method is declared.
	 */
	AnnotatedClass getAnnotatedClass();

	/**
	 * @return the method wrapped by the annotated method.
	 */
	Method getMethod();

	/**
	 * Returns true if an annotation for the specified type is present on this element, else false.
	 *
	 * @param annotationClass the Class object corresponding to the annotation type
	 * @return true if an annotation for the specified annotation type is present on this element, else false
	 */
	boolean isAnnotationPresent(Class<? extends Annotation> annotationClass);


	/**
	 * @return all inherited and declared annotations of the method.
	 */
	Annotation[] getAllAnnotations();

	/**
	 * @param annotationClass of the annotation to find.
	 * @return the inherited or declared annotation of the specified class.
	 */

	<T extends Annotation> T getAnnotation(Class<T> annotationClass);

	/**
	 * Returns an array of arrays that represent the annotations on the formal parameters,
	 * in declaration order, of the method represented by this Method object.
	 * @return
	 */
	Annotation[][] getParameterAnnotations();
}
