package org.milyn.csv.prog;

import junit.framework.TestCase;

import org.milyn.csv.Person;

import java.util.List;
import java.util.Map;
import java.io.InputStream;

/**
 * @author
 */
public class CSVBinderTest extends TestCase {

    public void test_CSVListBinder() {
        InputStream csvStream = getClass().getResourceAsStream("../input-message-01.csv");

        CSVListBinder binder = new CSVListBinder("firstname,lastname,gender,age,country", Person.class);
        List<Person> people = binder.bind(csvStream);

        assertEquals(2, people.size());
        assertEquals("[(Tom, Fennelly, Ireland, Male, 4), (Mike, Fennelly, Ireland, Male, 2)]", people.toString());
    }

    public void test_CSVMapBinder() {
        InputStream csvStream = getClass().getResourceAsStream("../input-message-01.csv");

        CSVMapBinder binder = new CSVMapBinder("firstname,lastname,gender,age,country", Person.class, "firstname");
        Map<String, Person> people = binder.bind(csvStream);

        assertEquals(2, people.size());
        assertEquals("(Tom, Fennelly, Ireland, Male, 4)", people.get("Tom").toString());
        assertEquals("(Mike, Fennelly, Ireland, Male, 2)", people.get("Mike").toString());
    }
}
