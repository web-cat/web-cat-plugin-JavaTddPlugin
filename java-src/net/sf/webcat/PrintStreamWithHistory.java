package net.sf.webcat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

//-------------------------------------------------------------------------
/**
 *  Use {@link student.testingsupport.PrintStreamWithHistory} instead.
 *
 *  @deprecated
 *  @author  Stephen Edwards
 *  @author Last changed by $Author$
 *  @version $Revision$, $Date$
 */
public class PrintStreamWithHistory
    extends student.testingsupport.PrintStreamWithHistory
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Use {@link student.testingsupport.PrintStreamWithHistory} instead.
     *
     * @param  out        The output stream to which values and objects will be
     *                    printed
     * @deprecated
     */
    public PrintStreamWithHistory(OutputStream out)
    {
        super(out);
    }


    // ----------------------------------------------------------
    /**
     * Use {@link student.testingsupport.PrintStreamWithHistory} instead.
     *
     * @param  out        The output stream to which values and objects will be
     *                    printed
     * @param  autoFlush  A boolean; if true, the output buffer will be flushed
     *                    whenever a byte array is written, one of the
     *                    <code>println</code> methods is invoked, or a newline
     *                    character or byte (<code>'\n'</code>) is written
     *
     * @see java.io.PrintWriter#PrintWriter(java.io.OutputStream, boolean)
     * @deprecated
     */
    public PrintStreamWithHistory(OutputStream out, boolean autoFlush)
    {
        super(out, autoFlush);
    }


    // ----------------------------------------------------------
    /**
     * Use {@link student.testingsupport.PrintStreamWithHistory} instead.
     *
     * @param  out        The output stream to which values and objects will be
     *                    printed
     * @param  autoFlush  A boolean; if true, the output buffer will be flushed
     *                    whenever a byte array is written, one of the
     *                    <code>println</code> methods is invoked, or a newline
     *                    character or byte (<code>'\n'</code>) is written
     * @param  encoding   The name of a supported
     *                    <a href="../lang/package-summary.html#charenc">
     *                    character encoding</a>
     *
     * @exception  UnsupportedEncodingException
     *             If the named encoding is not supported
     * @deprecated
     */
    public PrintStreamWithHistory(
        OutputStream out, boolean autoFlush, String encoding)
        throws UnsupportedEncodingException
    {
        super(out, autoFlush, encoding);
    }


    // ----------------------------------------------------------
    /**
     * Use {@link student.testingsupport.PrintStreamWithHistory} instead.
     *
     * @param  fileName
     *         The name of the file to use as the destination of this print
     *         stream.  If the file exists, then it will be truncated to
     *         zero size; otherwise, a new file will be created.  The output
     *         will be written to the file and is buffered.
     *
     * @throws  FileNotFoundException
     *          If the given file object does not denote an existing, writable
     *          regular file and a new regular file of that name cannot be
     *          created, or if some other error occurs while opening or
     *          creating the file
     *
     * @throws  SecurityException
     *          If a security manager is present and {@link
     *          SecurityManager#checkWrite checkWrite(fileName)} denies write
     *          access to the file
     *
     * @since  1.5
     * @deprecated
     */
    public PrintStreamWithHistory(String fileName)
        throws FileNotFoundException
    {
        super(fileName);
    }


    // ----------------------------------------------------------
    /**
     * Use {@link student.testingsupport.PrintStreamWithHistory} instead.
     *
     * @param  fileName
     *         The name of the file to use as the destination of this print
     *         stream.  If the file exists, then it will be truncated to
     *         zero size; otherwise, a new file will be created.  The output
     *         will be written to the file and is buffered.
     *
     * @param  csn
     *         The name of a supported {@linkplain java.nio.charset.Charset
     *         charset}
     *
     * @throws  FileNotFoundException
     *          If the given file object does not denote an existing, writable
     *          regular file and a new regular file of that name cannot be
     *          created, or if some other error occurs while opening or
     *          creating the file
     *
     * @throws  SecurityException
     *          If a security manager is present and {@link
     *          SecurityManager#checkWrite checkWrite(fileName)} denies write
     *          access to the file
     *
     * @throws  UnsupportedEncodingException
     *          If the named charset is not supported
     *
     * @deprecated
     */
    public PrintStreamWithHistory(String fileName, String csn)
        throws FileNotFoundException, UnsupportedEncodingException
    {
        super(fileName, csn);
    }


    // ----------------------------------------------------------
    /**
     * Use {@link student.testingsupport.PrintStreamWithHistory} instead.
     *
     * @param  file
     *         The file to use as the destination of this print stream.  If the
     *         file exists, then it will be truncated to zero size; otherwise,
     *         a new file will be created.  The output will be written to the
     *         file and is buffered.
     *
     * @throws  FileNotFoundException
     *          If the given file object does not denote an existing, writable
     *          regular file and a new regular file of that name cannot be
     *          created, or if some other error occurs while opening or
     *          creating the file
     *
     * @throws  SecurityException
     *          If a security manager is present and {@link
     *          SecurityManager#checkWrite checkWrite(file.getPath())}
     *          denies write access to the file
     *
     * @deprecated
     */
    public PrintStreamWithHistory(File file)
        throws FileNotFoundException
    {
        super(file);
    }


    // ----------------------------------------------------------
    /**
     * Use {@link student.testingsupport.PrintStreamWithHistory} instead.
     *
     * @param  file
     *         The file to use as the destination of this print stream.  If the
     *         file exists, then it will be truncated to zero size; otherwise,
     *         a new file will be created.  The output will be written to the
     *         file and is buffered.
     *
     * @param  csn
     *         The name of a supported {@linkplain java.nio.charset.Charset
     *         charset}
     *
     * @throws  FileNotFoundException
     *          If the given file object does not denote an existing, writable
     *          regular file and a new regular file of that name cannot be
     *          created, or if some other error occurs while opening or
     *          creating the file
     *
     * @throws  SecurityException
     *          If a security manager is presentand {@link
     *          SecurityManager#checkWrite checkWrite(file.getPath())}
     *          denies write access to the file
     *
     * @throws  UnsupportedEncodingException
     *          If the named charset is not supported
     *
     * @deprecated
     */
    public PrintStreamWithHistory(File file, String csn)
        throws FileNotFoundException, UnsupportedEncodingException
    {
        super(file, csn);
    }
}
