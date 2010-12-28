package se.sj.ipl.rollingstock.domain.types;

import java.io.Serializable;

/**
 * 
 * @author Daniel Bevenius
 *
 */
public class Weight implements Serializable
{
	static final long serialVersionUID = -7918001602172848474L;
	
	private double weight;
	private String unit = "kg";
	
	public Weight() {} 
	
	public Weight(final double weight)
	{
		this(weight, "kg");
	}

	/**
	 * 
	 * @param weight	the weight to set. Must be a positive value
	 */
	public Weight(final double weight, final String unit )
	{
		if ( weight < 0 )
			throw new IllegalArgumentException ( "weight must be positive");
		this.weight = weight;
		this.unit = unit;
	}

	public double getWeight()
	{
		return weight;
	}
	public void setWeight(double weight)
	{
		this.weight = weight;
	}	

	public String getUnit() { return unit; }
	public void setUnit(String unit) { this.unit = unit; }
	
	public boolean equals( Object obj )
	{
		if ( this == obj )  return true;
		if ( obj == null)  return false;
		
		if (obj.getClass() != this.getClass()) 
			return false;
		
		Weight objWeight = ( Weight ) obj;
		return weight == objWeight.getWeight() && this.unit == objWeight.getUnit();
	}
	
	public int hashCode()
	{
		int hash = 7;
		hash = 31 * hash * (int) weight;
		hash = 31 * hash * (unit != null ? unit.hashCode(): 0);
		return hash;
	}
	
	public String toString()
	{
		return "[weight:" + weight + ", unit:" + unit + "]";
	}
	

}
