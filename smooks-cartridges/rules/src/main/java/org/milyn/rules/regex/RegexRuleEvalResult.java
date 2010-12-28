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
package org.milyn.rules.regex;

import org.milyn.rules.BasicRuleEvalResult;

import java.util.regex.Pattern;

/**
 * Regex RuleEvalResult.
 * This class extends {@link BasicRuleEvalResult} and adds the Regex Pattern
 * and text that te regex was evaluated on.
 *
 * @author <a href="mailto:danielbevenius@gmail.com">Daniel Bevenius</a>
 */
public class RegexRuleEvalResult extends BasicRuleEvalResult
{
    /**
     * Serial unique identifier.
     */
    private static final long serialVersionUID = -3431124009222908170L;

    /**
     * The regex pattern.
     */
    final Pattern pattern;

    /**
     * The text used in the match.
     */
    private String text;

    /**
     * Creates a RuleEvalResult that indicates a successfully executed rule.
     */
    public RegexRuleEvalResult(final boolean matched, final String ruleName, final String ruleProviderName, final Pattern pattern, final String text)
    {
        super(matched, ruleName, ruleProviderName);
        this.pattern = pattern;
        this.text = text;
    }

    /**
     * @return Patten the compiled regular expression.
     */
    public Pattern getPattern()
    {
        return pattern;
    }

    /**
     * @return String the text that the  regular expression was evaluated on/against.
     */
    public String getText() {
        return text;
    }

    @Override
    public String toString()
    {
        return String.format("%s, matched=%b, providerName=%s, ruleName=%s, text=%s, pattern=%s", getClass().getSimpleName(), matched(), getRuleProviderName(), getRuleName(), text, pattern);
    }

}