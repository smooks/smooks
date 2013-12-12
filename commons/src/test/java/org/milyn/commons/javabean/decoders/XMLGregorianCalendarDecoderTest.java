package org.milyn.commons.javabean.decoders;

import junit.framework.TestCase;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class XMLGregorianCalendarDecoderTest extends TestCase {

    private Locale defaultLocale;

    public void test_DateDecoder_01() {
        XMLGregorianCalendarDecoder decoder = new XMLGregorianCalendarDecoder();
        Properties config = new Properties();

        config.setProperty(DateDecoder.FORMAT, "EEE MMM dd HH:mm:ss z yyyy");
        config.setProperty(LocaleAwareDecoder.LOCALE_LANGUAGE_CODE, "en");
        config.setProperty(LocaleAwareDecoder.LOCALE_COUNTRY_CODE, "IE");
        decoder.setConfiguration(config);

        Date date_a = ((XMLGregorianCalendar) decoder.decode("Wed Nov 15 13:45:28 EST 2006")).toGregorianCalendar().getTime();
        assertEquals(1163616328000L, date_a.getTime());
        Date date_b = ((XMLGregorianCalendar) decoder.decode("Wed Nov 15 13:45:28 EST 2006")).toGregorianCalendar().getTime();
        assertNotSame(date_a, date_b);
    }

    public void test_DateDecoder_02() {
        XMLGregorianCalendarDecoder decoder = new XMLGregorianCalendarDecoder();
        Properties config = new Properties();

        config.setProperty(DateDecoder.FORMAT, "EEE MMM dd HH:mm:ss z yyyy");
        config.setProperty(LocaleAwareDecoder.LOCALE, "en-IE");
        decoder.setConfiguration(config);

        Date date_a = ((XMLGregorianCalendar) decoder.decode("Wed Nov 15 13:45:28 EST 2006")).toGregorianCalendar().getTime();
        assertEquals(1163616328000L, date_a.getTime());
        Date date_b = ((XMLGregorianCalendar) decoder.decode("Wed Nov 15 13:45:28 EST 2006")).toGregorianCalendar().getTime();
        assertNotSame(date_a, date_b);
    }

    public void setUp() {
        defaultLocale = Locale.getDefault();
        Locale.setDefault(new Locale("de", "DE"));
    }

    protected void tearDown() throws Exception {
        Locale.setDefault(defaultLocale);
    }
}
