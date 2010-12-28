/*
	Milyn - Copyright (C) 2006

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
package org.milyn.distro.html.visitors;

import org.milyn.SmooksException;
import org.milyn.cdr.annotation.ConfigParam;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.dom.DOMVisitBefore;
import org.milyn.distro.html.Downloader;
import org.milyn.io.StreamUtils;
import org.w3c.dom.Element;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.UUID;

/**
 * Download a page object and store it in the specified directory.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class ObjectDownloader implements DOMVisitBefore {

    @ConfigParam
    private String objectAttribute;

    @ConfigParam
    private String objectDir;

    public void visitBefore(Element element, ExecutionContext executionContext) throws SmooksException {
        String objectURLString = element.getAttribute(objectAttribute);

        if(objectURLString != null && !objectURLString.trim().equals("")) {
            try {
                URI objectURI = executionContext.getDocumentSource().resolve(objectURLString);
                URL objectURL = objectURI.toURL();
                File objectInFile = new File(objectURI.getPath());
                String relDownloadFilePath = objectDir + "/" + objectInFile.getName();                

                try {
                    File downloadDir = Downloader.getDownloadDir(executionContext);
                    File objectDownFile = new File(downloadDir, relDownloadFilePath);

                    if(downloadObject(objectDownFile, executionContext)) {
                        InputStream objectStream = objectURL.openStream();
    
                        try {
                            // Read the object...
                            byte[] objectBytes = StreamUtils.readStream(objectStream);

                            // Write the object...
                            createObjectDir(objectDownFile);
                            StreamUtils.writeFile(objectDownFile, objectBytes);
                        } finally {
                            objectStream.close();
                        }
                    }

                    // Reset the object address to the relative download location...
                    element.setAttribute(objectAttribute, relDownloadFilePath);
                } catch (IOException e) {
                    System.out.println("Failed to read object '" + objectURLString + "'.");
                }
            } catch (MalformedURLException e) {
                System.out.println("Invalid object URL '" + objectURLString + "'.");
            }
        }
    }

    private boolean downloadObject(File objectDownFile, ExecutionContext executionContext) {
        if(!objectDownFile.exists()) {
            return true;
        } else {
            Date fileModTime = new Date(objectDownFile.lastModified());
            Date startTime = Downloader.getStartTime(executionContext);

            if(fileModTime.before(startTime)) {
                // If the file mod time is before the start time, delete it and get it again....
                objectDownFile.delete();
                return true;
            }

            return false;
        }
    }

    private void createObjectDir(File objectDownFile) {
        File destDir = objectDownFile.getParentFile();
        if(destDir == null) {
            throw new RuntimeException("WTF... no parent dir? [" + objectDownFile.getAbsolutePath() + "]");
        } else {
            destDir.mkdirs();
        }
    }
}
