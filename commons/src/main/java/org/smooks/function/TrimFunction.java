package org.smooks.function;

public class TrimFunction implements StringFunction {

	public String execute(String input) {
		return input.trim();
	}

}
