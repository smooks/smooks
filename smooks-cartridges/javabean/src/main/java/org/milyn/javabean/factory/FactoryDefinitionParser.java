/*
 * Milyn - Copyright (C) 2006 - 2010
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License (version 2.1) as published by the Free Software
 *  Foundation.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 *  See the GNU Lesser General Public License for more details:
 *  http://www.gnu.org/licenses/lgpl.txt
 */
package org.milyn.javabean.factory;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.milyn.container.ApplicationContext;
import org.milyn.javabean.DataDecodeException;
import org.milyn.util.ClassUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * A factory definition string is an expression that instructs how to create a certain object.
 * A {@link FactoryDefinitionParser} can parse the factory definition and create a {@link Factory} object which
 * can create the object according to the definition.
 *
 * A {@link FactoryDefinitionParser} must have a public argumentless constructor. The {@link FactoryDefinitionParser}
 * must be thread safe. The parse method can be called concurrently. If the {@link FactoryDefinitionParser} is created
 * with the {@link FactoryDefinitionParserFactory} then it will be created only once.
 *
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
@SuppressWarnings("deprecation")
public interface FactoryDefinitionParser {

	/**
	 * Parses the factory definition string and creates a factory object
	 * that can create the object according to the definition.
	 *
	 * @param factoryDefinition The factory definition
	 * @return The Factory object that creates the target object according to the definition.
	 * @throws InvalidFactoryDefinitionException If the factoryDefinition is invalid
	 * @throws FactoryException If something went wrong while creating the factory
	 */
	Factory<?> parse(String factoryDefinition);


	@SuppressWarnings("unchecked")
  class FactoryDefinitionParserFactory {

        private static Log logger = LogFactory.getLog(FactoryDefinitionParserFactory.class);

        public static String GLOBAL_DEFAULT_FACTORY_DEFINITION_PARSER_CLASS = "factory.definition.parser.class";
        public static String DEFAULT_FACTORY_DEFINITION_PARSER_CLASS = "org.milyn.javabean.factory.BasicFactoryDefinitionParser";

        public static final String DEFAULT_ALIAS = "default";

		private static volatile ConcurrentMap<String, FactoryDefinitionParser> instances = new ConcurrentHashMap<String, FactoryDefinitionParser>();

        private static volatile Map<String, Class<? extends FactoryDefinitionParser>> aliasToClassMap;

        public static FactoryDefinitionParser getInstance(String alias, ApplicationContext applicationContext) {

    		String className;
            if(StringUtils.isEmpty(alias) || alias.equals(DEFAULT_ALIAS)) {
                className = applicationContext.getStore().getGlobalParams().getStringParameter(GLOBAL_DEFAULT_FACTORY_DEFINITION_PARSER_CLASS, DEFAULT_FACTORY_DEFINITION_PARSER_CLASS);
            } else {
                loadAliasToClassMap();

                Class<? extends FactoryDefinitionParser> clazz = aliasToClassMap.get(alias);
                if(clazz == null) {

                    //We couldn't find any class that uses that alias so maybe the alias is a class name.
                    try {
                        clazz = ClassUtil.forName(alias, FactoryDefinitionParser.class);

                        className = clazz.getName();
                    } catch (ClassNotFoundException e) {
                       throw new IllegalFactoryAliasException("The FactoryDefinitionParser alias '" + alias + "' can't be found and doesn't seem to be a classname.", e);
                    }
                }
                className = clazz.getName();
            }

    		FactoryDefinitionParser factoryDefinitionParser = instances.get(className);
    		if(factoryDefinitionParser == null) {

    			try {
					@SuppressWarnings("unchecked")
					Class<FactoryDefinitionParser> factoryDefinitionParserClass = ClassUtil.forName(className, FactoryDefinitionParser.class);

					FactoryDefinitionParser newFactoryDefinitionParser = factoryDefinitionParserClass.newInstance();

					instances.putIfAbsent(className, newFactoryDefinitionParser);

					// We do an extra get to make sure that there is always only one factoryDefinitionParser instance
					factoryDefinitionParser = instances.get(className);

				} catch (ClassNotFoundException e) {
					throw new IllegalArgumentException("The FactoryDefinitionParser class '"+ className +"' can't be found", e);
				} catch (InstantiationException e) {
					throw new IllegalArgumentException("The FactoryDefinitionParser class '"+ className +"'can't be instantiated. The FactoryDefinitionParser class must have a argumentless public constructor.", e);
				} catch (IllegalAccessException e) {
					throw new IllegalArgumentException("The FactoryDefinitionParser class '"+ className +"' can't be instantiated.", e);
				}

    		}

    		return factoryDefinitionParser;
        }

		public static FactoryDefinitionParser getInstance(ApplicationContext applicationContext) {
            return getInstance("default", applicationContext);
		}

        public static Map<String, Class<? extends FactoryDefinitionParser>> getAliasToClassMap() {
            loadAliasToClassMap();

            return Collections.unmodifiableMap(aliasToClassMap);
        }

        private synchronized static void loadAliasToClassMap() throws DataDecodeException {
            if(aliasToClassMap == null) {
                synchronized (FactoryDefinitionParserFactory.class) {
                    if(aliasToClassMap == null) {
                        List<Class<FactoryDefinitionParser>> factories = ClassUtil.getClasses("META-INF/smooks-javabean-factory-definition-parsers.inf", FactoryDefinitionParser.class);

                        Set<String> toRemove = new HashSet<String>();

                        aliasToClassMap = new HashMap<String, Class<? extends FactoryDefinitionParser>>();
                        for (Class<? extends FactoryDefinitionParser> factory : factories) {
                            Alias alias = factory.getAnnotation(Alias.class);
                            if(alias != null) {
                                String[] names = alias.value();

                                for (String name : names) {
                                    if(name.equals(DEFAULT_ALIAS)) {
                                        throw new IllegalFactoryAliasException("The alias 'default' is a reserved alias name. Please use a different name");
                                    }
                                    if(aliasToClassMap.containsKey(name)) {
                                        Class<? extends FactoryDefinitionParser> prevClass = aliasToClassMap.get(name);

                                        logger.warn("More than one FactoryDefinitionParser has the alias '" + name + "' on the classpath. Previous: '" + prevClass.getName() + "'. Current '" + factory.getName() + "'. To use one of these factories you will have to declare the complete class name as alias.");

                                        toRemove.add(name); // We register that we need to remove that one. We keep it for to be able to give clear warning messages.
                                    }

                                    aliasToClassMap.put(name, factory);
                                }
                            }
                        }
                        //We remove all alias that we defined multiple times
                        for(String name : toRemove) {
                            aliasToClassMap.remove(name);
                        }
                    }
                }
            }
        }
	}
}
