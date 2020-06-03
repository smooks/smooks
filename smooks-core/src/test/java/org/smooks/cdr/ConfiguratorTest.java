/*-
 * ========================LICENSE_START=================================
 * Smooks Core
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0 or,
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
package org.smooks.cdr;

import org.junit.Test;
import static org.junit.Assert.*;
import org.smooks.delivery.ContentHandler;
import org.smooks.delivery.annotation.Initialize;
import org.smooks.delivery.annotation.Uninitialize;
import org.smooks.cdr.annotation.*;
import org.smooks.javabean.decoders.StringDecoder;
import org.smooks.javabean.decoders.IntegerDecoder;
import org.smooks.container.ApplicationContext;
import org.smooks.container.MockApplicationContext;

import java.nio.charset.Charset;

/**
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class ConfiguratorTest {

	@Test
    public void test_paramaterSetting_allok() {
        SmooksResourceConfiguration config = new SmooksResourceConfiguration();
        MyContentDeliveryUnit1 cdu = new MyContentDeliveryUnit1();

        config.setParameter("paramA", "A-Val");
        config.setParameter("param-b", "B-Val");
        config.setParameter("paramC", "8");

        Configurator.configure(cdu, config);
        assertEquals("A-Val", cdu.paramA);
        assertEquals("B-Val", cdu.paramB);
        assertEquals(8, cdu.paramC);
    }

	@Test
    public void test_paramaterSetting_missing_required() {
        SmooksResourceConfiguration config = new SmooksResourceConfiguration();
        MyContentDeliveryUnit1 cdu = new MyContentDeliveryUnit1();

        config.setParameter("paramA", "A-Val");
        config.setParameter("param-b", "B-Val");

        // Don't add the required paramC config

        try {
            Configurator.configure(cdu, config);
            fail(" Expected SmooksConfigurationException");
        } catch(SmooksConfigurationException e) {
            assertTrue(e.getMessage().startsWith("<param> 'paramC' not specified on resource configuration"));
        }
    }
	
	@Test
    public void test_paramaterSetting_optional_default() {
        SmooksResourceConfiguration config = new SmooksResourceConfiguration();
        MyContentDeliveryUnit2 cdu = new MyContentDeliveryUnit2();

        config.setParameter("paramA", "A-Val");
        config.setParameter("param-b", "B-Val");

        // Don't add the optional paramC config

        Configurator.configure(cdu, config);
        assertEquals("A-Val", cdu.paramA);
        assertEquals("B-Val", cdu.paramB);
        assertEquals(9, cdu.paramC);
    }

	@Test
    public void test_Config_And_Context_Setting() {
        SmooksResourceConfiguration config = new SmooksResourceConfiguration();
        MyContentDeliveryUnit3 cdu = new MyContentDeliveryUnit3();

        Configurator.configure(cdu, config, new MockApplicationContext());
        assertNotNull(cdu.config);
        assertNotNull(cdu.appContext);
    }

	@Test
    public void test_paramaterSetting_Config_setConfiguration_on_private_inner_class() {
        SmooksResourceConfiguration config = new SmooksResourceConfiguration();
        MyContentDeliveryUnit4 cdu = new MyContentDeliveryUnit4();

        try {
            Configurator.configure(cdu, config);
            fail("Expected SmooksConfigurationException");
        } catch(SmooksConfigurationException e) {
            assertEquals("Error invoking 'setConfiguration' method on class 'org.smooks.cdr.ConfiguratorTest$MyContentDeliveryUnit4'.  This class must be public.  Alternatively, use the @Config annotation on a class field.", e.getMessage());
        }
    }

	@Test
    public void test_paramaterSetting_Config_setConfiguration_on_top_level_class() {
        SmooksResourceConfiguration config = new SmooksResourceConfiguration();
        MyContentDeliveryUnit5 cdu = new MyContentDeliveryUnit5();

        Configurator.configure(cdu, config);
        assertNotNull(cdu.config);
    }

	@Test
    public void test_paramaterSetting_Config_choice() {
        SmooksResourceConfiguration config;
        MyContentDeliveryUnit6 cdu = new MyContentDeliveryUnit6();

        // Check that valid values are accepted....
        config = new SmooksResourceConfiguration();
        config.setParameter("paramA", "A");
        Configurator.configure(cdu, config);

        config = new SmooksResourceConfiguration();
        config.setParameter("paramA", "B");
        Configurator.configure(cdu, config);

        config = new SmooksResourceConfiguration();
        config.setParameter("paramA", "C");
        Configurator.configure(cdu, config);

        // Check that invalid values are accepted....
        config = new SmooksResourceConfiguration();
        config.setParameter("paramA", "X");
        try {
            Configurator.configure(cdu, config);
            fail("Expected SmooksConfigurationException");
        } catch(SmooksConfigurationException e) {
            assertEquals("Value 'X' for paramater 'paramA' is invalid.  Valid choices for this paramater are: [A, B, C]", e.getMessage());
        }
    }

	@Test
    public void test_paramaterSetting_decode_error() {
        SmooksResourceConfiguration config;
        MyContentDeliveryUnit7 cdu = new MyContentDeliveryUnit7();

        config = new SmooksResourceConfiguration();
        config.setParameter("encoding", "XXXX");
        try {
            Configurator.configure(cdu, config);
            fail("Expected SmooksConfigurationException.");
        } catch(SmooksConfigurationException e) {
            assertEquals("Failed to set paramater configuration value on 'org.smooks.cdr.ConfiguratorTest$MyContentDeliveryUnit7#encoding'.", e.getMessage());
            assertEquals("Unsupported character set 'XXXX'.", e.getCause().getMessage());
        }
    }

	@Test
    public void test_paramaterSetting_setterMethod() {
        SmooksResourceConfiguration config = new SmooksResourceConfiguration();
        MyContentDeliveryUnit8 cdu1 = new MyContentDeliveryUnit8();

        config.setParameter("encoding", "UTF-8");
        Configurator.configure(cdu1, config);
        assertEquals("UTF-8", cdu1.getEncoding().displayName());

        MyContentDeliveryUnit9 cdu2 = new MyContentDeliveryUnit9();
        config.setParameter("encoding", "UTF-8");
        try {
            Configurator.configure(cdu2, config);
            fail("Expected SmooksConfigurationException.");
        } catch(SmooksConfigurationException e) {
            assertEquals("Unable to determine the property name associated with 'org.smooks.cdr.ConfiguratorTest$MyContentDeliveryUnit9#encoding'. " +
                    "Setter methods that specify the @ConfigParam annotation must either follow the Javabean naming convention ('setX' for propert 'x'), " +
                    "or specify the propery name via the 'name' parameter on the @ConfigParam annotation.", e.getMessage());
        }

        MyContentDeliveryUnit10 cdu3 = new MyContentDeliveryUnit10();
        config.setParameter("encoding", "UTF-8");
        Configurator.configure(cdu3, config);
        assertEquals("UTF-8", cdu3.getEncoding().displayName());
    }

	@Test
    public void test_Initialize_Uninitialize() {
        SmooksResourceConfiguration config = new SmooksResourceConfiguration();
        MyContentDeliveryUnit11 cdu1 = new MyContentDeliveryUnit11();

        // Initialize....
        assertFalse(cdu1.initialised);
        assertFalse(cdu1.uninitialised);
        Configurator.configure(cdu1, config);

        // Uninitialize....
        assertTrue(cdu1.initialised);
        assertFalse(cdu1.uninitialised);
        Configurator.uninitialise(cdu1);
        assertTrue(cdu1.initialised);
        assertTrue(cdu1.uninitialised);

        // Initialize - exception....
        MyContentDeliveryUnit12 cdu2 = new MyContentDeliveryUnit12();
        try {
            Configurator.configure(cdu2, config);
            fail("Expected SmooksConfigurationException.");
        } catch(SmooksConfigurationException e) {
            assertEquals("Error invoking @Initialize method 'init' on class 'org.smooks.cdr.ConfiguratorTest$MyContentDeliveryUnit12'.", e.getMessage());
        }

        // Uninitialize - exception....
        try {
            Configurator.uninitialise(cdu2);
            fail("Expected SmooksConfigurationException.");
        } catch(SmooksConfigurationException e) {
            assertEquals("Error invoking @Uninitialize method 'uninit' on class 'org.smooks.cdr.ConfiguratorTest$MyContentDeliveryUnit12'.", e.getMessage());
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    private class MyContentDeliveryUnit1 implements ContentHandler {

        @ConfigParam
        private String paramA;

        @ConfigParam(name="param-b")
        private String paramB;

        @ConfigParam
        private int paramC;
    }

    private class MyContentDeliveryUnit2 implements ContentHandler {

        @ConfigParam(decoder=StringDecoder.class)
        private String paramA;

        @ConfigParam(name="param-b", decoder=StringDecoder.class)
        private String paramB;

        @ConfigParam(decoder=IntegerDecoder.class, use=ConfigParam.Use.OPTIONAL, defaultVal ="9")
        private int paramC;
    }

    private class MyContentDeliveryUnit3 implements ContentHandler {

        @AppContext
        private ApplicationContext appContext;

        @Config
        private SmooksResourceConfiguration config;
    }

    private class MyContentDeliveryUnit4 implements ContentHandler {

        private SmooksResourceConfiguration config;

        public void setConfiguration(SmooksResourceConfiguration resourceConfig) throws SmooksConfigurationException {
            this.config = resourceConfig;
        }
    }

    private class MyContentDeliveryUnit6 implements ContentHandler {

        @ConfigParam(choice = {"A", "B", "C"})
        private String paramA;
    }

    private class MyContentDeliveryUnit7 implements ContentHandler {

        @ConfigParam
        private Charset encoding;
    }

    public class MyContentDeliveryUnit8 implements ContentHandler {

        private Charset encoding;

        public Charset getEncoding() {
            return encoding;
        }

        @ConfigParam        
        public void setEncoding(Charset encoding) {
            this.encoding = encoding;
        }
    }

    public class MyContentDeliveryUnit9 implements ContentHandler {

        private Charset encoding;

        public Charset getEncoding() {
            return encoding;
        }

        @ConfigParam
        public void encoding(Charset encoding) {
            this.encoding = encoding;
        }
    }

    public class MyContentDeliveryUnit10 implements ContentHandler {

        private Charset encoding;

        public Charset getEncoding() {
            return encoding;
        }

        @ConfigParam(name = "encoding")
        public void encoding(Charset encoding) {
            this.encoding = encoding;
        }
    }

    public class MyContentDeliveryUnit11 implements ContentHandler {

        private boolean initialised;
        private boolean uninitialised;

        @Initialize
        public void init() {
            initialised = true;
        }

        @Uninitialize
        public void uninit() {
            uninitialised = true;
        }
    }

    public class MyContentDeliveryUnit12 implements ContentHandler {

        @Initialize
        public void init() {
            throw new RuntimeException("An initialise error....");
        }

        @Uninitialize
        public void uninit() {
            throw new RuntimeException("An uninitialise error....");
        }
    }
}
