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
import java.util.Locale;

import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityManager;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLErrorReporter;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.io.UCSReader;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.msg.XMLMessageFormatter;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.util.SymbolTable;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.util.XMLChar;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.util.XMLStringBuffer;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.QName;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XMLLocator;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XMLString;

/**
 * Implements the entity scanner methods.
 *
 * @xerces.internal
 * 
 * @author Andy Clark, IBM
 * @author Neil Graham, IBM
 * @version $Id$
 */
public class XMLEntityScanner implements XMLLocator {

    // constants
    private static final boolean DEBUG_ENCODINGS = false;
    private static final boolean DEBUG_BUFFER = false;
    
    /**
     * To signal the end of the document entity, this exception will be thrown.
     */
    private static final EOFException END_OF_DOCUMENT_ENTITY = new EOFException() {
        private static final long serialVersionUID = 980337771224675268L;
        public Throwable fillInStackTrace() {
            return this;
        }
    };

    //
    // Data
    //

    private org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityManager fEntityManager = null;
    protected org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityManager.ScannedEntity fCurrentEntity = null;

    protected SymbolTable fSymbolTable = null;

    protected int fBufferSize = org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityManager.DEFAULT_BUFFER_SIZE;

    /**
     * Error reporter. This property identifier is:
     * http://apache.org/xml/properties/internal/error-reporter
     */
    protected XMLErrorReporter fErrorReporter;
    //
    // Constructors
    //

    /** Default constructor. */
    public XMLEntityScanner() {
    } // <init>()

    //
    // XMLEntityScanner methods
    //

    /**
     * Returns the base system identifier of the currently scanned
     * entity, or null if none is available.
     */
    public final String getBaseSystemId() {
        return (fCurrentEntity != null && fCurrentEntity.entityLocation != null) ? fCurrentEntity.entityLocation.getExpandedSystemId() : null;
    } // getBaseSystemId():String

    /**
     * Sets the encoding of the scanner. This method is used by the
     * scanners if the XMLDecl or TextDecl line contains an encoding
     * pseudo-attribute.
     * <p>
     * <strong>Note:</strong> The underlying character reader on the
     * current entity will be changed to accomodate the new encoding.
     * However, the new encoding is ignored if the current reader was
     * not constructed from an input stream (e.g. an external entity
     * that is resolved directly to the appropriate java.io.Reader
     * object).
     *
     * @param encoding The IANA encoding name of the new encoding.
     *
     * @throws IOException Thrown if the new encoding is not supported.
     *
     * @see org.smooks.engine.delivery.sax.ng.org.apache.xerces.util.EncodingMap
     */
    public final void setEncoding(String encoding) throws IOException {

        if (DEBUG_ENCODINGS) {
            System.out.println("$$$ setEncoding: "+encoding);
        }

        if (fCurrentEntity.stream != null) {
            // if the encoding is the same, don't change the reader and
            // re-use the original reader used by the OneCharReader
            // NOTE: Besides saving an object, this overcomes deficiencies
            //       in the UTF-16 reader supplied with the standard Java
            //       distribution (up to and including 1.3). The UTF-16
            //       decoder buffers 8K blocks even when only asked to read
            //       a single char! -Ac
            if (fCurrentEntity.encoding == null ||
                !fCurrentEntity.encoding.equals(encoding)) {
                // UTF-16 is a bit of a special case.  If the encoding is UTF-16,
                // and we know the endian-ness, we shouldn't change readers.
                // If it's ISO-10646-UCS-(2|4), then we'll have to deduce
                // the endian-ness from the encoding we presently have.
                if(fCurrentEntity.encoding != null && fCurrentEntity.encoding.startsWith("UTF-16")) {
                    String ENCODING = encoding.toUpperCase(Locale.ENGLISH);
                    if(ENCODING.equals("UTF-16")) return;
                    if(ENCODING.equals("ISO-10646-UCS-4")) {
                        if(fCurrentEntity.encoding.equals("UTF-16BE")) {
                            fCurrentEntity.reader = new UCSReader(fCurrentEntity.stream, UCSReader.UCS4BE);
                        } else {
                            fCurrentEntity.reader = new UCSReader(fCurrentEntity.stream, UCSReader.UCS4LE);
                        }
                        return;
                    }
                    if(ENCODING.equals("ISO-10646-UCS-2")) {
                        if(fCurrentEntity.encoding.equals("UTF-16BE")) {
                            fCurrentEntity.reader = new UCSReader(fCurrentEntity.stream, UCSReader.UCS2BE);
                        } else {
                            fCurrentEntity.reader = new UCSReader(fCurrentEntity.stream, UCSReader.UCS2LE);
                        }
                        return;
                    }
                }
                // wrap a new reader around the input stream, changing
                // the encoding
                if (DEBUG_ENCODINGS) {
                    System.out.println("$$$ creating new reader from stream: "+
                                    fCurrentEntity.stream);
                }
                //fCurrentEntity.stream.reset();
                fCurrentEntity.setReader(fCurrentEntity.stream, encoding, null);
                fCurrentEntity.encoding = encoding;
            } else {
                if (DEBUG_ENCODINGS)
                    System.out.println("$$$ reusing old reader on stream");
            }
        }

    } // setEncoding(String)
    
