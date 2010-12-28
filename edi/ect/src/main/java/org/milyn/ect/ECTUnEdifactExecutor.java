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
package org.milyn.ect;

import java.io.*;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * {@link org.milyn.ect.EdiConvertionTool} Executor specific for Un/Edifact format.
 * @author bardl
 */
public class ECTUnEdifactExecutor {

    private File unEdifactZip;
    private String urn;
    private File mappingModelZip;
    private File mappingModelFolder;

    public void execute() throws EdiParseException {
        assertConfigOK();

        ZipInputStream definitionZipStream;
        try {
            definitionZipStream = new ZipInputStream(new FileInputStream(unEdifactZip));
        } catch (FileNotFoundException e) {
            throw new EdiParseException("Error opening zip file containing the Un/Edifact specification '" + unEdifactZip.getAbsoluteFile() + "'.", e);
        }

        try {
            if(mappingModelZip != null) {
                EdiConvertionTool.fromUnEdifactSpec(definitionZipStream, new ZipOutputStream(new FileOutputStream(mappingModelZip)), urn);
            } else {
                EdiConvertionTool.fromUnEdifactSpec(definitionZipStream, mappingModelFolder, urn);
            }
        } catch (Exception e) {
            throw new EdiParseException("Error parsing the Un/Edifact specification '" + unEdifactZip.getAbsoluteFile() + "'.", e);
        } 
    }

    private void assertConfigOK() {
        if(unEdifactZip == null) {
            throw new EdiParseException("Mandatory UN/EDIFACT ECT property 'unEdifactZip' + not specified.");
        }
        if(urn == null) {
            throw new EdiParseException("Mandatory UN/EDIFACT ECT property 'urn' + not specified.");
        }
        if(mappingModelZip == null && mappingModelFolder == null) {
            throw new EdiParseException("One of the properties 'mappingModelZip' or 'mappingModelFolder' must be specified on the UN/EDIFACT ECT configuration.");
        }
        if(mappingModelZip != null && mappingModelFolder != null) {
            throw new EdiParseException("Only one of the properties 'mappingModelZip' and 'mappingModelFolder' should be specified on the UN/EDIFACT ECT configuration.");
        }

        if(!unEdifactZip.exists()) {
            throw new EdiParseException("Specified UN/EDIFACT definition zip file '" + unEdifactZip.getAbsoluteFile() + "' does not exist.");
        }
        if(unEdifactZip.isDirectory()) {
            throw new EdiParseException("Specified UN/EDIFACT definition zip file '" + unEdifactZip.getAbsoluteFile() + "' exists, but is a directory.  Must be a zip file.");
        }
        if(mappingModelZip != null && mappingModelZip.exists()) {
            throw new EdiParseException("Specified mapping model zip file '" + mappingModelZip.getAbsoluteFile() + "' already exists.");
        }
    }

    public void setUnEdifactZip(File unEdifactZip) {
        this.unEdifactZip = unEdifactZip;
    }

    public void setUrn(String urn) {
        this.urn = urn;
    }

    public void setMappingModelZip(File mappingModelZip) {
        this.mappingModelZip = mappingModelZip;
    }

    public void setMappingModelFolder(File mappingModelFolder) {
        this.mappingModelFolder = mappingModelFolder;
    }
}