package org.milyn.cdr;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import org.milyn.profile.DefaultProfileSet;

public class SmooksResourceConfigurationSortComparatorTest {

    private DefaultProfileSet profileSet;

    @Before
    public void setUp() throws Exception {
        profileSet = new DefaultProfileSet("uaCommonName");
        profileSet.addProfiles(new String[] {"profile1", "profile2", "profile3"});
	}

    @Test
	public void test_getSpecificity_selector() {
		//  100 -> Explicit selector (non-xmldef, non-wildcard)
		//    0 -> Null namespace
		//    5 -> "*".
		assertSpecificityOK(105.0, "selector", null, "*");

		//   10 -> XmlDef selector
		//    0 -> Null namespace
        //    5 -> "*".
		assertSpecificityOK(15.0, SmooksResourceConfiguration.XML_DEF_PREFIX + "selector", null, "*");

		//    5 -> Wildcard selector "*".
		//    0 -> Null namespace
        //    5 -> "*".
		assertSpecificityOK(10.0, "*", null, "*");

		//  110 -> Explicit contextual selector - 2 deep
		//    0 -> Null namespace
        //    5 -> "*".
		assertSpecificityOK(115.0, "table tr", null, "*");

		//  110 -> Explicit contextual selector - 3 deep
		//    0 -> Null namespace
        //    5 -> "*".
		assertSpecificityOK(125.0, "table tr td", null, "*");
	}

    @Test
	public void test_getSpecificity_namespace() {
		//  100 -> Explicit selector (non-xmldef, non-wildcard)
		//   10 -> Namespace specified.
		//    5 -> Null Useragent => defaults to "*".
		assertSpecificityOK(115.0, "selector", "http://namespace", "*");
	}

    @Test
	public void test_getSpecificity_useragent() {
		//  100 -> Explicit selector (non-xmldef, non-wildcard)
		//    0 -> Null namespace
		//  100 -> Useragent specifying the ua common name.
		assertSpecificityOK(200.0, "selector", null, "uaCommonName");

		//  100 -> Explicit selector (non-xmldef, non-wildcard)
		//    0 -> Null namespace
		//    0 -> Useragent specifying a non-matching common name or profile.
		assertSpecificityOK(100.0, "selector", null, "X");

		//  100 -> Explicit selector (non-xmldef, non-wildcard)
		//    0 -> Null namespace
		//   10 -> Useragent specifying 1 ua profile.
		assertSpecificityOK(110.0, "selector", null, "profile1");

		//  100 -> Explicit selector (non-xmldef, non-wildcard)
		//    0 -> Null namespace
		//  110 -> Useragent specifying the ua common name + 1 ua profile.
		assertSpecificityOK(210.0, "selector", null, "uaCommonName, profile1");

		//  100 -> Explicit selector (non-xmldef, non-wildcard)
		//    0 -> Null namespace
		//  120 -> Useragent specifying the ua common name + 2 ua profiles.
		assertSpecificityOK(220.0, "selector", null, "uaCommonName, profile1, profile2");

		//  100 -> Explicit selector (non-xmldef, non-wildcard)
		//    0 -> Null namespace
		//   20 -> Useragent specifying 2 ua profiles.
		assertSpecificityOK(120.0, "selector", null, "profile1, profile2");

		//  100 -> Explicit selector (non-xmldef, non-wildcard)
		//    0 -> Null namespace
		//    1 -> Negated non-matching useragent
		assertSpecificityOK(101.0, "selector", null, "not:X");

		//  100 -> Explicit selector (non-xmldef, non-wildcard)
		//    0 -> Null namespace
		//    0 -> Negated matching useragent
		assertSpecificityOK(100.0, "selector", null, "not:uaCommonName");

		//  100 -> Explicit selector (non-xmldef, non-wildcard)
		//    0 -> Null namespace
		//    0 -> Negated matching profile
		assertSpecificityOK(100.0, "selector", null, "not:profile2");

		//  100 -> Explicit selector (non-xmldef, non-wildcard)
		//    0 -> Null namespace
		//    0 -> Negated matching non-useragent and non-profile
		assertSpecificityOK(101.0, "selector", null, "not:X");

		//  100 -> Explicit selector (non-xmldef, non-wildcard)
		//    0 -> Null namespace
		//  110 -> Matching expression - common name + 1 mathcing profile
		assertSpecificityOK(210.0, "selector", null, "uaCommonName AND profile2");
		//  100 -> Explicit selector (non-xmldef, non-wildcard)
		//    0 -> Null namespace
		//  101 -> Matching expression - common name + 1 negated non-mathcing useragent/profile
		assertSpecificityOK(201.0, "selector", null, "uaCommonName AND not:X");
		//  100 -> Explicit selector (non-xmldef, non-wildcard)
		//    0 -> Null namespace
		//    0 -> Non-matching expression - 2 negated mathcing useragents/profiles
		assertSpecificityOK(100.0, "selector", null, "uaCommonName AND not:profile2");
		//  100 -> Explicit selector (non-xmldef, non-wildcard)
		//    0 -> Null namespace
		//    2 -> Non-matching expression - 2 negated non-mathcing useragents/profiles
		assertSpecificityOK(102.0, "selector", null, "not:X AND not:Y");
	}

    @Test
	public void test_getSpecificity_all() {
		//  100 -> Explicit selector (non-xmldef, non-wildcard)
		//   10 -> Namespace specified.
		//  100 -> Useragent specifying the ua common name.
		assertSpecificityOK(210.0, "selector", "http://namespace", "uaCommonName");
	}
	
    @Test
	public void test_compare() {
		SmooksResourceConfigurationSortComparator sortComparator = new SmooksResourceConfigurationSortComparator(profileSet);
		SmooksResourceConfiguration config1;
		SmooksResourceConfiguration config2;

		// 0	-> same object instance
		config1 = new SmooksResourceConfiguration("selector", "http://namespace", "uaCommonName", null);
		assertEquals(0, sortComparator.compare(config1, config1));

		// 0	-> 2 configs of equal specificity
		config1 = new SmooksResourceConfiguration("selector", "http://namespace", "uaCommonName", null);
		config2 = new SmooksResourceConfiguration("selector", "http://namespace", "uaCommonName", null);
		assertEquals(0, sortComparator.compare(config1, config2));

		// -1	-> config1 more specific than config2
		config1 = new SmooksResourceConfiguration("selector", "http://namespace", "uaCommonName", null);
		config2 = new SmooksResourceConfiguration("selector", "http://namespace", "profile1", null);
		assertEquals(-1, sortComparator.compare(config1, config2));

		// 1	-> config2 more specific than config1
		config1 = new SmooksResourceConfiguration("selector", "http://namespace", "profile1", null);
		config2 = new SmooksResourceConfiguration("selector", "http://namespace", "uaCommonName", null);
		assertEquals(1, sortComparator.compare(config1, config2));
	}
	
	private void assertSpecificityOK(double expected, String selector, String namespaceURI, String useragents) {
		SmooksResourceConfigurationSortComparator sortComparator = new SmooksResourceConfigurationSortComparator(profileSet);
		SmooksResourceConfiguration config;
		double specificity;

		config = new SmooksResourceConfiguration(selector, namespaceURI, useragents, null);
		specificity = sortComparator.getSpecificity(config);
		assertEquals("Wrong specificity calculated.", expected, specificity, 0.01);
	}
}
