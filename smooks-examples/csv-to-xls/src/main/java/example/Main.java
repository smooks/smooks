package example;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.xml.transform.stream.StreamSource;

import org.apache.poi.ss.usermodel.Workbook;
import org.milyn.Smooks;
import org.milyn.SmooksException;
import org.milyn.container.ExecutionContext;
import org.milyn.event.report.HtmlReportGenerator;
import org.milyn.io.StreamUtils;
import org.milyn.payload.JavaResult;
import org.xml.sax.SAXException;

/**
 * Simple example main class.
 * @author Peter Shen
 */
public class Main {

    private static byte[] messageIn = readInputMessage();

    protected static String runSmooksTransform() throws IOException, SAXException, SmooksException {

        Smooks smooks = new Smooks("smooks-config.xml");

        try {
            ExecutionContext executionContext = smooks.createExecutionContext();
            JavaResult result = new JavaResult();
            
            // Configure the execution context to generate a report...
            executionContext.setEventListener(new HtmlReportGenerator("target/report/report.html"));

            smooks.filterSource(executionContext, new StreamSource(new InputStreamReader(new ByteArrayInputStream(messageIn), "UTF-8")), result);
            
            Workbook workbook = result.getBean(Workbook.class);
            File targetFile = new File("target.xls");
            java.io.FileOutputStream fos = new FileOutputStream(targetFile);
            workbook.write(fos);
            fos.flush();
            fos.close();
            
            return targetFile.getAbsolutePath();
        } finally {
            smooks.close();
        }
    }

    public static void main(String[] args) throws IOException, SAXException, SmooksException {
        System.out.println("\n\n==============Message In==============");
        System.out.println(new String(messageIn));
        System.out.println("======================================\n");
        
        String messageOut = Main.runSmooksTransform();

        System.out.println("==============Message Out=============");
        System.out.println(messageOut);
        System.out.println("======================================\n\n");
    }

    private static byte[] readInputMessage() {
        try {
            return StreamUtils.readStream(new FileInputStream("input-message.csv"));
        } catch (IOException e) {
            e.printStackTrace();
            return "<no-message/>".getBytes();
        }
    }
}
