/*==========================================================================*\
 |  $Id: ProfilingSecurityManager.java,v 1.1 2007/09/15 01:58:39 stedwar2 Exp $
 |*-------------------------------------------------------------------------*|
 | Modified from Mark Petrovic's original:
 |
 | Copyright (c) 2006 Mark Petrovic <mspetrovic@gmail.com>
 |
 | Permission is hereby granted, free of charge, to any person obtaining
 | a copy of this software and associated documentation files (the
 | "Software"), to deal in the Software without restriction, including
 | without limitation the rights to use, copy, modify, merge, publish,
 | distribute, sublicense, and/or sell copies of the Software, and to
 | permit persons to whom the Software is furnished to do so, subject to
 | the following conditions:
 |
 | The above copyright notice and this permission notice shall be
 | included in all copies or substantial portions of the Software.
 |
 | THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 | EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 | MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 | NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 | LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 | OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 | WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 |
 | Original Author: Mark Petrovic <mspetrovic@gmail.com>
\*==========================================================================*/

package net.sf.webcat.plugins.javatddplugin;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.security.AccessController;
import java.security.AccessControlContext;
import java.security.CodeSource;
import java.security.Permission;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Set;


//-------------------------------------------------------------------------
/**
 * <code>ProfilingSecurityManager</code> is a Java security manager that
 * profiles what resources an application accesses, and in what
 * manner--e.g., read, write, etc.  It does not enforce a
 * security policy, but rather produces a starting point for crafting one.
 * <p>
 * It extends {@link java.lang.SecurityManager} and overrides the two forms
 * of the <code>checkPermission()</code> method.  For each call to
 * <code>checkPermission()</code>, <code>ProfilingSecurityManager</code> first
 * guards against the condition that it itself induced the call to
 * <code>checkPermission()</code>, which would result in unterminated
 * recursion.  If a call to <code>checkPermission()</code> resulted from a
 * call outside <code>ProfilingSecurityManager</code>, the current context
 * is examined and each class found therein is profiled as needing access to
 * the {@link java.security.Permission} in question.
 *
 * Profiling is manifested as a writing to <code>System.out</code> a "grant"
 * rule for each <code>java.security.Permission</code> requested
 * on a per <code>CodeBase</code> basis.
 *
 * The implementation here does some very simple rule caching.  If a rule has
 * been seen previously, it is not output to System.out.  The caching cannot
 * prevent a security check, but it can reduce I/O during profiling.
 *
 * @author Mark S. Petrovic, with modifications by Stephen Edwards
 * @version $Id: ProfilingSecurityManager.java,v 1.1 2007/09/15 01:58:39 stedwar2 Exp $
 */
