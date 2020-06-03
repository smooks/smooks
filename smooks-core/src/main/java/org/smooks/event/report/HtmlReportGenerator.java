/*-
 * ========================LICENSE_START=================================
 * Smooks Core
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0 or,
 * the GNU Lesser General Public License version 3.0 or later.
 * 
 * SPDX-License-Identifier: Apache-2.0 OR LGPL-3.0-or-later
 * 
 * ======================================================================
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ======================================================================
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * =========================LICENSE_END==================================
 */
package org.smooks.event.report;

import freemarker.template.utility.HtmlEscape;
import org.smooks.event.report.model.DOMReport;
import org.smooks.event.report.model.Report;
import org.smooks.util.FreeMarkerTemplate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * HTML Execution Report generating {@link org.smooks.event.ExecutionEventListener}.
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
        if(file.getParentFile() != null) {
            getReportConfiguration().setTempOutDir(file.getParentFile());
        }
    }

    protected HtmlReportGenerator(ReportConfiguration reportConfiguration) {
        super(reportConfiguration);
    }

    @SuppressWarnings("unchecked")
    public void applyTemplate(Report report) throws IOException {
        FreeMarkerTemplate template;

        System.out.println();
        System.out.println("****************************************************************************************");
        System.out.println("  HTML REPORT GENERATOR IN USE!!!");
        System.out.println("  Please disable in Production mode.  This feature is a major performance drain!!");
        System.out.println("****************************************************************************************");
        System.out.println();

        if(report instanceof DOMReport) {
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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static Writer createOutputWriter(String outputFile) throws IOException {
        File file = new File(outputFile);
        if(file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }

        return new FileWriter(file);
    }

}
