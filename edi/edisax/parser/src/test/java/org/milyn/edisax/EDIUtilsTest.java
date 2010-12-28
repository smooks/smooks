package org.milyn.edisax;

import junit.framework.TestCase;

import java.io.IOException;

import org.milyn.edisax.model.internal.DelimiterType;
import org.milyn.edisax.model.internal.Delimiters;
import org.milyn.edisax.unedifact.UNEdifactInterchangeParser;
import org.milyn.edisax.util.EDIUtils;
import org.milyn.util.CollectionsUtil;
import org.xml.sax.SAXException;

/**
 * @author bardl
 */
public class EDIUtilsTest extends TestCase {

	public void test_with_escape() throws IOException, SAXException {
        String[] test = EDIUtils.split("first?::second??:third", ":", "?");
        String[] expected = new String[]{"first:", "second??", "third"};
        assertTrue("Result is [" + output(test) + "] should be [" + output(expected) + "] ", equal(test, expected));

        test = EDIUtils.split("ATS+hep:iee+hai??+kai=haikai+slut", "+", "?");
        expected = new String[]{"ATS", "hep:iee", "hai??", "kai=haikai", "slut"};
        assertTrue("Result is [" + output(test) + "] should be [" + output(expected) + "] ", equal(test, expected));

        test = EDIUtils.split("ATS+hep:iee+hai?#?#+kai=haikai+slut", "+", "?#");
        expected = new String[]{"ATS", "hep:iee", "hai?#?#", "kai=haikai", "slut"};
        assertTrue("Result is [" + output(test) + "] should be [" + output(expected) + "] ", equal(test, expected));

        test = EDIUtils.split("ATS+#hep:iee+#hai?#?#+#kai=haikai+#slut", "+#", "?#");
        expected = new String[]{"ATS", "hep:iee", "hai?#?#", "kai=haikai", "slut"};
        assertTrue("Result is [" + output(test) + "] should be [" + output(expected) + "] ", equal(test, expected));

        test = EDIUtils.split("ATS+#hep:iee+#hai??+#kai=haikai+#slut", "+#", "?");
        expected = new String[]{"ATS", "hep:iee", "hai??", "kai=haikai", "slut"};
        assertTrue("Result is [" + output(test) + "] should be [" + output(expected) + "] ", equal(test, expected));

        test = EDIUtils.split("ATS+#hep:iee+#hai??+#kai=haikai+#slut", "+#", null);
        expected = new String[]{"ATS", "hep:iee", "hai??", "kai=haikai", "slut"};
        assertTrue("Result is [" + output(test) + "] should be [" + output(expected) + "] ", equal(test, expected));

        // Test restarting escape sequence within escape sequence.
        test = EDIUtils.split("ATS+hep:iee+hai??#+kai=haikai+slut", "+", "?#");
        expected = new String[]{"ATS", "hep:iee", "hai?+kai=haikai", "slut"};
        assertTrue("Result is [" + output(test) + "] should be [" + output(expected) + "] ", equal(test, expected));

        // Test restarting delimiter sequence within delimiter sequence.
        test = EDIUtils.split("ATS++#hep:iee+#hai?+#kai=haikai+#slut", "+#", "?");
        expected = new String[]{"ATS+", "hep:iee", "hai+#kai=haikai", "slut"};
        assertTrue("Result is [" + output(test) + "] should be [" + output(expected) + "] ", equal(test, expected));

    }

	public void test_without_escape() {
        String[] result = EDIUtils.split(null, "*", null);        
        assertTrue("Result is [" + output(result) + "] should be [null] ", result == null);

        result = EDIUtils.split("", null, null);
        String[] expected = new String[0];
        assertTrue("Result is [" + output(result) + "] should be [" + output(expected) + "] ", equal(result, expected));

        result = EDIUtils.split("abc def", null, null);
        expected = new String[]{"abc", "def"};
        assertTrue("Result is [" + output(result) + "] should be [" + output(expected) + "] ", equal(result, expected));

        result = EDIUtils.split("abc def", " ", null);
        expected = new String[]{"abc", "def"};
        assertTrue("Result is [" + output(result) + "] should be [" + output(expected) + "] ", equal(result, expected));

        result = EDIUtils.split("abc  def", " ", null);
        expected = new String[]{"abc", "", "def"};
        assertTrue("Result is [" + output(result) + "] should be [" + output(expected) + "] ", equal(result, expected));

        result = EDIUtils.split("ab:cd:ef", ":", null);
        expected = new String[]{"ab", "cd", "ef"};
        assertTrue("Result is [" + output(result) + "] should be [" + output(expected) + "] ", equal(result, expected));

        result = EDIUtils.split("ab:cd:ef:", ":", null);
        expected = new String[]{"ab", "cd", "ef", ""};
        assertTrue("Result is [" + output(result) + "] should be [" + output(expected) + "] ", equal(result, expected));

        result = EDIUtils.split("ab:cd:ef::", ":", null);
        expected = new String[]{"ab", "cd", "ef", "", ""};
        assertTrue("Result is [" + output(result) + "] should be [" + output(expected) + "] ", equal(result, expected));

        result = EDIUtils.split(":cd:ef", ":", null);
        expected = new String[]{"", "cd", "ef"};
        assertTrue("Result is [" + output(result) + "] should be [" + output(expected) + "] ", equal(result, expected));

        result = EDIUtils.split("::cd:ef", ":", null);
        expected = new String[]{"", "", "cd", "ef"};
        assertTrue("Result is [" + output(result) + "] should be [" + output(expected) + "] ", equal(result, expected));

        result = EDIUtils.split(":cd:ef:", ":", null);
        expected = new String[]{"", "cd", "ef", ""};
        assertTrue("Result is [" + output(result) + "] should be [" + output(expected) + "] ", equal(result, expected));


	}

    public void test_concatAndTruncate() {
        Delimiters delims = UNEdifactInterchangeParser.defaultUNEdifactDelimiters;

        assertEquals("ab", EDIUtils.concatAndTruncate(CollectionsUtil.toList("a", "b", "+:+"), DelimiterType.SEGMENT, delims));
        assertEquals("a+:+b", EDIUtils.concatAndTruncate(CollectionsUtil.toList("a", "+:+", "b", "+:+"), DelimiterType.SEGMENT, delims));
        assertEquals("a+:+bc+:+", EDIUtils.concatAndTruncate(CollectionsUtil.toList("a", "+:+", "b", "c+:+"), DelimiterType.SEGMENT, delims));

        assertEquals("ab", EDIUtils.concatAndTruncate(CollectionsUtil.toList("a", "b", "+:+"), DelimiterType.FIELD, delims));
        assertEquals("ab+:+'", EDIUtils.concatAndTruncate(CollectionsUtil.toList("a", "b", "+:+'"), DelimiterType.FIELD, delims));
        assertEquals("ab+:+", EDIUtils.concatAndTruncate(CollectionsUtil.toList("a", "b", "+:+"), DelimiterType.COMPONENT, delims));
    }

    private String output(String[] value) {
        if (value == null) {
            return null;
        }

        String result = "{";
        String str;
        for (int i = 0; i < value.length; i++) {
            str = value[i];
            result += "\"" + str + "\"";
            if (i != value.length -1) {
                result += ", ";
            }
        }
        result += "}";
        return result;
    }

    private static boolean equal(String[] test, String[] expected) {
        if (test.length != expected.length) {
            return false;
        }

        for (int i = 0; i < test.length; i++) {
            if (!test[i].equals(expected[i])) {
                return false;
            }
        }
        return true;
    }
}
