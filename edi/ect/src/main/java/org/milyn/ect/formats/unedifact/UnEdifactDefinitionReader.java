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
package org.milyn.ect.formats.unedifact;

import org.milyn.edisax.model.internal.*;
import org.milyn.ect.EdiParseException;
import org.milyn.ect.common.XmlTagEncoder;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


/**
 * UnCefactDefinitionReader
 * @author bardl
 */
public class UnEdifactDefinitionReader {

    /**
     * Matches the line of '-' characters separating the data-, composite- or segment-definitions.
     */
    private static final String ELEMENT_SEPARATOR = "^-+$";

    /**
     * Matches the string "..".
     */
    private static final String DOTS = "\\.\\.";

    /**
     * Extracts information from Data element occuring on one single row in Composite definition.
     * Example - "010    3042  Street and number or post office box"
     * Group1 = line number
     * Group2 = id
     * Group3 = name
     * Group4 = mandatory
     * Group5 = type
     * Group6 = min occurance
     * Group7 = max occurance
     */
    private static final Pattern WHOLE_DATA_ELEMENT = Pattern.compile(" *(\\d{3})*[SX\\|\\+\\-\\*\\# ]*(\\d{4}) *(.*) *.*(C|M) *(an|n|a)(\\.*)(\\d*)");

    /**
     * Extracts information from Data element occuring on one single row in Composite definition.
     * Example - "010    9173  Event description code                    C      an..35"
     * Group1 = line number
     * Group2 = id
     * Group3 = name
     */
    private static final Pattern FIRST_DATA_ELEMENT_PART = Pattern.compile(" *(\\d{3})*[SX\\|\\+\\-\\*\\# ]*(\\d{4}) *(.*) *");

    /**
     * Extracts information from Data element occuring on one single row in Composite definition.
     * Example - "identifier                                M      an..35"
     * Group3 = name
     * Group4 = mandatory
     * Group5 = type
     * Group6 = min occurance
     * Group7 = max occurance
     */
    private static final Pattern SECOND_DATA_ELEMENT_PART = Pattern.compile(" *(.*) *.*(C|M) *(an|n|a)(\\.*)(\\d*)");

    /**
     * Extracts information from Data header.
     * Example: "3237  Sample location description code                        [B]"
     * Group1 = id
     * Group2 = name
     * Group3 = usage (not used today)
     */
    private static final Pattern ELEMENT_HEADER = Pattern.compile("[SX\\|\\+\\-\\*\\# ]*(\\w{4}) *(.*) *\\[(\\w)\\]");
    private static final Pattern ELEMENT_HEADER_OLD = Pattern.compile("[SX\\|\\+\\-\\*\\# ]*(\\w{4}) *(.*)");

    /**
     * Extracts information from Composite header.
     * Example: "C001 TRANSPORT MEANS"
     * Group1 = id
     * Group2 = name
     */
    private static final Pattern COMPOSITE_HEADER = Pattern.compile("[SX\\|\\+\\-\\*\\# ]*(\\w{4}) *(.*)");

    /**
     * Extracts information from Segment header.
     * Example: "AGR  AGREEMENT IDENTIFICATION"
     * Group1 = id
     * Group2 = name
     */
    private static final Pattern SEGMENT_HEADER = Pattern.compile("[SX\\|\\+\\-\\*\\# ]*(\\w{3}) *(.*)");

    /**
     * Extracts information from SegmentElement. Could be either a Composite or a Data.
     * Example: "010    C779 ARRAY STRUCTURE IDENTIFICATION             M    1"
     * Group1 = line number
     * Group2 = id
     * Group3 = name
     * Group4 = mandatory
     */
    private static final Pattern SEGMENT_ELEMENT = Pattern.compile(" *\\** *(\\d{3})*[SX\\|\\+\\-\\*\\# ]*(\\d{4}|C\\d{3}) *(.*) *( C| M).*");

    /**
     * Extracts information from first SegmentElement when Composite or Data element description exists on several
     * lines. Could be either a Composite or a Data.
     * Example: "010    C779 ARRAY STRUCTURE IDENTIFICATION
     *                       AND SOME MORE DESCRIPTION           M    1 an..15"
     * Group1 = line number
     * Group2 = id
     * Group3 = name
     * Group4 = mandatory
     */
    private static final Pattern FIRST_SEGMENT_ELEMENT = Pattern.compile(" *(\\d{3})*[SX\\|\\+\\-\\*\\# ]*(\\d{4}|C\\d{3}) *(.*)");

