package org.milyn.edisax.unedifact;

import org.milyn.cdr.SmooksConfigurationException;
import org.milyn.edisax.EDIContentHandler;
import org.milyn.edisax.interchange.InterchangeContext;
import org.milyn.edisax.model.EDIConfigDigester;
import org.milyn.edisax.model.EdifactModel;
import org.milyn.edisax.model.internal.Delimiters;
import org.milyn.edisax.model.internal.Edimap;
import org.milyn.edisax.model.internal.SegmentGroup;
import org.milyn.edisax.registry.MappingsRegistry;
import org.milyn.edisax.unedifact.handlers.r41.UNEdifact41ControlBlockHandlerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;

/**
 * Converts a XML document back to UN/Edifact interchange.
 */
public class EdifactInterchangeContentHandler extends EDIContentHandler {
    private static final Edimap UN_EDIFACT_V41;
    static {
        try {

            // Wrap UNH inside an interchangeMessage element
            Edimap edimap = EDIConfigDigester
                    .digestConfig(UNEdifact41ControlBlockHandlerFactory.class.getResourceAsStream("v41-segments.xml"));
            List<SegmentGroup> segments = edimap.getSegments().getSegments();
            SegmentGroup unbSegment = null;
            SegmentGroup ungSegment = null;
            SegmentGroup unhSegment = null;
            SegmentGroup untSegment = null;
            SegmentGroup uneSegment = null;
            SegmentGroup unzSegment = null;
            for(SegmentGroup segment : segments) {
                if(segment.getSegcode().equals("UNB")) {
                    unbSegment = segment;
                } else if(segment.getSegcode().equals("UNZ")) {
                    unzSegment = segment;
                } else if(segment.getSegcode().equals("UNG")) {
                    ungSegment = segment;
                } else if(segment.getSegcode().equals("UNE")) {
                    uneSegment = segment;
                } else if(segment.getSegcode().equals("UNH")) {
                    unhSegment = segment;
                } else if(segment.getSegcode().equals("UNT")) {
                    untSegment = segment;
                }
            }

            // There is no need to adjust parent as EdimapVisitor uses an internal stack to track it
            SegmentGroup interchangeMessage = new SegmentGroup();
            interchangeMessage.setXmltag(InterchangeContext.INTERCHANGE_MESSAGE_BLOCK_ELEMENT_NAME);
            interchangeMessage.setMaxOccurs(-1);
            interchangeMessage.getSegments().add(unhSegment);
            interchangeMessage.getSegments().add(untSegment);

            SegmentGroup group = new SegmentGroup();
            group.setParent(edimap.getSegments());
            group.setXmltag("group");
            group.setMaxOccurs(-1);
            group.getSegments().add(ungSegment);
            group.getSegments().add(interchangeMessage);
            group.getSegments().add(uneSegment);

            segments.clear();
            segments.add(unbSegment);
            segments.add(group);
            segments.add(interchangeMessage);
            segments.add(unzSegment);

            UN_EDIFACT_V41 = edimap;
        } catch (Exception e) {
            throw new SmooksConfigurationException("Unexpected exception reading UN/EDIFACT v4.1 segment definitions.", e);
        }
    }

    private final MappingsRegistry registry;
    private String messageName;
    private EdimapVisitor nestedVisitor;
    private int depth;
    private int messageIdOffset;
    private String genericSegcode;


    public EdifactInterchangeContentHandler(MappingsRegistry registry, StringBuilder result) {
        this(registry, UN_EDIFACT_V41.getDelimiters(), result);
    }

    @SuppressWarnings("WeakerAccess")
    public EdifactInterchangeContentHandler(MappingsRegistry registry, Delimiters delimiters, StringBuilder result) {
        super(UN_EDIFACT_V41, delimiters, result);
        this.registry = registry;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        if (nestedVisitor == null) {
            if (depth == 1
                    && localName.length() == 3
                    && !localName.equals("UNB")
                    && !localName.equals("UNZ")
                    && localName.startsWith("U")) {
                genericSegcode = localName;
            } else {
                super.startElement(uri, localName, qName, atts);
                if (localName.equals("messageIdentifier")) {
                    messageIdOffset = getResult().length();
                }
            }
        } else {
            nestedVisitor.open(localName);
        }
        depth++;
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        depth--;
        if (genericSegcode != null && genericSegcode.equals(localName)) {
            getResult().append(getDelimiters().getSegment());
            genericSegcode = null;
        } else if (nestedVisitor == null) {
            super.endElement(uri, localName, qName);
            if (localName.equals("messageIdentifier")) {
                messageName = getResult().substring(messageIdOffset);
            } else if (localName.equals("UNH")) {
                try {
                    EdifactModel model = registry.getMappingModel(messageName, getDelimiters());
                    nestedVisitor = new EdimapVisitor(model.getEdimap(), getDelimiters(), getResult());
                } catch (IOException e) {
                    throw new SAXException(e.getMessage(), e);
                }
            }
        } else {
            if (nestedVisitor.close(localName)) {
                nestedVisitor = null;
                messageName = null;
                messageIdOffset = -1;
            }
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (genericSegcode != null) {
            appendText(ch, start, length);
        } else if (nestedVisitor == null) {
            super.characters(ch, start, length);
        } else if (nestedVisitor.isText()) {
            appendText(ch, start, length);
        }
    }
}
