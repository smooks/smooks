package org.milyn.rules.mvel;

import org.milyn.rules.BasicRuleEvalResult;

/**
 * MVEL RuleEvalResult.
 *
 * @author <a href="mailto:julien.sirocchi@nexse.com">Julien Sirocchi</a>
 */
public class MVELRuleEvalResult extends BasicRuleEvalResult {

   private static final long serialVersionUID = 4417452451543918680L;

   /**
    * The text used in the match.
    */
   private String text;

   /**
    * Creates a RuleEvalResult that indicates a successfully executed rule.
    */
   public MVELRuleEvalResult(final boolean matched, final String ruleName, final String ruleProviderName, final String text) {
       super(matched, ruleName, ruleProviderName);
       this.text = text;
   }

   public MVELRuleEvalResult(final Throwable evalException, final String ruleName, final String ruleProviderName, final String text) {
       super(evalException, ruleName, ruleProviderName);
       this.text = text;
   }

   public String getText() {
       return text;
   }

   @Override
   public String toString()
   {
       return String.format("%s, matched=%b, providerName=%s, ruleName=%s, text=%s", getClass().getSimpleName(), matched(), getRuleProviderName(), getRuleName(), getText());
   }

}

