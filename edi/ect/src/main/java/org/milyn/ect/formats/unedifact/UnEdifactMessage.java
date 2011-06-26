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

import org.milyn.ect.EdiParseException;
import org.milyn.ect.common.XmlTagEncoder;
import org.milyn.edisax.interchange.ControlBlockHandlerFactory;
import org.milyn.edisax.model.internal.Delimiters;
import org.milyn.edisax.model.internal.Description;
import org.milyn.edisax.model.internal.Edimap;
import org.milyn.edisax.model.internal.Import;
import org.milyn.edisax.model.internal.Segment;
import org.milyn.edisax.model.internal.SegmentGroup;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * UnEdifactMessage
 * @author bardl
 */
public class UnEdifactMessage {

    /**
     * Marks the start of the Message Definition section.
     */
    private static final String MESSAGE_DEFINITION = "[\\d\\. ]*MESSAGE DEFINITION *";

    /**
     * Extracts description from start of segment documentation.
     * Group1 = id
     * Group2 = documentation
     */
    private static final String MESSAGE_DEFINITION_START = "^(\\d{4} *| *)[- \\*\\+\\|X]*(([A-Z]{3}),|[S|s]egment [G|g]roup \\d*:)+(.*)";

    /**
     * Marks the end of the Message Definition section.
     */
    private static final String MESSAGE_DEFINITION_END = "([\\d\\.]* *(Data)? *[S|s]egment [I|i]ndex.*)|( *[\\d\\.]+ *[M|m]essage [S|s]tructure.*)";

    /**
     * Extracts the value for Message type, version, release and agency.
     */
    private static final Pattern MESSAGE_TYPE = Pattern.compile(".*Message Type *: *(\\w*) *");
    private static final Pattern MESSAGE_RELEASE = Pattern.compile(".*Release *: *(\\w*) *");
    private static final Pattern MESSAGE_AGENCY = Pattern.compile(".*Contr. Agency *: *(\\w*) *");
    private static final Pattern MESSAGE_VERSION = Pattern.compile(".*Version *: *(\\w*) *");

    /**
     * Marks the start of the Segment table section.
     */
    private static final String SEGMENT_TABLE = "[\\d\\. ]*[S|s]egment [T|t]able *";
    private static final String SEGMENT_TABLE_HEADER = "(Pos *Tag *Name *S *R.*)|( *TAG *NAME *S *REPT *S *REPT)|( *POS *TAG *NAME *S *R *)";
//"(Pos *Tag *Name *S *R.*)|( *TAG *NAME *S *REPT *S *REPT)";


    /**
     * Extracts information from Regular segment definition.
     * Group1 = id
     * Group2 = segcode
     * Group3 = description
     * Group4 = isMandatory
     * Group5 = max occurance
     */                  
    private static String SEGMENT_REGULAR = "(\\d{4})*[-\\+\\* XS]*(\\w{3}) *(.*) +(M|C|m|c) *(\\d+)[ \\|]*";

    /**
     * Extracts information from Regular segment definition.
     * Group1 = id
     * Group2 = segcode
     * Group3 = description
     */
    private static String SEGMENT_REGULAR_START = "(\\d{4})*[-\\+\\* XS]*(\\w{3}) *(.*) *\\|";

    /**
     * Extracts information from Regular segment definition.
     * Group1 = description
     * Group2 = isMandatory
     * Group3 = max occurance
     */
    private static String SEGMENT_REGULAR_END = " *(.*) +(M|C|m|c) *(\\d*)[ \\|]*";


    /**
     * Matches and extracts information from start of segment group.
     * Group1 = id
     * Group2 = name
     * Group4 = isMandatory
     * Group5 = max occurance 
     */
    private static String SEGMENT_GROUP_START = "(\\d{4})*[-\\+\\* XS]*-* *([S|s]egment [G|g]roup \\d*) *-* +(C|M|c|m) *(\\d*)[ \\-\\+\\|]*";

