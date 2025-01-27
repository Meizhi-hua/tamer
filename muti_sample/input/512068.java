public class FormatDateRangeTest extends AndroidTestCase {
    static private class DateTest {
        public Time date1;
        public Time date2;
        public int flags;
        public String expectedOutput;
        public DateTest(int year1, int month1, int day1, int hour1, int minute1,
                int year2, int month2, int day2, int hour2, int minute2,
                int flags, String output) {
            if ((flags & DateUtils.FORMAT_UTC) != 0) {
                date1 = new Time(Time.TIMEZONE_UTC);
                date2 = new Time(Time.TIMEZONE_UTC);
            } else {
                date1 = new Time();
                date2 = new Time();
            }
            if (year1 == 0 && year2 == 0) {
                date1.set(System.currentTimeMillis());
                year1 = year2 = date1.year;
            }
            date1.set(0, minute1, hour1, day1, month1, year1);
            date1.normalize(true );
            date2.set(0, minute2, hour2, day2, month2, year2);
            date2.normalize(true );
            this.flags = flags;
            expectedOutput = output;
        }
        public DateTest(int year1, int month1, int day1, int hour1, int minute1,
                         int flags, String output) {
            this(year1, month1, day1, hour1, minute1,
                 year1, month1, day1, hour1, minute1,
                 flags, output);
        }
    }
    private Resources mResources;
    DateTest[] tests = {
            new DateTest(0, 10, 9, 8, 0, 0, 10, 9, 11, 0,
                    DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_ABBREV_ALL, "8am \u2013 11am"),
            new DateTest(0, 10, 9, 8, 0, 0, 10, 9, 11, 0,
                    DateUtils.FORMAT_SHOW_TIME, "8:00am \u2013 11:00am"),
            new DateTest(0, 10, 9, 8, 0, 0, 10, 9, 17, 0,
                    DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_24HOUR, "08:00 \u2013 17:00"),
            new DateTest(0, 10, 9, 8, 0, 0, 10, 9, 12, 0,
                    DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_ABBREV_ALL, "8am \u2013 noon"),
            new DateTest(0, 10, 9, 8, 0, 0, 10, 9, 12, 0,
                    DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_NO_NOON | DateUtils.FORMAT_ABBREV_ALL,
                    "8am \u2013 12pm"),
            new DateTest(0, 10, 9, 8, 0, 0, 10, 9, 12, 0,
                    DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_CAP_NOON | DateUtils.FORMAT_ABBREV_ALL,
                    "8am \u2013 Noon"),
            new DateTest(0, 10, 9, 10, 30, 0, 10, 9, 13, 0,
                    DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_ABBREV_ALL, "10:30am \u2013 1pm"),
            new DateTest(0, 10, 9, 13, 0, 0, 10, 9, 14, 0,
                    DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_ABBREV_ALL, "1pm \u2013 2pm"),
            new DateTest(0, 10, 9, 0, 0, 0, 10, 9, 14, 0,
                    DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_ABBREV_ALL, "12am \u2013 2pm"),
            new DateTest(0, 10, 9, 20, 0, 0, 10, 10, 0, 0,
                    DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_ABBREV_ALL, "8pm \u2013 midnight"),
            new DateTest(0, 10, 10, 0, 0,
                    DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_ABBREV_ALL, "12am"),
            new DateTest(0, 10, 9, 20, 0, 0, 10, 10, 0, 0,
                    DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_24HOUR | DateUtils.FORMAT_ABBREV_ALL,
                    "20:00 \u2013 00:00"),
            new DateTest(0, 10, 10, 0, 0,
                    DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_24HOUR | DateUtils.FORMAT_ABBREV_ALL,
                    "00:00"),
            new DateTest(0, 10, 9, 20, 0, 0, 10, 10, 0, 0,
                    DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL, "Nov 9"),
            new DateTest(0, 10, 10, 0, 0, 0, 10, 10, 0, 0,
                    DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL, "Nov 10"),
            new DateTest(0, 10, 9, 20, 0, 0, 10, 10, 0, 0,
                    DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_24HOUR | DateUtils.FORMAT_ABBREV_ALL,
                    "Nov 9"),
            new DateTest(0, 10, 10, 0, 0,
                    DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_24HOUR | DateUtils.FORMAT_ABBREV_ALL,
                    "Nov 10"),
            new DateTest(0, 10, 9, 20, 0, 0, 10, 10, 0, 0,
                    DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_NO_MIDNIGHT | DateUtils.FORMAT_ABBREV_ALL,
                    "8pm \u2013 12am"),
            new DateTest(0, 10, 9, 20, 0, 0, 10, 10, 0, 0,
                    DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_CAP_MIDNIGHT | DateUtils.FORMAT_ABBREV_ALL,
                    "8pm \u2013 Midnight"),
            new DateTest(0, 10, 9, 0, 0, 0, 10, 10, 0, 0,
                    DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_ABBREV_ALL, "12am \u2013 midnight"),
            new DateTest(0, 10, 9, 0, 0, 0, 10, 10, 0, 0,
                    DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_24HOUR | DateUtils.FORMAT_ABBREV_ALL,
                    "00:00 \u2013 00:00"),
            new DateTest(0, 10, 9, 0, 0, 0, 10, 10, 0, 0,
                    DateUtils.FORMAT_UTC | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL, "Nov 9"),
            new DateTest(0, 10, 9, 0, 0, 0, 10, 10, 0, 0,
                    DateUtils.FORMAT_UTC | DateUtils.FORMAT_ABBREV_ALL, "Nov 9"),
            new DateTest(0, 10, 9, 0, 0, 0, 10, 10, 0, 0,
                    DateUtils.FORMAT_UTC, "November 9"),
            new DateTest(0, 10, 8, 0, 0, 0, 10, 10, 0, 0,
                    DateUtils.FORMAT_UTC | DateUtils.FORMAT_ABBREV_ALL, "Nov 8 \u2013 9"),
            new DateTest(0, 10, 9, 0, 0, 0, 10, 11, 0, 0,
                    DateUtils.FORMAT_UTC | DateUtils.FORMAT_ABBREV_ALL, "Nov 9 \u2013 10"),
            new DateTest(0, 10, 9, 8, 0, 0, 10, 11, 17, 0,
                    DateUtils.FORMAT_UTC | DateUtils.FORMAT_ABBREV_ALL, "Nov 9 \u2013 11"),
            new DateTest(0, 9, 29, 8, 0, 0, 10, 3, 17, 0,
                    DateUtils.FORMAT_UTC | DateUtils.FORMAT_ABBREV_ALL, "Oct 29 \u2013 Nov 3"),
            new DateTest(2007, 11, 29, 8, 0, 2008, 0, 2, 17, 0,
                    DateUtils.FORMAT_UTC | DateUtils.FORMAT_ABBREV_ALL, "Dec 29, 2007 \u2013 Jan 2, 2008"),
            new DateTest(2007, 11, 29, 0, 0, 2008, 0, 2, 0, 0,
                    DateUtils.FORMAT_UTC | DateUtils.FORMAT_ABBREV_ALL, "Dec 29, 2007 \u2013 Jan 1, 2008"),
            new DateTest(2007, 11, 29, 8, 0, 2008, 0, 2, 17, 0,
                    DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_ABBREV_ALL,
                    "Dec 29, 2007, 8am \u2013 Jan 2, 2008, 5pm"),
            new DateTest(0, 10, 9, 8, 0, 0, 10, 11, 17, 0,
                    DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_ABBREV_ALL,
                    "Nov 9, 8am \u2013 Nov 11, 5pm"),
            new DateTest(2007, 10, 9, 8, 0, 2007, 10, 11, 17, 0,
                    DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_ABBREV_ALL,
                    "Fri, Nov 9, 2007 \u2013 Sun, Nov 11, 2007"),
            new DateTest(2007, 10, 9, 8, 0, 2007, 10, 11, 17, 0,
                    DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_ABBREV_ALL,
                    "Fri, Nov 9, 2007, 8am \u2013 Sun, Nov 11, 2007, 5pm"),
            new DateTest(2007, 11, 3, 13, 0, 2007, 11, 3, 14, 0,
                    DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR,
                    "1:00pm \u2013 2:00pm, December 3, 2007"),
            new DateTest(2007, 11, 3, 13, 0, 2007, 11, 3, 13, 0,
                    DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_NO_YEAR,
                    "December 3, 2007"),
            new DateTest(
                    Calendar.getInstance().get(Calendar.YEAR), 0, 3, 13, 0,
                    DateUtils.FORMAT_SHOW_DATE,
                    "January 3"),
            new DateTest(
                    Calendar.getInstance().get(Calendar.YEAR) - 1, 0, 3, 13, 0,
                    DateUtils.FORMAT_SHOW_DATE,
                    "January 3, " + (Calendar.getInstance().get(Calendar.YEAR) - 1)),
    };
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mResources = mContext.getResources();
    }
    @MediumTest
    public void testAll() throws Exception {
        int len = tests.length;
        for (int index = 0; index < len; index++) {
            DateTest dateTest = tests[index];
            long startMillis = dateTest.date1.toMillis(false );
            long endMillis = dateTest.date2.toMillis(false );
            int flags = dateTest.flags;
            String output = DateUtils.formatDateRange(mContext, startMillis, endMillis, flags);
            if (!dateTest.expectedOutput.equals(output)) {
                Log.i("FormatDateRangeTest", "index " + index
                        + " expected: " + dateTest.expectedOutput
                        + " actual: " + output);
            }
            assertEquals(dateTest.expectedOutput, output);
        }
    }
}
