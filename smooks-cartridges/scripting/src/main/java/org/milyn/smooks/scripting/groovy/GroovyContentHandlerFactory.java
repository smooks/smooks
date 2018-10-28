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

package org.milyn.smooks.scripting.groovy;

import groovy.lang.GroovyClassLoader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.control.CompilationFailedException;
import org.milyn.cdr.SmooksConfigurationException;
import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.cdr.annotation.Configurator;
import org.milyn.delivery.ContentHandler;
import org.milyn.delivery.ContentHandlerFactory;
import org.milyn.delivery.DomModelCreator;
import org.milyn.delivery.Visitor;
import org.milyn.delivery.annotation.Initialize;
import org.milyn.delivery.annotation.Resource;
import org.milyn.delivery.sax.SAXElement;
import org.milyn.io.StreamUtils;
import org.milyn.javabean.context.BeanContext;
import org.milyn.util.FreeMarkerTemplate;
import org.milyn.xml.DomUtils;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link Visitor} Factory class for the <a href="http://groovy.codehaus.org/">Groovy</a> scripting language.
 * <p/>
 * Implement DOM or SAX visitors using the Groovy scripting language.
 *
 * <h2>Usage Tips</h2>
 * <ul>
 *  <li><b>Imports</b>: Imports can be added via the "imports" element.  A number of classes are automatically imported:
 *      <ul>
 *          <li>{@link DomUtils org.milyn.xml.DomUtils}</li>
 *          <li>{@link BeanContext}</li>
 *          <li>{@link org.w3c.dom org.w3c.dom.*}</li>
 *          <li>groovy.xml.dom.DOMCategory, groovy.xml.dom.DOMUtil, groovy.xml.DOMBuilder</li>
 *      </ul>
 *  </li>
 *  <li><b>Visited Element</b>: The visited element is available to the script through the variable "element".  It is also available
 *      under a variable name equal to the element name, but only if the element name contains alpha-numeric
 *      characters only.</li>
 *  <li><b>Execute Before/After</b>: By default, the script is executed on the visitAfter event.  You can direct it to be
 *      executed on the visitBefore by setting the "executeBefore" attribute to "true".</li>
 *  <li><b>Comment/CDATA Script Wrapping</b>: If the script contains special XML characters, it can be wrapped in an XML
 *       Comment or CDATA section.  See example below.</li>
 * </ul>
 *
 * <h2>Mixing SAX and DOM Models</h2>
 * When using the SAX filter, Groovy scripts can take advantage of the {@link DomModelCreator}.  <b>This is only
 * the case when the script is applied on the visitAfter event of the targeted element</b> (i.e. executeBefore="false",
 * which is the default).  If executeBefore is set to "true", the {@link DomModelCreator} will not be utilized.
 * <p/>
 * What this means is that you can use DOM utilities to process the targeted message fragment.  The "element"
 * received by the Groovy script will be a DOM {@link Element}.  This makes Groovy scripting via the SAX filter
 * a lot easier, while at the same time maintaining the ability to process huge messages in a streamed fashion.
 * <p/>
 * <b>Notes</b>:
 * <ol>
 *  <li>Only available in default mode i.e. when executeBefore equals "false".  If executeBefore is configured
 *      "true", this facility is not available and the Groovy script will only have access to the element
 *      as a {@link SAXElement}.</li>
 *  <li>The DOM fragment must be explicitly writen to the result using "<b>writeFragment</b>".  See example below.</li>
 *  <li>There is an obvious performance overhead incurred using this facility (DOM construction).  That said, it can still
 *      be used to process huge messages because of how the {@link DomModelCreator} works for SAX.</li>
 * </ol>
 *
 * <h2>Example Configuration</h2>
 * Take an XML message such as:
 * <pre>
 * &lt;shopping&gt;
 *     &lt;category type="groceries"&gt;
 *         &lt;item&gt;Chocolate&lt;/item&gt;
 *         &lt;item&gt;Coffee&lt;/item&gt;
 *     &lt;/category&gt;
 *     &lt;category type="supplies"&gt;
 *         &lt;item&gt;Paper&lt;/item&gt;
 *         &lt;item quantity="4"&gt;Pens&lt;/item&gt;
 *     &lt;/category&gt;
 *     &lt;category type="present"&gt;
 *         &lt;item when="Aug 10"&gt;Kathryn's Birthday&lt;/item&gt;
 *     &lt;/category&gt;
 * &lt;/shopping&gt;
 * </pre>
 *
 * Using Groovy, we want to modify the "supplies" category in the shopping list, adding 2 to the
 * quantity, where the item is "Pens".  To do this, we write a simple little Groovy script and target
 * it at the &lt;category&gt; elements in the message.  The script simple iterates over the &lt;item&gt; elements
 * in the category and increments the quantity by 2, where the category type is "supplies" and the item is "Pens":
 *
 * <pre>
 * &lt;?xml version="1.0"?&gt;
 * &lt;smooks-resource-list xmlns="http://www.milyn.org/xsd/smooks-1.1.xsd" xmlns:g="<a href="http://www.milyn.org/xsd/smooks/groovy-1.1.xsd">http://www.milyn.org/xsd/smooks/groovy-1.1.xsd</a>"&gt;
 *
 *     &lt;!--
 *     Use the SAX filter.  Note how we can still process the fragment as a DOM, and write it out
 *     to the result stream after processing.
 *     --&gt;
 *     &lt;params&gt;
 *         &lt;param name="stream.filter.type"&gt;SAX&lt;/param&gt;
 *     &lt;/params&gt;
 *
 *     &lt;g:groovy executeOnElement="category"&gt;
 *         &lt;g:script&gt;
 *             &lt;!--
 *             use(DOMCategory) {
 *
 *                 // modify supplies: we need an extra 2 pens
 *                 if (category.'@type' == 'supplies') {
 *                     category.item.each { item -&gt;
 *                         if (item.text() == 'Pens') {
 *                             item['@quantity'] = item.'@quantity'.toInteger() + 2
 *                         }
 *                     }
 *                 }
 *             }
 *
 *             // Must explicitly write the fragment to the result stream when
 *             // using the SAX filter.
 *             writeFragment(category);
 *             --&gt;
 *         &lt;/g:script&gt;
 *     &lt;/g:groovy&gt;
 *
 * &lt;/smooks-resource-list&gt;
 * </pre>
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@Resource(type="groovy")
public class GroovyContentHandlerFactory implements ContentHandlerFactory {

