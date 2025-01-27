public class TextViewTest extends AndroidTestCase {
    @SmallTest
    public void testArray() throws Exception {
        TextView tv = new TextView(mContext);
        char[] c = new char[] { 'H', 'e', 'l', 'l', 'o', ' ',
                                'W', 'o', 'r', 'l', 'd', '!' };
        tv.setText(c, 1, 4);
        CharSequence oldText = tv.getText();
        tv.setText(c, 4, 5);
        CharSequence newText = tv.getText();
        assertTrue(newText == oldText);
        assertEquals(5, newText.length());
        assertEquals('o', newText.charAt(0));
        assertEquals("o Wor", newText.toString());
        assertEquals(" Wo", newText.subSequence(1, 4));
        char[] c2 = new char[7];
        ((GetChars) newText).getChars(1, 4, c2, 2);
        assertEquals('\0', c2[1]);
        assertEquals(' ', c2[2]);
        assertEquals('W', c2[3]);
        assertEquals('o', c2[4]);
        assertEquals('\0', c2[5]);
    }
}
