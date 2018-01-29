/*==========================================================================*\
 |  $Id: HintOptions.java,v 1.5 2013/08/26 04:11:00 stedwar2 Exp $
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

//-------------------------------------------------------------------------
/**
 *  A base class for managing the various options controlling default
 *  hint generation.
 *
 *  @author Stephen Edwards
 *  @version $Id: HintOptions.java,v 1.5 2013/08/26 04:11:00 stedwar2 Exp $
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
    @SuppressWarnings("deprecation")
    public void loadFromAnnotations(
        java.lang.reflect.AnnotatedElement element)
    {
        // filterFromStackTraces
        if (element.isAnnotationPresent(
            student.testingsupport.annotations.FilterFromStackTraces.class))
        {
            student.testingsupport.annotations.FilterFromStackTraces annotation
                = element.getAnnotation(student.testingsupport.annotations
                    .FilterFromStackTraces.class);
            setFilterFromStackTraces(annotation.value());
        }
        if (element.isAnnotationPresent(
            net.sf.webcat.annotations.FilterFromStackTraces.class))
        {
            net.sf.webcat.annotations.FilterFromStackTraces annotation =
                element.getAnnotation(
                    net.sf.webcat.annotations.FilterFromStackTraces.class);
            setFilterFromStackTraces(annotation.value());
        }

        // hint
        if (element.isAnnotationPresent(
            student.testingsupport.annotations.Hint.class))
        {
            student.testingsupport.annotations.Hint annotation = element
                .getAnnotation(student.testingsupport.annotations.Hint.class);
            setHint(annotation.value());
        }
        if (element.isAnnotationPresent(net.sf.webcat.annotations.Hint.class))
        {
            net.sf.webcat.annotations.Hint annotation =
                element.getAnnotation(net.sf.webcat.annotations.Hint.class);
            setHint(annotation.value());
        }

        // hintPrefix
        if (element.isAnnotationPresent(
            student.testingsupport.annotations.HintPrefix.class))
        {
            student.testingsupport.annotations.HintPrefix annotation =
                element.getAnnotation(
                    student.testingsupport.annotations.HintPrefix.class);
            setHintPrefix(annotation.value());
        }
        if (element.isAnnotationPresent(
            net.sf.webcat.annotations.HintPrefix.class))
        {
            net.sf.webcat.annotations.HintPrefix annotation = element
                .getAnnotation(net.sf.webcat.annotations.HintPrefix.class);
            setHintPrefix(annotation.value());
        }

        // noStackTraces and noStackTracesForAsserts
        if (element.isAnnotationPresent(
            student.testingsupport.annotations.NoStackTraces.class))
        {
            student.testingsupport.annotations.NoStackTraces annotation =
                element.getAnnotation(
                    student.testingsupport.annotations.NoStackTraces.class);
            setNoStackTraces(true);
            setNoStackTracesForAsserts(!annotation.value());
        }
        if (element.isAnnotationPresent(
            net.sf.webcat.annotations.NoStackTraces.class))
        {
            net.sf.webcat.annotations.NoStackTraces annotation = element
                .getAnnotation(net.sf.webcat.annotations.NoStackTraces.class);
            setNoStackTraces(true);
            setNoStackTracesForAsserts(!annotation.value());
        }

        // onlyExplicitHints
        if (element.isAnnotationPresent(
            student.testingsupport.annotations.OnlyExplicitHints.class)
            || element.isAnnotationPresent(
                net.sf.webcat.annotations.OnlyExplicitHints.class))
        {
            setOnlyExplicitHints(true);
        }

        // scoringWeight
        if (element.isAnnotationPresent(
            student.testingsupport.annotations.ScoringWeight.class))
        {
            student.testingsupport.annotations.ScoringWeight annotation =
                element.getAnnotation(
                    student.testingsupport.annotations.ScoringWeight.class);
            setScoringWeight(annotation.value());
            setDefaultMethodScoringWeight(annotation.defaultMethodWeight());
        }
        if (element.isAnnotationPresent(
            net.sf.webcat.annotations.ScoringWeight.class))
        {
            net.sf.webcat.annotations.ScoringWeight annotation = element
                .getAnnotation(net.sf.webcat.annotations.ScoringWeight.class);
            setScoringWeight(annotation.value());
            setDefaultMethodScoringWeight(annotation.defaultMethodWeight());
        }

        // hintPriority
        if (element.isAnnotationPresent(
            student.testingsupport.annotations.HintPriority.class))
        {
            student.testingsupport.annotations.HintPriority annotation =
                element.getAnnotation(
                    student.testingsupport.annotations.HintPriority.class);
            setHintPriority(annotation.value());
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
     * Determine if a non-default scoring weight has been set.
     * @return True if a non-default scoring weight has been set.
     */
    public boolean hasScoringWeight()
    {
        return scoringWeight != null;
    }


    // ----------------------------------------------------------
    /**
     * Get the hint priority for this object.
     * @return The hint priority.
     */
    public int hintPriority()
    {
        if ( hintPriority == null )
        {
            return ( parent == null )
                ? Integer.MAX_VALUE
                : parent.hintPriority();
        }
        return hintPriority;
    }


    // ----------------------------------------------------------
    /**
     * Set the hint priority for this object.
     * @param value The hint priority to use.
     */
    public void setHintPriority(int value)
    {
        hintPriority = value;
    }


    // ----------------------------------------------------------
    /**
     * Get the scoring weight for this object.
     * @return The scoring weight.
     */
    public double scoringWeight()
    {
        if ( scoringWeight == null )
        {
            return defaultMethodScoringWeight();
        }
        return scoringWeight.doubleValue();
    }


    // ----------------------------------------------------------
    /**
     * Set the scoring weight for this object.
     * @param value The scoring weight to use.
     */
    public void setScoringWeight( double value )
    {
        scoringWeight = Double.valueOf( value );
    }


    // ----------------------------------------------------------
    /**
     * Get the default scoring weight for methods in this object.
     * @return The default scoring weight for methods.
     */
    public double defaultMethodScoringWeight()
    {
        if ( defaultMethodScoringWeight == null )
        {
            return ( parent == null )
                ? 1.0
                : parent.defaultMethodScoringWeight();
        }
        return defaultMethodScoringWeight.doubleValue();
    }


    // ----------------------------------------------------------
    /**
     * Set the default scoring weight for methods in this object.
     * @param value The default scoring weight to use.
     */
    public void setDefaultMethodScoringWeight( double value )
    {
        defaultMethodScoringWeight = Double.valueOf( value );
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
    private Double   defaultMethodScoringWeight;
    private Integer  hintPriority;
    private String[] stackTraceStopFilters;

    private HintOptions parent;
}
