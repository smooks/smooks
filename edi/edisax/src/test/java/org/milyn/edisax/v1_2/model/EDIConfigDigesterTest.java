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

package org.milyn.edisax.v1_2.model;

import static org.milyn.io.StreamUtils.readStream;
import org.junit.Test;
import static org.junit.Assert.*;
import org.milyn.edisax.model.EDIConfigDigester;
import org.milyn.edisax.model.internal.*;
import org.milyn.edisax.EDIConfigurationException;
import org.milyn.javabean.decoders.DateDecoder;
import org.milyn.cdr.SmooksConfigurationException;
import org.xml.sax.SAXException;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

/**
 * This testcase tests that all new elements introduced in version 1.2 is digested from
 * configurationfile.
 *
 * @author bardl
 */
public class EDIConfigDigesterTest { 

    /**
     * This testcase tests that parent MappingNode is connected to the correct MappingNode.
     * @throws org.milyn.edisax.EDIConfigurationException is thrown when error occurs during config-digestion.
     * @throws java.io.IOException is thrown when unable to read edi-config in testcase.
     * @throws org.xml.sax.SAXException is thrown when error occurs during config-digestion.
     */
    @Test
    public void testParentMappingNodes() throws IOException, EDIConfigurationException, SAXException {
        InputStream input = new ByteArrayInputStream(readStream(getClass().getResourceAsStream("edi-config-all-new-elements.xml")));
        Edimap edimap = EDIConfigDigester.digestConfig(input);

        //SegmentGroup
        SegmentGroup rootSegmentGroup = edimap.getSegments();
        assertNull("Root segmentGroup should have no parent", rootSegmentGroup.getParent());

        SegmentGroup segmentGroup = edimap.getSegments().getSegments().get(0);
        assertEquals("SegmentGroup[" + segmentGroup.getXmltag() + "] should have the root SegmentGroup[" + rootSegmentGroup.getXmltag() + "] as parent but had parent[" + segmentGroup.getParent().getXmltag() + "]", segmentGroup.getParent(), rootSegmentGroup);

        //Segment
        Segment segment = (Segment)segmentGroup.getSegments().get(0);
        assertEquals("Segment[" + segment.getXmltag() + "] should have the SegmentGroup[" + segmentGroup.getXmltag() + "] as parent", segment.getParent(), segmentGroup);

        //Fields
        for (Field field : segment.getFields()) {
            assertEquals("Field[" + field.getXmltag() + "] should have the Segment[" + segment.getXmltag() + "] as parent", field.getParent(), segment);
            for (Component component : field.getComponents()) {
                assertEquals("Component[" + component.getXmltag() + "] should have the Field[" + field.getXmltag() + "] as parent", component.getParent(), field);
                for (SubComponent subComponent : component.getSubComponents()) {
                    assertEquals("SubComponent[" + subComponent.getXmltag() + "] should have the Component[" + component.getXmltag() + "] as parent", subComponent.getParent(), component);
                }
            }
        }
    }

