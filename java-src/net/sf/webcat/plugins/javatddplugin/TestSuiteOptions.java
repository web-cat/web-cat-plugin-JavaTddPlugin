/*==========================================================================*\
 |  $Id: TestSuiteOptions.java,v 1.2 2010/02/23 17:19:18 stedwar2 Exp $
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

import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest;

//-------------------------------------------------------------------------
/**
 *  Manages the various options controlling default hint generation for
 *  a test class.
 *
 *  @author Stephen Edwards
 *  @author Last changed by $Author: stedwar2 $
 *  @version $Revision: 1.2 $, $Date: 2010/02/23 17:19:18 $
 */
public class TestSuiteOptions
    extends HintOptions
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Create a HintOptions object that inherits defaults from another
     * HintOptions object.
     * @param suite the object representing options for the enclosing class
     * @param methodName the name of the method this object contains options
     * for
     */
    public TestSuiteOptions( JUnitTest suite )
    {
        this.suite = suite;
        loadFromAnnotations();
    }


    //~ Public Methods ........................................................

    // ----------------------------------------------------------
    /**
     * Get the test suite.
     * @return the suite
     */
    public JUnitTest suite()
    {
        return suite;
    }


    // ----------------------------------------------------------
    /**
     * Get the Class object associated with this test suite.
     * @return the suite's class
     */
    public Class<?> suiteClass()
    {
        return suiteClass;
    }


    //~ Public Methods ........................................................

    // ----------------------------------------------------------
    public String hint()
    {
        if ( hasNoLocalHint() && !onlyExplicitHints() )
        {
            setHint( hintFromClassName( suite.getName() ) );
        }
        return super.hint();
    }


    //~ Private Methods .......................................................

    // ----------------------------------------------------------
    private void loadFromAnnotations()
    {
        try
        {
            suiteClass = Class.forName(  suite.getName() );
        }
        catch ( ClassNotFoundException e )
        {
            // Ignore this one
        }

        if ( suiteClass != null )
        {
            loadFromAnnotations( suiteClass );
            setStackTraceStopFilters( new String[] { suite.getName() } );
        }
    }


    // ----------------------------------------------------------
    private String hintFromClassName( String name )
    {
        String result = name;

        // First, strip off package name, if any
        int pos = result.lastIndexOf( '.' );
        if ( pos > 0 )
        {
            result = result.substring( pos + 1 );
        }

        // Now strip off Test/Tests suffix
        for ( String suffix : suffixesToStrip )
        {
            if ( result.endsWith( suffix )
                 && result.length() > suffix.length() )
            {
                result = result.substring(
                    0, result.length() - suffix.length() );
                break;
            }
        }

        // Now strip off anything after the first $
        pos = result.indexOf( '$' );
        if ( pos == 0 )
        {
            result = null;
        }
        else if ( pos > 0 )
        {
            result = result.substring( 0, pos );
        }

        return result;
    }


    //~ Instance/static variables .............................................

    private JUnitTest suite;
    private Class<?>  suiteClass;
    private static String[] suffixesToStrip = { "Test", "Tests" };
}
