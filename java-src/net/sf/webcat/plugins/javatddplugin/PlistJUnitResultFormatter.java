/*==========================================================================*\
 |  $Id: PlistJUnitResultFormatter.java,v 1.9 2016/09/06 12:18:31 stedwar2 Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006-2010 Virginia Tech
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

package net.sf.webcat.plugins.javatddplugin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest;
import org.apache.tools.ant.util.StringUtils;

//-------------------------------------------------------------------------
/**
 *  A custom formatter for the ANT junit task that prints results
 *  in plist format (Apple-style property lists).
 *
 *  @author Stephen Edwards
 *  @author Last changed by $Author: stedwar2 $
 *  @version $Revision: 1.9 $, $Date: 2016/09/06 12:18:31 $
 */
public class PlistJUnitResultFormatter
    extends PerlScoringJUnitResultFormatter
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Default constructor.
     */
    public PlistJUnitResultFormatter()
    {
        // Nothing to construct
    }


    //~ Public Methods ........................................................

    // ----------------------------------------------------------
    /**
     * @see JUnitResultFormatter#startTestSuite(JUnitTest)
     */
    /** {@inheritDoc}. */
    public void startTestSuite( JUnitTest suite )
    {
        super.startTestSuite( suite );
        currentSuite = suite;
    }


    // ----------------------------------------------------------
    /**
     * @see TestListener#startTest(Test)
     */
    /** {@inheritDoc}. */
    public void startTest( Test test )
    {
        super.startTest( test );
        testPassed = true;
    }


    // ----------------------------------------------------------
    /**
     * @see TestListener#endTest(Test)
     */
    /** {@inheritDoc}. */
    public void endTest( Test test )
    {
        super.endTest( test );
        if ( testPassed )
        {
            formatTestResultAsPlist( test, null );
        }
    }


    // ----------------------------------------------------------
    /**
     * @see TestListener#addFailure(Test,AssertionFailedError)
     */
    /** {@inheritDoc}. */
    public void addFailure( Test test, AssertionFailedError t )
    {
        super.addFailure( test, t );
        testPassed = false;
        formatTestResultAsPlist( test, t );
    }


    // ----------------------------------------------------------
    /**
     * @see TestListener#addError(Test,Throwable)
     */
    /** {@inheritDoc}. */
    public void addError( Test test, Throwable error )
    {
        super.addError( test, error );
        testPassed = false;
        formatTestResultAsPlist( test, error );
    }


    // ----------------------------------------------------------
    /**
     * Escapes perl string interpolation characters.
     * @param text the input string to escape
     * @return the escaped version of text
     */
    public String perlEscape( String text )
    {
        if ( text == null )
        {
            return null;
        }
        return text.replaceAll( "([@$%#\"\\\\])", "\\\\$1" );
    }


    // ----------------------------------------------------------
    /**
     * Produces a perl string literal from the given string.
     * @param text the input string to convert
     * @return the corresponding string literal
     */
    public String perlStringLiteral(String text)
    {
        if (text == null)
        {
            return "''";
        }
        return "'" + text.replaceAll( "'", "\\\\'" ) + "'";
    }


    //~ Protected Methods .....................................................

    /** A simple record storing information about a test outcome. */
    protected static class TestResultDescriptor
    {
        /** The suite for this test case. */
        public JUnitTest suite;
        /** The test that has completed. */
        public Test      test;
        /** An associated exception object, or null. */
        public Throwable error;
        /** An error code. */
        public int       code;
        /** The associated error level. */
        public int       level;
        /** A priorty level (greater number == higher priority). */
        public int       priority = 0;
        /** A message associated with the exception object, if any. */
        public String    message;
        /** A stack trace associated with the exception object, if any. */
        public String    stackTrace;

        /**
         * Create a new descriptor.
         * @param suite
         * @param test
         * @param error
         * @param code
         * @param level
         * @param message
         */
        public TestResultDescriptor(
            JUnitTest suite,
            Test      test,
            Throwable error,
            int       code,
            int       level,
            String    message,
            String    stackTrace)
        {
            this.suite   = suite;
            this.test    = test;
            this.error   = error;
            this.code    = code;
            this.level   = level;
            this.message = message;
            this.stackTrace = stackTrace;
        }

    }


    // ----------------------------------------------------------
    /**
     * Generate a descriptor summarizing a test result.
     * @param test the test to print
     * @param error the exception produced by the test, or null
     * @return the descriptor
     */
    protected TestResultDescriptor describe( Test test, Throwable error )
    {
        int code  = codeOf( error );
        int level = levelOf( code );
        String msg = null;
        if ( error !=  null )
        {
            if ( level == 4
                 && error instanceof junit.framework.AssertionFailedError
                 && error.getCause() != null )
            {
                error = error.getCause();
            }
            msg = error.getMessage();
            if ( msg != null )
            {
                msg = msg.replaceAll( "\"", "\\\"" );
                if (code == 2 && msg.length() > MAX_MSG_LENGTH)
                {
                    // Comparison failure.  Trim possibly excessive match text.
                    Matcher m = EXPECTED_ACTUAL.matcher(msg);
                    if (m.find())
                    {
                        String prefix = msg.substring(0, m.start(1));
                        String expected = m.group(1);
                        String middle = msg.substring(m.end(1), m.start(4));
                        String actual = m.group(4);
                        String suffix = msg.substring(m.end(4));

                        // Compact the expected value, if necessary
                        String innerExpected = m.group(3);
                        if (innerExpected != null
                            && innerExpected.length() > MAX_MSG_LENGTH)
                        {
                            expected = msg.substring(m.start(1), m.start(3))
                                + innerExpected.substring(0, PREFIX_SIZE)
                                + "[...]"
                                + innerExpected.substring(
                                    innerExpected.length() - PREFIX_SIZE - 1)
                                + msg.substring(m.end(3), m.start(4));
                        }
                        else if (expected.length() > MAX_MSG_LENGTH)
                        {
                            expected = expected.substring(0, PREFIX_SIZE)
                            + "[...]"
                            + expected.substring(
                                expected.length() - PREFIX_SIZE - 1);
                        }

                        // Compact the actual value, if necessary
                        String innerActual = m.group(3);
                        if (innerActual != null
                            && innerActual.length() > MAX_MSG_LENGTH)
                        {
                            int prefixSize = (COMPACT_LENGTH - 5) / 2;
                            expected = msg.substring(m.start(1), m.start(3))
                                + innerActual.substring(0, prefixSize)
                                + "[...]"
                                + innerActual.substring(
                                    innerActual.length() - prefixSize - 1)
                                + msg.substring(m.end(3), m.start(4));
                        }
                        else if (actual.length() > MAX_MSG_LENGTH)
                        {
                            actual = actual.substring(0, PREFIX_SIZE)
                            + "[...]"
                            + actual.substring(
                                actual.length() - PREFIX_SIZE - 1);
                        }

                        // Reassemble the message from its parts
                        msg = prefix + expected + middle + actual + suffix;
                    }
                }
                if ( level > 2 )
                {
                    msg = error.getClass().getName() + ": " + msg;
                }
            }
            else
            {
                msg = error.getClass().getName();
            }
            String testName = "" + test;  // avoiding null dereference
            if ( level == 2
                 && testName.startsWith( "warning(" )
                 && msg.startsWith( "No tests found in " ) )
            {
                code = 10; // No tests in test class
            }
        }

        return new TestResultDescriptor(
            currentSuite, test, error, code, level, msg,
            stackTraceMessage(error, false));
    }


    // ----------------------------------------------------------
    /**
     * Format and print out a plist dictionary summarizing a test result.
     * @param test the test to print
     * @param error the exception produced by the test, or null
     */
    protected void formatTestResultAsPlist(Test test, Throwable error)
    {
        formatTestResultAsPlist(describe(test, error));
    }


    // ----------------------------------------------------------
    private void appendResults(char c)
    {
        testResultsPlist.append(c);
        testResultsPerlList.append(c);
    }


    // ----------------------------------------------------------
    private void appendResults(String str)
    {
        testResultsPlist.append(str);
        testResultsPerlList.append(str);
    }


    // ----------------------------------------------------------
    private void appendResultsQuotedValue(String str)
    {
        if (str == null)
        {
            str = "";
        }
        if (str.length() > 1024)
        {
            str = str.substring(0, 1024) + "...";
        }

        testResultsPlist.append('"');
        testResultsPlist.append(str.replace("\"", "\\\""));
        testResultsPlist.append('"');

        testResultsPerlList.append('\'');
        testResultsPerlList.append(str.replace("'", "\\'"));
        testResultsPerlList.append('\'');
    }


    // ----------------------------------------------------------
    private void appendResultsValue(int value)
    {
        appendResults(Integer.toString(value));
    }


    // ----------------------------------------------------------
    private void appendResultsLabel(String str)
    {
        testResultsPlist.append(str);
        testResultsPlist.append('=');

        testResultsPerlList.append('\'');
        testResultsPerlList.append(str);
        testResultsPerlList.append("'=>");
    }


    // ----------------------------------------------------------
    private void appendResultsValueSeparator()
    {
        testResultsPlist.append(';');
        testResultsPerlList.append(',');
        if (debugFormat) appendResults("\n\t");
    }


    // ----------------------------------------------------------
    /**
     * Format and print out a plist dictionary summarizing a test result.
     * @param result
     */
    protected void formatTestResultAsPlist(TestResultDescriptor result)
    {
        appendResults('{');
        if (debugFormat) appendResults("\n\t");

        appendResultsLabel("suite");
        appendResultsQuotedValue(result.suite.getName());
        appendResultsValueSeparator();

        appendResultsLabel("test");
        String testName = "";
        if ( result.test != null )
        {
            testName = result.test.toString();
            int pos = testName.indexOf( "(" );
            if ( pos >= 0 )
            {
                testName = testName.substring( 0, pos );
            }
        }
        appendResultsQuotedValue(testName);
        appendResultsValueSeparator();

        appendResultsLabel("level");
        appendResultsValue(result.level);
        appendResultsValueSeparator();

        appendResultsLabel("code");
        appendResultsValue(result.code);
        appendResultsValueSeparator();

        appendResultsLabel("priority");
        appendResultsValue(result.priority);
        appendResultsValueSeparator();

        if (result.message != null)
        {
            appendResultsLabel("message");
            appendResultsQuotedValue(result.message);
            appendResultsValueSeparator();
        }
        if (result.error != null)
        {
            appendResultsLabel("exception");
            appendResultsQuotedValue(result.error.getClass().getName());
            appendResultsValueSeparator();
        }
        if (result.stackTrace != null)
        {
            appendResultsLabel("trace");
            appendResultsQuotedValue(result.stackTrace);
            appendResultsValueSeparator();
        }
        appendResults("},");
        if (debugFormat) appendResults("\n");
    }


    // ----------------------------------------------------------
    /**
     * @see PerlScoringJUnitResultFormatter#outputForSuite(StringBuffer,JUnitTest)
     */
    /** {@inheritDoc}. */
    protected void outputForSuite( StringBuffer buffer, JUnitTest suite )
    {
        super.outputForSuite( buffer, suite );
        buffer.append( "# Suite: " );
        buffer.append( currentSuite.getName() );
        buffer.append( StringUtils.LINE_SEP );
        buffer.append( "$results->addToPlist( <<PLIST );");
        buffer.append( StringUtils.LINE_SEP );
        buffer.append( perlEscape( testResultsPlist.toString() ) );
        buffer.append( StringUtils.LINE_SEP );
        buffer.append( "PLIST");
        buffer.append( StringUtils.LINE_SEP );
        buffer.append( "$results->addToPerlList( <<PERLLIST );");
        buffer.append( StringUtils.LINE_SEP );
        buffer.append( perlEscape( testResultsPerlList.toString() ) );
        buffer.append( StringUtils.LINE_SEP );
        buffer.append( "PERLLIST");
        buffer.append( StringUtils.LINE_SEP );
        testResultsPlist.setLength( 0 );
    }


    // ----------------------------------------------------------
    /**
     * Look up the error code associated with an exception.
     * @param error the exception to code, or null
     * @return the code
     */
    protected int codeOf( Throwable error )
    {
        if ( error == null ) return 1;

        // First-pass code assignment is made by the code table
        int code = 0;
        for ( int i = 0; i < codeTable.length; i++ )
        {
            if ( codeTable[i] != null )
            {
                if (codeTable[i].isAssignableFrom(error.getClass())
                    || codeTable[i].getName().equals(error.getClass().getName()))
                {
                    // error instanceof codeTable[i]
                    code = i;
                    break;
                }
            }
        }

        // If it is a test case failure, we cannot use the exception type
        // alone, so we must break down the message to refine the code
        if (error instanceof AssertionFailedError)
        {
            // Older JUnit 3.x-style errors
            // Also works with newer 4.x-style errors, because of the
            // adapters used by the ANT JUnit task
            if (error instanceof junit.framework.ComparisonFailure
                || error instanceof org.junit.ComparisonFailure
                || (error.getCause() != null
                   && error.getCause() instanceof org.junit.ComparisonFailure))
            {
                code = 2;
            }
            else
            {
                code = 13;
                StackTraceElement[] trace = error.getStackTrace();
                String methodName = null;
                int pos = 0;
                if ( error.getCause() != null )
                {
                    // Then it is a 4.x-style error, wrapped by the ANT 1.7
                    // JUnit test adapter
                    trace = error.getCause().getStackTrace();
                }

                pos = findLast( trace, 0, "junit.framework.Assert" );
                if ( pos < trace.length
                     && trace[pos].getClassName().equals(
                         "junit.framework.Assert" ) )
                {
                    methodName = trace[pos].getMethodName();
                }
                else
                {
                    pos = findLast( trace, 0, "student.TestCase" );
                    if ( pos < trace.length
                         && trace[pos].getClassName().equals(
                              "student.TestCase" ) )
                    {
                        methodName = trace[pos].getMethodName();
                    }
                    else
                    {
                        pos = findLast( trace, 0, "org.junit.Assert" );
                        if ( pos < trace.length
                             && trace[pos].getClassName().equals(
                                 "org.junit.Assert" ) )
                        {
                            methodName = trace[pos].getMethodName();
                        }
                    }
                }
                if ( methodName != null )
                {
                    code = assertFailCodeOf( methodName );

                    // Next, check for a fuzzy equals
                    pos++;
                    if ( pos < trace.length
                         && trace[pos].getClassName().equals(
                             "net.sf.webcat.junit.Assert" ) )
                    {
                        code = 10;
                    }

                    // Last, check for a custom assert
                    pos = findFirst( trace, pos, currentSuite.getName() );
                    if ( pos < trace.length
                         && trace[pos].getMethodName().startsWith( "assert" ) )
                    {
                        // custom assert
                        code = 11;
                    }
                }
                else if ( pos < trace.length )
                {
                    // Must be a wrapped AssertionError from some other
                    // code
                    code = 29;
                }
            }
        }

        return code;
    }


    // ----------------------------------------------------------
    /**
     * Look up the error level for a given error code.
     * @param code the code to look up
     * @return the corresponding error level
     */
    protected int levelOf( int code )
    {
        if      ( code <= 1 )  return 1;
        else if ( code <= 13 ) return 2;
        else if ( code <= 28 ) return 3;
        else if ( code <= 33 ) return 4;
        else                   return 5;
    }


    // ----------------------------------------------------------
    /**
     * Search a stack trace for the next occurrence of any method from
     * a given class.  The search begins at the specified position,
     * and advances as long as stack trace elements are from other classes.
     * @param stack the stack trace to look in
     * @param pos the position in the stack trace array to start looking
     * @param className the class to look for
     * @return the index of the first call to any method in the specified
     * class.  Returns length + 1 if no such call exists.
     */
    protected int findFirst(
        StackTraceElement[] stack, int pos, String className )
    {
        while ( pos < stack.length
                && !stack[pos].getClassName().equals( className ) )
        {
            pos++;
        }
        return pos;
    }


    // ----------------------------------------------------------
    /**
     * Search a stack trace for the deepest contiguous occurrence of any
     * method from a given class.  The search begins at the specified position,
     * and advances as long as stack trace elements are located in the
     * named class.
     * @param stack the stack trace to look in
     * @param pos the position in the stack trace array to start looking
     * @param className the class to look for
     * @return the index of the deepest call of the contiguous block of
     * calls to any methods in the specified class.  Returns pos if the
     * given stack trace location doesn't belong to the specified class.
     */
    protected int findLast(
        StackTraceElement[] stack, int pos, String className )
    {
        if ( stack[pos].getClassName().equals( className ) )
        {
            pos++;
            while ( pos < stack.length
                    && stack[pos].getClassName().equals( className ) )
            {
                pos++;
            }
            pos--;
        }
        return pos;
    }


    // ----------------------------------------------------------
    /**
     * Look up the error code for a given JUnit assert method name.
     * @param name the method name to look up
     * @return the corresponding error level
     */
    protected int assertFailCodeOf( String name )
    {
        for ( int i = 0; i < assertMethodTable.length; i++ )
        {
            if ( assertMethodTable[i].equals( name ) )
            {
                return i + 3;
            }
        }
        return assertMethodTable.length + 2;
    }


    // ----------------------------------------------------------
    /**
     * Get a printable, filtered stack trace.
     * @param error the throwable containing the stack trace
     * @param filterSuite if true, the test suite's stack frame will also
     * be removed
     * @return the formatted stack trace
     */
    protected String stackTraceMessage(Throwable error, boolean filterSuite)
    {
        if (error == null)
        {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        sb.append(error);
        sb.append(StringUtils.LINE_SEP);
        while (error.getCause() != null)
        {
            error = error.getCause();
        }
        String suiteName = currentSuite.getName();
        int frameCount = 0;
        for (StackTraceElement frame : error.getStackTrace())
        {
            ++frameCount;
            if (frameCount > 20)
            {
                sb.append("... ");
                sb.append(Integer.toString(
                    error.getStackTrace().length - frameCount + 1));
                sb.append(" more omitted\n");
                break;
            }
            boolean inSuite =
                suiteName != null && suiteName.equals(frame.getClassName());
            if (inSuite && filterSuite)
            {
                break;
            }
            else if (!filterOut(frame))
            {
                sb.append("at ");
                sb.append(frame.getClassName());
                sb.append('.');
                sb.append(frame.getMethodName());
                String fileName = frame.getFileName();
                if (fileName != null)
                {
                    sb.append("(");
                    // Remove directory component for safety
                    int pos = fileName.lastIndexOf('/');
                    if (pos >= 0)
                    {
                        fileName = fileName.substring(pos + 1);
                    }
                    pos = fileName.lastIndexOf('\\');
                    if (pos >= 0)
                    {
                        fileName = fileName.substring(pos + 1);
                    }
                    sb.append(fileName);
                    int lineNo = frame.getLineNumber();
                    if (lineNo > 0)
                    {
                        sb.append(':');
                        sb.append(lineNo);
                    }
                    sb.append(")");
                }
                sb.append(StringUtils.LINE_SEP);
            }
            if (inSuite)
            {
                break;
            }
        }
        return sb.toString();
    }


    // ----------------------------------------------------------
    /**
     * Determine if a stack trace element should be excluded from.
     * A student-visible stack trace.
     * @param frame the stack trace element to match against
     * @return true if the frame should be hidden/removed/filtered
     */
    protected boolean filterOut(StackTraceElement frame)
    {
        return matches(frame, defaultStackFilters);
    }


    // ----------------------------------------------------------
    /**
     * Check a stack trace element against a list of filters.
     * @param frame the stack trace element to match against
     * @param filters a list of class prefixes to check for
     * @return true if the frame matches any filter in the list
     */
    protected boolean matches(StackTraceElement frame, String[] filters)
    {
        if (filters == null || filters.length == 0)
        {
            return false;
        }
        String frameClass = frame.getClassName();
        for (String filter : filters)
        {
            if (frameClass.startsWith(filter))
            {
                return true;
            }
        }
        return false;
    }


    //~ Instance/static variables .............................................

    /** The current suite name. */
    protected JUnitTest currentSuite = null;

    /** Records the status of the current test. */
    protected boolean testPassed = true;

    /** Records the status of the current test. */
    protected StringBuffer testResultsPlist = new StringBuffer();

    /** Records the status of the current test. */
    protected StringBuffer testResultsPerlList = new StringBuffer();

    /**
     * If true, extra newlines and tabs will be produced in the plist output.
     * */
    private static final boolean debugFormat = false;

    /** A lookup table for determining error codes. */
    private static final String[] assertMethodTable = {
        "assertEquals",
        "assertFalse",
        "assertNotNull",
        "assertNotSame",
        "assertNull",
        "assertSame",
        "assertTrue",
        "assertFuzzyEquals",
        "custom assert",
        "fail"
    };

    /** A lookup table for determining error codes. */
    private static final Class<?>[] codeTable = {
        // Nothing matches the zero case, EVER!
        null,                                               //    0
        // Nothing matches the pass case
        null,                                               //    1

        // Test case failures
        org.junit.ComparisonFailure.class,                  //    2
        null,                                               //    3
        null,                                               //    4
        null,                                               //    5
        null,                                               //    6
        null,                                               //    7
        null,                                               //    8
        null,                                               //    9
        null,                                               //   10
        null,                                               //   11
        null,                                               //   12
        junit.framework.AssertionFailedError.class,         //   13
        // The class above even works for JUnit 4.x, in the current ANT
        // JUnit task.

        // unchecked exceptions
        ArithmeticException.class,                          //   14
        ClassCastException.class,                           //   15
        java.util.ConcurrentModificationException.class,    //   16
        java.util.EmptyStackException.class,                //   17
        IllegalArgumentException.class,                     //   18
        IllegalStateException.class,                        //   19
        IndexOutOfBoundsException.class,                    //   20
        java.util.MissingResourceException.class,           //   21
        NegativeArraySizeException.class,                   //   22
        java.util.NoSuchElementException.class,             //   23
        NullPointerException.class,                         //   24
        SecurityException.class,                            //   25
        TypeNotPresentException.class,                      //   26
        UnsupportedOperationException.class,                //   27
        RuntimeException.class,                             //   28

        // Errors
        student.testingsupport.ReflectionSupport.ReflectionError.class, // 29
        AssertionError.class,                               //   30
        OutOfMemoryError.class,                             //   31
        StackOverflowError.class,                           //   32
        Error.class,                                        //   33

        // checked exceptions
        ClassNotFoundException.class,                       //   34
        CloneNotSupportedException.class,                   //   35
        java.security.GeneralSecurityException.class,       //   36
        IllegalAccessException.class,                       //   37
        InstantiationException.class,                       //   38
        java.io.IOException.class,                          //   39
        java.text.ParseException.class,                     //   40
        java.net.URISyntaxException.class,                  //   41
        Exception.class,                                    //   42
        Throwable.class                                     //   43
    };

    private static final String[] defaultStackFilters = {
        // JUnit 4 support:
        "org.junit.",
        // JUnit 3 support:
        "junit.",
        "java.",
        "javax.",
        "sun.",
        "org.apache.",
        // Web-CAT infrastructure
        "net.sf.webcat.",
        "student.",
        "sofia.",
        "cs1705.TestCase"
    };

    private static final int MAX_MSG_LENGTH = 256;
    private static final int COMPACT_LENGTH = 73;
    private static final int PREFIX_SIZE = (COMPACT_LENGTH - 5) / 2;
    private static final Pattern EXPECTED_ACTUAL = Pattern.compile(
        "expected:\\s*<([^\\[]*(\\[(.*)\\].*)?)> but "
        + "was:\\s*<([^\\[]*(\\[(.*)\\].*)?)>$", Pattern.DOTALL);
}
