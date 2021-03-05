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

package org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs;

import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.SchemaDVFactory;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.XSFacets;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.XSSimpleType;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.XSDeclarationPool;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.util.SymbolHash;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xs.XSConstants;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xs.XSObjectList;

/**
 * the base factory to create/return built-in schema DVs and create user-defined DVs
 * 
 * @xerces.internal 
 *
 * @author Neeraj Bajaj, Sun Microsystems, inc.
 * @author Sandy Gao, IBM
 * @author Khaled Noaman, IBM 
 *
 * @version $Id$
 */
public abstract class BaseSchemaDVFactory extends SchemaDVFactory {

    static final String URI_SCHEMAFORSCHEMA = "http://www.w3.org/2001/XMLSchema";

    protected XSDeclarationPool fDeclPool = null;

    // create common built-in types
    protected static void createBuiltInTypes(SymbolHash builtInTypes, org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl baseAtomicType) {
        // all schema simple type names
        final String ANYSIMPLETYPE     = "anySimpleType";
        final String ANYURI            = "anyURI";
        final String BASE64BINARY      = "base64Binary";
        final String BOOLEAN           = "boolean";
        final String BYTE              = "byte";
        final String DATE              = "date";
        final String DATETIME          = "dateTime";
        final String DAY               = "gDay";
        final String DECIMAL           = "decimal";
        final String DOUBLE            = "double";
        final String DURATION          = "duration";
        final String ENTITY            = "ENTITY";
        final String ENTITIES          = "ENTITIES";
        final String FLOAT             = "float";
        final String HEXBINARY         = "hexBinary";
        final String ID                = "ID";
        final String IDREF             = "IDREF";
        final String IDREFS            = "IDREFS";
        final String INT               = "int";
        final String INTEGER           = "integer";
        final String LONG              = "long";
        final String NAME              = "Name";
        final String NEGATIVEINTEGER   = "negativeInteger";
        final String MONTH             = "gMonth";
        final String MONTHDAY          = "gMonthDay";
        final String NCNAME            = "NCName";
        final String NMTOKEN           = "NMTOKEN";
        final String NMTOKENS          = "NMTOKENS";
        final String LANGUAGE          = "language";
        final String NONNEGATIVEINTEGER= "nonNegativeInteger";
        final String NONPOSITIVEINTEGER= "nonPositiveInteger";
        final String NORMALIZEDSTRING  = "normalizedString";
        final String NOTATION          = "NOTATION";
        final String POSITIVEINTEGER   = "positiveInteger";
        final String QNAME             = "QName";
        final String SHORT             = "short";
        final String STRING            = "string";
        final String TIME              = "time";
        final String TOKEN             = "token";
        final String UNSIGNEDBYTE      = "unsignedByte";
        final String UNSIGNEDINT       = "unsignedInt";
        final String UNSIGNEDLONG      = "unsignedLong";
        final String UNSIGNEDSHORT     = "unsignedShort";
        final String YEAR              = "gYear";
        final String YEARMONTH         = "gYearMonth";

        final XSFacets facets = new XSFacets();
        
        builtInTypes.put(ANYSIMPLETYPE, org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl.fAnySimpleType);

        org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl stringDV = new org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl(baseAtomicType, STRING, org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl.DV_STRING, XSSimpleType.ORDERED_FALSE, false, false, false , true, XSConstants.STRING_DT);
        builtInTypes.put(STRING, stringDV);
        builtInTypes.put(BOOLEAN, new org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl(baseAtomicType, BOOLEAN, org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl.DV_BOOLEAN, XSSimpleType.ORDERED_FALSE, false, true, false, true, XSConstants.BOOLEAN_DT));
        org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl decimalDV = new org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl(baseAtomicType, DECIMAL, org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl.DV_DECIMAL, XSSimpleType.ORDERED_TOTAL, false, false, true, true, XSConstants.DECIMAL_DT);
        builtInTypes.put(DECIMAL, decimalDV);

        builtInTypes.put(ANYURI, new org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl(baseAtomicType, ANYURI, org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl.DV_ANYURI, XSSimpleType.ORDERED_FALSE, false, false, false, true, XSConstants.ANYURI_DT));
        builtInTypes.put(BASE64BINARY, new org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl(baseAtomicType, BASE64BINARY, org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl.DV_BASE64BINARY, XSSimpleType.ORDERED_FALSE, false, false, false, true, XSConstants.BASE64BINARY_DT));

        org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl durationDV = new org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl(baseAtomicType, DURATION, org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl.DV_DURATION, XSSimpleType.ORDERED_PARTIAL, false, false, false, true, XSConstants.DURATION_DT);
        builtInTypes.put(DURATION, durationDV);

        builtInTypes.put(DATETIME, new org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl(baseAtomicType, DATETIME, org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl.DV_DATETIME, XSSimpleType.ORDERED_PARTIAL, false, false, false, true, XSConstants.DATETIME_DT));
        builtInTypes.put(TIME, new org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl(baseAtomicType, TIME, org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl.DV_TIME, XSSimpleType.ORDERED_PARTIAL, false, false, false, true, XSConstants.TIME_DT));
        builtInTypes.put(DATE, new org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl(baseAtomicType, DATE, org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl.DV_DATE, XSSimpleType.ORDERED_PARTIAL, false, false, false, true, XSConstants.DATE_DT));
        builtInTypes.put(YEARMONTH, new org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl(baseAtomicType, YEARMONTH, org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl.DV_GYEARMONTH, XSSimpleType.ORDERED_PARTIAL, false, false, false, true, XSConstants.GYEARMONTH_DT));
        builtInTypes.put(YEAR, new org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl(baseAtomicType, YEAR, org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl.DV_GYEAR, XSSimpleType.ORDERED_PARTIAL, false, false, false, true, XSConstants.GYEAR_DT));
        builtInTypes.put(MONTHDAY, new org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl(baseAtomicType, MONTHDAY, org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl.DV_GMONTHDAY, XSSimpleType.ORDERED_PARTIAL, false, false, false, true, XSConstants.GMONTHDAY_DT));
        builtInTypes.put(DAY, new org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl(baseAtomicType, DAY, org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl.DV_GDAY, XSSimpleType.ORDERED_PARTIAL, false, false, false, true, XSConstants.GDAY_DT));
        builtInTypes.put(MONTH, new org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl(baseAtomicType, MONTH, org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl.DV_GMONTH, XSSimpleType.ORDERED_PARTIAL, false, false, false, true, XSConstants.GMONTH_DT));

        org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl integerDV = new org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl(decimalDV, INTEGER, org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl.DV_INTEGER, XSSimpleType.ORDERED_TOTAL, false, false, true, true, XSConstants.INTEGER_DT);
        builtInTypes.put(INTEGER, integerDV);

        facets.maxInclusive = "0";
        org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl nonPositiveDV = new org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl(integerDV, NONPOSITIVEINTEGER, URI_SCHEMAFORSCHEMA, (short)0, false, null, XSConstants.NONPOSITIVEINTEGER_DT);
        nonPositiveDV.applyFacets1(facets , XSSimpleType.FACET_MAXINCLUSIVE, (short)0);
        builtInTypes.put(NONPOSITIVEINTEGER, nonPositiveDV);

        facets.maxInclusive = "-1";
        org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl negativeDV = new org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl(nonPositiveDV, NEGATIVEINTEGER, URI_SCHEMAFORSCHEMA, (short)0, false, null, XSConstants.NEGATIVEINTEGER_DT);
        negativeDV.applyFacets1(facets , XSSimpleType.FACET_MAXINCLUSIVE, (short)0);
        builtInTypes.put(NEGATIVEINTEGER, negativeDV);

        facets.maxInclusive = "9223372036854775807";
        facets.minInclusive = "-9223372036854775808";
        org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl longDV = new org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl(integerDV, LONG, URI_SCHEMAFORSCHEMA, (short)0, false, null, XSConstants.LONG_DT);
        longDV.applyFacets1(facets , (short)(XSSimpleType.FACET_MAXINCLUSIVE | XSSimpleType.FACET_MININCLUSIVE), (short)0 );
        builtInTypes.put(LONG, longDV);

        facets.maxInclusive = "2147483647";
        facets.minInclusive =  "-2147483648";
        org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl intDV = new org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl(longDV, INT, URI_SCHEMAFORSCHEMA, (short)0, false, null, XSConstants.INT_DT);
        intDV.applyFacets1(facets, (short)(XSSimpleType.FACET_MAXINCLUSIVE | XSSimpleType.FACET_MININCLUSIVE), (short)0 );
        builtInTypes.put(INT, intDV);

        facets.maxInclusive = "32767";
        facets.minInclusive = "-32768";
        org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl shortDV = new org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl(intDV, SHORT , URI_SCHEMAFORSCHEMA, (short)0, false, null, XSConstants.SHORT_DT);
        shortDV.applyFacets1(facets, (short)(XSSimpleType.FACET_MAXINCLUSIVE | XSSimpleType.FACET_MININCLUSIVE), (short)0 );
        builtInTypes.put(SHORT, shortDV);

        facets.maxInclusive = "127";
        facets.minInclusive = "-128";
        org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl byteDV = new org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl(shortDV, BYTE , URI_SCHEMAFORSCHEMA, (short)0, false, null, XSConstants.BYTE_DT);
        byteDV.applyFacets1(facets, (short)(XSSimpleType.FACET_MAXINCLUSIVE | XSSimpleType.FACET_MININCLUSIVE), (short)0 );
        builtInTypes.put(BYTE, byteDV);

        facets.minInclusive =  "0" ;
        org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl nonNegativeDV = new org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl(integerDV, NONNEGATIVEINTEGER , URI_SCHEMAFORSCHEMA, (short)0, false, null, XSConstants.NONNEGATIVEINTEGER_DT);
        nonNegativeDV.applyFacets1(facets, XSSimpleType.FACET_MININCLUSIVE, (short)0 );
        builtInTypes.put(NONNEGATIVEINTEGER, nonNegativeDV);

        facets.maxInclusive = "18446744073709551615" ;
        org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl unsignedLongDV = new org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl(nonNegativeDV, UNSIGNEDLONG , URI_SCHEMAFORSCHEMA, (short)0, false, null, XSConstants.UNSIGNEDLONG_DT);
        unsignedLongDV.applyFacets1(facets, XSSimpleType.FACET_MAXINCLUSIVE, (short)0 );
        builtInTypes.put(UNSIGNEDLONG, unsignedLongDV);

        facets.maxInclusive = "4294967295" ;
        org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl unsignedIntDV = new org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl(unsignedLongDV, UNSIGNEDINT , URI_SCHEMAFORSCHEMA, (short)0, false, null, XSConstants.UNSIGNEDINT_DT);
        unsignedIntDV.applyFacets1(facets, XSSimpleType.FACET_MAXINCLUSIVE, (short)0 );
        builtInTypes.put(UNSIGNEDINT, unsignedIntDV);

        facets.maxInclusive = "65535" ;
        org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl unsignedShortDV = new org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl(unsignedIntDV, UNSIGNEDSHORT , URI_SCHEMAFORSCHEMA, (short)0, false, null, XSConstants.UNSIGNEDSHORT_DT);
        unsignedShortDV.applyFacets1(facets, XSSimpleType.FACET_MAXINCLUSIVE, (short)0 );
        builtInTypes.put(UNSIGNEDSHORT, unsignedShortDV);

        facets.maxInclusive = "255" ;
        org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl unsignedByteDV = new org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl(unsignedShortDV, UNSIGNEDBYTE , URI_SCHEMAFORSCHEMA, (short)0, false, null, XSConstants.UNSIGNEDBYTE_DT);
        unsignedByteDV.applyFacets1(facets, XSSimpleType.FACET_MAXINCLUSIVE, (short)0 );
        builtInTypes.put(UNSIGNEDBYTE, unsignedByteDV);

        facets.minInclusive = "1" ;
        org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl positiveIntegerDV = new org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl(nonNegativeDV, POSITIVEINTEGER , URI_SCHEMAFORSCHEMA, (short)0, false, null, XSConstants.POSITIVEINTEGER_DT);
        positiveIntegerDV.applyFacets1(facets, XSSimpleType.FACET_MININCLUSIVE, (short)0 );
        builtInTypes.put(POSITIVEINTEGER, positiveIntegerDV);

        builtInTypes.put(FLOAT, new org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl(baseAtomicType, FLOAT, org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl.DV_FLOAT, XSSimpleType.ORDERED_PARTIAL, true, true, true, true, XSConstants.FLOAT_DT));
        builtInTypes.put(DOUBLE, new org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl(baseAtomicType, DOUBLE, org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl.DV_DOUBLE, XSSimpleType.ORDERED_PARTIAL, true, true, true, true, XSConstants.DOUBLE_DT));
        builtInTypes.put(HEXBINARY, new org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl(baseAtomicType, HEXBINARY, org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl.DV_HEXBINARY, XSSimpleType.ORDERED_FALSE, false, false, false, true, XSConstants.HEXBINARY_DT));
        builtInTypes.put(NOTATION, new org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl(baseAtomicType, NOTATION, org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl.DV_NOTATION, XSSimpleType.ORDERED_FALSE, false, false, false, true, XSConstants.NOTATION_DT));

        facets.whiteSpace =  XSSimpleType.WS_REPLACE;
        org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl normalizedDV = new org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl(stringDV, NORMALIZEDSTRING , URI_SCHEMAFORSCHEMA, (short)0, false, null, XSConstants.NORMALIZEDSTRING_DT);
        normalizedDV.applyFacets1(facets, XSSimpleType.FACET_WHITESPACE, (short)0 );
        builtInTypes.put(NORMALIZEDSTRING, normalizedDV);

        facets.whiteSpace = XSSimpleType.WS_COLLAPSE;
        org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl tokenDV = new org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl(normalizedDV, TOKEN , URI_SCHEMAFORSCHEMA, (short)0, false, null, XSConstants.TOKEN_DT);
        tokenDV.applyFacets1(facets, XSSimpleType.FACET_WHITESPACE, (short)0 );
        builtInTypes.put(TOKEN, tokenDV);

        facets.whiteSpace = XSSimpleType.WS_COLLAPSE;
        facets.pattern  = "([a-zA-Z]{1,8})(-[a-zA-Z0-9]{1,8})*";
        org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl languageDV = new org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl(tokenDV, LANGUAGE , URI_SCHEMAFORSCHEMA, (short)0, false, null, XSConstants.LANGUAGE_DT);
        languageDV.applyFacets1(facets, (short)(XSSimpleType.FACET_WHITESPACE | XSSimpleType.FACET_PATTERN) ,(short)0);
        builtInTypes.put(LANGUAGE, languageDV);

        facets.whiteSpace =  XSSimpleType.WS_COLLAPSE;
        org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl nameDV = new org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl(tokenDV, NAME , URI_SCHEMAFORSCHEMA, (short)0, false, null, XSConstants.NAME_DT);
        nameDV.applyFacets1(facets, XSSimpleType.FACET_WHITESPACE, (short)0, org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl.SPECIAL_PATTERN_NAME);
        builtInTypes.put(NAME, nameDV);

        facets.whiteSpace = XSSimpleType.WS_COLLAPSE;
        org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl ncnameDV = new org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl(nameDV, NCNAME , URI_SCHEMAFORSCHEMA, (short)0, false, null, XSConstants.NCNAME_DT) ;
        ncnameDV.applyFacets1(facets, XSSimpleType.FACET_WHITESPACE, (short)0, org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl.SPECIAL_PATTERN_NCNAME);
        builtInTypes.put(NCNAME, ncnameDV);

        builtInTypes.put(QNAME, new org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl(baseAtomicType, QNAME, org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl.DV_QNAME, XSSimpleType.ORDERED_FALSE, false, false, false, true, XSConstants.QNAME_DT));

        builtInTypes.put(ID, new org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl(ncnameDV,  ID, org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl.DV_ID, XSSimpleType.ORDERED_FALSE, false, false, false , true, XSConstants.ID_DT));
        org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl idrefDV = new org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl(ncnameDV,  IDREF , org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl.DV_IDREF, XSSimpleType.ORDERED_FALSE, false, false, false, true, XSConstants.IDREF_DT);
        builtInTypes.put(IDREF, idrefDV);

        facets.minLength = 1;
        org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl tempDV = new org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl(null, URI_SCHEMAFORSCHEMA, (short)0, idrefDV, true, null);
        org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl idrefsDV = new org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl(tempDV, IDREFS, URI_SCHEMAFORSCHEMA, (short)0, false, null);
        idrefsDV.applyFacets1(facets, XSSimpleType.FACET_MINLENGTH, (short)0);
        builtInTypes.put(IDREFS, idrefsDV);

        org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl entityDV = new org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl(ncnameDV, ENTITY , org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl.DV_ENTITY, XSSimpleType.ORDERED_FALSE, false, false, false, true, XSConstants.ENTITY_DT);
        builtInTypes.put(ENTITY, entityDV);

        facets.minLength = 1;
        tempDV = new org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl(null, URI_SCHEMAFORSCHEMA, (short)0, entityDV, true, null);
        org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl entitiesDV = new org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl(tempDV, ENTITIES, URI_SCHEMAFORSCHEMA, (short)0, false, null);
        entitiesDV.applyFacets1(facets, XSSimpleType.FACET_MINLENGTH, (short)0);
        builtInTypes.put(ENTITIES, entitiesDV);

        facets.whiteSpace  = XSSimpleType.WS_COLLAPSE;
        org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl nmtokenDV = new org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl(tokenDV, NMTOKEN, URI_SCHEMAFORSCHEMA, (short)0, false, null, XSConstants.NMTOKEN_DT);
        nmtokenDV.applyFacets1(facets, XSSimpleType.FACET_WHITESPACE, (short)0, org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl.SPECIAL_PATTERN_NMTOKEN);
        builtInTypes.put(NMTOKEN, nmtokenDV);

        facets.minLength = 1;
        tempDV = new org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl(null, URI_SCHEMAFORSCHEMA, (short)0, nmtokenDV, true, null);
        org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl nmtokensDV = new org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl(tempDV, NMTOKENS, URI_SCHEMAFORSCHEMA, (short)0, false, null);
        nmtokensDV.applyFacets1(facets, XSSimpleType.FACET_MINLENGTH, (short)0);
        builtInTypes.put(NMTOKENS, nmtokensDV);
    } //createBuiltInTypes()

