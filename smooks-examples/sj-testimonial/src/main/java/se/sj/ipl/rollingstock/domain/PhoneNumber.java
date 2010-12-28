package se.sj.ipl.rollingstock.domain;

import java.io.Serializable;

public class PhoneNumber implements Serializable
{
	static final long serialVersionUID = 3273758851944676665L;
	
	private int id;
    private String function;
	private String location;
	private String number;
	
	public PhoneNumber()  { }
	
	public PhoneNumber(String function, String location, String number)
	{
		if ( function == null)
			throw new IllegalArgumentException("function must not be null");
		if ( location == null)
			throw new IllegalArgumentException("location must not be null");
		if ( number == null)
			throw new IllegalArgumentException("number must not be null");
        this.function = function;
		this.location = location;
		this.number = number;
	}
	
	public int getId() { return id; }
	public void setId(int id) { this.id = id; }
	
    public String getFunction() { return function; }
    public void setFunction(String function) { this.function = function; }
  
	public String getLocation() { return location; }
	public void setLocation(String location) { this.location = location; }

	public String getNumber() { return number; }
	public void setNumber(String number) 
	{ 
		this.number = number; 
	}
	
	public boolean equals( Object obj )
	{
		if ( obj == this ) return true;
		if ( obj == null ) return false;
		
		if ( obj.getClass() != this.getClass() )
			return false;
		
		PhoneNumber phoneNumber = ( PhoneNumber ) obj;
		return	( function == phoneNumber.function && location == phoneNumber.location && number == phoneNumber.number)  || 
				( location != null && location.equals( phoneNumber.location ) &&  
				( number != null && number.equals( phoneNumber.number ) ) &&
				( function != null && function.equals( phoneNumber.function ) ) );
	}
	
	public int hashCode()
	{
		int hash = 7;
		hash = 31 * hash * ( location == null ? 0 : location.hashCode() );
		hash = 31 * hash * ( number == null ? 0 : number.hashCode() );
		hash = 31 * hash * ( function == null ? 0 : function.hashCode() );
		return hash;
	}
	
	public String toString()
	{
		return "[function:" + function + ",location:" + location + ",number:" + number + "]";
	}
	
}
