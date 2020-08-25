/*-
 * ========================LICENSE_START=================================
 * Smooks Core
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0, or
 * the GNU Lesser General Public License version 3.0 or later.
 * 
 * SPDX-License-Identifier: Apache-2.0 OR LGPL-3.0-or-later
 * 
 * ======================================================================
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ======================================================================
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * =========================LICENSE_END==================================
 */
package org.smooks.delivery.sax;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

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

    public void write(char[] cbuf, int off, int len) {
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

    private boolean isAlreadyWritten(char[] cbuf, int off, int len) {
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
        private final char[] lastWriteBuf;
        private final int lastWriteOff;
        private final int lastWriteLen;

        private WriteRecord(char[] lastWriteBuf, int lastWriteOff, int lastWriteLen) {
            this.lastWriteBuf = lastWriteBuf;
            this.lastWriteOff = lastWriteOff;
            this.lastWriteLen = lastWriteLen;
        }
    }
}
