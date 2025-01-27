public class LoggingPrintStreamTest extends TestCase {
    TestPrintStream out = new TestPrintStream();
    public void testPrintException() {
        @SuppressWarnings("ThrowableInstanceNeverThrown")
        Throwable t = new Throwable("Ignore me.");
        StringWriter sout = new StringWriter();
        t.printStackTrace(new PrintWriter(sout));
        t.printStackTrace(out);
        String[] lines = sout.toString().split("\\n");
        assertEquals(Arrays.asList(lines), out.lines);
    }
    public void testPrintObject() {
        Object o = new Object();
        out.print(4);
        out.print(o);
        out.print(2);
        out.flush();
        assertEquals(Arrays.asList("4" + o + "2"), out.lines);
    }
    public void testPrintlnObject() {
        Object o = new Object();
        out.print(4);
        out.println(o);
        out.print(2);
        out.flush();
        assertEquals(Arrays.asList("4" + o, "2"), out.lines);
    }
    public void testPrintf() {
        out.printf("Name: %s\nEmployer: %s", "Bob", "Google");
        assertEquals(Arrays.asList("Name: Bob"), out.lines);
        out.flush();
        assertEquals(Arrays.asList("Name: Bob", "Employer: Google"), out.lines);
    }
    public void testPrintInt() {
        out.print(4);
        out.print(2);
        assertTrue(out.lines.isEmpty());
        out.flush();
        assertEquals(Collections.singletonList("42"), out.lines);
    }
    public void testPrintlnInt() {
        out.println(4);
        out.println(2);
        assertEquals(Arrays.asList("4", "2"), out.lines);
    }
    public void testPrintCharArray() {
        out.print("Foo\nBar\nTee".toCharArray());
        assertEquals(Arrays.asList("Foo", "Bar"), out.lines);
        out.flush();
        assertEquals(Arrays.asList("Foo", "Bar", "Tee"), out.lines);
    }
    public void testPrintString() {
        out.print("Foo\nBar\nTee");
        assertEquals(Arrays.asList("Foo", "Bar"), out.lines);
        out.flush();
        assertEquals(Arrays.asList("Foo", "Bar", "Tee"), out.lines);
    }
    public void testPrintlnCharArray() {
        out.println("Foo\nBar\nTee".toCharArray());
        assertEquals(Arrays.asList("Foo", "Bar", "Tee"), out.lines);
    }
    public void testPrintlnString() {
        out.println("Foo\nBar\nTee");
        assertEquals(Arrays.asList("Foo", "Bar", "Tee"), out.lines);
    }
    public void testPrintlnStringWithBufferedData() {
        out.print(5);
        out.println("Foo\nBar\nTee");
        assertEquals(Arrays.asList("5Foo", "Bar", "Tee"), out.lines);
    }
    public void testAppend() {
        out.append("Foo\n")
            .append('4')
            .append('\n')
            .append("Bar", 1, 2)
            .append('\n');
        assertEquals(Arrays.asList("Foo", "4", "a"), out.lines);
    }
    public void testMultiByteCharactersSpanningBuffers() throws Exception {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            builder.append("\u20AC"); 
        }
        String expected = builder.toString();
        out.write(expected.getBytes("UTF-8"));
        out.flush();
        assertEquals(Arrays.asList(expected), out.lines);
    }
    public void testWriteOneByteAtATimeMultibyteCharacters() throws Exception {
        String expected = " \u20AC  \u20AC   \u20AC    \u20AC     ";
        for (byte b : expected.getBytes()) {
            out.write(b);
        }
        out.flush();
        assertEquals(Arrays.asList(expected), out.lines);
    }
    public void testWriteByteArrayAtATimeMultibyteCharacters() throws Exception {
        String expected = " \u20AC  \u20AC   \u20AC    \u20AC     ";
        out.write(expected.getBytes());
        out.flush();
        assertEquals(Arrays.asList(expected), out.lines);
    }
    public void testWriteWithOffsetsMultibyteCharacters() throws Exception {
        String expected = " \u20AC  \u20AC   \u20AC    \u20AC     ";
        byte[] bytes = expected.getBytes();
        int i = 0;
        while (i < bytes.length - 5) {
            out.write(bytes, i, 5);
            i += 5;
        }
        out.write(bytes, i, bytes.length - i);
        out.flush();
        assertEquals(Arrays.asList(expected), out.lines);
    }
    public void testWriteFlushesOnNewlines() throws Exception {
        String a = " \u20AC  \u20AC ";
        String b = "  \u20AC    \u20AC  ";
        String c = "   ";
        String toWrite = a + "\n" + b + "\n" + c;
        out.write(toWrite.getBytes());
        out.flush();
        assertEquals(Arrays.asList(a, b, c), out.lines);
    }
    static class TestPrintStream extends LoggingPrintStream {
        final List<String> lines = new ArrayList<String>();
        protected void log(String line) {
            lines.add(line);
        }
    }
}
