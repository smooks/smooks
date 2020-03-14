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

package org.smooks.validation;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.Test;
import org.smooks.Smooks;
import org.smooks.container.ExecutionContext;
import org.smooks.io.StreamUtils;
import org.smooks.payload.StringResult;
import org.smooks.payload.StringSource;
import org.xml.sax.SAXException;

/**
 * Function test for {@link Validator}
 *
 * @author <a href="mailto:danielbevenius@gmail.com">Daniel Bevenius</a>
 */
public class ValidatorFunctionTest
{
    @Test
    public void filter() throws IOException, SAXException
    {
        InputStream config = null;
        try
        {
            config = getSmooksConfig("smooks-validation-config.xml");
            final Smooks smooks = new Smooks(config);

            final String xml = readStringFromFile("validation-test.xml");

            final ExecutionContext context = smooks.createExecutionContext();
            final StringResult result = new StringResult();
            final ValidationResult validationResult = new ValidationResult();

            smooks.filterSource(context, new StringSource(xml), result, validationResult);

            final List<OnFailResult> warnings = validationResult.getWarnings();

            assertEquals(1, warnings.size());
            assertEquals(0, validationResult.getOKs().size());
            assertEquals(0, validationResult.getErrors().size());

        }
        finally
        {
            if (config != null)
                config.close();
        }
    }

    private InputStream getSmooksConfig(final String fileName)
    {
        return getClass().getResourceAsStream("/smooks-configs/extended/1.0/" + fileName);
    }

    private String readStringFromFile(final String fileName) throws IOException
    {
        return StreamUtils.readStreamAsString(getClass().getResourceAsStream("/test-input-files/" + fileName));
    }
}
