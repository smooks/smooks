/*
 * Milyn - Copyright (C) 2006 - 2010
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License (version 2.1) as published
 * by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.
 *
 * See the GNU Lesser General Public License for more details:
 * http://www.gnu.org/licenses/lgpl.txt
 */
package org.milyn.validation;

import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.milyn.SmooksException;
import org.milyn.resource.URIResourceLocator;
import org.milyn.util.FreeMarkerTemplate;
import org.milyn.xml.DomUtils;
import org.milyn.event.report.annotation.VisitBeforeReport;
import org.milyn.event.report.annotation.VisitAfterReport;
import org.milyn.payload.FilterResult;
import org.milyn.cdr.annotation.AppContext;
import org.milyn.cdr.annotation.ConfigParam;
import org.milyn.cdr.annotation.Config;
import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.cdr.SmooksConfigurationException;
import org.milyn.container.ApplicationContext;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.annotation.Initialize;
import org.milyn.delivery.dom.DOMVisitAfter;
import org.milyn.delivery.sax.SAXElement;
import org.milyn.delivery.sax.SAXVisitAfter;
import org.milyn.delivery.sax.SAXVisitBefore;
import org.milyn.delivery.sax.SAXUtil;
import org.milyn.rules.RuleEvalResult;
import org.milyn.rules.RuleProvider;
import org.milyn.rules.RuleProviderAccessor;
import org.w3c.dom.Element;

/**
 *
 * </p>
 * A Validator uses a predefined Rule that performs the actual validator for a Validator. This way a Validator does not know
 * about the technology used for the validation and users can mix and max different rules as appropriate to the use case they
 * have. For example, one problem might be solve nicely with a regular expression but another might be easier to sovle using
 * a MVEL expression.
 *
 * Example configuration:
 * <pre>{@code
 * <rules:ruleBases>
 *    <rules:ruleBase name="addressing" src="usa_address.properties" provider="org.milyn.smooks.validation.RegexProvider" />
 * </rules:ruleBases>
 *
 * <validation:field on="order/header/email" rule="addressing.email" onFail="WARN" />
 *
 * }</pre>
 * Options:
 * <ul>
 *  <li><b><i>on</b></i>
 *  The fragement that the validation will be performed upon. </li>
 *
 *  <li><b><i>rule</b></i>
 *  Is the name of a previously defined in a rules element. The rule itself is identified by ruleProviderName.ruleName.
 *  So taking the above example addressing is the ruleProviderName and email is the rule name. In this case email
 *  identifies a regular expression but if you were to change the provider that might change and a differnet technology
 *  could be used to validate an email address.</li>
 *
 *  <li><b><i>onFail</b></i>
 *  The onFail attribute in the validation configuration specified what action should be taken when a rule matches.
 *  This is all about reporting back valdiation failures.
 *  </li>
 *
 * </ul>
 *
 * @author <a href="mailto:daniel.bevenius@gmail.com">Daniel Bevenius</a>
 *
 */
@VisitBeforeReport(condition = "false")
@VisitAfterReport(summary = "Applied validation rule '${resource.parameters.name}'.")
public final class Validator implements SAXVisitBefore, SAXVisitAfter, DOMVisitAfter
{
    private static Log logger = LogFactory.getLog(Validator.class);

    /**
     * The name of the rule that will be used by this validator.
     */
    private String compositRuleName;
    /**
     * Rule provider name.
     */
    private String ruleProviderName;
    /**
     * Rule name.
     */
    private String ruleName;
    /**
     * Rule provider for this validator.
     */
    private RuleProvider ruleProvider;
    /**
     * The validation failure level. Default is OnFail.ERROR.
     */
    private OnFail onFail = OnFail.ERROR;
    /**
     * The Smooks {@link ApplicationContext}.
     */
    @AppContext
    private ApplicationContext appContext;
    /**
     * Config.
     */
    @Config
    private SmooksResourceConfiguration config;
    /**
     * Attribute name if the validation target is an attribute, otherwise null.
     */
    private String targetAttribute;

