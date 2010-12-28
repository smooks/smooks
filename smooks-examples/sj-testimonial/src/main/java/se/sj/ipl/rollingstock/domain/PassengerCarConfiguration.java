package se.sj.ipl.rollingstock.domain;

import java.io.Serializable;

public class PassengerCarConfiguration implements Serializable
{
	static final long serialVersionUID = -309776169294059697L;
	
	private int id;
	
	private int class1;
	private int class2;
	private int couchette;
	private int compartments;
	
	public PassengerCarConfiguration()  { }
	
	public PassengerCarConfiguration(int class1, int class2, int couchette, int compartments)
	{
		this.class1 = class1;
		this.class2 = class2;
		this.couchette = couchette;
		this.compartments = compartments;
	}

	public Integer getId() { return id; }
	public void setId(Integer id) { this.id = id; }

	public int getClass1() { return class1; }
	public void setClass1(int class1) { this.class1 = class1; }

	public int getClass2() { return class2; }
	public void setClass2(int class2) { this.class2 = class2; }

	public int getCouchette() { return couchette; }
	public void setCouchette(int sleepSeats) { this.couchette = sleepSeats; }

	public int getCompartments() { return compartments; }
	public void setCompartments(int compartments) { this.compartments = compartments; }
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
			return true;
		if (obj == null)
			return false;
		if (obj.getClass() != this.getClass())
			return false;
		PassengerCarConfiguration pass = ( PassengerCarConfiguration ) obj;
		return class1 == pass.class1 
			&& class2 == pass.class2 
			&& couchette == pass.couchette 
			&& compartments == pass.compartments;
	}
	
	public int hashCode()
	{
		int hash = 7;
		hash = 31 * hash * class1;
		hash = 31 * hash * class2;
		hash = 31 * hash * couchette;
		hash = 31 * hash * compartments;
		return hash;
	}
	
	public String toString()
	{
		return "[class1:" + class1 + ", class2:" + class2 + ",sleepSeats:" + couchette + ",compartments:" + compartments + "]";
	}
	
}
