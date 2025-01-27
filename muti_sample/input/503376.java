public class FastXmlSerializer implements XmlSerializer {
    private static final String ESCAPE_TABLE[] = new String[] {
        null,     null,     null,     null,     null,     null,     null,     null,  
        null,     null,     null,     null,     null,     null,     null,     null,  
        null,     null,     null,     null,     null,     null,     null,     null,  
        null,     null,     null,     null,     null,     null,     null,     null,  
        null,     null,     "&quot;", null,     null,     null,     "&amp;",  null,  
        null,     null,     null,     null,     null,     null,     null,     null,  
        null,     null,     null,     null,     null,     null,     null,     null,  
        null,     null,     null,     null,     "&lt;",   null,     "&gt;",   null,  
    };
    private static final int BUFFER_LEN = 8192;
    private final char[] mText = new char[BUFFER_LEN];
    private int mPos;
    private Writer mWriter;
    private OutputStream mOutputStream;
    private CharsetEncoder mCharset;
    private ByteBuffer mBytes = ByteBuffer.allocate(BUFFER_LEN);
    private boolean mInTag;
    private void append(char c) throws IOException {
        int pos = mPos;
        if (pos >= (BUFFER_LEN-1)) {
            flush();
            pos = mPos;
        }
        mText[pos] = c;
        mPos = pos+1;
    }
    private void append(String str, int i, final int length) throws IOException {
        if (length > BUFFER_LEN) {
            final int end = i + length;
            while (i < end) {
                int next = i + BUFFER_LEN;
                append(str, i, next<end ? BUFFER_LEN : (end-i));
                i = next;
            }
            return;
        }
        int pos = mPos;
        if ((pos+length) > BUFFER_LEN) {
            flush();
            pos = mPos;
        }
        str.getChars(i, i+length, mText, pos);
        mPos = pos + length;
    }
    private void append(char[] buf, int i, final int length) throws IOException {
        if (length > BUFFER_LEN) {
            final int end = i + length;
            while (i < end) {
                int next = i + BUFFER_LEN;
                append(buf, i, next<end ? BUFFER_LEN : (end-i));
                i = next;
            }
            return;
        }
        int pos = mPos;
        if ((pos+length) > BUFFER_LEN) {
            flush();
            pos = mPos;
        }
        System.arraycopy(buf, i, mText, pos, length);
        mPos = pos + length;
    }
    private void append(String str) throws IOException {
        append(str, 0, str.length());
    }
    private void escapeAndAppendString(final String string) throws IOException {
        final int N = string.length();
        final char NE = (char)ESCAPE_TABLE.length;
        final String[] escapes = ESCAPE_TABLE;
        int lastPos = 0;
        int pos;
        for (pos=0; pos<N; pos++) {
            char c = string.charAt(pos);
            if (c >= NE) continue;
            String escape = escapes[c];
            if (escape == null) continue;
            if (lastPos < pos) append(string, lastPos, pos-lastPos);
            lastPos = pos + 1;
            append(escape);
        }
        if (lastPos < pos) append(string, lastPos, pos-lastPos);
    }
    private void escapeAndAppendString(char[] buf, int start, int len) throws IOException {
        final char NE = (char)ESCAPE_TABLE.length;
        final String[] escapes = ESCAPE_TABLE;
        int end = start+len;
        int lastPos = start;
        int pos;
        for (pos=start; pos<end; pos++) {
            char c = buf[pos];
            if (c >= NE) continue;
            String escape = escapes[c];
            if (escape == null) continue;
            if (lastPos < pos) append(buf, lastPos, pos-lastPos);
            lastPos = pos + 1;
            append(escape);
        }
        if (lastPos < pos) append(buf, lastPos, pos-lastPos);
    }
    public XmlSerializer attribute(String namespace, String name, String value) throws IOException,
            IllegalArgumentException, IllegalStateException {
        append(' ');
        if (namespace != null) {
            append(namespace);
            append(':');
        }
        append(name);
        append("=\"");
        escapeAndAppendString(value);
        append('"');
        return this;
    }
    public void cdsect(String text) throws IOException, IllegalArgumentException,
            IllegalStateException {
        throw new UnsupportedOperationException();
    }
    public void comment(String text) throws IOException, IllegalArgumentException,
            IllegalStateException {
        throw new UnsupportedOperationException();
    }
    public void docdecl(String text) throws IOException, IllegalArgumentException,
            IllegalStateException {
        throw new UnsupportedOperationException();
    }
    public void endDocument() throws IOException, IllegalArgumentException, IllegalStateException {
        flush();
    }
    public XmlSerializer endTag(String namespace, String name) throws IOException,
            IllegalArgumentException, IllegalStateException {
        if (mInTag) {
            append(" />\n");
        } else {
            append("</");
            if (namespace != null) {
                append(namespace);
                append(':');
            }
            append(name);
            append(">\n");
        }
        mInTag = false;
        return this;
    }
    public void entityRef(String text) throws IOException, IllegalArgumentException,
            IllegalStateException {
        throw new UnsupportedOperationException();
    }
    private void flushBytes() throws IOException {
        int position;
        if ((position = mBytes.position()) > 0) {
            mBytes.flip();
            mOutputStream.write(mBytes.array(), 0, position);
            mBytes.clear();
        }
    }
    public void flush() throws IOException {
        if (mPos > 0) {
            if (mOutputStream != null) {
                CharBuffer charBuffer = CharBuffer.wrap(mText, 0, mPos);
                CoderResult result = mCharset.encode(charBuffer, mBytes, true);
                while (true) {
                    if (result.isError()) {
                        throw new IOException(result.toString());
                    } else if (result.isOverflow()) {
                        flushBytes();
                        result = mCharset.encode(charBuffer, mBytes, true);
                        continue;
                    }
                    break;
                }
                flushBytes();
                mOutputStream.flush();
            } else {
                mWriter.write(mText, 0, mPos);
                mWriter.flush();
            }
            mPos = 0;
        }
    }
    public int getDepth() {
        throw new UnsupportedOperationException();
    }
    public boolean getFeature(String name) {
        throw new UnsupportedOperationException();
    }
    public String getName() {
        throw new UnsupportedOperationException();
    }
    public String getNamespace() {
        throw new UnsupportedOperationException();
    }
    public String getPrefix(String namespace, boolean generatePrefix)
            throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }
    public Object getProperty(String name) {
        throw new UnsupportedOperationException();
    }
    public void ignorableWhitespace(String text) throws IOException, IllegalArgumentException,
            IllegalStateException {
        throw new UnsupportedOperationException();
    }
    public void processingInstruction(String text) throws IOException, IllegalArgumentException,
            IllegalStateException {
        throw new UnsupportedOperationException();
    }
    public void setFeature(String name, boolean state) throws IllegalArgumentException,
            IllegalStateException {
        if (name.equals("http:
            return;
        }
        throw new UnsupportedOperationException();
    }
    public void setOutput(OutputStream os, String encoding) throws IOException,
            IllegalArgumentException, IllegalStateException {
        if (os == null)
            throw new IllegalArgumentException();
        if (true) {
            try {
                mCharset = Charset.forName(encoding).newEncoder();
            } catch (IllegalCharsetNameException e) {
                throw (UnsupportedEncodingException) (new UnsupportedEncodingException(
                        encoding).initCause(e));
            } catch (UnsupportedCharsetException e) {
                throw (UnsupportedEncodingException) (new UnsupportedEncodingException(
                        encoding).initCause(e));
            }
            mOutputStream = os;
        } else {
            setOutput(
                encoding == null
                    ? new OutputStreamWriter(os)
                    : new OutputStreamWriter(os, encoding));
        }
    }
    public void setOutput(Writer writer) throws IOException, IllegalArgumentException,
            IllegalStateException {
        mWriter = writer;
    }
    public void setPrefix(String prefix, String namespace) throws IOException,
            IllegalArgumentException, IllegalStateException {
        throw new UnsupportedOperationException();
    }
    public void setProperty(String name, Object value) throws IllegalArgumentException,
            IllegalStateException {
        throw new UnsupportedOperationException();
    }
    public void startDocument(String encoding, Boolean standalone) throws IOException,
            IllegalArgumentException, IllegalStateException {
        append("<?xml version='1.0' encoding='utf-8' standalone='"
                + (standalone ? "yes" : "no") + "' ?>\n");
    }
    public XmlSerializer startTag(String namespace, String name) throws IOException,
            IllegalArgumentException, IllegalStateException {
        if (mInTag) {
            append(">\n");
        }
        append('<');
        if (namespace != null) {
            append(namespace);
            append(':');
        }
        append(name);
        mInTag = true;
        return this;
    }
    public XmlSerializer text(char[] buf, int start, int len) throws IOException,
            IllegalArgumentException, IllegalStateException {
        if (mInTag) {
            append(">");
            mInTag = false;
        }
        escapeAndAppendString(buf, start, len);
        return this;
    }
    public XmlSerializer text(String text) throws IOException, IllegalArgumentException,
            IllegalStateException {
        if (mInTag) {
            append(">");
            mInTag = false;
        }
        escapeAndAppendString(text);
        return this;
    }
}
