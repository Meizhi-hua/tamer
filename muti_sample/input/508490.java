final class SerializerTraceWriter extends Writer implements WriterChain
{
    private final java.io.Writer m_writer;
    private final SerializerTrace m_tracer;
    private int buf_length;
    private byte buf[];
    private int count;
    private void setBufferSize(int size)
    {
        buf = new byte[size + 3];
        buf_length = size;
        count = 0;
    }
    public SerializerTraceWriter(Writer out, SerializerTrace tracer)
    {
        m_writer = out;
        m_tracer = tracer;
        setBufferSize(1024);
    }
    private void flushBuffer() throws IOException
    {
        if (count > 0)
        {
            char[] chars = new char[count];
            for(int i=0; i<count; i++)
                chars[i] = (char) buf[i];
            if (m_tracer != null)
                m_tracer.fireGenerateEvent(
                    SerializerTrace.EVENTTYPE_OUTPUT_CHARACTERS,
                    chars,
                    0,
                    chars.length);
            count = 0;
        }
    }
    public void flush() throws java.io.IOException
    {
        if (m_writer != null)
            m_writer.flush();
        flushBuffer();
    }
    public void close() throws java.io.IOException
    {
        if (m_writer != null)   
            m_writer.close();
        flushBuffer();
    }
    public void write(final int c) throws IOException
    {
        if (m_writer != null)
            m_writer.write(c);
        if (count >= buf_length)
            flushBuffer();
        if (c < 0x80)
        {
            buf[count++] = (byte) (c);
        }
        else if (c < 0x800)
        {
            buf[count++] = (byte) (0xc0 + (c >> 6));
            buf[count++] = (byte) (0x80 + (c & 0x3f));
        }
        else
        {
            buf[count++] = (byte) (0xe0 + (c >> 12));
            buf[count++] = (byte) (0x80 + ((c >> 6) & 0x3f));
            buf[count++] = (byte) (0x80 + (c & 0x3f));
        }
    }
    public void write(final char chars[], final int start, final int length)
        throws java.io.IOException
    {
        if (m_writer != null)
            m_writer.write(chars, start, length);
        int lengthx3 = (length << 1) + length;
        if (lengthx3 >= buf_length)
        {
            flushBuffer();
            setBufferSize(2 * lengthx3);
        }
        if (lengthx3 > buf_length - count)
        {
            flushBuffer();
        }
        final int n = length + start;
        for (int i = start; i < n; i++)
        {
            final char c = chars[i];
            if (c < 0x80)
                buf[count++] = (byte) (c);
            else if (c < 0x800)
            {
                buf[count++] = (byte) (0xc0 + (c >> 6));
                buf[count++] = (byte) (0x80 + (c & 0x3f));
            }
            else
            {
                buf[count++] = (byte) (0xe0 + (c >> 12));
                buf[count++] = (byte) (0x80 + ((c >> 6) & 0x3f));
                buf[count++] = (byte) (0x80 + (c & 0x3f));
            }
        }
    }
    public void write(final String s) throws IOException
    {
        if (m_writer != null)
            m_writer.write(s);
        final int length = s.length();
        int lengthx3 = (length << 1) + length;
        if (lengthx3 >= buf_length)
        {
            flushBuffer();
            setBufferSize(2 * lengthx3);
        }
        if (lengthx3 > buf_length - count)
        {
            flushBuffer();
        }
        for (int i = 0; i < length; i++)
        {
            final char c = s.charAt(i);
            if (c < 0x80)
                buf[count++] = (byte) (c);
            else if (c < 0x800)
            {
                buf[count++] = (byte) (0xc0 + (c >> 6));
                buf[count++] = (byte) (0x80 + (c & 0x3f));
            }
            else
            {
                buf[count++] = (byte) (0xe0 + (c >> 12));
                buf[count++] = (byte) (0x80 + ((c >> 6) & 0x3f));
                buf[count++] = (byte) (0x80 + (c & 0x3f));
            }
        }
    }
    public Writer getWriter()
    {
        return m_writer;
    }
    public OutputStream getOutputStream()
    {
        OutputStream retval = null;
        if (m_writer instanceof WriterChain)
            retval = ((WriterChain) m_writer).getOutputStream();
        return retval;
    }
}
