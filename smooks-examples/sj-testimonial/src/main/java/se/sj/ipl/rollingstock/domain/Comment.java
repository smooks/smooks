package se.sj.ipl.rollingstock.domain;

import java.io.Serializable;

public class Comment implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	private int id;
	private String comment;
	
	public Comment() {  } 
	
	public Comment(String comment)
	{
		if ( comment == null)
			throw new IllegalArgumentException("comment must not be null");		
		this.comment = comment.trim();
	}
	
	public int getId() { return id; }
	public void setId(int id) { this.id = id; }
	
	public String getComment() { return comment; }
	public void setComment(String comment) 
	{ 
		if ( comment != null)
			this.comment = comment.trim(); 
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
			return true;
		if (obj == null)
			return false;
		if (obj.getClass() != this.getClass())
			return false;
		
		Comment commentObj = ( Comment ) obj;
		return	( comment != null && comment.equals( commentObj.comment ) );
	}
	
	public int hashCode()
	{
		int hash = 7;
		hash = hash * 31 * ( comment == null  ? 0 : comment.hashCode() ) ;
		return hash;
	}
	
	public String toString()
	{
		return "[comment:" + comment + "]";
	}

}
