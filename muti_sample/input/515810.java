@TestTargetClass(AndroidCharacter.class)
public class AndroidCharacterTest extends AndroidTestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test constructor",
        method = "AndroidCharacter",
        args = {}
    )
    public void testConstructor() {
        new AndroidCharacter();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test getDirectionalities(char[] src, byte[] dest, int count)",
        method = "getDirectionalities",
        args = {char[].class, byte[].class, int.class}
    )
    public void testGetDirectionalities() {
        char[] src = new char[128];
        for (int i = 0; i < src.length; i++) {
            src[i] = (char) i;
        }
        byte[] dest = new byte[128];
        int count = 128;
        AndroidCharacter.getDirectionalities(src, dest, count);
        byte[] expected = {9, 9, 9, 9, 9, 9, 9, 9, 9, 11, 10, 11, 12, 10, 9,
                9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 10, 10, 10, 11, 12, 13,
                13, 5, 5, 5, 13, 13, 13, 13, 13, 4, 7, 4, 7, 7, 3, 3, 3, 3, 3,
                3, 3, 3, 3, 3, 7, 13, 13, 13, 13, 13, 13, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 13,
                13, 13, 13, 13, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 13, 13, 13, 9};
        for (int i = 0; i < dest.length; i++) {
            assertEquals(expected[i], dest[i]);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test getEastAsianWidth(char input)",
        method = "getEastAsianWidth",
        args = {char.class}
    )
    public void testGetEastAsianWidth() {
        assertEquals(AndroidCharacter.EAST_ASIAN_WIDTH_NEUTRAL,
                AndroidCharacter.getEastAsianWidth((char)0x01D3));
        assertEquals(AndroidCharacter.EAST_ASIAN_WIDTH_AMBIGUOUS,
                AndroidCharacter.getEastAsianWidth((char)0xFFFD));
        assertEquals(AndroidCharacter.EAST_ASIAN_WIDTH_HALF_WIDTH,
                AndroidCharacter.getEastAsianWidth((char)0xFF86));
        assertEquals(AndroidCharacter.EAST_ASIAN_WIDTH_FULL_WIDTH,
                AndroidCharacter.getEastAsianWidth((char)0xFF41));
        assertEquals(AndroidCharacter.EAST_ASIAN_WIDTH_NARROW,
                AndroidCharacter.getEastAsianWidth((char)0x0041));
        assertEquals(AndroidCharacter.EAST_ASIAN_WIDTH_WIDE,
                AndroidCharacter.getEastAsianWidth((char)0x319F));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test getEastAsianWidths(char[] src, byte[] dest, int count)",
        method = "getEastAsianWidths",
        args = {char[].class, byte[].class, int.class}
    )
    public void testGetEastAsianWidths() {
        char[] src = {
                0x01D3, 0xFFFD, 0xFF86, 0xFF41, 0x0041, 0x319f,
                0x319F, 0x0041, 0xFF41, 0xFF86, 0xFFFD, 0x01D3,
        };
        int start = 2;
        int count = 8;
        byte[] dest = new byte[count];
        AndroidCharacter.getEastAsianWidths(src, start, count, dest);
        byte[] expected = {2, 3, 4, 5, 5, 4, 3, 2};
        for (int i = 0; i < dest.length; i++) {
            assertEquals(expected[i], dest[i]);
        }
        try {
            AndroidCharacter.getEastAsianWidths(src, 24, 8, dest);
            fail("Should throw ArrayIndexOutOfBoundsException.");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        try {
            AndroidCharacter.getEastAsianWidths(src, -1024, 1, dest);
            fail("Should throw ArrayIndexOutOfBoundsException.");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        try {
            AndroidCharacter.getEastAsianWidths(src, 0, -1, dest);
            fail("Should throw ArrayIndexOutOfBoundsException.");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test getMirror(char ch)",
        method = "getMirror",
        args = {char.class}
    )
    public void testGetMirror() {
        assertEquals('A', AndroidCharacter.getMirror('A'));
        assertEquals('B', AndroidCharacter.getMirror('B'));
        assertEquals('(', AndroidCharacter.getMirror(')'));
        assertEquals('[', AndroidCharacter.getMirror(']'));
        assertEquals('{', AndroidCharacter.getMirror('}'));
        assertEquals('<', AndroidCharacter.getMirror('>'));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test mirror(char[] text, int start, int count)",
        method = "mirror",
        args = {char[].class, int.class, int.class}
    )
    public void testMirror() {
        char[] src = new char[64];
        for (int i = 0; i < src.length; i++) {
            src[i] = (char) i;
        }
        assertFalse(AndroidCharacter.mirror(src, 0, 0));
        assertTrue(AndroidCharacter.mirror(src, 40, 24));
        try {
            AndroidCharacter.mirror(src, 65, 1);
            fail("Should throw ArrayIndexOutOfBoundsException.");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        try {
            AndroidCharacter.mirror(src, 60, 10);
            fail("Should throw ArrayIndexOutOfBoundsException.");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        try {
            AndroidCharacter.mirror(src, -1024, 1);
            fail("Should throw ArrayIndexOutOfBoundsException.");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        try {
            AndroidCharacter.mirror(src, 0, -1);
            fail("Should throw ArrayIndexOutOfBoundsException.");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        String str = new String("if(a>b)");
        char[] strChar = str.toCharArray();
        assertTrue(AndroidCharacter.mirror(strChar, 0, str.length()));
        assertEquals("if)a<b(", new String(strChar));
        assertFalse(AndroidCharacter.mirror(str.toCharArray(), 0, 2));
    }
}
