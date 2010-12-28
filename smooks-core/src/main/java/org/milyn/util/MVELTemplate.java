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
package org.milyn.util;

import org.mvel2.templates.CompiledTemplate;
import org.mvel2.templates.TemplateCompiler;
import org.mvel2.templates.TemplateRuntime;
import org.milyn.assertion.AssertArgument;

import java.util.Map;

/**
 *  <a href="http://mvel.codehaus.org/">MVEL</a> template.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
*/
public class MVELTemplate {

    private String template;
    private CompiledTemplate compiledTemplate;

    public MVELTemplate(String template) {
        AssertArgument.isNotNullAndNotEmpty(template, "template");
        this.template = template.replace("${", "@{");
        compiledTemplate = TemplateCompiler.compileTemplate(this.template);
    }

    public String getTemplate() {
        return template;
    }

    public String apply(Object contextObject) {
        return (String) TemplateRuntime.execute(compiledTemplate, contextObject);
    }
}
