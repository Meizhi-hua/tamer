@TestTargetClass(MediaStore.Video.Media.class)
public class MediaStore_Video_MediaTest extends InstrumentationTestCase {
    private ContentResolver mContentResolver;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContentResolver = getInstrumentation().getContext().getContentResolver();
    }
    @TestTargetNew(
      level = TestLevel.COMPLETE,
      method = "getContentUri",
      args = {String.class}
    )
    public void testGetContentUri() {
        assertNotNull(mContentResolver.query(Media.getContentUri("internal"), null, null, null,
                null));
        assertNotNull(mContentResolver.query(Media.getContentUri("external"), null, null, null,
                null));
        String volume = "fakeVolume";
        assertNull(mContentResolver.query(Media.getContentUri(volume), null, null, null, null));
    }
    public void testStoreVideoMediaExternal() {
        final String externalVideoPath = Environment.getExternalStorageDirectory().getPath() +
                 "/video/testvideo.3gp";
        final String externalVideoPath2 = Environment.getExternalStorageDirectory().getPath() +
                "/video/testvideo1.3gp";
        ContentValues values = new ContentValues();
        values.put(Media.ALBUM, "cts");
        values.put(Media.ARTIST, "cts team");
        values.put(Media.CATEGORY, "test");
        long dateTaken = System.currentTimeMillis();
        values.put(Media.DATE_TAKEN, dateTaken);
        values.put(Media.DESCRIPTION, "This is a video");
        values.put(Media.DURATION, 8480);
        values.put(Media.LANGUAGE, "en");
        values.put(Media.LATITUDE, 40.689060d);
        values.put(Media.LONGITUDE, -74.044636d);
        values.put(Media.IS_PRIVATE, 1);
        values.put(Media.MINI_THUMB_MAGIC, 0);
        values.put(Media.RESOLUTION, "176x144");
        values.put(Media.TAGS, "cts, test");
        values.put(Media.DATA, externalVideoPath);
        values.put(Media.DISPLAY_NAME, "testvideo");
        values.put(Media.MIME_TYPE, "video/3gpp");
        values.put(Media.SIZE, 86853);
        values.put(Media.TITLE, "testvideo");
        long dateAdded = System.currentTimeMillis();
        values.put(Media.DATE_ADDED, dateAdded);
        long dateModified = System.currentTimeMillis();
        values.put(Media.DATE_MODIFIED, dateModified);
        Uri uri = mContentResolver.insert(Media.EXTERNAL_CONTENT_URI, values);
        assertNotNull(uri);
        try {
            Cursor c = mContentResolver.query(uri, null, null, null, null);
            assertEquals(1, c.getCount());
            c.moveToFirst();
            long id = c.getLong(c.getColumnIndex(Media._ID));
            assertTrue(id > 0);
            assertEquals("cts", c.getString(c.getColumnIndex(Media.ALBUM)));
            assertEquals("cts team", c.getString(c.getColumnIndex(Media.ARTIST)));
            assertEquals("test", c.getString(c.getColumnIndex(Media.CATEGORY)));
            assertEquals(dateTaken, c.getLong(c.getColumnIndex(Media.DATE_TAKEN)));
            assertEquals(8480, c.getInt(c.getColumnIndex(Media.DURATION)));
            assertEquals("This is a video",
                    c.getString(c.getColumnIndex(Media.DESCRIPTION)));
            assertEquals("en", c.getString(c.getColumnIndex(Media.LANGUAGE)));
            assertEquals(40.689060d, c.getDouble(c.getColumnIndex(Media.LATITUDE)), 0d);
            assertEquals(-74.044636d, c.getDouble(c.getColumnIndex(Media.LONGITUDE)), 0d);
            assertEquals(1, c.getInt(c.getColumnIndex(Media.IS_PRIVATE)));
            assertEquals(0, c.getLong(c.getColumnIndex(Media.MINI_THUMB_MAGIC)));
            assertEquals("176x144", c.getString(c.getColumnIndex(Media.RESOLUTION)));
            assertEquals("cts, test", c.getString(c.getColumnIndex(Media.TAGS)));
            assertEquals(externalVideoPath, c.getString(c.getColumnIndex(Media.DATA)));
            assertEquals("testvideo.3gp", c.getString(c.getColumnIndex(Media.DISPLAY_NAME)));
            assertEquals("video/3gpp", c.getString(c.getColumnIndex(Media.MIME_TYPE)));
            assertEquals("testvideo", c.getString(c.getColumnIndex(Media.TITLE)));
            assertEquals(86853, c.getInt(c.getColumnIndex(Media.SIZE)));
            long realDateAdded = c.getLong(c.getColumnIndex(Media.DATE_ADDED));
            assertTrue(realDateAdded > 0);
            assertEquals(dateModified, c.getLong(c.getColumnIndex(Media.DATE_MODIFIED)));
            c.close();
            values.clear();
            values.put(Media.ALBUM, "cts1");
            values.put(Media.ARTIST, "cts team1");
            values.put(Media.CATEGORY, "test1");
            dateTaken = System.currentTimeMillis();
            values.put(Media.DATE_TAKEN, dateTaken);
            values.put(Media.DESCRIPTION, "This is another video");
            values.put(Media.DURATION, 8481);
            values.put(Media.LANGUAGE, "cn");
            values.put(Media.LATITUDE, 41.689060d);
            values.put(Media.LONGITUDE, -75.044636d);
            values.put(Media.IS_PRIVATE, 0);
            values.put(Media.MINI_THUMB_MAGIC, 2);
            values.put(Media.RESOLUTION, "320x240");
            values.put(Media.TAGS, "cts1, test1");
            values.put(Media.DATA, externalVideoPath2);
            values.put(Media.DISPLAY_NAME, "testvideo1");
            values.put(Media.MIME_TYPE, "video/3gpp");
            values.put(Media.SIZE, 86854);
            values.put(Media.TITLE, "testvideo1");
            dateModified = System.currentTimeMillis();
            values.put(Media.DATE_MODIFIED, dateModified);
            assertEquals(1, mContentResolver.update(uri, values, null, null));
            c = mContentResolver.query(uri, null, null, null, null);
            assertEquals(1, c.getCount());
            c.moveToFirst();
            assertEquals(id, c.getLong(c.getColumnIndex(Media._ID)));
            assertEquals("cts1", c.getString(c.getColumnIndex(Media.ALBUM)));
            assertEquals("cts team1", c.getString(c.getColumnIndex(Media.ARTIST)));
            assertEquals("test1", c.getString(c.getColumnIndex(Media.CATEGORY)));
            assertEquals(dateTaken, c.getLong(c.getColumnIndex(Media.DATE_TAKEN)));
            assertEquals(8481, c.getInt(c.getColumnIndex(Media.DURATION)));
            assertEquals("This is another video",
                    c.getString(c.getColumnIndex(Media.DESCRIPTION)));
            assertEquals("cn", c.getString(c.getColumnIndex(Media.LANGUAGE)));
            assertEquals(41.689060d, c.getDouble(c.getColumnIndex(Media.LATITUDE)), 0d);
            assertEquals(-75.044636d, c.getDouble(c.getColumnIndex(Media.LONGITUDE)), 0d);
            assertEquals(0, c.getInt(c.getColumnIndex(Media.IS_PRIVATE)));
            assertEquals(2, c.getLong(c.getColumnIndex(Media.MINI_THUMB_MAGIC)));
            assertEquals("320x240", c.getString(c.getColumnIndex(Media.RESOLUTION)));
            assertEquals("cts1, test1", c.getString(c.getColumnIndex(Media.TAGS)));
            assertEquals(externalVideoPath2,
                    c.getString(c.getColumnIndex(Media.DATA)));
            assertEquals("testvideo1", c.getString(c.getColumnIndex(Media.DISPLAY_NAME)));
            assertEquals("video/3gpp", c.getString(c.getColumnIndex(Media.MIME_TYPE)));
            assertEquals("testvideo1", c.getString(c.getColumnIndex(Media.TITLE)));
            assertEquals(86854, c.getInt(c.getColumnIndex(Media.SIZE)));
            assertEquals(realDateAdded, c.getLong(c.getColumnIndex(Media.DATE_ADDED)));
            assertEquals(dateModified, c.getLong(c.getColumnIndex(Media.DATE_MODIFIED)));
            c.close();
        } finally {
            assertEquals(1, mContentResolver.delete(uri, null, null));
        }
    }
    public void testStoreVideoMediaInternal() {
        try {
            mContentResolver.insert(Media.INTERNAL_CONTENT_URI, new ContentValues());
            fail("Should throw UnsupportedOperationException when inserting into internal "
                    + "database");
        } catch (UnsupportedOperationException e) {
        }
    }
}
