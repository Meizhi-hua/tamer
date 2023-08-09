public class ISO8601ZoneTest {
    static final Date TIMESTAMP = new Date(1283758039020L);
    static final String[][] formatData = {
        { "America/Los_Angeles", "2010-09-06T00:27:19.020-07", },
        { "America/Los_Angeles", "2010-09-06T00:27:19.020-0700", },
        { "America/Los_Angeles", "2010-09-06T00:27:19.020-07:00", },
        { "Australia/Sydney", "2010-09-06T17:27:19.020+10", },
        { "Australia/Sydney", "2010-09-06T17:27:19.020+1000", },
        { "Australia/Sydney", "2010-09-06T17:27:19.020+10:00", },
        { "GMT-07:00", "2010-09-06T00:27:19.020-07", },
        { "GMT-07:00", "2010-09-06T00:27:19.020-0700", },
        { "GMT-07:00", "2010-09-06T00:27:19.020-07:00", },
        { "UTC", "2010-09-06T07:27:19.020Z", },
        { "UTC", "2010-09-06T07:27:19.020Z", },
        { "UTC", "2010-09-06T07:27:19.020Z", },
    };
    static final String[] zones = {
        "America/Los_Angeles", "Australia/Sydney", "GMT-07:00",
        "UTC", "GMT+05:30", "GMT-01:23",
    };
    static final String[] isoZoneFormats = {
        "yyyy-MM-dd'T'HH:mm:ss.SSSX",
        "yyyy-MM-dd'T'HH:mm:ss.SSSXX",
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
    };
    static final String[][] badData = {
        { "X", "1", "1" },
        { "X", "+1", "+1" },
        { "X", "-2", "-2" },
        { "X", "-24", "-2" },
        { "X", "+24", "+2" },
        { "XX", "9", "9" },
        { "XX", "23", "2" },
        { "XX", "234", "2" },
        { "XX", "3456", "3" },
        { "XX", "23456", "2" },
        { "XX", "+1", "+1" },
        { "XX", "-12", "-12" },
        { "XX", "+123", "+123" },
        { "XX", "-12:34", "-12" },
        { "XX", "+12:34", "+12" },
        { "XX", "-2423", "-2" },
        { "XX", "+2423", "+2" },
        { "XX", "-1260", "-126" },
        { "XX", "+1260", "+126" },
        { "XXX", "9", "9" },
        { "XXX", "23", "2" },
        { "XXX", "234", "2" },
        { "XXX", "3456", "3" },
        { "XXX", "23456", "2" },
        { "XXX", "2:34", "2" },
        { "XXX", "12:4", "1" },
        { "XXX", "12:34", "1" },
        { "XXX", "-1", "-1" },
        { "XXX", "+1", "+1" },
        { "XXX", "-12", "-12" },
        { "XXX", "+12", "+12" },
        { "XXX", "-123", "-12" },
        { "XXX", "+123", "+12" },
        { "XXX", "-1234", "-12" },
        { "XXX", "+1234", "+12" },
        { "XXX", "+24:23", "+2" },
        { "XXX", "+12:60", "+12:6" },
        { "XXX", "+1:23", "+1" },
        { "XXX", "+12:3", "+12:3" },
    };
    static String[] badFormats = {
        "XXXX", "XXXXX", "XXXXXX",
    };
    public static void main(String[] args) throws Exception {
        TimeZone tz = TimeZone.getDefault();
        Locale loc = Locale.getDefault();
        Locale.setDefault(Locale.US);
        try {
            for (int i = 0; i < formatData.length; i++) {
                TimeZone.setDefault(TimeZone.getTimeZone(formatData[i][0]));
                formatTest(isoZoneFormats[i % isoZoneFormats.length],
                           formatData[i][1]);
            }
            for (String zone : zones) {
                TimeZone.setDefault(TimeZone.getTimeZone(zone));
                for (String fmt : isoZoneFormats) {
                    roundtripTest(fmt);
                    SimpleDateFormat f = new SimpleDateFormat(fmt);
                }
            }
            for (String[] d : badData) {
                badDataParsing(d[0], d[1], d[2].length());
            }
            for (String fmt : badFormats) {
                badFormat(fmt);
            }
        } finally {
            TimeZone.setDefault(tz);
            Locale.setDefault(loc);
        }
    }
    static void formatTest(String fmt, String expected) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat(fmt);
        String s = sdf.format(TIMESTAMP);
        if (!expected.equals(s)) {
            throw new RuntimeException("formatTest: got " + s
                                       + ", expected " + expected);
        }
        Date d = sdf.parse(s);
        if (d.getTime() != TIMESTAMP.getTime()) {
            throw new RuntimeException("formatTest: parse(" + s
                                       + "), got " + d.getTime()
                                       + ", expected " + TIMESTAMP.getTime());
        }
        ParsePosition pos = new ParsePosition(0);
        d = sdf.parse(s + "123", pos);
        if (d.getTime() != TIMESTAMP.getTime()) {
            throw new RuntimeException("formatTest: parse(" + s
                                       + "), got " + d.getTime()
                                       + ", expected " + TIMESTAMP.getTime());
        }
        if (pos.getIndex() != s.length()) {
            throw new RuntimeException("formatTest: wrong resulting parse position: "
                                       + pos.getIndex() + ", expected " + s.length());
        }
    }
    static void roundtripTest(String fmt) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat(fmt);
        Date date = new Date();
        int fractionalHour = sdf.getTimeZone().getOffset(date.getTime());
        fractionalHour %= 3600000; 
        String s = sdf.format(date);
        Date pd = sdf.parse(s);
        long diffsInMillis = pd.getTime() - date.getTime();
        if (diffsInMillis != 0) {
            if (diffsInMillis != fractionalHour) {
                throw new RuntimeException("fmt= " + fmt
                                           + ", diff="+diffsInMillis
                                           + ", fraction=" + fractionalHour);
            }
        }
    }
    static void badDataParsing(String fmt, String text, int expectedErrorIndex) {
        SimpleDateFormat sdf = new SimpleDateFormat(fmt);
        try {
            sdf.parse(text);
            throw new RuntimeException("didn't throw an exception: fmt=" + fmt
                                       + ", text=" + text);
        } catch (ParseException e) {
        }
        ParsePosition pos = new ParsePosition(0);
        Date d = sdf.parse(text, pos);
        int errorIndex = pos.getErrorIndex();
        if (d != null || errorIndex != expectedErrorIndex) {
            throw new RuntimeException("Bad error index=" + errorIndex
                                       + ", expected=" + expectedErrorIndex
                                       + ", fmt=" + fmt + ", text=" + text);
        }
    }
    static void badFormat(String fmt) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(fmt);
            throw new RuntimeException("Constructor didn't throw an exception: fmt=" + fmt);
        } catch (IllegalArgumentException e) {
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat();
            sdf.applyPattern(fmt);
            throw new RuntimeException("applyPattern didn't throw an exception: fmt=" + fmt);
        } catch (IllegalArgumentException e) {
        }
    }
}
