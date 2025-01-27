public class BigCache extends Activity {
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        final LinearLayout testBed = new LinearLayout(this);
        testBed.setOrientation(LinearLayout.VERTICAL);
        testBed.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        final int cacheSize = ViewConfiguration.getMaximumDrawingCacheSize();
        final Display display = getWindowManager().getDefaultDisplay();
        final int screenWidth = display.getWidth();
        final int screenHeight = display.getHeight();
        final View tiny = new View(this);
        tiny.setId(R.id.a);
        tiny.setBackgroundColor(0xFFFF0000);
        tiny.setLayoutParams(new LinearLayout.LayoutParams(screenWidth, screenHeight));
        final View large = new View(this);
        large.setId(R.id.b);
        large.setBackgroundColor(0xFF00FF00);
        final int height = 2 * (cacheSize / 2) / screenWidth;
        large.setLayoutParams(new LinearLayout.LayoutParams(screenWidth, height));
        final ScrollView scroller = new ScrollView(this);
        scroller.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        testBed.addView(tiny);
        testBed.addView(large);
        scroller.addView(testBed);
        setContentView(scroller);
    }
}
