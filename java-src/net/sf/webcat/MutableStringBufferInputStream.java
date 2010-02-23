package net.sf.webcat;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;

@SuppressWarnings("deprecation")
public class MutableStringBufferInputStream
    extends InputStream
{
    private InputStream src;
    private String name = "an input stream";

    public MutableStringBufferInputStream(String contents)
    {
        resetContents(contents);
    }

    public MutableStringBufferInputStream(InputStream contents)
    {
        resetContents(contents);
    }

    public int read()
        throws IOException
    {
        if (src == null)
        {
            handleMissingContents();
        }
        return src.read();
    }

    public int read(byte[] b, int off, int len)
        throws IOException
    {
        if (src == null)
        {
            handleMissingContents();
        }
        return src.read(b, off, len);
    }

    public long skip(long n)
        throws IOException
    {
        if (src == null)
        {
            handleMissingContents();
        }
        return src.skip(n);
    }

    public int available()
        throws IOException
    {
        if (src == null)
        {
            handleMissingContents();
        }
        return src.available();
    }

    public void reset()
        throws IOException
    {
        if (src == null)
        {
            handleMissingContents();
        }
        src.reset();
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void resetContents(String newContents)
    {
        if (newContents == null)
        {
            src = null;
        }
        else
        {
            src = new StringBufferInputStream(newContents);
        }
    }

    public void resetContents(InputStream newContents)
    {
        src = newContents;
    }

    protected void handleMissingContents()
    {
        throw new IllegalStateException("The program attempted to read from "
            + getName()
            + ", but no contents have been set yet.");
    }
}
