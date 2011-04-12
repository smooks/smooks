package org.milyn.templating.jxls;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerConfigurationException;

import net.sf.jxls.transformer.XLSTransformer;

import org.apache.poi.ss.usermodel.Workbook;
import org.milyn.SmooksException;
import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.dom.serialize.TextSerializationUnit;
import org.milyn.delivery.ordering.Consumer;
import org.milyn.event.report.annotation.VisitAfterReport;
import org.milyn.event.report.annotation.VisitBeforeReport;
import org.milyn.templating.AbstractTemplateProcessor;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * <a href="http://jxls.sourceforge.net">jXLS</a> template application ProcessingUnit.
 * <p/>
 * See {@link org.milyn.templating.jxls.JXLSContentHandlerFactory}.
 * <p/>
 * See <a href="http://jxls.sourceforge.net">jxls.sourceforge.net</a>
 * for more info.
 *
 * @author Peter Shen
 */
@VisitBeforeReport(condition = "false")
@VisitAfterReport(summary = "Applied jXLS Template.", detailTemplate = "reporting/JXLSTemplateProcessor_After.html")
public class JXLSTemplateProcessor extends AbstractTemplateProcessor implements Consumer {
	
	private File templateFile;
	private static XLSTransformer transformer = new XLSTransformer();
	

	@Override
	protected void loadTemplate(SmooksResourceConfiguration config)
			throws IOException, TransformerConfigurationException {

		templateFile = new File(config.getResource());		
		
	}

	@Override
	protected void visit(Element element, ExecutionContext executionContext)
			throws SmooksException {
		
		Map<String, Object> beans = executionContext.getBeanContext().getBeanMap();
		Workbook workbook = null;
		try
		{
			workbook = transformer.transformXLS(new FileInputStream(templateFile), beans);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		Node resultNode = TextSerializationUnit.createTextElement(element, workbook.toString());

        // Process the templating action, supplying the templating result...
        processTemplateAction(element, resultNode, executionContext);
	}

	public boolean consumes(Object object) {
		return true;
	}
	

}
