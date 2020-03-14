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

package org.smooks.container.plugin;

import javax.xml.transform.Result;
import javax.xml.transform.Source;

/**
 * {@link Source} - {@link Result} value Object.
 * <p/>
 * This class allows users of the {@link PayloadProcessor} class to explicitly specify
 * both the {@link Source} and {@link Result} payload carrier types.  This can be used
 * in situations where the required {@link Source} or {@link Result} are not supported
 * amoung the default payload types supported by the {@link PayloadProcessor}
 * (for the {@link Source}), or by the {@link ResultType} (for the {@link Result}).
 *
 * @author <a href="mailto:daniel.bevenius@gmail.com">Daniel Bevenius</a>
 * @since 1.0
 */
public class SourceResult {
    private Source source;
    private Result result;

    public SourceResult() {
    }

    public SourceResult(final Source source, final Result result) {
        this.source = source;
        this.result = result;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(final Source source) {
        this.source = source;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(final Result result) {
        this.result = result;
    }

}
