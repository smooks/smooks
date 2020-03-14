/*
	Milyn - Copyright (C) 2006 - 2010

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License (version 2.1) as published by the Free Software
	Foundation.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

	See the GNU Lesser General Public License for more details:
	http://www.gnu.org/licenses/lgpl.txt
*/
package org.smooks.delivery.sax;

import java.io.StringWriter;
import java.util.List;
import java.util.ArrayList;

/**
 * This is a specialized StringWriter that tracks the writes to make sure we don't
 * write the same buffer segment multiple times.
 * <p/>
 * See JIRA: http://jira.codehaus.org/browse/MILYN-238
 *
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a> 
 */
public class TrackedStringWriter extends StringWriter {
    private char[] lastWriteBuf;
    private int lastWriteOff;
    private int lastWriteLen;
    private List<WriteRecord> writeTrackingList;

    public void write(char cbuf[], int off, int len) {
        if(cbuf == lastWriteBuf && off == lastWriteOff && len == lastWriteLen) {
            // we've already written this character buffer segment...
            return;
        }
        if(lastWriteBuf != null) {
            // We've written to this writer already and the new incoming buffer
            // is not the same as the last buffer...

            if(writeTrackingList == null) {
                writeTrackingList = new ArrayList<WriteRecord>();
            } else {
                if(isAlreadyWritten(cbuf, off, len)) {
                    // we've already written this character buffer segment...
                    return;
                }
            }
            writeTrackingList.add(new WriteRecord(lastWriteBuf, lastWriteOff, lastWriteLen));
        }

        super.write(cbuf, off, len);
        lastWriteBuf = cbuf;
        lastWriteOff = off;
        lastWriteLen = len;
    }

    private boolean isAlreadyWritten(char cbuf[], int off, int len) {
        int trackListLen = writeTrackingList.size();

        for(int i = 0; i < trackListLen; i++) {
            WriteRecord listEntry = writeTrackingList.get(i);
            if(cbuf == listEntry.lastWriteBuf && off == listEntry.lastWriteOff && len == listEntry.lastWriteLen) {
                // we've already written this character buffer segment...
                return true;
            }
        }

        return false;
    }

    private class WriteRecord {
        private char[] lastWriteBuf;
        private int lastWriteOff;
        private int lastWriteLen;

        private WriteRecord(char[] lastWriteBuf, int lastWriteOff, int lastWriteLen) {
            this.lastWriteBuf = lastWriteBuf;
            this.lastWriteOff = lastWriteOff;
            this.lastWriteLen = lastWriteLen;
        }
    }
}
