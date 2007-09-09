package net.sf.webcat;

import java.io.*;

//-------------------------------------------------------------------------
/**
 *  An enhanced version of {@link PrintWriter} that provides for a history
 *  recall function and some other features making I/O testing a bit
 *  easier to perform.  See the documentation for {@link PrintWriter} for
 *  more thorough details on what methods are provided.
 *
 *  @author  Stephen Edwards
 *  @version 2007.08.15
 */
public class PrintWriterWithHistory
    extends PrintWriter
{
    //~ Instance/static variables .............................................

    private StringWriter  history = new StringWriter();


    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Create a new PrintWriter with no destination--useful when you just
     * want to record the history (for testing, for example).  If you want
     * to create a PrintWriter that points to {@link System#out} instead,
     * use this:
     * <pre>
     * new PrintWriterWithHistory(System.out);
     * </pre>
     */
    public PrintWriterWithHistory()
    {
        super(new NullWriter());
    }


    // ----------------------------------------------------------
    /**
     * Create a new PrintWriter, without automatic line flushing.
     *
     * @param  out        A character-output stream
     */
    public PrintWriterWithHistory(Writer out)
    {
        super(out, false);
    }


    // ----------------------------------------------------------
    /**
     * Create a new PrintWriter.
     *
     * @param  out        A character-output stream
     * @param  autoFlush  A boolean; if true, the <tt>println</tt>,
     *                    <tt>printf</tt>, or <tt>format</tt> methods will
     *                    flush the output buffer
     */
    public PrintWriterWithHistory(Writer out, boolean autoFlush)
    {
        super(out, autoFlush);
    }


    // ----------------------------------------------------------
    /**
     * Create a new PrintWriter, <b>with</b> automatic line flushing, from an
     * existing OutputStream.  This convenience constructor creates the
     * necessary intermediate OutputStreamWriter, which will convert characters
     * into bytes using the default character encoding.  It differs from the
     * corresponding constructor in {@link PrintWriter} by forcing automatic
     * line flushing on instead of off, since students most often use it to
     * pass in something like {@link System#out}.
     *
     * @param  out        An output stream
     *
     * @see java.io.OutputStreamWriter#OutputStreamWriter(java.io.OutputStream)
     */
    public PrintWriterWithHistory(OutputStream out)
    {
        super(out, true);
    }


    // ----------------------------------------------------------
    /**
     * Create a new PrintWriter from an existing OutputStream.  This
     * convenience constructor creates the necessary intermediate
     * OutputStreamWriter, which will convert characters into bytes using the
     * default character encoding.
     *
     * @param  out        An output stream
     * @param  autoFlush  A boolean; if true, the <tt>println</tt>,
     *                    <tt>printf</tt>, or <tt>format</tt> methods will
     *                    flush the output buffer
     *
     * @see java.io.OutputStreamWriter#OutputStreamWriter(java.io.OutputStream)
     */
    public PrintWriterWithHistory(OutputStream out, boolean autoFlush)
    {
        super(out, autoFlush);
    }


    // ----------------------------------------------------------
    /**
     * Creates a new PrintWriter, without automatic line flushing, with the
     * specified file name.  This convenience constructor creates the necessary
     * intermediate {@link java.io.OutputStreamWriter OutputStreamWriter},
     * which will encode characters using the {@linkplain
     * java.nio.charset.Charset#defaultCharset default charset} for this
     * instance of the Java virtual machine.
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
     */
    public PrintWriterWithHistory(String fileName)
        throws FileNotFoundException
    {
        super(fileName);
    }


    // ----------------------------------------------------------
    /**
     * Creates a new PrintWriter, without automatic line flushing, with the
     * specified file name and charset.  This convenience constructor creates
     * the necessary intermediate {@link java.io.OutputStreamWriter
     * OutputStreamWriter}, which will encode characters using the provided
     * charset.
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
     */
    public PrintWriterWithHistory(String fileName, String csn)
        throws FileNotFoundException, UnsupportedEncodingException
    {
        super(fileName, csn);
    }


    // ----------------------------------------------------------
    /**
     * Creates a new PrintWriter, without automatic line flushing, with the
     * specified file.  This convenience constructor creates the necessary
     * intermediate {@link java.io.OutputStreamWriter OutputStreamWriter},
     * which will encode characters using the {@linkplain
     * java.nio.charset.Charset#defaultCharset default charset} for this
     * instance of the Java virtual machine.
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
     */
    public PrintWriterWithHistory(File file)
        throws FileNotFoundException
    {
        super(file);
    }


    // ----------------------------------------------------------
    /**
     * Creates a new PrintWriter, without automatic line flushing, with the
     * specified file and charset.  This convenience constructor creates the
     * necessary intermediate {@link java.io.OutputStreamWriter
     * OutputStreamWriter}, which will encode characters using the provided
     * charset.
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
     */
    public PrintWriterWithHistory(File file, String csn)
        throws FileNotFoundException, UnsupportedEncodingException
    {
        super(file, csn);
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Retrieve the text history of what has been sent to this PrintWriter.
     * This will include all text printed through this object.  The
     * {@link #clearHistory()} method resets the history to be empty, just
     * as when the object was first created.  Note that newline characters
     * in the history are always represented by '\n', regardless of what
     * value the system <code>line.separator</code> property has.
     * @return all the text sent to this PrintWriter
     */
    public String getHistory()
    {
        return history.toString();
    }


    // ----------------------------------------------------------
    /**
     * Reset this object's history to be empty, just as when the object was
     * first created.  You can access the history using {@link #getHistory()}.
     */
    public void clearHistory()
    {
        getHistoryBuffer().setLength(0);
    }


    // ----------------------------------------------------------
    /**
     * Retrieve the StringBuffer object used to store this objects text
     * history.
     * @return The history as a string buffer
     */
    public StringBuffer getHistoryBuffer()
    {
        return history.getBuffer();
    }


    // ----------------------------------------------------------
    /**
     * Write a single character.
     * @param c int specifying a character to be written.
     */
    public void write(int c)
    {
        synchronized (history)
        {
            super.write(c);
            history.write(c);
        }
    }


    // ----------------------------------------------------------
    /**
     * Write A Portion of an array of characters.
     * @param buf Array of characters
     * @param off Offset from which to start writing characters
     * @param len Number of characters to write
     */
    public void write(char buf[], int off, int len)
    {
        synchronized (history)
        {
            super.write(buf, off, len);
            history.write(buf, off, len);
        }
    }


    // ----------------------------------------------------------
    /**
     * Write a portion of a string.
     * @param s A String
     * @param off Offset from which to start writing characters
     * @param len Number of characters to write
     */
    public void write(String s, int off, int len)
    {
        synchronized (history)
        {
            super.write(s, off, len);
            history.write(s, off, len);
        }
    }


    // ----------------------------------------------------------
    /**
     * Terminate the current line by writing the line separator string.  The
     * line separator string is defined by the system property
     * <code>line.separator</code>, and is not necessarily a single newline
     * character (<code>'\n'</code>).
     */
    public void println()
    {
        synchronized (history)
        {
            super.println();
            history.append('\n');
        }
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * This is an inner class that is used for "null" destinations, such as
     * in the default constructor for PrintWriterWithHistory.
     */
    protected static class NullWriter
        extends Writer
    {

        public void close() throws IOException
        {
            // Nothing to do
        }

        public void flush() throws IOException
        {
            // Nothing to do
        }

        public void write( char[] cbuf, int off, int len ) throws IOException
        {
            // Nothing to do
        }

    }
}
