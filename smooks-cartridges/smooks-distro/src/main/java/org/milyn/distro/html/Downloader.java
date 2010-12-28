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
package org.milyn.distro.html;

import org.milyn.Smooks;
import org.milyn.container.ExecutionContext;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.Date;

/**
 * Web page Downloader.
 * <p/>
 * Downloads the specified web page to the specified location and applies
 * the "page-downloader.xml" Smooks configuration.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class Downloader {

    private static final String DOWNLOAD_DIR = Downloader.class.getName() + "#DOWNLOAD_DIR";
    private static final String START_TIME = Downloader.class.getName() + "#START_TIME";

    public static void main(String[] args) throws Exception {
        if(args.length < 2) {
            throw new RuntimeException("Sorry, expected at least 2 args: <Input-URL> and <Destination-File>");
        }

        File destPage = new File(args[1]);
        String encoding;

        if(args.length == 3) {
            encoding = args[2];
        } else {
            encoding = "ISO-8859-1";
        }

        if(destPage.exists() && destPage.isDirectory()) {
            throw new RuntimeException("Destination file '" + destPage.getAbsolutePath() + "' already exists, but is a directory!");
        }

        File destDir = destPage.getParentFile();
        if(destDir == null) {
            throw new RuntimeException("WTF... no parent dir? [" + args[1] + "]");
        } else {
            destDir.mkdirs();
        }

        URL sourcePage = new URL(args[0]);
        InputStreamReader srcStream = new InputStreamReader(sourcePage.openStream(), encoding);
        try {
            OutputStreamWriter destPageOutputStream = new OutputStreamWriter(new FileOutputStream(destPage), encoding);
            try {
                Smooks smooks = new Smooks("/org/milyn/distro/html/downloader.xml");
                ExecutionContext execContext = smooks.createExecutionContext();

                execContext.setDocumentSource(sourcePage.toURI());
                setDownloadDir(destDir, execContext);
                setStartTime(execContext);

                //execContext.setEventListener(new HtmlReportGenerator("/zap/report.html"));
                smooks.filterSource(execContext, new StreamSource(srcStream), new StreamResult(destPageOutputStream));
            } finally {
                destPageOutputStream.close();
            }
        } finally {
            srcStream.close();
        }
    }

    private static void setDownloadDir(File dir, ExecutionContext execContext) {
        execContext.setAttribute(DOWNLOAD_DIR, dir);
    }

    public static File getDownloadDir(ExecutionContext execContext) {
        return (File) execContext.getAttribute(DOWNLOAD_DIR);
    }

    private static void setStartTime(ExecutionContext execContext) {
        execContext.setAttribute(START_TIME, new Date());
    }

    public static Date getStartTime(ExecutionContext execContext) {
        return (Date) execContext.getAttribute(START_TIME);
    }
}
