package org.milyn.edisax;

import org.milyn.edisax.model.EdifactModel;
import org.milyn.edisax.model.internal.Component;
import org.milyn.edisax.model.internal.Delimiters;
import org.milyn.edisax.model.internal.Edimap;
import org.milyn.edisax.model.internal.Field;
import org.milyn.edisax.model.internal.MappingNode;
import org.milyn.edisax.model.internal.Segment;
import org.milyn.edisax.model.internal.SegmentGroup;
import org.milyn.edisax.model.internal.SubComponent;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import java.util.Collections;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Stack;

/**
 * Content handler that converts a XML document back to EDI.
 */
public class EDIContentHandler implements ContentHandler {
    private final Edimap edifactModel;
    private final Delimiters delimiters;

    private final StringBuilder result;
    private EdimapVisitor visitor;

    @SuppressWarnings("WeakerAccess")
    public EDIContentHandler(EdifactModel edifactModel, StringBuilder result) {
        this(edifactModel.getEdimap(), edifactModel.getDelimiters(), result);
    }
    public EDIContentHandler(Edimap edifactModel, Delimiters delimiters, StringBuilder result) {
        this.edifactModel = edifactModel;
        this.delimiters = delimiters;
        this.result = result;
    }

    protected Delimiters getDelimiters() {
        return delimiters;
    }

    protected StringBuilder getResult() {
        return result;
    }

    @Override
    public void setDocumentLocator(Locator locator) {

    }

    @Override
    public void startDocument() throws SAXException {
        visitor = new EdimapVisitor(edifactModel, delimiters, result);
    }

