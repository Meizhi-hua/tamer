public class ConfigTest extends AndroidTestCase {
    enum Properties {
        LANGUAGE,
        COUNTRY,
        MCC,
        MNC,
        TOUCHSCREEN,
        KEYBOARD,
        KEYBOARDHIDDEN,
        NAVIGATION,
        ORIENTATION,
        WIDTH,
        HEIGHT,
        DENSITY
    }
    private static void checkValue(final Resources res, final int resId,
            final String expectedValue) {
        try {
            final String actual = res.getString(resId);
            assertNotNull("Returned wrong configuration-based simple value: expected <nothing>, "
                    + "got '" + actual + "' from resource 0x" + Integer.toHexString(resId),
                    expectedValue);
            assertEquals("Returned wrong configuration-based simple value: expected '"
                    + expectedValue + "', got '" + actual + "' from resource 0x"
                    + Integer.toHexString(resId), expectedValue, actual);
        } catch (NotFoundException e) {
            assertNull("Resource not found for configuration-based simple value: expecting \""
                    + expectedValue + "\"", expectedValue);
        }
    }
    private static void checkValue(final Resources res, final int resId,
            final int[] styleable, final String[] expectedValues) {
        final Resources.Theme theme = res.newTheme();
        final TypedArray sa = theme.obtainStyledAttributes(resId, styleable);
        for (int i = 0; i < styleable.length; i++) {
            final String actual = sa.getString(i);
            assertEquals("Returned wrong configuration-based style value: expected '"
                    + expectedValues[i] + "', got '" + actual + "' from attr "
                    + i + " of resource 0x" + Integer.toHexString(resId),
                    actual, expectedValues[i]);
        }
        sa.recycle();
    }
    private class TotalConfig {
        private Configuration mConfig;
        private DisplayMetrics mMetrics;
        public TotalConfig() {
            mConfig = new Configuration();
            mConfig.locale = new Locale("en", "US");
            mConfig.mcc = 310;
            mConfig.mnc = 001; 
            mConfig.touchscreen = Configuration.TOUCHSCREEN_FINGER;
            mConfig.keyboard = Configuration.KEYBOARD_QWERTY;
            mConfig.keyboardHidden = Configuration.KEYBOARDHIDDEN_YES;
            mConfig.navigation = Configuration.NAVIGATION_TRACKBALL;
            mConfig.orientation = Configuration.ORIENTATION_PORTRAIT;
            mMetrics = new DisplayMetrics();
            mMetrics.widthPixels = 200;
            mMetrics.heightPixels = 320;
            mMetrics.density = 1;
        }
        public void setProperty(final Properties p, final int value) {
            switch(p) {
                case MCC:
                    mConfig.mcc = value;
                    break;
                case MNC:
                    mConfig.mnc = value;
                    break;
                case TOUCHSCREEN:
                    mConfig.touchscreen = value;
                    break;
                case KEYBOARD:
                    mConfig.keyboard = value;
                    break;
                case KEYBOARDHIDDEN:
                    mConfig.keyboardHidden = value;
                    break;
                case NAVIGATION:
                    mConfig.navigation = value;
                    break;
                case ORIENTATION:
                    mConfig.orientation = value;
                    break;
                case WIDTH:
                    mMetrics.widthPixels = value;
                    break;
                case HEIGHT:
                    mMetrics.heightPixels = value;
                    break;
                case DENSITY:
                    mMetrics.density = (((float)value)/((float)DisplayMetrics.DENSITY_DEFAULT));
                    break;
                default:
                    assert(false);
                    break;
            }
        }
        public void setProperty(final Properties p, final String value) {
            switch(p) {
                case LANGUAGE:
                    final String oldCountry = mConfig.locale.getCountry();
                    mConfig.locale = new Locale(value, oldCountry);
                    break;
                case COUNTRY:
                    final String oldLanguage = mConfig.locale.getLanguage();
                    mConfig.locale = new Locale(oldLanguage, value);
                    break;
                default:
                    assert(false);
                    break;
            }
        }
        public Resources getResources() {
            final AssetManager assmgr = new AssetManager();
            assmgr.addAssetPath(mContext.getPackageResourcePath());
            return new Resources(assmgr, mMetrics, mConfig);
        }
    }
    private static void checkPair(Resources res, int[] notResIds,
            int simpleRes, String simpleString,
            int bagRes, String bagString) {
        boolean willHave = true;
        if (notResIds != null) {
            for (int i : notResIds) {
                if (i == simpleRes) {
                    willHave = false;
                    break;
                }
            }
        }
        checkValue(res, simpleRes, willHave ? simpleString : null);
        checkValue(res, bagRes, R.styleable.TestConfig,
                new String[]{willHave ? bagString : null});
    }
    @SmallTest
    public void testAllConfigs() {
        TotalConfig config = new TotalConfig();
        Resources res = config.getResources();
        checkValue(res, R.configVarying.simple, "simple default");
        checkValue(res, R.configVarying.bag,
                R.styleable.TestConfig, new String[]{"bag default"});
        config = new TotalConfig();
        config.setProperty(Properties.LANGUAGE, "xx");
        res = config.getResources();
        checkValue(res, R.configVarying.simple, "simple xx");
        checkValue(res, R.configVarying.bag,
                R.styleable.TestConfig, new String[]{"bag xx"});
        config = new TotalConfig();
        config.setProperty(Properties.LANGUAGE, "xx");
        config.setProperty(Properties.COUNTRY, "YY");
        res = config.getResources();
        checkValue(res, R.configVarying.simple, "simple xx-rYY");
        checkValue(res, R.configVarying.bag,
                R.styleable.TestConfig, new String[]{"bag xx-rYY"});
        config = new TotalConfig();
        config.setProperty(Properties.MCC, 111);
        res = config.getResources();
        checkValue(res, R.configVarying.simple, "simple mcc111");
        checkValue(res, R.configVarying.bag,
                R.styleable.TestConfig, new String[]{"bag mcc111"});
        config = new TotalConfig();
        config.setProperty(Properties.MNC, 222);
        res = config.getResources();
        checkValue(res, R.configVarying.simple, "simple mnc222");
        checkValue(res, R.configVarying.bag,
                R.styleable.TestConfig, new String[]{"bag mnc222"});
        config = new TotalConfig();
        config.setProperty(Properties.TOUCHSCREEN, Configuration.TOUCHSCREEN_NOTOUCH);
        res = config.getResources();
        checkValue(res, R.configVarying.simple, "simple notouch");
        checkValue(res, R.configVarying.bag,
                R.styleable.TestConfig, new String[]{"bag notouch"});
        config = new TotalConfig();
        config.setProperty(Properties.TOUCHSCREEN, Configuration.TOUCHSCREEN_STYLUS);
        res = config.getResources();
        checkValue(res, R.configVarying.simple, "simple stylus");
        checkValue(res, R.configVarying.bag,
                R.styleable.TestConfig, new String[]{"bag stylus"});
        config = new TotalConfig();
        config.setProperty(Properties.KEYBOARD, Configuration.KEYBOARD_NOKEYS);
        res = config.getResources();
        checkValue(res, R.configVarying.simple, "simple nokeys");
        checkValue(res, R.configVarying.bag,
                R.styleable.TestConfig, new String[]{"bag nokeys"});
        config = new TotalConfig();
        config.setProperty(Properties.KEYBOARD, Configuration.KEYBOARD_12KEY);
        res = config.getResources();
        checkValue(res, R.configVarying.simple, "simple 12key");
        checkValue(res, R.configVarying.bag,
                R.styleable.TestConfig, new String[]{"bag 12key"});
        config = new TotalConfig();
        config.setProperty(Properties.KEYBOARDHIDDEN, Configuration.KEYBOARDHIDDEN_NO);
        res = config.getResources();
        checkValue(res, R.configVarying.simple, "simple keysexposed");
        checkValue(res, R.configVarying.bag,
                R.styleable.TestConfig, new String[]{"bag keysexposed"});
        config = new TotalConfig();
        config.setProperty(Properties.NAVIGATION, Configuration.NAVIGATION_NONAV);
        res = config.getResources();
        checkValue(res, R.configVarying.simple, "simple nonav");
        checkValue(res, R.configVarying.bag,
                R.styleable.TestConfig, new String[]{"bag nonav"});
        config = new TotalConfig();
        config.setProperty(Properties.NAVIGATION, Configuration.NAVIGATION_DPAD);
        res = config.getResources();
        checkValue(res, R.configVarying.simple, "simple dpad");
        checkValue(res, R.configVarying.bag,
                R.styleable.TestConfig, new String[]{"bag dpad"});
        config = new TotalConfig();
        config.setProperty(Properties.NAVIGATION, Configuration.NAVIGATION_WHEEL);
        res = config.getResources();
        checkValue(res, R.configVarying.simple, "simple wheel");
        checkValue(res, R.configVarying.bag,
                R.styleable.TestConfig, new String[]{"bag wheel"});
        config = new TotalConfig();
        config.setProperty(Properties.HEIGHT, 480);
        config.setProperty(Properties.WIDTH, 320);
        res = config.getResources();
        checkValue(res, R.configVarying.simple, "simple 480x320");
        checkValue(res, R.configVarying.bag,
                R.styleable.TestConfig, new String[]{"bag 480x320"});
        config = new TotalConfig();
        config.setProperty(Properties.DENSITY, 240);
        res = config.getResources();
        checkValue(res, R.configVarying.simple, "simple 240dpi");
        checkValue(res, R.configVarying.bag,
                R.styleable.TestConfig, new String[]{"bag 240dpi"});
        config = new TotalConfig();
        config.setProperty(Properties.ORIENTATION, Configuration.ORIENTATION_LANDSCAPE);
        res = config.getResources();
        checkValue(res, R.configVarying.simple, "simple landscape");
        checkValue(res, R.configVarying.bag,
                R.styleable.TestConfig, new String[]{"bag landscape"});
        config = new TotalConfig();
        config.setProperty(Properties.ORIENTATION, Configuration.ORIENTATION_SQUARE);
        res = config.getResources();
        checkValue(res, R.configVarying.simple, "simple square");
        checkValue(res, R.configVarying.bag,
                R.styleable.TestConfig, new String[]{"bag square"});
    }
    @MediumTest
    public void testDensity() throws Exception {
        TotalConfig config = new TotalConfig();
        config.setProperty(Properties.DENSITY, 2);
        Resources res = config.getResources();
        checkValue(res, R.configVarying.simple, "simple 32dpi");
        checkValue(res, R.configVarying.bag,
                R.styleable.TestConfig, new String[]{"bag 32dpi"});
        config = new TotalConfig();
        config.setProperty(Properties.DENSITY, 32);
        res = config.getResources();
        checkValue(res, R.configVarying.simple, "simple 32dpi");
        checkValue(res, R.configVarying.bag,
                R.styleable.TestConfig, new String[]{"bag 32dpi"});
        config = new TotalConfig();
        config.setProperty(Properties.DENSITY, 48);
        res = config.getResources();
        checkValue(res, R.configVarying.simple, "simple 32dpi");
        checkValue(res, R.configVarying.bag,
                R.styleable.TestConfig, new String[]{"bag 32dpi"});
        config = new TotalConfig();
        config.setProperty(Properties.DENSITY, 49);
        res = config.getResources();
        checkValue(res, R.configVarying.simple, "simple default");
        checkValue(res, R.configVarying.bag,
                R.styleable.TestConfig, new String[]{"bag default"});
        config = new TotalConfig();
        config.setProperty(Properties.DENSITY, 150);
        res = config.getResources();
        checkValue(res, R.configVarying.simple, "simple default");
        checkValue(res, R.configVarying.bag,
                R.styleable.TestConfig, new String[]{"bag default"});
        config = new TotalConfig();
        config.setProperty(Properties.DENSITY, 181);
        res = config.getResources();
        checkValue(res, R.configVarying.simple, "simple default");
        checkValue(res, R.configVarying.bag,
                R.styleable.TestConfig, new String[]{"bag default"});
        config = new TotalConfig();
        config.setProperty(Properties.DENSITY, 182);
        res = config.getResources();
        checkValue(res, R.configVarying.simple, "simple 240dpi");
        checkValue(res, R.configVarying.bag,
                R.styleable.TestConfig, new String[]{"bag 240dpi"});
        config = new TotalConfig();
        config.setProperty(Properties.DENSITY, 239);
        res = config.getResources();
        checkValue(res, R.configVarying.simple, "simple 240dpi");
        checkValue(res, R.configVarying.bag,
                R.styleable.TestConfig, new String[]{"bag 240dpi"});
        config = new TotalConfig();
        config.setProperty(Properties.DENSITY, 490);
        res = config.getResources();
        checkValue(res, R.configVarying.simple, "simple 240dpi");
        checkValue(res, R.configVarying.bag,
                R.styleable.TestConfig, new String[]{"bag 240dpi"});
    }
    @MediumTest
    public void testCombinations() {
        TotalConfig config = new TotalConfig();
        config.setProperty(Properties.LANGUAGE, "xx");
        config.setProperty(Properties.COUNTRY, "YY");
        config.setProperty(Properties.MCC, 111);
        Resources res = config.getResources();
        checkValue(res, R.configVarying.simple, "simple mcc111 xx-rYY");
        checkValue(res, R.configVarying.bag, R.styleable.TestConfig,
                new String[] { "bag mcc111 xx-rYY" });
        config = new TotalConfig();
        config.setProperty(Properties.LANGUAGE, "xx");
        config.setProperty(Properties.COUNTRY, "YY");
        config.setProperty(Properties.MCC, 333);
        res = config.getResources();
        checkValue(res, R.configVarying.simple, "simple xx-rYY");
        checkValue(res, R.configVarying.bag,
                R.styleable.TestConfig, new String[] { "bag xx-rYY" });
        config = new TotalConfig();
        config.setProperty(Properties.MNC, 333);
        res = config.getResources();
        checkValue(res, R.configVarying.simple, "simple default");
        checkValue(res, R.configVarying.bag,
                R.styleable.TestConfig, new String[]{"bag default"});
    }
    @MediumTest
    public void testPrecidence() {
        TotalConfig config = new TotalConfig();
        config.setProperty(Properties.MCC, 110);
        config.setProperty(Properties.MNC, 220);
        config.setProperty(Properties.LANGUAGE, "xx");
        Resources res = config.getResources();
        checkValue(res, R.configVarying.simple, "simple mcc110 xx");
        checkValue(res, R.configVarying.bag,
                R.styleable.TestConfig, new String[]{"bag mcc110 xx"});
        config = new TotalConfig();
        config.setProperty(Properties.MCC, 111);
        config.setProperty(Properties.MNC, 222);
        config.setProperty(Properties.LANGUAGE, "xx");
        res = config.getResources();
        checkValue(res, R.configVarying.simple, "simple mcc111 mnc222");
        checkValue(res, R.configVarying.bag,
                R.styleable.TestConfig, new String[]{"bag mcc111 mnc222"});
        config = new TotalConfig();
        config.setProperty(Properties.MNC, 222);
        config.setProperty(Properties.LANGUAGE, "xx");
        config.setProperty(Properties.ORIENTATION, 
                Configuration.ORIENTATION_SQUARE);
        res = config.getResources();
        checkValue(res, R.configVarying.simple, "simple mnc222 xx");
        checkValue(res, R.configVarying.bag,
                R.styleable.TestConfig, new String[]{"bag mnc222 xx"});
        config = new TotalConfig();
        config.setProperty(Properties.LANGUAGE, "xx");
        config.setProperty(Properties.ORIENTATION, 
                Configuration.ORIENTATION_SQUARE);
        config.setProperty(Properties.DENSITY, 32);
        res = config.getResources();
        checkValue(res, R.configVarying.simple, "simple xx square");
        checkValue(res, R.configVarying.bag,
                R.styleable.TestConfig, new String[]{"bag xx square"});
        config = new TotalConfig();
        config.setProperty(Properties.ORIENTATION, 
                Configuration.ORIENTATION_SQUARE);
        config.setProperty(Properties.DENSITY, 32);
        config.setProperty(Properties.TOUCHSCREEN, 
                Configuration.TOUCHSCREEN_STYLUS);
        res = config.getResources();
        checkValue(res, R.configVarying.simple, "simple square 32dpi");
        checkValue(res, R.configVarying.bag,
                R.styleable.TestConfig, new String[]{"bag square 32dpi"});
        config = new TotalConfig();
        config.setProperty(Properties.DENSITY, 32);
        config.setProperty(Properties.TOUCHSCREEN, 
                Configuration.TOUCHSCREEN_STYLUS);
        config.setProperty(Properties.KEYBOARDHIDDEN, 
                Configuration.KEYBOARDHIDDEN_NO);
        res = config.getResources();
        checkValue(res, R.configVarying.simple, "simple 32dpi stylus");
        checkValue(res, R.configVarying.bag,
                R.styleable.TestConfig, new String[]{"bag 32dpi stylus"});
        config = new TotalConfig();
        config.setProperty(Properties.TOUCHSCREEN, 
                Configuration.TOUCHSCREEN_STYLUS);
        config.setProperty(Properties.KEYBOARDHIDDEN, 
                Configuration.KEYBOARDHIDDEN_NO);
        config.setProperty(Properties.KEYBOARD, Configuration.KEYBOARD_12KEY);
        res = config.getResources();
        checkValue(res, R.configVarying.simple, "simple stylus keysexposed");
        checkValue(res, R.configVarying.bag,
                R.styleable.TestConfig, new String[]{"bag stylus keysexposed"});
        config = new TotalConfig();
        config.setProperty(Properties.KEYBOARDHIDDEN, 
                Configuration.KEYBOARDHIDDEN_NO);
        config.setProperty(Properties.KEYBOARD, Configuration.KEYBOARD_12KEY);
        config.setProperty(Properties.NAVIGATION, 
                Configuration.NAVIGATION_DPAD);
        res = config.getResources();
        checkValue(res, R.configVarying.simple, "simple keysexposed 12key");
        checkValue(res, R.configVarying.bag,
                R.styleable.TestConfig, new String[]{"bag keysexposed 12key"});
        config = new TotalConfig();
        config.setProperty(Properties.KEYBOARD, Configuration.KEYBOARD_12KEY);
        config.setProperty(Properties.NAVIGATION, 
                Configuration.NAVIGATION_DPAD);
        config.setProperty(Properties.HEIGHT, 63);
        config.setProperty(Properties.WIDTH, 57);
        res = config.getResources();
        checkValue(res, R.configVarying.simple, "simple 12key dpad");
        checkValue(res, R.configVarying.bag,
                R.styleable.TestConfig, new String[]{"bag 12key dpad"});
        config = new TotalConfig();
        config.setProperty(Properties.NAVIGATION, 
                Configuration.NAVIGATION_DPAD);
        config.setProperty(Properties.HEIGHT, 640);
        config.setProperty(Properties.WIDTH, 400);
        res = config.getResources();
        checkValue(res, R.configVarying.simple, "simple dpad");
        checkValue(res, R.configVarying.bag,
                R.styleable.TestConfig, new String[]{"bag dpad"});
    }
    @MediumTest
    public void testVersions() {
        int vers = android.os.Build.VERSION.SDK_INT;
        if (!"REL".equals(android.os.Build.VERSION.CODENAME)) {
            vers++;
        }
        String expected = "v" + vers + "cur";
        assertEquals(expected, mContext.getResources().getString(R.string.version_cur));
        assertEquals("base",  mContext.getResources().getString(R.string.version_old));
        assertEquals("v3",  mContext.getResources().getString(R.string.version_v3));
    }
}
