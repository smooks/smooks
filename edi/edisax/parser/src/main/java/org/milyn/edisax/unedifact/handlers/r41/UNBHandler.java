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
package org.milyn.edisax.unedifact.handlers.r41;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.milyn.edisax.BufferedSegmentReader;
import org.milyn.edisax.EDIParseException;
import org.milyn.edisax.util.EDIUtils;
import org.milyn.edisax.interchange.ControlBlockHandler;
import org.milyn.edisax.interchange.InterchangeContext;
import org.milyn.edisax.model.internal.Component;
import org.milyn.edisax.model.internal.Field;
import org.milyn.edisax.model.internal.Segment;
import org.xml.sax.SAXException;

/**
 * UNB Segment Handler.
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
class UNBHandler implements ControlBlockHandler {

	private static Log logger = LogFactory.getLog(UNBHandler.class);
	
	private static Segment unbSegment;
	private static Segment unzSegment;
	private static Map<String, Charset> toCharsetMapping;
	
	static {
		createSegmentsDefs();
		createRepertoireToCharsetMap();
	}

	public void process(InterchangeContext interchangeContext) throws IOException, SAXException {
		BufferedSegmentReader segmentReader = interchangeContext.getSegmentReader();
		
		segmentReader.moveToNextSegment(false);
		
		String[] fields = segmentReader.getCurrentSegmentFields();
		
		interchangeContext.mapControlSegment(unbSegment, true);
		
		String[] syntaxIdComponents = EDIUtils.split(fields[1], segmentReader.getDelimiters().getComponent(), segmentReader.getDelimiters().getEscape());

		// First component (index 0) defines the char repertoire.  Fourth 
		// component (index 3) is optional and can override...
		if(syntaxIdComponents.length < 4) {
			changeReadEncoding(syntaxIdComponents[0], interchangeContext.getSegmentReader());
		} else {
			changeReadEncoding(syntaxIdComponents[3], interchangeContext.getSegmentReader());
		}
		
        while(true) {
	        String segCode = segmentReader.peek(3, true);
	        
	        if(segCode.equals("UNZ")) {
	    		segmentReader.moveToNextSegment(false);
	    		interchangeContext.mapControlSegment(unzSegment, true);
	    		break;
	        } else {	        	
	        	ControlBlockHandler handler = interchangeContext.getControlBlockHandler(segCode);
	        	handler.process(interchangeContext);
	        }
        }		
	}

	private void changeReadEncoding(String code, BufferedSegmentReader bufferedSegmentReader) throws EDIParseException, IOException {
		Charset charset = toCharsetMapping.get(code.toUpperCase());
		
		if(charset == null) {
			throw new EDIParseException("Unknown UN/EDIFACT character stream encoding code '" + code + "'.");
		}
		
		bufferedSegmentReader.changeEncoding(charset);
	}

	private static void createSegmentsDefs() {
		// UNB Segment Definition...
		// http://www.gefeg.com/jswg/v41/se/se13.htm
		unbSegment = new Segment();
		unbSegment.setSegcode("UNB");
		unbSegment.setXmltag("UNB");
		unbSegment.setDescription("UNB - Interchange Header");
		unbSegment.setTruncatable(true);
		unbSegment.addField(new Field("syntaxIdentifier",               true).
                addComponent(new Component("id",                        true)).
                addComponent(new Component("versionNum",                true)).
                addComponent(new Component("serviceCodeListDirVersion", false)).
                addComponent(new Component("codedCharacterEncoding",    false)).
                addComponent(new Component("releaseNum",                false)));
		unbSegment.addField(new Field("sender",             true).
                addComponent(new Component("id",            true)).
                addComponent(new Component("codeQualifier", false)).
                addComponent(new Component("internalId",    false)).
                addComponent(new Component("internalSubId", false)));
		unbSegment.addField(new Field("recipient",          true).
                addComponent(new Component("id",            true)).
                addComponent(new Component("codeQualifier", false)).
                addComponent(new Component("internalId",    false)).
                addComponent(new Component("internalSubId", false)));
		unbSegment.addField(new Field("dateTime",  true).
                addComponent(new Component("date", true)).
                addComponent(new Component("time", true)));
		unbSegment.addField(new Field("controlRef",   true));
		unbSegment.addField(new Field("recipientRef",      false).
                addComponent(new Component("ref",          true)).
                addComponent(new Component("refQualifier", false)));
		unbSegment.addField(new Field("applicationRef", false));
		unbSegment.addField(new Field("processingPriorityCode", false));
		unbSegment.addField(new Field("ackRequest", false));
		unbSegment.addField(new Field("agreementId", false));
		unbSegment.addField(new Field("testIndicator", false));

		// UNZ Segment Definition...
		// http://www.gefeg.com/jswg/v41/se/se21.htm
		unzSegment = new Segment();
		unzSegment.setSegcode("UNZ");
		unzSegment.setXmltag("UNZ");
		unzSegment.setDescription("UNZ - Interchange Trailer");
		unzSegment.setTruncatable(true);
		unzSegment.addField(new Field("controlCount", true));
		unzSegment.addField(new Field("controlRef", true));
	}

	private static void createRepertoireToCharsetMap() {
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
