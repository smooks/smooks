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
package org.milyn.rules.mvel;

import au.com.bytecode.opencsv.CSVReader;
import org.milyn.SmooksException;
import org.milyn.cdr.SmooksConfigurationException;
import org.milyn.container.ExecutionContext;
import org.milyn.expression.ExpressionEvaluator;
import org.milyn.expression.MVELExpressionEvaluator;
import org.milyn.resource.URIResourceLocator;
import org.milyn.rules.RuleEvalResult;
import org.milyn.rules.RuleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <a href="http://mvel.codehaus.org/">MVEL</a> Rule Provider.
 * <p/>
 * Rules must be specified in Comma Separated Value files (CSV).  These can be edited
 * using a Spreadsheet application (Excel or OpenOffice).
 *
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class MVELProvider implements RuleProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(MVELProvider.class);
    private String name;
    private String src;
    private Map<String, ExpressionEvaluator> rules = new HashMap<String, ExpressionEvaluator>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
        loadRules();
    }

    public RuleEvalResult evaluate(String ruleName, CharSequence selectedData, ExecutionContext context) throws SmooksException {
        ExpressionEvaluator evaluator = rules.get(ruleName);

        if (evaluator == null) {
            throw new SmooksException("Unknown rule name '" + ruleName + "' on MVEL RuleProvider '" + name + "'.");
        }

        try {
            return new MVELRuleEvalResult(evaluator.eval(context.getBeanContext().getBeanMap()), ruleName, name, selectedData.toString());
        } catch(Throwable t) {
            return new MVELRuleEvalResult(t, ruleName, name, selectedData.toString());
        }
    }

    @SuppressWarnings("unchecked")
	private void loadRules() {
        if (src == null) {
            throw new SmooksException("ruleFile not specified.");
        }

        InputStream ruleStream;

        // Get the input stream...
        try {
            ruleStream = new URIResourceLocator().getResource(src);
        }
        catch (final IOException e) {
            throw new SmooksException("Failed to open rule file '" + src + "'.", e);
        }

        CSVReader csvLineReader = new CSVReader(new InputStreamReader(ruleStream));
        List<String[]> entries;
        try {
            entries = csvLineReader.readAll();
        } catch (IOException e) {
            throw new SmooksConfigurationException("Error reading MVEL rule file (CSV format) '" + src + "'.", e);
        } finally {
            try {
                ruleStream.close();
            } catch (IOException e) {
                LOGGER.debug("Error closing MVEL rule file '" + src + "'.", e);
            }
        }

        for (String[] ruleLine : entries) {
            if(ruleLine.length == 2 && ruleLine[0].trim().charAt(0) != '#') {
                String ruleName = ruleLine[0].trim();
                String ruleExpression = ruleLine[1];

                if(rules.containsKey(ruleName)) {
                    LOGGER.debug("Duplicate rule definition '" + ruleName + "' in MVEL rule file '" + ruleName + "'.  Ignoring duplicate.");
                    continue;
                }

                rules.put(ruleName, new MVELExpressionEvaluator().setExpression(ruleExpression));
            }
        }
    }
}
