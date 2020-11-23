/*-
 * ========================LICENSE_START=================================
 * Smooks Core
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
package org.smooks.lifecycle;

import org.junit.BeforeClass;
import org.junit.Test;
import org.smooks.cdr.MyContentDeliveryUnit5;
import org.smooks.cdr.SmooksConfigurationException;
import org.smooks.cdr.ResourceConfig;
import org.smooks.container.ApplicationContext;
import org.smooks.container.MockApplicationContext;
import org.smooks.delivery.ContentHandler;
import org.smooks.injector.Scope;
import org.smooks.lifecycle.phase.PostConstructLifecyclePhase;
import org.smooks.lifecycle.phase.PreDestroyLifecyclePhase;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.Assert.*;

/**
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class LifecycleManagerTest {

    private static LifecycleManager lifecycleManager;

    @BeforeClass
    public static void beforeClass() {
        lifecycleManager = new DefaultLifecycleManager();
    }
    
	@Test
    public void test_paramaterSetting_allok() {
        ResourceConfig config = new ResourceConfig();
        MyContentDeliveryUnit1 cdu = new MyContentDeliveryUnit1();

        config.setParameter("paramA", "A-Val");
        config.setParameter("param-b", "B-Val");
        config.setParameter("paramC", 8);

        MockApplicationContext mockApplicationContext = new MockApplicationContext();
        lifecycleManager.applyPhase(cdu, new PostConstructLifecyclePhase(new Scope(mockApplicationContext.getRegistry(), config, cdu)));
        assertEquals("A-Val", cdu.paramA);
        assertEquals("B-Val", cdu.paramB);
        assertEquals((Integer) 8, cdu.paramC);
    }

	@Test
    public void test_paramaterSetting_missing_required() {
        ResourceConfig config = new ResourceConfig();
        MyContentDeliveryUnit1 cdu = new MyContentDeliveryUnit1();

        config.setParameter("paramA", "A-Val");
        config.setParameter("param-b", "B-Val");

        // Don't add the required paramC config

        MockApplicationContext mockApplicationContext = new MockApplicationContext();
        try {
            lifecycleManager.applyPhase(cdu, new PostConstructLifecyclePhase(new Scope(mockApplicationContext.getRegistry(), config, cdu)));
            fail(" Expected SmooksConfigurationException");
        } catch(SmooksConfigurationException e) {
            assertTrue(e.getMessage().startsWith("<param> 'paramC' not specified on resource configuration"));
        }
    }

    @Test
    public void test_parameterSetting_optional() {
        ResourceConfig config = new ResourceConfig();
        MyContentDeliveryUnit2 cdu = new MyContentDeliveryUnit2();

        config.setParameter("paramA", "A-Val");
        config.setParameter("param-b", "B-Val");
        config.setParameter("paramD", 8);

        // Don't add the optional paramC config

        MockApplicationContext mockApplicationContext = new MockApplicationContext();
        lifecycleManager.applyPhase(cdu, new PostConstructLifecyclePhase(new Scope(mockApplicationContext.getRegistry(), config, cdu)));
        assertEquals("A-Val", cdu.paramA);
        assertEquals("B-Val", cdu.paramB);
        assertEquals(Optional.empty(), cdu.paramC);
        assertEquals((Integer) 8, cdu.paramD);
    }
    
    @Test
    public void test_parameterSetting_default() {
        ResourceConfig config = new ResourceConfig();
        MyContentDeliveryUnit2 cdu = new MyContentDeliveryUnit2();

        config.setParameter("paramA", "A-Val");
        config.setParameter("param-b", "B-Val");
        config.setParameter("paramC", 8);

        // Don't add the optional paramD config

        MockApplicationContext mockApplicationContext = new MockApplicationContext();
        lifecycleManager.applyPhase(cdu, new PostConstructLifecyclePhase(new Scope(mockApplicationContext.getRegistry(), config, cdu)));
        assertEquals("A-Val", cdu.paramA);
        assertEquals("B-Val", cdu.paramB);
        assertEquals((Integer) 8, cdu.paramC.get());
        assertEquals((Integer) 9, cdu.paramD);
    }

	@Test
    public void test_Config_And_Context_Setting() {
        ResourceConfig config = new ResourceConfig();
        MyContentDeliveryUnit3 cdu = new MyContentDeliveryUnit3();

        MockApplicationContext mockApplicationContext = new MockApplicationContext();
        lifecycleManager.applyPhase(cdu, new PostConstructLifecyclePhase(new Scope(mockApplicationContext.getRegistry(), config, cdu)));
        assertNotNull(cdu.config);
        assertNotNull(cdu.appContext);
    }

	@Test
    public void test_paramaterSetting_Config_setConfiguration_on_private_inner_class() {
        ResourceConfig config = new ResourceConfig();
        MyContentDeliveryUnit4 cdu = new MyContentDeliveryUnit4();

        MockApplicationContext mockApplicationContext = new MockApplicationContext();
        try {
            lifecycleManager.applyPhase(cdu, new PostConstructLifecyclePhase(new Scope(mockApplicationContext.getRegistry(), config, cdu)));
            fail("Expected SmooksConfigurationException");
        } catch(SmooksConfigurationException e) {
            assertEquals("Error invoking 'setConfiguration' method on class 'org.smooks.lifecycle.LifecycleManagerTest$MyContentDeliveryUnit4'.  This class must be public.  Alternatively, use the @Config annotation on a class field.", e.getMessage());
        }
    }

	@Test
    public void test_paramaterSetting_Config_setConfiguration_on_top_level_class() {
        ResourceConfig config = new ResourceConfig();
        MyContentDeliveryUnit5 cdu = new MyContentDeliveryUnit5();

        MockApplicationContext mockApplicationContext = new MockApplicationContext();
        lifecycleManager.applyPhase(cdu, new PostConstructLifecyclePhase(new Scope(mockApplicationContext.getRegistry(), config, cdu)));
        assertNotNull(cdu.config);
    }

	@Test
    public void test_paramaterSetting_Config_choice() {
        ResourceConfig config;
        MyContentDeliveryUnit6 cdu = new MyContentDeliveryUnit6();

        MockApplicationContext mockApplicationContext = new MockApplicationContext();
        // Check that valid values are accepted....
        config = new ResourceConfig();
        config.setParameter("paramA", "A");
        lifecycleManager.applyPhase(cdu, new PostConstructLifecyclePhase(new Scope(mockApplicationContext.getRegistry(), config, cdu)));

        config = new ResourceConfig();
        config.setParameter("paramA", "B");
        lifecycleManager.applyPhase(cdu, new PostConstructLifecyclePhase(new Scope(mockApplicationContext.getRegistry(), config, cdu)));

        config = new ResourceConfig();
        config.setParameter("paramA", "C");
        lifecycleManager.applyPhase(cdu, new PostConstructLifecyclePhase(new Scope(mockApplicationContext.getRegistry(), config, cdu)));

        // Check that invalid values are accepted....
        config = new ResourceConfig();
        config.setParameter("paramA", "X");
        try {
            lifecycleManager.applyPhase(cdu, new PostConstructLifecyclePhase(new Scope(mockApplicationContext.getRegistry(), config, cdu)));
            fail("Expected SmooksConfigurationException");
        } catch(SmooksConfigurationException e) {
            assertEquals("Value 'X' for parameter 'paramA' is invalid.  Valid choices for this parameter are: [A, B, C]", e.getMessage());
        }
    }

	@Test
    public void test_paramaterSetting_decode_error() {
        ResourceConfig config;
        MyContentDeliveryUnit7 cdu = new MyContentDeliveryUnit7();

        config = new ResourceConfig();
        config.setParameter("encoding", "XXXX");
        MockApplicationContext mockApplicationContext = new MockApplicationContext();
        try {
            lifecycleManager.applyPhase(cdu, new PostConstructLifecyclePhase(new Scope(mockApplicationContext.getRegistry(), config, cdu)));
            fail("Expected SmooksConfigurationException.");
        } catch(SmooksConfigurationException e) {
            assertEquals("Failed to set parameter configuration value on 'org.smooks.lifecycle.LifecycleManagerTest$MyContentDeliveryUnit7#encoding'.", e.getMessage());
            assertEquals("Unsupported character set 'XXXX'.", e.getCause().getMessage());
        }
    }

	@Test
    public void test_paramaterSetting_setterMethod() {
        ResourceConfig config = new ResourceConfig();
        MyContentDeliveryUnit8 cdu1 = new MyContentDeliveryUnit8();
        MockApplicationContext mockApplicationContext = new MockApplicationContext();

        config.setParameter("encoding", StandardCharsets.UTF_8);
        lifecycleManager.applyPhase(cdu1, new PostConstructLifecyclePhase(new Scope(mockApplicationContext.getRegistry(), config, cdu1)));
        assertEquals("UTF-8", cdu1.getEncoding().displayName());

        MyContentDeliveryUnit9 cdu2 = new MyContentDeliveryUnit9();
        config.setParameter("encoding", StandardCharsets.UTF_8);
        try {
            lifecycleManager.applyPhase(cdu2, new PostConstructLifecyclePhase(new Scope(mockApplicationContext.getRegistry(), config, cdu2)));
            fail("Expected SmooksConfigurationException.");
        } catch(SmooksConfigurationException e) {
            assertEquals("Unable to determine the property name associated with 'org.smooks.lifecycle.LifecycleManagerTest$MyContentDeliveryUnit9#encoding'. " +
                    "Setter methods that specify the @Inject annotation must either follow the Javabean naming convention ('setX' for property 'x'), " +
                    "or specify the property name via the 'name' parameter on the @Inject annotation.", e.getMessage());
        }

        MyContentDeliveryUnit10 cdu3 = new MyContentDeliveryUnit10();
        config.setParameter("encoding", "UTF-8");
        lifecycleManager.applyPhase(cdu3, new PostConstructLifecyclePhase(new Scope(mockApplicationContext.getRegistry(), config, cdu3)));
        assertEquals("UTF-8", cdu3.getEncoding().displayName());
    }

	@Test
    public void test_Initialize_Uninitialize() {
        ResourceConfig config = new ResourceConfig();
        MyContentDeliveryUnit11 cdu1 = new MyContentDeliveryUnit11();
        MockApplicationContext mockApplicationContext = new MockApplicationContext();

        // Initialize....
        assertFalse(cdu1.initialised);
        assertFalse(cdu1.uninitialised);
        lifecycleManager.applyPhase(cdu1, new PostConstructLifecyclePhase(new Scope(mockApplicationContext.getRegistry(), config, cdu1)));
        
        // Uninitialize....
        assertTrue(cdu1.initialised);
        assertFalse(cdu1.uninitialised);
        lifecycleManager.applyPhase(cdu1, new PreDestroyLifecyclePhase());
        assertTrue(cdu1.initialised);
        assertTrue(cdu1.uninitialised);

        // Initialize - exception....
        MyContentDeliveryUnit12 cdu2 = new MyContentDeliveryUnit12();
        try {
            lifecycleManager.applyPhase(cdu2, new PostConstructLifecyclePhase(new Scope(mockApplicationContext.getRegistry(), config, cdu2)));
            fail("Expected SmooksConfigurationException.");
        } catch(SmooksConfigurationException e) {
            assertEquals("Error invoking @PostConstruct method 'init' on class 'org.smooks.lifecycle.LifecycleManagerTest$MyContentDeliveryUnit12'.", e.getMessage());
        }

        // Uninitialize - exception....
        try {
            lifecycleManager.applyPhase(cdu2, new PreDestroyLifecyclePhase());
            fail("Expected SmooksConfigurationException.");
        } catch(SmooksConfigurationException e) {
            assertEquals("Error invoking @PreDestroy method 'uninit' on class 'org.smooks.lifecycle.LifecycleManagerTest$MyContentDeliveryUnit12'.", e.getMessage());
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    private static class MyContentDeliveryUnit1 implements ContentHandler {

        @Inject
        private String paramA;

        @Inject
        @Named("param-b")
        private String paramB;

        @Inject
        private Integer paramC;
    }

    private class MyContentDeliveryUnit2 implements ContentHandler {

        @Inject
        private String paramA;

        @Inject
        @Named("param-b")
        private String paramB;

        @Inject
        private Optional<Integer> paramC;

        @Inject
        private Integer paramD = 9;
    }

    private class MyContentDeliveryUnit3 implements ContentHandler {

        @Inject
        private ApplicationContext appContext;

        @Inject
        private ResourceConfig config;
    }

    private class MyContentDeliveryUnit4 implements ContentHandler {

        private ResourceConfig config;

        public void setConfiguration(ResourceConfig resourceConfig) throws SmooksConfigurationException {
            this.config = resourceConfig;
        }
    }

    private static class MyContentDeliveryUnit6 implements ContentHandler {

	    public enum  paramAEnum {
	        A, B, C
        }
	    
        @Inject
        private paramAEnum paramA;
    }

    private class MyContentDeliveryUnit7 implements ContentHandler {

        @Inject
        private Charset encoding;
    }

    public class MyContentDeliveryUnit8 implements ContentHandler {

        private Charset encoding;

        public Charset getEncoding() {
            return encoding;
        }

        @Inject        
        public void setEncoding(Charset encoding) {
            this.encoding = encoding;
        }
    }

    public class MyContentDeliveryUnit9 implements ContentHandler {

        private Charset encoding;

        public Charset getEncoding() {
            return encoding;
        }

        @Inject
        public void encoding(Charset encoding) {
            this.encoding = encoding;
        }
    }

    public class MyContentDeliveryUnit10 implements ContentHandler {

        private Charset encoding;

        public Charset getEncoding() {
            return encoding;
        }

        @Inject
        public void encoding(@Named("encoding") Charset encoding) {
            this.encoding = encoding;
        }
    }

    public class MyContentDeliveryUnit11 implements ContentHandler {

        private boolean initialised;
        private boolean uninitialised;

        @PostConstruct
        public void init() {
            initialised = true;
        }

        @PreDestroy
        public void uninit() {
            uninitialised = true;
        }
    }

    public class MyContentDeliveryUnit12 implements ContentHandler {

        @PostConstruct
        public void init() {
            throw new RuntimeException("An initialise error....");
        }

        @PreDestroy
        public void uninit() {
            throw new RuntimeException("An uninitialise error....");
        }
    }
}
