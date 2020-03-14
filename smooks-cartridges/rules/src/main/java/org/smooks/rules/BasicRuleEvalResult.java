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
package org.smooks.rules;

import java.io.Serializable;

/**
 * Basic rule evaluation result.
 *
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class BasicRuleEvalResult implements RuleEvalResult, Serializable {

    private static final long serialVersionUID = 6702697098759533150L;
    private boolean matched;
    private String ruleName;
    private String ruleProviderName;
    private Throwable evalException;

    public BasicRuleEvalResult(boolean matched, String ruleName, String ruleProviderName) {
        this.matched = matched;
        this.ruleName = ruleName;
        this.ruleProviderName = ruleProviderName;
    }

    public BasicRuleEvalResult(Throwable evalException, String ruleName, String ruleProviderName) {
        this.matched = false;
        this.ruleName = ruleName;
        this.ruleProviderName = ruleProviderName;
        this.evalException = evalException;
    }

    public boolean matched() {
        return matched;
    }

    public String getRuleName() {
        return ruleName;
    }

    public String getRuleProviderName() {
        return ruleProviderName;
    }

    public Throwable getEvalException() {
        return evalException;
    }
}