    /**
     * Matches and extracts information from segment at end of segment group.
     * Group1 = id
     * Group2 = segcode
     * Group3 = description
     * Group4 = isMandatory
     * Group5 = max occurance
     * Group6 = nrOfClosedGroups
     */
    private static String SEGMENT_GROUP_END = "(\\d{4})*[-\\+\\* XS]*(\\w{3}) *([\\w /-]*) +(C|M|c|m) *(\\d*) *-+([ |\\+]*)";

    /**
     * Annex - notes after message structure.
     */
    private static final String ANNEX = "(Informative annex:.*)";

    /**
     * Newline character applied between documentation lines.
     */
    private static final String NEW_LINE = "\n";

    /**
     * A message must match the LEGAL_MESSAGE pattern. Otherwise it may be an index file located in the message folder.
     */
    private static final String LEGAL_MESSAGE = "\\s*UN/EDIFACT\\s*";
    /**
     * Default settings for UN/EDIFACT.
     */
    private static final String DELIMITER_SEGMENT = "&#39;!$";
    private static final String DELIMITER_COMPOSITE = "+";
    private static final String DELIMITER_DATA = ":";
    private static final String DELIMITER_NOT_USED = "~";

    private static final String ESCAPE = "?";

    private static List<String> ignoreSegments = Arrays.asList("UNA", "UNB", "UNG", "UNH", "UNT", "UNZ", "UNE");
    private String type;
    private String version;
    private String release;
    private String agency;
    private Edimap edimap;

    public UnEdifactMessage(Reader reader, boolean isSplitIntoImport, boolean useShortName, Edimap definitionModel) throws EdiParseException, IOException {

        BufferedReader breader = null;
        try {

            breader = new BufferedReader(reader);

            assertLegalMessage(breader);

            type = getValue(breader, MESSAGE_TYPE);
            version = getValue(breader, MESSAGE_VERSION);
            release = getValue(breader, MESSAGE_RELEASE);
            agency = getValue(breader, MESSAGE_AGENCY);

            edimap = new Edimap();
            SegmentGroup rootGroup = new SegmentGroup();
            rootGroup.setXmltag(XmlTagEncoder.encode(type));
            edimap.setSegments(rootGroup);

            Delimiters delimiters = new Delimiters();
            delimiters.setSegment(DELIMITER_SEGMENT);
            delimiters.setField(DELIMITER_COMPOSITE);
            delimiters.setComponent(DELIMITER_DATA);
            delimiters.setSubComponent(DELIMITER_NOT_USED);
            delimiters.setEscape(ESCAPE);
            edimap.setDelimiters(delimiters);

            edimap.setDescription(new Description());
            edimap.getDescription().setName(type);
            edimap.getDescription().setVersion(version + ":" + release + ":" + agency);
            edimap.getDescription().setNamespace(ControlBlockHandlerFactory.NAMESPACE_ROOT + ":" + agency.toLowerCase() + ":" + version.toLowerCase() + release.toLowerCase() + ":" + type.toLowerCase());

            Map<String, Segment> segmentDefinitions = null;
            if (isSplitIntoImport) {
                Import ediImport = new Import();
                ediImport.setNamespace(agency);
                ediImport.setResource(definitionModel.getDescription().getName() + ".xml");  // TODO: Review with B�rd
                edimap.getImports().add(ediImport);
            }  else {
                segmentDefinitions = getSegmentDefinitions(definitionModel);
            }


            Map<String, String> definitions = parseMessageDefinition(breader);


            parseMessageStructure(breader, rootGroup, definitions, isSplitIntoImport, useShortName, segmentDefinitions);

        } finally {
            if (breader != null) {
                breader.close();
            }
        }
    }

    public Edimap getEdimap() {
        return edimap;
    }

    public String getType() {
        return type;
    }

    public String getVersion() {
        return version;
    }

    public String getRelease() {
        return release;
    }

    public String getAgency() {
        return agency;
    }

    private Map<String, Segment> getSegmentDefinitions(Edimap definitionModel) {
        Map<String, Segment> result = new HashMap<String, Segment>();
        for (SegmentGroup segmentGroup : definitionModel.getSegments().getSegments()) {
            result.put(segmentGroup.getSegcode(), (Segment)segmentGroup);
        }
        return result;
    }

