/*==========================================================================*\
 |  $Id: HintingJUnitResultFormatter.java,v 1.12 2016/09/06 12:17:24 stedwar2 Exp $
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

import java.util.regex.Pattern;
import junit.framework.Test;
import org.apache.tools.ant.taskdefs.optional.junit.*;
import org.apache.tools.ant.util.*;

//-------------------------------------------------------------------------
/**
 *  A custom formatter for the ANT junit task that collects test failure
 *  hints for use by a Perl-based hint formatting engine.
 *
 *  @author Stephen Edwards
 *  @author Last changed by $Author: stedwar2 $
 *  @version $Revision: 1.12 $, $Date: 2016/09/06 12:17:24 $
 */
public class HintingJUnitResultFormatter
    extends PlistJUnitResultFormatter
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Default constructor.
     */
    public HintingJUnitResultFormatter()
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
        suiteOptions = null;
        if ( output != null )
        {
            synchronized ( output )
            {
                output.write( StringUtils.LINE_SEP );
                output.write(
                    "# Suite: " + suite.getName() + StringUtils.LINE_SEP );
                output.write( "# ---------------" + StringUtils.LINE_SEP );
            }
        }
    }


    // ----------------------------------------------------------
    /**
     * @see JUnitResultFormatter#startTest(Test)
     */
    /** {@inheritDoc}. */
    public void startTest( Test test )
    {
        super.startTest( test );
        testOptions = null;
    }


    //~ Protected Methods .....................................................

    // ----------------------------------------------------------
    /**
     * Get the HintOptions object for the current test suite.
     * @return the suite's hint options object
     */
    protected TestSuiteOptions suiteOptions()
    {
        if ( suiteOptions == null )
        {
            suiteOptions = new TestSuiteOptions( currentSuite );
        }
        return suiteOptions;
    }


    // ----------------------------------------------------------
    /**
     * Get the HintOptions object for the specified test case.
     * @param test the test case to look up
     * @return the case's hint options object
     */
    protected TestOptions testOptionsFor( Test test )
    {
        if ( testOptions != null && testOptions.test() != test )
        {
            testOptions = null;
        }

        if ( testOptions == null )
        {
            testOptions = new TestOptions( suiteOptions(), test );
        }
        return testOptions;
    }


    // ----------------------------------------------------------
    protected double scoringWeightOf( Test test )
    {
        return testOptionsFor( test ).scoringWeight();
    }


    // ----------------------------------------------------------
    protected void outputForSuite(StringBuffer buffer, JUnitTest suite)
    {
        // recalibrate the test weighting if necessary
        if (suiteOptions().hasScoringWeight())
        {
            numFailed =
                numFailed / numExecuted * suiteOptions().scoringWeight();
            numExecuted = suiteOptions().scoringWeight();
        }
        super.outputForSuite(buffer, suite);
    }


    // ----------------------------------------------------------
    protected String removeClassNameFromMessage( String msg, Object object )
    {
        if ( object != null && msg != null )
        {
            String className = object.getClass().getName();
            if ( msg.equals( className ) )
            {
                msg = null;
            }
            else
            {
                if ( msg.startsWith( className ) )
                {
                    msg = msg.substring( className.length() );
                    if ( msg.startsWith( ":" ) )
                    {
                        msg = msg.substring( 1 ).trim();
                    }
                }
                if ( "".equals( msg ) )
                {
                    msg = null;
                }
            }
        }
        return msg;
    }


    // ----------------------------------------------------------
    protected TestResultDescriptor describe(Test test, Throwable error)
    {
        TestResultDescriptor result = super.describe(test, error);
        result.message = removeClassNameFromMessage(result.message, error);
        if (result.message != null && "null".equals(result.message.trim()))
        {
            result.message = null;
        }
        if (error != null)
        {
            // System.out.println(
            //    "generating hint for: '" + result.message);
            // Figure out hint text
            String hint = null;
            TestPhase phase = stoppedInPhase(error);
            int mandatory =
                (phase != TestPhase.TEST_CASE
                  || (result.level == 4 && result.code != 29))
                ? 1 : 0;  // AssertionError

            // Provide a mandatory hint in setup/teardown phases
            switch (phase)
            {
                case SETUP:
                    hint = "Failure during test case setup";
                    break;
                case TEAR_DOWN:
                    hint = "Failure during test case tear down";
                    break;
                case CLASS_SETUP:
                    hint = "Failure during test suite setup";
                    break;
                case CLASS_TEAR_DOWN:
                    hint = "Failure during test suite tear down";
                    break;
                case TEST_CASE:
                    // This is the typical case, so don't provide a
                    // mandatory hint
                    break;
            }

            describeOptions = testOptionsFor(result.test);
            result.priority = describeOptions.hintPriority();

            // Look for mandatory JUnit errors
            //     Method "fName" not found
            //     Method "fName" should be public
            if (result.code == 13 && result.message != null)
            {
                if (result.message.matches(
                    "Method .* (not found|should be public)"))
                {
                    mandatory = 2;
                    hint = result.message;
                }
            }
            else if (result.code == 10)
            {
                hint = result.message;
                mandatory = 2; // Force instructor to see it!
            }


            // Look for explicit hint first
            if (hint == null
                && result.message != null
                && !Pattern.compile("In file .*( which reads|on this line):")
                    .matcher(result.message).find()
                /* && result.message.matches(HINT_MARKER_PLUS_ALL_RE) */)
            {
                hint = result.message.replaceFirst(HINT_MARKER_RE, "");
                hint = result.message.replaceFirst(
                    "^java.lang.AssertionError:", "assertion failed:");

                // remove trailing "expected" fragments
//                output.write(
//                     "explicit hint, before trimming: " + hint + "\n");
//                output.write(
//                    "    hint level: " + result.level + "\n");
//                output.write(
//                    "    hint code: " + result.code + "\n");
                if (result.level == 2)
                {
                    Pattern regex = expectedOutputRegExps[result.code];
                    if (regex != null)
                    {
                        hint = regex.matcher(hint).replaceFirst("");
                    }
                }

                if ("null".equals(hint) || "".equals(hint))
                {
                    hint = null;
                }
                else
                {
                    // Add the required prefix, if any, by pushing the message
                    // back through the options object
                    describeOptions.setHint(hint);
                    hint = describeOptions.fullHintText();
                }
//                output.write(
//                     "explicit hint, after trimming: " + hint + "\n");
            }

            // if none, generate default hint
            if (hint == null && !describeOptions.onlyExplicitHints())
            {
//                 output.write(
//                     "no explicit hint, looking for default ...\n");
                hint = describeOptions.fullHintText();
//                 output.write(
//                     "default hint: '" + hint + "'\n");
            }

            // Determine stack trace, if any
            String traceMsg = null;

            if ((mandatory == 1 && result.level != 4)
                || (result.level > 2
                    // Not a reflection failure
                    && (result.level != 4 || result.code != 29)
                    // Then it was an unexpected exception thrown by the
                    // code under test, or something called by the code
                    // under test
                    && ((result.level == 4 // assertion or other error
                         && !describeOptions.noStackTracesForAsserts())
                        || !describeOptions.noStackTraces())))
            {
                traceMsg = stackTraceMessage(result.error, true);
                if (traceMsg != null)
                {
                    traceMsg = "symptom: " + traceMsg;
                }
            }

            // Replace message content
            if (hint != null)
            {
                result.message = hint;
            }

            // Generate output for hint feedback
            System.out.println(currentSuite + ", hint = " + hint);
            if (output != null && hint != null)
            {
                synchronized (output)
                {
                    outBuffer.append("$results->addHint(");
                    outBuffer.append(mandatory);
                    outBuffer.append(", ");
                    outBuffer.append(result.priority);
                    outBuffer.append(", ");
                    outBuffer.append(perlStringLiteral(hint));
                    if ( traceMsg == null )
                    {
                        outBuffer.append( ", undef);" );
                    }
                    else
                    {
                        outBuffer.append(", <<TRACE);");
                        outBuffer.append(StringUtils.LINE_SEP);
                        outBuffer.append(perlEscape(traceMsg));
                        outBuffer.append("TRACE");
                    }
                    outBuffer.append(StringUtils.LINE_SEP);
                    output.write(outBuffer.toString());
                    outBuffer.setLength(0);
                }
            }
        }
        describeOptions = null;
        return result;
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
        boolean result = super.filterOut(frame);
        if (!result && describeOptions != null)
        {
            result = matches(frame, describeOptions.filterFromStackTraces());
        }
        return result;
    }


    // ----------------------------------------------------------
    private TestPhase stoppedInPhase(Throwable error)
    {
        TestPhase result = TestPhase.TEST_CASE;
        Class<?> suiteClass = suiteOptions().suiteClass();
        String suiteName = suiteOptions().suite().getName();
        if (error != null  &&  suiteClass != null  &&  suiteName != null)
        {
            boolean isJUnit3 = junit.framework.TestCase.class
                .isAssignableFrom(suiteClass);
            while (error.getCause() != null)
            {
                error = error.getCause();
            }
            for (StackTraceElement frame : error.getStackTrace())
            {
                if (suiteName.equals( frame.getClassName()))
                {
                    String methodName = frame.getMethodName();
                    java.lang.reflect.Method method = null;
                    try
                    {
                        method =
                            suiteClass.getMethod(methodName, (Class[])null);
                    }
                    catch (NoSuchMethodException e)
                    {
                        // Leave method == null
                    }

                    // Check for special methods
                    if (method != null)
                    {
                        if (isJUnit3)
                        {
                            if (methodName.equals("setUp"))
                            {
                                result = TestPhase.SETUP;
                                break;
                            }
                            else if (methodName.equals("tearDown"))
                            {
                                result = TestPhase.TEAR_DOWN;
                                break;
                            }
                            else if (methodName.startsWith("test"))
                            {
                                break;
                            }
                        }
                        else // JUnit4
                        {
                            if (method.isAnnotationPresent(
                                org.junit.Before.class))
                            {
                                result = TestPhase.SETUP;
                                break;
                            }
                            else if (method.isAnnotationPresent(
                                org.junit.BeforeClass.class))
                            {
                                result = TestPhase.CLASS_SETUP;
                                break;
                            }
                            else if (method.isAnnotationPresent(
                                org.junit.After.class))
                            {
                                result = TestPhase.TEAR_DOWN;
                                break;
                            }
                            else if (method.isAnnotationPresent(
                                org.junit.AfterClass.class))
                            {
                                result = TestPhase.CLASS_TEAR_DOWN;
                                break;
                            }
                            else if (method.isAnnotationPresent(
                                org.junit.Test.class))
                            {
                                break;
                            }
                        }
                    }
                }
            }
        }
        return result;
    }


    // ----------------------------------------------------------
    private static enum TestPhase {
        /** Marked with Before annotation, or JUnit 3.x named setUp(). */
        SETUP,
        /** Marked with BeforeClass annotation. */
        CLASS_SETUP,
        /** Marked with After annotation, or JUnit 3.x named tearDown(). */
        TEAR_DOWN,
        /** Marked with AfterClass annotation. */
        CLASS_TEAR_DOWN,
        /** Marked with Test annotation, or JUnit 3.x named test...(). */
        TEST_CASE
    }


    //~ Instance/static variables .............................................

    private static final String HINT_MARKER_RE = "^(?i)hint:\\s*";

    private static final Pattern[] expectedOutputRegExps = {
        null,                               // 0: not used
        null,                               // 1: not used
        Pattern.compile("(?is)\\s*"
            + "(\\(after normalizing strings\\)\\s*)?"
            + "expected:.*but was:.*$", Pattern.DOTALL),// 2: CompFailure
        Pattern.compile("(?is)(((\\s*expected:.*but was:.*)"
            + "|(<.*> was the same as:\\s*<.*>)"
            + "|(<.*> matches regex:\\s*<.*>)"
            + "|(<.*> does not match regex:\\s*<.*>)"
            + "|(<.*> contains:)"
            + "|(<.*> does not contain:\\s*<.*>)"
            + "|(<.*> contains regex:\\s*<.*>)"
            + "|(<.*> contains regexes:)"
            + "|(<.*> does not contain regex:\\s*<.*>)"
            + "|(: (expected|actual) array was null)"
            +"|(array lengths differed)"
            +"|(arrays firsts differed)).*)$",
            Pattern.DOTALL
            ),// 3: assertEquals (including JUnit 4.x array version
        Pattern.compile("(?is)(((\\s*expected:.*but was:.*)"
            + "|(<.*> was the same as:\\s*<.*>)"
            + "|(<.*> matches regex:\\s*<.*>)"
            + "|(<.*> does not match regex:\\s*<.*>)"
            + "|(<.*> contains:)"
            + "|(<.*> does not contain:\\s*<.*>)"
            + "|(<.*> contains regex:\\s*<.*>)"
            + "|(<.*> contains regexes:)"
            + "|(<.*> does not contain regex:\\s*<.*>)"
            + "|(: (expected|actual) array was null)"
            +"|(array lengths differed)"
            +"|(arrays firsts differed)).*)$",
            Pattern.DOTALL
            ),// 4: assertFalse
        null,                               // 5: assertNotNull
        Pattern.compile("(?i)\\s*expected not same$",
            Pattern.DOTALL),                // 6: assertNotSame
        null,                               // 7: assertNull
        Pattern.compile("(?is)\\s*expected same:.*was not:.*$",
            Pattern.DOTALL),                //8:assertSame
        Pattern.compile("(?is)(((\\s*expected:.*but was:.*)"
            + "|(<.*> was the same as:\\s*<.*>)"
            + "|(<.*> matches regex:\\s*<.*>)"
            + "|(<.*> does not match regex:\\s*<.*>)"
            + "|(<.*> contains:)"
            + "|(<.*> does not contain:\\s*<.*>)"
            + "|(<.*> contains regex:\\s*<.*>)"
            + "|(<.*> contains regexes:)"
            + "|(<.*> does not contain regex:\\s*<.*>)"
            + "|(: (expected|actual) array was null)"
            +"|(array lengths differed)"
            +"|(arrays firsts differed)).*)$",
            Pattern.DOTALL
            ),// 9: assertTrue
        null,                               // 10: not used
        Pattern.compile("(?is)(((\\s*expected:.*but was:.*)"
            + "|(<.*> was the same as:\\s*<.*>)"
            + "|(<.*> matches regex:\\s*<.*>)"
            + "|(<.*> does not match regex:\\s*<.*>)"
            + "|(<.*> contains:)"
            + "|(<.*> does not contain:\\s*<.*>)"
            + "|(<.*> contains regex:\\s*<.*>)"
            + "|(<.*> contains regexes:)"
            + "|(<.*> does not contain regex:\\s*<.*>)"
            + "|(: (expected|actual) array was null)"
            +"|(array lengths differed)"
            +"|(arrays firsts differed)).*)$",
            Pattern.DOTALL
            ),// 11: custom assert helper method in test case, so still apply these
        null,                               // 12: not used
        null                                // 13: not used
    };

    private TestSuiteOptions suiteOptions;
    private TestOptions testOptions;
    private TestOptions describeOptions;
}
