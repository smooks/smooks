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
package org.milyn.ejc;

import org.slf4j.Logger;

/**
 * A simple logger for writing status inside EJC.
 */
public class EJCLoggerFactory {

    private static EJCLog log;

    public static Logger getLogger(Class clazz) {
        if (log == null) {
            log = new EJCLog(Level.INFO);
        }
        return log;
    }

    public void setLevel(Level level) {
        ((EJCLog) getLogger(getClass())).setLevel(level);
    }

    public enum Level {
        DEBUG(0),
        INFO(1),
        WARN(2),
        ERROR(3),
        FATAL(4);

        private int value;

        Level(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }
}
