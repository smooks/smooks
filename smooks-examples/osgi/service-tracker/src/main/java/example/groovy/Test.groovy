package example.activator;

import org.milyn.container.ExecutionContext;
import org.milyn.delivery.dom.DOMVisitAfter;

public class Test implements DOMVisitAfter {

    public void visitAfter(Element element, ExecutionContext executionContext) 
    {
        println ("In Test.groovy...");
    }

}