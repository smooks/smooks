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
package org.smooks.archive;

import org.smooks.SmooksException;
import org.smooks.assertion.AssertArgument;
import org.smooks.io.FileUtils;
import org.smooks.io.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Java Archive.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class Archive {

    private static final Logger LOGGER = LoggerFactory.getLogger(Archive.class);

	private String archiveName;
    private File tmpDir;
    private LinkedHashMap<String, File> entries = new LinkedHashMap<String, File>();

    /**
     * Public constructor.
     */
    public Archive() {
        this.archiveName = "Unknown";
        createTempDir();
    }

    /**
     * Public constructor.
     * @param archiveName The archive name of the deployment.
     */
    public Archive(String archiveName) {
        AssertArgument.isNotNull(archiveName, "archiveName");
        this.archiveName = archiveName;
        createTempDir();
    }

    /**
     * Public constructor.
     * @param archiveStream Archive stream containing initial archive entries.
     * @throws IOException Error reading from zip stream.
     */
    public Archive(ZipInputStream archiveStream) throws IOException {
        this.archiveName = "Unknown";
        createTempDir();
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
        createTempDir();
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
            byte[] dataBytes = StreamUtils.readStream(data);
            addEntry(trimLeadingSlash(path.trim()), dataBytes);
        } finally {
            try {
                data.close();
            } catch (IOException e) {
                LOGGER.warn("Unable to close input stream for archive entry '" + path + "'.");
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
     * @param data The data.  Pass null to create a directory.
     * @return This archive instance.
     */
    public Archive addEntry(String path, byte[] data) {
        AssertArgument.isNotNullAndNotEmpty(path, "path");

        File entryFile = new File(tmpDir, path);

        if (entryFile.exists()) {
            entryFile.delete();
        }

        entryFile.getParentFile().mkdirs();
        if (data == null) {
            entryFile.mkdir();
        } else {
            try {
                FileUtils.writeFile(data, entryFile);
            } catch (IOException e) {
                throw new IllegalStateException("Unexpected error writing Archive file '" + entryFile.getAbsolutePath() + "'.", e);
            }
        }

        entries.put(trimLeadingSlash(path.trim()), entryFile);

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
            addEntry(trimLeadingSlash(path.trim()), data.getBytes("UTF-8"));
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
                if (zipEntry.isDirectory()) {
                    addEntry(zipEntry.getName(), (byte[]) null);
                } else {
                    while((byteReadCount = zipStream.read(byteReadBuffer)) != -1) {
                        outByteStream.write(byteReadBuffer, 0, byteReadCount);
                    }
                    addEntry(zipEntry.getName(), outByteStream.toByteArray());
                    outByteStream.reset();
                }
                zipEntry = zipStream.getNextEntry();
            }
        } finally {
            try {
                zipStream.close();
            } catch (IOException e) {
                LOGGER.debug("Unexpected error closing EDI Mapping Model Zip stream.", e);
            }
        }

        return this;
    }

    /**
     * Remove the archive entry at the specified path.
     *
     * @param path The target path of the entry to be removed from the archive.
     * @return This archive instance.
     */
    public Archive removeEntry(String path) {
        entries.remove(path);
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
    public Map<String, File> getEntries() {
        return Collections.unmodifiableMap(entries);
    }

    /**
     * Get the name of the entry at the specified index in the archive.
     * @param index The index.
     * @return The entry name at that index.
     */
    public String getEntryName(int index) {
        Set<Map.Entry<String, File>> entrySet = entries.entrySet();
        int i = 0;

        for (Map.Entry<String, File> entry : entrySet) {
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
        Set<Map.Entry<String, File>> entrySet = entries.entrySet();
        int i = 0;

        for (Map.Entry<String, File> entry : entrySet) {
            if(i == index) {
                File entryFile = entry.getValue();

                if (entryFile != null) {
                    try {
                        return FileUtils.readFile(entryFile);
                    } catch (IOException e) {
                        throw new IllegalStateException("Unexpected error reading Archive file '" + entryFile.getAbsolutePath() + "'.", e);
                    }
                } else {
                    return null;
                }
            }

            i++;
        }

        throw new ArrayIndexOutOfBoundsException(index);
    }

    /**
     * Get an Archive entries bytes.
     * @param resName Entry resource name.
     * @return The bytes, or null if the entry is not in the Archive.
     */
    public byte[] getEntryBytes(String resName) {
        File entryFile = getEntry(resName);

        if (entryFile != null) {
            try {
                return FileUtils.readFile(entryFile);
            } catch (IOException e) {
                throw new IllegalStateException("Unexpected error reading Archive file '" + entryFile.getAbsolutePath() + "'.", e);
            }
        } else {
            return null;
        }
    }

    /**
     * Get an Archive entry file.
     * @param resName Entry resource name.
     * @return The entry File, or null if the entry is not in the Archive.
     */
    public File getEntry(String resName) {
        AssertArgument.isNotNullAndNotEmpty(resName, "resName");
        return entries.get(resName);
    }

    /**
     * Get an Archive entry resource URL.
     * @param resName Entry resource name.
     * @return The entry resource URL, or null if the entry is not in the Archive.
     */
    public URL getEntryURL(String resName) {
        File entry = getEntry(resName);

        if (entry != null) {
            try {
                return entry.toURI().toURL();
            } catch (MalformedURLException e) {
                throw new IllegalStateException("Unexpected error getting URL for Archive file '" + entry.getAbsolutePath() + "'.", e);
            }
        } else {
            return null;
        }
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
                    LOGGER.info("Unable to close archive output stream.");
                }
            }
        }
    }

    /**
     * Create an archive in the specified File containing entries
     * for the data contained in the streams supplied entries arg.
     * specifying the entry name and the value is a InputStream containing
     * the entry data.
     * @param file The archive file.
     * @throws java.io.IOException Write failure.
     */
    public void toFile(File file) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(file);

        try {
            toOutputStream(new ZipOutputStream(fileOutputStream));
        } finally {
            try {
                fileOutputStream.flush();
            } finally {
                fileOutputStream.close();
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

        Set<Map.Entry<String, File>> entrySet = entries.entrySet();
        for (Map.Entry<String, File> entry : entrySet) {
            File archEntryFile = entry.getValue();
            byte[] fileBytes;
            File entryFile = new File(outputFolder, entry.getKey());

            if (archEntryFile != null) {
                fileBytes = FileUtils.readFile(archEntryFile);
                entryFile.getParentFile().mkdirs();
                FileUtils.writeFile(fileBytes, entryFile);
            } else {
                entryFile.mkdirs();
            }
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
        File manifestFile = entries.get(JarFile.MANIFEST_NAME);

        // Always write the jar manifest as the first entry, if it exists...
        if(manifestFile != null) {
            byte[] manifest = FileUtils.readFile(manifestFile);
            writeEntry(JarFile.MANIFEST_NAME, manifest, archiveStream);
        }

        Set<Map.Entry<String, File>> entrySet = entries.entrySet();
        for (Map.Entry<String, File> entry : entrySet) {
            if(!entry.getKey().equals(JarFile.MANIFEST_NAME)) {
                File file = entry.getValue();

                if (file != null && !file.isDirectory()) {
                    writeEntry(entry.getKey(), FileUtils.readFile(file), archiveStream);
                } else {
                    writeEntry(entry.getKey(), null, archiveStream);
                }
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
        Map<String, File> entriesToMerge = archive.getEntries();
        for (Entry<String, File> entry : entriesToMerge.entrySet()) {
            File file = entry.getValue();

            if (file != null && !file.isDirectory()) {
                try {
                    addEntry(entry.getKey(), FileUtils.readFile(file));
                } catch (IOException e) {
                    throw new IllegalStateException("Unexpected error reading Archive file '" + file.getAbsolutePath() + "'.", e);
                }
            } else {
                addEntry(entry.getKey(), (byte[]) null);
            }
        }
        return this;
    }

    public boolean contains(String path)
    {
        return entries.containsKey(path);
    }

    private void createTempDir() {
        if (tmpDir == null) {
            try {
                File tmpFile = File.createTempFile("tmp", "tmp");

                tmpFile.delete();
                tmpDir = new File(tmpFile.getParentFile(), UUID.randomUUID().toString());
                DeleteOnExitHook.add(tmpDir);

            } catch (IOException e) {
                throw new SmooksException("Unable to crete temp directory for archive.", e);
            }
        }
    }

    private static class DeleteOnExitHook {

        static {
            Runtime.getRuntime().addShutdownHook(
                new Thread() {
                    public void run() {
                        deleteDirs();
                    }
                }
            );

            dirsToDelete = new CopyOnWriteArrayList<String>();
        }

        private static List<String> dirsToDelete;

        private static synchronized void add(File dir) {
            if (dirsToDelete == null) {
                throw new IllegalStateException("Shutdown in progress");
            }
            dirsToDelete.add(dir.getAbsolutePath());
        }

        private static synchronized void deleteDirs() {
            if (dirsToDelete == null) {
                throw new IllegalStateException("Shutdown in progress");
            }

            List<String> dirs = dirsToDelete;
            dirsToDelete = null;

            for (String dir : dirs) {
                try {
                    FileUtils.deleteDir(new File(dir));
                } catch (Exception e) {
                    // Ignore... keep deleting dirs...
                }
            }
        }
    }
}