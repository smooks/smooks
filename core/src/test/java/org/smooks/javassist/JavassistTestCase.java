/*-
 * ========================LICENSE_START=================================
 * Core
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0, or
 * the GNU Lesser General Public License version 3.0 or later.
 * 
 * SPDX-License-Identifier: Apache-2.0 OR LGPL-3.0-or-later
 * 
 * ======================================================================
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ======================================================================
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * =========================LICENSE_END==================================
 */
package org.smooks.javassist;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import org.mvel2.MVEL;

import javassist.*;

/**
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class JavassistTestCase {

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
