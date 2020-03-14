// Copyright 2007 Fusionsoft, Inc. All rights reserved.
package org.smooks.annotation;

import static org.junit.Assert.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class TestAnnotation  {
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	static public @interface A {}

	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	static public @interface B {}

	@A
	static public interface BaseInterface {
		@B
		public void method1();
	}

	static public class BaseClass {
		@B
		public void method2(){}
	}

	static public class Derived extends BaseClass implements BaseInterface{
		public void method1(){}
		@Override
		public void method2(){}
	}

	/**
	 * Checks that annotation on the method "method" is inherited.
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	public void test_MethodAnnotationInheritance() throws SecurityException, NoSuchMethodException{
		final AnnotatedClass annotatedClass =
			AnnotationManager.getAnnotatedClass(Derived.class);
		assertNotNull(annotatedClass.getAnnotation(A.class));

		AnnotatedMethod annotatedMethod = annotatedClass
		.getAnnotatedMethod("method1", new Class[0]);
		assertNotNull(annotatedMethod.getAnnotation(B.class));

		annotatedMethod = annotatedClass
		.getAnnotatedMethod("method2", new Class[0]);
		assertNotNull(annotatedMethod.getAnnotation(B.class));

		assertNull(Derived.class.getMethod(
				"method1", new Class[0]).getAnnotation(B.class));
		assertNull(Derived.class.getMethod(
				"method2", new Class[0]).getAnnotation(B.class));
	}
}
