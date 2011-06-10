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

package org.milyn.edisax.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.milyn.archive.Archive;
import org.milyn.archive.ArchiveClassLoader;
import org.milyn.assertion.AssertArgument;
import org.milyn.edisax.EDIConfigurationException;
import org.milyn.edisax.EDIParser;
import org.milyn.edisax.model.EdifactModel;
import org.milyn.edisax.model.internal.DelimiterType;
import org.milyn.edisax.model.internal.Delimiters;
import org.milyn.edisax.model.internal.Description;
import org.milyn.io.StreamUtils;
import org.milyn.resource.URIResourceLocator;
import org.milyn.util.ClassUtil;
import org.xml.sax.SAXException;

/**
 * EDIUtils contain different helper-methods for handling edifact.
 *
 * @author bardl
 */
public class EDIUtils {

    private static Log logger = LogFactory.getLog(EDIUtils.class);
    
    public static final String EDI_MAPPING_MODEL_ZIP_LIST_FILE = "META-INF/services/org/smooks/edi/mapping-model.lst";
    public static final String EDI_MAPPING_MODEL_INTERCHANGE_PROPERTIES_FILE = "META-INF/services/org/smooks/edi/interchange.properties";
    public static final String EDI_MAPPING_MODEL_URN = "META-INF/services/org/smooks/edi/urn";
    /**
     * Most model sets contain a set of common definitions (common types).
     */
    public static final Description MODEL_SET_DEFINITIONS_DESCRIPTION = new Description().setName("__modelset_definitions").setVersion("local");
    
    /**
     * Lookup name (string representation) of {@link #MODEL_SET_DEFINITIONS_DESCRIPTION}
     */
    public static final String MODEL_SET_DEFINITIONS_DESCRIPTION_LOOKUP_NAME = toLookupName(MODEL_SET_DEFINITIONS_DESCRIPTION);

    /**
     * Splits a String by delimiter as long as delimiter does not follow an escape sequence.
     * The split method follows the same behavior as the method splitPreserveAllTokens(String, String)
     * in {@link org.apache.commons.lang.StringUtils}.
     *
     * @param value the string to split, may be null.
     * @param delimiter the delimiter sequence. A null delimiter splits on whitespace.
     * @param escape the escape sequence. A null escape is allowed,  and result will be consistent with the splitPreserveAllTokens method.
     * @return an array of split edi-sequences, null if null string input.
     */
    public static String[] split(String value, String delimiter, String escape) {

        // A null input string returns null
        if (value == null) {
            return null;
        }

        // Empty input string returns empty array
        if (value.length() == 0) {
            return new String[0];
        }

        // Empty delimiter splits on whitespace.
        if (delimiter == null) {
            delimiter = " ";
        }

        List<CharSequence> charSequences = new ArrayList<CharSequence>();
        readSequenceStructure(value, delimiter, escape, charSequences);

        return putCharacterSequenceIntoResult(charSequences);
    }
    
    public static void loadMappingModels(String mappingModelFiles, Map<String, EdifactModel> mappingModels, URI baseURI) throws EDIConfigurationException, IOException, SAXException {
		AssertArgument.isNotNullAndNotEmpty(mappingModelFiles, "mappingModelFiles");
		AssertArgument.isNotNull(mappingModels, "mappingModels");
		AssertArgument.isNotNull(baseURI, "baseURI");

		String[] mappingModelFileTokens = mappingModelFiles.split(",");

        for(String mappingModelFile : mappingModelFileTokens) {
            mappingModelFile = mappingModelFile.trim();

            // First try processing based on the file extension
            if(mappingModelFile.endsWith(".xml")) {
                if(loadXMLMappingModel(mappingModelFile, mappingModels, baseURI)) {
                    // Loaded an XML config... on to next config in list...
                    continue;
                }
            } else if(mappingModelFile.endsWith(".zip") || mappingModelFile.endsWith(".jar")) {
                if(loadZippedMappingModels(mappingModelFile, mappingModels, baseURI)) {
                    // Loaded an zipped config... on to next config in list...
                    continue;
                }
            } else if(mappingModelFile.startsWith("urn:")) {
                String urn = mappingModelFile.substring(4);
                List<String> rootMappingModels = getMappingModelList(urn);

                loadMappingModels(mappingModels, baseURI, rootMappingModels);

                continue;
            }

            // The file extension didn't match up with what we expected, so perform a
            // brute force attempt to process the config...
            if(!loadXMLMappingModel(mappingModelFile, mappingModels, baseURI)) {
                if(!loadZippedMappingModels(mappingModelFile, mappingModels, baseURI)) {
                    throw new EDIConfigurationException("Failed to process EDI Mapping Model config file '" + mappingModelFile + "'.  Not a valid EDI Mapping Model configuration.");
                }
            }
        }
    }

