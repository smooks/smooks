/*-
 * ========================LICENSE_START=================================
 * Smooks Core
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0, or
 * the GNU Lesser General Public License version 3.0 or later.
 * 
 * SPDX-License-Identifier: Apache-2.0 OR LGPL-3.0-or-later
 * 
 * ======================================================================
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ======================================================================
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * =========================LICENSE_END==================================
 */
package org.smooks.cdr.annotation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.assertion.AssertArgument;
import org.smooks.cdr.SmooksConfigurationException;
import org.smooks.cdr.SmooksResourceConfiguration;
import org.smooks.config.Configurable;
import org.smooks.container.ApplicationContext;
import org.smooks.datatype.DataTypeProviderFactoryLoader;
import org.smooks.datatype.factory.DataTypeProviderFactory;
import org.smooks.delivery.ContentHandler;
import org.smooks.delivery.Filter;
import org.smooks.delivery.sax.SAXToXMLWriter;
import org.smooks.delivery.sax.SAXVisitor;
import org.smooks.delivery.sax.annotation.StreamResultWriter;
import org.smooks.javabean.DataDecodeException;
import org.smooks.javabean.DataDecoder;
import org.smooks.util.ClassUtil;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Utility class for processing Smooks configuration annotations on an
 * object instance and applying resource configurations from the
 * supplied {@link SmooksResourceConfiguration}.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class Configurator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Configurator.class);

    /**
     * Configure the supplied {@link org.smooks.delivery.ContentHandler} instance using the supplied
     * {@link SmooksResourceConfiguration} and {@link org.smooks.container.ApplicationContext} instances.
     *
     * @param instance   The instance to be configured.
     * @param config     The configuration.
     * @param appContext Associated application context.
     * @return The configured ContentHandler instance.
     * @throws SmooksConfigurationException Invalid field annotations.
     */
    public static <U> U configure(U instance, SmooksResourceConfiguration config, ApplicationContext appContext) throws SmooksConfigurationException {
        AssertArgument.isNotNull(appContext, "appContext");

        // process the field annotations (@Inject)...
        processFieldContextAnnotation(instance, appContext);

        // Attach global parameters...
        config.attachGlobalParameters(appContext);

        // TODO: Add by-setter-method injection support for the app context

        return configure(instance, config);
    }

    /**
     * Configure the supplied {@link org.smooks.delivery.ContentHandler} instance using the supplied
     * {@link SmooksResourceConfiguration} instance.
     *
     * @param instance The instance to be configured.
     * @param config   The configuration.
     * @return The configured ContentHandler instance.
     * @throws SmooksConfigurationException Invalid field annotations.
     */
    public static <U> U configure(U instance, SmooksResourceConfiguration config) throws SmooksConfigurationException {
        AssertArgument.isNotNull(instance, "instance");
        AssertArgument.isNotNull(config, "config");

        // process the field annotations (@Inject)...
        processFieldConfigAnnotations(instance, config, true);

        // process the method annotations (@Inject)...
        processMethodConfigAnnotations(instance, config);

        // reflectively call the "setConfiguration" method, if defined...
        setConfiguration(instance, config);

        // process the @Initialise annotations...
        postConstruct(instance);

        return instance;
    }

    public static <U> void processFieldContextAnnotation(U instance, ApplicationContext appContext) {

        processFieldContextAnnotation(instance.getClass(), instance, appContext);

    }

    private static <U> void processFieldContextAnnotation(Class contentHandlerClass, U instance, ApplicationContext appContext) {
        Field[] fields = contentHandlerClass.getDeclaredFields();

        // Work back up the Inheritance tree first...
        Class superClass = contentHandlerClass.getSuperclass();
        if (superClass != null && ContentHandler.class.isAssignableFrom(superClass)) {
            processFieldContextAnnotation(superClass, instance, appContext);
        }

        for (Field field : fields) {
            Inject appContextAnnotation = field.getAnnotation(Inject.class);
            if (appContextAnnotation != null && ApplicationContext.class.isAssignableFrom(field.getType())) {
                try {
                    ClassUtil.setField(field, instance, appContext);
                } catch (IllegalAccessException e) {
                    throw new SmooksConfigurationException("Failed to set ApplicationContext value on '" + getLongMemberName(field) + "'.", e);
                }
            }
        }
    }

    public static <U> void processFieldConfigAnnotations(U instance, SmooksResourceConfiguration config, boolean includeInjectAnnotation) {
        Class contentHandlerClass = instance.getClass();
        processFieldConfigAnnotations(contentHandlerClass, instance, config);
        if (includeInjectAnnotation) {
            processFieldInjectAnnotations(contentHandlerClass, instance, config);
        }
        processStreamResultWriterAnnotations(instance, config);
    }

    private static <U> void processFieldInjectAnnotations(Class contentHandlerClass, U instance, SmooksResourceConfiguration config) {
        Field[] fields = contentHandlerClass.getDeclaredFields();

        // Work back up the Inheritance tree first...
        Class superClass = contentHandlerClass.getSuperclass();
        if (superClass != null) {
            processFieldInjectAnnotations(superClass, instance, config);
        }

        for (Field field : fields) {
            Inject injectAnnotation;

            injectAnnotation = field.getAnnotation(Inject.class);
            if (injectAnnotation != null) {
                inject(field.getAnnotation(Named.class), field, instance, config);
            }
        }
    }

    private static <U> void processFieldConfigAnnotations(Class contentHandlerClass, U instance, SmooksResourceConfiguration config) {
        Field[] fields = contentHandlerClass.getDeclaredFields();

        // Work back up the Inheritance tree first...
        Class superClass = contentHandlerClass.getSuperclass();
        if (superClass != null) {
            processFieldConfigAnnotations(superClass, instance, config);
        }

        for (Field field : fields) {
            Inject injectAnnotation = field.getAnnotation(Inject.class);

            if (injectAnnotation != null && SmooksResourceConfiguration.class.isAssignableFrom(field.getType())) {
                applyConfig(field, instance, config);
            }
        }
    }

    private static <U> void processStreamResultWriterAnnotations(U instance, SmooksResourceConfiguration config) {
        if (!(instance instanceof SAXVisitor)) {
            return;
        }

        List<Field> streamResFields = ClassUtil.getAnnotatedFields(instance.getClass(), StreamResultWriter.class);
        boolean encodeSpecialCharacters = Boolean.parseBoolean(config.getParameterValue(Filter.ENTITIES_REWRITE, String.class, "true"));

        for (Field streamResField : streamResFields) {
            // If already initialized, ignore...
            try {
                if (ClassUtil.getField(streamResField, instance) != null) {
                    continue;
                }
            } catch (IllegalAccessException e) {
                throw new SmooksConfigurationException("Unable to get property field value for '" + getLongMemberName(streamResField) + "'.", e);
            }

            Class<?> type = streamResField.getType();
            if (type == SAXToXMLWriter.class) {
                SAXToXMLWriter xmlWriter = new SAXToXMLWriter((SAXVisitor) instance, encodeSpecialCharacters);
                try {
                    ClassUtil.setField(streamResField, instance, xmlWriter);
                } catch (IllegalAccessException e) {
                    throw new SmooksConfigurationException("Unable to inject SAXToXMLWriter property field value for '" + getLongMemberName(streamResField) + "'.", e);
                }
            }
        }
    }

    private static <U> void checkPropertiesConfigured(Class contentHandlerClass, U instance) {
        Field[] fields = contentHandlerClass.getDeclaredFields();

        // Work back up the Inheritance tree first...
        Class superClass = contentHandlerClass.getSuperclass();
        if (superClass != null) {
            checkPropertiesConfigured(superClass, instance);
        }

        for (Field field : fields) {
            String fieldName = field.getName();
            Object fieldValue;

            try {
                fieldValue = ClassUtil.getField(field, instance);
            } catch (IllegalAccessException e) {
                throw new SmooksConfigurationException("Unable to get property field value for '" + getLongMemberName(field) + "'.", e);
            }

            if (fieldValue != null) {
                // It's set so no need to check anything....
                continue;
            }

            Inject injectAnnotation = field.getAnnotation(Inject.class);
            if (injectAnnotation == null) {
                // Check is there's a setter method for this property, with the @ConfigParam annotation
                // configured on it...
                String setterName = ClassUtil.toSetterName(fieldName);
                Method setterMethod = ClassUtil.getSetterMethod(setterName, contentHandlerClass, field.getType());

                if (setterMethod != null) {
                    injectAnnotation = setterMethod.getAnnotation(Inject.class);
                }
            }

            if (injectAnnotation != null) {
                throw new SmooksConfigurationException("Property '" + fieldName + "' not configured on class " + instance.getClass().getName() + "'.");
            }
        }
    }

    private static <U> void processMethodConfigAnnotations(U instance, SmooksResourceConfiguration config) {
        Method[] methods = instance.getClass().getMethods();

        for (Method method : methods) {
            Inject injectAnnotation = method.getAnnotation(Inject.class);
            if (injectAnnotation != null) {
                Class params[] = method.getParameterTypes();

                if (params.length == 1) {
                    inject(method.getParameters()[0].getAnnotation(Named.class), method, params[0], instance, config);
                } else {
                    throw new SmooksConfigurationException("Method '" + getLongMemberName(method) + "' defines a @Inject, yet it specifies more than a single paramater.");
                }
            }
        }
    }

    private static <U> void inject(Named namedAnnotation, Field field, U instance, SmooksResourceConfiguration config) throws SmooksConfigurationException {
        String name;
        Object paramValue;

        // Work out the property name, if not specified via the annotation....
        if (namedAnnotation == null) {
            // "name" not defined.  Use the field/method name...
            name = field.getName();
        } else {
            name = namedAnnotation.value();
        }

        paramValue = config.getParameterValue(name);

        if (paramValue == null) {
            try {
                paramValue = ClassUtil.getField(field, instance);
            } catch (IllegalAccessException e) {
                throw new SmooksConfigurationException(e);
            }
        }

        if (paramValue != null) {
            try {
                Object providedParamValue = createDecoder(paramValue.getClass(), field.getType(), null).createProvider(paramValue).get();
                if (field.getType().equals(Optional.class) && !(paramValue instanceof Optional)) {
                    setMember(field, instance, Optional.of(providedParamValue), name);
                } else {
                    setMember(field, instance, providedParamValue, name);
                }
            } catch (DataDecodeException e) {
                throw new SmooksConfigurationException("Failed to set parameter configuration value on '" + getLongMemberName(field) + "'.", e);
            }
        } else {
            if (field.getType().equals(Optional.class)) {
                setMember(field, instance, Optional.empty(), name);
            } else {
                throw new SmooksConfigurationException("<param> '" + name + "' not specified on resource configuration:\n" + config);
            }
        }
    }

    private static <U> void inject(Named namedAnnotation, Method method, Class type, U instance, SmooksResourceConfiguration config) throws SmooksConfigurationException {
        String name;
        Object paramValue;

        // Work out the property name, if not specified via the annotation....
        if (namedAnnotation == null) {
            // "name" not defined.  Use the method name...
            name = getPropertyName(method);
            if (name == null) {
                throw new SmooksConfigurationException("Unable to determine the property name associated with '" +
                        getLongMemberName(method) + "'. " +
                        "Setter methods that specify the @Inject annotation " +
                        "must either follow the Javabean naming convention ('setX' for property 'x'), or specify the " +
                        "property name via the 'name' parameter on the @Inject annotation.");
            }
        } else {
            name = namedAnnotation.value();
        }
        paramValue = config.getParameterValue(name);

        if (paramValue != null) {
            try {
                if (type.equals(Optional.class) && !(paramValue instanceof Optional)) {
                    Object providedParamValue = createDecoder(paramValue.getClass(), (Class<?>) ((ParameterizedType) method.getGenericParameterTypes()[0]).getActualTypeArguments()[0], null).createProvider(paramValue).get();
                    setMember(method, instance, Optional.of(providedParamValue), name);
                } else {
                    Object providedParamValue = createDecoder(paramValue.getClass(), type, null).createProvider(paramValue).get();
                    setMember(method, instance, providedParamValue, name);
                }
            } catch (DataDecodeException e) {
                throw new SmooksConfigurationException("Failed to set parameter configuration value on '" + getLongMemberName(method) + "'.", e);
            }
        } else {
            if (type.equals(Optional.class)) {
                setMember(method, instance, Optional.empty(), name);
            } else {
                throw new SmooksConfigurationException("<param> '" + name + "' not specified on resource configuration:\n" + config);
            }
        }
    }

    private static DataTypeProviderFactory createDecoder(Class sourceClass, Class targetClass, Class<? extends DataDecoder> decoderClass) {
        return DataTypeProviderFactoryLoader.getInstance().get(sourceClass, targetClass);
//        DataDecoder decoder;
//        if (decoderClass.isAssignableFrom(DataDecoder.class)) {
//            // No decoder specified via annotation.  Infer from the field type...
//            decoder = DataDecoder.Factory.create(type);
//            if (decoder == null) {
//                throw new SmooksConfigurationException("ContentHandler class member '" + getLongMemberName(member) + "' must define a decoder through it's @ConfigParam annotation.  Unable to automatically determine DataDecoder from member type.");
//            }
//        } else {
//            // Decoder specified on annotation...
//            try {
//                decoder = decoderClass.newInstance();
//            } catch (InstantiationException e) {
//                throw new SmooksConfigurationException("Failed to create DataDecoder instance from class '" + decoderClass.getName() + "'.  Make sure the DataDecoder implementation has a public default constructor.", e);
//            } catch (IllegalAccessException e) {
//                throw new SmooksConfigurationException("Failed to create DataDecoder instance from class '" + decoderClass.getName() + "'.  Make sure the DataDecoder implementation has a public default constructor.", e);
//            }
//        }
//        return decoder;
    }
    
    private static <U> void applyConfig(Field field, U instance, SmooksResourceConfiguration config) {
        try {
            ClassUtil.setField(field, instance, config);
        } catch (IllegalAccessException e) {
            throw new SmooksConfigurationException("Failed to set paramater configuration value on '" + getLongMemberName(field) + "'.", e);
        }
    }

    private static <U> void setConfiguration(U instance, SmooksResourceConfiguration config) {
        if (instance instanceof Configurable) {
            ((Configurable) instance).setConfiguration(config.toProperties());
        } else {
            try {
                Method setConfigurationMethod = instance.getClass().getMethod("setConfiguration", SmooksResourceConfiguration.class);

                setConfigurationMethod.invoke(instance, config);
            } catch (NoSuchMethodException e) {
                // That's fine
            } catch (IllegalAccessException e) {
                throw new SmooksConfigurationException("Error invoking 'setConfiguration' method on class '" + instance.getClass().getName() + "'.  This class must be public.  Alternatively, use the @Config annotation on a class field.", e);
            } catch (InvocationTargetException e) {
                if (e.getTargetException() instanceof SmooksConfigurationException) {
                    throw (SmooksConfigurationException) e.getTargetException();
                } else {
                    Throwable cause = e.getTargetException();
                    throw new SmooksConfigurationException("Error invoking 'setConfiguration' method on class '" + instance.getClass().getName() + "'.", (cause != null ? cause : e));
                }
            }
        }
    }

    private static String getLongMemberName(Member field) {
        return field.getDeclaringClass().getName() + "#" + field.getName();
    }

    private static <U> void setMember(Member member, U instance, Object value, String name) {
        try {
            if (member instanceof Field) {
                if (((Field) member).getType().isEnum()) {
                    Enum valueEnum;
                    try {
                        valueEnum = Enum.valueOf((Class) ((Field) member).getType(), value.toString());
                    } catch (IllegalArgumentException e) {
                        Object[] enumConstants = ((Field) member).getType().getEnumConstants();
                        throw new SmooksConfigurationException("Value '" + value.toString() + "' for parameter '" + name + "' is invalid.  Valid choices for this parameter are: " + Arrays.stream(enumConstants).map(c -> ((Enum) c).name()).collect(Collectors.toList()));
                    }
                    ClassUtil.setField((Field) member, instance, valueEnum);
                } else {
                    ClassUtil.setField((Field) member, instance, value);
                }
            } else {
                try {
                    ((Method) member).invoke(instance, value);
                } catch (InvocationTargetException e) {
                    throw new SmooksConfigurationException("Failed to set paramater configuration value on '" + getLongMemberName(member) + "'.", e.getTargetException());
                }
            }
        } catch (IllegalAccessException e) {
            throw new SmooksConfigurationException("Failed to set paramater configuration value on '" + getLongMemberName(member) + "'.", e);
        }
    }

    public static <U> void postConstruct(U instance) {
        checkPropertiesConfigured(instance.getClass(), instance);
        invoke(instance, PostConstruct.class);
    }

    public static <U> void preDestroy(U instance) {
        invoke(instance, PreDestroy.class);
    }

    private static <U> void invoke(U instance, Class<? extends Annotation> annotation) {
        Method[] methods = instance.getClass().getMethods();

        for (Method method : methods) {
            if (method.getAnnotation(annotation) != null) {
                if (method.getParameterTypes().length == 0) {
                    try {
                        method.invoke(instance);
                    } catch (IllegalAccessException e) {
                        throw new SmooksConfigurationException("Error invoking @" + annotation.getSimpleName() + " method '" + method.getName() + "' on class '" + instance.getClass().getName() + "'.", e);
                    } catch (InvocationTargetException e) {
                        throw new SmooksConfigurationException("Error invoking @" + annotation.getSimpleName() + " method '" + method.getName() + "' on class '" + instance.getClass().getName() + "'.", e.getTargetException());
                    }
                } else {
                    LOGGER.warn("Method '" + getLongMemberName(method) + "' defines an @" + annotation.getSimpleName() + " annotation on a paramaterized method.  This is not allowed!");
                }
            }
        }
    }

    private static String getPropertyName(Method method) {
        if (!method.getName().startsWith("set")) {
            return null;
        }

        StringBuffer methodName = new StringBuffer(method.getName());

        if (methodName.length() < 4) {
            return null;
        }

        methodName.delete(0, 3);
        methodName.setCharAt(0, Character.toLowerCase(methodName.charAt(0)));

        return methodName.toString();
    }
}