    /**
     * Extracts information from second SegmentElement when Composite or Data element description exists on several
     * lines. Could be either a Composite or a Data.
     * Example: "010    C779 ARRAY STRUCTURE IDENTIFICATION
     *                       AND SOME MORE DESCRIPTION           M    1 an..15"
     * Group1 = line number
     * Group2 = id
     * Group3 = name
     */
    private static final Pattern SECOND_SEGMENT_ELEMENT = Pattern.compile("^(.*) *( C| M).*");

    private static List<Segment> readSegments(Reader reader, Map<String, Field> composites, Map<String, Component> datas) throws IOException, EdiParseException {
        List<Segment> segments = new ArrayList<Segment>();

        BufferedReader _reader = new BufferedReader(reader);
        moveToNextPart(_reader);

        Segment segment = getSegment(_reader, composites, datas);
        while (segment != null) {
            segments.add(segment);
            segment = getSegment(_reader, composites, datas);
        }

        return segments;
    }

    private static Segment getSegment(BufferedReader reader, Map<String, Field> fields, Map<String, Component> componens) throws IOException, EdiParseException {
        //Read id and name.
        String line = readUntilValue(reader);

        if (line == null) {
            return null;
        }

        String segcode, name;
        Matcher headerMatcher = SEGMENT_HEADER.matcher(line);
        if (headerMatcher.matches()) {
            segcode = headerMatcher.group(1);
            name = headerMatcher.group(2);
        } else {
            throw new EdiParseException("Unable to extract segment code and name for Segment from line [" + line + "].");
        }

        String description = getValue(reader, "Function:");

        Segment segment = new Segment();
        segment.setSegcode(segcode);
        segment.setXmltag(XmlTagEncoder.encode(name.trim()));
        segment.setDescription(description);
        segment.setTruncatable(true);

        line = readUntilValue(reader);

        Matcher matcher;
        while (line != null && !line.matches(ELEMENT_SEPARATOR)) {
            matcher = SEGMENT_ELEMENT.matcher(line);
            if (matcher.matches()) {
                addFieldToSegment(fields, componens, segment, matcher.group(2), matcher.group(4).trim().equalsIgnoreCase("M"));
                if (matcher.group(2).startsWith("C")) {
                    while (line != null && !line.equals("")) {
                        line = reader.readLine();
                    }
                }
            } else {
                matcher = FIRST_SEGMENT_ELEMENT.matcher(line);
                if (matcher.matches()) {
                    String id = matcher.group(2);
                    line = reader.readLine();
                    if (line == null) {
                            continue;
                    }
                    matcher = SECOND_SEGMENT_ELEMENT.matcher(line);
                    if (matcher.matches()) {
                        addFieldToSegment(fields, componens, segment, id, matcher.group(2).trim().equalsIgnoreCase("M"));
                    }
//                    } else {
//                        throw new EdiParseException("Unable to match current line in segment description file. Erranous line [" + line + "].");
//                    }
                }
            }
            line = reader.readLine();
        }
        return segment;
    }

    private static void addFieldToSegment(Map<String, Field> fields, Map<String, Component> componens, Segment segment, String id, boolean isMandatory) {
        if (id.toUpperCase().startsWith("C")) {
            segment.getFields().add(copyField(fields.get(id), isMandatory));
        } else {
            segment.getFields().add(convertToField(componens.get(id), isMandatory));
        }
    }

    private static Field convertToField(Component component, boolean isMandatory) {
        Field field = new Field();
        field.setXmltag(XmlTagEncoder.encode(component.getXmltag()));
        field.setNodeTypeRef(component.getNodeTypeRef());
        field.setDocumentation(component.getDocumentation());
        field.setMaxLength(component.getMaxLength());
        field.setMinLength(component.getMinLength());
        field.setRequired(isMandatory);
        field.setTruncatable(true);
        field.setDataType(component.getDataType());
        field.setDataTypeParameters(component.getTypeParameters());
        return field;
    }

    private static Field copyField(Field oldField, boolean isMandatory) {
        Field field = new Field();
        field.setXmltag(XmlTagEncoder.encode(oldField.getXmltag()));
        field.setNodeTypeRef(oldField.getNodeTypeRef());
        field.setDocumentation(oldField.getDocumentation());
        field.setMaxLength(oldField.getMaxLength());
        field.setMinLength(oldField.getMinLength());
        field.setRequired(isMandatory);
        field.setTruncatable(true);
        field.setDataType(oldField.getDataType());
        field.setDataTypeParameters(oldField.getTypeParameters());
        field.getComponents().addAll(oldField.getComponents());
        return field;
    }