    private static boolean loadXMLMappingModel(String mappingModelFile, Map<String, EdifactModel> mappingModels, URI baseURI) throws EDIConfigurationException {
		try {
			EdifactModel model = EDIParser.parseMappingModel(mappingModelFile, baseURI);
			mappingModels.put(toLookupName(model.getEdimap().getDescription()), model);
			return true;
		} catch (IOException e) {
			return false;
		} catch (SAXException e) {
			logger.debug("Configured mapping model file '" + mappingModelFile + "' is not a valid Mapping Model xml file.");
			return false;
		}
	}

    private static boolean loadZippedMappingModels(String mappingModelFile, Map<String, EdifactModel> mappingModels, URI baseURI) throws IOException, SAXException, EDIConfigurationException {
		URIResourceLocator locator = new URIResourceLocator();

		locator.setBaseURI(baseURI);

		InputStream rawZipStream = locator.getResource(mappingModelFile);
		if(rawZipStream != null) {
            Archive archive = loadArchive(rawZipStream);

			if(archive != null) {
				List<String> rootMappingModels = getMappingModelList(archive);

				if(rootMappingModels.isEmpty()) {
					logger.debug("Configured mapping model file '" + mappingModelFile + "' is not a valid Mapping Model zip file.  Check that the zip has a valid '" + EDI_MAPPING_MODEL_ZIP_LIST_FILE + "' mapping list file.");
					return false;
				}

				ClassLoader threadCCL = Thread.currentThread().getContextClassLoader();

				try {
					ArchiveClassLoader archiveClassLoader = new ArchiveClassLoader(threadCCL, archive);

					Thread.currentThread().setContextClassLoader(archiveClassLoader);
                    loadMappingModels(mappingModels, baseURI, rootMappingModels);
                } finally {
					Thread.currentThread().setContextClassLoader(threadCCL);
				}

				return true;
			}
		}

		return false;
	}

    public static void loadMappingModels(Map<String, EdifactModel> mappingModels, URI baseURI, List<String> rootMappingModels) throws IOException, SAXException, EDIConfigurationException {
        for (String rootMappingModel : rootMappingModels) {
            try {
                EdifactModel mappingModel = EDIParser.parseMappingModel(rootMappingModel, baseURI);

                mappingModel.setAssociateModels(mappingModels.values());
                mappingModels.put(toLookupName(mappingModel.getDescription()), mappingModel);
            } catch(Exception e) {
                throw new EDIConfigurationException("Error parsing EDI Mapping Model '" + rootMappingModel + "'.", e);
            }
        }
    }

    @SuppressWarnings("unchecked")
	private static List<String> getMappingModelList(Archive archive) throws IOException {
		byte[] zipEntryBytes = archive.getEntries().get(EDI_MAPPING_MODEL_ZIP_LIST_FILE);

		if(zipEntryBytes != null) {
            return getMappingModelList(new ByteArrayInputStream(zipEntryBytes));
        }

		return Collections.EMPTY_LIST;
	}

    private static List<String> getMappingModelList(String urn) throws IOException, EDIConfigurationException {
        InputStream mappingModelListStream = getMappingModelConfigStream(urn, EDI_MAPPING_MODEL_ZIP_LIST_FILE);

        if(mappingModelListStream == null) {
            throw new EDIConfigurationException("Failed to locate jar file for EDI Mapping Model URN '" + urn + "'.  Jar must be available on classpath.");
        }

        return getMappingModelList(mappingModelListStream);
    }

