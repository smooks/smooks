package org.smooks.delivery;

import org.smooks.cdr.SmooksResourceConfiguration;
import org.smooks.cdr.SmooksConfigurationException;
import org.smooks.delivery.dom.serialize.DefaultSerializationUnit;
import org.smooks.delivery.dom.DOMElementVisitor;
import org.smooks.container.ExecutionContext;
import org.w3c.dom.Element;

import java.util.List;
import java.util.ArrayList;

/**
 * @author
 */
public class TestExpandableContentHandler implements DOMElementVisitor, ConfigurationExpander {

    public void setConfiguration(SmooksResourceConfiguration resourceConfig) throws SmooksConfigurationException {
    }

    public List<SmooksResourceConfiguration> expandConfigurations() {

        List<SmooksResourceConfiguration> expansionConfigs = new ArrayList<SmooksResourceConfiguration>();

        expansionConfigs.add(new SmooksResourceConfiguration("a", Assembly1.class.getName()));
        expansionConfigs.add(new SmooksResourceConfiguration("b", Processing1.class.getName()));        
        expansionConfigs.add(new SmooksResourceConfiguration("c", DefaultSerializationUnit.class.getName()));

        return expansionConfigs;
    }

    public void visitBefore(Element element, ExecutionContext executionContext) {
    }

    public void visitAfter(Element element, ExecutionContext executionContext) {
    }
}