public class ProfilingSecurityManager
    extends SecurityManager
{
    //~ Constructor ...........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new ProfilingSecurityManager object.
     */
    public ProfilingSecurityManager()
    {
        thisClassName=this.getClass().getName();
        CodeSource thisCodeSource =
            this.getClass().getProtectionDomain().getCodeSource();
        thisCodeSourceURLString = thisCodeSource.getLocation().toString();
    }


    //~ Public Methods ........................................................

    // ----------------------------------------------------------
    @Override
    public void checkPermission(final Permission permission)
    {
        try
        {
            super.checkPermission(permission);
        }
        catch (SecurityException e)
        {
            final Throwable t = new Throwable("Profiler stack probe");
            final StackTraceElement[] stack = t.getStackTrace();
            // Avoid recursion owing to actions in this class itself inducing
            // callbacks
            if( !isRecur(stack) )
            {
                buildRules(permission, AccessController.getContext());
            }
        }
    }


    // ----------------------------------------------------------
    @Override
    public void checkPermission(
        final Permission permission, final Object context)
    {
        try
        {
            super.checkPermission(permission, context);
        }
        catch (SecurityException e)
        {
            buildRules(permission, (AccessControlContext)context);
        }
    }


    // ----------------------------------------------------------
    @Override
    public String toString()
    {
        return "SecurityManager:  " + psmMsg;
    }


    //~ Private Methods .......................................................

    // ----------------------------------------------------------
    /**
     *  With a Permission and an AccessControlContext, we can build and print
     *  rules.
     */
    private void buildRules(
        final Permission permission, final AccessControlContext ctx)
    {
        try
        {
           final ProtectionDomain[] protectionDomain =
               getProtectionDomains(ctx);
           if (null != protectionDomain)
           {
              for (int i = 0; i < protectionDomain.length; ++i)
              {
                 final String grant =
                     formatRule(permission, protectionDomain[i]);
                 if (null != grant && !isCached(grant))
                 {
                    println(grant);
                 }
              }
           }
        }
        catch (IllegalStateException e)
        {
           e.printStackTrace();
        }
    }


    // ----------------------------------------------------------
    /**
     * Traverse the stack, returning true if the stack indicates we called
     * ourself.
     */
    private boolean isRecur(final StackTraceElement[] st)
    {
        boolean v = false;
        for (int i = st.length - 1; i >= 1; --i)
        {
            final boolean c = st[i].getClassName().equals(thisClassName);
            final boolean m = st[i].getMethodName().equals("buildRules");
            if (c && m)
            {
                v = true;
                break;
            }
        }
        return v;
    }


    // ----------------------------------------------------------
    /**
     * Get the protection domains by Java reflection.  There is no public API
     * for this info, making this code Sun
     *  Java 1.5 JVM implementation
     * dependent.
     */
    private ProtectionDomain[] getProtectionDomains(
        final AccessControlContext context)
        throws IllegalStateException
    {
        ProtectionDomain[] pda = null;
        try
        {
            final Field[] fields =
                AccessControlContext.class.getDeclaredFields();
            if (null == fields)
            {
               throw new IllegalStateException("No fields");
            }
            for (int i = 0; i < fields.length; ++i)
            {
                if (fields[i].getName().equals("context"))
                {  // Warning:  JVM-dependent
                    fields[i].setAccessible(true);
                    final Object o = fields[i].get(context);
                    pda = (ProtectionDomain[])o;
                    break;
                }
            }

            // No 'context' field found, throw exception.
            if (null == pda)
            {
               throw new IllegalStateException("No \"context\" Field found!");
            }

        }
        catch (IllegalAccessException e)
        {
           e.printStackTrace();
        }
        return pda;
    }


    // ----------------------------------------------------------
    private String formatRule(
        final Permission permission, final ProtectionDomain pd)
    {
        final CodeSource cs = pd.getCodeSource();

        if (null == cs)
        {
            return null;
        }
        final URL url = cs.getLocation();
        if (null == url)
        {
           return null;
        }

        // Remove ProfilingSecurityManager.class codebase from output rule
        // consideration
        if (url.toString().equals(thisCodeSourceURLString))
        {
            return null;
        }

        final StringBuilder sb = new StringBuilder();
        sb.append("grant codeBase \"");
        sb.append(url.toString());
        sb.append("\" {");
        sb.append("permission ");
        sb.append(" ");
        sb.append(permission.getClass().getName());
        sb.append(" ");
        sb.append("\"");

        // Some complex permissions have quoted strings embedded or
        // literal carriage returns that must be escaped.

        final String permissionName = permission.getName();
        final String escapedPermissionName =
            permissionName.replace("\"","\\\"").replace("\r","\\\r");

        sb.append(escapedPermissionName);
        sb.append("\", ");
        sb.append("\"");
        sb.append(permission.getActions());
        sb.append("\";");
        sb.append("};");
        return sb.toString();
    }


    // ----------------------------------------------------------
    /**
     * If the rule has been seen during this runtime invocation, do not
     * print it again.
     */
    private boolean isCached(final String candidate)
    {
        synchronized (cache)
        {
            if (cache.contains(candidate))
            {
                return true;
            }
            else
            {
                cache.add(candidate);
                return false;
            }
        }
    }


    // ----------------------------------------------------------
    private void println(final String value)
    {
        String outFileName =
            System.getProperty("ProfilingSecurityManager.output");
        if (outFileName != null)
        {
            try
            {
                PrintStream out = new PrintStream(
                    new FileOutputStream(new File(outFileName), true));
                out.println(value);
                out.close();
            }
            catch (IOException e)
            {
                System.out.println(e);
            }
        }
        System.out.println(value);
    }


    //~ Instance/static variables .............................................

    /* Variables of pure convenience */
    final private String thisClassName;
    final private String thisCodeSourceURLString;
    final private String psmMsg = "ProfilingSecurityManager";
    final private Set<String> cache = new HashSet<String>();
}
