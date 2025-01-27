@TestTargetClass(TextPaint.class)
public class TextPaintTest extends AndroidTestCase {
    private static final int DEFAULT_PAINT_FLAGS = TextPaint.DEV_KERN_TEXT_FLAG;
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "TextPaint",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "TextPaint",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "TextPaint",
            args = {android.graphics.Paint.class}
        )
    })
    @ToBeFixed(bug="1417734", explanation="should add @throws clause for" +
            " TextPaint#TextPaint(Paint) when the input Paint is null")
    public void testConstructor() {
        TextPaint textPaint;
        textPaint = new TextPaint();
        assertEquals(DEFAULT_PAINT_FLAGS, textPaint.getFlags());
        textPaint = new TextPaint(TextPaint.DITHER_FLAG);
        assertEquals((TextPaint.DITHER_FLAG | DEFAULT_PAINT_FLAGS),
                textPaint.getFlags());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "set",
        args = {android.text.TextPaint.class}
    )
    @ToBeFixed(bug="1417734", explanation="should add @throws clause for" +
            " TextPaint#set(TextPaint) when the input TextPaint is null")
    public void testSet() {
        TextPaint textPaintSrc = new TextPaint(TextPaint.DITHER_FLAG);
        int[] drawableState = new int[] { 0, 1 };
        textPaintSrc.bgColor = Color.GREEN;
        textPaintSrc.baselineShift = 10;
        textPaintSrc.linkColor = Color.BLUE;
        textPaintSrc.drawableState = drawableState;
        textPaintSrc.setTypeface(Typeface.DEFAULT_BOLD);
        TextPaint textPaint = new TextPaint();
        assertEquals(0, textPaint.bgColor);
        assertEquals(0, textPaint.baselineShift);
        assertEquals(0, textPaint.linkColor);
        assertNull(textPaint.drawableState);
        assertNull(textPaint.getTypeface());
        textPaint.set(textPaintSrc);
        assertEquals(textPaintSrc.bgColor, textPaint.bgColor);
        assertEquals(textPaintSrc.baselineShift, textPaint.baselineShift);
        assertEquals(textPaintSrc.linkColor, textPaint.linkColor);
        assertSame(textPaintSrc.drawableState, textPaint.drawableState);
        assertEquals(textPaintSrc.getTypeface(), textPaint.getTypeface());
        assertEquals(textPaintSrc.getFlags(), textPaint.getFlags());
        try {
            textPaint.set(null);
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
        }
    }
}
