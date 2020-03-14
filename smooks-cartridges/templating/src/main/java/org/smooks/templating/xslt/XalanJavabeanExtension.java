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
package org.smooks.templating.xslt;

import ognl.MemberAccess;
import ognl.Ognl;
import ognl.OgnlException;
import org.apache.xalan.extensions.XSLProcessorContext;
import org.apache.xalan.templates.AVT;
import org.apache.xalan.templates.ElemExtensionCall;
import org.smooks.container.ExecutionContext;
import org.smooks.delivery.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Hashtable;
import java.util.Map;

/**
 * Javabean access <a href="http://xml.apache.org/xalan-j/">Xalan</a> XSLT extension for XSLT templating.
 * <p/>
 * Provides XSLT template population using <a href="http://www.ognl.org/">OGNL</a> expressions
 * embedded in an XSLT element or function extension.  The <a href="http://www.ognl.org/">OGNL</a> expressions
 * are targeted at the Javabean data gathered through use of the
 * <a href="http://milyn.codehaus.org/downloads">Smooks JavaBean Cartridge</a>.
 * <p/>
 * <h3 id="usage">Usage</h3>
 * <pre>
 * &lt;xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 *                 xmlns:xalan="http://xml.apache.org/xalan"
 * 		xmlns:smooks-bean="org.smooks.templating.xslt.XalanJavabeanExtension"
 * 		extension-element-prefixes="smooks-bean"
 * 		version="1.0"&gt;
 *
 * 	&lt;xsl:template match="*"&gt;
 * 		&lt;!-- Using the XSLT extension element... --&gt;
 * 		&lt;smooks-bean:select ognl="<a href="http://www.ognl.org/">ognl-expression</a>" /&gt;
 *
 * 		&lt;!-- Using the XSLT extension function... --&gt;
 * 		&lt;xsl:value-of select="smooks-bean:select('<a href="http://www.ognl.org/">ognl-expression</a>')"/&gt;
 *
 * 	&lt;/xsl:template&gt;
 *
 * &lt;/xsl:stylesheet&gt;</pre>
 *
 * @author tfennelly
 */
public class XalanJavabeanExtension {

    private static final Logger LOGGER = LoggerFactory.getLogger(XalanJavabeanExtension.class);
    private static final MemberAccess MEMBER_ACCESS = new DefaultMemberAccess();

    /**
     * Static cache of preparsed expressions.
     */
    private static Hashtable<String,Object> expressionCache = new Hashtable<String,Object>();

    /**
     * Support OGNL based bean value injection via an XSLT extension element.
     * <p/>
     * The <a href="http://www.ognl.org/">OGNL</a> expression is expected to be specified in
     * the "ognl" attribute.
     * <p/>
     * See <a href="#usage">Usage</a>.
     *
     * @param context Processor context.
     * @param element Extension element instance.
     * @return The bean value, or null if the bean is unknown.
     * @throws OgnlException Extension element syntax is incorrectly formed, or the
     *                       <a href="http://www.ognl.org/">OGNL</a> expression is unspecified or its
     *                       syntax is incorrectly formed.
     */
    public Object select(XSLProcessorContext context, ElemExtensionCall element) throws OgnlException {
        AVT ognlAVT = element.getLiteralResultAttribute("ognl");

        if (ognlAVT == null) {
            throw new OgnlException("'ognl' expression attribute not specified.");
        }

        return select(ognlAVT.getSimpleString());
    }

    /**
     * Support OGNL based bean value injection via an XSLT extension function.
     * <p/>
     * The <a href="http://www.ognl.org/">OGNL</a> expression is expected to be specified in
     * the function call.
     * <p/>
     * See <a href="#usage">Usage</a>.
     *
     * @param ognlExpression <a href="http://www.ognl.org/">OGNL</a> expression.
     * @return The bean value, or null if the bean is unknown.
     * @throws OgnlException <a href="http://www.ognl.org/">OGNL</a> expression is unspecified or its
     *                       syntax is incorrectly formed.
     */
    public Object select(String ognlExpression) throws OgnlException {
        if (ognlExpression == null || (ognlExpression = ognlExpression.trim()).equals("")) {
            throw new OgnlException("'ognl' expression not specified, or is blank.");
        }

        ExecutionContext activeRequest = Filter.getCurrentExecutionContext();

        if (activeRequest == null) {
            String message = getClass().getName() + " can only be used within the context of a SmooksDOMFilter operation..";
            LOGGER.error(message);
            throw new IllegalStateException(message);
        }

        Map<String, Object> beans = activeRequest.getBeanContext().getBeanMap();
        Object parsedExpression = expressionCache.get(ognlExpression);

        if (parsedExpression == null) {
            try {
                // Parse and store the expression...
                parsedExpression = Ognl.parseExpression(ognlExpression);
                expressionCache.put(ognlExpression, parsedExpression);
            } catch (OgnlException e) {
                LOGGER.error("Exception parsing OGNL expression [" + ognlExpression + "].  Make sure the expression is properly constructed (http://www.ognl.org).", e);
                throw e;
            }
        }

        try {
            return Ognl.getValue(parsedExpression, Ognl.createDefaultContext(beans, MEMBER_ACCESS), beans);
        } catch (OgnlException e) {
            LOGGER.error("Unexpected exception using OGNL expression [" + ognlExpression + "] on Smooks Javabean cache.", e);
            throw e;
        }
    }
}