    private static Log logger = LogFactory.getLog(GroovyContentHandlerFactory.class);

    private FreeMarkerTemplate classTemplate;
    private volatile int classGenCount = 1;

    @Initialize
    public void initialize() throws IOException {
        String templateText = StreamUtils.readStreamAsString(getClass().getResourceAsStream("ScriptedGroovy.ftl"));
        classTemplate = new FreeMarkerTemplate(templateText);
    }

    /* (non-Javadoc)
	 * @see org.milyn.delivery.ContentHandlerFactory#create(org.milyn.cdr.SmooksResourceConfiguration)
	 */
	public ContentHandler create(SmooksResourceConfiguration configuration) throws SmooksConfigurationException
  {

        try {
			byte[] groovyScriptBytes = configuration.getBytes();
            String groovyScript = new String(groovyScriptBytes, "UTF-8");

            Object groovyObject;

            GroovyClassLoader groovyClassLoader = new GroovyClassLoader(getClass().getClassLoader());
            try {
                Class groovyClass = groovyClassLoader.parseClass(groovyScript);
                groovyObject = groovyClass.newInstance();
            } catch(CompilationFailedException e) {
                logger.debug("Failed to create Visitor class instance directly from script:\n==========================\n" + groovyScript + "\n==========================\n Will try applying Visitor template to script.", e);
                groovyObject = null;
            }

            if(!(groovyObject instanceof Visitor)) {
                groovyObject = createFromTemplate(groovyScript, configuration);
            }

            ContentHandler groovyResource = (ContentHandler) groovyObject;
            Configurator.configure(groovyResource, configuration);

            return groovyResource;
        } catch (Exception e) {
			throw new SmooksConfigurationException("Error constructing class from Groovy script " + configuration.getResource(), e);
        }
    }

    private Object createFromTemplate(String groovyScript, SmooksResourceConfiguration configuration) throws InstantiationException, IllegalAccessException {
        GroovyClassLoader groovyClassLoader = new GroovyClassLoader(getClass().getClassLoader());
        Map<String, Object> templateVars = new HashMap<String, Object>();
        String imports = configuration.getStringParameter("imports", "");

        templateVars.put("imports", cleanImportsConfig(imports));
        templateVars.put("visitorName", createClassName());
        templateVars.put("elementName", getElementName(configuration));
        templateVars.put("visitBefore", configuration.getBoolParameter("executeBefore", false));
        templateVars.put("visitorScript", groovyScript);

        String templatedClass = classTemplate.apply(templateVars);

        if(groovyScript.contains("writeFragment")) {
            configuration.setParameter("writeFragment", "true");
        }

        try {
            Class groovyClass = groovyClassLoader.parseClass(templatedClass);
            return groovyClass.newInstance();
        } catch(CompilationFailedException e) {
            throw new SmooksConfigurationException("Failed to compile Groovy scripted Visitor class:\n==========================\n" + templatedClass + "\n==========================\n", e);
        }
    }

    private Object cleanImportsConfig(String imports) {
        try {
            StringBuffer importsBuffer = StreamUtils.trimLines(new StringReader(imports));
            imports = importsBuffer.toString();
        } catch (IOException e) {
            throw new IllegalStateException("Unexpected IOException reading String.", e);
        }

        return imports.replace("import ", "\nimport ");
    }

    private synchronized String createClassName() {
        StringBuilder className = new StringBuilder();

        className.append("SmooksVisitor_");
        className.append(System.identityHashCode(this));
        className.append("_");
        className.append(classGenCount++);

        return className.toString();
    }

    private String getElementName(SmooksResourceConfiguration configuration) {
        String elementName = configuration.getTargetElement();

        for (int i = 0; i < elementName.length(); i++) {
            if(!Character.isLetterOrDigit(elementName.charAt(i))) {
                return elementName + "_Mangled";
            }
        }

        return elementName;
    }
}
