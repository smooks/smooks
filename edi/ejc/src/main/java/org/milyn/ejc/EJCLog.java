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

import org.apache.commons.logging.Log;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;
import static org.milyn.ejc.EJCLogFactory.Level;

public class EJCLog implements Log {

        private EJCLogFactory.Level level;

        public EJCLog(Level level) {
            this.level = level;
        }

        public void setLevel(Level level) {
            this.level = level;
        }

        public void debug(Object object) {
            write(Level.DEBUG, object);
        }

        public void debug(Object object, Throwable throwable) {
            write(Level.DEBUG, object);
        }

        public void error(Object object) {
            write(Level.ERROR, object);
        }

        public void error(Object object, Throwable throwable) {
            write(Level.ERROR, object);
        }

        public void fatal(Object object) {
            write(Level.FATAL, object);
        }

        public void fatal(Object object, Throwable throwable) {
            write(Level.FATAL, object);
        }

        public void info(Object object) {
            write(Level.INFO, object);
        }

        public void info(Object object, Throwable throwable) {
            write(Level.INFO, object);
        }

        public boolean isDebugEnabled() {
            return this.level == Level.DEBUG;
        }

        public boolean isErrorEnabled() {
            return true;
        }

        public boolean isFatalEnabled() {
            return true;
        }

        public boolean isInfoEnabled() {
            return true;
        }

        public boolean isTraceEnabled() {
            return false;
        }

        public boolean isWarnEnabled() {
            return true;
        }

        public void trace(Object object) {
        }

        public void trace(Object object, Throwable throwable) {
        }

        public void warn(Object object) {
            write(Level.WARN, object);
        }

        public void warn(Object object, Throwable throwable) {
            write(Level.WARN, object);
        }

        private void write(EJCLogFactory.Level level, Object object) {            
            if (this.level.getValue() > level.getValue()) {
                return;
            }

            System.out.println("[" + level + "] " + object);

        }
    }