    private void assertLegalMessage(BufferedReader reader) throws EdiParseException {
        String line;

        try {
            line = reader.readLine();
            while (line != null && !line.matches(LEGAL_MESSAGE)) {
                line = reader.readLine();
            }
        } catch (IOException e) {
            throw new EdiParseException("Error reading first line of UN/EDIFACT message.", e);
        }

        if(line == null) {
            throw new EdiParseException("Not a valid UN/EDIFACT message definition. First line doe not match pattern '" + LEGAL_MESSAGE + "'.");
        }
    }

    private void parseMessageStructure(BufferedReader reader, SegmentGroup group, Map<String, String> definitions, boolean isSplitIntoImport, boolean useShortName, Map<String, Segment> segmentDefinitions) throws IOException {
        String line = reader.readLine();
        while (!line.matches(SEGMENT_TABLE)) {
            line = reader.readLine();
        }

        while (!line.matches(SEGMENT_TABLE_HEADER)) {
            line = reader.readLine();
        }
        parseNextSegment(reader, group, definitions, isSplitIntoImport, useShortName, segmentDefinitions, new LineNumber());
    }

    private Map<String, String> parseMessageDefinition(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        while (!line.toUpperCase().matches(MESSAGE_DEFINITION)) {
            line = reader.readLine();
        }

        line = reader.readLine();
        while (!line.toUpperCase().matches(MESSAGE_DEFINITION)) {
            line = reader.readLine();
        }

        line = reader.readLine();
        while (!line.matches(MESSAGE_DEFINITION_START) || line.length() == 0) {
            line = reader.readLine();
        }


        LineNumber lineNo = new LineNumber();
        Map<String, String> definitions = new HashMap<String, String>();
        while (!line.matches(MESSAGE_DEFINITION_END)) {
            if (line.matches(MESSAGE_DEFINITION_START)) {
                Pattern pattern = Pattern.compile(MESSAGE_DEFINITION_START);
                Matcher matcher = pattern.matcher(line);
                matcher.matches();

                String id = getLineId(lineNo, matcher.group(1));

                StringBuilder definition = new StringBuilder();
                definition.append(matcher.group(2)).append(NEW_LINE);
                line = reader.readLine();

                while (!line.matches(MESSAGE_DEFINITION_START) && !line.matches(MESSAGE_DEFINITION_END)) {
                    definition.append(line).append(NEW_LINE);
                    line = reader.readLine();
                }
                definitions.put(id, definition.toString());
            } else {
                line = reader.readLine();
            }

        }
        return definitions;
    }

