public class SmsProviderTest extends AndroidTestCase {
    @LargeTest
    public void testProvider() throws Exception {
        long now = System.currentTimeMillis();
        Uri[] urls = new Uri[10];
        String[] dates = new String[]{
                Long.toString(new GregorianCalendar(1970, 1, 1, 0, 0, 0).getTimeInMillis()),
                Long.toString(new GregorianCalendar(1971, 2, 13, 16, 35, 3).getTimeInMillis()),
                Long.toString(new GregorianCalendar(1978, 10, 22, 0, 1, 0).getTimeInMillis()),
                Long.toString(new GregorianCalendar(1980, 1, 11, 10, 22, 30).getTimeInMillis()),
                Long.toString(now - (5 * 24 * 60 * 60 * 1000)),
                Long.toString(now - (2 * 24 * 60 * 60 * 1000)),
                Long.toString(now - (5 * 60 * 60 * 1000)),
                Long.toString(now - (30 * 60 * 1000)),
                Long.toString(now - (5 * 60 * 1000)),
                Long.toString(now)
        };
        ContentValues map = new ContentValues();
        map.put("address", "+15045551337");
        map.put("read", 0);
        ContentResolver contentResolver = mContext.getContentResolver();
        for (int i = 0; i < urls.length; i++) {
            map.put("body", "Test " + i + " !");
            map.put("date", dates[i]);
            urls[i] = contentResolver.insert(Sms.Inbox.CONTENT_URI, map);
            assertNotNull(urls[i]);
        }
        Cursor c = contentResolver.query(Sms.Inbox.CONTENT_URI, null, null, null, "date");
        for (Uri url : urls) {
            int count = contentResolver.delete(url, null, null);
            assertEquals(1, count);
        }
    }
}
