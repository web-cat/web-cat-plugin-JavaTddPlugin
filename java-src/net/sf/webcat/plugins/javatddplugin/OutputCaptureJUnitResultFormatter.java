/*==========================================================================*\
 |  $Id: OutputCaptureJUnitResultFormatter.java,v 1.2 2010/02/23 17:19:18 stedwar2 Exp $
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
 *  stdout/stderr contents.
 *
 *  @author Stephen Edwards
 *  @author Last changed by $Author: stedwar2 $
 *  @version $Revision: 1.2 $, $Date: 2010/02/23 17:19:18 $
 */
public class OutputCaptureJUnitResultFormatter
    implements JUnitResultFormatter
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Default constructor.
     */
    public OutputCaptureJUnitResultFormatter()
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
        systemOutput = out;
    }


    // ----------------------------------------------------------
    /**
     * @see JUnitResultFormatter#setSystemError(String)
     */
    /** {@inheritDoc}. */
    public void setSystemError( String err )
    {
        systemError = err;
    }


    // ----------------------------------------------------------
    /**
     * @see JUnitResultFormatter#startTestSuite(JUnitTest)
     */
    /** {@inheritDoc}. */
    public void startTestSuite( JUnitTest suite )
    {
        // Do nothing
    }


    // ----------------------------------------------------------
    /**
     * @see JUnitResultFormatter#endTestSuite(JUnitTest)
     */
    /** {@inheritDoc}. */
    public void endTestSuite( JUnitTest suite )
    {
        StringBuffer sb = new StringBuffer();

        // append the err and output streams to the log
        if ( systemOutput != null && systemOutput.length() > 0 )
        {
            sb.append( "------------- " );
            sb.append( suite.getName() );
            sb.append( ": standard output ---------------" );
            sb.append( StringUtils.LINE_SEP );
            sb.append( systemOutput );
            sb.append( "------------- ---------------- ---------------" );
            sb.append( StringUtils.LINE_SEP );
        }

        if ( systemError != null && systemError.length() > 0 )
        {
            sb.append( "------------- " );
            sb.append( suite.getName() );
            sb.append(": standard error -----------------");
            sb.append( StringUtils.LINE_SEP );
            sb.append( systemError );
            sb.append( "------------- ---------------- ---------------" );
            sb.append( StringUtils.LINE_SEP );
        }

        if ( output != null )
        {
            try
            {
                output.write( sb.toString() );
                output.flush();
            }
            finally
            {
                if ( out != System.out  &&  out != System.err )
                {
                    FileUtils.close( out );
                }
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
        // Do nothing
    }


    // ----------------------------------------------------------
    /**
     * @see TestListener#endTest(Test)
     */
    /** {@inheritDoc}. */
    public void endTest( Test test )
    {
        // Do nothing
    }


    // ----------------------------------------------------------
    /**
     * @see TestListener#addFailure(Test,AssertionFailedError)
     */
    /** {@inheritDoc}. */
    public void addFailure( Test test, AssertionFailedError t )
    {
        // Do nothing
    }


    // ----------------------------------------------------------
    /**
     * @see TestListener#addError(Test,Throwable)
     */
    /** {@inheritDoc}. */
    public void addError( Test test, Throwable error )
    {
        // Do nothing
    }


    //~ Instance/static variables .............................................

    /** Where to write the log to. */
    private OutputStream out;

    /** Used for writing the results. */
    private PrintWriter output;

    /** Output suite has written to System.out. */
    private String systemOutput = null;

    /** Output suite has written to System.err. */
    private String systemError = null;
}
