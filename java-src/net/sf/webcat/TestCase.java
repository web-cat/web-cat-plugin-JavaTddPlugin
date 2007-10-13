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

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//-------------------------------------------------------------------------
/**
 *  This class provides some customized behavior beyond the basics of
 *  {@link junit.framework.TestCase} to support testing of I/O driven
 *  programs and flexible/fuzzy comparison of strings.  In most cases, it
 *  can be used as a completely transparent drop-in replacement for its
 *  parent class.  Subclasses that override {@link #tearDown()} should also
 *  override {@link #setUp()}--be sure to call super.setUp() in this case,
 *  or you'll see unexpected behavior!
 *  <p>
 *  Fuzzy string comparisons in this class default to standard rules in
 *  {@link StringNormalizer} (excluding the OPT_* rules).  You can use
 *  the {@link #stringNormalizer()} method to access the normalizer and
 *  set your own comparison options, however.
 *  </p>
 *  <p>
 *  This class also provides a series of boolean predicates to perform
 *  various common kinds of string comparisons.  You can use these as
 *  helper predicates, calling them inside
 *  {@link junit.framework.TestCase#assertTrue(boolean)}.  These comparisons
 *  support the following variations:
 *  </p><ul>
 *  <li><p>Using plain strings, regular expressions, or fuzzy comparisons
 *  (normalized strings).</p></li>
 *  <li><p>Looking for a whole string match, or just substring
 *  containment.</p></li>
 *  <li><p>Using a single expected string/substring, or an array of substrings
 *  that should be found in the actual output in the corresponding
 *  order.</p></li>
 *  </ul>
 *
 *  @author  Stephen Edwards
 *  @version $Id$
 */
