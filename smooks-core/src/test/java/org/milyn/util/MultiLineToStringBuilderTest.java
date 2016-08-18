package org.milyn.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.milyn.container.ExecutionContext;
import org.milyn.container.MockExecutionContext;
import org.milyn.payload.FilterResult;
import org.milyn.payload.JavaResult;

import org.junit.Test;
import static org.junit.Assert.*;

public class MultiLineToStringBuilderTest {

	private static final String NL = System.getProperty("line.separator");

	@Test
	public void test() {

		ExecutionContext context = new MockExecutionContext();

		JavaResult dummyResult = new JavaResult(true);

		FilterResult.setResults(context, dummyResult);

		context.getBeanContext().addBean("string", "blaat", null);
		context.getBeanContext().addBean("emptyMap", Collections.emptyMap(), null);
		context.getBeanContext().addBean("emptyList", Collections.emptyList(), null);
		context.getBeanContext().addBean("emptyArray", new String[0], null);

		Map<String, String> stringMap = new LinkedHashMap<String, String>();
		stringMap.put("v1", "some text");
		stringMap.put("v2", "other text");
		stringMap.put("v3", null);

		context.getBeanContext().addBean("stringMap", stringMap, null);

		List<Integer> integerList = new ArrayList<Integer>();
		integerList.add(1);
		integerList.add(2);
		integerList.add(null);

		context.getBeanContext().addBean("integerList", integerList, null);

		context.getBeanContext().addBean("stringArray", new String[] {"a1", "a2", "a3", null}, null);

		Map<String, Object> objectMap = new LinkedHashMap<String, Object>();
		objectMap.put("self", objectMap);

		Map<String, Object> object2Map = new LinkedHashMap<String, Object>();
		object2Map.put("parent", objectMap);

		objectMap.put("map", object2Map);

		List<Object> list = new ArrayList<Object>();

		list.add(list);
		list.add(objectMap);

		object2Map.put("list", list);

		context.getBeanContext().addBean("objectMap", objectMap, null);

		context.getAttributes().put("multiline", "hello\nworld");

		String actual = MultiLineToStringBuilder.toString(context);

		System.out.println(actual);

		String expected =
			"BeanContext : {" + NL +
			"   \"string\" : \"blaat\"," + NL +
			"   \"emptyMap\" : {}," + NL +
			"   \"emptyList\" : []," + NL +
			"   \"emptyArray\" : []," + NL +
			"   \"stringMap\" : {" + NL +
			"      \"v1\" : \"some text\"," + NL +
			"      \"v2\" : \"other text\"," + NL +
			"      \"v3\" : NULL" + NL +
			"   }," + NL +
			"   \"integerList\" : [" + NL +
			"      1," + NL +
			"      2," + NL +
			"      NULL" + NL +
			"   ]," + NL +
			"   \"stringArray\" : [" + NL +
			"      \"a1\"," + NL +
			"      \"a2\"," + NL +
			"      \"a3\"," + NL +
			"      NULL" + NL +
			"   ]," + NL +
			"   \"objectMap\" : {" + NL +
			"      \"self\" : THIS," + NL +
			"      \"map\" : {" + NL +
			"         \"parent\" : PARENT-1," + NL +
			"         \"list\" : [" + NL +
			"            THIS," + NL +
			"            PARENT-2" + NL +
			"         ]" + NL +
			"      }" + NL +
			"   }" + NL +
			"}" + NL +
			NL +
			"Attributes : {" + NL +
			"   \"multiline\" : \"hello" + NL +
			"               world\"," + NL +
			"}";


		assertEquals(expected, actual);
	}

}
