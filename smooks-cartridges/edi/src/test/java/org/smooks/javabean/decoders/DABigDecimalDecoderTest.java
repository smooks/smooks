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
package org.smooks.javabean.decoders;

import org.junit.Test;
import static org.junit.Assert.*;
import org.smooks.edisax.model.internal.Delimiters;
import org.smooks.edisax.unedifact.UNEdifactInterchangeParser;

import java.math.BigDecimal;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class DABigDecimalDecoderTest {

    private static Delimiters DOT_DEC_DELIMITERS =   UNEdifactInterchangeParser.defaultUNEdifactDelimiters;
    private static Delimiters COMMA_DEC_DELIMITERS = ((Delimiters)UNEdifactInterchangeParser.defaultUNEdifactDelimiters.clone()).setDecimalSeparator(",");

    @Test
    public void test_decode_decimal_point_dot() {
        DABigDecimalDecoder decoder = new DotDABigDecimalDecoder();
        assertEquals(BigDecimal.valueOf(11, 1), decoder.decode("1.1"));
    }

    @Test
    public void test_decode_decimal_point_comma() {
        DABigDecimalDecoder decoder = new CommaDABigDecimalDecoder();
        assertEquals(BigDecimal.valueOf(11, 1), decoder.decode("1,1"));
    }

    @Test
    public void test_encode_decimal_point_dot() {
        DABigDecimalDecoder decoder = new DABigDecimalDecoder();
        assertEquals("1.1", decoder.encode(BigDecimal.valueOf(11, 1), DOT_DEC_DELIMITERS));
    }

    @Test
    public void test_encode_decimal_point_comma() {
        DABigDecimalDecoder decoder = new DABigDecimalDecoder();
        assertEquals("1,1", decoder.encode(BigDecimal.valueOf(11, 1), COMMA_DEC_DELIMITERS));
    }

    class DotDABigDecimalDecoder extends DABigDecimalDecoder {
        @Override
        protected Delimiters getContextDelimiters() {
            return DOT_DEC_DELIMITERS;
        }
    }
    class CommaDABigDecimalDecoder extends DABigDecimalDecoder {
        @Override
        protected Delimiters getContextDelimiters() {
            return COMMA_DEC_DELIMITERS;
        }
    }
}