    /**
     * This testcase tests that all values are read from ValueNode.
     * @throws org.milyn.edisax.EDIConfigurationException is thrown when error occurs during config-digestion.
     * @throws java.io.IOException is thrown when unable to read edi-config in testcase.
     * @throws org.xml.sax.SAXException is thrown when error occurs during config-digestion.
     */
    @Test
    public void testReadValueNodes() throws IOException, EDIConfigurationException, SAXException {
        InputStream input = new ByteArrayInputStream(readStream(getClass().getResourceAsStream("edi-config-all-new-elements.xml")));
        Edimap edimap = EDIConfigDigester.digestConfig(input);

        //SegmentGroup
        SegmentGroup segmentGroup = edimap.getSegments().getSegments().get(0);
        assertEquals("Failed to digest documentation for SegmentGroup", segmentGroup.getDocumentation(), "segmentGroup-documentation");

        Segment segment = (Segment)segmentGroup.getSegments().get(0);
        assertEquals("Failed to digest documentation for Segment", segment.getDocumentation(), "segment-documentation");
        List<Field> fields = segment.getFields();

        // Assert field is read correctly.
        // <medi:field xmltag="aTime" type="Time" format="HHmm" minLength="0" maxLength="4"/>
        assertEquals("Failed to digest type-attribute for Field", fields.get(0).getDataType(), "Date");
        assertEquals("Failed to digest parameters-attribute for Field", fields.get(0).getTypeParameters().get(0).getKey(), "format");
        assertEquals("Failed to digest parameters-attribute for Field", fields.get(0).getTypeParameters().get(0).getValue(), "HHmm");
        assertEquals("Failed to digest minLength-attribute for Field", fields.get(0).getMinLength(), new Integer(0));
        assertEquals("Failed to digest maxLength-attribute for Field", fields.get(0).getMaxLength(), new Integer(4));
        assertEquals("Failed to digest documentation for Field", fields.get(0).getDocumentation(), "field1-documentation");

        // Assert Component is read correctly.
        // <medi:component xmltag="aBinary" required="true" type="Binary" minLength="0" maxLength="8"/>
        Component component = fields.get(1).getComponents().get(0);
        assertEquals("Failed to digest type-attribute for Component", component.getDataType(), "Binary");
        assertNull("Parameters-attribute should be null in Component", component.getTypeParameters());
        assertEquals("Failed to digest minLength-attribute for Component", component.getMinLength(), new Integer(0));
        assertEquals("Failed to digest maxLength-attribute for Component", component.getMaxLength(), new Integer(8));
        assertEquals("Failed to digest documentation for Component", component.getDocumentation(), "component-documentation");

        // Assert SubComponent is read correctly.
        // <medi:sub-component xmltag="aNumeric" type="Numeric" format="#0.00" minLength="1" maxLength="4"/>
        SubComponent subcomponent = fields.get(1).getComponents().get(1).getSubComponents().get(0);
        assertEquals("Failed to digest type-attribute for SubComponent", subcomponent.getDataType(), "Double");
        assertEquals("Failed to digest parameters-attribute for SubComponent", subcomponent.getTypeParameters().get(0).getKey(), "format");
        assertEquals("Failed to digest format-attribute for SubComponent", subcomponent.getTypeParameters().get(0).getValue(), "#0.00");
        assertEquals("Failed to digest minLength-attribute for SubComponent", subcomponent.getMinLength(), new Integer(1));
        assertEquals("Failed to digest maxLength-attribute for SubComponent", subcomponent.getMaxLength(), new Integer(4));
        assertEquals("Failed to digest documentation for SubComponent", subcomponent.getDocumentation(), "subcomponent-documentation");
    }

    /**
     * This testcase tests that description attribute is read from Segment.
     * @throws org.milyn.edisax.EDIConfigurationException is thrown when error occurs during config-digestion.
     * @throws java.io.IOException is thrown when unable to read edi-config in testcase.
     * @throws org.xml.sax.SAXException is thrown when error occurs during config-digestion.
     */
    @Test
    public void testReadSegmentDescription() throws IOException, EDIConfigurationException, SAXException {
        InputStream input = new ByteArrayInputStream(readStream(getClass().getResourceAsStream("edi-config-all-new-elements.xml")));
        Edimap edimap = EDIConfigDigester.digestConfig(input);

        Segment segment = (Segment)edimap.getSegments().getSegments().get(0).getSegments().get(0);
        String expected = "This segment is used for testing all new elements in v.1.2";
        assertEquals("Description in segment [" + segment.getDescription() + "] doesn't match expected value [" + expected + "].", segment.getDescription(), expected);
    }

