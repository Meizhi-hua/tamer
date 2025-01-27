@TestTargetClass(DateUtils.class)
public class DateUtilsTest extends AndroidTestCase {
    private static final long MIN_DURATION = 1000;
    private static final long MINUTE_DURATION = 42 * 60 * 1000;
    private static final long HOUR_DURATION = 2 * 60 * 60 * 1000;
    private static final long DAY_DURATION = 5 * 24 * 60 * 60 * 1000;
    private long mBaseTime;
    private Locale mDefaultLocale;
    private Context mContext;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getContext();
        mBaseTime = System.currentTimeMillis();
        mDefaultLocale = Locale.getDefault();
        if (!mDefaultLocale.equals(Locale.US)) {
            Locale.setDefault(Locale.US);
        }
    }
    @Override
    protected void tearDown() throws Exception {
        if (!Locale.getDefault().equals(mDefaultLocale)) {
            Locale.setDefault(mDefaultLocale);
        }
        super.tearDown();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getDayOfWeekString",
        args = {int.class, int.class}
    )
    public void testGetDayOfWeekString() {
        assertEquals("Sunday",
                DateUtils.getDayOfWeekString(Calendar.SUNDAY, DateUtils.LENGTH_LONG));
        assertEquals("Sun",
                DateUtils.getDayOfWeekString(Calendar.SUNDAY, DateUtils.LENGTH_MEDIUM));
        assertEquals("Su",
                DateUtils.getDayOfWeekString(Calendar.SUNDAY, DateUtils.LENGTH_SHORT));
        assertEquals("Su",
                DateUtils.getDayOfWeekString(Calendar.SUNDAY, DateUtils.LENGTH_SHORTER));
        assertEquals("S",
                DateUtils.getDayOfWeekString(Calendar.SUNDAY, DateUtils.LENGTH_SHORTEST));
        assertEquals("Sun",
                DateUtils.getDayOfWeekString(Calendar.SUNDAY, 60));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getMonthString",
        args = {int.class, int.class}
    )
    public void testGetMonthString() {
        assertEquals("January", DateUtils.getMonthString(Calendar.JANUARY, DateUtils.LENGTH_LONG));
        assertEquals("Jan",
                DateUtils.getMonthString(Calendar.JANUARY, DateUtils.LENGTH_MEDIUM));
        assertEquals("Jan", DateUtils.getMonthString(Calendar.JANUARY, DateUtils.LENGTH_SHORT));
        assertEquals("Jan",
                DateUtils.getMonthString(Calendar.JANUARY, DateUtils.LENGTH_SHORTER));
        assertEquals("J",
                DateUtils.getMonthString(Calendar.JANUARY, DateUtils.LENGTH_SHORTEST));
        assertEquals("Jan", DateUtils.getMonthString(Calendar.JANUARY, 60));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getAMPMString",
        args = {int.class}
    )
    public void testGetAMPMString() {
        assertEquals("am", DateUtils.getAMPMString(Calendar.AM));
        assertEquals("pm", DateUtils.getAMPMString(Calendar.PM));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getRelativeTimeSpanString",
            args = {long.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getRelativeTimeSpanString",
            args = {long.class, long.class, long.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getRelativeTimeSpanString",
            args = {long.class, long.class, long.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getRelativeDateTimeString",
            args = {Context.class, long.class, long.class, long.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getRelativeTimeSpanString",
            args = {Context.class, long.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getRelativeTimeSpanString",
            args = {Context.class, long.class}
        )
    })
    public void testGetSpanString() {
        assertEquals("0 minutes ago",
                DateUtils.getRelativeTimeSpanString(mBaseTime - MIN_DURATION).toString());
        assertEquals("in 0 minutes",
                DateUtils.getRelativeTimeSpanString(mBaseTime + MIN_DURATION).toString());
        assertEquals("42 minutes ago", DateUtils.getRelativeTimeSpanString(
                mBaseTime - MINUTE_DURATION, mBaseTime, DateUtils.MINUTE_IN_MILLIS).toString());
        assertEquals("in 42 minutes", DateUtils.getRelativeTimeSpanString(
                mBaseTime + MINUTE_DURATION, mBaseTime, DateUtils.MINUTE_IN_MILLIS).toString());
        assertEquals("2 hours ago", DateUtils.getRelativeTimeSpanString(mBaseTime - HOUR_DURATION,
                mBaseTime, DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_NUMERIC_DATE).toString());
        assertEquals("in 2 hours", DateUtils.getRelativeTimeSpanString(mBaseTime + HOUR_DURATION,
                mBaseTime, DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_NUMERIC_DATE).toString());
        assertEquals("in 42 mins", DateUtils.getRelativeTimeSpanString(mBaseTime + MINUTE_DURATION,
                mBaseTime, DateUtils.MINUTE_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_RELATIVE).toString());
        assertNotNull(DateUtils.getRelativeDateTimeString(mContext,
                mBaseTime - DAY_DURATION, DateUtils.MINUTE_IN_MILLIS, DateUtils.DAY_IN_MILLIS,
                DateUtils.FORMAT_NUMERIC_DATE).toString());
        assertNotNull(DateUtils.getRelativeTimeSpanString(mContext,
                mBaseTime - DAY_DURATION, true).toString());
        assertNotNull(DateUtils.getRelativeTimeSpanString(mContext,
                mBaseTime - DAY_DURATION).toString());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "formatElapsedTime",
            args = {long.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "formatElapsedTime",
            args = {StringBuilder.class, long.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "formatDateRange",
            args = {Context.class, long.class, long.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "formatSameDayTime",
            args = {long.class, long.class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "formatDateTime",
            args = {Context.class, long.class, int.class}
        )
    })
    @SuppressWarnings("deprecation")
    public void testFormatMethods() {
        long elapsedTime = 2 * 60 * 60;
        String expected = "2:00:00";
        assertEquals(expected, DateUtils.formatElapsedTime(elapsedTime));
        StringBuilder sb = new StringBuilder();
        assertEquals(expected, DateUtils.formatElapsedTime(sb, elapsedTime));
        assertEquals(expected, sb.toString());
        Date date = new Date(109, 0, 19, 3, 30, 15);
        long fixedTime = date.getTime();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        Date dateWithCurrentYear = new Date(currentYear - 1900, 0, 19, 3, 30, 15);
        long timeWithCurrentYear = dateWithCurrentYear.getTime();
        assertEquals("Saturday, January 24, 2009", DateUtils.formatSameDayTime(
                fixedTime + DAY_DURATION, fixedTime, java.text.DateFormat.FULL,
                java.text.DateFormat.FULL).toString());
        assertEquals("Jan 24, 2009", DateUtils.formatSameDayTime(fixedTime + DAY_DURATION,
                fixedTime, java.text.DateFormat.DEFAULT, java.text.DateFormat.FULL).toString());
        assertEquals("January 24, 2009", DateUtils.formatSameDayTime(fixedTime + DAY_DURATION,
                fixedTime, java.text.DateFormat.LONG, java.text.DateFormat.FULL).toString());
        assertEquals("Jan 24, 2009", DateUtils.formatSameDayTime(fixedTime + DAY_DURATION,
                fixedTime, java.text.DateFormat.MEDIUM, java.text.DateFormat.FULL).toString());
        assertEquals("1/24/09", DateUtils.formatSameDayTime(fixedTime + DAY_DURATION,
                fixedTime, java.text.DateFormat.SHORT, java.text.DateFormat.FULL).toString());
        assertEquals("5:30:15 AM GMT+00:00", DateUtils.formatSameDayTime(fixedTime + HOUR_DURATION,
                fixedTime, java.text.DateFormat.FULL, java.text.DateFormat.FULL).toString());
        assertEquals("5:30:15 AM", DateUtils.formatSameDayTime(fixedTime + HOUR_DURATION,
                fixedTime, java.text.DateFormat.FULL, java.text.DateFormat.DEFAULT).toString());
        assertEquals("5:30:15 AM GMT+00:00", DateUtils.formatSameDayTime(fixedTime + HOUR_DURATION,
                fixedTime, java.text.DateFormat.FULL, java.text.DateFormat.LONG).toString());
        assertEquals("5:30:15 AM", DateUtils.formatSameDayTime(fixedTime + HOUR_DURATION,
                fixedTime, java.text.DateFormat.FULL, java.text.DateFormat.MEDIUM).toString());
        assertEquals("5:30 AM", DateUtils.formatSameDayTime(fixedTime + HOUR_DURATION,
                fixedTime, java.text.DateFormat.FULL, java.text.DateFormat.SHORT).toString());
        long noonDuration = (8 * 60 + 30) * 60 * 1000 - 15 * 1000;
        long midnightDuration = (3 * 60 + 30) * 60 * 1000 + 15 * 1000;
        long integralDuration = 30 * 60 * 1000 + 15 * 1000;
        assertEquals("Monday", DateUtils.formatDateRange(mContext, fixedTime, fixedTime
                + HOUR_DURATION, DateUtils.FORMAT_SHOW_WEEKDAY));
        assertEquals("January 19", DateUtils.formatDateRange(mContext, timeWithCurrentYear,
                timeWithCurrentYear + HOUR_DURATION, DateUtils.FORMAT_SHOW_DATE));
        assertEquals("3:30am", DateUtils.formatDateRange(mContext, fixedTime, fixedTime,
                DateUtils.FORMAT_SHOW_TIME));
        assertEquals("January 19, 2009", DateUtils.formatDateRange(mContext, fixedTime,
                fixedTime + HOUR_DURATION, DateUtils.FORMAT_SHOW_YEAR));
        assertEquals("January 19", DateUtils.formatDateRange(mContext, timeWithCurrentYear,
                timeWithCurrentYear + HOUR_DURATION, DateUtils.FORMAT_NO_YEAR));
        assertEquals("January", DateUtils.formatDateRange(mContext, timeWithCurrentYear,
                timeWithCurrentYear + HOUR_DURATION, DateUtils.FORMAT_NO_MONTH_DAY));
        assertEquals("3:30am", DateUtils.formatDateRange(mContext, fixedTime, fixedTime,
                DateUtils.FORMAT_12HOUR | DateUtils.FORMAT_SHOW_TIME));
        assertEquals("03:30", DateUtils.formatDateRange(mContext, fixedTime, fixedTime,
                DateUtils.FORMAT_24HOUR | DateUtils.FORMAT_SHOW_TIME));
        assertEquals("3:30AM", DateUtils.formatDateRange(mContext, fixedTime, fixedTime,
                DateUtils.FORMAT_12HOUR | DateUtils.FORMAT_CAP_AMPM | DateUtils.FORMAT_SHOW_TIME));
        assertEquals("noon", DateUtils.formatDateRange(mContext, fixedTime + noonDuration,
                fixedTime + noonDuration, DateUtils.FORMAT_12HOUR | DateUtils.FORMAT_SHOW_TIME));
        assertEquals("Noon", DateUtils.formatDateRange(mContext, fixedTime + noonDuration,
                fixedTime + noonDuration,
                DateUtils.FORMAT_12HOUR | DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_CAP_NOON));
        assertEquals("12:00pm", DateUtils.formatDateRange(mContext, fixedTime + noonDuration,
                fixedTime + noonDuration,
                DateUtils.FORMAT_12HOUR | DateUtils.FORMAT_NO_NOON | DateUtils.FORMAT_SHOW_TIME));
        assertEquals("12:00am", DateUtils.formatDateRange(mContext, fixedTime - midnightDuration,
                fixedTime - midnightDuration,
                DateUtils.FORMAT_12HOUR | DateUtils.FORMAT_SHOW_TIME
                | DateUtils.FORMAT_NO_MIDNIGHT));
        assertEquals("3:30am", DateUtils.formatDateRange(mContext, fixedTime, fixedTime,
                DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_UTC));
        assertEquals("3am", DateUtils.formatDateRange(mContext, fixedTime - integralDuration,
                fixedTime - integralDuration,
                DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_ABBREV_TIME));
        assertEquals("Mon", DateUtils.formatDateRange(mContext, fixedTime,
                fixedTime + HOUR_DURATION,
                DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_ABBREV_WEEKDAY));
        assertEquals("Jan 19", DateUtils.formatDateRange(mContext, timeWithCurrentYear,
                timeWithCurrentYear + HOUR_DURATION,
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH));
        assertEquals("Jan 19", DateUtils.formatDateRange(mContext, timeWithCurrentYear,
                timeWithCurrentYear + HOUR_DURATION,
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL));
        String actual = DateUtils.formatDateRange(mContext, fixedTime,
                fixedTime + HOUR_DURATION,
                DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_NUMERIC_DATE);
        assertTrue("1/19/2009".equals(actual) || "01/19/2009".equals(actual));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "isToday",
        args = {long.class}
    )
    public void testIsToday() {
        assertTrue(DateUtils.isToday(mBaseTime));
        assertFalse(DateUtils.isToday(mBaseTime - DAY_DURATION));
    }
}
