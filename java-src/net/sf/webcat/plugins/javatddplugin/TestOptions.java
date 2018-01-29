/*==========================================================================*\
 |  $Id: TestOptions.java,v 1.3 2010/02/23 17:19:18 stedwar2 Exp $
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

import junit.framework.Test;

//-------------------------------------------------------------------------
/**
 *  Manages the various options controlling default hint generation for
 *  a specific method.
 *
 *  @author Stephen Edwards
 *  @author Last changed by $Author: stedwar2 $
 *  @version $Revision: 1.3 $, $Date: 2010/02/23 17:19:18 $
 */
public class TestOptions
    extends HintOptions
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Create a HintOptions object that inherits defaults from another
     * HintOptions object.
     * @param suite the object representing options for the enclosing class
     * @param test the test that this object contains options for
     */
    public TestOptions( TestSuiteOptions suite, Test test )
    {
        super( suite );
        this.test = test;
        loadFromAnnotations();
    }


    //~ Public Methods ........................................................

    // ----------------------------------------------------------
    /**
     * Get the test case.
     * @return the case
     */
    public Test test()
    {
        return test;
    }


    // ----------------------------------------------------------
    /**
     * Get the method name associated with this test.
     * @return the method name
     */
    public String methodName()
    {
        if ( methodName == null && test != null )
        {
            methodName = test.toString();
            if ( methodName != null )
            {
                // Strip the class name from the end of the test case name
                int pos = methodName.indexOf( '(' );
                if ( pos > 0 )
                {
                    methodName = methodName.substring( 0, pos );
                }
            }
        }
        return methodName;
    }


    // ----------------------------------------------------------
    public String hint()
    {
        if ( hasNoLocalHint() && !onlyExplicitHints() )
        {
            setHint( hintFromMethodName( methodName() ) );
        }
        return super.hint();
    }


    //~ Private Methods .......................................................

    // ----------------------------------------------------------
    private void loadFromAnnotations()
    {
        Class<?> suiteClass = ( parent() == null )
            ? null
            : ((TestSuiteOptions)parent()).suiteClass();
        if ( suiteClass == null )
        {
            return;
        }

        String name = methodName();
        if ( name == null )
        {
            return;
        }

        java.lang.reflect.Method method = null;
        try
        {
            method = suiteClass.getMethod( name, (Class[])null );
        }
        catch ( NoSuchMethodException e )
        {
            // Ignore this one
        }

        if ( method != null )
        {
            loadFromAnnotations( method );
        }
    }


    // ----------------------------------------------------------
    private String hintFromMethodName( String aMethodName )
    {
        String result = aMethodName;
        if ( result != null )
        {
            // Remove "test" from the start
            if ( result.length() > 4
                 && result.startsWith( "test" )
                 && ( !Character.isLetter( result.charAt( 4 ) )
                      || Character.isUpperCase( result.charAt( 4 ) ) ) )
            {
                result = result.substring( 4 );
            }

            // Remove any trailing digits not preceded by an underscore
            result = result.replaceFirst( "(?<!_)[0-9]+$", "" );

            // Now split into phrases using underscores
            StringBuffer sb = new StringBuffer();
            for ( String phrase : result.replace("__", "_._").split( "_" ) )
            {
                if ( phrase != null && !phrase.equals( "" ) )
                {
                    String thisPhrase = translateWordPhrase( phrase );
                    if (".".equals(thisPhrase))
                    {
                        // Don't put spaces around it
                        int lastPos = sb.length() - 1;
                        if ( lastPos >= 0 && sb.charAt( lastPos ) == ' ' )
                        {
                            sb.deleteCharAt( lastPos );
                        }
                        sb.append( translateWordPhrase( phrase ) );
                    }
                    else
                    {
                        sb.append( translateWordPhrase( phrase ) );
                        sb.append( ' ' );
                    }
                }
            }
            if ( sb.length() > 0 )
            {
                sb.deleteCharAt( sb.length() - 1 );
            }
            result = sb.toString();
        }
        return result;
    }


    // ----------------------------------------------------------
    private String translateWordPhrase( String word )
    {
        String result = word;
        // First, look for method
        if ( result.length() > 1
             && result.charAt( 0 ) == 'c'
             && Character.isUpperCase( result.charAt( 1 ) ) )
        {
            result = result.substring( 1 );
        }

        // Second, look for method
        else if ( result.length() > 1
             && result.charAt( 0 ) == 'm'
             && Character.isUpperCase( result.charAt( 1 ) ) )
        {
            result = Character.toLowerCase(  result.charAt( 1 ) )
                + result.substring( 2 ) + "()";
        }

        // Then look for all uppercase
        else if ( isUpperCase( word ) )
        {
            // Do nothing
        }

        // Then split on caps and convert to lower case
        else
        {
            StringBuffer sb = new StringBuffer();
            for ( String wd : word.split( WORD_BOUNDARY_REGEX ) )
            {
                if ( wd != null && !"".equals( wd ) )
                {
                    if ( isUpperCase( wd ) )
                    {
                        sb.append( wd );
                    }
                    else
                    {
                        sb.append( wd.toLowerCase() );
                    }
                    sb.append( ' ' );
                }
            }
            if ( sb.length() > 0 )
            {
                sb.deleteCharAt( sb.length() - 1 );
            }
            result = sb.toString();
        }

        return result;
    }


    // ----------------------------------------------------------
    private boolean isUpperCase( String word )
    {
        int length = word.length();
        for( int i = 0; i < length; i++ )
        {
            char c = word.charAt( i );
            if ( !Character.isUpperCase( c )  &&  !Character.isDigit( c ) )
            {
                return false;
            }
        }
        return true;
    }


    //~ Instance/static variables .............................................

    private Test test;

    private String methodName;

    private static final String WORD_BOUNDARY_REGEX =
        "((?<=[^\\p{javaUpperCase}])(?=\\p{javaUpperCase}))"
        + "|((?<=\\p{javaUpperCase})(?=\\p{javaUpperCase}[^\\p{Upper}]))";
    // Originally, this was:
    //     "((?<=[^A-Z])(?=[A-Z]))|((?<=[A-Z])(?=[A-Z][^A-Z]))"
    // But it was changed so it would work with other languages more
    // effectively.  Unfortunately, I had to use Upper instead of
    // javaUpperCase in the very last character class--otherwise, it
    // triggered a bug in Java's regular expression package when splitting
    // a string containing a sequence of multiple upper case letters at
    // the end.
}
