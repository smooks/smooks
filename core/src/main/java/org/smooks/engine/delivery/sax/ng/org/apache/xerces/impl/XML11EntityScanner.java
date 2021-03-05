/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl;

import java.io.EOFException;
import java.io.IOException;

import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityScanner;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLErrorReporter;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.msg.XMLMessageFormatter;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.util.XML11Char;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.util.XMLChar;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.util.XMLStringBuffer;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.QName;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XMLString;

/**
 * Implements the entity scanner methods in
 * the context of XML 1.1.
 *
 * @xerces.internal
 *
 * @author Michael Glavassevich, IBM
 * @author Neil Graham, IBM
 * @version $Id$
 */
public class XML11EntityScanner
    extends XMLEntityScanner {

    //
    // Constructors
    //

    /** Default constructor. */
    public XML11EntityScanner() {
        super();
    } // <init>()

    //
    // XMLEntityScanner methods
    //

    /**
     * Returns the next character on the input.
     * <p>
     * <strong>Note:</strong> The character is <em>not</em> consumed.
     *
     * @throws IOException  Thrown if i/o error occurs.
     * @throws EOFException Thrown on end of file.
     */
    public int peekChar() throws IOException {

        // load more characters, if needed
        if (fCurrentEntity.position == fCurrentEntity.count) {
            load(0, true);
        }

        // peek at character
        int c = fCurrentEntity.ch[fCurrentEntity.position];

        // return peeked character
        if (fCurrentEntity.isExternal()) {
            return (c != '\r' && c != 0x85 && c != 0x2028) ? c : '\n';
        }
        else {
            return c;
        }

    } // peekChar():int

    /**
     * Returns the next character on the input.
     * <p>
     * <strong>Note:</strong> The character is consumed.
     *
     * @throws IOException  Thrown if i/o error occurs.
     * @throws EOFException Thrown on end of file.
     */
    public int scanChar() throws IOException {

        // load more characters, if needed
        if (fCurrentEntity.position == fCurrentEntity.count) {
            load(0, true);
        }

        // scan character
        int c = fCurrentEntity.ch[fCurrentEntity.position++];
        boolean external = false;
        if (c == '\n' ||
            ((c == '\r' || c == 0x85 || c == 0x2028) && (external = fCurrentEntity.isExternal()))) {
            fCurrentEntity.lineNumber++;
            fCurrentEntity.columnNumber = 1;
            if (fCurrentEntity.position == fCurrentEntity.count) {
                fCurrentEntity.ch[0] = (char)c;
                load(1, false);
            }
            if (c == '\r' && external) {
                int cc = fCurrentEntity.ch[fCurrentEntity.position++];
                if (cc != '\n' && cc != 0x85) {
                    fCurrentEntity.position--;
                }
            }
            c = '\n';
        }

        // return character that was scanned
        fCurrentEntity.columnNumber++;
        return c;

    } // scanChar():int

    /**
     * Returns a string matching the NMTOKEN production appearing immediately
     * on the input as a symbol, or null if NMTOKEN Name string is present.
     * <p>
     * <strong>Note:</strong> The NMTOKEN characters are consumed.
     * <p>
     * <strong>Note:</strong> The string returned must be a symbol. The
     * SymbolTable can be used for this purpose.
     *
     * @throws IOException  Thrown if i/o error occurs.
     * @throws EOFException Thrown on end of file.
     *
     * @see org.smooks.engine.delivery.sax.ng.org.apache.xerces.util.SymbolTable
     * @see XML11Char#isXML11Name
     */
    public String scanNmtoken() throws IOException {
        // load more characters, if needed
        if (fCurrentEntity.position == fCurrentEntity.count) {
            load(0, true);
        }

        // scan nmtoken
        int offset = fCurrentEntity.position;
        
        do {
            char ch = fCurrentEntity.ch[fCurrentEntity.position];
            if (XML11Char.isXML11Name(ch)) {
                if (++fCurrentEntity.position == fCurrentEntity.count) {
                    int length = fCurrentEntity.position - offset;
                    if (length == fCurrentEntity.ch.length) {
                        // bad luck we have to resize our buffer
                        resizeBuffer(offset, length);
                    }
                    else {
                        System.arraycopy(fCurrentEntity.ch, offset,
                                         fCurrentEntity.ch, 0, length);
                    }
                    offset = 0;
                    if (load(length, false)) {
                        break;
                    }
                }
            }
            else if (XML11Char.isXML11NameHighSurrogate(ch)) {
                if (++fCurrentEntity.position == fCurrentEntity.count) {
                    int length = fCurrentEntity.position - offset;
                    if (length == fCurrentEntity.ch.length) {
                        // bad luck we have to resize our buffer
                        resizeBuffer(offset, length);
                    }
                    else {
                        System.arraycopy(fCurrentEntity.ch, offset,
                                         fCurrentEntity.ch, 0, length);
                    }
                    offset = 0;
                    if (load(length, false)) {
                        --fCurrentEntity.startPosition;
                        --fCurrentEntity.position;
                        break;
                    }
                }
                char ch2 = fCurrentEntity.ch[fCurrentEntity.position];
                if ( !XMLChar.isLowSurrogate(ch2) ||
                     !XML11Char.isXML11Name(XMLChar.supplemental(ch, ch2)) ) {
                    --fCurrentEntity.position;
                    break;
                }
                if (++fCurrentEntity.position == fCurrentEntity.count) {
                    int length = fCurrentEntity.position - offset;
                    if (length == fCurrentEntity.ch.length) {
                        // bad luck we have to resize our buffer
                        resizeBuffer(offset, length);
                    }
                    else {
                        System.arraycopy(fCurrentEntity.ch, offset,
                                         fCurrentEntity.ch, 0, length);
                    }
                    offset = 0;
                    if (load(length, false)) {
                        break;
                    }
                }
            }
            else {
                break;
            }
        }
        while (true);
        
        int length = fCurrentEntity.position - offset;
        fCurrentEntity.columnNumber += length;

        // return nmtoken
        String symbol = null;
        if (length > 0) {
            symbol = fSymbolTable.addSymbol(fCurrentEntity.ch, offset, length);
        }
        return symbol;

    } // scanNmtoken():String

    /**
     * Returns a string matching the Name production appearing immediately
     * on the input as a symbol, or null if no Name string is present.
     * <p>
     * <strong>Note:</strong> The Name characters are consumed.
     * <p>
     * <strong>Note:</strong> The string returned must be a symbol. The
     * SymbolTable can be used for this purpose.
     *
     * @throws IOException  Thrown if i/o error occurs.
     * @throws EOFException Thrown on end of file.
     *
     * @see org.smooks.engine.delivery.sax.ng.org.apache.xerces.util.SymbolTable
     * @see XML11Char#isXML11Name
     * @see XML11Char#isXML11NameStart
     */
    public String scanName() throws IOException {
        // load more characters, if needed
        if (fCurrentEntity.position == fCurrentEntity.count) {
            load(0, true);
        }

        // scan name
        int offset = fCurrentEntity.position;
        char ch = fCurrentEntity.ch[offset];
        
        if (XML11Char.isXML11NameStart(ch)) {
            if (++fCurrentEntity.position == fCurrentEntity.count) {
                fCurrentEntity.ch[0] = ch;
                offset = 0;
                if (load(1, false)) {
                    fCurrentEntity.columnNumber++;
                    String symbol = fSymbolTable.addSymbol(fCurrentEntity.ch, 0, 1);
                    return symbol;
                }
            }
        }
        else if (XML11Char.isXML11NameHighSurrogate(ch)) {
            if (++fCurrentEntity.position == fCurrentEntity.count) {
                fCurrentEntity.ch[0] = ch;
                offset = 0;
                if (load(1, false)) {
                    --fCurrentEntity.position;
                    --fCurrentEntity.startPosition;
                    return null;
                }
            }
            char ch2 = fCurrentEntity.ch[fCurrentEntity.position];
            if ( !XMLChar.isLowSurrogate(ch2) ||
                 !XML11Char.isXML11NameStart(XMLChar.supplemental(ch, ch2)) ) {
                --fCurrentEntity.position;
                return null;
            }
            if (++fCurrentEntity.position == fCurrentEntity.count) {
                fCurrentEntity.ch[0] = ch;
                fCurrentEntity.ch[1] = ch2;
                offset = 0;
                if (load(2, false)) {
                    fCurrentEntity.columnNumber += 2;
                    String symbol = fSymbolTable.addSymbol(fCurrentEntity.ch, 0, 2);
                    return symbol;
                }
            }
        }
        else {
            return null;
        }
        
        do {
            ch = fCurrentEntity.ch[fCurrentEntity.position];
            if (XML11Char.isXML11Name(ch)) {
                if (++fCurrentEntity.position == fCurrentEntity.count) {
                    int length = fCurrentEntity.position - offset;
                    if (length == fCurrentEntity.ch.length) {
                        // bad luck we have to resize our buffer
                        resizeBuffer(offset, length);
                    }
                    else {
                        System.arraycopy(fCurrentEntity.ch, offset,
                                         fCurrentEntity.ch, 0, length);
                    }
                    offset = 0;
                    if (load(length, false)) {
                        break;
                    }
                }
            }
            else if (XML11Char.isXML11NameHighSurrogate(ch)) {
                if (++fCurrentEntity.position == fCurrentEntity.count) {
                    int length = fCurrentEntity.position - offset;
                    if (length == fCurrentEntity.ch.length) {
                        // bad luck we have to resize our buffer
                        resizeBuffer(offset, length);
                    }
                    else {
                        System.arraycopy(fCurrentEntity.ch, offset,
                                         fCurrentEntity.ch, 0, length);
                    }
                    offset = 0;
                    if (load(length, false)) {
                        --fCurrentEntity.position;
                        --fCurrentEntity.startPosition;
                        break;
                    }
                }
                char ch2 = fCurrentEntity.ch[fCurrentEntity.position];
                if ( !XMLChar.isLowSurrogate(ch2) ||
                     !XML11Char.isXML11Name(XMLChar.supplemental(ch, ch2)) ) {
                    --fCurrentEntity.position;
                    break;
                }
                if (++fCurrentEntity.position == fCurrentEntity.count) {
                    int length = fCurrentEntity.position - offset;
                    if (length == fCurrentEntity.ch.length) {
                        // bad luck we have to resize our buffer
                        resizeBuffer(offset, length);
                    }
                    else {
                        System.arraycopy(fCurrentEntity.ch, offset,
                                         fCurrentEntity.ch, 0, length);
                    }
                    offset = 0;
                    if (load(length, false)) {
                        break;
                    }
                }
            }
            else {
                break;
            }
        }
        while (true);

        int length = fCurrentEntity.position - offset;
        fCurrentEntity.columnNumber += length;

        // return name
        String symbol = null;
        if (length > 0) {
            symbol = fSymbolTable.addSymbol(fCurrentEntity.ch, offset, length);
        }
        return symbol;

    } // scanName():String

    /**
     * Returns a string matching the NCName production appearing immediately
     * on the input as a symbol, or null if no NCName string is present.
     * <p>
     * <strong>Note:</strong> The NCName characters are consumed.
     * <p>
     * <strong>Note:</strong> The string returned must be a symbol. The
     * SymbolTable can be used for this purpose.
     *
     * @throws IOException  Thrown if i/o error occurs.
     * @throws EOFException Thrown on end of file.
     *
     * @see org.smooks.engine.delivery.sax.ng.org.apache.xerces.util.SymbolTable
     * @see XML11Char#isXML11NCName
     * @see XML11Char#isXML11NCNameStart
     */
    public String scanNCName() throws IOException {

        // load more characters, if needed
        if (fCurrentEntity.position == fCurrentEntity.count) {
            load(0, true);
        }

        // scan name
        int offset = fCurrentEntity.position;
        char ch = fCurrentEntity.ch[offset];
        
        if (XML11Char.isXML11NCNameStart(ch)) {
            if (++fCurrentEntity.position == fCurrentEntity.count) {
                fCurrentEntity.ch[0] = ch;
                offset = 0;
                if (load(1, false)) {
                    fCurrentEntity.columnNumber++;
                    String symbol = fSymbolTable.addSymbol(fCurrentEntity.ch, 0, 1);
                    return symbol;
                }
            }
        }
        else if (XML11Char.isXML11NameHighSurrogate(ch)) {
            if (++fCurrentEntity.position == fCurrentEntity.count) {
                fCurrentEntity.ch[0] = ch;
                offset = 0;
                if (load(1, false)) {
                    --fCurrentEntity.position;
                    --fCurrentEntity.startPosition;
                    return null;
                }
            }
            char ch2 = fCurrentEntity.ch[fCurrentEntity.position];
            if ( !XMLChar.isLowSurrogate(ch2) ||
                 !XML11Char.isXML11NCNameStart(XMLChar.supplemental(ch, ch2)) ) {
                --fCurrentEntity.position;
                return null;
            }
            if (++fCurrentEntity.position == fCurrentEntity.count) {
                fCurrentEntity.ch[0] = ch;
                fCurrentEntity.ch[1] = ch2;
                offset = 0;
                if (load(2, false)) {
                    fCurrentEntity.columnNumber += 2;
                    String symbol = fSymbolTable.addSymbol(fCurrentEntity.ch, 0, 2);
                    return symbol;
                }
            }
        }
        else {
            return null;
        }
        
        do {
            ch = fCurrentEntity.ch[fCurrentEntity.position];
            if (XML11Char.isXML11NCName(ch)) {
                if (++fCurrentEntity.position == fCurrentEntity.count) {
                    int length = fCurrentEntity.position - offset;
                    if (length == fCurrentEntity.ch.length) {
                        // bad luck we have to resize our buffer
                        resizeBuffer(offset, length);
                    }
                    else {
                        System.arraycopy(fCurrentEntity.ch, offset,
                                         fCurrentEntity.ch, 0, length);
                    }
                    offset = 0;
                    if (load(length, false)) {
                        break;
                    }
                }
            }
            else if (XML11Char.isXML11NameHighSurrogate(ch)) {
                if (++fCurrentEntity.position == fCurrentEntity.count) {
                    int length = fCurrentEntity.position - offset;
                    if (length == fCurrentEntity.ch.length) {
                        // bad luck we have to resize our buffer
                        resizeBuffer(offset, length);
                    }
                    else {
                        System.arraycopy(fCurrentEntity.ch, offset,
                                         fCurrentEntity.ch, 0, length);
                    }
                    offset = 0;
                    if (load(length, false)) {
                        --fCurrentEntity.startPosition;
                        --fCurrentEntity.position;
                        break;
                    }
                }
                char ch2 = fCurrentEntity.ch[fCurrentEntity.position];
                if ( !XMLChar.isLowSurrogate(ch2) ||
                     !XML11Char.isXML11NCName(XMLChar.supplemental(ch, ch2)) ) {
                    --fCurrentEntity.position;
                    break;
                }
                if (++fCurrentEntity.position == fCurrentEntity.count) {
                    int length = fCurrentEntity.position - offset;
                    if (length == fCurrentEntity.ch.length) {
                        // bad luck we have to resize our buffer
                        resizeBuffer(offset, length);
                    }
                    else {
                        System.arraycopy(fCurrentEntity.ch, offset,
                                         fCurrentEntity.ch, 0, length);
                    }
                    offset = 0;
                    if (load(length, false)) {
                        break;
                    }
                }
            }
            else {
                break;
            }
        }
        while (true);
        
        int length = fCurrentEntity.position - offset;
        fCurrentEntity.columnNumber += length;

        // return name
        String symbol = null;
        if (length > 0) {
            symbol = fSymbolTable.addSymbol(fCurrentEntity.ch, offset, length);
        }
        return symbol;

    } // scanNCName():String

    /**
     * Scans a qualified name from the input, setting the fields of the
     * QName structure appropriately.
     * <p>
     * <strong>Note:</strong> The qualified name characters are consumed.
     * <p>
     * <strong>Note:</strong> The strings used to set the values of the
     * QName structure must be symbols. The SymbolTable can be used for
     * this purpose.
     *
     * @param qname The qualified name structure to fill.
     *
     * @return Returns true if a qualified name appeared immediately on
     *         the input and was scanned, false otherwise.
     *
     * @throws IOException  Thrown if i/o error occurs.
     * @throws EOFException Thrown on end of file.
     *
     * @see org.smooks.engine.delivery.sax.ng.org.apache.xerces.util.SymbolTable
     * @see XML11Char#isXML11Name
     * @see XML11Char#isXML11NameStart
     */
    public boolean scanQName(QName qname) throws IOException {

        // load more characters, if needed
        if (fCurrentEntity.position == fCurrentEntity.count) {
            load(0, true);
        }

        // scan qualified name
        int offset = fCurrentEntity.position;
        char ch = fCurrentEntity.ch[offset];
        
        if (XML11Char.isXML11NCNameStart(ch)) {
            if (++fCurrentEntity.position == fCurrentEntity.count) {
                fCurrentEntity.ch[0] = ch;
                offset = 0;
                if (load(1, false)) {
                    fCurrentEntity.columnNumber++;
                    String name = fSymbolTable.addSymbol(fCurrentEntity.ch, 0, 1);
                    qname.setValues(null, name, name, null);
                    return true;
                }
            }
        }
        else if (XML11Char.isXML11NameHighSurrogate(ch)) {
            if (++fCurrentEntity.position == fCurrentEntity.count) {
                fCurrentEntity.ch[0] = ch;
                offset = 0;
                if (load(1, false)) {
                    --fCurrentEntity.startPosition;
                    --fCurrentEntity.position;
                    return false;
                }
            }
            char ch2 = fCurrentEntity.ch[fCurrentEntity.position];
            if ( !XMLChar.isLowSurrogate(ch2) ||
                 !XML11Char.isXML11NCNameStart(XMLChar.supplemental(ch, ch2)) ) {
                --fCurrentEntity.position;
                return false;
            }
            if (++fCurrentEntity.position == fCurrentEntity.count) {
                fCurrentEntity.ch[0] = ch;
                fCurrentEntity.ch[1] = ch2;
                offset = 0;
                if (load(2, false)) {
                    fCurrentEntity.columnNumber += 2;
                    String name = fSymbolTable.addSymbol(fCurrentEntity.ch, 0, 2);
                    qname.setValues(null, name, name, null);
                    return true;
                }
            }
        }
        else {
            return false;
        }
        
        int index = -1;
        boolean sawIncompleteSurrogatePair = false;
        do {
            ch = fCurrentEntity.ch[fCurrentEntity.position];
            if (XML11Char.isXML11Name(ch)) {
                if (ch == ':') {
                    if (index != -1) {
                        break;
                    }
                    index = fCurrentEntity.position;
                }
                if (++fCurrentEntity.position == fCurrentEntity.count) {
                    int length = fCurrentEntity.position - offset;
                    if (length == fCurrentEntity.ch.length) {
                        // bad luck we have to resize our buffer
                        resizeBuffer(offset, length);
                    }
                    else {
                        System.arraycopy(fCurrentEntity.ch, offset,
                                         fCurrentEntity.ch, 0, length);
                    }
                    if (index != -1) {
                        index = index - offset;
                    }
                    offset = 0;
                    if (load(length, false)) {
                        break;
                    }
                }
            }
            else if (XML11Char.isXML11NameHighSurrogate(ch)) {
                if (++fCurrentEntity.position == fCurrentEntity.count) {
                    int length = fCurrentEntity.position - offset;
                    if (length == fCurrentEntity.ch.length) {
                        // bad luck we have to resize our buffer
                        resizeBuffer(offset, length);
                    }
                    else {
                        System.arraycopy(fCurrentEntity.ch, offset,
                                         fCurrentEntity.ch, 0, length);
                    }
                    if (index != -1) {
                        index = index - offset;
                    }
                    offset = 0;
                    if (load(length, false)) {
                        sawIncompleteSurrogatePair = true;
                        --fCurrentEntity.startPosition;
                        --fCurrentEntity.position;
                        break;
                    }
                }
                char ch2 = fCurrentEntity.ch[fCurrentEntity.position];
                if ( !XMLChar.isLowSurrogate(ch2) ||
                     !XML11Char.isXML11Name(XMLChar.supplemental(ch, ch2)) ) {
                    sawIncompleteSurrogatePair = true;
                    --fCurrentEntity.position;
                    break;
                }
                if (++fCurrentEntity.position == fCurrentEntity.count) {
                    int length = fCurrentEntity.position - offset;
                    if (length == fCurrentEntity.ch.length) {
                        // bad luck we have to resize our buffer
                        resizeBuffer(offset, length);
                    }
                    else {
                        System.arraycopy(fCurrentEntity.ch, offset,
                                         fCurrentEntity.ch, 0, length);
                    }
                    if (index != -1) {
                        index = index - offset;
                    }
                    offset = 0;
                    if (load(length, false)) {
                        break;
                    }
                }
            }
            else {
                break;
            }
        }
        while (true);
        
        int length = fCurrentEntity.position - offset;
        fCurrentEntity.columnNumber += length;
        
        if (length > 0) {
            String prefix = null;
            String localpart = null;
            String rawname = fSymbolTable.addSymbol(fCurrentEntity.ch,
                                                    offset, length);
            if (index != -1) {
                int prefixLength = index - offset;
                prefix = fSymbolTable.addSymbol(fCurrentEntity.ch,
                                                    offset, prefixLength);
                int len = length - prefixLength - 1;
                int startLocal = index +1;
                if (!XML11Char.isXML11NCNameStart(fCurrentEntity.ch[startLocal]) &&
                    (!XML11Char.isXML11NameHighSurrogate(fCurrentEntity.ch[startLocal]) ||
                    sawIncompleteSurrogatePair)){
                    fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN,
                                               "IllegalQName",
                                               null,
                                               XMLErrorReporter.SEVERITY_FATAL_ERROR);
                }
                localpart = fSymbolTable.addSymbol(fCurrentEntity.ch,
                                                   index + 1, len);

            }
            else {
                localpart = rawname;
            }
            qname.setValues(prefix, localpart, rawname, null);
            return true;
        }
        return false;

    } // scanQName(QName):boolean

    /**
     * Scans a range of parsed character data, setting the fields of the
     * XMLString structure, appropriately.
     * <p>
     * <strong>Note:</strong> The characters are consumed.
     * <p>
     * <strong>Note:</strong> This method does not guarantee to return
     * the longest run of parsed character data. This method may return
     * before markup due to reaching the end of the input buffer or any
     * other reason.
     * <p>
     * <strong>Note:</strong> The fields contained in the XMLString
     * structure are not guaranteed to remain valid upon subsequent calls
     * to the entity scanner. Therefore, the caller is responsible for
     * immediately using the returned character data or making a copy of
     * the character data.
     *
     * @param content The content structure to fill.
     *
     * @return Returns the next character on the input, if known. This
     *         value may be -1 but this does <em>note</em> designate
     *         end of file.
     *
     * @throws IOException  Thrown if i/o error occurs.
     * @throws EOFException Thrown on end of file.
     */
    public int scanContent(XMLString content) throws IOException {

        // load more characters, if needed
        if (fCurrentEntity.position == fCurrentEntity.count) {
            load(0, true);
        }
        else if (fCurrentEntity.position == fCurrentEntity.count - 1) {
            fCurrentEntity.ch[0] = fCurrentEntity.ch[fCurrentEntity.count - 1];
            load(1, false);
            fCurrentEntity.position = 0;
            fCurrentEntity.startPosition = 0;
        }

        // normalize newlines
        int offset = fCurrentEntity.position;
        int c = fCurrentEntity.ch[offset];
        int newlines = 0;
        boolean external = fCurrentEntity.isExternal();
        if (c == '\n' || ((c == '\r' || c == 0x85 || c == 0x2028) && external)) {
            do {
                c = fCurrentEntity.ch[fCurrentEntity.position++];
                if ((c == '\r' ) && external) {
                    newlines++;
                    fCurrentEntity.lineNumber++;
                    fCurrentEntity.columnNumber = 1;
                    if (fCurrentEntity.position == fCurrentEntity.count) {
                        offset = 0;
                        fCurrentEntity.baseCharOffset += (fCurrentEntity.position - fCurrentEntity.startPosition);
                        fCurrentEntity.position = newlines;
                        fCurrentEntity.startPosition = newlines;
                        if (load(newlines, false)) {
                            break;
                        }
                    }
                    int cc = fCurrentEntity.ch[fCurrentEntity.position]; 
                    if (cc == '\n' || cc == 0x85) {
                        fCurrentEntity.position++;
                        offset++;
                    }
                    /*** NEWLINE NORMALIZATION ***/
                    else {
                        newlines++;
                    }
                }
                else if (c == '\n' || ((c == 0x85 || c == 0x2028) && external)) {
                    newlines++;
                    fCurrentEntity.lineNumber++;
                    fCurrentEntity.columnNumber = 1;
                    if (fCurrentEntity.position == fCurrentEntity.count) {
                        offset = 0;
                        fCurrentEntity.baseCharOffset += (fCurrentEntity.position - fCurrentEntity.startPosition);
                        fCurrentEntity.position = newlines;
                        fCurrentEntity.startPosition = newlines;
                        if (load(newlines, false)) {
                            break;
                        }
                    }
                }
                else {
                    fCurrentEntity.position--;
                    break;
                }
            } while (fCurrentEntity.position < fCurrentEntity.count - 1);
            for (int i = offset; i < fCurrentEntity.position; i++) {
                fCurrentEntity.ch[i] = '\n';
            }
            int length = fCurrentEntity.position - offset;
            if (fCurrentEntity.position == fCurrentEntity.count - 1) {
                content.setValues(fCurrentEntity.ch, offset, length);
                return -1;
            }
        }

        // inner loop, scanning for content
        if (external) {
            while (fCurrentEntity.position < fCurrentEntity.count) {
                c = fCurrentEntity.ch[fCurrentEntity.position++];
                if (!XML11Char.isXML11Content(c) || c == 0x85 || c == 0x2028) {
                    fCurrentEntity.position--;
                    break;
                }
            }
        }
        else {
            while (fCurrentEntity.position < fCurrentEntity.count) {
                c = fCurrentEntity.ch[fCurrentEntity.position++];
                // In internal entities control characters are allowed to appear unescaped.
                if (!XML11Char.isXML11InternalEntityContent(c)) {
                    fCurrentEntity.position--;
                    break;
                }
            }
        }
        int length = fCurrentEntity.position - offset;
        fCurrentEntity.columnNumber += length - newlines;
        content.setValues(fCurrentEntity.ch, offset, length);

        // return next character
        if (fCurrentEntity.position != fCurrentEntity.count) {
            c = fCurrentEntity.ch[fCurrentEntity.position];
            // REVISIT: Does this need to be updated to fix the
            //          #x0D ^#x0A newline normalization problem? -Ac
            if ((c == '\r' || c == 0x85 || c == 0x2028) && external) {
                c = '\n';
            }
        }
        else {
            c = -1;
        }
        return c;

    } // scanContent(XMLString):int

    /**
     * Scans a range of attribute value data, setting the fields of the
     * XMLString structure, appropriately.
     * <p>
     * <strong>Note:</strong> The characters are consumed.
     * <p>
     * <strong>Note:</strong> This method does not guarantee to return
     * the longest run of attribute value data. This method may return
     * before the quote character due to reaching the end of the input
     * buffer or any other reason.
     * <p>
     * <strong>Note:</strong> The fields contained in the XMLString
     * structure are not guaranteed to remain valid upon subsequent calls
     * to the entity scanner. Therefore, the caller is responsible for
     * immediately using the returned character data or making a copy of
     * the character data.
     *
     * @param quote   The quote character that signifies the end of the
     *                attribute value data.
     * @param content The content structure to fill.
     *
     * @return Returns the next character on the input, if known. This
     *         value may be -1 but this does <em>note</em> designate
     *         end of file.
     *
     * @throws IOException  Thrown if i/o error occurs.
     * @throws EOFException Thrown on end of file.
     */
    public int scanLiteral(int quote, XMLString content)
        throws IOException {

        // load more characters, if needed
        if (fCurrentEntity.position == fCurrentEntity.count) {
            load(0, true);
        }
        else if (fCurrentEntity.position == fCurrentEntity.count - 1) {
            fCurrentEntity.ch[0] = fCurrentEntity.ch[fCurrentEntity.count - 1];
            load(1, false);
            fCurrentEntity.startPosition = 0;
            fCurrentEntity.position = 0;
        }

        // normalize newlines
        int offset = fCurrentEntity.position;
        int c = fCurrentEntity.ch[offset];
        int newlines = 0;
        boolean external = fCurrentEntity.isExternal();
        if (c == '\n' || ((c == '\r' || c == 0x85 || c == 0x2028) && external)) {
            do {
                c = fCurrentEntity.ch[fCurrentEntity.position++];
                if ((c == '\r' ) && external) {
                    newlines++;
                    fCurrentEntity.lineNumber++;
                    fCurrentEntity.columnNumber = 1;
                    if (fCurrentEntity.position == fCurrentEntity.count) {
                        offset = 0;
                        fCurrentEntity.baseCharOffset += (fCurrentEntity.position - fCurrentEntity.startPosition);
                        fCurrentEntity.position = newlines;
                        fCurrentEntity.startPosition = newlines;
                        if (load(newlines, false)) {
                            break;
                        }
                    }
                    int cc = fCurrentEntity.ch[fCurrentEntity.position]; 
                    if (cc == '\n' || cc == 0x85) {
                        fCurrentEntity.position++;
                        offset++;
                    }
                    /*** NEWLINE NORMALIZATION ***/
                    else {
                        newlines++;
                    }
                }
                else if (c == '\n' || ((c == 0x85 || c == 0x2028) && external)) {
                    newlines++;
                    fCurrentEntity.lineNumber++;
                    fCurrentEntity.columnNumber = 1;
                    if (fCurrentEntity.position == fCurrentEntity.count) {
                        offset = 0;
                        fCurrentEntity.baseCharOffset += (fCurrentEntity.position - fCurrentEntity.startPosition);
                        fCurrentEntity.position = newlines;
                        fCurrentEntity.startPosition = newlines;
                        if (load(newlines, false)) {
                            break;
                        }
                    }
                }
                else {
                    fCurrentEntity.position--;
                    break;
                }
            } while (fCurrentEntity.position < fCurrentEntity.count - 1);
            for (int i = offset; i < fCurrentEntity.position; i++) {
                fCurrentEntity.ch[i] = '\n';
            }
            int length = fCurrentEntity.position - offset;
            if (fCurrentEntity.position == fCurrentEntity.count - 1) {
                content.setValues(fCurrentEntity.ch, offset, length);
                return -1;
            }
        }

        // scan literal value
        if (external) {
            while (fCurrentEntity.position < fCurrentEntity.count) {
                c = fCurrentEntity.ch[fCurrentEntity.position++];
                if (c == quote || c == '%' || !XML11Char.isXML11Content(c) 
                    || c == 0x85 || c == 0x2028) {
                    fCurrentEntity.position--;
                    break;
                }
            }
        }
        else {
            while (fCurrentEntity.position < fCurrentEntity.count) {
                c = fCurrentEntity.ch[fCurrentEntity.position++];
                // In internal entities control characters are allowed to appear unescaped.
                if ((c == quote && !fCurrentEntity.literal)
                    || c == '%' || !XML11Char.isXML11InternalEntityContent(c)) {
                    fCurrentEntity.position--;
                    break;
                }
            }
        }
        int length = fCurrentEntity.position - offset;
        fCurrentEntity.columnNumber += length - newlines;
        content.setValues(fCurrentEntity.ch, offset, length);

        // return next character
        if (fCurrentEntity.position != fCurrentEntity.count) {
            c = fCurrentEntity.ch[fCurrentEntity.position];
            // NOTE: We don't want to accidentally signal the
            //       end of the literal if we're expanding an
            //       entity appearing in the literal. -Ac
            if (c == quote && fCurrentEntity.literal) {
                c = -1;
            }
        }
        else {
            c = -1;
        }
        return c;

    } // scanLiteral(int,XMLString):int

    /**
     * Scans a range of character data up to the specicied delimiter,
     * setting the fields of the XMLString structure, appropriately.
     * <p>
     * <strong>Note:</strong> The characters are consumed.
     * <p>
     * <strong>Note:</strong> This assumes that the internal buffer is
     * at least the same size, or bigger, than the length of the delimiter
     * and that the delimiter contains at least one character.
     * <p>
     * <strong>Note:</strong> This method does not guarantee to return
     * the longest run of character data. This method may return before
     * the delimiter due to reaching the end of the input buffer or any
     * other reason.
     * <p>
     * <strong>Note:</strong> The fields contained in the XMLString
     * structure are not guaranteed to remain valid upon subsequent calls
     * to the entity scanner. Therefore, the caller is responsible for
     * immediately using the returned character data or making a copy of
     * the character data.
     *
     * @param delimiter The string that signifies the end of the character
     *                  data to be scanned.
     * @param buffer    The XMLStringBuffer to fill.
     *
     * @return Returns true if there is more data to scan, false otherwise.
     *
     * @throws IOException  Thrown if i/o error occurs.
     * @throws EOFException Thrown on end of file.
     */
    public boolean scanData(String delimiter, XMLStringBuffer buffer)
        throws IOException {

        boolean done = false;
        int delimLen = delimiter.length();
        char charAt0 = delimiter.charAt(0);
        boolean external = fCurrentEntity.isExternal();
        do {
            // load more characters, if needed 
            if (fCurrentEntity.position == fCurrentEntity.count) {
                load(0, true);
            }

            boolean bNextEntity = false;

            while ((fCurrentEntity.position >= fCurrentEntity.count - delimLen)
                && (!bNextEntity))
            {
              System.arraycopy(fCurrentEntity.ch,
                               fCurrentEntity.position,
                               fCurrentEntity.ch,
                               0,
                               fCurrentEntity.count - fCurrentEntity.position);

              bNextEntity = load(fCurrentEntity.count - fCurrentEntity.position, false);
              fCurrentEntity.position = 0;
              fCurrentEntity.startPosition = 0;
            }

            if (fCurrentEntity.position >= fCurrentEntity.count - delimLen) {
                // something must be wrong with the input:  e.g., file ends  an unterminated comment
                int length = fCurrentEntity.count - fCurrentEntity.position;
                buffer.append (fCurrentEntity.ch, fCurrentEntity.position, length); 
                fCurrentEntity.columnNumber += fCurrentEntity.count;
                fCurrentEntity.baseCharOffset += (fCurrentEntity.position - fCurrentEntity.startPosition);
                fCurrentEntity.position = fCurrentEntity.count;
                fCurrentEntity.startPosition = fCurrentEntity.count;
                load(0,true);
                return false;
            }

            // normalize newlines
            int offset = fCurrentEntity.position;
            int c = fCurrentEntity.ch[offset];
            int newlines = 0;
            if (c == '\n' || ((c == '\r' || c == 0x85 || c == 0x2028) && external)) {
                do {
                    c = fCurrentEntity.ch[fCurrentEntity.position++];
                    if ((c == '\r' ) && external) {
                        newlines++;
                        fCurrentEntity.lineNumber++;
                        fCurrentEntity.columnNumber = 1;
                        if (fCurrentEntity.position == fCurrentEntity.count) {
                            offset = 0;
                            fCurrentEntity.baseCharOffset += (fCurrentEntity.position - fCurrentEntity.startPosition);
                            fCurrentEntity.position = newlines;
                            fCurrentEntity.startPosition = newlines;
                            if (load(newlines, false)) {
                                break;
                            }
                        }
                        int cc = fCurrentEntity.ch[fCurrentEntity.position]; 
                        if (cc == '\n' || cc == 0x85) {
                            fCurrentEntity.position++;
                            offset++;
                        }
                        /*** NEWLINE NORMALIZATION ***/
                        else {
                            newlines++;
                        }
                    }
                    else if (c == '\n' || ((c == 0x85 || c == 0x2028) && external)) {
                        newlines++;
                        fCurrentEntity.lineNumber++;
                        fCurrentEntity.columnNumber = 1;
                        if (fCurrentEntity.position == fCurrentEntity.count) {
                            offset = 0;
                            fCurrentEntity.baseCharOffset += (fCurrentEntity.position - fCurrentEntity.startPosition);
                            fCurrentEntity.position = newlines;
                            fCurrentEntity.startPosition = newlines;
                            fCurrentEntity.count = newlines;
                            if (load(newlines, false)) {
                                break;
                            }
                        }
                    }
                    else {
                        fCurrentEntity.position--;
                        break;
                    }
                } while (fCurrentEntity.position < fCurrentEntity.count - 1);
                for (int i = offset; i < fCurrentEntity.position; i++) {
                    fCurrentEntity.ch[i] = '\n';
                }
                int length = fCurrentEntity.position - offset;
                if (fCurrentEntity.position == fCurrentEntity.count - 1) {
                    buffer.append(fCurrentEntity.ch, offset, length);
                    return true;
                }
            }

            // iterate over buffer looking for delimiter
            if (external) {
                OUTER: while (fCurrentEntity.position < fCurrentEntity.count) {
                    c = fCurrentEntity.ch[fCurrentEntity.position++];
                    if (c == charAt0) {
                        // looks like we just hit the delimiter
                        int delimOffset = fCurrentEntity.position - 1;
                        for (int i = 1; i < delimLen; i++) {
                            if (fCurrentEntity.position == fCurrentEntity.count) {
                                fCurrentEntity.position -= i;
                                break OUTER;
                            }
                            c = fCurrentEntity.ch[fCurrentEntity.position++];
                            if (delimiter.charAt(i) != c) {
                                fCurrentEntity.position--;
                                break;
                            }
                         }
                         if (fCurrentEntity.position == delimOffset + delimLen) {
                            done = true;
                            break;
                         }
                    }
                    else if (c == '\n' || c == '\r' || c == 0x85 || c == 0x2028) {
                        fCurrentEntity.position--;
                        break;
                    }
                    // In external entities control characters cannot appear 
                    // as literals so do not skip over them.
                    else if (!XML11Char.isXML11ValidLiteral(c)) {
                        fCurrentEntity.position--;
                        int length = fCurrentEntity.position - offset;
                        fCurrentEntity.columnNumber += length - newlines;
                        buffer.append(fCurrentEntity.ch, offset, length); 
                        return true;
                    }
                }
            }
            else {
                OUTER: while (fCurrentEntity.position < fCurrentEntity.count) {
                    c = fCurrentEntity.ch[fCurrentEntity.position++];
                    if (c == charAt0) {
                        // looks like we just hit the delimiter
                        int delimOffset = fCurrentEntity.position - 1;
                        for (int i = 1; i < delimLen; i++) {
                            if (fCurrentEntity.position == fCurrentEntity.count) {
                                fCurrentEntity.position -= i;
                                break OUTER;
                            }
                            c = fCurrentEntity.ch[fCurrentEntity.position++];
                            if (delimiter.charAt(i) != c) {
                                fCurrentEntity.position--;
                                break;
                            }
                        }
                        if (fCurrentEntity.position == delimOffset + delimLen) {
                            done = true;
                            break;
                        }
                    }
                    else if (c == '\n') {
                        fCurrentEntity.position--;
                        break;
                    }
                    // Control characters are allowed to appear as literals
                    // in internal entities.
                    else if (!XML11Char.isXML11Valid(c)) {
                        fCurrentEntity.position--;
                        int length = fCurrentEntity.position - offset;
                        fCurrentEntity.columnNumber += length - newlines;
                        buffer.append(fCurrentEntity.ch, offset, length); 
                        return true;
                    }
                }
            }
            int length = fCurrentEntity.position - offset;
            fCurrentEntity.columnNumber += length - newlines;
            if (done) {
                length -= delimLen;
            }
            buffer.append(fCurrentEntity.ch, offset, length);

            // return true if string was skipped
        } while (!done);
        return !done;

    } // scanData(String,XMLString)

    /**
     * Skips a character appearing immediately on the input.
     * <p>
     * <strong>Note:</strong> The character is consumed only if it matches
     * the specified character.
     *
     * @param c The character to skip.
     *
     * @return Returns true if the character was skipped.
     *
     * @throws IOException  Thrown if i/o error occurs.
     * @throws EOFException Thrown on end of file.
     */
    public boolean skipChar(int c) throws IOException {

        // load more characters, if needed
        if (fCurrentEntity.position == fCurrentEntity.count) {
            load(0, true);
        }

        // skip character
        int cc = fCurrentEntity.ch[fCurrentEntity.position];
        if (cc == c) {
            fCurrentEntity.position++;
            if (c == '\n') {
                fCurrentEntity.lineNumber++;
                fCurrentEntity.columnNumber = 1;
            }
            else {
                fCurrentEntity.columnNumber++;
            }
            return true;
        }
        else if (c == '\n' && ((cc == 0x2028 || cc == 0x85) && fCurrentEntity.isExternal())) {
            fCurrentEntity.position++;
            fCurrentEntity.lineNumber++;
            fCurrentEntity.columnNumber = 1;
            return true;
        }
        else if (c == '\n' && (cc == '\r' ) && fCurrentEntity.isExternal()) {
            // handle newlines
            if (fCurrentEntity.position == fCurrentEntity.count) {
                fCurrentEntity.ch[0] = (char)cc;
                load(1, false);
            }
            int ccc = fCurrentEntity.ch[++fCurrentEntity.position];
            if (ccc == '\n' || ccc == 0x85) {
                fCurrentEntity.position++;
            }
            fCurrentEntity.lineNumber++;
            fCurrentEntity.columnNumber = 1;
            return true;
        }

        // character was not skipped
        return false;

    } // skipChar(int):boolean

    /**
     * Skips space characters appearing immediately on the input.
     * <p>
     * <strong>Note:</strong> The characters are consumed only if they are
     * space characters.
     *
     * @return Returns true if at least one space character was skipped.
     *
     * @throws IOException  Thrown if i/o error occurs.
     * @throws EOFException Thrown on end of file.
     *
     * @see XMLChar#isSpace
     * @see XML11Char#isXML11Space
     */
    public boolean skipSpaces() throws IOException {

        // load more characters, if needed
        if (fCurrentEntity.position == fCurrentEntity.count) {
            load(0, true);
        }

        // skip spaces
        int c = fCurrentEntity.ch[fCurrentEntity.position];
        
        // External --  Match: S + 0x85 + 0x2028, and perform end of line normalization
        if (fCurrentEntity.isExternal()) {
            if (XML11Char.isXML11Space(c)) {
                do {
                    boolean entityChanged = false;
                    // handle newlines
                    if (c == '\n' || c == '\r' || c == 0x85 || c == 0x2028) {
                        fCurrentEntity.lineNumber++;
                        fCurrentEntity.columnNumber = 1;
                        if (fCurrentEntity.position == fCurrentEntity.count - 1) {
                            fCurrentEntity.ch[0] = (char)c;
                            entityChanged = load(1, true);
                            if (!entityChanged) {
                                // the load change the position to be 1,
                                // need to restore it when entity not changed
                                fCurrentEntity.startPosition = 0;
                                fCurrentEntity.position = 0;
                            }
                        }
                        if (c == '\r') {
                            // REVISIT: Does this need to be updated to fix the
                            //          #x0D ^#x0A newline normalization problem? -Ac
                            int cc = fCurrentEntity.ch[++fCurrentEntity.position];
                            if (cc != '\n' && cc != 0x85 ) {
                                fCurrentEntity.position--;
                            }
                        }
                    }
                    else {
                        fCurrentEntity.columnNumber++;
                    }
                    // load more characters, if needed
                    if (!entityChanged)
                        fCurrentEntity.position++;
                    if (fCurrentEntity.position == fCurrentEntity.count) {
                        load(0, true);
                    }
                } while (XML11Char.isXML11Space(c = fCurrentEntity.ch[fCurrentEntity.position]));
                return true;
            }
        }
        // Internal -- Match: S (only)
        else if (XMLChar.isSpace(c)) {
            do {
                boolean entityChanged = false;
                // handle newlines
                if (c == '\n') {
                    fCurrentEntity.lineNumber++;
                    fCurrentEntity.columnNumber = 1;
                    if (fCurrentEntity.position == fCurrentEntity.count - 1) {
                        fCurrentEntity.ch[0] = (char)c;
                        entityChanged = load(1, true);
                        if (!entityChanged) {
                            // the load change the position to be 1,
                            // need to restore it when entity not changed
                            fCurrentEntity.startPosition = 0;
                            fCurrentEntity.position = 0;
                        }
                    }
                }
                else {
                    fCurrentEntity.columnNumber++;
                }
                // load more characters, if needed
                if (!entityChanged)
                    fCurrentEntity.position++;
                if (fCurrentEntity.position == fCurrentEntity.count) {
                    load(0, true);
                }
            } while (XMLChar.isSpace(c = fCurrentEntity.ch[fCurrentEntity.position]));
            return true;
        }

        // no spaces were found
        return false;

    } // skipSpaces():boolean

    /**
     * Skips the specified string appearing immediately on the input.
     * <p>
     * <strong>Note:</strong> The characters are consumed only if they are
     * space characters.
     *
     * @param s The string to skip.
     *
     * @return Returns true if the string was skipped.
     *
     * @throws IOException  Thrown if i/o error occurs.
     * @throws EOFException Thrown on end of file.
     */
    public boolean skipString(String s) throws IOException {

        // load more characters, if needed
        if (fCurrentEntity.position == fCurrentEntity.count) {
            load(0, true);
        }

        // skip string
        final int length = s.length();
        for (int i = 0; i < length; i++) {
            char c = fCurrentEntity.ch[fCurrentEntity.position++];
            if (c != s.charAt(i)) {
                fCurrentEntity.position -= i + 1;
                return false;
            }
            if (i < length - 1 && fCurrentEntity.position == fCurrentEntity.count) {
                System.arraycopy(fCurrentEntity.ch, fCurrentEntity.count - i - 1, fCurrentEntity.ch, 0, i + 1);
                // REVISIT: Can a string to be skipped cross an
                //          entity boundary? -Ac
                if (load(i + 1, false)) {
                    fCurrentEntity.startPosition -= i + 1;
                    fCurrentEntity.position -= i + 1;
                    return false;
                }
            }
        }
        fCurrentEntity.columnNumber += length;
        return true;

    } // skipString(String):boolean

} // class XML11EntityScanner

