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

package org.milyn.edisax.v1_2.validation;

import junit.framework.TestCase;

/**
 * Tests validation of type in ValueNode.
 * @author bardl 
 */
public class TypeValidatorTest extends TestCase {

    public void test() {

    }

//    public void test_type_String_valid() throws IOException {
//        EDITypeEnum ediType = EDITypeEnum.String;
//
//        String value = "testing123";
//        assertTrue("The value [" + value + "] should be a valid String.", ediType.validateType(value, null));
//    }
//
//    public void test_type_String_invalid() throws IOException {
//        //Can't think of any invalid cases.
//    }
//
//    public void test_type_Numeric_valid() throws IOException {
//        EDITypeEnum ediType = EDITypeEnum.Numeric;
//
//        String value = "123";
//        assertTrue("The value [" + value + "] should be a valid Numeric.", ediType.validateType(value, buildParameterList(new ParamEntry<String,String>("fomat", "#0.00"))));
//
//        value = "123.00";
//        assertTrue("The value [" + value + "] should be a valid Numeric.", ediType.validateType(value, buildParameterList(new ParamEntry<String,String>("fomat", "#0.00"))));
//    }
//
//    public void test_type_Numeric_invalid() throws IOException {
//        EDITypeEnum ediType = EDITypeEnum.Numeric;
//
//        String value = "12A3";
//        assertFalse("The value [" + value + "] should not be a valid Numeric.", ediType.validateType(value, buildParameterList(new ParamEntry<String,String>("fomat", "#0.00"))));
//    }
//
//    public void test_type_Decimal_valid() throws IOException {
//        EDITypeEnum ediType = EDITypeEnum.Decimal;
//
//        String value = "123";
//        assertTrue("The value [" + value + "] should be a valid Numeric.", ediType.validateType(value, buildParameterList(new ParamEntry<String,String>("fomat", "#0.00"))));
//
//        value = "123.00";
//        assertTrue("The value [" + value + "] should be a valid Numeric.", ediType.validateType(value, buildParameterList(new ParamEntry<String,String>("format", "#0.00"))));
//    }
//
//    public void test_type_Decimal_invalid() throws IOException {
//        EDITypeEnum ediType = EDITypeEnum.Date;
//
//        String value = "12A3";
//        assertFalse("The value [" + value + "] should not be a valid Numeric.", ediType.validateType(value, buildParameterList(new ParamEntry<String,String>("format", "#0.00"))));
//    }
//
//    public void test_type_Date_valid() throws IOException {
//        EDITypeEnum ediType = EDITypeEnum.Date;
//
//        String value = "20090401";
//        String format = "yyyyMMdd";
//        assertTrue("The value [" + value + "] with format [" + format + "] should be a valid Date.", ediType.validateType(value, buildParameterList(new ParamEntry<String,String>("format", format))));
//    }
//
//    public void test_type_Date_invalid() throws IOException {
//        EDITypeEnum ediType = EDITypeEnum.Date;
//
//        String value = "200908bb";
//        String format = "yyyyMMdd";
//        assertFalse("The value [" + value + "] with format [" + format + "] should not be a valid Date.", ediType.validateType(value, buildParameterList(new ParamEntry<String,String>("format", format))));
//    }
//
//    public void test_type_Time_valid() throws IOException {
//        EDITypeEnum ediType = EDITypeEnum.Time;
//
//        String value = "2251";
//        String format = "HHmm";
//        assertTrue("The value [" + value + "] with format [" + format + "] should be a valid Time.", ediType.validateType(value, buildParameterList(new ParamEntry<String,String>("format", format))));
//    }
//
//    public void test_type_Time_invalid() throws IOException {
//        EDITypeEnum ediType = EDITypeEnum.Time;
//
//        String value = "22s";
//        String format = "HHmm";
//        assertFalse("The value [" + value + "] with format [" + format + "] should not be a valid Time.", ediType.validateType(value, buildParameterList(new ParamEntry<String,String>("format", format))));
//    }
//
//    public void test_type_Binary_valid() throws IOException {
//        EDITypeEnum ediType = EDITypeEnum.Binary;
//
//        String value = "0101010101111000";
//        assertTrue("The value [" + value + "] should be a valid binary sequence.", ediType.validateType(value, null));
//    }
//
//    public void test_type_Binary_invalid() throws IOException {
//        EDITypeEnum ediType = EDITypeEnum.Binary;
//
//        String value = "0101001200";
//        assertFalse("The value [" + value + "] should not be a valid binary sequence.", ediType.validateType(value, null));
//    }
//
//    private List<Map.Entry<String, String>> buildParameterList(Map.Entry<String, String>... entries) {
//        List<Map.Entry<String, String>> list = new ArrayList<Map.Entry<String, String>>();
//        for (Map.Entry<String, String> entry : entries) {
//            list.add(entry);
//        }
//        return list;
//    }
}