    /**
     * Message bundle name for the ruleset.
     */
    private String messageBundleBaseName;
    /**
     * The maximum number of failures permitted per {@link ValidationResult} instance..
     */
    private int maxFails;

    /**
     * No-args constructor required by Smooks.
     */
    public Validator() {}

    /**
     * Initialize the visitor instance.
     */
    @Initialize
    public void initialize() {
        targetAttribute = config.getTargetAttribute();

    }

    /**
     * Public constructor.
     * @param compositRuleName The name of the rule that will be used by this validator.
     * @param onFail The failure level.
     */
    public Validator(final String compositRuleName, final OnFail onFail)
    {
        setCompositRuleName(compositRuleName);
        this.onFail = onFail;
    }

    public void visitBefore(final SAXElement element, final ExecutionContext executionContext) throws SmooksException, IOException {
        if(targetAttribute == null) {
            // The selected text is not an attribute, which means it's the element text,
            // which means we need to turn on text accumulation for SAX...
            element.accumulateText();
        }
    }

    public void visitAfter(final SAXElement element, final ExecutionContext executionContext) throws SmooksException, IOException
    {
        if(targetAttribute != null) {
            OnFailResultImpl result = _validate(element.getAttribute(targetAttribute), executionContext);
            if(result != null) {
                result.setFailFragmentPath(SAXUtil.getXPath(element) + "/@" + targetAttribute);
                assertValidationException(result, executionContext);
            }
        } else {
            OnFailResultImpl result = _validate(element.getTextContent(), executionContext);
            if(result != null) {
                result.setFailFragmentPath(SAXUtil.getXPath(element));
                assertValidationException(result, executionContext);
            }
        }
    }

    public void visitAfter(final Element element, final ExecutionContext executionContext) throws SmooksException
    {
        if(targetAttribute != null) {
            OnFailResultImpl result = _validate(element.getAttribute(targetAttribute), executionContext);
            if(result != null) {
                result.setFailFragmentPath(DomUtils.getXPath(element) + "/@" + targetAttribute);
                assertValidationException(result, executionContext);
            }
        } else {
            OnFailResultImpl result = _validate(element.getTextContent(), executionContext);
            if(result != null) {
                result.setFailFragmentPath(DomUtils.getXPath(element));
                assertValidationException(result, executionContext);
            }
        }
    }

    private void assertValidationException(OnFailResultImpl result, ExecutionContext executionContext) {
        if (onFail == OnFail.FATAL) {
            throw new ValidationException("A FATAL validation failure has occured " + result, result);
        }

        ValidationResult validationResult = getValidationResult(executionContext);
        if(validationResult != null && validationResult.getNumFailures() > maxFails) {
            throw new ValidationException("The maximum number of allowed validation failures (" + maxFails + ") has been exceeded.", result);
        }
    }

    /**
     * Validate will lookup the configured RuleProvider and validate the text against the
     * rule specfied by the composite rule name.
     *
     * @param text The selected data to perform the evaluation on.
     * @param executionContext The Smooks {@link org.milyn.container.ExecutionContext}.
     *
     * @throws ValidationException A FATAL Validation failure has occured, or the maximum number of
     * allowed failures has been exceeded.
     */
    void validate(final String text, final ExecutionContext executionContext) throws ValidationException
    {
        OnFailResultImpl result = _validate(text, executionContext);
        if(result != null) {
            assertValidationException(result, executionContext);
        }
    }

