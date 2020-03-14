package example.groovy;

import org.smooks.container.ExecutionContext;
import org.smooks.delivery.sax.SAXVisitAfter;
import org.smooks.delivery.sax.SAXElement;

public class Test implements SAXVisitAfter {

    public void visitAfter(Element element, ExecutionContext executionContext) 
    {
        println ("In Test.groovy..." + k)
    }

}