    public static Properties getInterchangeProperties(String ediMappingModel) throws IOException {
        InputStream interchangePropertiesStream = null;

        if(ediMappingModel.startsWith("urn:")) {
            interchangePropertiesStream = getMappingModelConfigStream(ediMappingModel, EDI_MAPPING_MODEL_INTERCHANGE_PROPERTIES_FILE);

            if(interchangePropertiesStream == null) {
                throw new EDIConfigurationException("Failed to locate jar file for EDI Mapping Model URN '" + ediMappingModel + "'.  Jar must be available on classpath.");
            }
        } else if(ediMappingModel.endsWith(".jar") || ediMappingModel.endsWith(".zip")) {
            URIResourceLocator locator = new URIResourceLocator();

            InputStream rawZipStream = locator.getResource(ediMappingModel);
            if(rawZipStream != null) {
                Archive archive = loadArchive(rawZipStream);
                if(archive != null) {
                    byte[] bytes = archive.getEntries().get(EDI_MAPPING_MODEL_INTERCHANGE_PROPERTIES_FILE);
                    if(bytes != null) {
                        interchangePropertiesStream = new ByteArrayInputStream(bytes);
                    }
                }
            }
        }

        if(interchangePropertiesStream != null) {
            Properties properties = new Properties();
            try {
                properties.load(interchangePropertiesStream);
                return properties;
            } finally {
                interchangePropertiesStream.close();
            }
        }

        return null;
    }

    public static String concatAndTruncate(List<String> nodeTokens, DelimiterType outerDelimiterType, Delimiters delimiters) {
        if(nodeTokens.isEmpty()) {
            return "";
        }

        for(int i = nodeTokens.size() - 1; i >= 0; i--) {
            if(!delimiters.removeableNodeToken(nodeTokens.get(i), outerDelimiterType)) {
                break;
            }
            nodeTokens.remove(i);
        }

        StringBuilder stringBuilder = new StringBuilder();
        for(String nodeToken : nodeTokens) {
            stringBuilder.append(nodeToken);
        }
        return stringBuilder.toString();
    }

    private static InputStream getMappingModelConfigStream(String urn, String fileName) throws IOException, EDIConfigurationException {
        List<URL> urnFiles = ClassUtil.getResources(EDI_MAPPING_MODEL_URN, EDIUtils.class);
        boolean ignoreVersion = false;
        
        if(urn.startsWith("urn:")) {
            urn = urn.substring(4);
        }
        if (urn.endsWith(":*")) {
        	// We have an wildcard as a version
        	ignoreVersion = true;
        	urn = urn.substring(0, urn.lastIndexOf(':'));
        }
        
        for(URL urnFile : urnFiles) {
            InputStream urnStream = urnFile.openStream();
            try {
                String archiveURN = StreamUtils.readStreamAsString(urnStream);
                if (ignoreVersion) {
                	// Cut the version out
                	archiveURN = archiveURN.substring(0, archiveURN.lastIndexOf(':'));
                }
                if(archiveURN.equals(urn)) {
                    String urnFileString = urnFile.toString();
                    String modelConfigFile = urnFileString.substring(0, urnFileString.length() - EDI_MAPPING_MODEL_URN.length()) + fileName;

                    List<URL> urlList = ClassUtil.getResources(fileName, EDIUtils.class);

                    for(URL url : urlList) {
                        if(url.toString().equals(modelConfigFile)) {
                            return url.openStream();
                        }
                    }
                }
            } finally {
                urnStream.close();
            }
        }

        throw new EDIConfigurationException("Failed to locate jar file for EDI Mapping Model URN '" + urn + "'.  Jar must be available on classpath.");
    }

    public static List<String> getMappingModelList(InputStream modelListStream) throws IOException {
        List<String> rootMappingModels = new ArrayList<String>();

        try {
            BufferedReader lineReader = new BufferedReader(new InputStreamReader(modelListStream, "UTF-8"));

            String line = lineReader.readLine();
            while(line != null) {
                line = line.trim();
                if(line.length() > 0 && !line.startsWith("#")) {
                    rootMappingModels.add(line);
                }
                line = lineReader.readLine();
            }

        } finally {
            modelListStream.close();
        }

        return rootMappingModels;
    }

    private static Archive loadArchive(InputStream rawStream) {
        try {
            return new Archive(new ZipInputStream(rawStream));
		} catch(Exception e) {
			// Assume it's not a Zip file.  Just return null...
			return null;
		}
	}

