package net.sf.webcat;

import java.io.*;

//-------------------------------------------------------------------------
/**
 *  A utility class that provides functions for replacing {@link System}
 *  I/O streams with helpful alternative implementations to make some
 *  testing jobs easier.  This class is really for use by infrastructure
 *  and support code, and students should never need to use it directly.
 *
 *  <p>Since this class provides only static methods, clients should not
 *  create an instance.  As a result, it provides no public constructors.</p>
 *
 *  @author  Stephen Edwards
 *  @version 2007.08.15
 */
public class SystemIOUtilities
{
    //~ Instance/static variables .............................................

    private static PrintStream originalOut = System.out;
    private static PrintStream originalErr = System.err;
    private static InputStream originalIn  = System.in;

    private static PrintStreamWithHistory  wrappedOut;
    private static PrintStreamWithHistory  wrappedErr;


    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Since this class provides only static methods, clients should not create
     * an instance.
     */
    private SystemIOUtilities()
    {
        // nothing to do
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Get a "wrapped" version of {@link System#out} that provides
     * history recording functions.
     * @return a version of System.out that provides history features
     */
    public static PrintStreamWithHistory out()
    {
        assertNotOnServer();
        if (wrappedOut != null && wrappedOut != System.out)
        {
            throw new IllegalStateException("Previously wrapped System.out "
                + "was replaced by user code");
        }
        if (wrappedOut == null)
        {
            // Using System.out here just in case it's been replaced!
            wrappedOut = new PrintStreamWithHistory(System.out);
            System.setOut(wrappedOut);
        }
        return wrappedOut;
    }


    // ----------------------------------------------------------
    /**
     * "Unwrap" {@link System#out} by removing any history recording
     * wrapper, and return it to its original state.
     */
    public static void restoreSystemOut()
    {
        assertNotOnServer();
        if (System.out != originalOut)
        {
            if (wrappedOut == null || wrappedOut != System.out)
            {
                throw new IllegalStateException("Previously wrapped "
                    + "System.out was replaced by user code");
            }
            System.setOut(originalOut);
            wrappedOut = null;
        }
    }


    // ----------------------------------------------------------
    /**
     * Get a "wrapped" version of {@link System#err} that provides
     * history recording functions.
     * @return a version of System.err that provides history features
     */
    public static PrintStreamWithHistory err()
    {
        assertNotOnServer();
        if (wrappedErr != null && wrappedErr != System.err)
        {
            throw new IllegalStateException("Previously wrapped System.err "
                + "was replaced by user code");
        }
        if (wrappedErr == null)
        {
            // Using System.out here just in case it's been replaced!
            wrappedErr = new PrintStreamWithHistory(System.err);
            System.setErr(wrappedErr);
        }
        return wrappedErr;
    }


    // ----------------------------------------------------------
    /**
     * "Unwrap" {@link System#err} by removing any history recording
     * wrapper, and return it to its original state.
     */
    public static void restoreSystemErr()
    {
        assertNotOnServer();
        if (System.err != originalErr)
        {
            if (wrappedErr == null || wrappedErr != System.err)
            {
                throw new IllegalStateException("Previously wrapped "
                    + "System.err was replaced by user code");
            }
            System.setErr(originalErr);
            wrappedErr = null;
        }
    }


    // ----------------------------------------------------------
    /**
     * Replace {@link System#in} with the contents of the given string.
     * @param contents The content to read from
     */
    @SuppressWarnings("deprecation")
    public static void replaceSystemInContents(String contents)
    {
        assertNotOnServer();
        System.setIn(new StringBufferInputStream(contents));
    }


    // ----------------------------------------------------------
    /**
     * Restore {@link System#in} to its original value.
     */
    public static void restoreSystemIn()
    {
        assertNotOnServer();
        System.setIn(originalIn);
    }


    // ----------------------------------------------------------
    /**
     * Checks to see if the calling program is running under the Apache
     * Tomcat servlet container.
     * @return True if running as a servlet
     */
    public static boolean isOnServer()
    {
        boolean inServlet = false;
        try
        {
            if (SystemIOUtilities.class.getClassLoader()
                    .loadClass("cs1705.web.internal.Interpreter") != null)
            {
                inServlet = true;
            }
        }
        catch (ClassNotFoundException e)
        {
            // If that class isn't around, then we're not running under
            // the ZK servlet engine, so assume tweaking System.in/out
            // is OK.
            inServlet = false;
        }
        return inServlet;
    }


    // ----------------------------------------------------------
    /**
     * Checks to see if the calling program is running under the Apache
     * Tomcat servlet container.  When running in such an environment,
     * some behaviors should be avoided.  For example, it is not appropriate
     * to modify globally shared resources like those in the class System.
     */
    public static void assertNotOnServer()
    {
        assert !isOnServer()
            : "This method cannot be executed while running on the server.";
    }
}
