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
package example;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;

import org.milyn.Smooks;
import org.milyn.SmooksException;
import org.milyn.payload.StringSource;
import org.milyn.rules.RuleEvalResult;
import org.milyn.container.ExecutionContext;
import org.milyn.event.report.HtmlReportGenerator;
import org.milyn.io.StreamUtils;
import org.milyn.validation.ValidationResult;
import org.milyn.validation.OnFailResult;
import org.xml.sax.SAXException;

/**
 * Simple example main class.
 *
 * @author <a href="mailto:daniel.bevenius@gmail.com">Daniel Bevenius</a>
 */
public class Main {
    public static void main(final String... args) throws IOException, SAXException, SmooksException {
        final String messageIn = readInputMessage();

        // Uncomment to enable a Swedish locale.
        //Locale.setDefault(new Locale("sv", "SE"));

        System.out.println("\n\n==============Message In==============");
        System.out.println(new String(messageIn));
        System.out.println("======================================");

        final ValidationResult results = Main.runSmooks(messageIn);

        System.out.println("\n==============Validation Result=======");
        System.out.println("Errors:");
        for (OnFailResult result : results.getErrors()) {
        	RuleEvalResult rule = result.getFailRuleResult();
            System.out.println("\t" + rule.getRuleName() + ": " + result.getMessage());
            System.out.println("\tSwedish:");
            System.out.println("\t" + result.getMessage(new Locale("sv", "SE")));
        }

        System.out.println("Warnings:");
        for (OnFailResult result : results.getWarnings()) {
            System.out.println("\t" + result.getMessage());
            System.out.println("\tSwedish:");
            System.out.println("\t" + result.getMessage(new Locale("sv", "SE")));
        }

        System.out.println("======================================\n");
    }

    protected static ValidationResult runSmooks(final String messageIn) throws IOException, SAXException, SmooksException {
        // Instantiate Smooks with the config...
        final Smooks smooks = new Smooks("smooks-config.xml");

        try {
            // Create an exec context - no profiles....
            final ExecutionContext executionContext = smooks.createExecutionContext();
            final ValidationResult validationResult = new ValidationResult();

            // Configure the execution context to generate a report...
            executionContext.setEventListener(new HtmlReportGenerator("target/report/report.html"));

            // Filter the input message...
            smooks.filterSource(executionContext, new StringSource(messageIn), validationResult);

            return validationResult;
        }
        finally {
            smooks.close();
        }
    }

    protected static String readInputMessage() {
        try {
            return StreamUtils.readStreamAsString(new FileInputStream("input-message.xml"));
        } catch (final IOException e) {
            throw new RuntimeException("Error reading input message.", e);
        }
    }
}
