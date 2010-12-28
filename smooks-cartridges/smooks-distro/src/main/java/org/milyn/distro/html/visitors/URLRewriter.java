/*
	Milyn - Copyright (C) 2006

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
package org.milyn.distro.html.visitors;

import org.milyn.SmooksException;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.dom.DOMVisitBefore;
import org.w3c.dom.Element;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Anchor URL rewriter.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class URLRewriter implements DOMVisitBefore {

    public void visitBefore(Element element, ExecutionContext executionContext) throws SmooksException {
        try {
            String hrefString = element.getAttribute("href").trim();

            if(hrefString.equals("")) {
                // Ignore...
                return;
            } else if(hrefString.startsWith("#")) {
                // Ignore...
                return;
            } else {
                URI href = new URI(hrefString);
                String fragment = href.getRawFragment();

                if(fragment != null && IdLogger.isInternalId(fragment, executionContext)) {
                    // If it's pointing to an internal object, remove all but the fragment part...
                    element.setAttribute("href", "#" + fragment);
                } else {
                    // Just resolve it against the doc source URI...
                    URI sourceURI = executionContext.getDocumentSource();
                    element.setAttribute("href", sourceURI.resolve(href).toString());
                }
            }
        } catch (URISyntaxException e) {
            System.out.println("Anchor href '" + element.getAttribute("href") + "' is not a valid URI.");
        }
    }
}