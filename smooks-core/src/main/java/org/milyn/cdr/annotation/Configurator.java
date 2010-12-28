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
package org.milyn.cdr.annotation;

import org.apache.commons.logging.*;
import org.milyn.assertion.*;
import org.milyn.cdr.*;
import org.milyn.container.*;
import org.milyn.delivery.*;
import org.milyn.delivery.annotation.*;
import org.milyn.delivery.sax.SAXToXMLWriter;
import org.milyn.delivery.sax.SAXVisitor;
import org.milyn.delivery.sax.annotation.StreamResultWriter;
import org.milyn.javabean.*;
import org.milyn.config.Configurable;
import org.milyn.util.ClassUtil;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * Utility class for processing configuration annotations on a
 * {@link org.milyn.delivery.ContentHandler} instance and applying resource configurations from the
 * supplied {@link SmooksResourceConfiguration}.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class Configurator {

    private static Log logger = LogFactory.getLog(Configurator.class);

    /**
     * Configure the supplied {@link org.milyn.delivery.ContentHandler} instance using the supplied
     * {@link SmooksResourceConfiguration} and {@link org.milyn.container.ApplicationContext} instances.
     * @param instance The instance to be configured.
     * @param config The configuration.
     * @param appContext Associated application context.
     * @return The configured ContentHandler instance.
     * @throws SmooksConfigurationException Invalid field annotations.
     */
    public static <U> U configure(U instance, SmooksResourceConfiguration config, ApplicationContext appContext) throws SmooksConfigurationException {
        AssertArgument.isNotNull(appContext, "appContext");

        // process the field annotations (@AppContext)...
        processFieldContextAnnotation(instance, appContext);

        // Attach global parameters...
        config.attachGlobalParameters(appContext);

        // TODO: Add by-setter-method injection support for the app context

        return configure(instance, config);
    }

    /**
     * Configure the supplied {@link org.milyn.delivery.ContentHandler} instance using the supplied
     * {@link SmooksResourceConfiguration} isntance.
     * @param instance The instance to be configured.
     * @param config The configuration.
     * @return The configured ContentHandler instance.
     * @throws SmooksConfigurationException Invalid field annotations.
     */
    public static <U> U configure(U instance, SmooksResourceConfiguration config) throws SmooksConfigurationException {
        AssertArgument.isNotNull(instance, "instance");
        AssertArgument.isNotNull(config, "config");

        // process the field annotations (@ConfigParam and @Config)...
        processFieldConfigAnnotations(instance, config, true);

        // process the method annotations (@ConfigParam)...
        processMethodConfigAnnotations(instance, config);

        // reflectively call the "setConfiguration" method, if defined...
        setConfiguration(instance, config);

        // process the @Initialise annotations...
        initialise(instance);

        return instance;
    }

    public static <U> void processFieldContextAnnotation(U instance, ApplicationContext appContext) {
    	
        processFieldContextAnnotation(instance.getClass(), instance, appContext);
        
    }
    
    private static <U> void processFieldContextAnnotation(Class contentHandlerClass, U instance, ApplicationContext appContext) {
    	Field[] fields = contentHandlerClass.getDeclaredFields();
    	
    	// Work back up the Inheritance tree first...
        Class superClass = contentHandlerClass.getSuperclass();
        if(superClass != null && ContentHandler.class.isAssignableFrom(superClass)) {
        	processFieldContextAnnotation(superClass, instance, appContext);
        }    	
    	
    	for (Field field : fields) {
            AppContext appContextAnnotation = field.getAnnotation(AppContext.class);
            if(appContextAnnotation != null) {
                try {
                    ClassUtil.setField(field, instance, appContext);
                } catch (IllegalAccessException e) {
                    throw new SmooksConfigurationException("Failed to set ApplicationContext value on '" + getLongMemberName(field) + "'.", e);
                }
            }
        }
    }

    public static <U> void processFieldConfigAnnotations(U instance, SmooksResourceConfiguration config, boolean includeConfigParams) {
        Class contentHandlerClass = instance.getClass();
        if(includeConfigParams) {
            processFieldConfigParamAnnotations(contentHandlerClass, instance, config);
        }
        processFieldConfigAnnotations(contentHandlerClass, instance, config);
        processStreamResultWriterAnnotations(instance, config);
    }
    
    private static <U> void processFieldConfigParamAnnotations(Class contentHandlerClass, U instance, SmooksResourceConfiguration config) {
        Field[] fields = contentHandlerClass.getDeclaredFields();

        // Work back up the Inheritance tree first...
        Class superClass = contentHandlerClass.getSuperclass();
        if(superClass != null) {
            processFieldConfigParamAnnotations(superClass, instance, config);
        }

        for (Field field : fields) {
            ConfigParam configParamAnnotation = null;
            
            configParamAnnotation = field.getAnnotation(ConfigParam.class);
            if(configParamAnnotation != null) {
                applyConfigParam(configParamAnnotation, field, field.getType(), instance, config);
            }
        }
    }

    private static <U> void processFieldConfigAnnotations(Class contentHandlerClass, U instance, SmooksResourceConfiguration config) {
        Field[] fields = contentHandlerClass.getDeclaredFields();

        // Work back up the Inheritance tree first...
        Class superClass = contentHandlerClass.getSuperclass();
        if(superClass != null) {
            processFieldConfigAnnotations(superClass, instance, config);
        }

        for (Field field : fields) {
            ConfigParam configParamAnnotation = field.getAnnotation(ConfigParam.class);
            Config configAnnotation = field.getAnnotation(Config.class);
            
            if(configAnnotation != null) {
                if(configParamAnnotation != null) {
                    throw new SmooksConfigurationException("Invalid Smooks configuration annotations on Field '" + getLongMemberName(field) + "'.  Field should not specify both @ConfigParam and @Config annotations.");
                }
                applyConfig(field, instance, config);
            }
        }
    }

    private static <U> void processStreamResultWriterAnnotations(U instance, SmooksResourceConfiguration config) {
    	if(!(instance instanceof SAXVisitor)) {
    		return;
    	}
    	
    	List<Field> streamResFields = ClassUtil.getAnnotatedFields(instance.getClass(), StreamResultWriter.class);
    	boolean encodeSpecialCharacters = config.getBoolParameter(Filter.ENTITIES_REWRITE, true);
    	
    	for(Field streamResField : streamResFields) {
    		// If already initialized, ignore...
    		try {
	    		if(ClassUtil.getField(streamResField, instance) != null) {
	    			continue;
	    		}
	        } catch (IllegalAccessException e) {
	            throw new SmooksConfigurationException("Unable to get property field value for '" + getLongMemberName(streamResField) + "'.", e);
	        }
    		
    		Class<?> type = streamResField.getType();
    		if(type == SAXToXMLWriter.class) {
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
        if(superClass != null) {
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

            if(fieldValue != null) {
                // It's set so no need to check anything....
                continue;
            }

            ConfigParam configParamAnnotation = field.getAnnotation(ConfigParam.class);
            if(configParamAnnotation == null) {
                // Check is there's a setter method for this property, with the @ConfigParam annotation
                // configured on it...
                String setterName = ClassUtil.toSetterName(fieldName);
                Method setterMethod = ClassUtil.getSetterMethod(setterName, contentHandlerClass, field.getType());

                if(setterMethod != null) {
                    configParamAnnotation = setterMethod.getAnnotation(ConfigParam.class);
                }
            }

            if(configParamAnnotation != null) {
                // If it's required and not configured with a default value of AnnotationConstants.NULL_STRING, error... 
                if(configParamAnnotation.use() == ConfigParam.Use.REQUIRED) {
                    // Property configured and it's required....

                    String defaultVal = configParamAnnotation.defaultVal();

                    // If there is no default (i.e. it's "UNASSIGNED"), we have an error...
                    if(defaultVal.equals(AnnotationConstants.UNASSIGNED)) {
                        throw new SmooksConfigurationException("Property '" + fieldName + "' not configured on class " + instance.getClass().getName() + "'.");
                    }

                    // If the default is "NULL", just continue...
                    if(defaultVal.equals(AnnotationConstants.NULL_STRING)) {
                        continue;
                    }

                    // Decode the default and set it on the property...
                    Class<? extends DataDecoder> decoderClass = configParamAnnotation.decoder();
                    DataDecoder decoder = createDecoder(field, field.getType(), decoderClass);

                    try {
                        ClassUtil.setField(field, instance, decoder.decode(defaultVal));
                    } catch (IllegalAccessException e) {
                        throw new SmooksConfigurationException("Unable to set property field value for '" + getLongMemberName(field) + "'.", e);
                    }
                }
            }
        }
    }

    private static <U> void processMethodConfigAnnotations(U instance, SmooksResourceConfiguration config) {
        Method[] methods = instance.getClass().getMethods();

        for (Method method : methods) {
            ConfigParam configParamAnnotation = method.getAnnotation(ConfigParam.class);
            if(configParamAnnotation != null) {
                Class params[] = method.getParameterTypes();

                if(params.length == 1) {
                    applyConfigParam(configParamAnnotation, method, params[0], instance, config);
                } else {
                    throw new SmooksConfigurationException("Method '" + getLongMemberName(method) + "' defines a @ConfigParam, yet it specifies more than a single paramater.");
                }
            }
        }
    }

    private static <U> void applyConfigParam(ConfigParam configParam, Member member, Class type, U instance, SmooksResourceConfiguration config) throws SmooksConfigurationException {
        String name = configParam.name();
        String paramValue;

        // Work out the property name, if not specified via the annotation....
        if(AnnotationConstants.NULL_STRING.equals(name)) {
            // "name" not defined.  Use the field/method name...
            if(member instanceof Method) {
                name = getPropertyName((Method)member);
                if(name == null) {
                    throw new SmooksConfigurationException("Unable to determine the property name associated with '" +
                            getLongMemberName(member)+ "'. " +
                            "Setter methods that specify the @ConfigParam annotation " +
                            "must either follow the Javabean naming convention ('setX' for propert 'x'), or specify the " +
                            "propery name via the 'name' parameter on the @ConfigParam annotation.");
                }
            } else {
                name = member.getName();
            }
        }
        paramValue = config.getStringParameter(name);

        if(paramValue == null) {
            paramValue = configParam.defaultVal();
            if(AnnotationConstants.NULL_STRING.equals(paramValue)) {
                // A null default was assigned...
                String[] choices = configParam.choice();
                assertValidChoice(choices, name, AnnotationConstants.NULL_STRING);
                setMember(member, instance, null);
                return;
            } else if(AnnotationConstants.UNASSIGNED.equals(paramValue)) {
                // No default was assigned...
                paramValue = null;
            }
        }

        if(paramValue != null) {
            String[] choices = configParam.choice();
            Class<? extends DataDecoder> decoderClass;
            DataDecoder decoder;

            assertValidChoice(choices, name, paramValue);

            decoderClass = configParam.decoder();
            decoder = createDecoder(member, type, decoderClass);

            try {
                setMember(member, instance, decoder.decode(paramValue));
            } catch (DataDecodeException e) {
                throw new SmooksConfigurationException("Failed to set paramater configuration value on '" + getLongMemberName(member) + "'.", e);
            }
        } else if(configParam.use() == ConfigParam.Use.REQUIRED) {
            throw new SmooksConfigurationException("<param> '" + name + "' not specified on resource configuration:\n" + config);
        }
    }

    private static <U> DataDecoder createDecoder(Member member, Class type, Class<? extends DataDecoder> decoderClass) {
        DataDecoder decoder;
        if(decoderClass.isAssignableFrom(DataDecoder.class)) {
            // No decoder specified via annotation.  Infer from the field type...
            decoder = DataDecoder.Factory.create(type);
            if(decoder == null) {
                throw new SmooksConfigurationException("ContentHandler class member '" + getLongMemberName(member) + "' must define a decoder through it's @ConfigParam annotation.  Unable to automatically determine DataDecoder from member type.");
            }
        } else {
            // Decoder specified on annotation...
            try {
                decoder = decoderClass.newInstance();
            } catch (InstantiationException e) {
                throw new SmooksConfigurationException("Failed to create DataDecoder instance from class '" + decoderClass.getName() + "'.  Make sure the DataDecoder implementation has a public default constructor.", e);
            } catch (IllegalAccessException e) {
                throw new SmooksConfigurationException("Failed to create DataDecoder instance from class '" + decoderClass.getName() + "'.  Make sure the DataDecoder implementation has a public default constructor.", e);
            }
        }
        return decoder;
    }

    private static void assertValidChoice(String[] choices, String name, String paramValue) throws SmooksConfigurationException {
        if(choices == null || choices.length == 0) {
            throw new RuntimeException("Unexpected annotation default choice value.  Should not be null or empty.  Code may have changed incompatibly.");
        } else if(choices.length == 1 && AnnotationConstants.NULL_STRING.equals(choices[0])) {
            // A choice wasn't specified on the paramater config.
            return;
        } else {
            // A choice was specified. Check it against the value...
            for (String choice : choices) {
                if(paramValue.equals(choice)) {
                    return;
                }
            }
        }

        throw new SmooksConfigurationException("Value '" + paramValue + "' for paramater '" + name + "' is invalid.  Valid choices for this paramater are: " + Arrays.asList(choices));
    }

    private static <U> void applyConfig(Field field, U instance, SmooksResourceConfiguration config) {
        try {
            ClassUtil.setField(field, instance, config);
        } catch (IllegalAccessException e) {
            throw new SmooksConfigurationException("Failed to set paramater configuration value on '" + getLongMemberName(field) + "'.", e);
        }
    }

    private static <U> void setConfiguration(U instance, SmooksResourceConfiguration config) {
        if(instance instanceof Configurable) {
            ((Configurable)instance).setConfiguration(config.toProperties());
        } else {
            try {
                Method setConfigurationMethod = instance.getClass().getMethod("setConfiguration", SmooksResourceConfiguration.class);

                setConfigurationMethod.invoke(instance, config);
            } catch (NoSuchMethodException e) {
                // That's fine
            } catch (IllegalAccessException e) {
                throw new SmooksConfigurationException("Error invoking 'setConfiguration' method on class '" + instance.getClass().getName() + "'.  This class must be public.  Alternatively, use the @Config annotation on a class field.", e);
            } catch (InvocationTargetException e) {
                if(e.getTargetException() instanceof SmooksConfigurationException) {
                    throw (SmooksConfigurationException)e.getTargetException();
                } else {
                    Throwable cause = e.getTargetException();
                    throw new SmooksConfigurationException("Error invoking 'setConfiguration' method on class '" + instance.getClass().getName() + "'.", (cause != null?cause:e));
                }
            }
        }
    }

    private static String getLongMemberName(Member field) {
        return field.getDeclaringClass().getName() + "#" + field.getName();
    }

    private static <U> void setMember(Member member, U instance, Object value) {
        try {
            if(member instanceof Field) {
                ClassUtil.setField((Field)member, instance, value);
            } else {
                try {
                    setMethod((Method)member, instance, value);
                } catch (InvocationTargetException e) {
                    throw new SmooksConfigurationException("Failed to set paramater configuration value on '" + getLongMemberName(member) + "'.", e.getTargetException());
                }
            }
        } catch (IllegalAccessException e) {
            throw new SmooksConfigurationException("Failed to set paramater configuration value on '" + getLongMemberName(member) + "'.", e);
        }
    }

    private static <U> void setMethod(Method method, U instance, Object value) throws IllegalAccessException, InvocationTargetException {
        method.invoke(instance, value);
    }

    public static <U> void initialise(U instance) {
        checkPropertiesConfigured(instance.getClass(), instance);
        invoke(instance, Initialize.class);
    }

    public static <U> void uninitialise(U instance) {
        invoke(instance, Uninitialize.class);
    }

    private static <U> void invoke(U instance, Class<? extends Annotation> annotation) {
        Method[] methods = instance.getClass().getMethods();

        for (Method method : methods) {
            if(method.getAnnotation(annotation) != null) {
                if(method.getParameterTypes().length == 0) {
                    try {
                        method.invoke(instance);
                    } catch (IllegalAccessException e) {
                        throw new SmooksConfigurationException("Error invoking @" + annotation.getSimpleName() + " method '" + method.getName() + "' on class '" + instance.getClass().getName() + "'.", e);
                    } catch (InvocationTargetException e) {
                        throw new SmooksConfigurationException("Error invoking @" + annotation.getSimpleName() + " method '" + method.getName() + "' on class '" + instance.getClass().getName() + "'.", e.getTargetException());
                    }
                } else {
                    logger.warn("Method '" + getLongMemberName(method) + "' defines an @" + annotation.getSimpleName() + " annotation on a paramaterized method.  This is not allowed!");
                }
            }
        }
    }

    private static String getPropertyName(Method method) {
        if(!method.getName().startsWith("set")) {
            return null;
        }

        StringBuffer methodName = new StringBuffer(method.getName());

        if(methodName.length() < 4) {
            return null;
        }

        methodName.delete(0, 3);
        methodName.setCharAt(0, Character.toLowerCase(methodName.charAt(0)));

        return methodName.toString();
    }
}
