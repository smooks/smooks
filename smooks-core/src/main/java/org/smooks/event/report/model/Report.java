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
package org.smooks.event.report.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Execution Report.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class Report {

    private List<ReportInfoNode> configurations = new ArrayList<ReportInfoNode>();
    private List<MessageNode> processings = new ArrayList<MessageNode>();
    private List<ResultNode> results;

    public List<ReportInfoNode> getConfigurations() {
        return configurations;
    }

    public List<MessageNode> getProcessings() {
        return processings;
    }

    public void setResults(List<ResultNode> results) {
        this.results = results;
    }

    public List<ResultNode> getResults() {
        return results;
    }
}
