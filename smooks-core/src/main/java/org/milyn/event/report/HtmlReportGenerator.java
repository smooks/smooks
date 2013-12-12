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
package org.milyn.event.report;

import freemarker.template.utility.HtmlEscape;
import org.milyn.commons.util.FreeMarkerTemplate;
import org.milyn.event.report.model.DOMReport;
import org.milyn.event.report.model.Report;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * HTML Execution Report generating {@link org.milyn.event.ExecutionEventListener}.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class HtmlReportGenerator extends AbstractReportGenerator {

    public HtmlReportGenerator(Writer outputWriter) {
        this(new ReportConfiguration(outputWriter));
    }

    public HtmlReportGenerator(String outputFile) throws IOException {
        super(new ReportConfiguration(createOutputWriter(outputFile)));

        File file = new File(outputFile);
        if (file.getParentFile() != null) {
            getReportConfiguration().setTempOutDir(file.getParentFile());
        }
    }

    protected HtmlReportGenerator(ReportConfiguration reportConfiguration) {
        super(reportConfiguration);
    }

    public void applyTemplate(Report report) throws IOException {
        FreeMarkerTemplate template;

        System.out.println();
        System.out.println("****************************************************************************************");
        System.out.println("  HTML REPORT GENERATOR IN USE!!!");
        System.out.println("  Please disable in Production mode.  This feature is a major performance drain!!");
        System.out.println("****************************************************************************************");
        System.out.println();

        if (report instanceof DOMReport) {
            template = new FreeMarkerTemplate("html/template-dom.html", HtmlReportGenerator.class);
        } else {
            template = new FreeMarkerTemplate("html/template-sax.html", HtmlReportGenerator.class);
        }

        Writer writer = getReportConfiguration().getOutputWriter();
        Map templateModel = new HashMap();

        templateModel.put("report", report);
        templateModel.put("htmlEscape", new HtmlEscape());
        writer.write(template.apply(templateModel));
    }

    private static Writer createOutputWriter(String outputFile) throws IOException {
        File file = new File(outputFile);
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }

        return new FileWriter(file);
    }

}