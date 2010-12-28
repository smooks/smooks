package org.milyn.cdr;

import org.milyn.cdr.annotation.ConfigParam;
import org.milyn.delivery.sax.SAXVisitBefore;
import org.milyn.delivery.sax.SAXElement;
import org.milyn.container.ExecutionContext;
import org.milyn.SmooksException;

import java.io.IOException;

/**
 * @author
 */
public class ConfigurableVisitor implements SAXVisitBefore {

    private String stringParam;
    private Integer intParam;
    private String optionalStringParam;
    private int otherIntProp;

    @ConfigParam
    public ConfigurableVisitor setStringParam(String stringParam) {
        this.stringParam = stringParam;
        return this;
    }

    @ConfigParam(defaultVal = "5")
    public ConfigurableVisitor setIntParam(Integer intParam) {
        this.intParam = intParam;
        return this;
    }

    @ConfigParam(use = ConfigParam.Use.OPTIONAL)
    public ConfigurableVisitor setOptionalStringParam(String stringParam) {
        this.optionalStringParam = stringParam;
        return this;
    }

    public String getStringParam() {
        return stringParam;
    }

    public int getIntParam() {
        return intParam;
    }

    public String getOptionalStringParam() {
        return optionalStringParam;
    }

    public void visitBefore(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
    }
}
