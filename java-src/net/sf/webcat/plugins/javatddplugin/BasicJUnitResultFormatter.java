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
\*==========================================================================*/

package net.sf.webcat.plugins.javatddplugin;

import org.apache.tools.ant.taskdefs.optional.junit.PlainJUnitResultFormatter;

//-------------------------------------------------------------------------
/**
 *  A custom replacement formatter for the ANT junit task.  It is based
 *  on the "plain" formatter provided by ANT, but offers slightly different
 *  output formatting and omits all stdout/stderr from test cases.
 *
 *  @author Stephen Edwards
 *  @version $Id$
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
}
