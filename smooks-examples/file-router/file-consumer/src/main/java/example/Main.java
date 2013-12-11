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
package example;

import org.milyn.commons.io.StreamUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple file consumer.
 * <p/>
 * Eats the files produced by the Smooks file splitter.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class Main {

    public static void main(String[] args) throws Exception {
        File fileDir = new File("../splitter-router/target/orders");
        SplitFilenameFilter filter = new SplitFilenameFilter();

        fileDir.mkdirs();
        (new File(fileDir, "order-332.lst")).delete();
        (new File(fileDir, "order-332.lst")).deleteOnExit();

        System.out.println("Started!");
        System.out.println("Waiting...\n");
        while (true) {
            File[] files = fileDir.listFiles(filter);

            if (files.length > 0) {
                for (File file : files) {
                    if (file.getName().endsWith(".xml")) {
                        System.out.println("Consuming File: " + file.getName());
                        System.out.println(new String(StreamUtils.readFile(file)));
                        System.out.println("\n");
                        file.delete();
                    }

                    Thread.sleep(500);
                }
                System.out.println("Waiting...");
            }

            Thread.sleep(1000);
        }
    }

    public static class SplitFilenameFilter implements FileFilter {

        private Pattern regexPattern = Pattern.compile("order-.*.xml");

        public boolean accept(File file) {
            Matcher matcher = regexPattern.matcher(file.getName());
            return matcher.matches();
        }
    }
}