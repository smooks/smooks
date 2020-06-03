/*-
 * ========================LICENSE_START=================================
 * Smooks Commons
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0 or,
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
package org.smooks.function;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts a StringFunction definition into a function list.
 *
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 */
public class StringFunctionDefinitionParser {

    public static final TrimFunction TRIM_FUNCTION = new TrimFunction();
    public static final LeftTrimFunction LEFT_TRIM_FUNCTION = new LeftTrimFunction();
    public static final RightTrimFunction RIGHT_TRIM_FUNCTION = new RightTrimFunction();
    public static final UpperCaseFunction UPPER_CASE_FUNCTION = new UpperCaseFunction();
    public static final LowerCaseFunction LOWER_CASE_FUNCTION = new LowerCaseFunction();
    public static final CapitalizeFunction CAPITALIZE_FUNCTION = new CapitalizeFunction();
    public static final CapitalizeFirstFunction CAPITALIZE_FIRST_FUNCTION = new CapitalizeFirstFunction();
    public static final UncapitalizeFirstFunction UNCAPITALIZE_FIRST_FUNCTION = new UncapitalizeFirstFunction();

    public static final String TRIM_DEFINITION = "trim";
    public static final String LEFT_TRIM_DEFINITION = "left_trim";
    public static final String RIGHT_TRIM_DEFINITION = "right_trim";
    public static final String UPPER_CASE_DEFINITION = "upper_case";
    public static final String LOWER_CASE_DEFINITION = "lower_case";
    public static final String CAPITALIZE_DEFINITION = "capitalize";
    public static final String CAPITALIZE_FIRST_DEFINITION = "cap_first";
    public static final String UNCAPITALIZE_FIRST_DEFINITION = "uncap_first";
    
    public static final char SEPARATOR = '.';

    private StringFunctionDefinitionParser() {
    }

    public static List<StringFunction> parse(String definition) {
        List<StringFunction> functions = new ArrayList<StringFunction>();

        String[] functionsDef = StringUtils.split(definition, SEPARATOR);

        for(String functionDef : functionsDef) {
            if(functionDef.equals(TRIM_DEFINITION)) {
                functions.add(TRIM_FUNCTION);
            }else if(functionDef.equals(LEFT_TRIM_DEFINITION)) {
                functions.add(LEFT_TRIM_FUNCTION);
            }else if(functionDef.equals(RIGHT_TRIM_DEFINITION)) {
                functions.add(RIGHT_TRIM_FUNCTION);
            } else if(functionDef.equals(UPPER_CASE_DEFINITION)) {
                functions.add(UPPER_CASE_FUNCTION);
            } else if(functionDef.equals(LOWER_CASE_DEFINITION)) {
                functions.add(LOWER_CASE_FUNCTION);
            }  else if(functionDef.equals(CAPITALIZE_DEFINITION)) {
                functions.add(CAPITALIZE_FUNCTION);
            }  else if(functionDef.equals(CAPITALIZE_FIRST_DEFINITION)) {
                functions.add(CAPITALIZE_FIRST_FUNCTION);
            }  else if(functionDef.equals(UNCAPITALIZE_FIRST_DEFINITION)) {
                functions.add(UNCAPITALIZE_FIRST_FUNCTION);
            }  else {
                throw new UnknownStringFunctionException("The function '"+ functionDef +"' in the function definition '"+ definition +"' is unknown.");
            }
        }

        return functions;
    }

}
