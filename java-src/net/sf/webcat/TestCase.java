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
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Scanner;

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
    private Scanner                tcIn  = null;
    private StringNormalizer       sn = new StringNormalizer(true);

    // Used for communicating with assertTrue() and assertFalse().  Ideally,
    // they should be instance vars, but assertTrue() and assertFalse()
    // have to be static so these messages must be too.
    private static String predicateReturnsTrueReason;
    private static String predicateReturnsFalseReason;


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
        predicateReturnsTrueReason = null;
        predicateReturnsFalseReason = null;
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
     * {@link #setIn(Scanner)} to set the contents before you begin
     * using this stream.  This stream gets reset for every test case.
     * @return a {@link Scanner} containing any contents you
     * have specified.
     */
    public Scanner in()
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
        tcIn = new Scanner(contents);
    }


    // ----------------------------------------------------------
    /**
     * Set the contents of this test case's input stream, which can then
     * be retrieved using {@link #in()}.
     * @param contents The contents to use for the stream, which replace
     * any that were there before.
     */
    public void setIn(Scanner contents)
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
     * Asserts that a condition is true. If it isn't, it throws an
     * AssertionFailedError with the given message.  This is a
     * special version of
     * {@link junit.framework.TestCase#assertTrue(String,boolean)}
     * that issues special diagnostics when the assertion fails, if
     * the given condition supports it.
     * @param message   The message to use for a failed assertion
     * @param condition The condition to check
     */
    public static void assertTrue(String message, boolean condition)
    {
        String falseReason = predicateReturnsFalseReason;
        predicateReturnsFalseReason = null;
        predicateReturnsTrueReason = null;
        if (!condition)
        {
        	if (message == null)
        	{
        		message = falseReason;
        	}
        	else
        	{
        		message += " " + falseReason;
        	}
        }
        junit.framework.TestCase.assertTrue(message, condition);
    }


    // ----------------------------------------------------------
    /**
     * Asserts that a condition is true. If it isn't, it throws an
     * AssertionFailedError with the given message.  This is a
     * special version of
     * {@link junit.framework.TestCase#assertTrue(boolean)}
     * that issues special diagnostics when the assertion fails, if
     * the given condition supports it.
     * @param condition The condition to check
     */
    public static void assertTrue(boolean condition)
    {
        assertTrue(null, condition);
    }


    // ----------------------------------------------------------
    /**
     * Asserts that a condition is false. If it isn't, it throws an
     * AssertionFailedError with the given message.  This is a
     * special version of
     * {@link junit.framework.TestCase#assertFalse(String,boolean)}
     * that issues special diagnostics when the assertion fails, if
     * the given condition supports it.
     * @param message   The message to use for a failed assertion
     * @param condition The condition to check
     */
    public static void assertFalse(String message, boolean condition)
    {
        String trueReason = predicateReturnsTrueReason;
        predicateReturnsFalseReason = null;
        predicateReturnsTrueReason = null;
        if (condition)
        {
        	if (message == null)
        	{
        		message = trueReason;
        	}
        	else
        	{
        		message += " " + trueReason;
        	}
        }
        junit.framework.TestCase.assertFalse(message, condition);
    }


    // ----------------------------------------------------------
    /**
     * Asserts that a condition is false. If it isn't, it throws an
     * AssertionFailedError with the given message.  This is a
     * special version of
     * {@link junit.framework.TestCase#assertFalse(boolean)}
     * that issues special diagnostics when the assertion fails, if
     * the given condition supports it.
     * @param condition The condition to check
     */
    public static void assertFalse(boolean condition)
    {
        assertFalse(null, condition);
    }


    // ----------------------------------------------------------
    /**
     * Takes a string and, if it is too long, shortens it by replacing the
     * middle with an ellipsis.  For example, calling <code>compact("hello
     * there", 6, 3)</code> will return "hel...ere".
     * @param content The string to shorten
     * @param threshold Strings longer than this will be compacted, while
     *        strings less than or equal to this limit will be returned
     *        unchanged
     * @param prefixLen How many characters at the front and back of the
     *        string to keep.  This number must be less than or equal to half
     *        the threshold
     * @return The shortened version of the string
     */
    public static String compact(String content, int threshold, int prefixLen)
    {
        if (content != null && content.length() > threshold)
        {
            assert prefixLen < (threshold + 1) / 2;
            return content.substring(0, prefixLen) + "..."
                + content.substring(content.length() - prefixLen);
        }
        else
        {
            return content;
        }
    }


    // ----------------------------------------------------------
    /**
     * Takes a string and, if it is too long, shortens it by replacing the
     * middle with an ellipsis.
     * @param content The string to shorten
     * @return The shortened version of the string
     */
    public static String compact(String content)
    {
        return compact(content, 15, 5);
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
        boolean result = left == right;
        if (left != null && right != null)
        {
            result = left.equals(right);
        }
        if (result)
        {
            predicateReturnsTrueReason =
                "<" + compact(left) + "> was the same as:<"
                + compact(right) + ">";
        }
        else
        {
            String msg =
                (new junit.framework.ComparisonFailure(null, left, right))
                    .getMessage();
            if (msg.startsWith("null "))
            {
                msg = msg.substring("null ".length());
            }
            predicateReturnsFalseReason = msg;
        }
        return result;
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
        return equals(stringNormalizer().normalize(left),
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
     * Note that this predicate uses the opposite parameter ordering
     * from JUnit assertions: The value to test is the <b>first</b>
     * parameter, and the expected pattern is the <b>second</b>.
     * </p>
     * @param actual   The value to test
     * @param expected The expected value (interpreted as a regular
     *                 expression {@link Pattern})
     * @return True if the actual matches the expected pattern
     */
    public boolean equalsRegex(String actual, String expected)
    {
        return equalsRegex(actual, Pattern.compile(expected));
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
     * Note that this predicate uses the opposite parameter ordering
     * from JUnit assertions: The value to test is the <b>first</b>
     * parameter, and the expected pattern is the <b>second</b>.
     * </p>
     * @param actual   The value to test
     * @param expected The expected value
     * @return True if the actual matches the expected pattern
     */
    public boolean equalsRegex(String actual, Pattern expected)
    {
        if (actual == null)
        {
            actual = "";
        }
        boolean result = expected.matcher(actual).matches();
        if (result)
        {
            predicateReturnsTrueReason =
                "<" + compact(actual) + "> matches regex:<"
                + compact(expected.toString(), 25, 10) + ">";
        }
        else
        {
            predicateReturnsFalseReason =
                "<" + compact(actual) + "> does not match regex:<"
                + compact(expected.toString(), 25, 10) + ">";
        }
        return result;
    }


    // ----------------------------------------------------------
    /**
     * Determines whether a String exactly matches an expected regular
     * expression, respecting preferences for what differences matter.
     * A null for the actual value is treated the same as an empty string
     * for the purposes of matching.  The regular expression must match
     * the full string (all characters taken together).  To match a substring,
     * use {@link #fuzzyContainsRegex(String,String)} instead.
     * <p>
     * Note that this predicate uses the opposite parameter ordering
     * from JUnit assertions: The value to test is the <b>first</b>
     * parameter, and the expected pattern is the <b>second</b>.
     * </p>
     * <p>Use
     * {@link #stringNormalizer()} to access and modify the
     * {@link StringNormalizer} object's preferences for comparing
     * strings.</p>
     * @param actual   The value to test
     * @param expected The expected value (interpreted as a regular
     *                 expression {@link Pattern})
     * @return True if the actual matches the expected pattern
     */
    public boolean fuzzyEqualsRegex(String actual, String expected)
    {
        return fuzzyEqualsRegex(actual, Pattern.compile(expected));
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
     * @param actual   The value to test
     * @param expected The expected value
     * @return True if the actual matches the expected pattern
     */
    public boolean fuzzyEqualsRegex(String actual, Pattern expected)
    {
        return equalsRegex(stringNormalizer().normalize(actual), expected);
    }


    // ----------------------------------------------------------
    /**
     * Determine whether one String contains another as a substring.
     * This method is identical to {@link String#contains(String)}, but
     * is provided for symmetry with the other comparison predicates provided
     * in this class.  If the largerString is null, this method returns
     * false (since it can contain nothing).
     * <p>
     * Note that this predicate uses the opposite parameter ordering
     * from JUnit assertions: The value to test is the <b>first</b>
     * parameter, and the expected substring is the <b>second</b>.
     * </p>
     * @param largerString The target to look in
     * @param substring    The substring to look for
     * @return True if the largerString contains the substring
     */
    public boolean contains(String largerString, String substring)
    {
        boolean result = largerString != null
            && largerString.contains(substring);
        if (result)
        {
            predicateReturnsTrueReason =
                "<" + compact(largerString) + "> contains:<"
                + compact(substring) + ">";
        }
        else
        {
            predicateReturnsFalseReason =
                "<" + compact(largerString) + "> does not contain:<"
                + compact(substring) + ">";
        }
        return result;
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
     * <p>
     * Note that this predicate uses the opposite parameter ordering
     * from JUnit assertions: The value to test is the <b>first</b>
     * parameter, and the expected substrings are the <b>second</b>.
     * </p>
     * @param largerString The target to look in
     * @param substrings   The substrings to look for (in order)
     * @return True if the largerString contains all of the specified
     * substrings in order.
     */
    public boolean contains(String largerString, String[] substrings)
    {
        int pos = (largerString == null) ? -1 : 0;
        for (int i = 0; i < substrings.length  &&  largerString != null; i++)
        {
            pos = largerString.indexOf(substrings[i], pos);
            if (pos > 0)
            {
                pos += substrings[i].length();
            }
            else
            {
                predicateReturnsFalseReason =
                    "<" + compact(largerString) + "> does not contain:<"
                    + compact(substrings[i]) + ">";
            }
        }
        if (pos >= 0)
        {
            predicateReturnsTrueReason =
                "<" + compact(largerString) + "> contains:"
                + Arrays.toString(substrings);
            return true;
        }
        else
        {
            return false;
        }
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
     * <p>
     * Note that this predicate uses the opposite parameter ordering
     * from JUnit assertions: The value to test is the <b>first</b>
     * parameter, and the expected substring is the <b>second</b>.
     * </p>
     * @param largerString The target to look in
     * @param substring    The substring to look for
     * @return True if the largerString contains the specified
     * substring.
     */
    public boolean fuzzyContains(String largerString, String substring)
    {
        return contains(
            stringNormalizer().normalize(largerString),
            stringNormalizer().normalize(substring));
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
     * <p>
     * Note that this predicate uses the opposite parameter ordering
     * from JUnit assertions: The value to test is the <b>first</b>
     * parameter, and the expected substrings are the <b>second</b>.
     * </p>
     * @param substrings   The substrings to look for (in order)
     * @param largerString The target to look in
     * @return True if the largerString contains all of the specified
     * substrings in order.
     */
    public boolean fuzzyContains(String largerString, String[] substrings)
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
            stringNormalizer().normalize(largerString), normalizedSubstrings);
    }


    // ----------------------------------------------------------
    /**
     * Determine whether one String contains another as a substring, where
     * the expected substring is specified as a regular expression.
     * If the largerString is null, this method returns false (since it
     * can contain nothing).
     * <p>
     * Note that this predicate uses the opposite parameter ordering
     * from JUnit assertions: The value to test is the <b>first</b>
     * parameter, and the expected substring is the <b>second</b>.
     * </p>
     * @param largerString The target to look in
     * @param substring    The substring to look for (interpreted as a
     *                     regular expression {@link Pattern})
     * @return True if the largerString contains the substring pattern
     */
    public boolean containsRegex(String largerString, String substring)
    {
        return containsRegex(largerString, Pattern.compile(substring));
    }


    // ----------------------------------------------------------
    /**
     * Determine whether one String contains another as a substring, where
     * the expected substring is specified as a regular expression.
     * If the largerString is null, this method returns false (since it
     * can contain nothing).
     * <p>
     * Note that this predicate uses the opposite parameter ordering
     * from JUnit assertions: The value to test is the <b>first</b>
     * parameter, and the expected substring is the <b>second</b>.
     * </p>
     * @param largerString The target to look in
     * @param substring    The substring to look for
     * @return True if the largerString contains the substring pattern
     */
    public boolean containsRegex(String largerString, Pattern substring)
    {
        boolean result = largerString != null
          && substring.matcher(largerString).find();
        if (result)
        {
            predicateReturnsTrueReason =
                "<" + compact(largerString) + "> contains regex:<"
                + compact(substring.toString(), 25, 10) + ">";
        }
        else
        {
            predicateReturnsFalseReason =
                "<" + compact(largerString) + "> does not contain regex:<"
                + compact(substring.toString(), 25, 10) + ">";
        }
        return result;
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
     * <p>
     * Note that this predicate uses the opposite parameter ordering
     * from JUnit assertions: The value to test is the <b>first</b>
     * parameter, and the expected substrings are the <b>second</b>.
     * </p>
     * @param largerString The target to look in
     * @param substrings   An array of expected substrings (interpreted as
     *                     regular expression {@link Pattern}s), which must
     *                     occur in the same order in the larger string
     * @return True if the largerString contains all of the specified
     * substrings in order.
     */
    public boolean containsRegex(String largerString, String[] substrings)
    {
        Pattern[] patterns = new Pattern[substrings.length];
        for (int i = 0; i < substrings.length; i++)
        {
            patterns[i] = Pattern.compile(substrings[i]);
        }
        return containsRegex(largerString, patterns);
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
     * @param largerString The target to look in
     * @param substrings   An array of expected substrings, which must
     *                     occur in the same order in the larger string
     * @return True if the largerString contains all of the specified
     * substrings in order.
     */
    public boolean containsRegex(String largerString, Pattern[] substrings)
    {
        boolean result = true;
        int pos = 0;
        for (int i = 0; i < substrings.length; i++)
        {
            Matcher matcher = substrings[i].matcher(largerString);
            result = matcher.find(pos);
            if (!result)
            {
                predicateReturnsFalseReason =
                    "<" + compact(largerString) + "> does not contain regex:<"
                    + compact(substrings[i].toString(), 25, 10) + ">";
                break;
            }
            pos = matcher.end();
        }
        if (result)
        {
            predicateReturnsTrueReason =
                "<" + compact(largerString) + "> contains:"
                + Arrays.toString(substrings);
            return true;
        }
        else
        {
            return false;
        }
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
     * <p>
     * Note that this predicate uses the opposite parameter ordering
     * from JUnit assertions: The value to test is the <b>first</b>
     * parameter, and the expected substring is the <b>second</b>.
     * </p>
     * @param largerString The target to look in
     * @param substring    The substring to look for (interpreted as a
     *                     regular expression {@link Pattern})
     * @return True if the largerString contains the substring pattern
     */
    public boolean fuzzyContainsRegex(String largerString, String substring)
    {
        return fuzzyContainsRegex(largerString, Pattern.compile(substring));
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
     * <p>
     * Note that this predicate uses the opposite parameter ordering
     * from JUnit assertions: The value to test is the <b>first</b>
     * parameter, and the expected substring is the <b>second</b>.
     * </p>
     * @param largerString The target to look in
     * @param substring    The substring to look for
     * @return True if the largerString contains the substring pattern
     */
    public boolean fuzzyContainsRegex(String largerString, Pattern substring)
    {
        return containsRegex(
            stringNormalizer().normalize(largerString), substring);
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
     * @param largerString The target to look in
     * @param substrings   An array of expected substrings (interpreted as
     *                     regular expression {@link Pattern}s), which must
     *                     occur in the same order in the larger string
     * @return True if the largerString contains all of the specified
     * substrings in order.
     */
    public boolean fuzzyContainsRegex(String largerString, String[] substrings)
    {
        Pattern[] patterns = new Pattern[substrings.length];
        for (int i = 0; i < substrings.length; i++)
        {
            patterns[i] = Pattern.compile(substrings[i]);
        }
        return fuzzyContainsRegex(largerString, patterns);
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
     * @param largerString The target to look in
     * @param substrings   An array of expected substrings, which must
     *                     occur in the same order in the larger string
     * @return True if the largerString contains all of the specified
     * substrings in order.
     */
    public boolean fuzzyContainsRegex(String largerString, Pattern[] substrings)
    {
        return containsRegex(
            stringNormalizer().normalize(largerString), substrings);
    }
}
