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
package org.milyn.cdr;

import org.milyn.profile.HttpAcceptHeaderProfile;
import org.milyn.profile.Profile;
import org.milyn.profile.ProfileSet;

/**
 * Represents a single parsed profile targeting expression.
 * <p/>
 * The <b>target-profile</b> attribute can contain multiple comma seperated "profile targeting expressions" i.e.
 * a list of them.  See {@link org.milyn.cdr.SmooksResourceConfiguration} docs.  This class represents
 * a single expression within a list of expressions.
 * <p/>
 * So, a single expression is composed of 1 or more "expression tokens" seperated by 
 * "AND".  The expression arg to the constructor will be in one of 
 * the following forms:
 * <ol>
 * 	<li>"profileX" i.e. a single entity.</li>
 * 	<li>"profileX AND profileY" i.e. a compound entity.</li>
 * 	<li>"profileX AND not:profileY" i.e. a compound entity.</li>
 * </ol>
 * Note, we only supports "AND" operations between the tokens, but a token can be
 * negated by prefixing it with "not:".
 * <p/>
 * See {@link ProfileTargetingExpression.ExpressionToken}.
 * @author tfennelly
 */
public class ProfileTargetingExpression {
	
	private String expression;
	private ExpressionToken[] expressionTokens;
	
	public ProfileTargetingExpression(String expression) {
		if(expression == null || expression.trim().equals("")) {
			throw new IllegalArgumentException("null or empty 'expression' arg.");
		}
		this.expression = expression;
		
		String[] tokens = expression.split(" AND ");
		expressionTokens = new ExpressionToken[tokens.length];
		for(int i = 0; i < tokens.length; i++) {
			String token = tokens[i].trim();
			expressionTokens[i] = new ExpressionToken(token);
		}
	}
	
	/**
	 * Does this expression match one of the profiles in the supplied profile set.
	 * @param profileSet Profile set.
	 * @return True if this expression matches one of the profiles in the supplied
     * profile set, otherwise false.
	 */
	public boolean isMatch(ProfileSet profileSet) {
		for (int i = 0; i < expressionTokens.length; i++) {
			if(!expressionTokens[i].isMatch(profileSet)) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Get the specificity of this expression with respect to the supplied profile set.
	 * <p/>
	 * Iterates over this expressions list of {@link ExpressionToken}s calling
	 * {@link ExpressionToken#getSpecificity(org.milyn.profile.ProfileSet)} and adds up their specificities.
	 * @param profileSet Evaluation Profile Set.
	 * @return Specificity value of the expression for the supplied profile set.
	 */
	public double getSpecificity(ProfileSet profileSet) {
		double specificity = 0;

		// Only if the expression matches the device.
		if(isMatch(profileSet)) {
			for (int i = 0; i < expressionTokens.length; i++) {
				if(expressionTokens[i].isMatch(profileSet)) {
					specificity += expressionTokens[i].getSpecificity(profileSet);
				}
			}
		}
		
		return specificity;
	}

	/**
	 * Get the expression used to construct this instance.
	 * @return The expression string for this instance.
	 */
	public String getExpression() {
		return expression;
	}

	/**
	 * Get the list of {@link ExpressionToken}s parsed out of the {@link #ProfileTargetingExpression(String) expression}
	 * used to construct this instance.
	 * @return The list of {@link ExpressionToken}s.
	 */
	public ExpressionToken[] getExpressionTokens() {
		return expressionTokens;
	}
	
	public String toString() {
		return expression;
	}
	
	/**
	 * Profile targeting expression token.
	 * @author tfennelly
	 */
	public class ExpressionToken {
		
		private String expressionToken;
		private boolean negated;

		/**
		 * Private constructor.
		 * @param expressionToken
		 */
		private ExpressionToken(String expressionToken) {
			negated = expressionToken.startsWith("not:");
			if(negated) {
				this.expressionToken = expressionToken.substring(4);
			} else {
				this.expressionToken = expressionToken;
			}
		}

		/**
		 * Get the token value (profile).
		 * @return The profile specified in this token.
		 */
		public String getToken() {
			return expressionToken;
		}

		/**
		 * Is this token negated.
		 * <p/>
		 * Is the token prefixed with "not:".
		 * @return True if the token is negated, otherwise false.
		 */
		public boolean isNegated() {
			return negated;
		}
		
		/**
		 * Is the token a wildcard token.
		 * <p/>
		 * Is the token equal to "*".
		 * @return True if the token is a wildcard token, otherwise false. 
		 */
		public boolean isWildcard() {
			return expressionToken.equals("*");
		}
		
		/**
		 * Is the profile specified in this token is a member of the supplied profile set.
		 * @param profileSet Profile set.
		 * @return True if the profile specified in this token is a member of the supplied
         * profile set, otherwise false.
		 */
		public boolean isMatch(ProfileSet profileSet) {
			if(isWildcard()) {
				return true;
			} else if(negated) {
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
     if(isNegated()) {
         if(profileSet.getBaseProfile().equals(expressionToken)) {
             return 0;
         } else if(profileSet.isMember(expressionToken)) {
             return 0;
         } else if(isWildcard()) {
             return 0;
         }
         return 1;
     } else {
         // Is the "expressionToken" referencing the base profile, a sub-profile,
         // or is it a wildcard token.
         Profile profile = profileSet.getProfile(expressionToken);

         if(profileSet.getBaseProfile().equalsIgnoreCase(expressionToken)) {
             return 100;
         } else if(profile != null) {
             // If it's a HTTP "Accept" header media profile, multiple
             // the specificity by the media qvalue.  See the
             // HttpAcceptHeaderProfile javadocs.
             if(profile instanceof HttpAcceptHeaderProfile) {
                 return (10 * ((HttpAcceptHeaderProfile)profile).getParamNumeric("q", 1));
             } else {
                 return 10;
             }
         } else if(isWildcard()) {
             return 5;
         }
         return 0;
     }
		 * </pre>
		 * @param profileSet Profile set.
		 * @return Specificity value for the token.
		 */
		public double getSpecificity(ProfileSet profileSet) {
			if(isNegated()) {
				if(profileSet.getBaseProfile().equals(expressionToken)) {
					return 0;
				} else if(profileSet.isMember(expressionToken)) {
					return 0;
				} else if(isWildcard()) {
					return 0;
				}
				return 1;
			} else {
				// Is the "expressionToken" referencing the base profile, a sub-profile,
				// or is it a wildcard token.
				Profile profile = profileSet.getProfile(expressionToken);

				if(profileSet.getBaseProfile().equalsIgnoreCase(expressionToken)) {
					return 100;
				} else if(profile != null) {
					// If it's a HTTP "Accept" header media profile, multiple
					// the specificity by the media qvalue.  See the
					// HttpAcceptHeaderProfile javadocs.
					if(profile instanceof HttpAcceptHeaderProfile) {
						return (10 * ((HttpAcceptHeaderProfile)profile).getParamNumeric("q", 1));
					} else {
						return 10;
					}
				} else if(isWildcard()) {
					return 5;
				}
				return 0;
			}
		}

		public String toString() {
			return expressionToken;
		}
	}
}
