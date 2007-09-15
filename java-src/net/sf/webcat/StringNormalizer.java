/*==========================================================================*\
 |  $Id$
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2007 Virginia Tech
 |
 |  This file is part of Web-CAT.
 |
 |  Web-CAT is free software; you can redistribute it and/or modify
 |  it under the terms of the GNU General Public License as published by
 |  the Free Software Foundation; either version 2 of the License, or
 |  (at your option) any later version.
 |
 |  Web-CAT is distributed in the hope that it will be useful,
 |  but WITHOUT ANY WARRANTY; without even the implied warranty of
 |  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 |  GNU General Public License for more details.
 |
 |  You should have received a copy of the GNU General Public License
 |  along with Web-CAT; if not, write to the Free Software
 |  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 |
 |  Project manager: Stephen Edwards <edwards@cs.vt.edu>
 |  Virginia Tech CS Dept, 660 McBryde Hall (0106), Blacksburg, VA 24061 USA
\*==========================================================================*/

package net.sf.webcat;

import java.util.regex.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

//-------------------------------------------------------------------------
/**
 *  This class represents a programmable string "normalizing" engine that
 *  can be used to convert strings into a canonical form, say, before
 *  comparing strings for equality or something.  Basically, a normalizer
 *  is a list of zero or more rules, or transformations.  The
 *  {@link #normalize(String)} method can be used to apply the entire
 *  set of transformations to a given string.
 *  <p>
 *  For example, you can build a string normalizer that replaces all
 *  sequences of one or more whitespace characters by a single space
 *  character, trims any leading or trailing space, and converts a
 *  string to lower case.  This class provides a number of predefined
 *  transformations in the {@link StandardRule} enumeration.
 *  Some examples:</p>
 *  <pre>
 *  // An "identity" transformation that does nothing:
 *  StringNormalizer norm1 = new StringNormalizer();
 *  // norm1.normalize(...) returns its argument unchanged
 *
 *  // A "lower case" normalizer:
 *  StringNormalizer norm2 = new StringNormalizer(
 *      StringNormalizer.StandardRule.IGNORE_CAPITALIZATION);
 *  // norm2.normalize(...) returns a lower case version of its argument
 *
 *  // self-explanatory:
 *  StringNormalizer norm3 = new StringNormalizer(
 *      StringNormalizer.StandardRule.IGNORE_CAPITALIZATION,
 *      StringNormalizer.StandardRule.IGNORE_PUNCTUATION);
 *
 *  // A "standard" normalizer:
 *  StringNormalizer norm4 = new StringNormalizer(true);
 *  // norm4.normalize(...) returns its contents with all punctuation
 *  // characters removed, all letters converted to lower case, all
 *  // whitespace sequences replaced by single spaces, all MS-DOS or
 *  // Mac line terminators replaced by "\n"'s, and all leading and
 *  // trailing whitespace removed.
 *  </pre>
 *  <p>
 *  Note that string normalizers that contain multiple rules apply those
 *  rules <b>in order</b> (i.e., in the order added, or the
 *  {@link java.util.List} order of this class).  This may produce
 *  inconsistent results if you are not careful when you add your rules.
 *  </p>
 *
 *  @author  Stephen Edwards
 *  @version $Id$
 */
