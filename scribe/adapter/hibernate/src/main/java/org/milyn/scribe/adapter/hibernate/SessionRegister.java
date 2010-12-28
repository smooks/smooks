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
package org.milyn.scribe.adapter.hibernate;

import java.util.Map;

import org.hibernate.Session;
import org.milyn.assertion.AssertArgument;
import org.milyn.scribe.register.AbstractDaoAdapterRegister;
import org.milyn.scribe.register.AbstractDaoRegister;


/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class SessionRegister extends AbstractDaoAdapterRegister<SessionDaoAdapter, Session> {

	public SessionRegister(Session session) {
		super(session);
	}

	public SessionRegister(Session defaultSession, Map<String, ? extends Session> sessionMap) {
		super(defaultSession, sessionMap);
	}

	public SessionRegister(Map<String, ? extends Session> sessionMap) {
		super(sessionMap);
	}

	/* (non-Javadoc)
	 * @see org.milyn.scribe.register.AbstractDaoAdapterRegister#createAdapter(java.lang.Object)
	 */
	@Override
	protected SessionDaoAdapter createAdapter(Session adaptable) {
		return new SessionDaoAdapter(adaptable);
	}

}
