package org.milyn.commons.javabean.decoders;

import org.milyn.commons.config.Configurable;
import org.milyn.commons.cdr.SmooksConfigurationException;

import java.text.*;
import java.util.*;

/**
 * LocaleAwareDateDecoder is a decoder 'helper' that can be subclassed by Date decoders to enable
 * them to use locale specific date formats.
 * <p/>
 * Usage (on Java Binding value config using the {@link org.milyn.commons.javabean.decoders.DateDecoder}):
 * <pre>
 * &lt;jb:value property="date" decoder="Date" data="order/@date"&gt;
 *     &lt;-- Format: Defaults to "yyyy-MM-dd'T'HH:mm:ss" (SOAP) --&gt;
 *     &lt;jb:decodeParam name="format"&gt;EEE MMM dd HH:mm:ss z yyyy&lt;/jb:decodeParam&gt;
 *     &lt;-- Locale: Defaults to machine Locale --&gt;
 *     &lt;jb:decodeParam name="locale"&gt;sv-SE&lt;/jb:decodeParam&gt;
 *     &lt;-- Verify Locale: Default false --&gt;
 *     &lt;jb:decodeParam name="verify-locale"&gt;true&lt;/jb:decodeParam&gt;
 * &lt;/jb:value&gt;
 * </pre>
 *
 * @author <a href="mailto:daniel.bevenius@gmail.com">daniel.bevenius@gmail.com</a>
 */
public abstract class LocaleAwareDateDecoder extends LocaleAwareDecoder
{
    /**
     * Date format configuration key.
     */
    public static final String FORMAT = "format";

    /**
     * Default date format string.
     */
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    /**
     * ISO Language Code. Lower case two-letter code defined by ISO-639
     * @deprecated Use {@link LocaleAwareDecoder}.
     */
    public static final String LOCALE_LANGUAGE_CODE = "locale-language";

    /**
     * ISO Country Code. Upper case two-letter code defined by ISO-3166
     * @deprecated Use {@link LocaleAwareDecoder}.
     */
    public static final String LOCALE_COUNTRY_CODE = "locale-country";

    /**
     * True or false(default).
     * Whether or not a check should be performed to verify that
     * the specified locale is installed. This operation can take some
     * time and should be turned off in a production evironment
     * @deprecated Use {@link LocaleAwareDecoder}.
     */
    public static final String VERIFY_LOCALE = "verify-locale";

    protected String format;

    /*
     * 	Need to initialize a default decoder as not calls can be make
     * 	directly to decode without calling setConfigurtion.
     */
    protected SimpleDateFormat decoder = new SimpleDateFormat( DEFAULT_DATE_FORMAT );

    public void setConfiguration(Properties resourceConfig) throws SmooksConfigurationException {
        super.setConfiguration(resourceConfig);

        format = resourceConfig.getProperty(FORMAT, DEFAULT_DATE_FORMAT);
        if (format == null) {
            throw new SmooksConfigurationException("Decoder must specify a 'format' parameter.");
        }

        Locale configuredLocale = getLocale();
        if(configuredLocale != null) {
            decoder = new SimpleDateFormat(format.trim(), configuredLocale);
        } else {
            decoder = new SimpleDateFormat(format.trim());
        }
    }
}
