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
package org.milyn.javabean;

import org.milyn.javabean.decoders.StringDecoder;
import org.milyn.javabean.decoders.EnumDecoder;
import org.milyn.util.ClassUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

/**
 * Data decoder.
 * <p/>
 * A data decoder converts data (encoded in a String) to an Object of some form, determined by
 * the decoder implementation.
 * <p/>
 * There are a number of pre-installed decoders in the {@link org.milyn.javabean.decoders}
 * package.  DataDecoders are used in a numkber of places in Smooks:
 * <ul>
 *     <li>Component configuration value decoding.</li>
 *     <li>JavaBean Cartridge.  Used to decode the binding value.</li>
 * </ul>
 * Smooks can automatically select an appropriate DataDecoder for a given purpose (via the
 * {@link org.milyn.javabean.DataDecoder.Factory} factory class) if the decoder is:
 * <ol>
 *     <li>Annotated with the {@link DecodeType} annotation.</li>
 *     <li>Specified in the "/META-INF/data-decoders.inf" file on the classpath.  Obviously this file can
 *         exist on any number of classpath URIs.  If defining custom decoders, just add the
 *         "/META-INF/data-decoders.inf" file to your .jar file (or somewhere on the classpath).  Smooks will
 *         find all such files on the classpath.</li>
 * </ol>
 * Note how all the decoders in this package follow a well defined naming pattern of "<i>type</i>Decoder",
 * where type is the Java datatype to which the implementation decodes.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 * @see org.milyn.javabean.DataEncoder
 */
public interface DataDecoder extends Serializable {
    
    /**
     * Decode the supplied String data into a new Object data instance.
     *
     * @param data Data to be decoded.
     * @return Decoded data Object.
     * @throws DataDecodeException Error decoding data.
     */
    public Object decode(String data) throws DataDecodeException;

    /**
     * Factory method for constructing decoders defined in the "decoders" package.
     */
    public static class Factory {

        private static Log logger = LogFactory.getLog(DataDecoder.class);

        private static volatile Map<Class, Class<? extends DataDecoder>> installedDecoders;
							        
        public static DataDecoder create(final Class targetType) throws DataDecodeException {
            Class<? extends DataDecoder> decoderType = getInstance(targetType);

            if(decoderType != null) {
                if(decoderType == EnumDecoder.class) {
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
            if(installedDecoders == null) {
                loadInstalledDecoders();
            }

            if(targetType.isEnum()) {
                return EnumDecoder.class;
            } else {
                return installedDecoders.get(targetType);
            }
        }

        private synchronized static void loadInstalledDecoders() throws DataDecodeException {
            if(installedDecoders == null) {
                synchronized (Factory.class) {
                    if(installedDecoders == null) {
                        List<Class<DataDecoder>> decoders = ClassUtil.getClasses("META-INF/data-decoders.inf", DataDecoder.class);

                        if(decoders.isEmpty()) {
                            throw new DataDecodeException("Failed to find installed DataDecoders on clasaspath.");
                        }

                        installedDecoders = new HashMap<Class, Class<? extends DataDecoder>>();
                        for (Class decoder : decoders) {
                            DecodeType decoodeType = (DecodeType) decoder.getAnnotation(DecodeType.class);
                            if(decoodeType != null) {
                                Class[] types = decoodeType.value();

                                for (Class type : types) {
                                    if(installedDecoders.containsKey(type)) {
                                        logger.warn("More than one DataDecoder for type '" + type.getName() + "' is installed on the classpath.  You must manually configure decoding of this type, where required.");
                                        installedDecoders.put(type, null); // We don't remove, because we need to maintain a record of this!
                                    } else {
                                        installedDecoders.put(type, decoder);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        /**
         * Get the full set of installed decoders, keyed by the decode type.
         * @return The set of installed decoders, keyed by the decode type.
         */
        public static Map<Class, Class<? extends DataDecoder>> getInstalledDecoders() {
            if(installedDecoders == null) {
                loadInstalledDecoders();
            }
            return Collections.unmodifiableMap(installedDecoders);
        }

        /**
         * Attempt to construct a decoder instance from it's type alias based on the
         * packaging and naming convention used in the {@link org.milyn.javabean.decoders} package.
         *
         * @param typeAlias Decoder alias used to construct an instance by prefixing the
         *              alias with the "decoders" package and suffixing it with the word "Decoder".
         * @return The DateDecoder instance, or null if no such instance is available.
         * @throws DataDecodeException Failed to load alias decoder.
         */
        public static DataDecoder create(String typeAlias) throws DataDecodeException {
            if(typeAlias == null) {
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
