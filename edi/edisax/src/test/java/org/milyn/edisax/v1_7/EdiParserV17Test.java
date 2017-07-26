package org.milyn.edisax.v1_7;

import org.junit.Test;
import org.milyn.edisax.AbstractEDIParserTestCase;

import java.io.IOException;

public class EdiParserV17Test extends AbstractEDIParserTestCase {

    @Test
    public void fieldMaxOccurs() throws IOException {
        test("field_maxoccurs");
    }

    @Test
    public void compositeMaxOccurs() throws IOException {
        test("composite_maxoccurs");
    }

    @Test
    public void componentMaxOccurs() throws IOException {
        test("component_maxoccurs");
    }

    @Test
    public void mon_b_and_t() throws IOException {
        test("mon_b_and_t");
    }
}
