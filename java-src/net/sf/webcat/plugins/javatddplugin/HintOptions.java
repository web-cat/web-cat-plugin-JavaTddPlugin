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

import net.sf.webcat.annotations.*;

//-------------------------------------------------------------------------
/**
 *  A base class for managing the various options controlling default
 *  hint generation.
 *
 *  @author Stephen Edwards
 *  @version $Id$
 */
public class HintOptions
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Default constructor.
     */
    public HintOptions()
    {
        // Nothing to construct
    }


    // ----------------------------------------------------------
    /**
     * Create a HintOptions object that inherits defaults from another
     * HintOptions object.
     * @param inheritFrom the parent object to inherit defaults from
     */
    public HintOptions( HintOptions inheritFrom )
    {
        parent = inheritFrom;
    }


    //~ Public Methods ........................................................

    // ----------------------------------------------------------
    /**
     * Determine if this options object has its own hint text stored.
     * @return the hint text
     */
    public boolean hasNoLocalHint()
    {
        return hint == null;
    }


    // ----------------------------------------------------------
    /**
     * Get the hint text (including any prefix).
     * @return the hint text
     */
    public String fullHintText()
    {
        String result = hint();
        if ( result != null )
        {
            String prefix = hintPrefix();
            if ( prefix != null && prefix.length() > 0 )
            {
                if (  result.length() > 0
                      && !Character.isWhitespace(
                         prefix.charAt( prefix.length() - 1 ) )
                     && !Character.isWhitespace( result.charAt( 0 ) ) )
                {
                    prefix += " ";
                }
                result = prefix + result;
            }
        }
        return result;
    }


    // ----------------------------------------------------------
    /**
     * Read annotations from a given annotated element, and use the
     * results to populate this object's fields.
     * @param element the element to read annotations from
     */
    public void loadFromAnnotations(
        java.lang.reflect.AnnotatedElement element )
    {
        // filterFromStackTraces
        if ( element.isAnnotationPresent( FilterFromStackTraces.class ) )
        {
            FilterFromStackTraces annotation =
                element.getAnnotation( FilterFromStackTraces.class );
            setFilterFromStackTraces( annotation.value() );
        }

        // hint
        if ( element.isAnnotationPresent( Hint.class ) )
        {
            Hint annotation = element.getAnnotation( Hint.class );
            setHint( annotation.value() );
        }

        // hintPrefix
        if ( element.isAnnotationPresent( HintPrefix.class ) )
        {
            HintPrefix annotation =
                element.getAnnotation( HintPrefix.class );
            setHintPrefix( annotation.value() );
        }

        // noStackTraces and noStackTracesForAsserts
        if ( element.isAnnotationPresent( NoStackTraces.class ) )
        {
            NoStackTraces annotation =
                element.getAnnotation( NoStackTraces.class );
            setNoStackTraces( true );
            setNoStackTracesForAsserts( !annotation.value() );
        }

        // onlyExplicitHints
        if ( element.isAnnotationPresent( OnlyExplicitHints.class ) )
        {
            setOnlyExplicitHints( true );
        }

        // scoringWeight
        if ( element.isAnnotationPresent( ScoringWeight.class ) )
        {
            ScoringWeight annotation =
                element.getAnnotation( ScoringWeight.class );
            setScoringWeight( annotation.value() );
        }
    }


    //~ Public Accessor Methods ...............................................

    // ----------------------------------------------------------
    /**
     * Get the list of classes to filter from stack traces, if any.
     * @return an array of class/package name prefixes to filter, or null
     * if there are none.
     */
    public String[] filterFromStackTraces()
    {
        if ( filterFromStackTraces == null  &&  parent != null )
        {
            return parent.filterFromStackTraces();
        }
        return filterFromStackTraces;
    }


    // ----------------------------------------------------------
    /**
     * Set the list of classes to filter from stack traces.
     * @param value the list of class/package name prefixes
     * to filter, or null
     */
    public void setFilterFromStackTraces( String[] value )
    {
        filterFromStackTraces = value;
    }


    // ----------------------------------------------------------
    /**
     * Get the raw hint text, if any.
     * @return the hint text (no prefix prepended)
     */
    public String hint()
    {
        if ( hint == null  &&  parent != null  &&  !onlyExplicitHints() )
        {
            return parent.hint();
        }
        return hint;
    }


    // ----------------------------------------------------------
    /**
     * Set the raw hint text.
     * @param value the hint text
     */
    public void setHint( String value )
    {
        hint = value;
    }


    // ----------------------------------------------------------
    /**
     * Get the hint text prefix, if any.
     * @return the hint text prefix
     */
    public String hintPrefix()
    {
        if ( hintPrefix == null  &&  parent != null )
        {
            return parent.hintPrefix();
        }
        return hintPrefix;
    }


    // ----------------------------------------------------------
    /**
     * Set the raw hint text prefix.
     * @param value the hint text prefix
     */
    public void setHintPrefix( String value )
    {
        hintPrefix = value;
    }


    // ----------------------------------------------------------
    /**
     * Find out if stack traces should be generated for unexpected
     * exceptions (other than internal assert failures).
     * @return true if stack traces should be omitted
     */
    public boolean noStackTraces()
    {
        if ( noStackTraces == null )
        {
            return ( parent == null )
                ? false
                : parent.noStackTraces();
        }
        return noStackTraces.booleanValue();
    }


    // ----------------------------------------------------------
    /**
     * Set whether to generate stack traces for unexpected exceptions
     * (other than internal assert failures).
     * @param value true if stack traces should be omitted
     */
    public void setNoStackTraces( boolean value )
    {
        noStackTraces = Boolean.valueOf( value );
    }


    // ----------------------------------------------------------
    /**
     * Find out if stack traces should be generated for internal assert
     * failures in the component under test.
     * @return true if stack traces should be omitted
     */
    public boolean noStackTracesForAsserts()
    {
        if ( noStackTracesForAsserts == null )
        {
            return ( parent == null )
                ? false
                : parent.noStackTracesForAsserts();
        }
        return noStackTracesForAsserts.booleanValue();
    }


    // ----------------------------------------------------------
    /**
     * Set whether to generate stack traces for internal assert failures
     * in the component under test.
     * @param value true if stack traces should be omitted
     */
    public void setNoStackTracesForAsserts( boolean value )
    {
        noStackTracesForAsserts = Boolean.valueOf( value );
    }


    // ----------------------------------------------------------
    /**
     * Find out if only explicit hints should be used.
     * @return true if only explicit hints should be used
     */
    public boolean onlyExplicitHints()
    {
        if ( onlyExplicitHints == null )
        {
            return ( parent == null )
                ? false
                : parent.onlyExplicitHints();
        }
        return onlyExplicitHints.booleanValue();
    }


    // ----------------------------------------------------------
    /**
     * Set whether only explicit hints should be used.
     * @param value true if only explicit hints should be used
     */
    public void setOnlyExplicitHints( boolean value )
    {
        onlyExplicitHints = Boolean.valueOf( value );
    }


    // ----------------------------------------------------------
    /**
     * Find out if only explicit hints should be used.
     * @return true if only explicit hints should be used
     */
    public double scoringWeight()
    {
        if ( scoringWeight == null )
        {
            return ( parent == null )
                ? 1.0
                : parent.scoringWeight();
        }
        return scoringWeight.doubleValue();
    }


    // ----------------------------------------------------------
    /**
     * Set whether only explicit hints should be used.
     * @param value true if only explicit hints should be used
     */
    public void setScoringWeight( double value )
    {
        scoringWeight = Double.valueOf( value );
    }


    // ----------------------------------------------------------
    /**
     * Get the list of classes (or class prefixes) to interpret as the
     * topmost level for generated hint stack traces.
     * @return an array of class/package name prefixes, or null
     * if there are none.
     */
    public String[] stackTraceStopFilters()
    {
        if ( stackTraceStopFilters == null  &&  parent != null )
        {
            return parent.stackTraceStopFilters();
        }
        return stackTraceStopFilters;
    }


    // ----------------------------------------------------------
    /**
     * Set the list of classes (or class prefixes) to interpret as the
     * topmost level for generated hint stack traces.
     * @param value the list of class/package name prefixes, or null
     */
    public void setStackTraceStopFilters( String[] value )
    {
        stackTraceStopFilters = value;
    }


    // ----------------------------------------------------------
    /**
     * Get this object's parent, from whom it will inherit default values.
     * @return the parent object, or null
     */
    public HintOptions parent()
    {
        return parent;
    }


    // ----------------------------------------------------------
    /**
     * Set this object's parent, from whom it will inherit default values.
     * @param value the new parent, or null
     */
    public void setParent( HintOptions value )
    {
        parent = value;
    }


    //~ Instance/static variables .............................................

    private String[] filterFromStackTraces;
    private String   hint;
    private String   hintPrefix;
    private Boolean  noStackTraces;
    private Boolean  noStackTracesForAsserts;
    private Boolean  onlyExplicitHints;
    private Double   scoringWeight;
    private String[] stackTraceStopFilters;

    private HintOptions parent;
}
