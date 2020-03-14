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

package org.smooks.edi;

import org.smooks.cdr.ProfileTargetingExpression;
import org.smooks.cdr.SmooksResourceConfiguration;
import org.smooks.cdr.annotation.AppContext;
import org.smooks.cdr.annotation.Config;
import org.smooks.cdr.annotation.ConfigParam;
import org.smooks.container.ApplicationContext;
import org.smooks.container.ExecutionContext;
import org.smooks.edisax.EDIConfigurationException;
import org.smooks.edisax.EDIParser;
import org.smooks.edisax.model.EdifactModel;
import org.smooks.resource.ContainerResourceLocator;
import org.smooks.resource.URIResourceLocator;
import org.smooks.xml.SmooksXMLReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

/**
 * Smooks EDI Reader.
 * <p/>
 * Hooks the Milyn {@link org.smooks.edisax.EDIParser} into the <a href="http://milyn.codehaus.org/Smooks" target="new">Smooks</a> framework.
 * This adds EDI processing support to Smooks.
 *
 * <h3>Configuration</h3>
 * <pre>
 * &lt;edi:reader mappingModel="edi-to-xml-order-mapping.xml" validate="false"/&gt;
 * </pre>
 *
 * @author tfennelly
 */
@SuppressWarnings("unchecked")
public class EDIReader extends EDIParser implements SmooksXMLReader {

	/**
	 * Logger.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(EDIReader.class);
	/**
	 * Context lookup key for the mapping model table.
	 */
	private static String MAPPING_TABLE_CTX_KEY = EDIReader.class.getName() + "#MAPPING_TABLE_CTX_KEY";
	/**
	 * Model resource configuration key.
	 */
	public static final String MODEL_CONFIG_KEY = "mapping-model";
	/**
	 * The parser configuration.
	 */
    @Config
    private SmooksResourceConfiguration configuration;
    /**
     * Application context.
     */
    @AppContext
    private ApplicationContext applicationContext;

    @ConfigParam(name = MODEL_CONFIG_KEY)
    private String modelConfigData;

    @ConfigParam(defaultVal = "false")
    private boolean ignoreNewLines;

    @ConfigParam(defaultVal = "UTF-8")
    private Charset encoding;

    @ConfigParam(defaultVal = "false")
    private Boolean validate;

    public void setExecutionContext(ExecutionContext executionContext) {
	}

	/**
	 * Parse the EDI message.
	 * <p/>
	 * Overridden so as to set the EDI to XML mapping model on the parser.
	 */
	public void parse(InputSource ediSource) throws IOException, SAXException {
		EdifactModel edi2xmlMappingModel = getMappingModel();

		setMappingModel(edi2xmlMappingModel);

		// Set features...
		setFeature(FEATURE_VALIDATE, validate);
		setFeature(FEATURE_IGNORE_NEWLINES, ignoreNewLines);

        super.parse(ediSource);
	}

    /**
	 * Get the mapping model associated with the supplied SmooksResourceConfiguration.
	 * <p/>
	 * The parsed and validated model are cached in the Smooks container context, keyed
	 * by the SmooksResourceConfiguration instance.
	 * @return The Mapping Model.
	 * @throws IOException Error reading resource configuration data (the mapping model).
	 * @throws SAXException Error parsing mapping model.
	 */
	private EdifactModel getMappingModel() throws IOException, SAXException {
        EdifactModel edifactModel;
        Hashtable mappings = getMappingTable(applicationContext);

		synchronized (configuration) {
            edifactModel = (EdifactModel) mappings.get(configuration);
            if(edifactModel == null) {
				try {
	            	ContainerResourceLocator resourceLocator = applicationContext.getResourceLocator();

                    if(modelConfigData.startsWith("urn:") || modelConfigData.endsWith(".jar") || modelConfigData.endsWith(".zip")) {
                        throw new IOException("Unsupported mapping model config URI for basic EDI Parser '" + modelConfigData + "'.  Check that you are using the correct EDI parser.  You may need to configure an Interchange Parser, such as the UN/EDIFACT parser.");
                    }

	            	if(resourceLocator instanceof URIResourceLocator) {
	            		// This will resolve config paths relative to the containing smooks config file....
	            		edifactModel = EDIParser.parseMappingModel(modelConfigData, (resourceLocator).getBaseURI());
	            	} else {
	            		edifactModel = EDIParser.parseMappingModel(modelConfigData, URIResourceLocator.getSystemBaseURI());
	            	}
	    			if(edifactModel == null) {
	    				LOGGER.error("Invalid " + MODEL_CONFIG_KEY + " config value '" + modelConfigData + "'. Failed to locate EDI Mapping Model resource!");
	    			}
				} catch (IOException e) {
					throw new IOException("Error parsing EDI mapping model [" + configuration.getStringParameter(MODEL_CONFIG_KEY) + "].  Target Profile(s) " + getTargetProfiles() + "."
							, e);
				} catch (SAXException e) {
					throw new SAXException("Error parsing EDI mapping model [" + configuration.getStringParameter(MODEL_CONFIG_KEY) + "].  Target Profile(s) " + getTargetProfiles() + ".", e);
				} catch (EDIConfigurationException e) {
                    throw new SAXException("Error parsing EDI mapping model [" + configuration.getStringParameter(MODEL_CONFIG_KEY) + "].  Target Profile(s) " + getTargetProfiles() + ".", e);
                }
                mappings.put(configuration, edifactModel);
				LOGGER.debug("Parsed, validated and cached EDI mapping model [" + edifactModel.getEdimap().getDescription().getName() + ", Version " + edifactModel.getEdimap().getDescription().getVersion() + "].  Target Profile(s) " + getTargetProfiles() + ".");
			} else if(LOGGER.isInfoEnabled()) {
				LOGGER.debug("Found EDI mapping model [" + edifactModel.getEdimap().getDescription().getName() + ", Version " + edifactModel.getEdimap().getDescription().getVersion() + "] in the model cache.  Target Profile(s) " + getTargetProfiles() + ".");
			}
		}

		return edifactModel;
	}

	/**
	 * Get the mapping model table from the context.
	 * @param context The context from which to extract the mapping table.
	 * @return The mapping model talbe.
	 */
	protected static Hashtable getMappingTable(ApplicationContext context) {
		Hashtable mappingModelTable = (Hashtable) context.getAttribute(MAPPING_TABLE_CTX_KEY);

		if(mappingModelTable == null) {
			mappingModelTable = new Hashtable();
			context.setAttribute(MAPPING_TABLE_CTX_KEY, mappingModelTable);
		}

		return mappingModelTable;
	}

    private List<ProfileTargetingExpression> getTargetProfiles() {
        return Arrays.asList(configuration.getProfileTargetingExpressions());
    }
}
