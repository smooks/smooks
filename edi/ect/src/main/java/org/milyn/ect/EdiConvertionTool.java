/*
 * Milyn - Copyright (C) 2006 - 2010
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License (version 2.1) as published by the Free Software
 *  Foundation.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 *  See the GNU Lesser General Public License for more details:
 *  http://www.gnu.org/licenses/lgpl.txt
 */

package org.milyn.ect;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.emf.ecore.EPackage;
import org.milyn.archive.Archive;
import org.milyn.assertion.AssertArgument;
import org.milyn.ect.ecore.ECoreGenerator;
import org.milyn.ect.ecore.SchemaConverter;
import org.milyn.ect.formats.unedifact.UnEdifactSpecificationReader;
import org.milyn.edisax.interchange.EdiDirectory;
import org.milyn.edisax.model.internal.Component;
import org.milyn.edisax.model.internal.Description;
import org.milyn.edisax.model.internal.Edimap;
import org.milyn.edisax.model.internal.Field;
import org.milyn.edisax.model.internal.MappingNode;
import org.milyn.edisax.model.internal.Segment;
import org.milyn.edisax.model.internal.SegmentGroup;
import org.milyn.edisax.util.EDIUtils;

/**
 * EDI Convertion Tool.
 * <p/>
 * Takes the set of messages from an {@link EdiSpecificationReader} and generates
 * a Smooks EDI Mapping Model archive that can be written to a zip file or folder.
 * 
 * @author bardl
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class EdiConvertionTool {

    private static final Log logger = LogFactory.getLog(EdiConvertionTool.class);

    /**
     * Write an EDI Mapping Model configuration set from a UN/EDIFACT
     * specification.
     *
     * @param specification The UN/EDIFACT specification zip file.
     * @param modelSetOutStream The output zip stream for the generated EDI Mapping Model configuration set.
     * @param urn The URN for the EDI Mapping model configuration set.
     * @throws IOException Error writing Mapping Model configuration set.
     */
    public static void fromUnEdifactSpec(ZipInputStream specification, ZipOutputStream modelSetOutStream, String urn) throws IOException {
        try {
            fromSpec(new UnEdifactSpecificationReader(specification, true), modelSetOutStream, urn);
        } finally {
            specification.close();
        }
    }

    /**
     * Write an EDI Mapping Model configuration set from a UN/EDIFACT
     * specification.
     *
     * @param specification The UN/EDIFACT specification zip file.
     * @param urn The URN for the EDI Mapping model configuration set.
     * @return Smooks EDI Mapping model Archive.
     * @throws IOException Error writing Mapping Model configuration set.
     */
    public static Archive fromUnEdifactSpec(File specification, String urn) throws IOException {
        return fromUnEdifactSpec(specification, urn, null);
    }

    /**
     * Write an EDI Mapping Model configuration set from a UN/EDIFACT
     * specification.
     *
     * @param specification The UN/EDIFACT specification zip file.
     * @param urn The URN for the EDI Mapping model configuration set.
     * @param messages The messages to be included in the generated Archive.
     *
     * @return Smooks EDI Mapping model Archive.
     * @throws IOException Error writing Mapping Model configuration set.
     */
    public static Archive fromUnEdifactSpec(File specification, String urn, String... messages) throws IOException {
         ZipInputStream definitionZipStream;

        try {
            definitionZipStream = new ZipInputStream(new FileInputStream(specification));
        } catch (FileNotFoundException e) {
            throw new EdiParseException("Error opening zip file containing the Un/Edifact specification '" + specification.getAbsoluteFile() + "'.", e);
        }

        return createArchive(new UnEdifactSpecificationReader(definitionZipStream, true), urn, messages);
    }

    /**
     * Write an EDI Mapping Model configuration set from the specified EDI Specification Reader.
     * @param ediSpecificationReader The configuration reader for the EDI interchange configuration set.
     * @param modelSetOutStream The EDI Mapping Model output Stream.
     * @param urn The URN for the EDI Mapping model configuration set.
     * @throws IOException Error writing Mapping Model configuration set.
     */
    public static void fromSpec(EdiSpecificationReader ediSpecificationReader, ZipOutputStream modelSetOutStream, String urn) throws IOException {
        AssertArgument.isNotNull(ediSpecificationReader, "ediSpecificationReader");
        AssertArgument.isNotNull(modelSetOutStream, "modelSetOutStream");

        try {
            Archive archive = createArchive(ediSpecificationReader, urn);

            // Now output the generated archive...
            archive.toOutputStream(modelSetOutStream);
        } catch (Throwable t) {
            logger.fatal("Error while generating EDI Mapping Model archive for '" + urn + "'.", t);
        } finally {
            modelSetOutStream.close();
        }
    }

    /**
     * Write an EDI Mapping Model configuration set from a UN/EDIFACT
     * specification.
     *
     * @param specification The UN/EDIFACT specification zip file.
     * @param modelSetOutFolder The output folder for the generated EDI Mapping Model configuration set.
     * @param urn The URN for the EDI Mapping model configuration set.
     * @throws IOException Error writing Mapping Model configuration set.
     */
    public static void fromUnEdifactSpec(ZipInputStream specification, File modelSetOutFolder, String urn) throws IOException {
        try {
            fromSpec(new UnEdifactSpecificationReader(specification, true), modelSetOutFolder, urn);
        } finally {
            specification.close();
        }
    }

    /**
     * Write an EDI Mapping Model configuration set from the specified EDI Specification Reader.
     * @param ediSpecificationReader The configuration reader for the EDI interchange configuration set.
     * @param modelSetOutFolder The output folder for the generated EDI Mapping Model configuration set.
     * @param urn The URN for the EDI Mapping model configuration set.
     * @throws IOException Error writing Mapping Model configuration set.
     */
    public static void fromSpec(EdiSpecificationReader ediSpecificationReader, File modelSetOutFolder, String urn) throws IOException {
        AssertArgument.isNotNull(ediSpecificationReader, "ediSpecificationReader");
        AssertArgument.isNotNull(modelSetOutFolder, "modelSetOutFolder");

        Archive archive = createArchive(ediSpecificationReader, urn);

        // Now output the generated archive...
        archive.toFileSystem(modelSetOutFolder);
    }

    private static Archive createArchive(EdiSpecificationReader ediSpecificationReader, String urn, String... messages) throws IOException {
        Archive archive = new Archive();
        StringBuilder modelListBuilder = new StringBuilder();
        StringWriter messageEntryWriter = new StringWriter();
        String pathPrefix = urn.replace(".", "_").replace(":", "/");
        EdiDirectory ediDirectory = ediSpecificationReader.getEdiDirectory(messages);

        // Add the common model...
        addModel(ediDirectory.getCommonModel(), pathPrefix, modelListBuilder, messageEntryWriter, archive);

        // Add each of the messages...
        for(Edimap messageModel : ediDirectory.getMessageModels()) {
            addModel(messageModel, pathPrefix, modelListBuilder, messageEntryWriter, archive);
        }

        // Now create XML Schemas
        Set<EPackage> packages = new ECoreGenerator().generatePackages(ediDirectory);
        String pluginID = "org.milyn.edi.unedifact.unknown";
        if (urn.lastIndexOf(':') > 0) {
        	pluginID = urn.substring(0, urn.lastIndexOf(':')).replace(':', '.').toLowerCase();
        }
        Archive schemas = SchemaConverter.INSTANCE.createArchive(packages, pluginID, pathPrefix);
        archive.merge(schemas);

        // Add the generated mapping model to the archive...
        archive.addEntry(EDIUtils.EDI_MAPPING_MODEL_ZIP_LIST_FILE, modelListBuilder.toString());

        // Add the model set URN to the archive...
        archive.addEntry(EDIUtils.EDI_MAPPING_MODEL_URN, urn);

        // Add an entry for the interchange properties...
        Properties interchangeProperties = ediSpecificationReader.getInterchangeProperties();
        ByteArrayOutputStream propertiesOutStream = new ByteArrayOutputStream();
        try {
            interchangeProperties.store(propertiesOutStream, "UN/EDIFACT Interchange Properties");
            propertiesOutStream.flush();
            archive.addEntry(EDIUtils.EDI_MAPPING_MODEL_INTERCHANGE_PROPERTIES_FILE, propertiesOutStream.toByteArray());
        } finally {
            propertiesOutStream.close();
        }

        return archive;
    }

    private static void addModel(Edimap model, String pathPrefix, StringBuilder modelListBuilder, StringWriter messageEntryWriter, Archive archive) throws IOException {
        Description modelDesc = model.getDescription();
        String messageEntryPath = pathPrefix + "/" + modelDesc.getName() + ".xml";

        // Generate the mapping model for this message...
        messageEntryWriter.getBuffer().setLength(0);
        model.write(messageEntryWriter);

        // Add the generated mapping model to the archive...
        archive.addEntry(messageEntryPath, messageEntryWriter.toString());

        // Add this messages archive entry to the mapping model list file...
        modelListBuilder.append("/" + messageEntryPath);
        modelListBuilder.append("!" + modelDesc.getName());
        modelListBuilder.append("!" + modelDesc.getVersion());
        modelListBuilder.append("!" + modelDesc.getNamespace());
        modelListBuilder.append("\n");
    }

    public static void removeDuplicateSegments(SegmentGroup segmentGroup) {
        if(segmentGroup instanceof Segment) {
            removeDuplicateFields(((Segment)segmentGroup).getFields()); 
        }

        List<SegmentGroup> segments = segmentGroup.getSegments();
        if(segments != null) {
            removeDuplicateMappingNodes(segments);
            for(SegmentGroup childSegmentGroup : segments) {
                removeDuplicateSegments(childSegmentGroup);
            }
        }
    }

    private static void removeDuplicateFields(List<Field> fields) {
        if(fields != null && !fields.isEmpty()) {
            // Remove the duplicates from the fields themselves...
            removeDuplicateMappingNodes(fields);

            // Drill down into the field components...
            for(Field field : fields) {
                removeDuplicateComponents(field.getComponents());
            }
        }
    }

    private static void removeDuplicateComponents(List<Component> components) {
        if(components != null && !components.isEmpty()) {
            // Remove the duplicates from the components themselves...
            removeDuplicateMappingNodes(components);

            // Remove duplicate sub components from each component...
            for(Component component : components) {
                removeDuplicateMappingNodes(component.getSubComponents());
            }
        }
    }

    private static void removeDuplicateMappingNodes(List mappingNodes) {
        if(mappingNodes == null || mappingNodes.isEmpty()) {
            return;
        }
        
        Set<String> nodeNames = getMappingNodeNames(mappingNodes);

        if(nodeNames.size() < mappingNodes.size()) {
            // There may be duplicates... find them and number them...
            for(String nodeName : nodeNames) {
                int nodeCount = getMappingNodeCount(mappingNodes, nodeName);
                if(nodeCount > 1) {
                    removeDuplicateMappingNodes(mappingNodes, nodeName);
                }
            }
        }
    }

    private static void removeDuplicateMappingNodes(List mappingNodes, String nodeName) {
        int tagIndex = 1;

        for(Object mappingNodeObj : mappingNodes) {
            MappingNode mappingNode = (MappingNode) mappingNodeObj;
            String xmlTag = mappingNode.getXmltag();

            if(xmlTag != null && xmlTag.equals(nodeName)) {
                mappingNode.setXmltag(xmlTag + MappingNode.INDEXED_NODE_SEPARATOR + tagIndex);
                tagIndex++;
            }
        }
    }

    private static Set<String> getMappingNodeNames(List mappingNodes) {
        Set<String> nodeNames = new LinkedHashSet<String>();

        for(Object mappingNode : mappingNodes) {
            String xmlTag = ((MappingNode) mappingNode).getXmltag();
            if(xmlTag != null) {
                nodeNames.add(xmlTag);
            }
        }

        return nodeNames;
    }

    private static int getMappingNodeCount(List mappingNodes, String nodeName) {
        int nodeCount = 0;

        for(Object mappingNode : mappingNodes) {
            String xmlTag = ((MappingNode) mappingNode).getXmltag();
            if(xmlTag != null && xmlTag.equals(nodeName)) {
                nodeCount++;
            }
        }

        return nodeCount;
    }
}
