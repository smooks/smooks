/*-
 * ========================LICENSE_START=================================
 * Core
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
package org.smooks.engine.resource.config;

import org.smooks.api.profile.Profile;
import org.smooks.api.profile.ProfileSet;
import org.smooks.api.resource.config.ProfileTargetingExpression;

public class DefaultProfileTargetingExpression implements ProfileTargetingExpression {

    private final String expression;
    private final ExpressionToken[] expressionTokens;

    public DefaultProfileTargetingExpression(String expression) {
        if (expression == null || expression.trim().equals("")) {
            throw new IllegalArgumentException("null or empty 'expression' arg.");
        }
        this.expression = expression;

        String[] tokens = expression.split(" AND ");
        expressionTokens = new ExpressionToken[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i].trim();
            expressionTokens[i] = new ExpressionToken(token);
        }
    }

    /**
     * Does this expression match one of the profiles in the supplied profile set.
     *
     * @param profileSet Profile set.
     * @return True if this expression matches one of the profiles in the supplied
     * profile set, otherwise false.
     */
    @Override
    public boolean isMatch(ProfileSet profileSet) {
        for (ExpressionToken expressionToken : expressionTokens) {
            if (!expressionToken.isMatch(profileSet)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Get the specificity of this expression with respect to the supplied profile set.
     * <p/>
     * Iterates over this expressions list of {@link ExpressionToken}s calling
     * {@link ExpressionToken#getSpecificity(org.smooks.api.profile.ProfileSet)} and adds up their specificities.
     *
     * @param profileSet Evaluation Profile Set.
     * @return Specificity value of the expression for the supplied profile set.
     */
    @Override
    public double getSpecificity(ProfileSet profileSet) {
        double specificity = 0;

        // Only if the expression matches the device.
        if (isMatch(profileSet)) {
            for (ExpressionToken expressionToken : expressionTokens) {
                if (expressionToken.isMatch(profileSet)) {
                    specificity += expressionToken.getSpecificity(profileSet);
                }
            }
        }

        return specificity;
    }

    /**
     * Get the expression used to construct this instance.
     *
     * @return The expression string for this instance.
     */
    @Override
    public String getExpression() {
        return expression;
    }

    /**
     * Get the list of {@link ExpressionToken}s parsed out of the {@link #ProfileTargetingExpression(String) expression}
     * used to construct this instance.
     *
     * @return The list of {@link ExpressionToken}s.
     */
    protected ExpressionToken[] getExpressionTokens() {
        return expressionTokens;
    }

    @Override
    public String toString() {
        return expression;
    }

    /**
     * Profile targeting expression token.
     *
     * @author tfennelly
     */
    protected static class ExpressionToken {

        private final String expressionToken;
        private final boolean negated;

        /**
         * Private constructor.
         *
         * @param expressionToken
         */
        private ExpressionToken(String expressionToken) {
            negated = expressionToken.startsWith("not:");
            if (negated) {
                this.expressionToken = expressionToken.substring(4);
            } else {
                this.expressionToken = expressionToken;
            }
        }

        /**
         * Get the token value (profile).
         *
         * @return The profile specified in this token.
         */
        public String getToken() {
            return expressionToken;
        }

        /**
         * Is this token negated.
         * <p/>
         * Is the token prefixed with "not:".
         *
         * @return True if the token is negated, otherwise false.
         */
        public boolean isNegated() {
            return negated;
        }

        /**
         * Is the token a wildcard token.
         * <p/>
         * Is the token equal to "*".
         *
         * @return True if the token is a wildcard token, otherwise false.
         */
        public boolean isWildcard() {
            return expressionToken.equals("*");
        }

        /**
         * Is the profile specified in this token is a member of the supplied profile set.
         *
         * @param profileSet Profile set.
         * @return True if the profile specified in this token is a member of the supplied
         * profile set, otherwise false.
         */
        public boolean isMatch(ProfileSet profileSet) {
            if (isWildcard()) {
                return true;
            } else if (negated) {
                return !profileSet.isMember(expressionToken);
            } else {
                return profileSet.isMember(expressionToken);
            }
        }

        /**
         * Get the specificity of this token with respect to the supplied device.
         * <p/>
         * The following outlines the algorithm:
         * <pre>
         * if(isNegated()) {
         * if(profileSet.getBaseProfile().equals(expressionToken)) {
         * return 0;
         * } else if(profileSet.isMember(expressionToken)) {
         * return 0;
         * } else if(isWildcard()) {
         * return 0;
         * }
         * return 1;
         * } else {
         * // Is the "expressionToken" referencing the base profile, a sub-profile,
         * // or is it a wildcard token.
         * Profile profile = profileSet.getProfile(expressionToken);
         *
         * if(profileSet.getBaseProfile().equalsIgnoreCase(expressionToken)) {
         * return 100;
         * } else if(profile != null) {
         * // If it's a HTTP "Accept" header media profile, multiple
         * // the specificity by the media qvalue.  See the
         * // HttpAcceptHeaderProfile javadocs.
         * if(profile instanceof HttpAcceptHeaderProfile) {
         * return (10 * ((HttpAcceptHeaderProfile)profile).getParamNumeric("q", 1));
         * } else {
         * return 10;
         * }
         * } else if(isWildcard()) {
         * return 5;
         * }
         * return 0;
         * }
         * </pre>
         *
         * @param profileSet Profile set.
         * @return Specificity value for the token.
         */
        public double getSpecificity(ProfileSet profileSet) {
            if (isNegated()) {
                if (profileSet.getBaseProfile().equals(expressionToken)) {
                    return 0;
                } else if (profileSet.isMember(expressionToken)) {
                    return 0;
                } else if (isWildcard()) {
                    return 0;
                }
                return 1;
            } else {
                // Is the "expressionToken" referencing the base profile, a sub-profile,
                // or is it a wildcard token.
                Profile profile = profileSet.getProfile(expressionToken);

                if (profileSet.getBaseProfile().equalsIgnoreCase(expressionToken)) {
                    return 100;
                } else if (profile != null) {
                    return 10;
                } else if (isWildcard()) {
                    return 5;
                }
                return 0;
            }
        }

        @Override
        public String toString() {
            return expressionToken;
        }
    }
}
