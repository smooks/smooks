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

import java.math.BigInteger;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;

import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.InvalidDatatypeValueException;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.ValidationContext;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.DurationDV;

/**
 * Used to validate the <yearMonthDuration> type
 *
 * @xerces.internal  
 * 
 * @author Ankit Pasricha, IBM
 *  
 * @version $Id$
 */
class YearMonthDurationDV extends org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.DurationDV {
    
    public Object getActualValue(String content, ValidationContext context)
        throws InvalidDatatypeValueException {
        try {
            return parse(content, DurationDV.YEARMONTHDURATION_TYPE);
        } 
        catch (Exception ex) {
            throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.1", new Object[]{content, "yearMonthDuration"});
        }
    }
    
    protected Duration getDuration(DateTimeData date) {
        int sign = 1;
        if ( date.year<0 || date.month<0) {
            sign = -1;
        }
        return datatypeFactory.newDuration(sign == 1, 
                date.year != DatatypeConstants.FIELD_UNDEFINED?BigInteger.valueOf(sign*date.year):null, 
                date.month != DatatypeConstants.FIELD_UNDEFINED?BigInteger.valueOf(sign*date.month):null, 
                null,
                null,
                null,
                null);
    }
}
