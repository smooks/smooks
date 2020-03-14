/*
 * Milyn - Copyright (C) 2006 - 2010
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License (version 2.1) as published
 * by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.
 *
 * See the GNU Lesser General Public License for more details:
 * http://www.gnu.org/licenses/lgpl.txt
 */
package org.smooks.rules;

import org.smooks.SmooksException;
import org.smooks.container.ExecutionContext;

/**
 * RuleProvider declares the contract which must be followed to make
 * different types of rule/evaluation technologies work with Smooks.
 * </p>
 *
 * @author <a href="mailto:danielbevenius@gmail.com">Daniel Bevenius</a>
 *
 */
public interface RuleProvider
{
    /**
     * Gets this providers name.
     * @return String This providers name.
     */
    String getName();

    /**
     * The name of this rule provider.
     * @param name The rule providers name.
     */
    void setName(final String name);

    /**
     * Gets the source for this rule provider
     * @return String The src for this rule provider
     */
    String getSrc();

    /**
     * Sets the src for this rule provider.
     * @param src The source which defines the rules.
     */
    void setSrc(final String src);

    /**
     * Evalutate the rule.
     *
     * @param ruleName The ruleName to be used in this evaluation.
     * @param selectedData The data that this evalute method will evaluate upon.
     * @param context The Smooks Excecution context.
     * @return {@code RuleEvalResult} Object representing an evaluation result.
     *
     * @throws SmooksException
     */
    RuleEvalResult evaluate(final String ruleName, final CharSequence selectedData, final ExecutionContext context) throws SmooksException;

}
