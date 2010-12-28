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
package org.milyn.routing.jms.message.creationstrategies;

/**
 * Factory for creating {@link MessageCreationStrategy} instaces
 *
 * @author <a href="mailto:daniel.bevenius@gmail.com">Daniel Bevenius</a>
 *
 */
public final class StrategyFactory
{
	private static StrategyFactory instance = new StrategyFactory();

	private StrategyFactory() { };

	public static StrategyFactory getInstance()
	{
		return instance;
	}

	public static final String TEXT_MESSAGE = "TextMessage";
	public static final String OBJECT_MESSAGE = "ObjectMessage";
	public static final String MAP_MESSAGE = "MapMessage";

	public MessageCreationStrategy createStrategy( final String messageType )
	{
		if ( messageType.equals( TEXT_MESSAGE ))
		{
			return new TextMessageCreationStrategy();
		}
		else if ( messageType.equals( OBJECT_MESSAGE ))
		{
			return new ObjectMessageCreationStrategy();
		}
		else if ( messageType.equals( MAP_MESSAGE ))
		{
			return new MapMessageCreationStrategy();
		}

		throw new IllegalArgumentException( "No strategy for messageType [" + messageType + "] was found.");
	}

}
