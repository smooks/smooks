// Copyright 2007 Fusionsoft, Inc. All rights reserved.
// Use is subject to license terms.
package org.smooks.annotation;

import java.util.HashMap;
import java.util.Map;

/** The core class for wrapping classes as annotated classes.
 * The annotated class provides access to all declared and inherited
 * annotations from classes and interfaces. Also the annotated class
 * provides wrapping for its methods for gathering all declared and inherited
 * annotations for it from base classes and interfaces.
 *
 * <p> By now only public methods can inherit annotations with the mechanism.
 *
 * Note: This class is a modified version of the original Fusionsoft Annotation
 * library. See: {@link http://www.fusionsoft-online.com/articles-java-annotations.php}
 *
 * @author Vladimir Ovchinnikov
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 * @version 1.0
 */
public class AnnotationManager {
	private static Map<Class<?>, AnnotatedClass> classToAnnotatedMap = new HashMap<Class<?>, AnnotatedClass>();

	/**
	 * @param theClass to wrap.
	 * @return the annotated class wrapping the specified one.
	 */
	public static AnnotatedClass getAnnotatedClass(Class<?> theClass){
		AnnotatedClass annotatedClass = classToAnnotatedMap.get(theClass);
		if (annotatedClass == null){
			annotatedClass = new AnnotatedClassImpl(theClass);
			classToAnnotatedMap.put(theClass, annotatedClass);
		}
		return annotatedClass;
	}
}
