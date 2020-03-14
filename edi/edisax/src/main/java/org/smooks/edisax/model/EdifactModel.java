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

package org.smooks.edisax.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.smooks.assertion.AssertArgument;
import org.smooks.edisax.EDIConfigurationException;
import org.smooks.edisax.EDIParseException;
import org.smooks.edisax.model.internal.Component;
import org.smooks.edisax.model.internal.Delimiters;
import org.smooks.edisax.model.internal.Description;
import org.smooks.edisax.model.internal.Edimap;
import org.smooks.edisax.model.internal.Field;
import org.smooks.edisax.model.internal.Import;
import org.smooks.edisax.model.internal.Segment;
import org.smooks.edisax.model.internal.SegmentGroup;
import org.smooks.io.StreamUtils;
import org.smooks.resource.URIResourceLocator;
import org.xml.sax.SAXException;

/**                                          
 * EdifactModel contains all logic for handling imports for the
 * edi-message-mapping model.
 */
public class EdifactModel {

    private static final URI UNSPECIFIED = URI.create("unspecified");
	private Description description;
    private String mappingConfig;
    private final URI modelURI;
    private final URI importBaseURI;

    private volatile Edimap edimap;
    private Collection<EdifactModel> associateModels;

    /**
     * Public Constructor.
     * @param edimap Mapping Model.
     */
    public EdifactModel(Edimap edimap) {
        AssertArgument.isNotNull(edimap, "edimap");
        this.edimap = edimap;
        modelURI = UNSPECIFIED;
        importBaseURI = URIResourceLocator.getSystemBaseURI();
    }

    /**
     * Public Constructor.
     */
    public EdifactModel(InputStream mappingModelStream) throws IOException {
        AssertArgument.isNotNull(mappingModelStream, "mappingModelStream");
		this.importBaseURI = URIResourceLocator.getSystemBaseURI();
        modelURI = UNSPECIFIED;
		try {
            this.mappingConfig = StreamUtils.readStreamAsString(mappingModelStream);
        } finally {
            mappingModelStream.close();
        }
    }
    
    /**
     * Public constructor.
     * @param modelURI The model resource URI.
     * @param importBaseURI The base URI for loading imports.
     * @param mappingModelStream The edi-message-mapping.
	 */
	public EdifactModel(URI modelURI, URI importBaseURI, InputStream mappingModelStream) throws IOException {
		AssertArgument.isNotNull(importBaseURI, "importBaseURI");
        AssertArgument.isNotNull(mappingModelStream, "mappingModelStream");

		this.modelURI = modelURI;
		this.importBaseURI = importBaseURI;
        try {
            this.mappingConfig = StreamUtils.readStreamAsString(mappingModelStream);
        } finally {
            mappingModelStream.close();
        }
	}

    /**
     * Public constructor.
     * @param modelURI The model resource URI.
     * @param importBaseURI The base URI for loading imports.
     * @param mappingModelStream The edi-message-mapping.
	 */
	public EdifactModel(URI modelURI, URI importBaseURI, Reader mappingModelStream) throws IOException {
		AssertArgument.isNotNull(importBaseURI, "importBaseURI");
        AssertArgument.isNotNull(mappingModelStream, "mappingModelStream");

		this.modelURI = modelURI;
		this.importBaseURI = importBaseURI;
        try {
            this.mappingConfig = StreamUtils.readStream(mappingModelStream);
        } finally {
            mappingModelStream.close();
        }
	}

    public void setDescription(Description description) {
        this.description = description;
    }

    public Description getDescription() {
        if(description != null) {
            return description;
        }
        return getEdimap().getDescription();
    }

	/**
     * Returns the edimap containing the parser logic.
     * @return edi-message-mapping.
     */
    public Edimap getEdimap() {
        if(edimap == null) {
            // Lazy parsing of the Edimap configuration...
            try {
                parseSequence();
            } catch (Exception e) {
                throw new EDIConfigurationException("Error parsing EDI Mapping Model [" + mappingConfig + "].", e);
            }
        }
        return edimap;
    }

    /**
     * Returns the delimiters used in edifact format.
     * @return delimiters.
     */
    public Delimiters getDelimiters() {
        return getEdimap().getDelimiters();
    }

    /**
     * Get the model URI.
     * @return The model URI.
     */
    public URI getModelURI() {
        return modelURI;
    }

    /**
     * Set a set of models that are associated with this model instance.
     * <p/>
     * An associate set of models could be (for example) the other models in
     * a UN/EDIFACT model set, or some other interchange type.
     *
     * @param associateModels Associate models.
     */
    public void setAssociateModels(Collection<EdifactModel> associateModels) {
        this.associateModels = associateModels;
    }

