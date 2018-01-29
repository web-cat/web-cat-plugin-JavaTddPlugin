package net.sf.webcat;

import java.io.*;

//-------------------------------------------------------------------------
/**
 *  Use {@link student.testingsupport.PrintWriterWithHistory} instead.
 *
 *  @deprecated
 *  @author  Stephen Edwards
 *  @author Last changed by $Author: stedwar2 $
 *  @version $Revision: 1.3 $, $Date: 2010/02/23 19:47:10 $
 */
public class PrintWriterWithHistory
    extends student.testingsupport.PrintWriterWithHistory
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Use {@link student.testingsupport.PrintWriterWithHistory} instead.
     * @deprecated
     */
    public PrintWriterWithHistory()
    {
        super();
    }


    // ----------------------------------------------------------
    /**
     * Use {@link student.testingsupport.PrintWriterWithHistory} instead.
     *
     * @param  out        A character-output stream
     * @deprecated
     */
    public PrintWriterWithHistory(Writer out)
    {
        super(out);
    }


    // ----------------------------------------------------------
    /**
     * Use {@link student.testingsupport.PrintWriterWithHistory} instead.
     *
     * @param  out        A character-output stream
     * @param  autoFlush  A boolean; if true, the <tt>println</tt>,
     *                    <tt>printf</tt>, or <tt>format</tt> methods will
     *                    flush the output buffer
     * @deprecated
     */
    public PrintWriterWithHistory(Writer out, boolean autoFlush)
    {
        super(out, autoFlush);
    }


    // ----------------------------------------------------------
    /**
     * Use {@link student.testingsupport.PrintWriterWithHistory} instead.
     *
     * @param  out        An output stream
     *
     * @see java.io.OutputStreamWriter#OutputStreamWriter(java.io.OutputStream)
     * @deprecated
     */
    public PrintWriterWithHistory(OutputStream out)
    {
        super(out);
    }


    // ----------------------------------------------------------
    /**
     * Use {@link student.testingsupport.PrintWriterWithHistory} instead.
     *
     * @param  out        An output stream
     * @param  autoFlush  A boolean; if true, the <tt>println</tt>,
     *                    <tt>printf</tt>, or <tt>format</tt> methods will
     *                    flush the output buffer
     *
     * @see java.io.OutputStreamWriter#OutputStreamWriter(java.io.OutputStream)
     * @deprecated
     */
    public PrintWriterWithHistory(OutputStream out, boolean autoFlush)
    {
        super(out, autoFlush);
    }


    // ----------------------------------------------------------
    /**
     * Use {@link student.testingsupport.PrintWriterWithHistory} instead.
     *
     * @param  fileName
     *         The name of the file to use as the destination of this writer.
     *         If the file exists then it will be truncated to zero size;
     *         otherwise, a new file will be created.  The output will be
     *         written to the file and is buffered.
     *
     * @throws  FileNotFoundException
     *          If the given string does not denote an existing, writable
     *          regular file and a new regular file of that name cannot be
     *          created, or if some other error occurs while opening or
     *          creating the file
     *
     * @throws  SecurityException
     *          If a security manager is present and {@link
     *          SecurityManager#checkWrite checkWrite(fileName)} denies write
     *          access to the file
     * @deprecated
     */
    public PrintWriterWithHistory(String fileName)
        throws FileNotFoundException
    {
        super(fileName);
    }


    // ----------------------------------------------------------
    /**
     * Use {@link student.testingsupport.PrintWriterWithHistory} instead.
     *
     * @param  fileName
     *         The name of the file to use as the destination of this writer.
     *         If the file exists then it will be truncated to zero size;
     *         otherwise, a new file will be created.  The output will be
     *         written to the file and is buffered.
     *
     * @param  csn
     *         The name of a supported {@linkplain java.nio.charset.Charset
     *         charset}
     *
     * @throws  FileNotFoundException
     *          If the given string does not denote an existing, writable
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
     * @deprecated
     */
    public PrintWriterWithHistory(String fileName, String csn)
        throws FileNotFoundException, UnsupportedEncodingException
    {
        super(fileName, csn);
    }


    // ----------------------------------------------------------
    /**
     * Use {@link student.testingsupport.PrintWriterWithHistory} instead.
     *
     * @param  file
     *         The file to use as the destination of this writer.  If the file
     *         exists then it will be truncated to zero size; otherwise, a new
     *         file will be created.  The output will be written to the file
     *         and is buffered.
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
     * @deprecated
     */
    public PrintWriterWithHistory(File file)
        throws FileNotFoundException
    {
        super(file);
    }


    // ----------------------------------------------------------
    /**
     * Use {@link student.testingsupport.PrintWriterWithHistory} instead.
     *
     * @param  file
     *         The file to use as the destination of this writer.  If the file
     *         exists then it will be truncated to zero size; otherwise, a new
     *         file will be created.  The output will be written to the file
     *         and is buffered.
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
     *          SecurityManager#checkWrite checkWrite(file.getPath())}
     *          denies write access to the file
     *
     * @throws  UnsupportedEncodingException
     *          If the named charset is not supported
     * @deprecated
     */
    public PrintWriterWithHistory(File file, String csn)
        throws FileNotFoundException, UnsupportedEncodingException
    {
        super(file, csn);
    }
}
