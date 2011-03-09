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
package org.milyn.payload;


import javax.xml.transform.Result;

/**
 * An extractor of results produces by Smooks.
 * 
 * Implementors of ResultExtractor are able to extract specific a object from the
 * result of a Smooks filtering process.
 * </p>
 * 
 * @author Daniel Bevenius
 * @since 1.4
 */
public interface ResultExtractor<T extends Result>
{
    Object extractFromResult(T result, Export export);
}
