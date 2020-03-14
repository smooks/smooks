/*
 * Milyn - Copyright (C) 2006 - 2010
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License (version 2.1) as published
 * by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.
 *
 * See the GNU Lesser General Public License for more details:
 * http://www.gnu.org/licenses/lgpl.txt
 */

package org.smooks.routing.file.naming;

import java.rmi.dgc.VMID;

/**
 * Default file naming strategy.
 * </pre>
 * This implementation uses java.rmi.dgc.VMID to generate
 * identifiers that are unique across all Java virtual machines.
 *
 * @author <a href="mailto:daniel.bevenius@gmail.com">Daniel Bevenius</a>
 *
 */
public class UniqueFileNamingStrategy implements NamingStrategy
{
	/**
	 * Will generate a String with the format
	 * <prefix><name><VMID.toString()><suffix>
	 */
	public String generateFileName( final String fileNamePattern, final Object object )
	{
		final StringBuilder sb = new StringBuilder();
		appendString( sb, fileNamePattern );
		appendString( sb, new VMID().toString() );

		return sb.toString();
	}

	private void appendString( final StringBuilder sb, final String str )
	{
		if ( str != null )
		{
			sb.append( str );
		}
	}

}
