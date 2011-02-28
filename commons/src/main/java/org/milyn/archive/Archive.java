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
package org.milyn.archive;

import java.util.*;
import java.util.Map.Entry;
import java.util.jar.JarFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;
import java.io.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.milyn.assertion.AssertArgument;
import org.milyn.io.FileUtils;
import org.milyn.io.StreamUtils;

/**
 * Java Archive.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class Archive {

    private static Log logger = LogFactory.getLog(Archive.class);

	private String archiveName;
    private LinkedHashMap<String, byte[]> entries = new LinkedHashMap<String, byte[]>();

    /**
     * Public constructor.
     */
    public Archive() {
        this.archiveName = "Unknown";
    }

    /**
     * Public constructor.
     * @param archiveName The archive name of the deployment.
     */
    public Archive(String archiveName) {
        AssertArgument.isNotNull(archiveName, "archiveName");
        this.archiveName = archiveName;
    }

    /**
     * Public constructor.
     * @param archiveStream Archive stream containing initial archive entries.
     * @throws IOException Error reading from zip stream.
     */
    public Archive(ZipInputStream archiveStream) throws IOException {
        this.archiveName = "Unknown";
        addEntries(archiveStream);
    }

    /**
     * Public constructor.
     * @param archiveName The archive name of the deployment.
     * @param archiveStream Archive stream containing initial archive entries.
     * @throws IOException Error reading from zip stream.
     */
    public Archive(String archiveName, ZipInputStream archiveStream) throws IOException {
        AssertArgument.isNotNullAndNotEmpty(archiveName, "archiveName");
        this.archiveName = archiveName;
        addEntries(archiveStream);
    }

    /**
     * Get the name of the deployment associated with this archive.
     * @return The name of the deployment.
     */
    public String getArchiveName() {
        return archiveName;
    }

    /**
     * Add the supplied data as an entry in the deployment.
     *
     * @param path The target path of the entry when added to the archive.
     * @param data The data.
     * @return This archive instance.
     * @throws IOException Error reading from data stream.
     */
    public Archive addEntry(String path, InputStream data) throws IOException {
        AssertArgument.isNotNullAndNotEmpty(path, "path");
        AssertArgument.isNotNull(data, "data");

        try {
            entries.put(trimLeadingSlash(path.trim()), StreamUtils.readStream(data));
        } finally {
            try {
                data.close();
            } catch (IOException e) {
                logger.warn("Unable to close input stream for archive entry '" + path + "'.");
            }
        }

        return this;
    }

    /**
     * Add the supplied class as an entry in the deployment.
     * @param clazz The class to be added.
     * @return This archive instance.
     * @throws java.io.IOException Failed to read class from classpath.
     */
    public Archive addEntry(Class<?> clazz) throws IOException {
        AssertArgument.isNotNull(clazz, "clazz");
        String className = clazz.getName();

        className = className.replace('.', '/') + ".class";
        addClasspathResourceEntry(className, "/" + className);

        return this;
    }

    /**
     * Add the supplied data as an entry in the deployment.
     *
     * @param path The target path of the entry when added to the archive.
     * @param data The data.
     * @return This archive instance.
     */
    public Archive addEntry(String path, byte[] data) {
        AssertArgument.isNotNullAndNotEmpty(path, "path");
        AssertArgument.isNotNull(data, "data");

        entries.put(trimLeadingSlash(path.trim()), data);

        return this;
    }

    /**
     * Add an "empty" entry in the deployment.
     * <p/>
     * Equivalent to adding an empty folder.
     *
     * @param path The target path of the entry when added to the archive.
     * @return This archive instance.
     */
    public Archive addEntry(String path) {
        AssertArgument.isNotNullAndNotEmpty(path, "path");

        path = path.trim();
        if(path.endsWith("/")) {
            entries.put(trimLeadingSlash(path), null);
        } else {
            entries.put(trimLeadingSlash(path) + "/", null);
        }

        return this;
    }

    /**
     * Add the specified classpath resource as an entry in the deployment.
     *
     * @param path The target path of the entry when added to the archive.
     * @param resource The classpath resource.
     * @return This archive instance.
     * @throws java.io.IOException Failed to read resource from classpath.
     */
    public Archive addClasspathResourceEntry(String path, String resource) throws IOException {
        AssertArgument.isNotNull(path, "path");
        AssertArgument.isNotNull(resource, "resource");

        InputStream resourceStream = getClass().getResourceAsStream(resource);
        if(resourceStream == null) {
            throw new IOException("Classpath resource '" + resource + "' no found.");
        } else {
            addEntry(path, resourceStream);
        }

        return this;
    }

    /**
     * Add the supplied character data as an entry in the deployment.
     *
     * @param path The target path of the entry when added to the archive.
     * @param data The data.
     * @return This archive instance.
     */
    public Archive addEntry(String path, String data) {
        AssertArgument.isNotNullAndNotEmpty(path, "path");
        AssertArgument.isNotNull(data, "data");

        try {
            entries.put(trimLeadingSlash(path.trim()), data.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unexpected UnsupportedEncodingException exception for encoding 'UTF-8' when writing Archive entry from a StringBuilder instance.", e);
        }

        return this;
    }

    /**
     * Add the entries from the supplied {@link ZipInputStream} to this archive instance.
     * @param zipStream The zip stream.
     * @return This archive instance.
     * @throws IOException Error reading zip stream.
     */
    public Archive addEntries(ZipInputStream zipStream) throws IOException {
        AssertArgument.isNotNull(zipStream, "zipStream");
        
        try {
            ZipEntry zipEntry = zipStream.getNextEntry();
            ByteArrayOutputStream outByteStream = new ByteArrayOutputStream();
            byte[] byteReadBuffer = new byte[512];
            int byteReadCount;

            while(zipEntry != null) {
                while((byteReadCount = zipStream.read(byteReadBuffer)) != -1) {
                    outByteStream.write(byteReadBuffer, 0, byteReadCount);
                }
                entries.put(zipEntry.getName(), outByteStream.toByteArray());
                outByteStream.reset();

                zipEntry = zipStream.getNextEntry();
            }
        } finally {
            try {
                zipStream.close();
            } catch (IOException e) {
                logger.debug("Unexpected error closing EDI Mapping Model Zip stream.", e);
            }
        }

        return this;
    }

    /**
     * Get the archive entries.
     * <p/>
     * The returned map entries are ordered in line with the order in which they were added
     * to the archive.
     * 
     * @return An unmodifiable {@link Map} of the archive entries.
     */
    public Map<String, byte[]> getEntries() {
        return Collections.unmodifiableMap(entries);
    }

    /**
     * Get the name of the entry at the specified index in the archive.
     * @param index The index.
     * @return The entry name at that index.
     */
    public String getEntryName(int index) {
        Set<Map.Entry<String, byte[]>> entrySet = entries.entrySet();
        int i = 0;

        for (Map.Entry<String, byte[]> entry : entrySet) {
            if(i == index) {
                return entry.getKey();
            }

            i++;
        }

        throw new ArrayIndexOutOfBoundsException(index);
    }

    /**
     * Get the value of the entry at the specified index in the archive.
     * @param index The index.
     * @return The entry value at that index.
     */
    public byte[] getEntryValue(int index) {
        Set<Map.Entry<String, byte[]>> entrySet = entries.entrySet();
        int i = 0;

        for (Map.Entry<String, byte[]> entry : entrySet) {
            if(i == index) {
                return entry.getValue();
            }

            i++;
        }

        throw new ArrayIndexOutOfBoundsException(index);
    }

    /**
     * Create an archive of the specified name and containing entries
     * for the data contained in the streams supplied entries arg.
     * specifying the entry name and the value is a InputStream containing
     * the entry data.
     * @param archiveStream The archive output stream.
     * @throws java.io.IOException Write failure.
     */
    public void toOutputStream(ZipOutputStream archiveStream) throws IOException {
        AssertArgument.isNotNull(archiveStream, "archiveStream");

        try {
            writeEntriesToArchive(archiveStream);
        } finally {
            try {
                archiveStream.flush();
            } finally {
                try {
                    archiveStream.close();
                } catch (IOException e) {
                    logger.info("Unable to close archive output stream.");
                }
            }
        }
    }

    /**
     * Output the entries to the specified output folder on the file system.
     * @param outputFolder The target output folder.
     * @throws java.io.IOException Write failure.
     */
    public void toFileSystem(File outputFolder) throws IOException {
        AssertArgument.isNotNull(outputFolder, "outputFolder");

        if(outputFolder.isFile()) {
            throw new IOException("Cannot write Archive entries to '" + outputFolder.getAbsolutePath() + "'.  This is a normal file i.e. not a directory.");            
        }
        if(!outputFolder.exists()) {
            outputFolder.mkdirs();
        }

        Set<Map.Entry<String, byte[]>> entrySet = entries.entrySet();
        for (Map.Entry<String, byte[]> entry : entrySet) {
            byte[] fileBytes = entry.getValue();

            if(fileBytes == null) {
                fileBytes = new byte[0];
            }

            File entryFile = new File(outputFolder, entry.getKey());
            entryFile.getParentFile().mkdirs();
            FileUtils.writeFile(fileBytes, entryFile);
        }
    }

    /**
     * Create a {@link ZipInputStream} for the entries defined in this
     * archive.
     * @return The {@link ZipInputStream} for the entries in this archive.
     * @throws java.io.IOException Failed to create stream.
     */
    public ZipInputStream toInputStream() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        toOutputStream(new ZipOutputStream(outputStream));

        return new ZipInputStream(new ByteArrayInputStream(outputStream.toByteArray()));
    }

    private void writeEntriesToArchive(ZipOutputStream archiveStream) throws IOException {
        byte[] manifest = entries.get(JarFile.MANIFEST_NAME);

        // Always write the jar manifest as the first entry, if it exists...
        if(manifest != null) {
            writeEntry(JarFile.MANIFEST_NAME, manifest, archiveStream);
        }

        Set<Map.Entry<String, byte[]>> entrySet = entries.entrySet();
        for (Map.Entry<String, byte[]> entry : entrySet) {
            if(!entry.getKey().equals(JarFile.MANIFEST_NAME)) {
                writeEntry(entry.getKey(), entry.getValue(), archiveStream);
            }
        }
    }

    private void writeEntry(String entryName, byte[] entryValue, ZipOutputStream archiveStream) throws IOException {
        try {
            archiveStream.putNextEntry(new ZipEntry(entryName));
            if(entryValue != null) {
                archiveStream.write(entryValue);
            }
            archiveStream.closeEntry();
        } catch (Exception e) {
            throw (IOException) new IOException("Unable to create archive entry '" + entryName + "'.").initCause(e);
        }
    }

    private String trimLeadingSlash(String path) {
        StringBuilder builder = new StringBuilder(path);
        while(builder.length() > 0) {
            if(builder.charAt(0) == '/') {
                builder.deleteCharAt(0);
            } else {
                break;
            }
        }
        return builder.toString();
    }

    public Archive merge(Archive archive) {
        Map<String, byte[]> entriesToMerge = archive.getEntries();
        for (Entry<String, byte[]> entry : entriesToMerge.entrySet()) {
            addEntry(entry.getKey(), entry.getValue());
        }
        return this;
    }

    public boolean contains(String path)
    {
        return entries.containsKey(path);
    }
}