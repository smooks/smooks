/*
	Milyn - Copyright (C) 2008

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

package org.milyn.yaml;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.milyn.SmooksException;
import org.milyn.cdr.Parameter;
import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.cdr.annotation.Config;
import org.milyn.cdr.annotation.ConfigParam;
import org.milyn.cdr.annotation.ConfigParam.Use;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.annotation.Initialize;
import org.milyn.xml.SmooksXMLReader;
import org.milyn.yaml.handler.AliasReferencingEventHandler;
import org.milyn.yaml.handler.AliasResolvingEventHandler;
import org.milyn.yaml.handler.EventHandler;
import org.milyn.yaml.handler.YamlEventStreamHandler;
import org.milyn.yaml.handler.YamlToSaxHandler;
import org.w3c.dom.Element;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.events.Event;

/**
 * YAML to SAX event reader.
 * <p/>
 * This YAML Reader can be plugged into Smooks in order to convert a
 * YAML based message stream into a stream of SAX events to be consumed by the other
 * Smooks resources.
 *
 * <h3>Configuration</h3>
 * <pre>
 * &lt;resource-config selector="org.xml.sax.driver"&gt;
 *  &lt;resource&gt;org.milyn.yaml.YamlReader&lt;/resource&gt;
 *  &lt;!--
 *      (Optional) The element name of the SAX document root. Default of 'yaml'.
 *  --&gt;
 *  &lt;param name="<b>rootName</b>"&gt;<i>&lt;root-name&gt;</i>&lt;/param&gt;
 *  &lt;!--
 *      (Optional) The element name of a array element. Default of 'element'.
 *  --&gt;
 *  &lt;param name="<b>arrayElementName</b>"&gt;<i>&lt;array-element-name&gt;</i>&lt;/param&gt;
 *  &lt;!--
 *      (Optional) The replacement string for YAML NULL values. Default is an empty string.
 *  --&gt;
 *  &lt;param name="<b>nullValueReplacement</b>"&gt;<i>&lt;null-value-replacement&gt;</i>&lt;/param&gt;
 *  &lt;!--
 *      (Optional) The replacement character for whitespaces in a YAML map key. By default this not defined, so that the reader doesn't search for whitespaces.
 *  --&gt;
 *  &lt;param name="<b>keyWhitspaceReplacement</b>"&gt;<i>&lt;key-whitspace-replacement&gt;</i>&lt;/param&gt;
 *  &lt;!--
 *      (Optional) The prefix character to add if the YAML node name starts with a number. By default this is not defined, so that the reader doesn't search for element names that start with a number.
 *  --&gt;
 *  &lt;param name="<b>keyPrefixOnNumeric</b>"&gt;<i>&lt;key-prefix-on-numeric&gt;</i>&lt;/param&gt;
 *  &lt;!--
 *      (Optional) If illegal characters are encountered in a YAML element name then they are replaced with this value. By default this is not defined, so that the reader doesn't doesn't search for illegal characters.
 *  --&gt;
 *  &lt;param name="<b>illegalElementNameCharReplacement</b>"&gt;<i>&lt;illegal-element-name-char-replacement&gt;</i>&lt;/param&gt;
 *  &lt;!--
 *      (Optional) Defines a map of keys and there replacement. The from key will be replaced with the to key or the contents of the element.
 *  --&gt;
 *  &lt;param name="<b>keyMap</b>"&gt;
 *   &lt;key from="fromKey" to="toKey" /&gt;
 *   &lt;key from="fromKey"&gt;&lt;to&gt;&lt;/key&gt;
 *  &lt;/param&gt;
 *  &lt;!--
 *      (Optional) The strategy how to handle YAML anchors and aliases. Possible values:
 *          - REFER: Adds a 'id' attribute to the element with the anchor and the 'ref' attribute to the elements with the alias.
 *                   The value of these attributes is the name of the anchor. The reference needs to be handled within the Smooks config.
 *                   The attribute names can be set via the 'anchorAttributeName' and 'aliasAttributeName' properties
 *          - RESOLVE: The elements or value from the anchor are resolved (copied) under the element with the alias.
 *                     Smooks doesn't see that there was a reference.
 *          - REFER_RESOLVE: A combination of REFER and RESOLVE. The element of the anchor are resolved and the attributes are set.
 *                           You should use this if you want to resolve the element but also need the alias name because it has a
 *                           business meaning.
 *
 *  	 Default: 'REFER'
 *  --&gt;
 *  &lt;param name="<b>aliasStrategy</b>"&gt;<i>&lt;alias-strategy&gt;</i>&lt;/param&gt;
 *  &lt;!--
 *      (Optional) The name of the anchor attribute when the aliasStrategy is REFER or REFER_RESOLVER. Default of 'id'
 *  --&gt;
 *  &lt;param name="<b>anchorAttributeName</b>"&gt;<i>&lt;anchor-attribute-name&gt;</i>&lt;/param&gt;
 *  &lt;!--
 *      (Optional) The name of the alias attribute when the aliasStrategy is REFER or REFER_RESOLVER. Default of 'ref'
 *  --&gt;
 *  &lt;param name="<b>aliasAttributeName</b>"&gt;<i>&lt;alias-attribute-name&gt;</i>&lt;/param&gt;
 * &lt;/resource-config&gt;
 * </pre>
 *
 * @author <a href="mailto:maurice@zeijen.net">maurice@zeijen.net</a>
 */
