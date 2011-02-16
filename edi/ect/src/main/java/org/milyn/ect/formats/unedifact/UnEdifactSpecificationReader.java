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

import org.milyn.ect.EdiSpecificationReader;
import org.milyn.ect.EdiParseException;
import org.milyn.edisax.util.EDIUtils;
import org.milyn.edisax.interchange.ControlBlockHandler;
import org.milyn.edisax.model.EdifactModel;
import org.milyn.edisax.model.internal.Edimap;
import org.milyn.edisax.model.internal.Field;
import org.milyn.edisax.model.internal.Segment;
import org.milyn.edisax.unedifact.UNEdifactInterchangeParser;
import org.milyn.util.ClassUtil;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * UN/EDIFACT Specification Reader.
 * 
 * @author bardl
 */
public class UnEdifactSpecificationReader implements EdiSpecificationReader {

    public static final String INTERCHANGE_TYPE = "UNEDIFACT";
    
    private static final int BUFFER = 2048;
    private static final String INTERCHANGE_DEFINITION = "un-edifact-interchange-definition.xml";

    private boolean useImport;
    private Map<String, byte[]> definitionFiles;
    private Map<String, byte[]> messageFiles;
    private Edimap definitionModel;

    public UnEdifactSpecificationReader(ZipInputStream specificationInStream, boolean useImport) throws IOException {
        this.useImport = useImport;

        definitionFiles = new HashMap<String, byte[]>();
        messageFiles = new HashMap<String, byte[]>();
        readDefinitionEntries(specificationInStream, new ZipDirectoryEntry("eded.", definitionFiles), new ZipDirectoryEntry("edcd.", definitionFiles), new ZipDirectoryEntry("edsd.", definitionFiles), new ZipDirectoryEntry("edmd.", "*", messageFiles));

        // Read Definition Configuration
        definitionModel = parseEDIDefinitionFiles();

        addMissingDefinitions(definitionModel);

        //Interchange envelope is inserted into the definitions. Handcoded at the moment.
        try {
            EdifactModel interchangeEnvelope = new EdifactModel(ClassUtil.getResourceAsStream(INTERCHANGE_DEFINITION, this.getClass()));
            definitionModel.getSegments().getSegments().addAll(interchangeEnvelope.getEdimap().getSegments().getSegments());
        } catch (Exception e) {
            throw new EdiParseException(e.getMessage(), e);
        }

    }

    public Set<String> getMessageNames() {
        Set<String> names = new LinkedHashSet<String>();
        names.add(definitionModel.getDescription().getName());
        names.addAll(messageFiles.keySet());
        return names;
    }

    public Edimap getMappingModel(String messageName) throws IOException {
        if(messageName.equals(definitionModel.getDescription().getName())) {
            return definitionModel;
        } else {
            return parseEdiMessage(messageName).getEdimap();
        }
    }

    public Properties getInterchangeProperties() {
        Properties properties = new Properties();

        properties.setProperty(EdiSpecificationReader.INTERCHANGE_TYPE, INTERCHANGE_TYPE);
        properties.setProperty(EdiSpecificationReader.MESSAGE_BINDING_CONFIG, "/org/milyn/smooks/edi/unedifact/model/r41/bindings/unedifact-message.xml");
        properties.setProperty(EdiSpecificationReader.INTERCHANGE_BINDING_CONFIG, "/org/milyn/smooks/edi/unedifact/model/r41/bindings/unedifact-interchange.xml");

        return properties;
    }

    private void addMissingDefinitions(Edimap definitionModel) {
        Segment ugh = new Segment();
        Segment ugt = new Segment();

        ugh.setSegcode("UGH");
        ugh.setXmltag("UGH");
        ugh.addField(new Field("id",ControlBlockHandler.NAMESPACE, true));

        ugt.setSegcode("UGT");
        ugt.setXmltag("UGT");
        ugt.addField(new Field("id",ControlBlockHandler.NAMESPACE, true));

        definitionModel.getSegments().getSegments().add(ugh);
        definitionModel.getSegments().getSegments().add(ugt);
    }

