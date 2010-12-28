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
package org.milyn.edisax;

/**
 * Buffered Segment listener.
 * <p/>
 * Implementations of this interface can control when and how the {@link BufferedSegmentReader#moveToNextSegment()} 
 * method returns true (segment exists) or false (segment does not exist).
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public interface BufferedSegmentListener {
	
	/**
	 * Notify the listener of a new segment.
	 * 
	 * @param bufferedSegmentReader The segment reader.
	 * @return True if the {@link BufferedSegmentReader} is to tell the caller of the 
	 * {@link BufferedSegmentReader#moveToNextSegment()} method whether or not the segment exists.
	 */
	boolean onSegment(BufferedSegmentReader bufferedSegmentReader);
}
