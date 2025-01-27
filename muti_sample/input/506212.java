public class BooleanTest extends TestCase {
    @SmallTest
    public void testBoolean() throws Exception {
        Boolean a = new Boolean(true);
        Boolean b = new Boolean("True");
        Boolean c = new Boolean(false);
        Boolean d = new Boolean("Yes");
        assertEquals(a, b);
        assertEquals(c, d);
        assertTrue(a.booleanValue());
        assertFalse(c.booleanValue());
        assertEquals("true", a.toString());
        assertEquals("false", c.toString());
        assertEquals(Boolean.TRUE, a);
        assertEquals(Boolean.FALSE, c);
        assertSame(Boolean.valueOf(true), Boolean.TRUE);
        assertSame(Boolean.valueOf(false), Boolean.FALSE);
    }
}
