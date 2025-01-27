@TestTargetClass(android.provider.Settings.class)
public class SettingsTest extends AndroidTestCase {
    public void testSystemTable() throws RemoteException {
        final String[] SYSTEM_PROJECTION = new String[] {
                Settings.System._ID, Settings.System.NAME, Settings.System.VALUE
        };
        final int ID_INDEX = 0;
        final int NAME_INDEX = 1;
        final int VALUE_INDEX = 2;
        String insertName = "name_insert";
        String insertValue = "value_insert";
        String updateName = "name_update";
        String updateValue = "value_update";
        ContentResolver cr = mContext.getContentResolver();
        IContentProvider provider = cr.acquireProvider(Settings.System.CONTENT_URI);
        Cursor cursor = null;
        try {
            ContentValues value = new ContentValues();
            value.put(Settings.System.NAME, insertName);
            value.put(Settings.System.VALUE, insertValue);
            provider.insert(Settings.System.CONTENT_URI, value);
            cursor = provider.query(Settings.System.CONTENT_URI, SYSTEM_PROJECTION,
                    Settings.System.NAME + "=\"" + insertName + "\"", null, null);
            assertNotNull(cursor);
            assertEquals(1, cursor.getCount());
            assertTrue(cursor.moveToFirst());
            assertEquals(insertName, cursor.getString(NAME_INDEX));
            assertEquals(insertValue, cursor.getString(VALUE_INDEX));
            int Id = cursor.getInt(ID_INDEX);
            cursor.close();
            value.clear();
            value.put(Settings.System.NAME, updateName);
            value.put(Settings.System.VALUE, updateValue);
            provider.update(Settings.System.CONTENT_URI, value,
                    Settings.System.NAME + "=\"" + insertName + "\"", null);
            cursor = provider.query(Settings.System.CONTENT_URI, SYSTEM_PROJECTION,
                    Settings.System._ID + " = " + Id, null, null);
            assertNotNull(cursor);
            assertEquals(1, cursor.getCount());
            assertTrue(cursor.moveToFirst());
            assertEquals(updateName, cursor.getString(NAME_INDEX));
            assertEquals(updateValue, cursor.getString(VALUE_INDEX));
            cursor.close();
            provider.delete(Settings.System.CONTENT_URI,
                    Settings.System.NAME + "=\"" + updateName + "\"", null);
            cursor = provider.query(Settings.System.CONTENT_URI, SYSTEM_PROJECTION,
                    Settings.System._ID + " = " + Id, null, null);
            assertNotNull(cursor);
            assertEquals(0, cursor.getCount());
        } finally {
            if (cursor != null)
                cursor.close();
        }
    }
    public void testBluetoothDevicesTable() throws RemoteException {
        final String[] BLUETOOTH_DEVICES_PROJECTION = new String[] {
                "name", "addr", "channel", "type"
        };
        final int ID_INDEX = 0;
        final int ADDR_INDEX = 1;
        final int CHANNEL_INDEX = 2;
        final int TYPE_INDEX = 3;
        String insertName = "name_insert";
        String insertAddr = "addr_insert";
        String updateName = "name_update";
        String updateAddr = "addr_update";
        Uri uri = Uri.parse("content:
        ContentResolver cr = mContext.getContentResolver();
        IContentProvider provider = cr.acquireProvider(uri);
        Cursor cursor = null;
        try {
            ContentValues value = new ContentValues();
            value.put("name", insertName);
            value.put("addr", insertAddr);
            value.put("channel", 1);
            value.put("type", 2);
            provider.insert(uri, value);
            cursor = provider.query(uri, BLUETOOTH_DEVICES_PROJECTION,
                    "name=\"" + insertName + "\"", null, null);
            assertNotNull(cursor);
            assertEquals(1, cursor.getCount());
            assertTrue(cursor.moveToFirst());
            assertEquals(insertAddr, cursor.getString(ADDR_INDEX));
            assertEquals(1, cursor.getInt(CHANNEL_INDEX));
            assertEquals(2, cursor.getInt(TYPE_INDEX));
            int Id = cursor.getInt(ID_INDEX);
            cursor.close();
            value.clear();
            value.put("name", updateName);
            value.put("addr", updateAddr);
            value.put("channel", 3);
            value.put("type", 4);
            provider.update(uri, value, "name=\"" + insertName + "\"", null);
            cursor = provider.query(uri, BLUETOOTH_DEVICES_PROJECTION,
                    "name=\"" + updateName + "\"", null, null);
            assertNotNull(cursor);
            assertEquals(1, cursor.getCount());
            assertTrue(cursor.moveToFirst());
            assertEquals(updateAddr, cursor.getString(ADDR_INDEX));
            assertEquals(3, cursor.getInt(CHANNEL_INDEX));
            assertEquals(4, cursor.getInt(TYPE_INDEX));
            cursor.close();
            provider.delete(uri, "name=\"" + updateName + "\"", null);
            cursor = provider.query(uri, BLUETOOTH_DEVICES_PROJECTION, "_id = " + Id, null, null);
            assertNotNull(cursor);
            assertEquals(0, cursor.getCount());
        } finally {
            if (cursor != null)
                cursor.close();
        }
    }
    @BrokenTest("Cannot access secure settings table")
    public void testSecureTable() throws RemoteException {
        final String[] SECURE_PROJECTION = new String[] {
                Settings.Secure._ID, Settings.Secure.NAME, Settings.Secure.VALUE
        };
        String insertName = "name_insert";
        String insertValue = "value_insert";
        ContentResolver cr = mContext.getContentResolver();
        IContentProvider provider = cr.acquireProvider(Settings.Secure.CONTENT_URI);
        ContentValues value = new ContentValues();
        value.put(Settings.Secure.NAME, insertName);
        value.put(Settings.Secure.VALUE, insertValue);
        provider.insert(Settings.Secure.CONTENT_URI, value);
        Cursor cursor = null;
        try {
            cursor = provider.query(Settings.Secure.CONTENT_URI, SECURE_PROJECTION,
                    Settings.Secure.NAME + "=\"" + insertName + "\"", null, null);
            assertNotNull(cursor);
            assertEquals(1, cursor.getCount());
        } finally {
            if (cursor != null)
                cursor.close();
        }
        try {
            provider.delete(Settings.Secure.CONTENT_URI,
                    Settings.Secure.NAME + "=\"" + insertName + "\"", null);
            cursor = provider.query(Settings.Secure.CONTENT_URI, SECURE_PROJECTION,
                    Settings.Secure.NAME + "=\"" + insertName + "\"", null, null);
            assertNotNull(cursor);
            assertEquals(0, cursor.getCount());
        } finally {
            if (cursor != null)
                cursor.close();
        }
    }
    private static final String[] SELECT_VALUE =
        new String[] { Settings.NameValueTable.VALUE };
    private static final String NAME_EQ_PLACEHOLDER = "name=?";
    private void tryBadTableAccess(String table, String goodtable, String name) {
        ContentResolver cr = mContext.getContentResolver();
        Uri uri = Uri.parse("content:
        ContentValues cv = new ContentValues();
        cv.put("name", "name");
        cv.put("value", "xxxTESTxxx");
        try {
            cr.insert(uri, cv);
            fail("SettingsProvider didn't throw IllegalArgumentException for insert name "
                    + name + " at URI " + uri);
        } catch (IllegalArgumentException e) {
        }
        try {
            cr.update(uri, cv, NAME_EQ_PLACEHOLDER, new String[]{name});
            fail("SettingsProvider didn't throw IllegalArgumentException for update name "
                    + name + " at URI " + uri);
        } catch (IllegalArgumentException e) {
        }
        try {
            Cursor c = cr.query(uri, SELECT_VALUE, NAME_EQ_PLACEHOLDER,
                    new String[]{name}, null);
            fail("SettingsProvider didn't throw IllegalArgumentException for query name "
                    + name + " at URI " + uri);
        } catch (IllegalArgumentException e) {
        }
        try {
            cr.delete(uri, NAME_EQ_PLACEHOLDER, new String[]{name});
            fail("SettingsProvider didn't throw IllegalArgumentException for delete name "
                    + name + " at URI " + uri);
        } catch (IllegalArgumentException e) {
        }
        String mimeType = cr.getType(uri);
        assertNull("SettingsProvider didn't return null MIME type for getType at URI "
                + uri, mimeType);
        uri = Uri.parse("content:
        try {
            Cursor c = cr.query(uri, SELECT_VALUE, NAME_EQ_PLACEHOLDER,
                    new String[]{name}, null);
            assertNotNull(c);
            String value = c.moveToNext() ? c.getString(0) : null;
            if ("xxxTESTxxx".equals(value)) {
                fail("Successfully modified " + name + " at URI " + uri);
            }
            c.close();
        } catch (SQLiteException e) {
        }
    }
    public void testAccessNonTable() {
        tryBadTableAccess("SYSTEM", "system", "install_non_market_apps");
        tryBadTableAccess("BOOKMARKS", "bookmarks", "install_non_market_apps");
        tryBadTableAccess("SECURE", "secure", "install_non_market_apps");
        tryBadTableAccess("BLUETOOTH_DEVICES", "bluetooth_devices", "install_non_market_apps");
        tryBadTableAccess(" secure", "secure", "install_non_market_apps");
        tryBadTableAccess("secure ", "secure", "install_non_market_apps");
        tryBadTableAccess(" secure ", "secure", "install_non_market_apps");
    }
}
