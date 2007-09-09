package net.sf.webcat;

import java.io.BufferedReader;
import java.io.StringReader;

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
 *  @author  Stephen Edwards (based on Petr Skoda's original)
 *  @version 2007.08.12
 */
public class TestCase
    extends junit.framework.TestCase
{
    //~ Instance/static variables .............................................

    // These don't use the names "in" or "out" to provide better error
    // messages if students type those method names and accidentally leave
    // off the parens.
    private PrintWriterWithHistory tcOut;
    private BufferedReader         tcIn;
    private StringNormalizer       sn = new StringNormalizer(true);
    private PrintWriterWithHistory tcSystemOut;
    private PrintWriterWithHistory tcSystemErr;


    //~ Constructor ...........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new TestCase object.
     */
    public TestCase()
    {
        super();
    }


    // ----------------------------------------------------------
    /**
     * Creates a new TestCase object.
     * @param name The name of this test case
     */
    public TestCase(String name)
    {
        super(name);
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
        // Clear out all the stream history stuff
        tcOut = null;
        tcIn = null;
        SystemIOUtilities.restoreSystemIn();
        SystemIOUtilities.restoreSystemOut();
        SystemIOUtilities.restoreSystemErr();
        super.tearDown();
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
     * @return a {@link java.io.PrintWriter} connected to System.out that
     * is suitable for use in test cases
     */
    public PrintWriterWithHistory systemOut()
    {
        if (tcSystemOut == null)
        {
            tcSystemOut = new PrintWriterWithHistory(System.out);
        }
        return tcSystemOut;
    }


    // ----------------------------------------------------------
    /**
     * Get a version of {@link System#err} that records its history
     * so you can compare against it later.  The history of this
     * stream gets cleared for every test case.
     * @return a {@link java.io.PrintWriter} connected to System.err that
     * is suitable for use in test cases
     */
    public PrintWriterWithHistory systemErr()
    {
        if (tcSystemErr == null)
        {
            tcSystemErr = new PrintWriterWithHistory(System.err);
        }
        return tcSystemErr;
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
        String differenceMessage = "(after normalizing strings)";
        if (message != null)
        {
            differenceMessage = message + ": ";
        }
        assertEquals(
            differenceMessage, sn.normalize(expected), sn.normalize(actual));
    }
}
