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

package org.milyn.validation;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.milyn.assertion.AssertArgument;
import org.milyn.container.ExecutionContext;
import org.milyn.rules.RuleEvalResult;
import org.milyn.payload.FilterResult;

/**
 * ValidationResult object for capturing validation failures
 * at different levels.
 *
 * @author <a href="mailto:danielbevenius@gmail.com">Daniel Bevenius</a>
 */
public class ValidationResult extends FilterResult
{
    /**
     * The validation result Map, keyed by OnFail Type.
     */
    private Map<OnFail, List<OnFailResult>> results = new HashMap<OnFail, List<OnFailResult>>();

    /**
     * Fatal failure result.
     */
    private OnFailResult fatal; 

    /**
     * Public default constructor.
     */
    public ValidationResult()
    {
        results.put(OnFail.OK, new ArrayList<OnFailResult>());
        results.put(OnFail.WARN, new ArrayList<OnFailResult>());
        results.put(OnFail.ERROR, new ArrayList<OnFailResult>());
    }

    /**
     * Gets all the {@link OnFailResult}s that were reported at the {@link OnFail#OK}
     * level.
     *
     * @return List {@link OnFailResult} reported at {@link OnFail#OK}.
     */
    public List<OnFailResult> getOKs()
    {
        return Collections.unmodifiableList(results.get(OnFail.OK));
    }

    /**
     * Gets all the {@link OnFailResult}s that were reported at the {@link OnFail#WARN}
     * level.
     *
     * @return List of {@link OnFailResult} reported at {@link OnFail#WARN}.
     */
    public List<OnFailResult> getWarnings()
    {
        return Collections.unmodifiableList(results.get(OnFail.WARN));
    }

    /**
     * Gets all the {@link OnFailResult}s that were reported at the {@link OnFail#ERROR}
     * level.
     *
     * @return List of {@link OnFailResult} reported at {@link OnFail#ERROR}.
     */
    public List<OnFailResult> getErrors()
    {
        return Collections.unmodifiableList(results.get(OnFail.ERROR));
    }

    /**
     * Gets the {@link OnFailResult} that was reported as a {@link OnFail#FATAL}.
     * <p/>
     * Can only be one {@link OnFail#FATAL}.
     *
     * @return {@link OnFail#FATAL} {@link OnFailResult} if one occured, otherwise null.
     */
    public OnFailResult getFatal()
    {
        return fatal;
    }

    /**
     * Get the total number of failures on this {@link org.milyn.validation.ValidationResult} instance.
     * @return The total number of failures on this {@link org.milyn.validation.ValidationResult} instance.
     */
    public int getNumFailures() {
        int numFailures = 0;
        Collection<List<OnFailResult>> values = results.values();

        for(List<OnFailResult> value : values) {
            numFailures += value.size();
        }

        if(fatal != null) {
            numFailures++;
        }

        return numFailures;
    }

    /**
     * Adds the {@link OnFailResult} with {@link OnFail} level passed in.
     *
     * @param result The {@link OnFailResult}. Cannot be null.
     * @param onFail The {@link OnFail} level for which this rule should be reported.
     */
    protected void addResult(final OnFailResult result, final OnFail onFail)
    {
        AssertArgument.isNotNull(result, "result");
        AssertArgument.isNotNull(onFail, "onFail");

        if(onFail == OnFail.FATAL) {
            fatal = result;
        } else {
            // Add the OnFailResult to the specific list.
            results.get(onFail).add(result);
        }
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("OK Failures:\n");
        addResultMessages(getOKs(), stringBuilder);
        stringBuilder.append("WARN Failures:\n");
        addResultMessages(getWarnings(), stringBuilder);
        stringBuilder.append("ERROR Failures:\n");
        addResultMessages(getErrors(), stringBuilder);
        stringBuilder.append("FATAL Failure:\n");
        if(fatal != null) {
            stringBuilder.append("\t" + fatal.getMessage() + "\n");
        } else {
            stringBuilder.append("\t(none)\n");
        }

        return stringBuilder.toString();
    }

    private void addResultMessages(List<OnFailResult> results, StringBuilder stringBuilder) {
        if(results.isEmpty()) {
            stringBuilder.append("\t(none)\n");
        } else {
            for(OnFailResult result : results) {
                stringBuilder.append("\t- " + result.getMessage() + "\n");
            }
        }
    }
}