    private int parseNextSegment(BufferedReader reader, SegmentGroup parentGroup, Map<String, String> definitions, boolean isSplitIntoImport, boolean useShortName, Map<String, Segment> segmentDefinitions, LineNumber lineNo) throws IOException {
        String line = reader.readLine();
        while (line != null) {
            if (line.matches(SEGMENT_GROUP_START)) {
                Matcher matcher = Pattern.compile(SEGMENT_GROUP_START).matcher(line);
                matcher.matches();
                String id = getLineId(lineNo, matcher.group(1));
                SegmentGroup group = createGroup(id, matcher.group(2), matcher.group(3), matcher.group(4), definitions);
                parentGroup.getSegments().add(group);

                int result = parseNextSegment(reader, group, definitions, isSplitIntoImport, useShortName, segmentDefinitions, lineNo);
                if (result != 0) {
                    return result - 1;
                }

            } else if (line.matches(SEGMENT_GROUP_END)) {
                Matcher matcher = Pattern.compile(SEGMENT_GROUP_END).matcher(line);
                matcher.matches();
                String id = getLineId(lineNo, matcher.group(1));
                Segment segment = createSegment(id, matcher.group(2), matcher.group(3), matcher.group(4), matcher.group(5), definitions, isSplitIntoImport, useShortName, segmentDefinitions);
                parentGroup.getSegments().add(segment);
                return extractPlusCharacter(matcher.group(6)).length() - 1;
            } else if (line.matches(SEGMENT_REGULAR)) {
                Matcher matcher = Pattern.compile(SEGMENT_REGULAR).matcher(line);
                matcher.matches();
                String id = getLineId(lineNo, matcher.group(1));
                if (!ignoreSegments.contains(matcher.group(2))) {
                    Segment segment = createSegment(id, matcher.group(2), matcher.group(3), matcher.group(4), matcher.group(5), definitions, isSplitIntoImport, useShortName, segmentDefinitions);
                    parentGroup.getSegments().add(segment);
                }
            } else if (!line.trim().equals("") && line.matches(SEGMENT_REGULAR_START)) {
                Matcher matcher = Pattern.compile(SEGMENT_REGULAR_START).matcher(line);
                matcher.matches();
                String id = getLineId(lineNo, matcher.group(1));
                String segcode = matcher.group(2);
                String description = matcher.group(3);
                line = reader.readLine();
                matcher = Pattern.compile(SEGMENT_REGULAR_END).matcher(line);
                matcher.matches();
                description += " " + matcher.group(1);
                if (!ignoreSegments.contains(matcher.group(2))) {
                    Segment segment = createSegment(id, segcode, description, matcher.group(2), matcher.group(3), definitions, isSplitIntoImport, useShortName, segmentDefinitions);
                    parentGroup.getSegments().add(segment);
                }
            } else if (line.matches(ANNEX)) {
                return 0;
            }
            
            line = reader.readLine();
        }
        return 0;
    }

    private String getLineId(UnEdifactMessage.LineNumber lineNo, String id) {
        if (id == null || id.trim().equals("")) {
            id = String.valueOf(lineNo.increment());
        }
        return id.trim();
    }

    private String extractPlusCharacter(String value) {
        return value.replaceAll("[^\\+]", "");
    }

    private SegmentGroup createGroup(String id, String name, String mandatory, String maxOccurance, Map<String, String> definitions) {
        SegmentGroup group = new SegmentGroup();
        group.setXmltag(XmlTagEncoder.encode(name.trim()));
        String test = definitions.get(id);
        
        group.setDocumentation(test.trim());
        group.setMinOccurs(mandatory.equals("M") ? 1 : 0);
        group.setMaxOccurs(Integer.valueOf(maxOccurance));
        return group;
    }

    private Segment createSegment(String id, String segcode, String name, String mandatory, String maxOccurance, Map<String, String> definitions, boolean isSplitIntoImport, boolean useShortName, Map<String, Segment> segmentDefinitions) {
        Segment segment = new Segment();

        name = name.trim();

        segment.setName(name);
        segment.setSegcode(segcode);
        segment.setNodeTypeRef(agency + ":" + segcode);

        if (!isSplitIntoImport) {
            Segment importedSegment = segmentDefinitions.get(segcode);

            if(importedSegment == null) {
                throw new EdiParseException("Unknown segment code '" + segcode + "'.");
            }

            segment.getFields().addAll(importedSegment.getFields());

            if (importedSegment.getSegments().size() > 0) {
                segment.getSegments().addAll(importedSegment.getSegments());
            }
        }

        if (useShortName) {
            segment.setXmltag(segcode);
        } else {
            segment.setXmltag(XmlTagEncoder.encode(name.trim()));
        }

        segment.setDocumentation(definitions.get(id).trim());
        segment.setMinOccurs(mandatory.equals("M") ? 1 : 0);
        segment.setMaxOccurs(Integer.valueOf(maxOccurance));
        segment.setTruncatable(true);

        return segment;
    }

    private String getValue(BufferedReader reader, Pattern pattern) throws IOException {
        String line = reader.readLine();
        Matcher matcher = pattern.matcher(line);
        while (!matcher.matches()) {
            line = reader.readLine();
            matcher = pattern.matcher(line);
        }
        return matcher.group(1);
    }

    private class LineNumber {
        private int value = 0;

        public int getValue() {
            return value;
        }

        public int increment() {
            return value++;
        }
    }
}
