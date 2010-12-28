package example.groovy;

import org.milyn.container.ExecutionContext;
import org.milyn.delivery.sax.SAXVisitAfter;
import org.milyn.delivery.sax.SAXElement;

public class Test implements SAXVisitAfter {

    public void visitAfter(Element element, ExecutionContext executionContext) 
    {
        println ("In Test.groovy..." + k)
    }

}