    /**
     * Loops through all CharSequences and decides whether to write out value or split.
     * @param charSequences a list of CharSequence
     * @return a String[] containing the split values.
     */
    private static String[] putCharacterSequenceIntoResult(List<CharSequence> charSequences) {
        List<String> result = new ArrayList<String>();
        StringBuilder stringBuilder = new StringBuilder();
        boolean escapeNextSequence = false;
        boolean delimiterLastSequence = false;
        CharSequence previousSequence = null;
        
        for (CharSequence sequence : charSequences) {
            delimiterLastSequence = false;

            if (escapeNextSequence && (sequence.getType() != CharSequenceTypeEnum.DELIMITER)) {
                stringBuilder.append(previousSequence.getValue());
            }

            try {
                switch (sequence.getType()) {
                    case PLAIN : {
                        stringBuilder.append(sequence.getValue());
                        break;
                    }
                    case DELIMITER : {
                        if (escapeNextSequence) {
                            stringBuilder.append(sequence.getValue());
                        } else {
                            result.add(stringBuilder.toString());
                            stringBuilder.setLength(0);
                            delimiterLastSequence = true;
                        }
                        break;
                    }
                    case ESCAPE : {
                        if (escapeNextSequence) {
                            stringBuilder.append(sequence.getValue());
                        } else {
                            escapeNextSequence = true;
                            continue;
                        }
                        break;
                    }
                }

                escapeNextSequence = false;
            } finally {
                previousSequence = sequence;
            }
        }

        if (stringBuilder.length() > 0 || delimiterLastSequence) {
            result.add(stringBuilder.toString());
        }

        return result.toArray(new String[result.size()]);
    }

    /**
     * Reads value and put the different parts into a list of CharSequence for easier handling of escape- and
     * delimiter-sequences when splitting value.
     * @param value the string to split
     * @param delimiter the characters defining the delimiter
     * @param escape the characters defining the escape
     * @param result a lis CharSequence.
     */
    private static void readSequenceStructure(String value, String delimiter, String escape, List<CharSequence> result) {
        StringBuilder stringBuilder = new StringBuilder();
        int escapeLength = escape == null ? 0 : escape.length();
        int delimiterLength = delimiter == null ? 0 : delimiter.length();

        for (int j = 0; j < value.length(); j++) {
            char theChar = value.charAt(j);
            stringBuilder.append(theChar);

            int curTokenLength = stringBuilder.length();
            if (builderEndsWith(stringBuilder, delimiter)) {
                stringBuilder.setLength(curTokenLength - delimiterLength);
                if (stringBuilder.length() > 0) {
                    result.add(new CharSequence(stringBuilder.toString(), CharSequenceTypeEnum.PLAIN));
                    stringBuilder.setLength(0);
                }
                result.add(new CharSequence(delimiter, CharSequenceTypeEnum.DELIMITER));
                continue;
            } else if (builderEndsWith(stringBuilder, escape)) {
                stringBuilder.setLength(curTokenLength - escapeLength);
                if (stringBuilder.length() > 0) {
                    result.add(new CharSequence(stringBuilder.toString(), CharSequenceTypeEnum.PLAIN));
                    stringBuilder.setLength(0);
                }
                result.add(new CharSequence(escape, CharSequenceTypeEnum.ESCAPE));
                continue;
            }
        }

        if (stringBuilder.length() > 0) {
            result.add(new CharSequence(stringBuilder.toString(), CharSequenceTypeEnum.PLAIN));
        }
    }

    private static boolean builderEndsWith(StringBuilder stringBuilder, String string) {
        if(string == null) {
            return false;
        }

        int builderLen = stringBuilder.length();
        int stringLen = string.length();

        if(builderLen < stringLen) {
            return false;
        }

        int stringIndx = 0;
        for(int i = (builderLen - stringLen); i < builderLen; i++) {
            if(stringBuilder.charAt(i) != string.charAt(stringIndx)) {
                return false;
            }
            stringIndx++;
        }

        return true;
    }

    private static class CharSequence {
        String value;
        CharSequenceTypeEnum type;

        public CharSequence(String value, CharSequenceTypeEnum type) {
            this.value = value;
            this.type = type;
        }

        public String getValue() {
            return value;
        }

        public CharSequenceTypeEnum getType() {
            return type;
        }
    }

	/**
	 * Convert {@link Description} to the string representation
	 * that is used for lookup in the hashmaps
	 * 
	 * @param description
	 * @return
	 */
	public static String toLookupName(Description description) {
		return description.getName() + ":"
				+ description.getVersion();
	}    
    
    private enum CharSequenceTypeEnum {
        PLAIN,
        ESCAPE,
        DELIMITER
    }
}
