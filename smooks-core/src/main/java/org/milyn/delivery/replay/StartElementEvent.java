package org.milyn.delivery.replay;

import org.milyn.SmooksException;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * End element event replay.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class StartElementEvent implements SAXEventReplay {

    public String uri;
    public String localName;
    public String qName;
    public Attributes atts;

    public void set(String uri, String localName, String qName, Attributes atts) {
        this.uri = uri;
        this.localName = localName;
        this.qName = qName;
        this.atts = atts;
    }

    public void replay(ContentHandler handler) throws SmooksException {
        try {
            handler.startElement(uri, localName, qName, atts);
        } catch (SAXException e) {
            throw new SmooksException("Error replaying startElement event.", e);
        }
    }
}