    /**
     * Validate will lookup the configured RuleProvider and validate the text against the
     * rule specfied by the composite rule name.
     *
     * @param text The selected data to perform the evaluation on.
     * @param executionContext The Smooks {@link org.milyn.container.ExecutionContext}.
     *
     * @throws ValidationException A FATAL Validation failure has occured, or the maximum number of
     * allowed failures has been exceeded.
     */
    private OnFailResultImpl _validate(final String text, final ExecutionContext executionContext) throws ValidationException
    {
        if(ruleProvider == null) {
            setRuleProvider(executionContext);
        }

        final RuleEvalResult result = ruleProvider.evaluate(ruleName, text, executionContext);

        if(logger.isDebugEnabled()) {
            logger.debug(result);
        }

        if (!result.matched())
        {
            ValidationResult validationResult = getValidationResult(executionContext);
            OnFailResultImpl onFailResult = new OnFailResultImpl();
            onFailResult.setRuleResult(result);
            onFailResult.setBeanContext(executionContext.getBeanContext().getBeanMap());
            validationResult.addResult(onFailResult, onFail);

            return onFailResult;
        }

        return null;
    }

    private ValidationResult getValidationResult(ExecutionContext executionContext) {
        ValidationResult validationResult = (ValidationResult) FilterResult.getResult(executionContext, ValidationResult.class);
        // Create a new ValidationResult if one was not available in the execution context.
        // This would be the case for example if one as not specified to Smooks filter method.
        if (validationResult == null) {
            validationResult = new ValidationResult();
        }

        return validationResult;
    }

    private synchronized void setRuleProvider(ExecutionContext executionContext) {
        if(ruleProvider != null) {
            return;
        }

        ruleProvider = RuleProviderAccessor.get(appContext, ruleProviderName);
        if(ruleProvider == null) {
            throw new SmooksException("Unknown rule provider '" + ruleProviderName + "'.");
        }

        // Configure the base bundle name for validation failure messages...
        setMessageBundleBaseName();

        // Configure the maxFails per ValidationResult instance...
        String maxFailsConfig = executionContext.getConfigParameter(OnFailResult.MAX_FAILS);
        if(maxFailsConfig != null) {
            try {
                maxFails = Integer.parseInt(maxFailsConfig.trim());
            } catch(NumberFormatException e) {
                throw new SmooksConfigurationException("Invalid config value '" + maxFailsConfig.trim() + "' for global parameter '" + OnFailResult.MAX_FAILS + "'.  Must be a valid Integer value.");
            }
        } else {
            maxFails = Integer.MAX_VALUE;
        }
    }

    private void setMessageBundleBaseName() {
        String ruleSource = ruleProvider.getSrc();
        File srcFile = new File(ruleSource);
        String srcFileName = srcFile.getName();
        int indexOfExt = srcFileName.lastIndexOf('.');
        File parentFolder = srcFile.getParentFile();

        if(indexOfExt != -1) {
            messageBundleBaseName = srcFileName.substring(0, indexOfExt);
        } else {
            messageBundleBaseName = ruleSource;
        }

        if(parentFolder != null) {
            messageBundleBaseName = parentFolder.getPath() + "/i18n/" + messageBundleBaseName;
        } else {
            messageBundleBaseName = "i18n/" + messageBundleBaseName;
        }

        messageBundleBaseName = messageBundleBaseName.replace('\\', '/');
    }

    @Override
    public String toString()
    {
        return String.format("%s [rule=%s, onFail=%s]", getClass().getSimpleName(), compositRuleName, onFail);
    }

    @ConfigParam (name="name")
    public void setCompositRuleName(final String compositRuleName)
    {
        this.compositRuleName = compositRuleName;
        this.ruleProviderName = RuleProviderAccessor.parseRuleProviderName(compositRuleName);
        this.ruleName = RuleProviderAccessor.parseRuleName(compositRuleName);
    }

    public String getCompositRuleName()
    {
        return compositRuleName;
    }

    @ConfigParam (defaultVal = "ERROR")
    public void setOnFail(final OnFail onFail)
    {
        this.onFail = onFail;
    }

    public OnFail getOnFail()
    {
        return onFail;
    }

    public Validator setAppContext(ApplicationContext appContext) {
        this.appContext = appContext;
        return this;
    }