    private static Map<String, Field> readFields(Reader reader, Map<String, Component> components) throws IOException, EdiParseException {
        Map<String, Field> fields = new HashMap<String, Field>();

        BufferedReader _reader = new BufferedReader(reader);
        moveToNextPart(_reader);

        Field field = new Field();
        String id = populateField(_reader, components, field);
        while (id != null) {
            fields.put(id, field);
            moveToNextPart(_reader);
            field = new Field();
            id = populateField(_reader, components, field);
        }

        return fields;
    }

    private static String populateField(BufferedReader reader, Map<String, Component> components, Field field) throws IOException, EdiParseException {
        //Read id and name.
        String line = readUntilValue(reader);

        if (line == null) {
            return null;
        }

        String id, name;
        Matcher headerMatcher = COMPOSITE_HEADER.matcher(line);
        if (headerMatcher.matches()) {
            id = headerMatcher.group(1);
            name = headerMatcher.group(2);
        } else {
            throw new EdiParseException("Unable to extract id and name for Composite element from line [" + line + "].");
        }

        String description = getValue(reader, "Desc:");

        field.setNodeTypeRef(id);
        field.setXmltag(XmlTagEncoder.encode(name));
        field.setDocumentation(description);

        line = readUntilValue(reader);
        LinePart linePart;
        while (line != null && line.length() != 0) {
            linePart = getLinePart(reader, line);
            if (linePart != null) {
                Component component = new Component();
                component.setRequired(linePart.isMandatory());
if (components.get(linePart.getId()) == null) {
    System.out.println("POPULATE COMPONENT - " + linePart.getId() + " : " + line);
}
                populateComponent(component, components.get(linePart.getId()));
                field.getComponents().add(component);
            }
            line = reader.readLine();
        }

        return id;
    }

    private static void populateComponent(Component toComponent, Component fromComponent) {
        toComponent.setDocumentation(fromComponent.getDocumentation());
        toComponent.setMaxLength(fromComponent.getMaxLength());
        toComponent.setMinLength(fromComponent.getMinLength());
        toComponent.setTruncatable(true);
        toComponent.setDataType(fromComponent.getDataType());
        toComponent.setDataTypeParameters(fromComponent.getTypeParameters());
        toComponent.setXmltag(XmlTagEncoder.encode(fromComponent.getXmltag()));
    }

    private static Map<String, Component> readComponents(Reader reader) throws IOException, EdiParseException {
        Map<String, Component> datas = new HashMap<String, Component>();

        BufferedReader _reader = new BufferedReader(reader);
        moveToNextPart(_reader);

        Component component = new Component();
        String id = populateComponent(_reader, component);
        while (id != null) {
            datas.put(id, component);
            moveToNextPart(_reader);
            component = new Component();
            id = populateComponent(_reader, component);
        }

        return datas;
    }

    private static String populateComponent(BufferedReader reader, Component component) throws IOException, EdiParseException {

        //Read id and name.
        String line = readUntilValue(reader);

        if (line == null) {
            return null;
        }

        String id, name;
        Matcher headerMatcher = ELEMENT_HEADER.matcher(line);
        if (headerMatcher.matches()) {
            id = headerMatcher.group(1);
            name = headerMatcher.group(2);
        } else {
	    Matcher headerMatcherOld = ELEMENT_HEADER_OLD.matcher(line);
	    if (headerMatcherOld.matches()) {
		id = headerMatcherOld.group(1);
		name = headerMatcherOld.group(2);
	    } else {
		throw new EdiParseException("Unable to extract id and name for Data element from line [" + line + "].");
	    }
        }

        String description = getValue(reader, "Desc:");

        String repr = getValue(reader, "Repr:");
        String[] typeAndOccurance = repr.split(DOTS);

        component.setNodeTypeRef(id);
        component.setXmltag(XmlTagEncoder.encode(name.trim()));
        component.setDataType(getType(typeAndOccurance));
        component.setMinLength(getMinLength(typeAndOccurance));
        component.setMaxLength(getMaxLength(typeAndOccurance));
        component.setDocumentation(description);

        return id;
    }

    private static int getMinLength(String[] typeAndOccurance) {
        if (typeAndOccurance.length == 0) {
            return 0;
        } else if (typeAndOccurance.length == 1) {
            return Integer.valueOf(typeAndOccurance[0].trim().replace("a", "").replace("n", ""));
        } else { // .. is considered to be from 0.
            return 0;
        }
    }

