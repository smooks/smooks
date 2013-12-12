package org.milyn.delivery;

import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.commons.cdr.SmooksConfigurationException;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.dom.DOMElementVisitor;
import org.milyn.delivery.dom.serialize.DefaultSerializationUnit;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

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