    /**
     * Create a new simple type which is derived by restriction from another
     * simple type.
     *
     * @param name              name of the new type, could be null
     * @param targetNamespace   target namespace of the new type, could be null
     * @param finalSet          value of "final"
     * @param base              base type of the new type
     * @param annotations       set of annotations
     * @return                  the newly created simple type
     */
    public XSSimpleType createTypeRestriction(String name, String targetNamespace,
                                              short finalSet, XSSimpleType base, XSObjectList annotations) {
        
        if (fDeclPool != null) {
           org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl st= fDeclPool.getSimpleTypeDecl();
           return st.setRestrictionValues((org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl)base, name, targetNamespace, finalSet, annotations);
        }
        return new org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl((org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl)base, name, targetNamespace, finalSet, false, annotations);
    }

    /**
     * Create a new simple type which is derived by list from another simple
     * type.
     *
     * @param name              name of the new type, could be null
     * @param targetNamespace   target namespace of the new type, could be null
     * @param finalSet          value of "final"
     * @param itemType          item type of the list type
     * @param annotations       set of annotations
     * @return                  the newly created simple type
     */
    public XSSimpleType createTypeList(String name, String targetNamespace,
                                       short finalSet, XSSimpleType itemType,
                                       XSObjectList annotations) {
        if (fDeclPool != null) {
           org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl st= fDeclPool.getSimpleTypeDecl();
           return st.setListValues(name, targetNamespace, finalSet, (org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl)itemType, annotations);
        }
        return new org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl(name, targetNamespace, finalSet, (org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl)itemType, false, annotations);
    }

    /**
     * Create a new simple type which is derived by union from a list of other
     * simple types.
     *
     * @param name              name of the new type, could be null
     * @param targetNamespace   target namespace of the new type, could be null
     * @param finalSet          value of "final"
     * @param memberTypes       member types of the union type
     * @param annotations       set of annotations
     * @return                  the newly created simple type
     */
    public XSSimpleType createTypeUnion(String name, String targetNamespace,
                                        short finalSet, XSSimpleType[] memberTypes,
                                        XSObjectList annotations) {
        int typeNum = memberTypes.length;
        org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl[] mtypes = new org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl[typeNum];
        System.arraycopy(memberTypes, 0, mtypes, 0, typeNum);

        if (fDeclPool != null) {
           org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl st= fDeclPool.getSimpleTypeDecl();
           return st.setUnionValues(name, targetNamespace, finalSet, mtypes, annotations);
        }
        return new org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl(name, targetNamespace, finalSet, mtypes, annotations);
    }

    public void setDeclPool (XSDeclarationPool declPool){
        fDeclPool = declPool;
    }

    /** Implementation internal **/
    public org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl newXSSimpleTypeDecl() {
        return new XSSimpleTypeDecl();
    }
} //BaseSchemaDVFactory
