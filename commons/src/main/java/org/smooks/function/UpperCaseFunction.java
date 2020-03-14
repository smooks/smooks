package org.smooks.function;

public class UpperCaseFunction implements StringFunction {

	public String execute(String input) {
		return input.toUpperCase();
	}

}
