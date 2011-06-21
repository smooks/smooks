/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and others contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2005-2006, JBoss Inc.
 */
package org.milyn.ejc.ant;

import org.apache.tools.ant.BuildException;
import org.milyn.edisax.util.IllegalNameException;
import org.milyn.ejc.EJCExecutor;
import org.milyn.ejc.EJCException;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * {@link org.milyn.ejc.EJC} Ant task.
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class EJCAntTask extends EJCExecutor {

    public void execute() throws BuildException, IOException, ClassNotFoundException, SAXException, IllegalNameException {
        try {
            super.execute();
        } catch (EJCException e) {
            throw new BuildException("Error Executing EJC Ant Task.  See chained cause.", e);
        }
    }
}