    private static int getMaxLength(String[] typeAndOccurance) {
        if (typeAndOccurance.length == 0) {
            return 0;
        } else if (typeAndOccurance.length == 1) {
            return Integer.valueOf(typeAndOccurance[0].trim().replace("a", "").replace("n", ""));
        } else { // .. is considered to be from 0.
            return Integer.valueOf(typeAndOccurance[1].trim());
        }
    }

    private static String getType(String[] typeAndOccurance) {
        if (typeAndOccurance.length == 0) {
            return "String";
        }

        if (typeAndOccurance[0].trim().equals("n")) {
            return "DABigDecimal";
        } else {
            return "String";
        }
    }

    private static String getValue(BufferedReader reader, String prefix) throws IOException {
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = readUntilValue(reader)) != null)
        {
            line = line.replace("|", "").trim();
            if (line.startsWith(prefix)) {
                result.append(line.replace(prefix, ""));
                line = reader.readLine();
                while (line != null && line.trim().length() != 0) {
                    result.append(line.trim());
                    line = reader.readLine();
                }
                break;
            }
        }
        return result.toString();
    }

    private static String readUntilValue(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        while (line != null && line.length() == 0) {
            line = reader.readLine();
        }
        return line;
    }


    private static void moveToNextPart(BufferedReader reader) throws IOException {
        String currentLine = "";

        while ( currentLine != null && !currentLine.matches(ELEMENT_SEPARATOR)) {
            currentLine = reader.readLine();
        }
    }

    private static LinePart getLinePart(BufferedReader reader, String line) throws IOException {

        LinePart part = null;

        Matcher matcher = WHOLE_DATA_ELEMENT.matcher(line);
        if (matcher.matches()) {
            part = new LinePart(matcher.group(2), matcher.group(3), matcher.group(4), matcher.group(5), matcher.group(6), matcher.group(7));
        } else {
            matcher = FIRST_DATA_ELEMENT_PART.matcher(line);
            if (matcher.matches()) {

                part = new LinePart(matcher.group(2), matcher.group(3));

                line = reader.readLine();
                matcher = SECOND_DATA_ELEMENT_PART.matcher(line);
                if (matcher.matches()) {
                    part.setDescription(part.getDescription() + " " + matcher.group(1));
                    part.setMandatory(matcher.group(2));
                    part.setType(matcher.group(3));
                    part.setMinOccurance(matcher.group(4), matcher.group(5));
                    part.setMaxOccurance(matcher.group(5));
                }
            }
        }

        return part;
    }

    public static Edimap parse(Reader dataReader, Reader compositeReader, Reader segmentReader) throws IOException, EdiParseException {

        Map<String, Component> datas = UnEdifactDefinitionReader.readComponents(dataReader);
        Map<String, Field> composites = UnEdifactDefinitionReader.readFields(compositeReader, datas);
        List<Segment> segments = UnEdifactDefinitionReader.readSegments(segmentReader, composites, datas);

        Edimap edimap = new Edimap();
        edimap.setNamespace("Commonone");
        edimap.setSegments(new SegmentGroup());
        edimap.getSegments().getSegments().addAll(segments);
        return edimap;
    }


    private static class LinePart {
        private String id;
        private String description;
        private String type;
        private Integer minOccurance;
        private Integer maxOccurance;
        private boolean isMandatory;

        public LinePart(String id, String description, String mandatory, String type, String minOccurs, String maxOccurs) {
            this.id = id;
            this.description = description;
            setMandatory(mandatory);
            setType(type);
            setMinOccurance(minOccurs, maxOccurs);
            setMaxOccurance(maxOccurs);
        }

        private void setMandatory(String mandatory) {
            this.isMandatory = mandatory.equalsIgnoreCase("M");
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setMaxOccurance(String maxOccurs) {
            this.maxOccurance = Integer.valueOf(maxOccurs);
        }

        public void setMinOccurance(String minOccurs, String maxOccurs) {
            this.minOccurance = minOccurs.equals("..") ? 0 : Integer.valueOf(maxOccurs);
        }

        public void setType(String type) {
            if (type.equalsIgnoreCase("n")) {
                this.type = "DABigDecimal";
            } else {
                this.type = "String";
            }
        }

        public LinePart(String id, String description) {
            this.id = id;
            this.description = description;
        }

        public String getId() {
            return id;
        }

        public String getDescription() {
            return description;
        }

        public String getType() {
            return type;
        }

        public Integer getMinOccurance() {
            return minOccurance;
        }

        public Integer getMaxOccurance() {
            return maxOccurance;
        }

        public boolean isMandatory() {
            return isMandatory;
        }
    }
}
