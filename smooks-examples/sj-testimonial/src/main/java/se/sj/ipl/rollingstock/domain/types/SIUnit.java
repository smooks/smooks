package se.sj.ipl.rollingstock.domain.types;

public enum SIUnit
{
	M("meter"), KG("kilogram"), s("second");
	
	private String name;
	
	SIUnit( String name)
	{
		this.name = name;
	}
	
	public String getName()
	{
		return name;
	}

}