    @Override
    public void endDocument() throws SAXException {

    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {

    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {

    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        visitor.open(localName);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        visitor.close(localName);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (visitor.isText()) {
            appendText(ch, start, length);
        }
    }

    protected void appendText(char[] ch, int start, int length) {
        String text = new String(ch, start, length);
        text = delimiters.escape(text);
        result.append(text);
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {

    }

    @Override
    public void processingInstruction(String target, String data) throws SAXException {

    }

    @Override
    public void skippedEntity(String name) throws SAXException {

    }

    protected static class EdimapVisitor {
        private final Edimap edimap;
        private final Delimiters delimiters;
        private final StringBuilder result;
        private Stack<GroupIterator> groups;
        private SegmentGroup currentGroup;
        private Segment currentSegment;
        private ListIterator<Field> fields;
        private Field currentField;
        private ListIterator<Component> components;
        private Component currentComponent;
        private ListIterator<SubComponent> subComponents;
        private SubComponent currentSubComponent;

        public EdimapVisitor(Edimap edimap, Delimiters delimiters, StringBuilder result) {
            this.edimap = edimap;
            this.delimiters = delimiters;
            this.result = result;
            groups = new Stack<GroupIterator>();
        }

        public void open(String xmlTag) {
            if (fields == null) {
                nextGroup(xmlTag);
            } else if (currentField == null) {
                MappingNode nextField = nextField(xmlTag);

                // If a field cannot be found try a nested segment
                if (nextField == null) {
                    nextGroup(xmlTag);
                }
            } else if (currentComponent == null) {
                nextComponent(xmlTag);
            } else if (currentSubComponent == null) {
                nextSubComponent(xmlTag);
            } else {
                throw new IllegalArgumentException("Unknown XML element " + xmlTag);
            }
        }

        public boolean close(String xmlTag) {
            if (currentSubComponent != null) {
                assert currentSubComponent.getXmltag().equals(xmlTag);
                closeSubComponent();
            } else if (currentComponent != null) {
                assert currentComponent.getXmltag().equals(xmlTag);
                closeComponent();
            } else if (currentField != null) {
                assert currentField.getXmltag().equals(xmlTag);
                closeField();
            } else if (currentGroup != null) {
                assert currentSegment == null || currentGroup == currentSegment;
                assert currentGroup.getXmltag().equals(xmlTag) : currentGroup.getXmltag() + " vs " + xmlTag;
                closeGroup();
                return currentGroup == null;
            } else {
                throw new IllegalArgumentException("Unknown XML element " + xmlTag);
            }
            return false;
        }

        public boolean isText() {
            if (currentField == null) {
                return false;
            }

            // Inside single field without components
            if (components == null) {
                return true;
            }

            // Inside component without subcomponents
            if (currentComponent != null && subComponents == null) {
                return true;
            }
            return currentSubComponent != null;
        }

        private void nextGroup(String xmlTag) {
            if (groups.isEmpty()) {
                if (edimap.getSegments().getXmltag().equals(xmlTag)) {
                    openGroup(edimap.getSegments());
                    return;
                }
                throw new IllegalArgumentException("No more segment groups for " + path(xmlTag) + ":\n" + result
                        .toString());
            }
            GroupIterator iterator = groups.peek();
            SegmentGroup group = iterator.next(xmlTag);
            if (group == null) {
                throw new IllegalArgumentException("No more segment groups for " + path(xmlTag) + ":\n" + result
                        .toString());
            } else {
                openGroup(group);
            }
        }

        private MappingNode nextField(String xmlTag) {

            // TODO limit repetition to max occurs of the field
            if (fields.hasPrevious()) {
                Field previous = fields.previous();
                fields.next();
                if (previous.getXmltag().equals(xmlTag)) {
                    String delimiter = delimiters.getFieldRepeat();
                    result.append(delimiter == null ? delimiters.getField() : delimiter);
                    openField(previous);
                    return previous;
                }
            }
            int offset = result.length();
            while (fields.hasNext()) {
                result.append(delimiters.getField());
                Field current = fields.next();
                if (current.getXmltag().equals(xmlTag)) {
                    openField(current);
                    return current;
                }
            }
            if (currentSegment.isTruncatable()) {
                result.setLength(offset);
            }
            return null;
        }

        private void nextComponent(String xmlTag) {

            // TODO limit repetition to max occurs of the component
            if (components.hasPrevious()) {
                Component previous = components.previous();
                components.next();
                if (previous.getXmltag().equals(xmlTag)) {
                    result.append(delimiters.getComponent());
                    openComponent(previous);
                    return;
                }
            }

            while (components.hasNext()) {

                // Skip component delimiter for the first component of a composite as it's already delimited by a field
                // delimiter
                if (components.hasPrevious()) {
                    result.append(delimiters.getComponent());
                }
                Component current = components.next();
                if (current.getXmltag().equals(xmlTag)) {
                    openComponent(current);
                    return;
                }
            }
        }

        private void nextSubComponent(String xmlTag) {
            while (subComponents.hasNext()) {

                // Skip subcomponent delimiter for the first component of a composite as it's already delimited by a field
                // delimiter
                if (subComponents.hasPrevious()) {
                    result.append(delimiters.getSubComponent());
                }
                SubComponent current = subComponents.next();
                if (current.getXmltag().equals(xmlTag)) {
                    openSubComponent(current);
                    return;
                }
            }
        }

        private void openGroup(SegmentGroup current) {
            if (current instanceof Segment) {
                openSegment((Segment) current);
            } else {
                currentSegment = null;
            }
            currentGroup = current;
            groups.push(new GroupIterator(current));
            assert currentSegment == null || currentGroup == currentSegment;

            if (current.getXmltag() == null && !current.getSegments().isEmpty()) {
                openGroup(current.getSegments().get(0));
            }
        }

        private void openSegment(Segment current) {

            // Because segments can be nested
            assert fields == null || !currentSegment.getSegments().isEmpty();

            // When opening the first nested segment add delimiter for the parent
            GroupIterator parent = groups.peek();
            if (parent.close()) {
                result.append(delimiters.getSegment());
            }
            currentSegment = current;
            fields = current.getFields().listIterator();
            result.append(current.getSegcode());
        }

        private void openField(Field current) {
            currentField = current;
            if (!current.getComponents().isEmpty()) {
                components = current.getComponents().listIterator();
            }
        }

        private void openComponent(Component current) {
            currentComponent = current;
            if (!current.getSubComponents().isEmpty()) {
                subComponents = current.getSubComponents().listIterator();
            }
        }

        private void openSubComponent(SubComponent current) {
            currentSubComponent = current;
        }

        private void closeGroup() {
            assert currentGroup != null;
            assert currentSegment == null || currentGroup == currentSegment;
            if (currentGroup == currentSegment) {
                closeSegment();
            } else {
                groups.pop();
            }
            if (groups.isEmpty()) {
                currentSegment = null;
                currentGroup = null;
            } else {
                GroupIterator current = groups.peek();
                if (current.parent instanceof Segment) {
                    currentSegment = (Segment) current.parent;
                } else {
                    currentSegment = null;
                }
                currentGroup = current.parent;
                if (currentGroup.getXmltag() == null && !current.groups.hasNext()) {
                    closeGroup();
                }
            }
            assert currentSegment == null || currentGroup == currentSegment;
        }

        private void closeSegment() {
            assert currentGroup != null;
            assert currentSegment != null;
            assert !currentSegment.getSegments().isEmpty() || fields != null;
            assert currentField == null;
            assert components == null;
            assert currentComponent == null;
            assert subComponents == null;
            assert currentSubComponent == null;

            // For segment with nested inner segments the delimiter is added when the inner segment is opened
            GroupIterator current = groups.pop();
            if (current.close()) {
                result.append(delimiters.getSegment());
            }
            fields = null;
        }

        private void closeField() {
            assert currentGroup != null;
            assert currentSegment != null;
            assert fields != null;
            assert currentField != null;
            assert components == null || !currentField.getComponents().isEmpty();
            assert currentComponent == null;
            assert subComponents == null;
            assert currentSubComponent == null;
            currentField = null;
            components = null;
        }

        private void closeComponent() {
            assert currentGroup != null;
            assert currentSegment != null;
            assert fields != null;
            assert currentField != null;
            assert components != null;
            assert currentComponent != null;
            assert currentSubComponent == null;
            currentComponent = null;
            subComponents = null;
        }

        private void closeSubComponent() {
            assert currentGroup != null;
            assert currentSegment != null;
            assert fields != null;
            assert currentField != null;
            assert components != null;
            assert currentComponent != null;
            assert currentSubComponent != null;
            currentSubComponent = null;
        }

        String path(String xmlTag) {
            StringBuilder path = new StringBuilder();
            for (GroupIterator i : groups) {
                if (i.parent == null) {
                    path.append(" ROOT ");
                } else {
                    path.append(i.parent.getXmltag());
                }
                path.append(" / ");
            }
            path.append(xmlTag);
            return path.toString();
        }
    }

    static class GroupIterator {
        final SegmentGroup parent;
        final Iterator<SegmentGroup> groups;
        SegmentGroup current;
        int count;
        boolean open;

        GroupIterator(Edimap edimap) {
            parent = null;
            groups = Collections.singletonList(edimap.getSegments()).iterator();
            open = false;
        }

        GroupIterator(SegmentGroup group) {
            parent = group;
            groups = group.getSegments().iterator();
            count = 0;
            open = group instanceof Segment;
        }

        SegmentGroup next(String xmlTag) {
            if (current != null
                    && (current.getMaxOccurs() == -1 || count < current.getMaxOccurs())
                    && matchesXmlTag(xmlTag)) {
                count++;
                return current;
            }
            while (groups.hasNext()) {
                current = groups.next();
                if (matchesXmlTag(xmlTag)) {
                    count = 1;
                    return current;
                }
            }
            return null;
        }

        private boolean matchesXmlTag(String xmlTag) {

            // Support optional segment groups without an XML tag
            if (current.getXmltag() == null && !current.getSegments().isEmpty()) {
                SegmentGroup firstChild = current.getSegments().get(0);
                return firstChild.getXmltag().equals(xmlTag);
            }
            return current.getXmltag().equals(xmlTag);
        }

        boolean close() {
            boolean previous = open;
            open = false;
            return previous;
        }
    }
}
