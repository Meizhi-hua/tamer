@TestTargetClass(java.nio.charset.CharsetDecoder.class)
public class UTF16LECharsetDecoderTest extends AbstractCharsetDecoderTestCase {
    protected void setUp() throws Exception {
        cs = Charset.forName("utf-16le");
        unibytes = new byte[] { 32, 0, 98, 0, 117, 0, 102, 0, 102, 0, 101, 0,
                114, 0 };
        super.setUp();
    }
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    ByteBuffer getUnmappedByteBuffer() throws UnsupportedEncodingException {
        return null;
    }
    ByteBuffer getMalformByteBuffer() throws UnsupportedEncodingException {
        return null;
    }
    ByteBuffer getExceptionByteArray() throws UnsupportedEncodingException {
        return null;
    }
    byte[] getUnibytes() {
        return new byte[] { 32, 0, 98, 0, 117, 0, 102, 0, 102, 0, 101, 0, 114,
                0 };
    }
}
