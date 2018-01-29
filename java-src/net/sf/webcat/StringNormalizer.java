package net.sf.webcat;

import java.util.Collection;

//-------------------------------------------------------------------------
/**
 *  Use {@link student.testingsupport.StringNormalizer} instead.
 *
 *  @author  Stephen Edwards
 *  @author Last changed by $Author: stedwar2 $
 *  @version $Revision: 1.3 $, $Date: 2010/02/23 19:47:19 $
 */
public class StringNormalizer
    extends student.testingsupport.StringNormalizer
{
    //~ Instance/static variables .............................................

    private static final long serialVersionUID = 5952248055096628574L;


    //~ Constructor ...........................................................

    // ----------------------------------------------------------
    /**
     * Use {@link student.testingsupport.StringNormalizer} instead.
     * @deprecated
     */
    public StringNormalizer()
    {
        super();
    }


    // ----------------------------------------------------------
    /**
     * Use {@link student.testingsupport.StringNormalizer} instead.
     * @param useStandardRules If true, the set of standard (non-OPT_*)
     * rules will be used.  If false, an "identity" normalizer will be
     * produced instead.
     * @deprecated
     */
    public StringNormalizer(boolean useStandardRules)
    {
        super(useStandardRules);
    }


    // ----------------------------------------------------------
    /**
     * Use {@link student.testingsupport.StringNormalizer} instead.
     * @param rules a (variable-length) comma-separated sequence of
     * rules to add
     * @deprecated
     */
    public StringNormalizer(StandardRule ... rules)
    {
        super(rules);
    }


    // ----------------------------------------------------------
    /**
     * Use {@link student.testingsupport.StringNormalizer} instead.
     * @param rules a (variable-length) comma-separated sequence of
     * rules to add
     * @deprecated
     */
    public StringNormalizer(NormalizerRule ... rules)
    {
        super(rules);
    }


    // ----------------------------------------------------------
    /**
     * Use {@link student.testingsupport.StringNormalizer} instead.
     * @param rules a collection of rules to add (could be another
     * StringNormalizer, or any other kind of collection)
     * @deprecated
     */
    public StringNormalizer(Collection<? extends NormalizerRule> rules)
    {
        super(rules);
    }

}
