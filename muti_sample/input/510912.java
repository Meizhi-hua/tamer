@TestTargetClass(ComponentInfo.class)
public class ComponentInfoTest extends AndroidTestCase {
    private final String PACKAGE_NAME = "com.android.cts.stub";
    private ComponentInfo mComponentInfo;
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "ComponentInfo",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "ComponentInfo",
            args = {android.content.pm.ComponentInfo.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "ComponentInfo",
            args = {android.os.Parcel.class}
        )
    })
    @ToBeFixed(bug = "1695243", explanation = "ComponentInfo#ComponentInfo(ComponentInfo), " +
            "ComponentInfo#ComponentInfo(Parcel), should check whether the input is null")
    public void testConstructor() {
        Parcel p = Parcel.obtain();
        ComponentInfo componentInfo = new ComponentInfo();
        componentInfo.applicationInfo = new ApplicationInfo();
        componentInfo.writeToParcel(p, 0);
        p.setDataPosition(0);
        new MyComponentInfo(p);
        new ComponentInfo();
        new ComponentInfo(componentInfo);
        try {
            new ComponentInfo((ComponentInfo) null);
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
        }
        try {
            new MyComponentInfo((Parcel) null);
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "loadIcon",
        args = {android.content.pm.PackageManager.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "NullPointerException is not expected.")
    public void testLoadIcon() {
        mComponentInfo = new ComponentInfo();
        mComponentInfo.applicationInfo = new ApplicationInfo();
        PackageManager pm = mContext.getPackageManager();
        assertNotNull(pm);
        Drawable defaultIcon = pm.getDefaultActivityIcon();
        Drawable d = null;
        Drawable d2 = null;
        d = mComponentInfo.loadIcon(pm);
        assertNotNull(d);
        assertNotSame(d, defaultIcon);
        WidgetTestUtils.assertEquals(((BitmapDrawable) d).getBitmap(),
                ((BitmapDrawable) defaultIcon).getBitmap());
        d2 = mComponentInfo.loadIcon(pm);
        assertNotNull(d2);
        assertNotSame(d, d2);
        WidgetTestUtils.assertEquals(((BitmapDrawable) d).getBitmap(),
                ((BitmapDrawable) d2).getBitmap());
        try {
            mComponentInfo.loadIcon(null);
            fail("ComponentInfo#loadIcon() throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "dumpBack",
        args = {android.util.Printer.class, java.lang.String.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "NullPointerException is not expected.")
    public void testDumpBack() {
        MyComponentInfo ci = new MyComponentInfo();
        StringBuilder sb = new StringBuilder();
        assertEquals(0, sb.length());
        StringBuilderPrinter p = new StringBuilderPrinter(sb);
        String prefix = "";
        ci.dumpBack(p, prefix);
        assertNotNull(sb.toString());
        assertTrue(sb.length() > 0);
        ci.applicationInfo = new ApplicationInfo();
        sb = new StringBuilder();
        assertEquals(0, sb.length());
        p = new StringBuilderPrinter(sb);
        ci.dumpBack(p, prefix);
        assertNotNull(sb.toString());
        assertTrue(sb.length() > 0);
        try {
            ci.dumpBack(null, null);
            fail("ComponentInfo#dumpBack() throw NullPointerException here.");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getIconResource",
        args = {}
    )
    public void testGetIconResource() {
        mComponentInfo = new ComponentInfo();
        mComponentInfo.applicationInfo = new ApplicationInfo();
        assertEquals(0, mComponentInfo.getIconResource());
        mComponentInfo.icon = R.drawable.red;
        assertEquals(mComponentInfo.icon, mComponentInfo.getIconResource());
        mComponentInfo.icon = 0;
        assertEquals(mComponentInfo.applicationInfo.icon, mComponentInfo.getIconResource());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "dumpFront",
        args = {android.util.Printer.class, java.lang.String.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "NullPointerException is not expected.")
    public void testDumpFront() {
        MyComponentInfo ci = new MyComponentInfo();
        StringBuilder sb = new StringBuilder();
        assertEquals(0, sb.length());
        StringBuilderPrinter p = new StringBuilderPrinter(sb);
        String prefix = "";
        ci.dumpFront(p, prefix);
        assertNotNull(sb.toString());
        assertTrue(sb.length() > 0);
        ci.applicationInfo = new ApplicationInfo();
        sb = new StringBuilder();
        p = new StringBuilderPrinter(sb);
        assertEquals(0, sb.length());
        ci.dumpFront(p, prefix);
        assertNotNull(sb.toString());
        assertTrue(sb.length() > 0);
        try {
            ci.dumpFront(null, null);
            fail("ComponentInfo#dumpFront() throw NullPointerException here.");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "loadLabel",
        args = {android.content.pm.PackageManager.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "NullPointerException is not expected.")
    public void testLoadLabel() throws NameNotFoundException {
        mComponentInfo = new ComponentInfo();
        mComponentInfo.applicationInfo = new ApplicationInfo();
        final PackageManager pm = mContext.getPackageManager();
        assertNotNull(mComponentInfo);
        mComponentInfo.packageName = PACKAGE_NAME;
        mComponentInfo.nonLocalizedLabel = "nonLocalizedLabel";
        assertEquals("nonLocalizedLabel", mComponentInfo.loadLabel(pm));
        mComponentInfo.nonLocalizedLabel = null;
        mComponentInfo.labelRes = 0;
        mComponentInfo.name = "name";
        assertEquals("name", mComponentInfo.loadLabel(pm));
        mComponentInfo.applicationInfo =
                mContext.getPackageManager().getApplicationInfo(PACKAGE_NAME, 0);
        mComponentInfo.nonLocalizedLabel = null;
        mComponentInfo.labelRes = R.string.hello_android;
        assertEquals(mContext.getString(mComponentInfo.labelRes), mComponentInfo.loadLabel(pm));
        try {
            mComponentInfo.loadLabel(null);
            fail("ComponentInfo#loadLabel throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "writeToParcel",
        args = {android.os.Parcel.class, int.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "NullPointerException is not expected.")
    public void testWriteToParcel() {
        Parcel p = Parcel.obtain();
        mComponentInfo = new ComponentInfo();
        mComponentInfo.applicationInfo = new ApplicationInfo();
        mComponentInfo.writeToParcel(p, 0);
        p.setDataPosition(0);
        MyComponentInfo ci = new MyComponentInfo(p);
        assertEquals(mComponentInfo.processName, ci.processName);
        assertEquals(mComponentInfo.enabled, ci.enabled);
        assertEquals(mComponentInfo.exported, ci.exported);
        StringBuilder sb1 = new StringBuilder();
        StringBuilderPrinter p1 = new StringBuilderPrinter(sb1);
        StringBuilder sb2 = new StringBuilder();
        StringBuilderPrinter p2 = new StringBuilderPrinter(sb2);
        mComponentInfo.applicationInfo.dump(p1, "");
        ci.applicationInfo.dump(p2, "");
        assertEquals(sb1.toString(), sb2.toString());
        try {
            mComponentInfo.writeToParcel(null, 0);
            fail("ComponentInfo#writeToParcel() throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }
    private static class MyComponentInfo extends ComponentInfo {
        public MyComponentInfo() {
            super();
        }
        public MyComponentInfo(ComponentInfo orig) {
            super(orig);
        }
        public MyComponentInfo(Parcel source) {
            super(source);
        }
        public void dumpBack(Printer pw, String prefix) {
            super.dumpBack(pw, prefix);
        }
        public void dumpFront(Printer pw, String prefix) {
            super.dumpFront(pw, prefix);
        }
    }
}
