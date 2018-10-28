package example;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.milyn.delivery.dom.DOMElementVisitor;
import org.milyn.container.ExecutionContext;
import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.xml.DomUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node
import org.milyn.delivery.dom.DOMVisitAfter;

/**
 * Date Formatting class.
 * <p/>
 * Simply parses a date field and replaces it with date "elements" that can be more easily processed by
 * something else (like xslt).
 *
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class DateFormatter implements DOMVisitAfter {

    private SimpleDateFormat dateDecodeFormat;
    private Properties outputFields;

    public void setConfiguration(SmooksResourceConfiguration configuration) {
        String inputFormat = configuration.getStringParameter("input-format");
        String outputFormats = configuration.getStringParameter("output-format", "time=HH:mm\nday=dd\nmonth=MM\nyear=yy");

        assert inputFormat != null;
        assert inputFormat != '';
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        dateDecodeFormat = new SimpleDateFormat(inputFormat);
        outputFields = parseOutputFields(outputFormats);
    }

    public void visitAfter(Element element, ExecutionContext executionContext) {
        String dateString = null;
        Date date = null;

        // Decode the date string...
        dateString = element.getTextContent();
        try {
            date = dateDecodeFormat.parse(dateString);
        } catch (ParseException e) {
            date = new Date(0);
        }

        // Clear the child contents of the element...
        DomUtils.removeChildren(element);

        // Define a closure that we'll use for adding formatted date fields
        // from the decoded date...
        def addDateField = { fieldName, fieldFormat ->
            Document doc = element.getOwnerDocument();
            Element newElement = doc.createElement(fieldName);
            SimpleDateFormat dateFormatter = new SimpleDateFormat(fieldFormat);

            element.appendChild(newElement);
            newElement.appendChild(doc.createTextNode(dateFormatter.format(date)));
        }

        // Apply the "addDateField" closure to the entries of the outputFields specified as
        // a Smooks resource parameter...
        for (entry in outputFields) {
            addDateField(entry.key, entry.value);
        }
    }

    private Properties parseOutputFields(String outputFormats) {
        Properties properties = new Properties();
        properties.load(new ByteArrayInputStream(outputFormats.getBytes()));
        return properties;
    }
}