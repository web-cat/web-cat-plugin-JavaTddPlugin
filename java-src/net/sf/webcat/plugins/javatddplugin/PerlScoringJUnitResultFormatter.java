/*==========================================================================*\
 |  $Id$
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

import java.io.OutputStream;
import java.io.PrintWriter;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitResultFormatter;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.StringUtils;

//-------------------------------------------------------------------------
/**
 *  A custom formatter for the ANT junit task that just captures
 *  test case execute/fail results in a form that can be easily
 *  interpreted by Perl.
 *
 *  @author Stephen Edwards
 *  @author Last changed by $Author$
 *  @version $Revision$, $Date$
 */
public class PerlScoringJUnitResultFormatter
    implements JUnitResultFormatter
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Default constructor.
     */
    public PerlScoringJUnitResultFormatter()
    {
        // Nothing to construct
    }


    //~ Public Methods ........................................................

    // ----------------------------------------------------------
    /**
     * @see JUnitResultFormatter#setOutput(OutputStream)
     */
    /** {@inheritDoc}. */
    public void setOutput( OutputStream out )
    {
        this.out = out;
        output = new PrintWriter( out );
    }


    // ----------------------------------------------------------
    /**
     * @see JUnitResultFormatter#setSystemOutput(String)
     */
    /** {@inheritDoc}. */
    public void setSystemOutput( String out )
    {
        // Do nothing
    }


    // ----------------------------------------------------------
    /**
     * @see JUnitResultFormatter#setSystemError(String)
     */
    /** {@inheritDoc}. */
    public void setSystemError( String err )
    {
        // Do nothing
    }


    // ----------------------------------------------------------
    /**
     * @see JUnitResultFormatter#startTestSuite(JUnitTest)
     */
    /** {@inheritDoc}. */
    public void startTestSuite( JUnitTest suite )
    {
        numExecuted = 0.0;
        numFailed   = 0.0;
    }


    // ----------------------------------------------------------
    /**
     * @see JUnitResultFormatter#endTestSuite(JUnitTest)
     */
    /** {@inheritDoc}. */
    public void endTestSuite( JUnitTest suite )
    {
        if ( output == null ) return;
        outputForSuite( outBuffer, suite );
        try
        {
            output.write( outBuffer.toString() );
            output.flush();
        }
        finally
        {
            outBuffer.setLength( 0 );
            if ( out != System.out  &&  out != System.err )
            {
                FileUtils.close( out );
            }
        }
    }


    // ----------------------------------------------------------
    /**
     * @see TestListener#startTest(Test)
     */
    /** {@inheritDoc}. */
    public void startTest( Test test )
    {
        numExecuted += scoringWeightOf( test );
    }


    // ----------------------------------------------------------
    /**
     * @see TestListener#endTest(Test)
     */
    /** {@inheritDoc}. */
    public void endTest( Test test )
    {
        // Nothing to do
    }


    // ----------------------------------------------------------
    /**
     * @see TestListener#addFailure(Test,AssertionFailedError)
     */
    /** {@inheritDoc}. */
    public void addFailure( Test test, AssertionFailedError t )
    {
        numFailed += scoringWeightOf( test );
    }


    // ----------------------------------------------------------
    /**
     * @see TestListener#addError(Test,Throwable)
     */
    /** {@inheritDoc}. */
    public void addError( Test test, Throwable error )
    {
        numFailed += scoringWeightOf( test );
    }


    //~ Protected Methods .....................................................

    // ----------------------------------------------------------
    /**
     * Get the scoring weight associated with this test case.
     * @param test the test case
     * @return the scoring weight
     */
    protected double scoringWeightOf(Test test)
    {
        return 1.0;
    }


    // ----------------------------------------------------------
    /**
     * Format and print out results for this test suite.  This is an
     * extension point for subclasses so that additional output can
     * be added.  Subclasses should always call super.outputForSuite()
     * when overriding this method.
     * @param buffer the string buffer where output should be placed
     * @param suite  the test suite that is ending
     */
    protected void outputForSuite(StringBuffer buffer, JUnitTest suite)
    {
        buffer.append( "$results->addTestsExecuted(" );
        buffer.append( numExecuted );
        buffer.append( ");" );
        buffer.append( StringUtils.LINE_SEP );
        buffer.append( "$results->addTestsFailed(" );
        buffer.append( numFailed );
        buffer.append( ");");
        buffer.append( StringUtils.LINE_SEP );
    }


    //~ Instance/static variables .............................................

    /** Where to write the log to. */
    protected OutputStream out;

    /** Used for writing the results. */
    protected PrintWriter output;

    /** A scratch string buffer for formatting purposes. */
    protected StringBuffer outBuffer = new StringBuffer();

    protected double numExecuted = 0.0;
    protected double numFailed   = 0.0;
}
