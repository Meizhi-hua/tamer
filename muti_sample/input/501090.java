public class CharArrayWriterTest extends TestCase {
    @SmallTest
    public void testCharArrayWriter() throws Exception {
        String str = "AbCdEfGhIjKlMnOpQrStUvWxYz";
        CharArrayWriter a = new CharArrayWriter();
        CharArrayWriter b = new CharArrayWriter();
        a.write(str, 0, 26);
        a.write('X');
        a.writeTo(b);
        assertEquals(27, a.size());
        assertEquals("AbCdEfGhIjKlMnOpQrStUvWxYzX", a.toString());
        b.write("alphabravodelta", 5, 5);
        b.append('X');
        assertEquals("AbCdEfGhIjKlMnOpQrStUvWxYzXbravoX", b.toString());
        b.append("omega");
        assertEquals("AbCdEfGhIjKlMnOpQrStUvWxYzXbravoXomega", b.toString());
    }
}
