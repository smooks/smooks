package se.sj.ipl.rollingstock.domain.types;

import java.io.Serializable;

/**
 * 
 * @author Daniel Bevenius
 *
 */
public class Speed implements Serializable
{
	static final long serialVersionUID = 2943790455843454272L;
	
	private double speed;
	private transient String unit = "s";
	
	public Speed() {}
	/**
	 * Sole constructor
	 * 
	 * @param speed	the length to set. Must be a positive value
	 */
	public Speed( double speed)
	{
		if ( speed < 0 )
			throw new IllegalArgumentException ( "length must be positive");
		this.speed = speed;
	}
	
	public void setSpeed(double speed) { this.speed = speed; }
	public double getSpeed() { return speed; }

	public String getUnit() { return unit; }
	public void setUnit(String unit) { this.unit = unit; }
	
	public boolean equals ( Object obj )
	{
		if ( obj == this ) return true;
		if ( obj == null ) return false;
		
		if ( obj.getClass() != this.getClass() )
			return false;
		
		Speed objSpeed = ( Speed ) obj;
		return this.speed == objSpeed.getSpeed() && this.unit == objSpeed.getUnit();
	}
	
	public int hashCode()
	{
		int hash = 7;
		hash = 31 * hash * (int) speed;
		hash = 31 * hash * (unit != null ? unit.hashCode():0);
		return hash;
	}
	
	public String toString()
	{
		return "[speed:" + speed + ", unit:" + unit + "]";
	}

}
