package org.smooks.edisax.v1_2.model;

import org.smooks.edisax.AbstractEDIParserTestCase;

import java.io.IOException;

public class EDIParserTest extends AbstractEDIParserTestCase {

    public void testWrongType() throws IOException {
        test("type");
    }

    public void testMinLength() throws IOException {
        test("minlength");
    }

    public void testMaxLength() throws IOException {
        test("maxlength");
    }
}
