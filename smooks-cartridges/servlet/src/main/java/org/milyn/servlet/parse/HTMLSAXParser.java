package org.milyn.servlet.parse;

import org.apache.xerces.parsers.AbstractSAXParser;
import org.cyberneko.html.HTMLConfiguration;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

/**
 * HTML parser using the cyberneko HTML configuration.
 * @author tfennelly
 */
public class HTMLSAXParser extends AbstractSAXParser {
    public HTMLSAXParser() {
        super(new HTMLConfiguration());

        try {
            // Set all the required neko features.
            setFeature("http://cyberneko.org/html/features/balance-tags", false);
            setFeature("http://cyberneko.org/html/features/scanner/cdata-sections", true);
            setFeature("http://apache.org/xml/features/scanner/notify-char-refs", true);
            setFeature("http://apache.org/xml/features/scanner/notify-builtin-refs", true);
            setFeature("http://cyberneko.org/html/features/scanner/notify-builtin-refs", true);
        } catch (SAXNotRecognizedException e) {
            IllegalStateException state = new IllegalStateException("Unable to create CyberNeko SAX parser instance.");
            state.initCause(e);
            throw state;
        } catch (SAXNotSupportedException e) {
            IllegalStateException state = new IllegalStateException("Unable to create CyberNeko SAX parser instance.");
            state.initCause(e);
            throw state;
        }
    }
}