    private class OnFailResultImpl implements OnFailResult {

        private String failFragmentPath;
        private RuleEvalResult ruleResult;
        public Map<String, Object> beanContext;

        public void setFailFragmentPath(String failFragmentPath) {
            this.failFragmentPath = failFragmentPath;
        }

        public String getFailFragmentPath() {
            return failFragmentPath;
        }

        public void setRuleResult(RuleEvalResult ruleResult) {
            this.ruleResult = ruleResult;
        }

        public RuleEvalResult getFailRuleResult() {
            return ruleResult;
        }

        public void setBeanContext(Map<String, Object> beanContext) {
            // Need to create a shallow copy as the context data may change.
            // Even this is not foolproof, as internal bean data can also be
            // overwritten by the bean context!!
            this.beanContext = new HashMap();
            this.beanContext.putAll(beanContext);
        }

        public String getMessage() {
            return getMessage(Locale.getDefault());
        }

        public String getMessage(Locale locale) {
            if(ruleResult.getEvalException() != null) {
                return ruleResult.getEvalException().getMessage();
            }

            String message = getMessage(locale, ruleName);
            // If no ResouceBundle was configured then use this instances toString
            if (message == null) {
                return toString();
            }

            if (message.startsWith("ftl:")) {
                // TODO: Is there a way to optimize this e.g. attach the compiled template
                // to the bundle as an object and then get back using ResourceBundle.getObject??
                // I timed it and it was able to create and apply 10000 templates in about 2500 ms
                // on an "average" spec machine, so it's not toooooo bad, and it's only done on demand :)
                FreeMarkerTemplate template = new FreeMarkerTemplate(message.substring("ftl:".length()));
                beanContext.put("ruleResult", ruleResult);
                beanContext.put("path", failFragmentPath);
                message = template.apply(beanContext);
            }

            return message;
        }

        private String getMessage(final Locale locale, final String messageName) {
            final ResourceBundle bundle = getMessageBundle(locale);
            if (messageName == null || bundle == null)
                return null;

           return bundle.getString(messageName);
         }

        /*
        public String getMessage(Locale locale) {
            if(ruleResult.getEvalException() != null) {
                return ruleResult.getEvalException().getMessage();
            } else {
                ResourceBundle bundle = getMessageBundle(locale);

                String message = bundle.getString(ruleName);
                if (message != null && message.startsWith("ftl:")) {
                    // TODO: Is there a way to optimize this e.g. attach the compiled template
                    // to the bundle as an object and then get back using ResourceBundle.getObject??
                    // I timed it and it was able to create and apply 10000 templates in about 2500 ms
                    // on an "average" spec machine, so it's not toooooo bad, and it's only done on demand :)
                    FreeMarkerTemplate template = new FreeMarkerTemplate(message.substring("ftl:".length()));
                    beanContext.put("ruleResult", ruleResult);
                    beanContext.put("path", failFragmentPath);
                    message = template.apply(beanContext);
                }

                return message;
            }
        }
        */

        /**
         * @param The Locale to look up.
         * @return {@link ResourceBundle} for the Locale and message bundle base name. Or null if no bundle exists.
         */
        private ResourceBundle getMessageBundle(final Locale locale) {
            try {
                return ResourceBundle.getBundle(messageBundleBaseName, locale, new ResourceBundleClassLoader());
            } catch (final MissingResourceException e) {
                logger.warn("Failed to load Validation rule message bundle '" + messageBundleBaseName + "'.  This resource must be on the classpath!", e);
            }

            return null;
        }

        @Override
        public String toString() {
            return "[" + failFragmentPath + "] " + ruleResult.toString();
        }
    }

    private class ResourceBundleClassLoader extends ClassLoader {
        @Override
        public InputStream getResourceAsStream(String name) {
            try {
                return new URIResourceLocator().getResource(name);
            } catch (IOException e) {
                return null;
            }
        }
    }
}
