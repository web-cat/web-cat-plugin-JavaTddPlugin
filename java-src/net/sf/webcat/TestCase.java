/*==========================================================================*\
 |  $Id$
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2007-2010 Virginia Tech
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

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import junit.framework.AssertionFailedError;
import net.sf.webcat.MutableStringBufferInputStream;
import net.sf.webcat.PrintStreamWithHistory;
import net.sf.webcat.PrintWriterWithHistory;
import net.sf.webcat.StringNormalizer;
import net.sf.webcat.SystemIOUtilities;

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
 *
 *  @author  Stephen Edwards
 *  @version 2007.09.12
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
    private MutableStringBufferInputStream tcInBuf = null;
    private StringNormalizer       sn = new StringNormalizer(true);

    // Used for communicating with assertTrue() and assertFalse().  Ideally,
    // they should be instance vars, but assertTrue() and assertFalse()
    // have to be static so these messages must be too.
    private static String predicateReturnsTrueReason;
    private static String predicateReturnsFalseReason;

    private static Boolean trimStackTraces;


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

    @Override
    public void runBare()
        throws Throwable
    {
        predicateReturnsTrueReason = null;
        predicateReturnsFalseReason = null;
        instrumentIO();
        super.runBare();
        resetIO();
    }


    // ----------------------------------------------------------
    /**
     * Sets up the fixture, for example, open a network connection. This
     * method is called before each test in this class is executed.
     */
    @Override
    protected void setUp()
        throws Exception
    {
        // Included only for Javadoc purposes--implementation adds nothing.
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
        // Included only for Javadoc purposes--implementation adds nothing.
        super.tearDown();
    }


    // ----------------------------------------------------------
    /**
     * An internal helper that ensures input/output buffering is in place
     * before each test case.
     */
    protected void instrumentIO()
    {
        // Clear out all the stream history stuff
        tcIn = null;
        tcOut = null;
        tcInBuf = null;

        // Make sure these are history-wrapped
        SystemIOUtilities.out();
        SystemIOUtilities.err();

        // First, make sure the original System.in gets captured, so it
        // can be restored later
        SystemIOUtilities.replaceSystemInContents(null);

        // The previous line actually replaced System.in, but now we'll
        // "replace the replacement" with one that uses fail() if it
        // has no contents.
        System.setIn(new MutableStringBufferInputStream((String)null)
        {
            protected void handleMissingContents()
            {
                fail("Attempt to access System.in before its contents "
                    + "have been set");
            }
        });
    }


    // ----------------------------------------------------------
    /**
     * An internal helper that resets all of the input/output buffering
     * after each test case.
     */
    protected void resetIO()
    {
        // Clear out all the stream history stuff
        tcIn = null;
        tcOut = null;
        tcInBuf = null;

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
        if (tcIn == null)
        {
            setIn((String)null);
        }
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
        if (tcInBuf == null)
        {
            tcInBuf = new MutableStringBufferInputStream(contents)
            {
                protected void handleMissingContents()
                {
                    fail("Attempt to access built-in test case Scanner "
                        + "in() before its contents have been set");
                }
            };
            tcIn = new Scanner(tcInBuf);
        }
        else
        {
            tcInBuf.resetContents(contents);
        }
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
        tcInBuf = null;
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
        try
        {
            assertEquals(
                message, stringNormalizer().normalize(expected),
                stringNormalizer().normalize(actual));
        }
        catch (AssertionFailedError e)
        {
            trimStack(e);
            throw e;
        }
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
        if (falseReason != null)
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
        try
        {
            junit.framework.TestCase.assertTrue(message, condition);
        }
        catch (AssertionFailedError e)
        {
            trimStack(e);
            throw e;
        }
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
        if (trueReason != null)
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
        try
        {
            junit.framework.TestCase.assertFalse(message, condition);
        }
        catch (AssertionFailedError e)
        {
            trimStack(e);
            throw e;
        }
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
     * match a substring, use {@link #containsRegex(String,Pattern)}
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
     * Determine whether one String contains a sequence of other substrings
     * in order.  In addition to the string to search, you can provide an
     * arbitrary number of additional parameters to search for.  If you only
     * provide one substring, this method behaves the same as
     * {@link String#contains(CharSequence)}.  If you provide more than
     * one substring, it looks for each such element in turn
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
     * @param substrings   One or more substrings to look for (in order)
     * @return True if the largerString contains all of the specified
     * substrings in order.
     */
    public boolean contains(String largerString, String ... substrings)
    {
        int pos = (largerString == null) ? -1 : 0;
        for (int i = 0; i < substrings.length  &&  pos >= 0; i++)
        {
            pos = largerString.indexOf(substrings[i], pos);
            if (pos >= 0)
            {
                pos += substrings[i].length();
            }
            else
            {
                predicateReturnsFalseReason =
                    "<" + compact(largerString) + "> does not contain:<"
                    + compact(substrings[i], 25, 10) + ">";
                if (substrings.length > 1)
                {
                    predicateReturnsFalseReason += "(substring " + i + ")";
                }
                break;
            }
        }
        if (pos >= 0)
        {
            predicateReturnsTrueReason =
                "<" + compact(largerString) + "> contains:";
            for (int i = 0; i < substrings.length; i++)
            {
                if (i > 0)
                {
                    predicateReturnsTrueReason += ", ";
                }
                predicateReturnsTrueReason +=
                    "<" + compact(substrings[i], 25, 10) + ">";
            }
            return true;
        }
        else
        {
            return false;
        }
    }


    // ----------------------------------------------------------
    /**
     * Determine whether one String contains a sequence of other substrings
     * in order, respecting preferences for what differences matter.  It
     * looks for each of the specified substrings in turn
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
     * @param largerString The target to look in
     * @param substrings   The substrings to look for (in order)
     * @return True if the largerString contains all of the specified
     * substrings in order.
     */
    public boolean fuzzyContains(String largerString, String ... substrings)
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
     * Determine whether one String contains a sequence of other substrings
     * in order, where the expected substrings are specified as a regular
     * expressions.  It looks for each of the specified regular expressions
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
     * @param substrings   A sequence of expected substrings (interpreted as
     *                     regular expression {@link Pattern}s), which must
     *                     occur in the same order in the larger string
     * @return True if the largerString contains all of the specified
     * regular expressions in order.
     */
    public boolean containsRegex(String largerString, String ... substrings)
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
     * expressions.  It looks for each of the specified regular expressions
     * in turn in the larger string, making sure they are all found in the
     * proper order (each substring must strictly follow the previous one,
     * although there can be any amount of intervening characters between
     * any two substrings in the array).  If the larger string is null, this
     * method returns false (since it can contain nothing).
     * @param largerString The target to look in
     * @param substrings   A sequence of expected regular expressions, which
     *                     must occur in the same order in the larger string
     * @return True if the largerString contains all of the specified
     * regular expressions in order.
     */
    public boolean containsRegex(String largerString, Pattern ... substrings)
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
                if (substrings.length > 1)
                {
                    predicateReturnsFalseReason += "(pattern " + i + ")";
                }
                break;
            }
            pos = matcher.end();
        }
        if (result)
        {
            predicateReturnsTrueReason =
                "<" + compact(largerString) + "> contains regexes:";
            for (int i = 0; i < substrings.length; i++)
            {
                if (i > 0)
                {
                    predicateReturnsTrueReason += ", ";
                }
                predicateReturnsTrueReason +=
                    "<" + compact(substrings[i].toString(), 25, 10) + ">";
            }
            return true;
        }
        else
        {
            return false;
        }
    }


    // ----------------------------------------------------------
    /**
     * Determine whether one String contains a sequence of other substrings
     * in order, where the expected substrings are specified as regular
     * expressions, and respecting preferences for what differences matter.
     * This method behaves just like {@link #fuzzyContains(String,String...)},
     * except that the second argument is interpreted as an array of regular
     * expressions.  String normalization rules are only appled to the
     * larger string, not to the regular expressions.
     * @param largerString The target to look in
     * @param substrings   An array of expected substrings (interpreted as
     *                     regular expression {@link Pattern}s), which must
     *                     occur in the same order in the larger string
     * @return True if the largerString contains all of the specified
     * substrings in order.
     */
    public boolean fuzzyContainsRegex(String largerString, String ... substrings)
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
     * This method behaves just like {@link #fuzzyContains(String,String...)},
     * except that the second argument is interpreted as an array of regular
     * expressions.  String normalization rules are only appled to the
     * larger string, not to the regular expressions.
     * @param largerString The target to look in
     * @param substrings   An array of expected substrings, which must
     *                     occur in the same order in the larger string
     * @return True if the largerString contains all of the specified
     * substrings in order.
     */
    public boolean fuzzyContainsRegex(String largerString, Pattern ... substrings)
    {
        return containsRegex(
            stringNormalizer().normalize(largerString), substrings);
    }


    // ----------------------------------------------------------
    private static void trimStack(Throwable t)
    {
        if (trimStackTraces == null)
        {
            try
            {
                String setting =
                    System.getProperty("student.TestCase.trimStackTraces");
                if (setting == null)
                {
                    trimStackTraces = true;
                }
                else
                {
                    setting = setting.toLowerCase().trim();
                    trimStackTraces = "yes".equals(setting)
                        || "true".equals(setting)
                        || "on".equals(setting)
                        || "1".equals(setting);
                }
            }
            catch (Exception e)
            {
                trimStackTraces = true;
            }
        }

        if (!trimStackTraces)
        {
            return;
        }

        StackTraceElement[] oldTrace = t.getStackTrace();
        int pos1 = 0;
        while (pos1 < oldTrace.length
            && (oldTrace[pos1].getClassName().equals("junit.framework.Assert")))
        {
            ++pos1;
        }
        int pos2 = pos1;
        while (pos2 < oldTrace.length
            && (oldTrace[pos2].getClassName().equals("student.TestCase")))
        {
            ++pos2;
        }

        if (pos2 > pos1 && pos2 < oldTrace.length - 1)
        {
            StackTraceElement[] newTrace =
                new StackTraceElement[oldTrace.length - (pos2 - pos1)];
            if (pos1 > 0)
            {
                System.arraycopy(oldTrace, 0, newTrace, 0, pos1);
            }
            System.arraycopy(
                oldTrace, pos2, newTrace, pos1, newTrace.length - pos1);
            t.setStackTrace(newTrace);
        }
    }
}
