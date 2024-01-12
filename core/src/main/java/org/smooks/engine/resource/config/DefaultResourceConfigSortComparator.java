/*-
 * ========================LICENSE_START=================================
 * Core
 * %%
 * Copyright (C) 2020 - 2021 Smooks
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
package org.smooks.engine.resource.config;

import org.smooks.api.profile.ProfileSet;
import org.smooks.api.resource.config.ProfileTargetingExpression;
import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.api.resource.config.ResourceConfigSortComparator;

public class DefaultResourceConfigSortComparator implements ResourceConfigSortComparator {
    /**
     * Profile set.
     */
    private final ProfileSet profileSet;

    /**
     * Private constructor.
     *
     * @param profileSet Profile set used to evaluate specificity.
     */
    public DefaultResourceConfigSortComparator(ProfileSet profileSet) {
        this.profileSet = profileSet;
    }

    /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(ResourceConfig configObj1, ResourceConfig configObj2) {
        if (configObj1 == configObj2) {
            return 0;
        }

        double config1Specificity = getSpecificity(configObj1);
        double config2Specificity = getSpecificity(configObj2);

        // They are ordered as follow (most specific first). 
        if (config1Specificity > config2Specificity) {
            return -1;
        } else if (config1Specificity < config2Specificity) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * Get the specificity of the ResourceConfig.
     * <p/>
     * The "specificity" is evaluated based on the selector and target-profile values.
     *
     * @param resourceConfig Resource configuration.
     * @return Configuration specificity.
     */
    protected double getSpecificity(ResourceConfig resourceConfig) {
        double specificity = 0;

        // If the following code is modified, please update the class Javadoc.

        // Get the combined specificity of all the profile targeting expressions.
        ProfileTargetingExpression[] profileTargetingExpressions = resourceConfig.getProfileTargetingExpressions();
        for (int i = 0; i < profileTargetingExpressions.length; i++) {
            specificity += profileTargetingExpressions[i].getSpecificity(profileSet);
        }

        // Check the 'selector' attribute value.
        if (resourceConfig.isXmlDef()) {
            specificity += 10;
        } else if (resourceConfig.getSelectorPath().getSelector().equals("*")) {
            specificity += 5;
        } else {
            // Explicit selector listed
            specificity += 100;

            // If the selector is contextual it's, therefore more specific so
            // account for that.  Subtract 1 because that "1" is already accounted
            // for by the addition of 100 - it's the extra we're accounting for here...
            if (resourceConfig.getSelectorPath().size() > 1) {
                int contextSpecificity = resourceConfig.getSelectorPath().size();
                specificity += (10 * (contextSpecificity - 1));
            }
        }

        return specificity;
    }
}