    /**
     * Set the edifact edimap from the mapping model InputStream.
     * @throws org.smooks.edisax.EDIParseException is thrown when EdifactModel is unable to initialize edimap.
     * @throws org.smooks.edisax.EDIConfigurationException is thrown when edi-message-mapping contains multiple or no namespace declaration.
     * @throws java.io.IOException is thrown when error occurs when parsing edi-message-mapping.
     */
    private synchronized void parseSequence() throws EDIConfigurationException, IOException, SAXException {

        if(edimap != null) {
            return;
        }

        //To prevent circular dependency the name/url of all imported urls are stored in a dependency tree.
        //If a name/url already exists in a parent node, we have a circular dependency.
        DependencyTree<String> tree = new DependencyTree<String>();
        EDIConfigDigester digester = new EDIConfigDigester(modelURI, importBaseURI);

        edimap = digester.digestEDIConfig(new StringReader(mappingConfig));
        description = edimap.getDescription();
        importFiles(tree.getRoot(), edimap, tree);
    }

    /**
     * Handle all imports for the specified edimap. The parent Node is used by the
     * DependencyTree tree to keep track of previous imports for preventing cyclic dependency.
     * @param parent The node representing the importing file.
     * @param edimap The importing edimap.
     * @param tree The DependencyTree for preventing cyclic dependency in import.
     * @throws EDIParseException Thrown when a cyclic dependency is detected.
     * @throws org.smooks.edisax.EDIConfigurationException is thrown when edi-message-mapping contains multiple or no namespace declaration.
     * @throws java.io.IOException is thrown when error occurs when parsing edi-message-mapping.
     */
    private void importFiles(Node<String> parent, Edimap edimap, DependencyTree<String> tree) throws SAXException, EDIConfigurationException, IOException {
        Edimap importedEdimap;
        Node<String> child, conflictNode;

        for (Import imp : edimap.getImports()) {
            URI importUri = imp.getResourceURI();
            Map<String, Segment> importedSegments = null;

            child = new Node<String>(importUri.toString());
            conflictNode = tree.add(parent, child);
            if ( conflictNode != null ) {
                throw new EDIParseException(edimap, "Circular dependency encountered in edi-message-mapping with imported files [" + importUri + "] and [" + conflictNode.getValue() + "]");
            }

            importedSegments = getImportedSegments(importUri);
            if(importedSegments == null) {
                EDIConfigDigester digester = new EDIConfigDigester(importUri, URIResourceLocator.extractBaseURI(importUri));

                importedEdimap = digester.digestEDIConfig(new URIResourceLocator().getResource(importUri.toString()));
                importFiles(child, importedEdimap, tree);
                importedSegments = createImportMap(importedEdimap);
            }

            applyImportOnSegments(edimap.getSegments().getSegments(), imp, importedSegments);            
        }

        // Imports have been applied on the segments, so clear them now...
        edimap.getImports().clear();
    }

    private Map<String, Segment> getImportedSegments(URI importUri) {
        if(associateModels != null) {
            for(EdifactModel model : associateModels) {
                if(model.getModelURI().equals(importUri)) {
                    return createImportMap(model.getEdimap());
                }
            }
        }

        return null;
    }

    private void applyImportOnSegments(List<SegmentGroup> segmentGroup, Import imp, Map<String, Segment> importedSegments) throws EDIParseException {
        for (SegmentGroup segment : segmentGroup) {
            if(segment instanceof Segment) {
                applyImportOnSegment((Segment)segment, imp, importedSegments);
            }

            if (segment.getSegments() != null) {
                applyImportOnSegments(segment.getSegments(), imp, importedSegments);
            }
        }
    }

    /**
     * Inserts data from imported segment into the importing segment. Continues through all
     * the child segments of the importing segment.
     * @param segment the importing segment.
     * @param imp import information like url and namespace.
     * @param importedSegments the imported segment.
     * @throws EDIParseException Thrown when a segref attribute in importing segment contains
     * a value not located in the imported segment but with the namespace referencing the imported file.
     */
    private void applyImportOnSegment(Segment segment, Import imp, Map<String, Segment> importedSegments) throws EDIParseException {
        if (segment.getNodeTypeRef() != null && segment.getNodeTypeRef().startsWith(imp.getNamespace()+":")) {
            String key = segment.getNodeTypeRef().substring(segment.getNodeTypeRef().indexOf(':') + 1);
            Segment importedSegment = importedSegments.get(key);

            if (importedSegment == null) {
                throw new EDIParseException(edimap, "Referenced segment [" + key + "] does not exist in imported edi-message-mapping [" + imp.getResource() + "]");
            }
            insertImportedSegmentInfo(segment, importedSegment, imp.isTruncatableSegments(), imp.isTruncatableFields(), imp.isTruncatableComponents());
        }
    }

