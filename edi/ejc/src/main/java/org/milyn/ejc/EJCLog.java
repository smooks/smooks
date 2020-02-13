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
import org.slf4j.Marker;

import static org.milyn.ejc.EJCLoggerFactory.Level;

public class EJCLog implements Logger {

    private EJCLoggerFactory.Level level;

    public EJCLog(Level level) {
        this.level = level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public boolean isDebugEnabled() {
        return this.level == Level.DEBUG;
    }

    @Override
    public void debug(String msg) {
        write(Level.DEBUG, msg);
    }

    @Override
    public void debug(String format, Object arg) {
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
    }

    @Override
    public void debug(String format, Object... arguments) {
    }

    @Override
    public void debug(String msg, Throwable t) {
        write(Level.DEBUG, msg);
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return this.level == Level.DEBUG;
    }

    @Override
    public void debug(Marker marker, String msg) {
        write(Level.DEBUG, msg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg) {
    }

    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
    }

    @Override
    public void debug(Marker marker, String format, Object... arguments) {
    }

    @Override
    public void debug(Marker marker, String msg, Throwable t) {
        write(Level.DEBUG, msg);
    }

    @Override
    public boolean isInfoEnabled() {
        return this.level == Level.INFO;
    }

    public boolean isErrorEnabled() {
        return this.level == Level.ERROR;
    }

    @Override
    public void error(String msg) {
        write(Level.ERROR, msg);
    }

    @Override
    public void error(String format, Object arg) {
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
    }

    @Override
    public void error(String format, Object... arguments) {
    }

    @Override
    public void error(String msg, Throwable t) {
        write(Level.ERROR, msg);
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return this.level == Level.ERROR;
    }

    @Override
    public void error(Marker marker, String msg) {
        write(Level.ERROR, msg);
    }

    @Override
    public void error(Marker marker, String format, Object arg) {
    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
    }

    @Override
    public void error(Marker marker, String format, Object... arguments) {
    }

    @Override
    public void error(Marker marker, String msg, Throwable t) {
        write(Level.ERROR, msg);
    }

    @Override
    public void info(String msg) {
        write(Level.INFO, msg);
    }

    @Override
    public void info(String format, Object arg) {
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
    }

    @Override
    public void info(String format, Object... arguments) {
    }

    @Override
    public void info(String msg, Throwable t) {
        write(Level.INFO, msg);
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return this.level == Level.INFO;
    }

    @Override
    public void info(Marker marker, String msg) {
        write(Level.INFO, msg);
    }

    @Override
    public void info(Marker marker, String format, Object arg) {
    }

    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
    }

    @Override
    public void info(Marker marker, String format, Object... arguments) {
    }

    @Override
    public void info(Marker marker, String msg, Throwable t) {
        write(Level.INFO, msg);
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isTraceEnabled() {
        return false;
    }

    @Override
    public void trace(String msg) {
    }

    @Override
    public void trace(String format, Object arg) {
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
    }

    @Override
    public void trace(String format, Object... arguments) {
    }

    @Override
    public void trace(String msg, Throwable t) {
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return false;
    }

    @Override
    public void trace(Marker marker, String msg) {
    }

    @Override
    public void trace(Marker marker, String format, Object arg) {
    }

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
    }

    @Override
    public void trace(Marker marker, String format, Object... argArray) {
    }

    @Override
    public void trace(Marker marker, String msg, Throwable t) {
    }

    @Override
    public boolean isWarnEnabled() {
        return true;
    }

    @Override
    public void warn(String msg) {
        write(Level.WARN, msg);
    }

    @Override
    public void warn(String format, Object arg) {

    }

    @Override
    public void warn(String format, Object... arguments) {

    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {

    }

    @Override
    public void warn(String msg, Throwable t) {
        write(Level.WARN, msg);
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return this.level == Level.WARN;
    }

    @Override
    public void warn(Marker marker, String msg) {
        write(Level.WARN, msg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg) {

    }

    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {

    }

    @Override
    public void warn(Marker marker, String format, Object... arguments) {

    }

    @Override
    public void warn(Marker marker, String msg, Throwable t) {
        write(Level.WARN, msg);
    }

    private void write(EJCLoggerFactory.Level level, Object object) {
        if (this.level.getValue() > level.getValue()) {
            return;
        }

        System.out.println("[" + level + "] " + object);
    }
}
