package example;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.transform.stream.StreamSource;

import org.milyn.Smooks;
import org.milyn.container.ExecutionContext;
import org.milyn.io.StreamUtils;
import org.milyn.payload.JavaResult;

import example.model.Order;
import example.model.OrderItem;

public class ExampleUtil
{
    private ExampleUtil()
    {
    }

    public static void performFiltering(String input, Smooks smooks)
    {
        ExecutionContext executionContext = smooks.createExecutionContext();
        JavaResult result = new JavaResult();
        StreamSource source = new StreamSource(new ByteArrayInputStream(readFileContents(input)));
        smooks.filterSource(executionContext, source, result);
            
        Order order = (Order) result.getBean("order");
        printOrder(order);
    }
    
    public static void printOrder(Order order)
    {
        System.out.println("============Order Javabeans===========");
        System.out.println("Header - Customer Name: " + order.getHeader().getCustomerName());
        System.out.println("       - Customer Num:  " + order.getHeader().getCustomerNumber());
        System.out.println("       - Order Date:    " + order.getHeader().getDate());
        System.out.println("\n");
        System.out.println("Order Items:");
        for(int i = 0; i < order.getOrderItems().size(); i++) {
            OrderItem orderItem = order.getOrderItems().get(i);
            System.out.println("       (" + (i + 1) + ") Product ID:  " + orderItem.getProductId());
            System.out.println("       (" + (i + 1) + ") Quantity:    " + orderItem.getQuantity());
            System.out.println("       (" + (i + 1) + ") Price:       " + orderItem.getPrice());
        }
        System.out.println("======================================");
        System.out.println("\n\n");
    }
    
    public static byte[] readFileContents(final String file) {
        try {
            return StreamUtils.readStream(new FileInputStream(file));
        } catch (IOException e) {
            e.printStackTrace();
            return "<no-message/>".getBytes();
        }
    }
        

}