    /**
     * Sets the XML version. This method is used by the
     * scanners to report the value of the version pseudo-attribute
     * in an XML or text declaration.
     *
     * @param xmlVersion the XML version of the current entity
     */
    public final void setXMLVersion(String xmlVersion) {
        fCurrentEntity.xmlVersion = xmlVersion;
    } // setXMLVersion(String)

    /** Returns true if the current entity being scanned is external. */
    public final boolean isExternal() {
        return fCurrentEntity.isExternal();
    } // isExternal():boolean

    /**
     * Returns the next character on the input.
     * <p>
     * <strong>Note:</strong> The character is <em>not</em> consumed.
     *
     * @throws IOException  Thrown if i/o error occurs.
     * @throws EOFException Thrown on end of file.
     */
    public int peekChar() throws IOException {
        if (DEBUG_BUFFER) {
            System.out.print("(peekChar: ");
            org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityManager.print(fCurrentEntity);
            System.out.println();
        }

        // load more characters, if needed
        if (fCurrentEntity.position == fCurrentEntity.count) {
            load(0, true);
        }

        // peek at character
        int c = fCurrentEntity.ch[fCurrentEntity.position];

        // return peeked character
        if (DEBUG_BUFFER) {
            System.out.print(")peekChar: ");
            org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityManager.print(fCurrentEntity);
            if (fCurrentEntity.isExternal()) {
                System.out.println(" -> '"+(c!='\r'?(char)c:'\n')+"'");
            }
            else {
                System.out.println(" -> '"+(char)c+"'");
            }
        }
        if (fCurrentEntity.isExternal()) {
            return c != '\r' ? c : '\n';
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
        if (DEBUG_BUFFER) {
            System.out.print("(scanChar: ");
            org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityManager.print(fCurrentEntity);
            System.out.println();
        }

        // load more characters, if needed
        if (fCurrentEntity.position == fCurrentEntity.count) {
            load(0, true);
        }

        // scan character
        int c = fCurrentEntity.ch[fCurrentEntity.position++];
        boolean external = false;
        if (c == '\n' ||
            (c == '\r' && (external = fCurrentEntity.isExternal()))) {
            fCurrentEntity.lineNumber++;
            fCurrentEntity.columnNumber = 1;
            if (fCurrentEntity.position == fCurrentEntity.count) {
                fCurrentEntity.ch[0] = (char)c;
                load(1, false);
            }
            if (c == '\r' && external) {
                if (fCurrentEntity.ch[fCurrentEntity.position++] != '\n') {
                    fCurrentEntity.position--;
                }
                c = '\n';
            }
        }

        // return character that was scanned
        if (DEBUG_BUFFER) {
            System.out.print(")scanChar: ");
            org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityManager.print(fCurrentEntity);
            System.out.println(" -> '"+(char)c+"'");
        }
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
     * @see SymbolTable
     * @see XMLChar#isName
     */
    public String scanNmtoken() throws IOException {
        if (DEBUG_BUFFER) {
            System.out.print("(scanNmtoken: ");
            org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityManager.print(fCurrentEntity);
            System.out.println();
        }

        // load more characters, if needed
        if (fCurrentEntity.position == fCurrentEntity.count) {
            load(0, true);
        }

        // scan nmtoken
        int offset = fCurrentEntity.position;
        while (XMLChar.isName(fCurrentEntity.ch[fCurrentEntity.position])) {
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
        int length = fCurrentEntity.position - offset;
        fCurrentEntity.columnNumber += length;

        // return nmtoken
        String symbol = null;
        if (length > 0) {
            symbol = fSymbolTable.addSymbol(fCurrentEntity.ch, offset, length);
        }
        if (DEBUG_BUFFER) {
            System.out.print(")scanNmtoken: ");
            org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityManager.print(fCurrentEntity);
            System.out.println(" -> "+String.valueOf(symbol));
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
     * @see SymbolTable
     * @see XMLChar#isName
     * @see XMLChar#isNameStart
     */
    public String scanName() throws IOException {
        if (DEBUG_BUFFER) {
            System.out.print("(scanName: ");
            org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityManager.print(fCurrentEntity);
            System.out.println();
        }

        // load more characters, if needed
        if (fCurrentEntity.position == fCurrentEntity.count) {
            load(0, true);
        }

        // scan name
        int offset = fCurrentEntity.position;
        if (XMLChar.isNameStart(fCurrentEntity.ch[offset])) {
            if (++fCurrentEntity.position == fCurrentEntity.count) {
                fCurrentEntity.ch[0] = fCurrentEntity.ch[offset];
                offset = 0;
                if (load(1, false)) {
                    fCurrentEntity.columnNumber++;
                    String symbol = fSymbolTable.addSymbol(fCurrentEntity.ch, 0, 1);
                    if (DEBUG_BUFFER) {
                        System.out.print(")scanName: ");
                        org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityManager.print(fCurrentEntity);
                        System.out.println(" -> "+String.valueOf(symbol));
                    }
                    return symbol;
                }
            }
            while (XMLChar.isName(fCurrentEntity.ch[fCurrentEntity.position])) {
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
        }
        int length = fCurrentEntity.position - offset;
        fCurrentEntity.columnNumber += length;

        // return name
        String symbol = null;
        if (length > 0) {
            symbol = fSymbolTable.addSymbol(fCurrentEntity.ch, offset, length);
        }
        if (DEBUG_BUFFER) {
            System.out.print(")scanName: ");
            org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityManager.print(fCurrentEntity);
            System.out.println(" -> "+String.valueOf(symbol));
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
     * @see SymbolTable
     * @see XMLChar#isNCName
     * @see XMLChar#isNCNameStart
     */
    public String scanNCName() throws IOException {
        if (DEBUG_BUFFER) {
            System.out.print("(scanNCName: ");
            org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityManager.print(fCurrentEntity);
            System.out.println();
        }

        // load more characters, if needed
        if (fCurrentEntity.position == fCurrentEntity.count) {
            load(0, true);
        }

        // scan name
        int offset = fCurrentEntity.position;
        if (XMLChar.isNCNameStart(fCurrentEntity.ch[offset])) {
            if (++fCurrentEntity.position == fCurrentEntity.count) {
                fCurrentEntity.ch[0] = fCurrentEntity.ch[offset];
                offset = 0;
                if (load(1, false)) {
                    fCurrentEntity.columnNumber++;
                    String symbol = fSymbolTable.addSymbol(fCurrentEntity.ch, 0, 1);
                    if (DEBUG_BUFFER) {
                        System.out.print(")scanNCName: ");
                        org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityManager.print(fCurrentEntity);
                        System.out.println(" -> "+String.valueOf(symbol));
                    }
                    return symbol;
                }
            }
            while (XMLChar.isNCName(fCurrentEntity.ch[fCurrentEntity.position])) {
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
        }
        int length = fCurrentEntity.position - offset;
        fCurrentEntity.columnNumber += length;

        // return name
        String symbol = null;
        if (length > 0) {
            symbol = fSymbolTable.addSymbol(fCurrentEntity.ch, offset, length);
        }
        if (DEBUG_BUFFER) {
            System.out.print(")scanNCName: ");
            org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityManager.print(fCurrentEntity);
            System.out.println(" -> "+String.valueOf(symbol));
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
     * @see SymbolTable
     * @see XMLChar#isName
     * @see XMLChar#isNameStart
     */
    public boolean scanQName(QName qname) throws IOException {
        if (DEBUG_BUFFER) {
            System.out.print("(scanQName, "+qname+": ");
            org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityManager.print(fCurrentEntity);
            System.out.println();
        }

        // load more characters, if needed
        if (fCurrentEntity.position == fCurrentEntity.count) {
            load(0, true);
        }

        // scan qualified name
        int offset = fCurrentEntity.position;
        if (XMLChar.isNCNameStart(fCurrentEntity.ch[offset])) {
            if (++fCurrentEntity.position == fCurrentEntity.count) {
                fCurrentEntity.ch[0] = fCurrentEntity.ch[offset];
                offset = 0;
                if (load(1, false)) {
                    fCurrentEntity.columnNumber++;
                    String name =
                        fSymbolTable.addSymbol(fCurrentEntity.ch, 0, 1);
                    qname.setValues(null, name, name, null);
                    if (DEBUG_BUFFER) {
                        System.out.print(")scanQName, "+qname+": ");
                        org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityManager.print(fCurrentEntity);
                        System.out.println(" -> true");
                    }
                    return true;
                }
            }
            int index = -1;
            while (XMLChar.isName(fCurrentEntity.ch[fCurrentEntity.position])) {
                char c = fCurrentEntity.ch[fCurrentEntity.position];

                if (c == ':') {
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
                    if (!XMLChar.isNCNameStart(fCurrentEntity.ch[startLocal])){
                        fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN,
                                                 "IllegalQName",
                                                  null,
                                                  XMLErrorReporter.SEVERITY_FATAL_ERROR);
                    }
                    localpart = fSymbolTable.addSymbol(fCurrentEntity.ch,
                                                       startLocal, len);

                }
                else {
                    localpart = rawname;
                }
                qname.setValues(prefix, localpart, rawname, null);
                if (DEBUG_BUFFER) {
                    System.out.print(")scanQName, "+qname+": ");
                    org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityManager.print(fCurrentEntity);
                    System.out.println(" -> true");
                }
                return true;
            }
        }

        // no qualified name found
        if (DEBUG_BUFFER) {
            System.out.print(")scanQName, "+qname+": ");
            org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityManager.print(fCurrentEntity);
            System.out.println(" -> false");
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
        if (DEBUG_BUFFER) {
            System.out.print("(scanContent: ");
            org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityManager.print(fCurrentEntity);
            System.out.println();
        }

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
        if (c == '\n' || (c == '\r' && external)) {
            if (DEBUG_BUFFER) {
                System.out.print("[newline, "+offset+", "+fCurrentEntity.position+": ");
                org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityManager.print(fCurrentEntity);
                System.out.println();
            }
            do {
                c = fCurrentEntity.ch[fCurrentEntity.position++];
                if (c == '\r' && external) {
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
                    if (fCurrentEntity.ch[fCurrentEntity.position] == '\n') {
                        fCurrentEntity.position++;
                        offset++;
                    }
                    /*** NEWLINE NORMALIZATION ***/
                    else {
                        newlines++;
                    }
                }
                else if (c == '\n') {
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
                if (DEBUG_BUFFER) {
                    System.out.print("]newline, "+offset+", "+fCurrentEntity.position+": ");
                    org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityManager.print(fCurrentEntity);
                    System.out.println();
                }
                return -1;
            }
            if (DEBUG_BUFFER) {
                System.out.print("]newline, "+offset+", "+fCurrentEntity.position+": ");
                org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityManager.print(fCurrentEntity);
                System.out.println();
            }
        }

        // inner loop, scanning for content
        while (fCurrentEntity.position < fCurrentEntity.count) {
            c = fCurrentEntity.ch[fCurrentEntity.position++];
            if (!XMLChar.isContent(c)) {
                fCurrentEntity.position--;
                break;
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
            if (c == '\r' && external) {
                c = '\n';
            }
        }
        else {
            c = -1;
        }
        if (DEBUG_BUFFER) {
            System.out.print(")scanContent: ");
            org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityManager.print(fCurrentEntity);
            System.out.println(" -> '"+(char)c+"'");
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
        if (DEBUG_BUFFER) {
            System.out.print("(scanLiteral, '"+(char)quote+"': ");
            org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityManager.print(fCurrentEntity);
            System.out.println();
        }

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
        if (c == '\n' || (c == '\r' && external)) {
            if (DEBUG_BUFFER) {
                System.out.print("[newline, "+offset+", "+fCurrentEntity.position+": ");
                org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityManager.print(fCurrentEntity);
                System.out.println();
            }
            do {
                c = fCurrentEntity.ch[fCurrentEntity.position++];
                if (c == '\r' && external) {
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
                    if (fCurrentEntity.ch[fCurrentEntity.position] == '\n') {
                        fCurrentEntity.position++;
                        offset++;
                    }
                    /*** NEWLINE NORMALIZATION ***/
                    else {
                        newlines++;
                    }
                    /***/
                }
                else if (c == '\n') {
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
                if (DEBUG_BUFFER) {
                    System.out.print("]newline, "+offset+", "+fCurrentEntity.position+": ");
                    org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityManager.print(fCurrentEntity);
                    System.out.println();
                }
                return -1;
            }
            if (DEBUG_BUFFER) {
                System.out.print("]newline, "+offset+", "+fCurrentEntity.position+": ");
                org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityManager.print(fCurrentEntity);
                System.out.println();
            }
        }

        // scan literal value
        while (fCurrentEntity.position < fCurrentEntity.count) {
            c = fCurrentEntity.ch[fCurrentEntity.position++];
            if ((c == quote &&
                 (!fCurrentEntity.literal || external))
                || c == '%' || !XMLChar.isContent(c)) {
                fCurrentEntity.position--;
                break;
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
        if (DEBUG_BUFFER) {
            System.out.print(")scanLiteral, '"+(char)quote+"': ");
            org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityManager.print(fCurrentEntity);
            System.out.println(" -> '"+(char)c+"'");
        }
        return c;

    } // scanLiteral(int,XMLString):int

    /**
     * Scans a range of character data up to the specified delimiter,
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

        // REVISIT: This method does not need to use a string buffer.
        //          The change would avoid the array copies and increase
        //          performance. -Ac
        //
        //          Currently, this method is called for scanning CDATA
        //          sections, comments,  and processing instruction data. 
        //          So if this code is updated to NOT buffer, the scanning
        //          code for comments and processing instructions will 
        //          need to be updated to do its own buffering. The code 
        //          for CDATA sections is safe as-is. -Ac

        boolean found = false;
        int delimLen = delimiter.length();
        char charAt0 = delimiter.charAt(0);
        boolean external = fCurrentEntity.isExternal();
        if (DEBUG_BUFFER) {
            System.out.print("(scanData: ");
            org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityManager.print(fCurrentEntity);
            System.out.println();
        }

        // load more characters, if needed

        if (fCurrentEntity.position == fCurrentEntity.count) {
            load(0, true);
        }

        boolean bNextEntity = false;

        while ((fCurrentEntity.position > fCurrentEntity.count - delimLen)
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

        if (fCurrentEntity.position > fCurrentEntity.count - delimLen) {
            // something must be wrong with the input:  e.g., file ends in an unterminated comment
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
        if (c == '\n' || (c == '\r' && external)) {
            if (DEBUG_BUFFER) {
                System.out.print("[newline, "+offset+", "+fCurrentEntity.position+": ");
                org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityManager.print(fCurrentEntity);
                System.out.println();
            }
            do {
                c = fCurrentEntity.ch[fCurrentEntity.position++];
                if (c == '\r' && external) {
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
                    if (fCurrentEntity.ch[fCurrentEntity.position] == '\n') {
                        fCurrentEntity.position++;
                        offset++;
                    }
                    /*** NEWLINE NORMALIZATION ***/
                    else {
                        newlines++;
                    }
                }
                else if (c == '\n') {
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
                if (DEBUG_BUFFER) {
                    System.out.print("]newline, "+offset+", "+fCurrentEntity.position+": ");
                    org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityManager.print(fCurrentEntity);
                    System.out.println();
                }
                return true;
            }
            if (DEBUG_BUFFER) {
                System.out.print("]newline, "+offset+", "+fCurrentEntity.position+": ");
                org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityManager.print(fCurrentEntity);
                System.out.println();
            }
        }

        // iterate over buffer looking for delimiter
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
                    found = true;
                    break;
                }
            }
            else if (c == '\n' || (external && c == '\r')) {
                fCurrentEntity.position--;
                break;
            }
            else if (XMLChar.isInvalid(c)) {
                fCurrentEntity.position--;
                int length = fCurrentEntity.position - offset;
                fCurrentEntity.columnNumber += length - newlines;
                buffer.append(fCurrentEntity.ch, offset, length); 
                return true;
            }
        }
        int length = fCurrentEntity.position - offset;
        fCurrentEntity.columnNumber += length - newlines;
        if (found) {
            length -= delimLen;
        }
        buffer.append (fCurrentEntity.ch, offset, length);

        // return true if string was skipped
        if (DEBUG_BUFFER) {
            System.out.print(")scanData: ");
            org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityManager.print(fCurrentEntity);
            System.out.println(" -> " + !found);
        }
        return !found;

    } // scanData(String,XMLString):boolean

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
        if (DEBUG_BUFFER) {
            System.out.print("(skipChar, '"+(char)c+"': ");
            org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityManager.print(fCurrentEntity);
            System.out.println();
        }

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
            if (DEBUG_BUFFER) {
                System.out.print(")skipChar, '"+(char)c+"': ");
                org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityManager.print(fCurrentEntity);
                System.out.println(" -> true");
            }
            return true;
        }
        else if (c == '\n' && cc == '\r' && fCurrentEntity.isExternal()) {
            // handle newlines
            if (fCurrentEntity.position == fCurrentEntity.count) {
                fCurrentEntity.ch[0] = (char)cc;
                load(1, false);
            }
            fCurrentEntity.position++;
            if (fCurrentEntity.ch[fCurrentEntity.position] == '\n') {
                fCurrentEntity.position++;
            }
            fCurrentEntity.lineNumber++;
            fCurrentEntity.columnNumber = 1;
            if (DEBUG_BUFFER) {
                System.out.print(")skipChar, '"+(char)c+"': ");
                org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityManager.print(fCurrentEntity);
                System.out.println(" -> true");
            }
            return true;
        }

        // character was not skipped
        if (DEBUG_BUFFER) {
            System.out.print(")skipChar, '"+(char)c+"': ");
            org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityManager.print(fCurrentEntity);
            System.out.println(" -> false");
        }
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
     */
    public boolean skipSpaces() throws IOException {
        if (DEBUG_BUFFER) {
            System.out.print("(skipSpaces: ");
            org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityManager.print(fCurrentEntity);
            System.out.println();
        }

        // load more characters, if needed
        if (fCurrentEntity.position == fCurrentEntity.count) {
            load(0, true);
        }

        // skip spaces
        int c = fCurrentEntity.ch[fCurrentEntity.position];
        if (XMLChar.isSpace(c)) {
            boolean external = fCurrentEntity.isExternal();
            do {
                boolean entityChanged = false;
                // handle newlines
                if (c == '\n' || (external && c == '\r')) {
                    fCurrentEntity.lineNumber++;
                    fCurrentEntity.columnNumber = 1;
                    if (fCurrentEntity.position == fCurrentEntity.count - 1) {
                        fCurrentEntity.ch[0] = (char)c;
                        entityChanged = load(1, true);
                        if (!entityChanged) {
                            // the load change the position to be 1,
                            // need to restore it when entity not changed
                            fCurrentEntity.position = 0;
                            fCurrentEntity.startPosition = 0;
                        }
                    }
                    if (c == '\r' && external) {
                        // REVISIT: Does this need to be updated to fix the
                        //          #x0D ^#x0A newline normalization problem? -Ac
                        if (fCurrentEntity.ch[++fCurrentEntity.position] != '\n') {
                            fCurrentEntity.position--;
                        }
                    }
                    /*** NEWLINE NORMALIZATION ***
                    else {
                        if (fCurrentEntity.ch[fCurrentEntity.position + 1] == '\r'
                            && external) {
                            fCurrentEntity.position++;
                        }
                    }
                    /***/
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
            if (DEBUG_BUFFER) {
                System.out.print(")skipSpaces: ");
                org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityManager.print(fCurrentEntity);
                System.out.println(" -> true");
            }
            return true;
        }

        // no spaces were found
        if (DEBUG_BUFFER) {
            System.out.print(")skipSpaces: ");
            org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityManager.print(fCurrentEntity);
            System.out.println(" -> false");
        }
        return false;

    } // skipSpaces():boolean

    /**
     * Skips space characters appearing immediately on the input that would
     * match non-terminal S (0x09, 0x0A, 0x0D, 0x20) before end of line 
     * normalization is performed. This is useful when scanning structures 
     * such as the XMLDecl and TextDecl that can only contain US-ASCII 
     * characters.
     * <p>
     * <strong>Note:</strong> The characters are consumed only if they would
     * match non-terminal S before end of line normalization is performed.
     *
     * @return Returns true if at least one space character was skipped.
     *
     * @throws IOException  Thrown if i/o error occurs.
     * @throws EOFException Thrown on end of file.
     *
     * @see XMLChar#isSpace
     */
    public final boolean skipDeclSpaces() throws IOException {
        if (DEBUG_BUFFER) {
            System.out.print("(skipDeclSpaces: ");
            org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityManager.print(fCurrentEntity);
            System.out.println();
        }

        // load more characters, if needed
        if (fCurrentEntity.position == fCurrentEntity.count) {
            load(0, true);
        }

        // skip spaces
        int c = fCurrentEntity.ch[fCurrentEntity.position];
        if (XMLChar.isSpace(c)) {
            boolean external = fCurrentEntity.isExternal();
            do {
                boolean entityChanged = false;
                // handle newlines
                if (c == '\n' || (external && c == '\r')) {
                    fCurrentEntity.lineNumber++;
                    fCurrentEntity.columnNumber = 1;
                    if (fCurrentEntity.position == fCurrentEntity.count - 1) {
                        fCurrentEntity.ch[0] = (char)c;
                        entityChanged = load(1, true);
                        if (!entityChanged) {
                            // the load change the position to be 1,
                            // need to restore it when entity not changed
                            fCurrentEntity.position = 0;
                            fCurrentEntity.startPosition = 0;
                        }
                    }
                    if (c == '\r' && external) {
                        // REVISIT: Does this need to be updated to fix the
                        //          #x0D ^#x0A newline normalization problem? -Ac
                        if (fCurrentEntity.ch[++fCurrentEntity.position] != '\n') {
                            fCurrentEntity.position--;
                        }
                    }
                    /*** NEWLINE NORMALIZATION ***
                    else {
                        if (fCurrentEntity.ch[fCurrentEntity.position + 1] == '\r'
                            && external) {
                            fCurrentEntity.position++;
                        }
                    }
                    /***/
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
            if (DEBUG_BUFFER) {
                System.out.print(")skipDeclSpaces: ");
                org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityManager.print(fCurrentEntity);
                System.out.println(" -> true");
            }
            return true;
        }

        // no spaces were found
        if (DEBUG_BUFFER) {
            System.out.print(")skipDeclSpaces: ");
            org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityManager.print(fCurrentEntity);
            System.out.println(" -> false");
        }
        return false;

    } // skipDeclSpaces():boolean

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
        if (DEBUG_BUFFER) {
            System.out.print("(skipString, \""+s+"\": ");
            org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityManager.print(fCurrentEntity);
            System.out.println();
        }

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
                if (DEBUG_BUFFER) {
                    System.out.print(")skipString, \""+s+"\": ");
                    org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityManager.print(fCurrentEntity);
                    System.out.println(" -> false");
                }
                return false;
            }
            if (i < length - 1 && fCurrentEntity.position == fCurrentEntity.count) {
                System.arraycopy(fCurrentEntity.ch, fCurrentEntity.count - i - 1, fCurrentEntity.ch, 0, i + 1);
                // REVISIT: Can a string to be skipped cross an
                //          entity boundary? -Ac
                if (load(i + 1, false)) {
                    fCurrentEntity.startPosition -= i + 1; 
                    fCurrentEntity.position -= i + 1;
                    if (DEBUG_BUFFER) {
                        System.out.print(")skipString, \""+s+"\": ");
                        org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityManager.print(fCurrentEntity);
                        System.out.println(" -> false");
                    }
                    return false;
                }
            }
        }
        if (DEBUG_BUFFER) {
            System.out.print(")skipString, \""+s+"\": ");
            org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityManager.print(fCurrentEntity);
            System.out.println(" -> true");
        }
        fCurrentEntity.columnNumber += length;
        return true;

    } // skipString(String):boolean

    //
    // Locator methods
    //

    /**
     * Return the public identifier for the current document event.
     * <p>
     * The return value is the public identifier of the document
     * entity or of the external parsed entity in which the markup
     * triggering the event appears.
     *
     * @return A string containing the public identifier, or
     *         null if none is available.
     */
    public final String getPublicId() {
        return (fCurrentEntity != null && fCurrentEntity.entityLocation != null) ? fCurrentEntity.entityLocation.getPublicId() : null;
    } // getPublicId():String

    /**
     * Return the expanded system identifier for the current document event.
     * <p>
     * The return value is the expanded system identifier of the document
     * entity or of the external parsed entity in which the markup
     * triggering the event appears.
     * <p>
     * If the system identifier is a URL, the parser must resolve it
     * fully before passing it to the application.
     *
     * @return A string containing the expanded system identifier, or null
     *         if none is available.
     */
    public final String getExpandedSystemId() {
        if (fCurrentEntity != null) {
            if (fCurrentEntity.entityLocation != null &&
                    fCurrentEntity.entityLocation.getExpandedSystemId() != null ) {
                return fCurrentEntity.entityLocation.getExpandedSystemId();
            }
            else {
                // get the current entity to return something appropriate:
                return fCurrentEntity.getExpandedSystemId();
            }
        }
        return null;
    } // getExpandedSystemId():String

    /**
     * Return the literal system identifier for the current document event.
     * <p>
     * The return value is the literal system identifier of the document
     * entity or of the external parsed entity in which the markup
     * triggering the event appears.
     * <p>
     * @return A string containing the literal system identifier, or null
     *         if none is available.
     */
    public final String getLiteralSystemId() {
        if (fCurrentEntity != null) {
            if (fCurrentEntity.entityLocation != null &&
                    fCurrentEntity.entityLocation.getLiteralSystemId() != null ) {
                return fCurrentEntity.entityLocation.getLiteralSystemId();
            }
            else {
                // get the current entity to do it:
                return fCurrentEntity.getLiteralSystemId();
            }
        }
        return null;
    } // getLiteralSystemId():String

    /**
     * Returns the line number where the current document event ends.
     * <p>
     * <strong>Warning:</strong> The return value from the method
     * is intended only as an approximation for the sake of error
     * reporting; it is not intended to provide sufficient information
     * to edit the character content of the original XML document.
     * <p>
     * The return value is an approximation of the line number
     * in the document entity or external parsed entity where the
     * markup triggering the event appears.
     * <p>
     * If possible, the line position of the first character after the 
     * text associated with the document event should be provided.
     * The first line in the document is line 1.
     *
     * @return The line number, or -1 if none is available.
     */
    public final int getLineNumber() {
        if (fCurrentEntity != null) {
            if (fCurrentEntity.isExternal()) {
                return fCurrentEntity.lineNumber;
            }
            else {
                // ask the current entity to return something appropriate:
                return fCurrentEntity.getLineNumber();
            }
        }

        return -1;

    } // getLineNumber():int

    /**
     * Returns the column number where the current document event ends.
     * <p>
     * <strong>Warning:</strong> The return value from the method
     * is intended only as an approximation for the sake of error
     * reporting; it is not intended to provide sufficient information
     * to edit the character content of the original XML document.
     * <p>
     * The return value is an approximation of the column number
     * in the document entity or external parsed entity where the
     * markup triggering the event appears.
     * <p>
     * If possible, the line position of the first character after the 
     * text associated with the document event should be provided.
     * The first column in each line is column 1.
     *
     * @return The column number, or -1 if none is available.
     */
    public final int getColumnNumber() {
        if (fCurrentEntity != null) {
            if (fCurrentEntity.isExternal()) {
                return fCurrentEntity.columnNumber;
            }
            else {
                // ask current entity to find appropriate column number
                return fCurrentEntity.getColumnNumber();
            }
        }

        return -1;
    } // getColumnNumber():int
    
    /**
     * Returns the character offset where the current document event ends.
     * <p>
     * <strong>Warning:</strong> The return value from the method
     * is intended only as an approximation for the sake of error
     * reporting; it is not intended to provide sufficient information
     * to edit the character content of the original XML document.
     * <p>
     * The return value is an approximation of the character offset
     * in the document entity or external parsed entity where the
     * markup triggering the event appears.
     * <p>
     * If possible, the character offset of the first character after the 
     * text associated with the document event should be provided.
     *
     * @return The character offset, or -1 if none is available.
     */
    public final int getCharacterOffset() {
        if (fCurrentEntity != null) {
            if (fCurrentEntity.isExternal()) {
                return fCurrentEntity.baseCharOffset + (fCurrentEntity.position - fCurrentEntity.startPosition);
            }
            else {
                // ask current entity to find appropriate character offset
                return fCurrentEntity.getCharacterOffset();
            }
        }
        
        return -1;
    } // getCharacterOffset():int
    
    /** 
     * Returns the encoding of the current entity.  
     * Note that, for a given entity, this value can only be
     * considered final once the encoding declaration has been read (or once it
     * has been determined that there is no such declaration) since, no encoding
     * having been specified on the XMLInputSource, the parser
     * will make an initial "guess" which could be in error. 
     */
    public final String getEncoding() {
        if (fCurrentEntity != null) {
            if (fCurrentEntity.isExternal()) {
                return fCurrentEntity.encoding;
            }
            else {
                // ask current entity to find appropriate encoding
                return fCurrentEntity.getEncoding();
            }
        }
        return null;
    } // getEncoding():String
    
    /**
     * Returns the XML version of the current entity. This will normally be the
     * value from the XML or text declaration or defaulted by the parser. Note that
     * that this value may be different than the version of the processing rules 
     * applied to the current entity. For instance, an XML 1.1 document may refer to
     * XML 1.0 entities. In such a case the rules of XML 1.1 are applied to the entire 
     * document. Also note that, for a given entity, this value can only be considered
     * final once the XML or text declaration has been read or once it has been
     * determined that there is no such declaration.
     */
    public final String getXMLVersion() {
        if (fCurrentEntity != null) {
            if (fCurrentEntity.isExternal()) {
                return fCurrentEntity.xmlVersion;
            }
            else {
                // ask current entity to find the appropriate XML version
                return fCurrentEntity.getXMLVersion();
            }
        }
        return null;
    } // getXMLVersion():String
    
    // allow entity manager to tell us what the current entityis:
    public final void setCurrentEntity(org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityManager.ScannedEntity ent) {
        fCurrentEntity = ent;
    }

    // set buffer size:
    public final void setBufferSize(int size) {
        // REVISIT: Buffer size passed to entity scanner 
        // was not being kept in synch with the actual size
        // of the buffers in each scanned entity. If any
        // of the buffers were actually resized, it was possible
        // that the parser would throw an ArrayIndexOutOfBoundsException
        // for documents which contained names which are longer than
        // the current buffer size. Conceivably the buffer size passed
        // to entity scanner could be used to determine a minimum size
        // for resizing, if doubling its size is smaller than this
        // minimum. -- mrglavas 
        fBufferSize = size;
    }

    // reset what little state we have...
    public final void reset(SymbolTable symbolTable, org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityManager entityManager,
                        XMLErrorReporter reporter) {
        fCurrentEntity = null;
        fSymbolTable = symbolTable;
        fEntityManager = entityManager;
        fErrorReporter = reporter;
    }

    //
    // Private methods
    //

    /**
     * Loads a chunk of text.
     *
     * @param offset       The offset into the character buffer to
     *                     read the next batch of characters.
     * @param changeEntity True if the load should change entities
     *                     at the end of the entity, otherwise leave
     *                     the current entity in place and the entity
     *                     boundary will be signaled by the return
     *                     value.
     *
     * @returns Returns true if the entity changed as a result of this
     *          load operation.
     */
    final boolean load(int offset, boolean changeEntity)
        throws IOException {
        if (DEBUG_BUFFER) {
            System.out.print("(load, "+offset+": ");
            org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityManager.print(fCurrentEntity);
            System.out.println();
        }

        fCurrentEntity.baseCharOffset += (fCurrentEntity.position - fCurrentEntity.startPosition);
        // read characters
        int length = fCurrentEntity.ch.length - offset;
        if (!fCurrentEntity.mayReadChunks && length > org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityManager.DEFAULT_XMLDECL_BUFFER_SIZE) {
            length = org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityManager.DEFAULT_XMLDECL_BUFFER_SIZE;
        }
        if (DEBUG_BUFFER) System.out.println("  length to try to read: "+length);
        int count = fCurrentEntity.reader.read(fCurrentEntity.ch, offset, length);
        if (DEBUG_BUFFER) System.out.println("  length actually read:  "+count);

        // reset count and position
        boolean entityChanged = false;
        if (count != -1) {
            if (count != 0) {
                fCurrentEntity.count = count + offset;
                fCurrentEntity.position = offset;
                fCurrentEntity.startPosition = offset;
            }
        }

        // end of this entity
        else {
            fCurrentEntity.count = offset;
            fCurrentEntity.position = offset;
            fCurrentEntity.startPosition = offset;
            entityChanged = true;
            if (changeEntity) {
                fEntityManager.endEntity();
                if (fCurrentEntity == null) {
                    throw END_OF_DOCUMENT_ENTITY;
                }
                // handle the trailing edges
                if (fCurrentEntity.position == fCurrentEntity.count) {
                    load(0, true);
                }
            }
        }
        if (DEBUG_BUFFER) {
            System.out.print(")load, "+offset+": ");
            XMLEntityManager.print(fCurrentEntity);
            System.out.println();
        }

        return entityChanged;

    } // load(int, boolean):boolean
    
    /**
     * This method is invoked to increase the size of the current entity's
     * buffer if an XML name being scanned is too large to fit within
     * the buffer at its current size. 
     */
    final void resizeBuffer(int offset, int length) {
        char[] tmp = new char[fCurrentEntity.ch.length << 1];
        System.arraycopy(fCurrentEntity.ch, offset,
                         tmp, 0, length);
        fCurrentEntity.ch = tmp;
    } // resizeBuffer(int, int)

} // class XMLEntityScanner

