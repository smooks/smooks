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
package org.smooks.javabean;

import org.smooks.javabean.decoders.EnumDecoder;
import org.smooks.javabean.decoders.StringDecoder;
import org.smooks.util.ClassUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data decoder.
 * <p/>
 * A data decoder converts data (encoded in a String) to an Object of some form, determined by
 * the decoder implementation.
 * <p/>
 * There are a number of pre-installed decoders in the {@link org.smooks.javabean.decoders}
 * package.  DataDecoders are used in a numkber of places in Smooks:
 * <ul>
 * <li>Component configuration value decoding.</li>
 * <li>JavaBean Cartridge.  Used to decode the binding value.</li>
 * </ul>
 * Smooks can automatically select an appropriate DataDecoder for a given purpose (via the
 * {@link org.smooks.javabean.DataDecoder.Factory} factory class) if the decoder is:
 * <ol>
 * <li>Annotated with the {@link DecodeType} annotation.</li>
 * <li>Specified in the "/META-INF/data-decoders.inf" file on the classpath.  Obviously this file can
 * exist on any number of classpath URIs.  If defining custom decoders, just add the
 * "/META-INF/data-decoders.inf" file to your .jar file (or somewhere on the classpath).  Smooks will
 * find all such files on the classpath.</li>
 * </ol>
 * Note how all the decoders in this package follow a well defined naming pattern of "<i>type</i>Decoder",
 * where type is the Java datatype to which the implementation decodes.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 * @see org.smooks.javabean.DataEncoder
 */
public interface DataDecoder extends Serializable {
    /**
     * Decode the supplied String data into a new Object data instance.
     *
     * @param data Data to be decoded.
     * @return Decoded data Object.
     * @throws DataDecodeException Error decoding data.
     */
    Object decode(String data) throws DataDecodeException;

    /**
     * Factory method for constructing decoders defined in the "decoders" package.
     */
    class Factory {
        private static final Logger LOGGER = LoggerFactory.getLogger(DataDecoder.class);

        private static Map<Class, Class<? extends DataDecoder>> installedDecoders;

        public static DataDecoder create(final Class targetType) throws DataDecodeException {
            Class<? extends DataDecoder> decoderType = getInstance(targetType);

            if (decoderType != null) {
                if (decoderType == EnumDecoder.class) {
                    EnumDecoder decoder = new EnumDecoder();
                    decoder.setEnumType(targetType);
                    return decoder;
                } else {
                    return newInstance(decoderType);
                }
            }

            return null;
        }

        public static Class<? extends DataDecoder> getInstance(Class targetType) {
            loadInstalledDecoders();

            if (targetType.isEnum()) {
                return EnumDecoder.class;
            } else {
                return installedDecoders.get(targetType);
            }
        }

        private synchronized static void loadInstalledDecoders() throws DataDecodeException {
            if (installedDecoders == null) {
                // Attempt to find configured decoders.
                final List<Class<DataDecoder>> decoders = ClassUtil.getClasses("META-INF/data-decoders.inf", DataDecoder.class);

                if (decoders.isEmpty()) {
                    throw new DataDecodeException("Failed to find installed DataDecoders on classpath.");
                }

                // Prepare to initialize decoders.
                final Map<Class, Class<? extends DataDecoder>> loadedDecoders = new HashMap<Class, Class<? extends DataDecoder>>();
                for (final Class<DataDecoder> decoder : decoders) {
                    final DecodeType decodeType = decoder.getAnnotation(DecodeType.class);
                    if (decodeType != null) {
                        for (final Class type : decodeType.value()) {
                            if (loadedDecoders.containsKey(type)) {
                                LOGGER.warn("More than one DataDecoder for type '" + type.getName() + "' is installed on the classpath.  You must manually configure decoding of this type, where required.");
                                loadedDecoders.put(type, null); // We don't remove, because we need to maintain a record of this!
                            } else {
                                loadedDecoders.put(type, decoder);
                            }
                        }
                    }
                }

                // Save the loaded decoders so that they can be reused.
                installedDecoders = loadedDecoders;
            }
        }

        /**
         * Get the full set of installed decoders, keyed by the decode type.
         *
         * @return The set of installed decoders, keyed by the decode type.
         */
        public static Map<Class, Class<? extends DataDecoder>> getInstalledDecoders() {
            loadInstalledDecoders();

            return Collections.unmodifiableMap(installedDecoders);
        }

        /**
         * Attempt to construct a decoder instance from it's type alias based on the
         * packaging and naming convention used in the {@link org.smooks.javabean.decoders} package.
         *
         * @param typeAlias Decoder alias used to construct an instance by prefixing the
         *                  alias with the "decoders" package and suffixing it with the word "Decoder".
         * @return The DateDecoder instance, or null if no such instance is available.
         * @throws DataDecodeException Failed to load alias decoder.
         */
        public static DataDecoder create(String typeAlias) throws DataDecodeException {
            if (typeAlias == null) {
                return new StringDecoder();
            }

            String className = StringDecoder.class.getPackage().getName() + "." + typeAlias + "Decoder";

            try {
                Class decoderType = ClassUtil.forName(className, DataDecoder.class);
                return newInstance(decoderType);
            } catch (ClassNotFoundException e) {
                try {
                    Class decoderType = ClassUtil.forName(typeAlias, DataDecoder.class);
                    return newInstance(decoderType);
                } catch (ClassNotFoundException e1) {
                    throw new DataDecodeException("DataDecoder Class '" + className + "' is not available on the classpath.");
                }
            }
        }

        private static DataDecoder newInstance(Class decoderType) throws DataDecodeException {
            try {
                return (DataDecoder) decoderType.newInstance();
            } catch (ClassCastException e) {
                throw new DataDecodeException("Class '" + decoderType.getName() + "' is not a valid DataDecoder.  It doesn't implement " + DataDecoder.class.getName());
            } catch (IllegalAccessException e) {
                throw new DataDecodeException("Failed to load DataDecoder Class '" + decoderType.getName() + "'.", e);
            } catch (InstantiationException e) {
                throw new DataDecodeException("Failed to load DataDecoder Class '" + decoderType.getName() + "'.", e);
            }
        }
    }
}
