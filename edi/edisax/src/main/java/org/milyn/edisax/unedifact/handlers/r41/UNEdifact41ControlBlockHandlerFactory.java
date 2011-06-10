/*
 * Milyn - Copyright (C) 2006 - 2010
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License (version 2.1) as published by the Free Software
 * Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU Lesser General Public License for more details:
 * http://www.gnu.org/licenses/lgpl.txt
 */

package org.milyn.edisax.unedifact.handlers.r41;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.milyn.cdr.SmooksConfigurationException;
import org.milyn.edisax.interchange.ControlBlockHandler;
import org.milyn.edisax.interchange.ControlBlockHandlerFactory;
import org.milyn.edisax.model.EDIConfigDigester;
import org.milyn.edisax.model.internal.Edimap;
import org.milyn.edisax.model.internal.Segment;
import org.milyn.edisax.model.internal.SegmentGroup;
import org.milyn.edisax.unedifact.UNEdifactNamespaceResolver;
import org.milyn.edisax.unedifact.handlers.GenericHandler;
import org.milyn.edisax.unedifact.handlers.UNAHandler;
import org.milyn.edisax.unedifact.handlers.UNBHandler;
import org.milyn.edisax.unedifact.handlers.UNGHandler;
import org.milyn.edisax.unedifact.handlers.UNHHandler;
import org.milyn.namespace.NamespaceResolver;
import org.milyn.xml.hierarchy.HierarchyChangeListener;
import org.xml.sax.SAXException;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;

/**
 * UN/EDIFACT control block handler factory (Version 4, Release 1).
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class UNEdifact41ControlBlockHandlerFactory implements ControlBlockHandlerFactory {

    private static Log logger = LogFactory.getLog(UNBHandler.class);

    public static final String NAMESPACE = UNEdifactNamespaceResolver.NAMESPACE_ROOT + ".v41";

    private static Segment unbSegment;
    private static Segment unzSegment;
    private static Segment ungSegment;
    private static Segment uneSegment;
    private static Segment unhSegment;
    private static Segment untSegment;
    private static HashMap<String,Charset> toCharsetMapping;

    private HierarchyChangeListener hierarchyChangeListener;

    public UNEdifact41ControlBlockHandlerFactory(HierarchyChangeListener hierarchyChangeListener) {
        this.hierarchyChangeListener = hierarchyChangeListener;
    }

    public String getNamespace() {
        return NAMESPACE;
    }

    public NamespaceResolver newNamespaceResolver() {
        return new41NamespaceResolver();
    }

    public static NamespaceResolver new41NamespaceResolver() {
        return new UNEdifactNamespaceResolver(NAMESPACE);
    }

    public ControlBlockHandler getControlBlockHandler(String segCode) throws SAXException {

        if(segCode.equals("UNH")) {
            return new UNHHandler(unhSegment, untSegment, hierarchyChangeListener);
        } else if(segCode.equals("UNG")) {
            return new UNGHandler(ungSegment, uneSegment);
        } else if(segCode.equals("UNA")) {
            return new UNAHandler();
        } else if(segCode.equals("UNB")) {
            return new UNBHandler(unbSegment, unzSegment, toCharsetMapping);
        } else if(segCode.charAt(0) == 'U') {
            return new GenericHandler();
        }

        throw new SAXException("Unknown/Unexpected UN/EDIFACT control block segment code '" + segCode + "'.");
    }

    static {
        try {
            Edimap controlBlockSegments = EDIConfigDigester.digestConfig(UNEdifact41ControlBlockHandlerFactory.class.getResourceAsStream("v41-segments.xml"));
            List<SegmentGroup> segments = controlBlockSegments.getSegments().getSegments();
            for(SegmentGroup segment : segments) {
                if(segment.getSegcode().equals("UNB")) {
                    unbSegment = (Segment) segment;
                } else if(segment.getSegcode().equals("UNZ")) {
                    unzSegment = (Segment) segment;
                } else if(segment.getSegcode().equals("UNG")) {
                    ungSegment = (Segment) segment;
                } else if(segment.getSegcode().equals("UNE")) {
                    uneSegment = (Segment) segment;
                } else if(segment.getSegcode().equals("UNH")) {
                    unhSegment = (Segment) segment;
                } else if(segment.getSegcode().equals("UNT")) {
                    untSegment = (Segment) segment;
                }
            }
        } catch (Exception e) {
            throw new SmooksConfigurationException("Unexpected exception reading UN/EDIFACT v4.1 segment definitions.", e);
        }

        toCharsetMapping = new HashMap<String, Charset>();

        // http://www.gefeg.com/jswg/cl/v41/40107/cl1.htm
        addCharsetMapping("UNOA", "ASCII");
        addCharsetMapping("UNOB", "ASCII");
        addCharsetMapping("UNOC", "ISO8859-1");
        addCharsetMapping("UNOD", "ISO8859-2");
        addCharsetMapping("UNOE", "ISO8859-5");
        addCharsetMapping("UNOF", "ISO8859-7");
        addCharsetMapping("UNOG", "ISO8859-3");
        addCharsetMapping("UNOH", "ISO8859-4");
        addCharsetMapping("UNOI", "ISO8859-6");
        addCharsetMapping("UNOJ", "ISO8859-8");
        addCharsetMapping("UNOK", "ISO8859-9");
        addCharsetMapping("UNOL", "ISO8859-15");
        addCharsetMapping("UNOW", "UTF-8");
        addCharsetMapping("UNOX", "ISO-2022-CN");
        addCharsetMapping("UNOY", "UTF-8");

        // http://www.gefeg.com/jswg/cl/v41/40107/cl17.htm
        addCharsetMapping("1", "ASCII");
        addCharsetMapping("2", "ASCII");
        addCharsetMapping("3", "IBM500");
        addCharsetMapping("4", "IBM850");
        addCharsetMapping("5", "UTF-16");
        addCharsetMapping("6", "UTF-32");
        addCharsetMapping("7", "UTF-8");
        addCharsetMapping("8", "UTF-16");
    }

    private static void addCharsetMapping(String code, String charsetName) {
        if(Charset.isSupported(charsetName)) {
            toCharsetMapping.put(code, Charset.forName(charsetName));
        } else {
            logger.debug("Unsupported character set '" + charsetName + "'.  Cannot support for '" + code + "' if defined on the syntaxIdentifier field on the UNB segment.  Check the JVM version etc.");
        }
    }
}
