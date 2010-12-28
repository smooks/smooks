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

import org.milyn.SmooksException;

/**
 * Exception that carries information about a validation failure.
 *
 * @author <a href="mailto:danielbevenius@gmail.com">Daniel Bevenius</a>
 */
public class ValidationException extends SmooksException
{
    /**
     * Serial version unique identifier.
     */
    private static final long serialVersionUID = 1L;

    /**
     *
     */
    private OnFailResult result;

    /**
     * Public constructor.
     *
     * @param message The exception message.
     */
    public ValidationException(final String message)
    {
        super(message);
    }

    public ValidationException(final String message, final OnFailResult result)
    {
        super(message);
        this.result = result;
    }

    public OnFailResult getOnFailResult()
    {
        return result;
    }
}
