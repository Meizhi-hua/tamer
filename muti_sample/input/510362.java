@TestTargetClass(ColorMatrixColorFilter.class)
public class ColorMatrixColorFilterTest extends TestCase {
    private static final int TOLERANCE = 1;
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "ColorMatrixColorFilter",
            args = {ColorMatrix.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "ColorMatrixColorFilter",
            args = {float[].class}
        )
    })
    public void testColorMatrixColorFilter() {
        ColorMatrixColorFilter filter;
        ColorMatrix cm = new ColorMatrix();
        float[] blueToCyan = new float[] {
                1f, 0f, 0f, 0f, 0f,
                0f, 1f, 1f, 0f, 0f,
                0f, 0f, 1f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f };
        cm.set(blueToCyan);
        filter = new ColorMatrixColorFilter(cm);
        Bitmap bitmap = Bitmap.createBitmap(1, 1, Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setColorFilter(filter);
        canvas.drawPoint(0, 0, paint);
        assertColor(Color.CYAN, bitmap.getPixel(0, 0));
        paint.setColor(Color.GREEN);
        canvas.drawPoint(0, 0, paint);
        assertColor(Color.GREEN, bitmap.getPixel(0, 0));
        paint.setColor(Color.RED);
        canvas.drawPoint(0, 0, paint);
        assertColor(Color.RED, bitmap.getPixel(0, 0));
        paint.setColor(Color.MAGENTA);
        canvas.drawPoint(0, 0, paint);
        assertColor(Color.WHITE, bitmap.getPixel(0, 0));
        float[] transparentRedAddBlue = new float[] {
                1f, 0f, 0f, 0f, 0f,
                0f, 1f, 0f, 0f, 0f,
                0f, 0f, 1f, 0f, 64f,
                -0.5f, 0f, 0f, 1f, 0f
        };
        filter = new ColorMatrixColorFilter(transparentRedAddBlue);
        paint.setColorFilter(filter);
        paint.setColor(Color.RED);
        bitmap.eraseColor(Color.TRANSPARENT);
        canvas.drawPoint(0, 0, paint);
        assertColor(Color.argb(128, 255, 0, 64), bitmap.getPixel(0, 0));
        paint.setColor(Color.CYAN);
        canvas.drawPoint(0, 0, paint);
        assertColor(Color.CYAN, bitmap.getPixel(0, 0));
        assertEquals(1f, transparentRedAddBlue[6]);
        transparentRedAddBlue[6] = 0f;
        canvas.drawPoint(0, 0, paint);
        assertColor(Color.CYAN, bitmap.getPixel(0, 0));
        paint.setColorFilter(new ColorMatrixColorFilter(transparentRedAddBlue));
        canvas.drawPoint(0, 0, paint);
        assertColor(Color.BLUE, bitmap.getPixel(0, 0));
    }
    private void assertColor(int expected, int actual) {
        assertEquals(Color.red(expected), Color.red(actual), TOLERANCE);
        assertEquals(Color.green(expected), Color.green(actual), TOLERANCE);
        assertEquals(Color.blue(expected), Color.blue(actual), TOLERANCE);
        assertEquals(Color.alpha(expected), Color.alpha(actual), TOLERANCE);
    }
}
