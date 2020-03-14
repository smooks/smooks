package org.smooks.csv.prog;

import org.junit.Test;
import org.smooks.csv.Person;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author
 */
@SuppressWarnings("unchecked")
public class CSVBinderTest {

    @Test
    public void test_CSVListBinder() {
        InputStream csvStream = getClass().getResourceAsStream("../input-message-01.csv");

        CSVListBinder binder = new CSVListBinder("firstname,lastname,gender,age,country", Person.class);
        List<Person> people = binder.bind(csvStream);

        assertEquals(2, people.size());
        assertEquals("[(Tom, Fennelly, Ireland, Male, 4), (Mike, Fennelly, Ireland, Male, 2)]", people.toString());
    }

    @Test
    public void test_CSVMapBinder() {
        InputStream csvStream = getClass().getResourceAsStream("../input-message-01.csv");

        CSVMapBinder binder = new CSVMapBinder("firstname,lastname,gender,age,country", Person.class, "firstname");
        Map<String, Person> people = binder.bind(csvStream);

        assertEquals(2, people.size());
        assertEquals("(Tom, Fennelly, Ireland, Male, 4)", people.get("Tom").toString());
        assertEquals("(Mike, Fennelly, Ireland, Male, 2)", people.get("Mike").toString());
    }
}
