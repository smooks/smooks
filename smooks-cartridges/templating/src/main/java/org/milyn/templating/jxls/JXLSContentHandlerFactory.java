package org.milyn.templating.jxls;

import org.milyn.cdr.SmooksConfigurationException;
import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.cdr.annotation.AppContext;
import org.milyn.cdr.annotation.Configurator;
import org.milyn.container.ApplicationContext;
import org.milyn.delivery.ContentHandlerFactory;
import org.milyn.delivery.annotation.Resource;
import org.milyn.javabean.context.BeanContext;


/**
 * <a href="http://jxls.sourceforge.net">jXLS</a> templating {@link org.milyn.delivery.Visitor} Creator class.
 * <p/>
 * This templating solution relies on the <a href="http://milyn.codehaus.org/downloads">Smooks JavaBean Cartridge</a>
 * to perform the JavaBean population that's required by <a href="http://jxls.sourceforge.net">jXLS</a> (for the data model).
 *
 * <h2>Targeting "jxls" Templates</h2>
 * The following is the basic configuration specification for jXLS resources:
 * <p/>
 *
 * @author Peter Shen
 */
@Resource(type="jxls")
public class JXLSContentHandlerFactory implements ContentHandlerFactory {

	@AppContext
	private ApplicationContext applicationContext;
	
	/**
	 * Create a jXLS based ContentHandler.
     * @param resourceConfig The SmooksResourceConfiguration for the jXLS.
     * @return The jXLS {@link org.milyn.delivery.ContentHandler} instance.
	 */
	public Object create(SmooksResourceConfiguration resourceConfig)
			throws SmooksConfigurationException, InstantiationException {
		try {
            return Configurator.configure(new JXLSTemplateProcessor(), resourceConfig, applicationContext);
        } catch (SmooksConfigurationException e) {
            throw e;
        } catch (Exception e) {
			InstantiationException instanceException = new InstantiationException("jXLSTemplate ProcessingUnit resource [" + resourceConfig.getResource() + "] not loadable.  jXLSTemplate resource invalid.");
			instanceException.initCause(e);
			throw instanceException;
		}
	}

}