    private UnEdifactMessage parseEdiMessage(String messageName) throws IOException {
        byte[] message = messageFiles.get(messageName);

        if (message != null) {
            InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(message));
            try {
                return new UnEdifactMessage(reader, useImport, definitionModel);
            } finally {
                reader.close();
            }
        }
        return null;
    }

    public Edimap getDefinitionModel() throws IOException {
        return definitionModel;
    }

    private Edimap parseEDIDefinitionFiles() throws IOException, EdiParseException {

        Edimap edifactModel;
        Reader dataISR = null;
        Reader compositeISR = null;
        Reader segmentISR = null;
        try {
            dataISR = new InputStreamReader(new ByteArrayInputStream(definitionFiles.get("eded.")));
            compositeISR = new InputStreamReader(new ByteArrayInputStream(definitionFiles.get("edcd.")));
            segmentISR = new InputStreamReader(new ByteArrayInputStream(definitionFiles.get("edsd.")));

            edifactModel = UnEdifactDefinitionReader.parse(dataISR, compositeISR, segmentISR);
            edifactModel.setDescription(EDIUtils.MODEL_SET_DEFINITIONS_DESCRIPTION);
            edifactModel.getSegments().setXmltag("DefinitionMap");
            edifactModel.setDelimiters(UNEdifactInterchangeParser.defaultUNEdifactDelimiters);
        } finally {
            if (dataISR != null) {
                dataISR.close();
            }
            if (compositeISR != null) {
                compositeISR.close();
            }
            if (segmentISR != null) {
                segmentISR.close();
            }
        }
        return edifactModel;

    }

    private static void readDefinitionEntries(ZipInputStream folderZip, ZipDirectoryEntry... entries) throws IOException {

        ZipEntry fileEntry = folderZip.getNextEntry();
        while (fileEntry != null) {
            String fName = new File(fileEntry.getName().toLowerCase()).getName().replaceFirst("tr", "ed");
            for (ZipDirectoryEntry entry : entries) {
                if (fName.startsWith(entry.getDirectory())) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();

                    byte[] bytes = new byte[BUFFER];
                    int size;
                      while ((size = folderZip.read(bytes, 0, bytes.length)) != -1) {
                        baos.write(bytes, 0, size);
                      }

                    ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(baos.toByteArray()));
                    readZipEntry(entry.getEntries(), zipInputStream, entry.getFile());
                    zipInputStream.close();
                }
            }
            folderZip.closeEntry();
            fileEntry = folderZip.getNextEntry();
        }
    }

    private static boolean readZipEntry(Map<String, byte[]> files, ZipInputStream folderZip, String entry) throws IOException {

        boolean result = false;

        ZipEntry fileEntry = folderZip.getNextEntry();
        while (fileEntry != null) {
            String fName = new File(fileEntry.getName().toLowerCase()).getName().replaceFirst("tr", "ed");
            if (fName.startsWith(entry) || entry.equals("*")) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                byte[] bytes = new byte[2048];
                int size;
                  while ((size = folderZip.read(bytes, 0, bytes.length)) != -1) {
                    translatePseudoGraph(bytes);
                    baos.write(bytes, 0, size);
                  }

                result = true;
                if (entry.equals("*")) {
                    if (fileEntry.getName().indexOf('_') != -1) {
                        files.put(fName.substring(0, fName.indexOf('_')).toUpperCase(), baos.toByteArray());
                    }
                } else {
                    files.put(entry, baos.toByteArray());
                    break;
                }
            }
            folderZip.closeEntry();
            fileEntry = folderZip.getNextEntry();
        }

        return result;
    }

    private static void translatePseudoGraph(byte[] bytes)
    {
	for (int i=0, l=bytes.length; i<l; i++)
	{
	    switch(bytes[i])
	    {
		case (byte)0xC4:
		    bytes[i] = (byte)'-';
		    break;

		case (byte)0xC1:
		case (byte)0xBF:
		case (byte)0xD9:
		    bytes[i] = (byte)'+';
		    break;

		case (byte)0xB3:
		    bytes[i] = (byte)'|';
		    break;
	    }
	}
    }

    private static class ZipDirectoryEntry {
        private String directory;
        private String file;
        private Map<String, byte[]> entries;

        private ZipDirectoryEntry(String directory, Map<String, byte[]> entries) {
            this(directory, directory, entries);
        }

        public ZipDirectoryEntry(String directory, String file, Map<String, byte[]> entries) {
            this.directory = directory;
            this.file = file;
            this.entries = entries;
        }

        public String getDirectory() {
            return directory;
        }

        public String getFile() {
            return file;
        }

        public Map<String, byte[]> getEntries() {
            return entries;
        }
    }


}