    /**
     * Inserts fields and segments from the imported segment into the importing segment. Also
     * overrides the truncatable attributes in Fields and Components of the imported file if
     * values are set to true or false in truncatableFields or truncatableComponents.
     * @param segment the importing segment.
     * @param importedSegment the imported segment.
     * @param truncatableFields a global attribute for overriding the truncatable attribute in imported segment.
     * @param truncatableComponents a global attribute for overriding the truncatable attribute in imported segment.
     */
    private void insertImportedSegmentInfo(Segment segment, Segment importedSegment, Boolean truncatableSegments, Boolean truncatableFields, Boolean truncatableComponents) {
        //Overwrite all existing fields in segment, but add additional segments to existing segments.
        segment.getFields().addAll(importedSegment.getFields());

        segment.setImportXmlTag(importedSegment.getXmltag());

        if (importedSegment.getSegments().size() > 0) {
            segment.getSegments().addAll(importedSegment.getSegments());
        }

        //If global truncatable attributes are set in importing mapping, then
        //override the attributes in the imported files.
        if (truncatableSegments != null) {
            segment.setTruncatable(truncatableSegments);
        }
        
        if (truncatableFields != null || truncatableComponents != null) {
            for ( Field field : segment.getFields()) {
                field.setTruncatable(isTruncatable(truncatableFields, field.isTruncatable()));
                if ( truncatableComponents != null ) {
                    for (Component component : field.getComponents()) {
                        component.setTruncatable(isTruncatable(truncatableComponents, component.isTruncatable()));
                    }
                }
            }
        }        
    }

    /**
     * Creates a Map given an Edimap. All segments in edimap are stored as values in the Map
     * with the corresponding segcode as key.
     * @param edimap the edimap containing segments to be inserted into Map.
     * @return Map containing all segment in edimap.
     */
    private Map<String, Segment> createImportMap(Edimap edimap) {
        HashMap<String, Segment> result = new HashMap<String, Segment>();
        for (SegmentGroup segmentGroup : edimap.getSegments().getSegments()) {
            if(segmentGroup instanceof Segment) {
                result.put(((Segment)segmentGroup).getSegcode(), (Segment) segmentGroup);
            }
        }
        return result;
    }

    /**
     * Returns truncatable attributes specified in import element in the importing edi-message-mapping
     * if it exists. Otherwise it sets value of the truncatable attribute found the imported segment.
     * @param truncatableImporting truncatable value found in import element in importing edi-message-mapping.
     * @param truncatableImported truncatable value found in imported segment.
     * @return truncatable from importing edi-message-mapping if it exists, otherwise return value from imported segment.
     */
    private Boolean isTruncatable(Boolean truncatableImporting, boolean truncatableImported) {
        Boolean result = truncatableImported;
        if (truncatableImporting != null) {
            result = truncatableImporting;
        }
        return result;
    }

    /************************************************************************
     * Private classes  used for locating and preventing cyclic dependency. *
     ************************************************************************/

    private class DependencyTree<T> {
        Node<T> root;

        public DependencyTree() {
            root = new Node<T>(null);
        }

        public Node<T> getRoot() {
            return root;
        }

        /**
         * Add child to parent Node if value does not exist in direct path from child to root
         * node, i.e. in any ancestralnode.
         * @param parent parent node
         * @param child the child node to add.
         * @return null if the value in child is not in confilct with value in any ancestor Node, otherwise return the conflicting ancestor Node. 
         */
        public Node<T> add(Node<T> parent, Node<T> child){
            Node<T> node = parent;
            while (node != null ) {
                if (node != root && node.getValue().equals(child.getValue())) {
                    return node;
                }
                node = node.getParent();
            }
            child.setParent(parent);
            parent.getChildren().add(child);
            return null;
        }

        public List<T> getUniqueValues() {
            List<T> result = new ArrayList<T>();
            return getUniqueValuesForNode(root, result);
        }

        private List<T> getUniqueValuesForNode(Node<T> node, List<T> list) {
            if ( node.getValue() != null && !list.contains( node.getValue() ) ) {
                list.add(node.getValue());
            }
            return list;
        }
    }

    private class Node<T> {
        private T value;
        private Node<T> parent;
        private List<Node<T>> children;

        public Node(T value) {
            children = new ArrayList<Node<T>>();
            this.value = value;
        }

        public T getValue() {
            return value;
        }

        public Node<T> getParent() {
            return parent;
        }

        public void setParent(Node<T> parent) {
            this.parent = parent;
        }

        public List<Node<T>> getChildren() {
            return children;
        }
    }
}


