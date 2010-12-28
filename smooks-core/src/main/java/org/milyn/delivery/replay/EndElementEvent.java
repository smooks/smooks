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
public class EndElementEvent implements SAXEventReplay {

    public String uri;
    public String localName;
    public String qName;

    public void set(String uri, String localName, String qName) {
        this.uri = uri;
        this.localName = localName;
        this.qName = qName;
    }

    public void replay(ContentHandler handler) throws SmooksException {
        try {
            handler.endElement(uri, localName, qName);
        } catch (SAXException e) {
            throw new SmooksException("Error replaying endElement event.", e);
        }
    }
}