package se.sj.ipl.rollingstock.domain.types;

import java.io.Serializable;

/**
 * 
 * @author Daniel Bevenius
 *
 */
public class Length implements Serializable
{
	static final long serialVersionUID = -8338845567930524772L;
	
	private double length;
	
	private String unit = "m";
	
	public Length() {}
	
	/**
	 * Sole constructor
	 * 
	 * @param length	the length to set. Must be a positive value
	 */
	public Length( double length)
	{
		if ( length < 0 )
			throw new IllegalArgumentException ( "length must be positive");
		this.length = length;
	}

	public String getUnit() { return unit; }
	
	public boolean equals ( Object obj )
	{
		if ( obj == this ) return true;
		if ( obj == null ) return false;
		
		if ( obj.getClass() != this.getClass() )
			return false;
		Length objLenght = ( Length ) obj;
		
		return this.length == objLenght.getLength() && this.unit == objLenght.getUnit();
	}
	
	public int hashCode()
	{
		int hash = 7;
		hash = 31 * hash * (int) length;
		hash = 31 * hash * unit.hashCode();
		return hash;
	}
	
	public String toString()
	{
		return "[length:" + length + ", unit:" + unit + "]";
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public double getLength()
	{
		return length;
	}

	public void setLength(double length)
	{
		this.length = length;
	}
	
}
