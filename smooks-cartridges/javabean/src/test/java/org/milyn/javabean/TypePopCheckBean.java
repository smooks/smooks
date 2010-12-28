/*
	Milyn - Copyright (C) 2006 - 2010

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License (version 2.1) as published by the Free Software
	Foundation.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

	See the GNU Lesser General Public License for more details:
	http://www.gnu.org/licenses/lgpl.txt
*/
package org.milyn.javabean;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class TypePopCheckBean {
    // Primitives...
	private byte byteVal;
	private short shortVal;
    private int intVal;
    private long longVal;
    private boolean boolVal;
    private float floatVal;
    private double doubleVal;
    private char charVal;

    // Object...
    private Integer integerVal;
    private Date dateVal;

    // Primitive Arrays...
    private int[] intValArray;

    // Object Arrays...
    private Integer[] integerValArray;

    // List...
    private List<Integer> integerValList;

    // Map...
    private Map<String, Integer> integerValMap;

    /**
	 * @return the byteVal
	 */
	public byte getByteVal() {
		return byteVal;
	}

	/**
	 * @param byteVal the byteVal to set
	 */
	public void setByteVal(byte byteVal) {
		this.byteVal = byteVal;
	}

	/**
	 * @return the shortVal
	 */
	public short getShortVal() {
		return shortVal;
	}

	/**
	 * @param shortVal the shortVal to set
	 */
	public void setShortVal(short shortVal) {
		this.shortVal = shortVal;
	}
	
    public int getIntVal() {
        return intVal;
    }

	public void setIntVal(int intVal) {
        this.intVal = intVal;
    }

    public long getLongVal() {
        return longVal;
    }

    public void setLongVal(long longVal) {
        this.longVal = longVal;
    }

    public boolean isBoolVal() {
        return boolVal;
    }

    public void setBoolVal(boolean boolVal) {
        this.boolVal = boolVal;
    }

    public float getFloatVal() {
        return floatVal;
    }

    public void setFloatVal(float floatVal) {
        this.floatVal = floatVal;
    }

    public double getDoubleVal() {
        return doubleVal;
    }

    public void setDoubleVal(double doubleVal) {
        this.doubleVal = doubleVal;
    }

    public char getCharVal() {
        return charVal;
    }

    public void setCharVal(char charVal) {
        this.charVal = charVal;
    }

    public Date getDateVal() {
        return dateVal;
    }

    public Integer getIntegerVal() {
        return integerVal;
    }

    public void setIntegerVal(Integer integerVal) {
        this.integerVal = integerVal;
    }

    public int[] getIntValArray() {
        return intValArray;
    }

    public void setIntValArray(int[] intValArray) {
        this.intValArray = intValArray;
    }

    public Integer[] getIntegerValArray() {
        return integerValArray;
    }

    public void setIntegerValArray(Integer[] integerValArray) {
        this.integerValArray = integerValArray;
    }

    public void setDateVal(Date dateVal) {
        this.dateVal = dateVal;
    }

    public List<Integer> getIntegerValList() {
        return integerValList;
    }

    public void setIntegerValList(List<Integer> integerValList) {
        this.integerValList = integerValList;
    }

    public Map<String, Integer> getIntegerValMap() {
        return integerValMap;
    }

    public void setIntegerValMap(Map<String, Integer> integerValMap) {
        this.integerValMap = integerValMap;
    }

    @Override
	public String toString() {
        StringBuffer string = new StringBuffer();

        string.append(intVal + ", ");
        string.append(longVal + ", ");
        string.append(boolVal + ", ");
        string.append(floatVal + ", ");
        string.append(doubleVal + ", ");
        string.append(charVal + ", ");

        string.append(integerVal + ", ");
        string.append((dateVal != null?dateVal.getTime():"null") + ", ");

        // Primitive Arrays...
        if(intValArray != null) {
            string.append(intValArray[0] + ", ");
            string.append(intValArray[1] + ", ");
            string.append(intValArray[2] + ", ");
        }

        // Object Arrays...
        if(integerValArray != null) {
            string.append(Arrays.asList(integerValArray) + ", ");
        }

        // List...
        string.append(integerValList + ", ");

        // Map...
        string.append(integerValMap + ", ");

        return string.toString();
    }
}