public class TestCase
    extends junit.framework.TestCase
{
    //~ Instance/static variables .............................................

    // These don't use the names "in" or "out" to provide better error
    // messages if students type those method names and accidentally leave
    // off the parens.
    private PrintWriterWithHistory tcOut = null;
    private BufferedReader         tcIn  = null;
    private StringNormalizer       sn = new StringNormalizer(true);


    //~ Constructor ...........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new TestCase object.
     */
    public TestCase()
    {
        super();
        resetIO();
    }


    // ----------------------------------------------------------
    /**
     * Creates a new TestCase object.
     * @param name The name of this test case
     */
    public TestCase(String name)
    {
        super(name);
        resetIO();
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Sets up the fixture, for example, open a network connection. This
     * method is called before each test in this class is executed.
     */
    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
    }


    // ----------------------------------------------------------
    /**
     * Tears down the fixture, for example, close a network connection.
     * This method is called after each test in this class is executed.
     */
    @Override
    protected void tearDown()
        throws Exception
    {
        super.tearDown();
        resetIO();
    }


    // ----------------------------------------------------------
    /**
     * An internal helper that resets all of the input/output buffering
     * between test cases.
     */
    protected void resetIO()
    {
        // Clear out all the stream history stuff
        tcIn = null;
        tcOut = null;

        // Make sure these are history-wrapped
        SystemIOUtilities.out().clearHistory();
        SystemIOUtilities.err().clearHistory();
        SystemIOUtilities.restoreSystemIn();
    }


    // ----------------------------------------------------------
    /**
     * Get an output stream suitable for use in test cases.  You can pass
     * this output stream to your own methods, and later call its
     * {@link net.sf.webcat.PrintWriterWithHistory#getHistory()} method to
     * extract all the output in the form of a string.  The contents of this
     * stream get cleared for every test case.
     * @return a {@link java.io.PrintWriter} suitable for use in test cases
     */
    public PrintWriterWithHistory out()
    {
        if (tcOut == null)
        {
            tcOut = new PrintWriterWithHistory();
        }
        return tcOut;
    }


    // ----------------------------------------------------------
    /**
     * Get a version of {@link System#out} that records its history
     * so you can compare against it later.  The history of this
     * stream gets cleared for every test case.
     * @return a {@link java.io.PrintStream} connected to System.out that
     * is suitable for use in test cases
     */
    public PrintStreamWithHistory systemOut()
    {
        return SystemIOUtilities.out();
    }


    // ----------------------------------------------------------
    /**
     * Get a version of {@link System#err} that records its history
     * so you can compare against it later.  The history of this
     * stream gets cleared for every test case.
     * @return a {@link java.io.PrintStream} connected to System.err that
     * is suitable for use in test cases
     */
    public PrintStreamWithHistory systemErr()
    {
        return SystemIOUtilities.err();
    }


    // ----------------------------------------------------------
    /**
     * Get an input stream containing the contents that you specify.
     * Set the contents by calling {@link #setIn(String)} or
     * {@link #setIn(BufferedReader)} to set the contents before you begin
     * using this stream.  This stream gets reset for every test case.
     * @return a {@link BufferedReader} containing any contents you
     * have specified.
     */
    public BufferedReader in()
    {
        assert tcIn != null
            : "You must call setIn() before you can access the stream";
        return tcIn;
    }


    // ----------------------------------------------------------
    /**
     * Set the contents of this test case's input stream, which can then
     * be retrieved using {@link #in()}.
     * @param contents The contents to use for the stream, which replace
     * any that were there before.
     */
    public void setIn(String contents)
    {
        tcIn = new BufferedReader(new StringReader(contents));
    }


    // ----------------------------------------------------------
    /**
     * Set the contents of this test case's input stream, which can then
     * be retrieved using {@link #in()}.
     * @param contents The contents to use for the stream, which replace
     * any that were there before.
     */
    public void setIn(BufferedReader contents)
    {
        tcIn = contents;
    }


    // ----------------------------------------------------------
    /**
     * Set the contents of this test case's input stream, which can then
     * be retrieved using {@link #in()}.
     * @param contents The contents to use for the stream, which replace
     * any that were there before.
     */
    public void setSystemIn(String contents)
    {
        SystemIOUtilities.replaceSystemInContents(contents);
    }


    // ----------------------------------------------------------
    /**
     * Access the string normalizer that this test case uses in
     * fuzzy string comparisons.  You can set your preferences for
     * fuzzy string comparisons using this object's methods.  These settings
     * are persistent from test case method to test case method, so it is
     * sufficient to set them in your test class constructor if you want
     * to use the same settings for all of your test case methods.
     * @return the string normalizer
     * @see #assertFuzzyEquals(String, String)
     * @see StringNormalizer
     * @see net.sf.webcat.StringNormalizer#addStandardRules()
     */
    protected StringNormalizer stringNormalizer()
    {
        return sn;
    }


    // ----------------------------------------------------------
    /**
     * Asserts that two Strings are equal, respecting preferences for what
     * differences matter.  This method mirrors the static
     * {@link junit.framework.TestCase#assertEquals(String,String)}
     * method, augmenting its behavior with the ability to make "fuzzy"
     * string comparisons that ignore things like differences in spacing,
     * punctuation, or capitalization.  Use
     * {@link #stringNormalizer()} to access and modify the
     * {@link StringNormalizer} object's preferences for comparing
     * strings.
     * @param expected The expected value
     * @param actual   The value to test
     */
    public void assertFuzzyEquals(String expected, String actual)
    {
        assertFuzzyEquals(null, expected, actual);
    }


    // ----------------------------------------------------------
    /**
     * Asserts that two Strings are equal, respecting preferences for what
     * differences matter.  This method mirrors the static
     * {@link junit.framework.TestCase#assertEquals(String,String)}
     * method, augmenting its behavior with the ability to make "fuzzy"
     * string comparisons that ignore things like differences in spacing,
     * punctuation, or capitalization.  Use
     * {@link #stringNormalizer()} to access and modify the
     * {@link StringNormalizer} object's preferences for comparing
     * strings.
     * @param message  The message to use for a failed assertion
     * @param expected The expected value
     * @param actual   The value to test
     */
    public void assertFuzzyEquals(
        String message, String expected, String actual)
    {
        if (message != null)
        {
            message += " (after normalizing strings)";
        }
        assertEquals(
            message, stringNormalizer().normalize(expected),
            stringNormalizer().normalize(actual));
    }


    // ----------------------------------------------------------
    /**
     * Determines whether two Strings are equal.  This method is identical
     * to {@link String#equals(Object)}, but is provided for symmetry with
     * the other comparison predicates provided in this class.  For
     * assertion writing, remember that
     * {@link junit.framework.TestCase#assertEquals(String,String)} will
     * produce more useful information on failure, however.
     * @param left  The first string to compare
     * @param right The second string to compare
     * @return True if the strings are equal
     */
    public boolean equals(String left, String right)
    {
        if (left == null || right == null)
        {
            return left == right;
        }
        return left.equals(right);
    }


    // ----------------------------------------------------------
    /**
     * Determines whether two Strings are equal, respecting preferences for
     * what differences matter.  This method mirrors
     * {@link #equals(String,String)}, augmenting its behavior with the
     * ability to make "fuzzy" string comparisons that ignore things like
     * differences in spacing, punctuation, or capitalization.  It is also
     * identical to {@link #assertFuzzyEquals(String,String)}, except that it
     * returns the boolean result of the comparison instead of making a
     * test case assertion.  Use
     * {@link #stringNormalizer()} to access and modify the
     * {@link StringNormalizer} object's preferences for comparing
     * strings.  For assertion writing, remember that
     * {@link #assertFuzzyEquals(String,String)} will
     * produce more useful information on failure, however.
     * @param left  The first string to compare
     * @param right The second string to compare
     * @return True if the strings are equal
     */
    public boolean fuzzyEquals(String left, String right)
    {
        if (left == null || right == null)
        {
            return left == right;
        }
        return stringNormalizer().normalize(left).equals(
            stringNormalizer().normalize(right));
    }


    // ----------------------------------------------------------
    /**
     * Determines whether a String exactly matches an expected regular
     * expression.  A null for the actual value is treated the same as an
     * empty string for the purposes of matching.  The regular expression
     * must match the full string (all characters taken together).  To
     * match a substring, use {@link #containsRegex(String,String)}
     * instead.
     * <p>
     * Note that, to be constistent with parameter ordering in JUnit
     * assertions, the expected pattern is the <b>first</b> parameter,
     * and the string to compare against is the <b>second</b>.
     * </p>
     * @param expected The expected value (interpreted as a regular
     *                 expression {@link Pattern})
     * @param actual   The value to test
     * @return True if the actual matches the expected pattern
     */
    public boolean equalsRegex(String expected, String actual)
    {
        return equalsRegex(Pattern.compile(expected), actual);
    }


    // ----------------------------------------------------------
    /**
     * Determines whether a String exactly matches an expected regular
     * expression.  A null for the actual value is treated the same as an
     * empty string for the purposes of matching.  The regular expression
     * must match the full string (all characters taken together).  To
     * match a substring, use {@link #containsRegex(Pattern,String)}
     * instead.
     * <p>
     * Note that, to be constistent with parameter ordering in JUnit
     * assertions, the expected pattern is the <b>first</b> parameter,
     * and the string to compare against is the <b>second</b>.
     * </p>
     * @param expected The expected value
     * @param actual   The value to test
     * @return True if the actual matches the expected pattern
     */
    public boolean equalsRegex(Pattern expected, String actual)
    {
        if (actual == null)
        {
            actual = "";
        }
        return expected.matcher(actual).matches();
    }


    // ----------------------------------------------------------
    /**
     * Determines whether a String exactly matches an expected regular
     * expression, respecting preferences for what differences matter.
     * A null for the actual value is treated the same as an empty string
     * for the purposes of matching.  The regular expression must match
     * the full string (all characters taken together).  To match a substring,
     * use {@link #fuzzyContainsRegex(String,String)} instead.
     * <p>Use
     * {@link #stringNormalizer()} to access and modify the
     * {@link StringNormalizer} object's preferences for comparing
     * strings.</p>
     * @param expected The expected value (interpreted as a regular
     *                 expression {@link Pattern})
     * @param actual   The value to test
     * @return True if the actual matches the expected pattern
     */
    public boolean fuzzyEqualsRegex(String expected, String actual)
    {
        return fuzzyEqualsRegex(Pattern.compile(expected), actual);
    }


    // ----------------------------------------------------------
    /**
     * Determines whether a String exactly matches an expected regular
     * expression, respecting preferences for what differences matter.
     * A null for the actual value is treated the same as an empty string
     * for the purposes of matching.  The regular expression must match
     * the full string (all characters taken together).  To match a substring,
     * use {@link #fuzzyContainsRegex(String,String)} instead.
     * <p>Use
     * {@link #stringNormalizer()} to access and modify the
     * {@link StringNormalizer} object's preferences for comparing
     * strings.</p>
     * @param expected The expected value
     * @param actual   The value to test
     * @return True if the actual matches the expected pattern
     */
    public boolean fuzzyEqualsRegex(Pattern expected, String actual)
    {
        return equalsRegex(expected, stringNormalizer().normalize(actual));
    }


    // ----------------------------------------------------------
    /**
     * Determine whether one String contains another as a substring.
     * This method is identical to {@link String#contains(String)}, but
     * is provided for symmetry with the other comparison predicates provided
     * in this class.  If the largerString is null, this method returns
     * false (since it can contain nothing).
     * @param substring    The substring to look for
     * @param largerString The target to look in
     * @return True if the largerString contains the substring
     */
    public boolean contains(String substring, String largerString)
    {
        return largerString != null && largerString.contains(substring);
    }


    // ----------------------------------------------------------
    /**
     * Determine whether one String contains a sequence of other substrings
     * in order.  It looks for each element of the array of substrings in turn
     * in the larger string, making sure they are all found in the proper order
     * (each substring must strictly follow the previous one, although there
     * can be any amount of intervening characters between any two substrings
     * in the array).  If the larger string is null, this method returns
     * false (since it can contain nothing).
     * @param substrings   The substrings to look for (in order)
     * @param largerString The target to look in
     * @return True if the largerString contains all of the specified
     * substrings in order.
     */
    public boolean contains(String[] substrings, String largerString)
    {
        int pos = (largerString == null) ? -1 : 0;
        for (int i = 0; i < substrings.length  &&  pos >= 0; i++)
        {
            pos = largerString.indexOf(substrings[i], pos);
            if (pos > 0)
            {
                pos += substrings[i].length();
            }
        }
        return pos >= 0;
    }


    // ----------------------------------------------------------
    /**
     * Determine whether one String contains another as a substring,
     * respecting preferences for what differences matter.  If the larger
     * string is null, this method returns false (since it can contain
     * nothing).
     * <p>This method makes "fuzzy" string comparisons that ignore things
     * like differences in spacing, punctuation, or capitalization.  Use
     * {@link #stringNormalizer()} to access and modify the
     * {@link StringNormalizer} object's preferences for comparing
     * strings.
     * </p>
     * @param substring    The substring to look for
     * @param largerString The target to look in
     * @return True if the largerString contains the specified
     * substring.
     */
    public boolean fuzzyContains(String substring, String largerString)
    {
        return contains(
            stringNormalizer().normalize(substring),
            stringNormalizer().normalize(largerString));
    }


    // ----------------------------------------------------------
    /**
     * Determine whether one String contains a sequence of other substrings
     * in order, respecting preferences for what differences matter.  It
     * looks for each element of the array of substrings in turn
     * in the larger string, making sure they are all found in the proper order
     * (each substring must strictly follow the previous one, although there
     * can be any amount of intervening characters between any two substrings
     * in the array).  If the larger string is null, this method returns
     * false (since it can contain nothing).
     * <p>This method makes "fuzzy" string comparisons that ignore things
     * like differences in spacing, punctuation, or capitalization.  Use
     * {@link #stringNormalizer()} to access and modify the
     * {@link StringNormalizer} object's preferences for comparing
     * strings.
     * </p>
     * @param substrings   The substrings to look for (in order)
     * @param largerString The target to look in
     * @return True if the largerString contains all of the specified
     * substrings in order.
     */
    public boolean fuzzyContains(String[] substrings, String largerString)
    {
        // Normalized the array of expected substrings
        String[] normalizedSubstrings = new String[substrings.length];
        for (int i = 0; i < substrings.length; i++)
        {
            normalizedSubstrings[i] =
                stringNormalizer().normalize(substrings[i]);
        }

        // Now call the regular version on the normalized args
        return contains(
            normalizedSubstrings,
            stringNormalizer().normalize(largerString));
    }


    // ----------------------------------------------------------
    /**
     * Determine whether one String contains another as a substring, where
     * the expected substring is specified as a regular expression.
     * If the largerString is null, this method returns false (since it
     * can contain nothing).
     * @param substring    The substring to look for (interpreted as a
     *                     regular expression {@link Pattern})
     * @param largerString The target to look in
     * @return True if the largerString contains the substring pattern
     */
    public boolean containsRegex(String substring, String largerString)
    {
        return containsRegex(Pattern.compile(substring), largerString);
    }


    // ----------------------------------------------------------
    /**
     * Determine whether one String contains another as a substring, where
     * the expected substring is specified as a regular expression.
     * If the largerString is null, this method returns false (since it
     * can contain nothing).
     * @param substring    The substring to look for
     * @param largerString The target to look in
     * @return True if the largerString contains the substring pattern
     */
    public boolean containsRegex(Pattern substring, String largerString)
    {
        return largerString != null
          && substring.matcher(largerString).find();
    }


    // ----------------------------------------------------------
    /**
     * Determine whether one String contains a sequence of other substrings
     * in order, where the expected substrings are specified as a regular
     * expressions.  It looks for each element of the array of substrings
     * in turn in the larger string, making sure they are all found in the
     * proper order (each substring must strictly follow the previous one,
     * although there can be any amount of intervening characters between
     * any two substrings in the array).  If the larger string is null, this
     * method returns false (since it can contain nothing).
     * @param substrings   An array of expected substrings (interpreted as
     *                     regular expression {@link Pattern}s), which must
     *                     occur in the same order in the larger string
     * @param largerString The target to look in
     * @return True if the largerString contains all of the specified
     * substrings in order.
     */
    public boolean containsRegex(String[] substrings, String largerString)
    {
        Pattern[] patterns = new Pattern[substrings.length];
        for (int i = 0; i < substrings.length; i++)
        {
            patterns[i] = Pattern.compile(substrings[i]);
        }
        return containsRegex(patterns, largerString);
    }


    // ----------------------------------------------------------
    /**
     * Determine whether one String contains a sequence of other substrings
     * in order, where the expected substrings are specified as a regular
     * expressions.  It looks for each element of the array of substrings
     * in turn in the larger string, making sure they are all found in the
     * proper order (each substring must strictly follow the previous one,
     * although there can be any amount of intervening characters between
     * any two substrings in the array).  If the larger string is null, this
     * method returns false (since it can contain nothing).
     * @param substrings   An array of expected substrings, which must
     *                     occur in the same order in the larger string
     * @param largerString The target to look in
     * @return True if the largerString contains all of the specified
     * substrings in order.
     */
    public boolean containsRegex(Pattern[] substrings, String largerString)
    {
        boolean result = true;
        int pos = 0;
        for (int i = 0; i < substrings.length; i++)
        {
            Matcher matcher = substrings[i].matcher(largerString);
            result = matcher.find(pos);
            if (!result) break;
            pos = matcher.end();
        }
        return result;
    }


    // ----------------------------------------------------------
    /**
     * Determine whether one String contains another as a substring, where
     * the expected substring is specified as a regular expression, and
     * respecting preferences for what differences matter.  This method
     * behaves just like {@link #fuzzyContains(String,String)}, except
     * that the first argument is interpreted as a regular expression.
     * String normalization rules are only appled to the larger string,
     * not to the regular expression.
     * @param substring    The substring to look for (interpreted as a
     *                     regular expression {@link Pattern})
     * @param largerString The target to look in
     * @return True if the largerString contains the substring pattern
     */
    public boolean fuzzyContainsRegex(String substring, String largerString)
    {
        return fuzzyContainsRegex(Pattern.compile(substring), largerString);
    }


    // ----------------------------------------------------------
    /**
     * Determine whether one String contains another as a substring, where
     * the expected substring is specified as a regular expression, and
     * respecting preferences for what differences matter.  This method
     * behaves just like {@link #fuzzyContains(String,String)}, except
     * that the first argument is interpreted as a regular expression.
     * String normalization rules are only appled to the larger string,
     * not to the regular expression.
     * @param substring    The substring to look for
     * @param largerString The target to look in
     * @return True if the largerString contains the substring pattern
     */
    public boolean fuzzyContainsRegex(Pattern substring, String largerString)
    {
        return containsRegex(
            substring, stringNormalizer().normalize(largerString));
    }


    // ----------------------------------------------------------
    /**
     * Determine whether one String contains a sequence of other substrings
     * in order, where the expected substrings are specified as regular
     * expressions, and respecting preferences for what differences matter.
     * This method behaves just like {@link #fuzzyContains(String[],String)},
     * except that the first argument is interpreted as an array of regular
     * expressions.  String normalization rules are only appled to the
     * larger string, not to the regular expressions.
     * @param substrings   An array of expected substrings (interpreted as
     *                     regular expression {@link Pattern}s), which must
     *                     occur in the same order in the larger string
     * @param largerString The target to look in
     * @return True if the largerString contains all of the specified
     * substrings in order.
     */
    public boolean fuzzyContainsRegex(String[] substrings, String largerString)
    {
        Pattern[] patterns = new Pattern[substrings.length];
        for (int i = 0; i < substrings.length; i++)
        {
            patterns[i] = Pattern.compile(substrings[i]);
        }
        return fuzzyContainsRegex(patterns, largerString);
    }


    // ----------------------------------------------------------
    /**
     * Determine whether one String contains a sequence of other substrings
     * in order, where the expected substrings are specified as regular
     * expressions, and respecting preferences for what differences matter.
     * This method behaves just like {@link #fuzzyContains(Pattern[],String)},
     * except that the first argument is interpreted as an array of regular
     * expressions.  String normalization rules are only appled to the
     * larger string, not to the regular expressions.
     * @param substrings   An array of expected substrings, which must
     *                     occur in the same order in the larger string
     * @param largerString The target to look in
     * @return True if the largerString contains all of the specified
     * substrings in order.
     */
    public boolean fuzzyContainsRegex(Pattern[] substrings, String largerString)
    {
        return containsRegex(
            substrings, stringNormalizer().normalize(largerString));
    }
}