public class YamlReader implements SmooksXMLReader {

	private static Log logger = LogFactory.getLog(YamlReader.class);

	public static final String CONFIG_PARAM_KEY_MAP = "keyMap";

	public static final String XML_ROOT = "yaml";

	public static final String XML_DOCUMENT = "document";

	public static final String XML_ARRAY_ELEMENT_NAME = "element";

	public static final String DEFAULT_ANCHOR_NAME = "id";

    public static final String DEFAULT_ALIAS_NAME = "ref";

    private ContentHandler contentHandler;

	private ExecutionContext executionContext;

	@ConfigParam(defaultVal = XML_ROOT)
    private String rootName;

	@ConfigParam(defaultVal = XML_DOCUMENT)
    private String documentName;

	@ConfigParam(defaultVal = XML_ARRAY_ELEMENT_NAME)
    private String arrayElementName;

	@ConfigParam(use = Use.OPTIONAL)
    private String keyWhitspaceReplacement;

	@ConfigParam(use = Use.OPTIONAL)
    private String keyPrefixOnNumeric;

	@ConfigParam(use = Use.OPTIONAL)
    private String illegalElementNameCharReplacement;

	@ConfigParam(defaultVal = DEFAULT_ANCHOR_NAME)
    private String anchorAttributeName;

	@ConfigParam(defaultVal = DEFAULT_ALIAS_NAME)
    private String aliasAttributeName;

    @ConfigParam(defaultVal = "false")
    private boolean indent;

    @ConfigParam(defaultVal = AliasStrategy.REFER_STR, decoder = AliasStrategy.DataDecoder.class)
    private AliasStrategy aliasStrategy;

    @Config
    private SmooksResourceConfiguration config;

    private final Yaml yaml = new Yaml();

	private YamlEventStreamHandler yamlEventStreamParser;

    @Initialize
    public void initialize() {
    	ElementNameFormatter elementNameFormatter = new ElementNameFormatter(initKeyMap(), keyWhitspaceReplacement, keyPrefixOnNumeric, illegalElementNameCharReplacement);
    	yamlEventStreamParser = new YamlEventStreamHandler(elementNameFormatter, documentName, arrayElementName);
    }
    /*
     * (non-Javadoc)
     * @see org.milyn.xml.SmooksXMLReader#setExecutionContext(org.milyn.container.ExecutionContext)
     */
	public void setExecutionContext(ExecutionContext request) {
		this.executionContext = request;
	}

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.XMLReader#parse(org.xml.sax.InputSource)
	 */
	public void parse(InputSource yamlInputSource) throws IOException, SAXException {
        if(contentHandler == null) {
            throw new IllegalStateException("'contentHandler' not set.  Cannot parse YAML stream.");
        }
        if(executionContext == null) {
            throw new IllegalStateException("Smooks container 'executionContext' not set.  Cannot parse YAML stream.");
        }

        try {
			// Get a reader for the YAML source...
	        Reader yamlStreamReader = yamlInputSource.getCharacterStream();
	        if(yamlStreamReader == null) {
	            throw new SmooksException("The InputSource doesn't provide a Reader character stream. Make sure that you supply a reader to the Smooks.filterSource method.");
	        }
	        YamlToSaxHandler yamlToSaxHandler = new YamlToSaxHandler(contentHandler, anchorAttributeName, aliasAttributeName, indent);

	        EventHandler eventHandler;
	        if(aliasStrategy == AliasStrategy.REFER) {
	        	eventHandler = new AliasReferencingEventHandler(yamlToSaxHandler);
	        } else {
	        	eventHandler = new AliasResolvingEventHandler(yamlEventStreamParser, yamlToSaxHandler, aliasStrategy == AliasStrategy.REFER_RESOLVE);
	        }

	        if(logger.isTraceEnabled()) {
	        	logger.trace("Starting YAML parsing");
	        }

	        Iterable<Event> yamlEventStream = yaml.parse(yamlStreamReader);

	        // Start the document and add the root  element...
	        contentHandler.startDocument();

	        yamlToSaxHandler.startElementStructure(rootName, null, false);

	        yamlEventStreamParser.handle(eventHandler, yamlEventStream);

	        yamlToSaxHandler.endElementStructure(rootName);

	        contentHandler.endDocument();

        } finally {
        	contentHandler = null;
        	executionContext = null;
        }
	}



