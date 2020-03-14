package org.smooks.general;

import org.smooks.delivery.sax.SAXElementVisitor;
import org.smooks.delivery.sax.SAXElement;
import org.smooks.delivery.sax.SAXText;
import org.smooks.delivery.sax.SAXVisitAfter;
import org.smooks.container.ExecutionContext;
import org.smooks.SmooksException;

import java.io.IOException;

/**
 * @author
 */
public class SAXVisitor implements SAXVisitAfter {

    public static boolean match = false;

    public void visitAfter(SAXElement saxElement, ExecutionContext executionContext) throws SmooksException, IOException {
        match = true;
    }
}
