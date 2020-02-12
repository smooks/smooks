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
package org.milyn.routing.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Session;

/**
 * Enum to type-safe JMS Client Acknowledgement mode string
 * mappings to JMS Session's integers.
 *
 */
public enum AcknowledgeModeEnum
{
	CLIENT_ACKNOWLEDGE(Session.CLIENT_ACKNOWLEDGE),
	AUTO_ACKNOWLEDGE(Session.AUTO_ACKNOWLEDGE),
	DUPS_OK_ACKNOWLEDGE(Session.DUPS_OK_ACKNOWLEDGE);

	private static final Logger LOGGER = LoggerFactory.getLogger( AcknowledgeModeEnum.class ); // NOPMD by danbev on 8/03/08 09:20

	private int jmsAckModeInt;

	AcknowledgeModeEnum(final int jmsAckModeInt)
	{
		this.jmsAckModeInt = jmsAckModeInt;
	}

	public int getAcknowledgeModeInt()
	{
		return jmsAckModeInt;
	}

	static public AcknowledgeModeEnum getAckMode(final String ackMode)
	{
		if(ackMode != null)
		{
			try
			{
				return  AcknowledgeModeEnum.valueOf(ackMode); // NOPMD by danbev on 8/03/08 09:20
			}
			catch (IllegalArgumentException e)
			{
				LOGGER.debug("' " + ackMode + "' is invalid : " + ". Will use default '" + AcknowledgeModeEnum.AUTO_ACKNOWLEDGE);
			}
		}
		return AcknowledgeModeEnum.AUTO_ACKNOWLEDGE;
	}
}
