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
package org.smooks.engine.lifecycle;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.api.lifecycle.LifecycleManager;
import org.smooks.api.SmooksConfigException;
import org.smooks.engine.resource.config.DefaultResourceConfig;
import org.smooks.api.ApplicationContext;
import org.smooks.tck.MockApplicationContext;
import org.smooks.api.delivery.ContentHandler;
import org.smooks.engine.injector.Scope;
import org.smooks.engine.resource.config.MyContentDeliveryUnit5;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class LifecycleManagerTestCase {

    private static LifecycleManager lifecycleManager;

    @BeforeAll
    public static void beforeClass() {
        lifecycleManager = new DefaultLifecycleManager();
    }
    
	@Test
    public void test_paramaterSetting_allok() {
        ResourceConfig resourceConfig = new DefaultResourceConfig();
        MyContentDeliveryUnit1 cdu = new MyContentDeliveryUnit1();

        resourceConfig.setParameter("paramA", "A-Val");
        resourceConfig.setParameter("param-b", "B-Val");
        resourceConfig.setParameter("paramC", 8);

        MockApplicationContext mockApplicationContext = new MockApplicationContext();
        lifecycleManager.applyPhase(cdu, new PostConstructLifecyclePhase(new Scope(mockApplicationContext.getRegistry(), resourceConfig, cdu)));
        assertEquals("A-Val", cdu.paramA);
        assertEquals("B-Val", cdu.paramB);
        assertEquals((Integer) 8, cdu.paramC);
    }

	@Test
    public void test_paramaterSetting_missing_required() {
        ResourceConfig resourceConfig = new DefaultResourceConfig();
        MyContentDeliveryUnit1 cdu = new MyContentDeliveryUnit1();

        resourceConfig.setParameter("paramA", "A-Val");
        resourceConfig.setParameter("param-b", "B-Val");

        // Don't add the required paramC config

        MockApplicationContext mockApplicationContext = new MockApplicationContext();
        try {
            lifecycleManager.applyPhase(cdu, new PostConstructLifecyclePhase(new Scope(mockApplicationContext.getRegistry(), resourceConfig, cdu)));
            fail(" Expected SmooksConfigurationException");
        } catch(SmooksConfigException e) {
            assertTrue(e.getMessage().startsWith("<param> 'paramC' not specified on resource configuration"));
        }
    }

    @Test
    public void test_parameterSetting_optional() {
        ResourceConfig resourceConfig = new DefaultResourceConfig();
        MyContentDeliveryUnit2 cdu = new MyContentDeliveryUnit2();

        resourceConfig.setParameter("paramA", "A-Val");
        resourceConfig.setParameter("param-b", "B-Val");
        resourceConfig.setParameter("paramD", 8);

        // Don't add the optional paramC config

        MockApplicationContext mockApplicationContext = new MockApplicationContext();
        lifecycleManager.applyPhase(cdu, new PostConstructLifecyclePhase(new Scope(mockApplicationContext.getRegistry(), resourceConfig, cdu)));
        assertEquals("A-Val", cdu.paramA);
        assertEquals("B-Val", cdu.paramB);
        assertEquals(Optional.empty(), cdu.paramC);
        assertEquals((Integer) 8, cdu.paramD);
    }
    
    @Test
    public void test_parameterSetting_default() {
        ResourceConfig resourceConfig = new DefaultResourceConfig();
        MyContentDeliveryUnit2 cdu = new MyContentDeliveryUnit2();

        resourceConfig.setParameter("paramA", "A-Val");
        resourceConfig.setParameter("param-b", "B-Val");
        resourceConfig.setParameter("paramC", 8);

        // Don't add the optional paramD config

        MockApplicationContext mockApplicationContext = new MockApplicationContext();
        lifecycleManager.applyPhase(cdu, new PostConstructLifecyclePhase(new Scope(mockApplicationContext.getRegistry(), resourceConfig, cdu)));
        assertEquals("A-Val", cdu.paramA);
        assertEquals("B-Val", cdu.paramB);
        assertEquals((Integer) 8, cdu.paramC.get());
        assertEquals((Integer) 9, cdu.paramD);
    }

	@Test
    public void test_Config_And_Context_Setting() {
        ResourceConfig resourceConfig = new DefaultResourceConfig();
        MyContentDeliveryUnit3 cdu = new MyContentDeliveryUnit3();

        MockApplicationContext mockApplicationContext = new MockApplicationContext();
        lifecycleManager.applyPhase(cdu, new PostConstructLifecyclePhase(new Scope(mockApplicationContext.getRegistry(), resourceConfig, cdu)));
        assertNotNull(cdu.config);
        assertNotNull(cdu.appContext);
    }

	@Test
    @Disabled("TODO")
    public void test_parameterSetting_Config_setConfiguration_on_private_inner_class() {
        ResourceConfig resourceConfig = new DefaultResourceConfig();
        MyContentDeliveryUnit4 cdu = new MyContentDeliveryUnit4();

        MockApplicationContext mockApplicationContext = new MockApplicationContext();
        try {
            lifecycleManager.applyPhase(cdu, new PostConstructLifecyclePhase(new Scope(mockApplicationContext.getRegistry(), resourceConfig, cdu)));
            fail("Expected SmooksConfigurationException");
        } catch(SmooksConfigException e) {
            assertEquals("Error invoking 'setConfiguration' method on class 'org.smooks.engine.lifecycle.LifecycleManagerTestCase$MyContentDeliveryUnit4'.  This class must be public.  Alternatively, use the @Inject annotation on a class field.", e.getMessage());
        }
    }

	@Test
    public void test_paramaterSetting_Config_setConfiguration_on_top_level_class() {
        ResourceConfig resourceConfig = new DefaultResourceConfig();
        MyContentDeliveryUnit5 cdu = new MyContentDeliveryUnit5();

        MockApplicationContext mockApplicationContext = new MockApplicationContext();
        lifecycleManager.applyPhase(cdu, new PostConstructLifecyclePhase(new Scope(mockApplicationContext.getRegistry(), resourceConfig, cdu)));
        assertNotNull(cdu.config);
    }

	@Test
    public void test_paramaterSetting_Config_choice() {
        ResourceConfig resourceConfig;
        MyContentDeliveryUnit6 cdu = new MyContentDeliveryUnit6();

        MockApplicationContext mockApplicationContext = new MockApplicationContext();
        // Check that valid values are accepted....
        resourceConfig = new DefaultResourceConfig();
        resourceConfig.setParameter("paramA", "A");
        lifecycleManager.applyPhase(cdu, new PostConstructLifecyclePhase(new Scope(mockApplicationContext.getRegistry(), resourceConfig, cdu)));

        resourceConfig = new DefaultResourceConfig();
        resourceConfig.setParameter("paramA", "B");
        lifecycleManager.applyPhase(cdu, new PostConstructLifecyclePhase(new Scope(mockApplicationContext.getRegistry(), resourceConfig, cdu)));

        resourceConfig = new DefaultResourceConfig();
        resourceConfig.setParameter("paramA", "C");
        lifecycleManager.applyPhase(cdu, new PostConstructLifecyclePhase(new Scope(mockApplicationContext.getRegistry(), resourceConfig, cdu)));

        // Check that invalid values are accepted....
        resourceConfig = new DefaultResourceConfig();
        resourceConfig.setParameter("paramA", "X");
        try {
            lifecycleManager.applyPhase(cdu, new PostConstructLifecyclePhase(new Scope(mockApplicationContext.getRegistry(), resourceConfig, cdu)));
            fail("Expected SmooksConfigurationException");
        } catch(SmooksConfigException e) {
            assertEquals("Value 'X' for parameter 'paramA' is invalid.  Valid choices for this parameter are: [A, B, C]", e.getMessage());
        }
    }

	@Test
    public void test_paramaterSetting_decode_error() {
        ResourceConfig resourceConfig;
        MyContentDeliveryUnit7 cdu = new MyContentDeliveryUnit7();

        resourceConfig = new DefaultResourceConfig();
        resourceConfig.setParameter("encoding", "XXXX");
        MockApplicationContext mockApplicationContext = new MockApplicationContext();
        try {
            lifecycleManager.applyPhase(cdu, new PostConstructLifecyclePhase(new Scope(mockApplicationContext.getRegistry(), resourceConfig, cdu)));
            fail("Expected SmooksConfigurationException.");
        } catch(SmooksConfigException e) {
            assertEquals("Failed to set parameter configuration value on 'org.smooks.engine.lifecycle.LifecycleManagerTestCase$MyContentDeliveryUnit7#encoding'.", e.getMessage());
            assertEquals("Unsupported character set 'XXXX'.", e.getCause().getMessage());
        }
    }

	@Test
    public void test_paramaterSetting_setterMethod() {
        ResourceConfig resourceConfig = new DefaultResourceConfig();
        MyContentDeliveryUnit8 cdu1 = new MyContentDeliveryUnit8();
        MockApplicationContext mockApplicationContext = new MockApplicationContext();

        resourceConfig.setParameter("encoding", StandardCharsets.UTF_8);
        lifecycleManager.applyPhase(cdu1, new PostConstructLifecyclePhase(new Scope(mockApplicationContext.getRegistry(), resourceConfig, cdu1)));
        assertEquals("UTF-8", cdu1.getEncoding().displayName());

        MyContentDeliveryUnit9 cdu2 = new MyContentDeliveryUnit9();
        resourceConfig.setParameter("encoding", StandardCharsets.UTF_8);
        try {
            lifecycleManager.applyPhase(cdu2, new PostConstructLifecyclePhase(new Scope(mockApplicationContext.getRegistry(), resourceConfig, cdu2)));
            fail("Expected SmooksConfigurationException.");
        } catch(SmooksConfigException e) {
            assertEquals("Unable to determine the property name associated with 'org.smooks.engine.lifecycle.LifecycleManagerTestCase$MyContentDeliveryUnit9#encoding'. " +
                    "Setter methods that specify the @Inject annotation must either follow the Javabean naming convention ('setX' for property 'x'), " +
                    "or specify the property name via the 'name' parameter on the @Inject annotation.", e.getMessage());
        }

        MyContentDeliveryUnit10 cdu3 = new MyContentDeliveryUnit10();
        resourceConfig.setParameter("encoding", "UTF-8");
        lifecycleManager.applyPhase(cdu3, new PostConstructLifecyclePhase(new Scope(mockApplicationContext.getRegistry(), resourceConfig, cdu3)));
        assertEquals("UTF-8", cdu3.getEncoding().displayName());
    }

	@Test
    public void test_Initialize_Uninitialize() {
        ResourceConfig resourceConfig = new DefaultResourceConfig();
        MyContentDeliveryUnit11 cdu1 = new MyContentDeliveryUnit11();
        MockApplicationContext mockApplicationContext = new MockApplicationContext();

        // Initialize....
        assertFalse(cdu1.initialised);
        assertFalse(cdu1.uninitialised);
        lifecycleManager.applyPhase(cdu1, new PostConstructLifecyclePhase(new Scope(mockApplicationContext.getRegistry(), resourceConfig, cdu1)));
        
        // Uninitialize....
        assertTrue(cdu1.initialised);
        assertFalse(cdu1.uninitialised);
        lifecycleManager.applyPhase(cdu1, new PreDestroyLifecyclePhase());
        assertTrue(cdu1.initialised);
        assertTrue(cdu1.uninitialised);

        // Initialize - exception....
        MyContentDeliveryUnit12 cdu2 = new MyContentDeliveryUnit12();
        try {
            lifecycleManager.applyPhase(cdu2, new PostConstructLifecyclePhase(new Scope(mockApplicationContext.getRegistry(), resourceConfig, cdu2)));
            fail("Expected SmooksConfigurationException.");
        } catch(SmooksConfigException e) {
            assertEquals("Error invoking @PostConstruct method 'init' on class 'org.smooks.engine.lifecycle.LifecycleManagerTestCase$MyContentDeliveryUnit12'.", e.getMessage());
        }

        // Uninitialize - exception....
        try {
            lifecycleManager.applyPhase(cdu2, new PreDestroyLifecyclePhase());
            fail("Expected SmooksConfigurationException.");
        } catch(SmooksConfigException e) {
            assertEquals("Error invoking @PreDestroy method 'uninit' on class 'org.smooks.engine.lifecycle.LifecycleManagerTestCase$MyContentDeliveryUnit12'.", e.getMessage());
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

    private static class MyContentDeliveryUnit2 implements ContentHandler {

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

    private static class MyContentDeliveryUnit3 implements ContentHandler {

        @Inject
        private ApplicationContext appContext;

        @Inject
        private ResourceConfig config;
    }

    private static class MyContentDeliveryUnit4 implements ContentHandler {

        private ResourceConfig config;

        public void setConfiguration(ResourceConfig resourceConfig) throws SmooksConfigException {
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

    private static class MyContentDeliveryUnit7 implements ContentHandler {

        @Inject
        private Charset encoding;
    }

    public static class MyContentDeliveryUnit8 implements ContentHandler {

        private Charset encoding;

        public Charset getEncoding() {
            return encoding;
        }

        @Inject        
        public void setEncoding(Charset encoding) {
            this.encoding = encoding;
        }
    }

    public static class MyContentDeliveryUnit9 implements ContentHandler {

        private Charset encoding;

        public Charset getEncoding() {
            return encoding;
        }

        @Inject
        public void encoding(Charset encoding) {
            this.encoding = encoding;
        }
    }

    public static class MyContentDeliveryUnit10 implements ContentHandler {

        private Charset encoding;

        public Charset getEncoding() {
            return encoding;
        }

        @Inject
        public void encoding(@Named("encoding") Charset encoding) {
            this.encoding = encoding;
        }
    }

    public static class MyContentDeliveryUnit11 implements ContentHandler {

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

    public static class MyContentDeliveryUnit12 implements ContentHandler {

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
