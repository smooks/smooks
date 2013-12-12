/*
 * Milyn - Copyright (C) 2006 - 2010
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License (version 2.1) as published by the Free Software
 *  Foundation.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 *  See the GNU Lesser General Public License for more details:
 *  http://www.gnu.org/licenses/lgpl.txt
 */

package org.milyn.test.ant;

import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * AntRunner test utility.
 * <p/>
 * <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class AntRunner {

    private Project project = new Project();

    /**
     * Public constructor.
     * <p/>
     * The <code>antScript</code> parameter is a filename on the classpath,
     * relative to the caller Class.  AntRunner (<code>this</code>) uses the
     * current Thread's {@link Thread#getStackTrace Stack Trace}
     * to determine the calling Class.
     *
     * @param antScript  The Ant script to be executed.  Classpath resource relative
     *                   to the caller Class.
     * @param properties Optional Ant properties.
     * @throws IOException Error reading Ant Script.
     */
    public AntRunner(String antScript, String... properties) throws IOException {
        StackTraceElement[] thisStack = Thread.currentThread().getStackTrace();

        for (int i = 0; i < thisStack.length; i++) {
            StackTraceElement stackFrame = thisStack[i];

            if (stackFrame.getClassName().equals(getClass().getName())) {
                StackTraceElement callerStackFrame = thisStack[i + 1];
                try {
                    Class callerClass = Class.forName(callerStackFrame.getClassName());
                    InputStream antScriptStream = callerClass.getResourceAsStream(antScript);

                    if (antScriptStream == null) {
                        throw new RuntimeException("Unable resolve Ant Script resource '" + antScript + "' relative to caller class '" + callerClass.getName() + "'.");
                    }

                    configureProject(antScriptStream, properties);
                    return;
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("Unexpected Exception: Unable resolve caller Class '" + callerStackFrame.getClassName() + "' for AntRunner on current Thread.");
                }
            }
        }

        throw new RuntimeException("Unexpected Exception: Unable resolve caller Class for AntRunner on current Thread.");
    }

    /**
     * Public constructor.
     *
     * @param antScript  The Ant script to be executed.
     * @param properties Optional Ant properties.
     * @throws IOException Error reading Ant Script.
     */
    public AntRunner(InputStream antScript, String... properties) throws IOException {
        configureProject(antScript, properties);
    }

    /**
     * Run a target on the Ant Script.
     *
     * @param target The target to run.
     * @return <code>this</code> class instance.
     */
    public AntRunner run(String target) {
        if (target == null) {
            throw new IllegalArgumentException("null 'target' argument.");
        }
        project.executeTarget(target);
        return this;
    }

    private void configureProject(InputStream antScript, String[] properties) throws IOException {
        if (antScript == null) {
            throw new IllegalArgumentException("null 'antScript' argument.");
        }

        try {
            project.init();

            DefaultLogger antLogger = new DefaultLogger();
            antLogger.setErrorPrintStream(System.err);
            antLogger.setOutputPrintStream(System.out);
            antLogger.setMessageOutputLevel(Project.MSG_INFO);

            project.addBuildListener(antLogger);
            project.setBaseDir(new File("./"));

            File executeScript = new File("./target/ant-exec.xml");
            FileOutputStream fileOs = new FileOutputStream(executeScript);

            try {
                byte[] readBuf = new byte[254];
                int readCount = 0;

                while ((readCount = antScript.read(readBuf)) != -1) {
                    fileOs.write(readBuf, 0, readCount);
                }
            } finally {
                fileOs.flush();
                fileOs.close();
            }

            ProjectHelper.configureProject(project, executeScript);

            if (properties != null) {
                for (String property : properties) {
                    int eqIndex = property.indexOf('=');

                    if (eqIndex == -1 || eqIndex + 1 == property.length()) {
                        throw new RuntimeException("Invalid AntRunner property '" + property + "'.  No value.");
                    }

                    String key = property.substring(0, eqIndex);
                    String value = property.substring(eqIndex + 1, property.length());

                    project.setProperty(key, value);
                }
            }
        } finally {
            antScript.close();
        }
    }
}