	/**
	 *
	 */
	@SuppressWarnings("unchecked")
	private Map<String, String> initKeyMap() {
		Parameter keyMapParam = config.getParameter(CONFIG_PARAM_KEY_MAP);

       if (keyMapParam != null) {
           Object objValue = keyMapParam.getObjValue();

           if(objValue instanceof Map<?, ?>) {
               return (HashMap<String, String>) objValue;
           } else {
               Element keyMapParamElement = keyMapParam.getXml();

               if(keyMapParamElement != null) {
                   return KeyMapDigester.digest(keyMapParamElement);
               } else {
            	   throw new SmooksException("Sorry, the key properties must be available as XML DOM. Please configure using XML.");
               }
           }
       }
       return Collections.emptyMap();
	}

	public void setContentHandler(ContentHandler contentHandler) {
        this.contentHandler = contentHandler;
    }

    public ContentHandler getContentHandler() {
        return contentHandler;
    }

	/**
	 * @return the rootName
	 */
	public String getRootName() {
		return rootName;
	}


	/**
	 * @param rootName the rootName to set
	 */
	public void setRootName(String rootName) {
		this.rootName = rootName;
	}


	/**
	 * @return the arrayElementName
	 */
	public String getArrayElementName() {
		return arrayElementName;
	}


	/**
	 * @param arrayElementName the arrayElementName to set
	 */
	public void setArrayElementName(String arrayElementName) {
		this.arrayElementName = arrayElementName;
	}


	/**
	 * @return the keyWhitspaceReplacement
	 */
	public String getKeyWhitspaceReplacement() {
		return keyWhitspaceReplacement;
	}


	/**
	 * @param keyWhitspaceReplacement the keyWhitspaceReplacement to set
	 */
	public void setKeyWhitspaceReplacement(String keyWhitspaceReplacement) {
		this.keyWhitspaceReplacement = keyWhitspaceReplacement;
	}


	/**
	 * @return the keyPrefixOnNumeric
	 */
	public String getKeyPrefixOnNumeric() {
		return keyPrefixOnNumeric;
	}


	/**
	 * @param keyPrefixOnNumeric the keyPrefixOnNumeric to set
	 */
	public void setKeyPrefixOnNumeric(String keyPrefixOnNumeric) {
		this.keyPrefixOnNumeric = keyPrefixOnNumeric;
	}


	/**
	 * @return the illegalElementNameCharReplacement
	 */
	public String getIllegalElementNameCharReplacement() {
		return illegalElementNameCharReplacement;
	}


	/**
	 * @param illegalElementNameCharReplacement the illegalElementNameCharReplacement to set
	 */
	public void setIllegalElementNameCharReplacement(
			String illegalElementNameCharReplacement) {
		this.illegalElementNameCharReplacement = illegalElementNameCharReplacement;
	}

    public void setIndent(boolean indent) {
        this.indent = indent;
    }

	/****************************************************************************
     *
     * The following methods are currently unimplemented...
     *
     ****************************************************************************/

    public void parse(String systemId) throws IOException, SAXException {
        throw new UnsupportedOperationException("Operation not supports by this reader.");
    }

    public boolean getFeature(String name) throws SAXNotRecognizedException,
            SAXNotSupportedException {
        return false;
    }

    public void setFeature(String name, boolean value)
            throws SAXNotRecognizedException, SAXNotSupportedException {
    }

    public DTDHandler getDTDHandler() {
        return null;
    }

    public void setDTDHandler(DTDHandler arg0) {
    }

    public EntityResolver getEntityResolver() {
        return null;
    }

    public void setEntityResolver(EntityResolver arg0) {
    }

    public ErrorHandler getErrorHandler() {
        return null;
    }

    public void setErrorHandler(ErrorHandler arg0) {
    }

    public Object getProperty(String name) throws SAXNotRecognizedException,
            SAXNotSupportedException {
        return null;
    }

    public void setProperty(String name, Object value)
            throws SAXNotRecognizedException, SAXNotSupportedException {
    }
}