public class StringNormalizer
    extends ArrayList<StringNormalizer.NormalizerRule>
{
    //~ Instance/static variables .............................................

    private static final long serialVersionUID = 5952248055096628574L;
    private static Map<StandardRule, NormalizerRule> standardRules =
        new EnumMap<StandardRule, NormalizerRule>(StandardRule.class);
    // initialize the map
    static
    {
        // ---- IGNORE_PUNCTUATION ----
        standardRules.put(StandardRule.IGNORE_PUNCTUATION,
            new RegexNormalizerRule(
                "[^\\p{javaLetterOrDigit}" // if not letters or digits
                + "\\p{Pc}"                // or connector punctuation
                + "\\p{Nl}"                // or LETTER_NUMBER
                + "\\p{javaWhitespace}]+", // or white space
                ""                         // then remove it
            ));

        // ---- IGNORE_CAPITALIZATION ----
        standardRules.put(StandardRule.IGNORE_CAPITALIZATION,
            new NormalizerRule() {
                public String normalize(String content)
                {
                    return content.toLowerCase();
                }
            });

        // ---- IGNORE_NEWLINE_DIFFERENCES ----
        standardRules.put(StandardRule.IGNORE_NEWLINE_DIFFERENCES,
            new RegexNormalizerRule(
                "\\p{Zl}|\r(\n?)", "\n"
            ));

        // ---- IGNORE_SPACING_DIFFERENCES ----
        standardRules.put(StandardRule.IGNORE_SPACING_DIFFERENCES,
            new RegexNormalizerRule(
                // normalize line endings
                "\\p{Zl}|\r(\n?)", "\n",

                // trim leading space from every line
                "(?dm)^[\\p{javaWhitespace}&&[^\n]]+", "",
                // trim trailing space from every line
                "(?dm)[\\p{javaWhitespace}&&[^\n]]+$", "",
                // normalize other space
                "[\\p{javaWhitespace}&&[^\n]]+",  " "
            ));

        // ---- IGNORE_TRAILING_NEWLINES ----
        standardRules.put(StandardRule.IGNORE_TRAILING_NEWLINES,
            new RegexNormalizerRule(
                "(\\p{Zl}|\r(\n?)|\n)+$", ""
            ));

        // ---- OPT_IGNORE_BLANK_LINES ----
        standardRules.put(StandardRule.OPT_IGNORE_BLANK_LINES,
            new RegexNormalizerRule(
                "(\\p{Zl}|\r(\n?)|\n)(\\p{javaWhitespace}+)"
                + "(\\p{Zl}|\r(\n?)|\n)", "$1"
            ));

        // ---- OPT_IGNORE_ALL_WHITESPACE ----
        standardRules.put(StandardRule.OPT_IGNORE_ALL_WHITESPACE,
            new RegexNormalizerRule(
                "[\\p{javaWhitespace}&&[^\\p{Zl}\r\n]]+", ""
            ));

        // ---- OPT_IGNORE_ALL_WHITESPACE_AND_NEWLINES ----
        standardRules.put(StandardRule.OPT_IGNORE_ALL_WHITESPACE_AND_NEWLINES,
            new RegexNormalizerRule("\\p{javaWhitespace}+", ""));
    }


    //~ Constructor ...........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new StringNormalizer object containing no rules (the
     * "identity" normalizer).
     */
    public StringNormalizer()
    {
        // Nothing to do
    }


    // ----------------------------------------------------------
    /**
     * Creates a new StringNormalizer object, optionally containing the
     * standard set of rules.  The standard set is all those in
     * {@link StandardRule} exception the OPT_* rules.
     * @param useStandardRules If true, the set of standard (non-OPT_*)
     * rules will be used.  If false, an "identity" normalizer will be
     * produced instead.
     */
    public StringNormalizer(boolean useStandardRules)
    {
        if (useStandardRules)
        {
            addStandardRules();
        }
    }


    // ----------------------------------------------------------
    /**
     * Creates a new StringNormalizer object containing the given
     * set of rules.
     * @param rules a (variable-length) comma-separated sequence of
     * rules to add
     */
    public StringNormalizer(StandardRule ... rules)
    {
        if (rules != null)
        {
            for (StandardRule rule : rules)
            {
                add(rule);
            }
        }
    }


    // ----------------------------------------------------------
    /**
     * Creates a new StringNormalizer object containing the given
     * set of rules.
     * @param rules a (variable-length) comma-separated sequence of
     * rules to add
     */
    public StringNormalizer(NormalizerRule ... rules)
    {
        if (rules != null)
        {
            for (NormalizerRule rule : rules)
            {
                add(rule);
            }
        }
    }


    // ----------------------------------------------------------
    /**
     * Creates a new StringNormalizer object containing the given
     * set of rules.
     * @param rules a collection of rules to add (could be another
     * StringNormalizer, or any other kind of collection)
     */
    public StringNormalizer(Collection<? extends NormalizerRule> rules)
    {
        super(rules);
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Normalize a string by applying a set of normalization rules
     * (transformations).
     * @param content The string to transform
     * @return The result after all rules have been applied
     */
    public String normalize(String content)
    {
        if (content == null) return content;
        for (NormalizerRule rule : this)
        {
            content = rule.normalize(content);
        }
        return content;
    }


    // ----------------------------------------------------------
    /**
     * Add the standard set of rules.  The standard set is all those in
     * {@link StandardRule} exception the OPT_* rules.
     */
    public void addStandardRules()
    {
        for (StandardRule rule : StandardRule.values())
        {
            if (!rule.toString().startsWith("OPT_"))
            {
                add(rule);
            }
        }
    }


    // ----------------------------------------------------------
    /**
     * Add the specified standard rule, as defined in {@link StandardRule}.
     * Note that you can also use the inherited
     * {@link java.util.List#add(Object)} method to add custom NormalizerRule
     * objects.
     * @param rule The rule to add
     */
    public void add(StandardRule rule)
    {
        add(standardRule(rule));
    }


    // ----------------------------------------------------------
    /**
     * Add the specified rule.  For efficiency, only adds the rule if it
     * is not already present in this normalizer.
     * @param rule The rule to add
     * @return True if the rule was added, or false if it is already
     * present
     */
    public boolean add(NormalizerRule rule)
    {
        return contains(rule)
            ? false
            : super.add(rule);
    }


    // ----------------------------------------------------------
    /**
     * Remove the specified standard rule, as defined in {@link StandardRule}.
     * Note that you can also use the inherited
     * {@link java.util.List#remove(Object)} method to remove other kinds
     * of NormalizerRule objects.
     * @param rule The rule to remove
     */
    public void remove(StandardRule rule)
    {
        remove(standardRule(rule));
    }


    // ----------------------------------------------------------
    /**
     * This interface defines what it means to be a normalizer rule: an
     * object having an appropriate {@link #normalize(String)} method.
     */
    public static interface NormalizerRule
    {
        /**
         * Apply this rule by normalizing the given string.
         * @param content The string to normalize
         * @return The normalized result
         */
        public String normalize(String content);
    }


    // ----------------------------------------------------------
    /**
     * A highly reusable concrete implementation of {@link NormalizerRule}
     * that applies a series of {@link Pattern regular expression}
     * substitutions.
     */
    public static class RegexNormalizerRule
        implements NormalizerRule
    {
        private boolean   everywhere;
        private Pattern[] patterns;
        private String[]  replacements;


        // ----------------------------------------------------------
        /**
         * Create a new regular expression rule using a series of
         * pattern/replacement pairs.  Each pattern/replacement will
         * be applied globally (all matches that can be found).  If multiple
         * patterns are given, they will be applied in the order given.
         * Use this form:
         * <pre>
         * myRule = new RegexNormalizerRule(
         *     "pattern1", "replacement1",
         *     "pattern2", "replacement2",
         *     ... // As many as you want
         * );
         * </pre>
         * @param patterns a series of regular expression pattern/replacement
         * pairs (there <b>must</b> be an even number!)
         */
        public RegexNormalizerRule(String ... patterns)
        {
            this(true, patterns);
        }


        // ----------------------------------------------------------
        /**
         * Create a new regular expression rule using a series of
         * pattern/replacement pairs.
         * @param everywhere True if all pattern/replacements should be
         * applied globally (for every match), or false if the replacements
         * should only be applied to the first match for each pattern.
         * @param patterns a series of regular expression pattern/replacement
         * pairs (there <b>must</b> be an even number!)
         */
        public RegexNormalizerRule(boolean everywhere, String ... patterns)
        {
            assert patterns.length % 2 == 0
                : "patterns/replacements must come in pairs";
            this.everywhere   = everywhere;
            this.patterns     = new Pattern[patterns.length / 2];
            this.replacements = new String[patterns.length / 2];
            for (int i = 0; i < patterns.length; i++)
            {
                this.patterns[i/2]     = Pattern.compile(patterns[i]);
                i++;
                this.replacements[i/2] = patterns[i];
            }
        }


        // ----------------------------------------------------------
        /**
         * Normalize a string by applying all of this rule's regular
         * expression pattern/replacement pairs, in order.
         * @param content The string to transform
         * @return The result after all substitutions have been applied
         */
        public String normalize(String content)
        {
//            System.out.println("normalizing: '" + content + "'");
            for (int i = 0; i < patterns.length; i++)
            {
//                System.out.println("Applying: '" + patterns[i] + "' => '"
//                    + replacements[i] + "'");
                Matcher matcher = patterns[i].matcher(content);
                if (everywhere)
                {
                    content = matcher.replaceAll(replacements[i]);
                }
                else
                {
                    content = matcher.replaceFirst(replacements[i]);
                }
//                System.out.println("    --> '" + content + "'");
            }
            return content;
        }
    }


    // ----------------------------------------------------------
    /**
     * This enumeration defines the set of predefined transformation rules.
     */
    public static enum StandardRule
    {
        /**
         * Strips all punctuation characters (that is, characters that are
         * not letters, numbers, connecting punctuation like "_", or white
         * space).
         */
        IGNORE_PUNCTUATION,

        /**
         * Converts to all lower case.
         */
        IGNORE_CAPITALIZATION,

        /**
         * Convert all MS-DOS (CRLF, "\r\n") and Mac (CR, "\r") line
         * termination sequences to Unix-style (LF, "\n") termination
         * sequences.
         */
        IGNORE_NEWLINE_DIFFERENCES,

        /**
         * Same as IGNORE_NEWLINE_DIFFERENCES, but also converts all
         * non-empty sequences of white space characters (except newlines)
         * to single spaces, and trims any leading or trailing white space
         * from every line.
         */
        IGNORE_SPACING_DIFFERENCES,

        /**
         * Trims any sequence of trailing line termination sequences
         * (regardless of OS).
         */
        IGNORE_TRAILING_NEWLINES,

        /**
         * Removes any blank lines.
         */
        OPT_IGNORE_BLANK_LINES,

        /**
         * Removes all white space except line termination sequences (from
         * any OS).
         */
        OPT_IGNORE_ALL_WHITESPACE,

        /**
         * Removes all white space of any kind, including newlines of any kind.
         */
        OPT_IGNORE_ALL_WHITESPACE_AND_NEWLINES
    }


    // ----------------------------------------------------------
    /**
     * Retrieve a standard rule by name.
     * @param rule the rule to retrieve
     * @return The corresponding {@link NormalizerRule}
     */
    public static NormalizerRule standardRule(StandardRule rule)
    {
        return standardRules.get(rule);
    }
}
