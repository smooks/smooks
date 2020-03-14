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
package org.smooks.templating;

import org.smooks.assertion.AssertArgument;

/**
 * Templating Configuration.
 * <p/>
 * Allow programmatic configuration of a {@link org.smooks.templating.AbstractTemplateProcessor}
 * implementation.
 * 
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class TemplatingConfiguration {

    private String template;
    private boolean applyBefore = false;
    private Usage usage = Inline.REPLACE;

    /**
     * Public constructor.
     * @param template The template.  This can be a URI referencing the template resource,
     * or can be an inlined template.
     */
    public TemplatingConfiguration(String template) {
        AssertArgument.isNotNullAndNotEmpty(template, "template");
        this.template = template;
    }

    /**
     * Get the template.
     * @return The template.  This can be a URI referencing the template resource,
     * or can be an inlined template.
     */
    protected String getTemplate() {
        return template;
    }

    /**
     * Should the template be applied at the start of the fragment and before processing
     * any of the fragment's child content.
     * @return True if the template is to be applied at the start of the fragment, otherwise false.
     */
    protected boolean applyBefore() {
        return applyBefore;
    }

    /**
     * Set whether or not the template should be applied at the start of the fragment and before processing
     * any of the fragment's child content.
     * @param applyBefore True if the template is to be applied at the start of the fragment, otherwise false.
     * @return This instance.
     */
    public TemplatingConfiguration setApplyBefore(boolean applyBefore) {
        this.applyBefore = applyBefore;
        return this;
    }

    /**
     * Get the templating {@link Usage} directive for the templating result.
     * @return The templating result usage.
     */
    protected Usage getUsage() {
        return usage;
    }

    /**
     * Set the templating {@link Usage} directive for the templating result.
     * @param usage The templating result usage.
     * @return This instance.
     */
    public TemplatingConfiguration setUsage(Usage usage) {
        AssertArgument.isNotNull(usage, "usage");
        this.usage = usage;
        return this;
    }
}
