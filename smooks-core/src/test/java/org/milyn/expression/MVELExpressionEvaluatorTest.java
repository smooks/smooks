package org.milyn.expression;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

public class MVELExpressionEvaluatorTest extends TestCase {

	MVELExpressionEvaluator evaluator;

	public void test_getValue() {

		Object expected = new Object();

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("test", expected);

		evaluator.setExpression("return test");
		Object result = evaluator.getValue(map);

		assertSame("Expected object is not same as the result", expected, result);
	}

	public void test_eval() {

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("value1", 1);
		map.put("value2", 2);
		map.put("value3", 3);

		evaluator.setExpression("value1 + value2 == value3");
		boolean result = evaluator.eval(map);

		assertTrue("Expected true", result);

		map.put("value3", 4);

		result = evaluator.eval(map);

		assertFalse("Expected false", result);
	}

	public void test_vars_isdef() {

		Map<String, Object> map = new HashMap<String, Object>();

		evaluator.setExpression("VARS.isdef('value')");
		boolean result = evaluator.eval(map);

		assertFalse("Expected false", result);

		map.put("value", new Object());

		result = evaluator.eval(map);

		assertTrue("Expected true", result);

	}

	public void test_vars_resolvable() {

		Map<String, Object> map = new HashMap<String, Object>();

		evaluator.setExpression("VARS.isResolveable('value')");
		boolean result = evaluator.eval(map);

		assertFalse("Expected false", result);

		map.put("value", new Object());

		result = evaluator.eval(map);

		assertTrue("Expected true", result);

	}

	public void test_vars_unresolvable() {

		Map<String, Object> map = new HashMap<String, Object>();

		evaluator.setExpression("VARS.isUnresolveable('value')");
		boolean result = evaluator.eval(map);

		assertTrue("Expected true", result);

		map.put("value", new Object());

		result = evaluator.eval(map);

		assertFalse("Expected false", result);
	}

	public void test_vars_get() {
		Object var = new Object();

		Map<String, Object> map = new HashMap<String, Object>();

		evaluator.setExpression("VARS.get('value') == null");
		boolean result = evaluator.eval(map);

		assertTrue("Expected true", result);

		map.put("value", var);

		result = evaluator.eval(map);

		assertFalse("Expected false", result);

		evaluator.setExpression("VARS.get('value')");

		Object resultObj = evaluator.getValue(map);

		assertSame(var, resultObj);

	}

	@Override
	protected void setUp() throws Exception {
		evaluator = new MVELExpressionEvaluator();
	}

}
