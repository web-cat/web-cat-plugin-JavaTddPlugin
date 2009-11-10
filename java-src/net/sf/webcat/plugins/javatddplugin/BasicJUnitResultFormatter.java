/*==========================================================================*\
 |  $Id$
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006 Virginia Tech
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
 |
 |  Adapted from org.apache.tools.ant.taskdefs.optional.junit.PlainJUnitResultFormatter,
\*==========================================================================*/

package net.sf.webcat.plugins.javatddplugin;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import junit.framework.Test;
import net.sf.webcat.PrintStreamWithHistory;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest;
import org.apache.tools.ant.taskdefs.optional.junit.PlainJUnitResultFormatter;

//-------------------------------------------------------------------------
/**
 *  A custom replacement formatter for the ANT junit task.  It is based
 *  on the "plain" formatter provided by ANT, but offers slightly different
 *  output formatting and omits all stdout/stderr from test cases.
 *
 *  @author Stephen Edwards
 *  @author Last changed by $Author$
 *  @version $Revision$, $Date$
 */
public class BasicJUnitResultFormatter
    extends PlainJUnitResultFormatter
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Default constructor.
     */
    public BasicJUnitResultFormatter()
    {
        super();
    }


    //~ Public Methods ........................................................

    // ----------------------------------------------------------
    /**
     * Set the stdout content for the current test suite.
     * This implementation does nothing, so that it hides the content
     * from the parent class.
     *
     * @param out the stdout contents
     */
    public void setSystemOutput( String out )
    {
        // Do nothing, so parent class omits this content.
    }


    // ----------------------------------------------------------
    /**
     * Set the stderr content for the current test suite.
     * This implementation does nothing, so that it hides the content
     * from the parent class.
     *
     * @param err the stderr contents
     */
    public void setSystemError( String err )
    {
        // Do nothing, so parent class omits this content.
    }


    // ----------------------------------------------------------
    public void setOutput(OutputStream out)
    {
        this.out = out;
        super.setOutput(out);
    }


    // ----------------------------------------------------------
    public void startTestSuite(JUnitTest suite)
        throws BuildException
    {
        StringBuffer buf = new StringBuffer(100);
        messages.clear();

        if (needsSeparator)
        {
            buf.append("\n");
        }
        buf.append("====================\n");
        buf.append("Testsuite: ");
        buf.append(suite.getName());
        buf.append("\n");
        buf.append("--------------------\n");
        try
        {
            out.write(buf.toString().getBytes());
            out.flush();
        } catch (IOException e)
        {
            throw new BuildException("Unable to write output", e);
        }
    }


    // ----------------------------------------------------------
    public void addFailure(Test test, Throwable t)
    {
        messages.add(t.getMessage());
        super.addFailure(test, t);
    }


    // ----------------------------------------------------------
    public void addError(Test test, Throwable t)
    {
        messages.add(t.getMessage());
        super.addError(test, t);
    }


    // ----------------------------------------------------------
    public void endTestSuite(JUnitTest suite)
        throws BuildException
    {
        synchronized (capture)
        {
            capture.clearHistory();
            super.setOutput(capture);
            super.endTestSuite(suite);
            super.setOutput(out);

            String result = capture.getHistory();
            String resultLine = null;
            Matcher m = RESULT_LINE.matcher(result);
            if (m.find())
            {
                resultLine = m.group(0).trim();
                if (m.end() >= result.length())
                {
                    result = "";
                }
                else
                {
                    result = result.substring(m.end());
                }
            }
            for (String message : messages)
            {
                result = result.replaceFirst(
                    "(Testcase:.*(?:\r\n|\n|\r)"
                    + "\t(?:FAILED|Caused an ERROR)(?:\r\n|\n|\r))\\Q"
                    + message
                    + "\\E(?:\r\n|\n|\r)", "$1");
            }
            messages.clear();
            result += "--------------------\n"
                + resultLine
                + "\n====================\n";
            try
            {
                out.write(result.getBytes());
                out.flush();
            } catch (IOException e)
            {
                throw new BuildException("Unable to write output", e);
            }
        }
    }


    //~ Instance/static variables .............................................

    private OutputStream out;
    private boolean needsSeparator = false;
    private PrintStreamWithHistory capture =
        new PrintStreamWithHistory(new OutputStream() {
            public void write(int b)
                throws IOException
            {
                // Ignore all output
            }
        });
    private Pattern RESULT_LINE = Pattern.compile("^Tests run.*[\n\r]+");
    private List<String> messages = new ArrayList<String>();
}
