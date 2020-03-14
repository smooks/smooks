package org.smooks.edisax.test_empty_rows;

import org.smooks.edisax.AbstractEDIParserTestCase;

import java.io.IOException;

/**
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class EDIParserTest extends AbstractEDIParserTestCase {

    public void test_extra_segment() throws IOException {
        test("test_extra_segment");
    }

    public void test_newlines_and_segment() throws IOException {
        test("test_newlines_and_segment");
    }

    public void test_newlines_whitespaces() throws IOException {
        test("test_newlines_whitespaces");
    }
}
