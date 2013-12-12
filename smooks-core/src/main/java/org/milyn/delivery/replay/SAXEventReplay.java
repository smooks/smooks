package org.milyn.delivery.replay;

import org.milyn.commons.SmooksException;

/**
 * SAX event replay interface.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public interface SAXEventReplay {

    /**
     * Replay the last SAX event onto the supplied SAX {@link org.xml.sax.ContentHandler} instance.
     *
     * @param handler The handler on whic to replay the last event.
     * @throws SmooksException Error replaying last event.
     */
    void replay(org.xml.sax.ContentHandler handler) throws SmooksException;
}
