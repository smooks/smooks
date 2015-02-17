/*
	Milyn - Copyright (C) 2006 - 2010

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License (version 2.1) as published by the Free Software
	Foundation.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

	See the GNU Lesser General Public License for more details:
	http://www.gnu.org/licenses/lgpl.txt
*/
package org.milyn.javassist;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Set;

import org.mvel2.MVEL;
import org.mvel2.integration.VariableResolver;
import org.mvel2.integration.VariableResolverFactory;

import javassist.*;

/**
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class JavassistTest {

    private static final int INVOKE_COUNT = 1000000;

    @Test
    public void test_reflective() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InterruptedException {
        Method setPropMethod = TestPOJO.class.getMethod("setProp", String.class);
        TestPOJO objInst = new TestPOJO();

        for(int i = 0; i < 10000; i++) {
            setPropMethod.invoke(objInst, "hi");
        }
        assertEquals("hi", objInst.getProp());

        Thread.sleep(1000);

        long start = System.currentTimeMillis();
        for(int i = 0; i < INVOKE_COUNT; i++) {
            setPropMethod.invoke(objInst, "hi");
        }
        System.out.println("Reflective Time: " + (System.currentTimeMillis() - start));
    }

    @Test
    public void test_javassist() throws NotFoundException, CannotCompileException, IllegalAccessException, InstantiationException, InterruptedException {
        JavassistSetter setter = buildSetterClass();
        TestPOJO objInst = new TestPOJO();

        for(int i = 0; i < 10000; i++) {
            setter.set(objInst, "hi");
        }
        assertEquals("hi", objInst.getProp());

        Thread.sleep(1000);

        long start = System.currentTimeMillis();
        for(int i = 0; i < INVOKE_COUNT; i++) {
            setter.set(objInst, "hi");
        }
        System.out.println("Javasist Time: " + (System.currentTimeMillis() - start));
    }

    @Test
    public void test_mvel() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InterruptedException {

    	Serializable compiled = MVEL.compileExpression("testPOJO.setProp('hi')");
        TestPOJO objInst = new TestPOJO();
        HashMap<String, Object> vars = new HashMap<String, Object>();
        vars.put("testPOJO", objInst);

        for(int i = 0; i < 10000; i++) {
            MVEL.executeExpression(compiled, vars);
        }
        assertEquals("hi", objInst.getProp());

        Thread.sleep(1000);

        long start = System.currentTimeMillis();
        for(int i = 0; i < INVOKE_COUNT; i++) {
        	MVEL.executeExpression(compiled, vars);
        }
        System.out.println("MVEL Time: " + (System.currentTimeMillis() - start));
    }

    private JavassistSetter buildSetterClass() throws CannotCompileException, NotFoundException, InstantiationException, IllegalAccessException {
        ClassPool pool = ClassPool.getDefault();
        CtClass cc = pool.makeClass("com.acme.Blah");

        // Create the class...
        cc.setSuperclass(pool.get(JavassistSetter.class.getName()));

        String setMethod = "public void set(Object object, Object value) {\n" +
                "    ((" + TestPOJO.class.getName() + ")object).setProp((String) value);\n" +
                "}";
        cc.addMethod(CtNewMethod.make(setMethod, cc));

        // Create the instance...
        return (JavassistSetter) cc.toClass().newInstance();
    }

}