    @Test
    public void testCorrectParametersNoCustomType() throws IOException, SAXException, EDIConfigurationException {
        InputStream input = new ByteArrayInputStream(readStream(getClass().getResourceAsStream("edi-config-correct-no-custom-parameter.xml")));

        Edimap edimap = EDIConfigDigester.digestConfig(input);

        Segment segment = (Segment)edimap.getSegments().getSegments().get(0);
        Field field = segment.getFields().get(0);

        assertEquals("Number of parameters in list [" + field.getTypeParameters().size() + "] doesn't match expected value [2].", field.getTypeParameters().size(), 2);

        String expected = "format";
        String value = field.getTypeParameters().get(0).getKey();
        assertEquals("Key in parameters [" + value + "] doesn't match expected value [" + expected + "].", value, expected);

        expected = "yyyyMMdd";
        value = field.getTypeParameters().get(0).getValue();
        assertEquals("Value in parameters [" + value + "] doesn't match expected value [" + expected + "].", value, expected);

        expected = "param2";
        value = field.getTypeParameters().get(1).getKey();
        assertEquals("Key in parameters [" + value + "] doesn't match expected value [" + expected + "].", value, expected);

        expected = "value2";
        value = field.getTypeParameters().get(1).getValue(); 
        assertEquals("Value in parameters [" + value + "] doesn't match expected value [" + expected + "].", value, expected);

    }

    @Test
    public void testIncorrectParametersNoCustomType() throws IOException, SAXException {
        InputStream input = new ByteArrayInputStream(readStream(getClass().getResourceAsStream("edi-config-incorrect-no-custom-parameter.xml")));
        
        try {
            EDIConfigDigester.digestConfig(input);
            assertTrue("EDIConfigDigester should fail for test configuration.", false);
        } catch (EDIConfigurationException e) {
            String expected = "Invalid use of paramaters in ValueNode. A parameter-entry should consist of a key-value-pair separated with the '='-character. Example: [parameters=\"key1=value1;key2=value2\"]";
            assertEquals("Message in exception [" + e.getMessage() + "] doesn't match expected value [" + expected + "].", e.getMessage(), expected);
        }
    }

    @Test
    public void testIncorrectParametersNoCustomType_ClassName() throws IOException, SAXException {
        InputStream input = new ByteArrayInputStream(readStream(getClass().getResourceAsStream("edi-config-incorrect-no-custom-parameter2.xml")));

        try {
            EDIConfigDigester.digestConfig(input);
            assertTrue("EDIConfigDigester should fail for test configuration.", false);
        } catch (EDIConfigurationException e) {
            String expected = "When first parameter in list of parameters is not a key-value-pair the type of the ValueNode should be Custom.";
            assertEquals("Message in exception [" + e.getMessage() + "] doesn't match expected value [" + expected + "].", e.getMessage(), expected);
        }
    }

    @Test
    public void testCorrectParametersCustomType() throws IOException, SAXException, EDIConfigurationException {
        InputStream input = new ByteArrayInputStream(readStream(getClass().getResourceAsStream("edi-config-correct-custom-parameter.xml")));

        Edimap edimap = EDIConfigDigester.digestConfig(input);

        Segment segment = (Segment)edimap.getSegments().getSegments().get(0);
        Field field = segment.getFields().get(0);

        assertEquals("Number of parameters in list [" + field.getTypeParameters().size() + "] doesn't match expected value [2].", field.getTypeParameters().size(), 2);

        String expected = "CustomClass";
        String value = field.getTypeParameters().get(0).getValue();
        assertEquals("Value in parameters [" + value + "] doesn't match expected value [" + expected + "].", DateDecoder.class.getName(), value);

        expected = "param1";
        value = field.getTypeParameters().get(1).getKey();
        assertEquals("Key in parameters [" + value + "] doesn't match expected value [" + expected + "].", value, expected);

        expected = "value1";
        value = field.getTypeParameters().get(1).getValue();
        assertEquals("Value in parameters [" + value + "] doesn't match expected value [" + expected + "].", value, expected);

    }

    @Test
    public void testIncorrectParametersCustomType_NoClassName() throws IOException, SAXException, EDIConfigurationException {
        InputStream input = new ByteArrayInputStream(readStream(getClass().getResourceAsStream("edi-config-incorrect-custom-parameter.xml")));

        try {
            EDIConfigDigester.digestConfig(input);
            fail("Expected SmooksConfigurationException");
        } catch (SmooksConfigurationException e) {
            assertEquals("Mandatory property 'decoderClass' not specified.", e.getMessage());
        }
    }
}
