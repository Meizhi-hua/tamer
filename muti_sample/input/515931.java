@TestTargetClass(android.provider.Contacts.class)
public class ContactsTest extends InstrumentationTestCase {
    private ContentResolver mContentResolver;
    private IContentProvider mProvider;
    private IContentProvider mCallLogProvider;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContentResolver = getInstrumentation().getTargetContext().getContentResolver();
        mProvider = mContentResolver.acquireProvider(Contacts.AUTHORITY);
        mCallLogProvider = mContentResolver.acquireProvider(CallLog.AUTHORITY);
    }
    public void testPeopleTable() {
        final String[] PEOPLE_PROJECTION = new String[] {
                People._ID,
                People.NAME, People.NOTES, People.TIMES_CONTACTED,
                People.LAST_TIME_CONTACTED, People.STARRED,
                People.CUSTOM_RINGTONE, People.SEND_TO_VOICEMAIL,};
        final int ID_INDEX = 0;
        final int NAME_INDEX = 1;
        final int NOTES_INDEX = 2;
        final int TIMES_CONTACTED_INDEX = 3;
        final int LAST_TIME_CONTACTED_INDEX = 4;
        final int STARRED_INDEX = 5;
        final int CUSTOM_RINGTONE_INDEX = 6;
        final int SEND_TO_VOICEMAIL_INDEX = 7;
        String insertPeopleName = "name_insert";
        String insertPeopleNotes = "notes_insert";
        String updatePeopleName = "name_update";
        String updatePeopleNotes = "notes_update";
        try {
            mProvider.delete(People.CONTENT_URI, PeopleColumns.NAME + " = ?",
                    new String[] {insertPeopleName});
            ContentValues value = new ContentValues();
            value.put(PeopleColumns.NAME, insertPeopleName);
            value.put(PeopleColumns.NOTES, insertPeopleNotes);
            value.put(PeopleColumns.LAST_TIME_CONTACTED, 0);
            value.put(PeopleColumns.CUSTOM_RINGTONE, (String) null);
            value.put(PeopleColumns.SEND_TO_VOICEMAIL, 1);
            Uri uri = mProvider.insert(People.CONTENT_URI, value);
            Cursor cursor = mProvider.query(People.CONTENT_URI,
                    PEOPLE_PROJECTION, PeopleColumns.NAME + " = ?",
                    new String[] {insertPeopleName}, null);
            assertTrue(cursor.moveToNext());
            assertEquals(insertPeopleName, cursor.getString(NAME_INDEX));
            assertEquals(insertPeopleNotes, cursor.getString(NOTES_INDEX));
            assertEquals(0, cursor.getInt(LAST_TIME_CONTACTED_INDEX));
            assertNull(cursor.getString(CUSTOM_RINGTONE_INDEX));
            assertEquals(1, cursor.getInt(SEND_TO_VOICEMAIL_INDEX));
            assertEquals(0, cursor.getInt(TIMES_CONTACTED_INDEX));
            assertEquals(0, cursor.getInt(STARRED_INDEX));
            int id = cursor.getInt(ID_INDEX);
            cursor.close();
            value.clear();
            long now = new Date().getTime();
            value.put(PeopleColumns.NAME, updatePeopleName);
            value.put(PeopleColumns.NOTES, updatePeopleNotes);
            value.put(PeopleColumns.LAST_TIME_CONTACTED, (int) now);
            mProvider.update(uri, value, null, null);
            cursor = mProvider.query(People.CONTENT_URI, PEOPLE_PROJECTION,
                    "people._id" + " = " + id, null, null);
            assertTrue(cursor.moveToNext());
            assertEquals(updatePeopleName, cursor.getString(NAME_INDEX));
            assertEquals(updatePeopleNotes, cursor.getString(NOTES_INDEX));
            assertEquals((int) now, cursor.getInt(LAST_TIME_CONTACTED_INDEX));
            assertNull(cursor.getString(CUSTOM_RINGTONE_INDEX));
            assertEquals(1, cursor.getInt(SEND_TO_VOICEMAIL_INDEX));
            assertEquals(1, cursor.getInt(TIMES_CONTACTED_INDEX));
            assertEquals(0, cursor.getInt(STARRED_INDEX));
            cursor.close();
            mProvider.delete(uri, null, null);
            cursor = mProvider.query(People.CONTENT_URI, PEOPLE_PROJECTION,
                    "people._id" + " = " + id, null, null);
            assertEquals(0, cursor.getCount());
        } catch (RemoteException e) {
            fail("Unexpected RemoteException");
        }
    }
    @BrokenTest("Throws NPE in call to update(). uri parameter null?")
    public void testGroupsTable() {
        final String[] GROUPS_PROJECTION = new String[] {
                Groups._ID, Groups.NAME, Groups.NOTES, Groups.SHOULD_SYNC,
                Groups.SYSTEM_ID, Groups._SYNC_ACCOUNT, Groups._SYNC_ACCOUNT_TYPE, Groups._SYNC_ID,
                Groups._SYNC_TIME, Groups._SYNC_VERSION, Groups._SYNC_LOCAL_ID,
                Groups._SYNC_DIRTY};
        final int ID_INDEX = 0;
        final int NAME_INDEX = 1;
        final int NOTES_INDEX = 2;
        final int SHOULD_SYNC_INDEX = 3;
        final int SYSTEM_ID_INDEX = 4;
        final int SYNC_ACCOUNT_NAME_INDEX = 5;
        final int SYNC_ACCOUNT_TYPE_INDEX = 6;
        final int SYNC_ID_INDEX = 7;
        final int SYNC_TIME_INDEX = 8;
        final int SYNC_VERSION_INDEX = 9;
        final int SYNC_LOCAL_ID_INDEX = 10;
        final int SYNC_DIRTY_INDEX = 11;
        String insertGroupsName = "name_insert";
        String insertGroupsNotes = "notes_insert";
        String updateGroupsName = "name_update";
        String updateGroupsNotes = "notes_update";
        String updateGroupsSystemId = "system_id_update";
        try {
            ContentValues value = new ContentValues();
            value.put(GroupsColumns.NAME, insertGroupsName);
            value.put(GroupsColumns.NOTES, insertGroupsNotes);
            value.put(GroupsColumns.SYSTEM_ID, Groups.GROUP_MY_CONTACTS);
            Uri uri = mProvider.insert(Groups.CONTENT_URI, value);
            Cursor cursor = mProvider.query(Groups.CONTENT_URI,
                    GROUPS_PROJECTION, GroupsColumns.NAME + " = ?",
                    new String[] {insertGroupsName}, null);
            assertTrue(cursor.moveToNext());
            assertEquals(insertGroupsName, cursor.getString(NAME_INDEX));
            assertEquals(insertGroupsNotes, cursor.getString(NOTES_INDEX));
            assertEquals(0, cursor.getInt(SHOULD_SYNC_INDEX));
            assertEquals(Groups.GROUP_MY_CONTACTS, cursor.getString(SYSTEM_ID_INDEX));
            int id = cursor.getInt(ID_INDEX);
            cursor.close();
            value.clear();
            value.put(GroupsColumns.NAME, updateGroupsName);
            value.put(GroupsColumns.NOTES, updateGroupsNotes);
            value.put(GroupsColumns.SYSTEM_ID, updateGroupsSystemId);
            value.put(GroupsColumns.SHOULD_SYNC, 1);
            mProvider.update(uri, value, null, null);
            cursor = mProvider.query(Groups.CONTENT_URI, GROUPS_PROJECTION,
                    Groups._ID + " = " + id, null, null);
            assertTrue(cursor.moveToNext());
            assertEquals(updateGroupsName, cursor.getString(NAME_INDEX));
            assertEquals(updateGroupsNotes, cursor.getString(NOTES_INDEX));
            assertEquals(1, cursor.getInt(SHOULD_SYNC_INDEX));
            assertEquals(updateGroupsSystemId, cursor.getString(SYSTEM_ID_INDEX));
            cursor.close();
            mProvider.delete(uri, null, null);
            cursor = mProvider.query(Groups.CONTENT_URI, GROUPS_PROJECTION,
                    Groups._ID + " = " + id, null, null);
            assertEquals(0, cursor.getCount());
        } catch (RemoteException e) {
            fail("Unexpected RemoteException");
        }
    }
    @BrokenTest("Should not test EXISTS_ON_SERVER_INDEX?")
    public void testPhotosTable() {
        final String[] PHOTOS_PROJECTION = new String[] {
                Photos._ID, Photos.EXISTS_ON_SERVER, Photos.PERSON_ID,
                Photos.LOCAL_VERSION, Photos.DATA, Photos._SYNC_ACCOUNT, Photos._SYNC_ACCOUNT_TYPE,
                Photos._SYNC_ID, Photos._SYNC_TIME, Photos._SYNC_VERSION,
                Photos._SYNC_LOCAL_ID, Photos._SYNC_DIRTY,
                Photos.SYNC_ERROR};
        final int ID_INDEX = 0;
        final int EXISTS_ON_SERVER_INDEX = 1;
        final int PERSON_ID_INDEX = 2;
        final int LOCAL_VERSION_INDEX = 3;
        final int DATA_INDEX = 4;
        final int SYNC_ACCOUNT_NAME_INDEX = 5;
        final int SYNC_ACCOUNT_TYPE_INDEX = 6;
        final int SYNC_ID_INDEX = 7;
        final int SYNC_TIME_INDEX = 8;
        final int SYNC_VERSION_INDEX = 9;
        final int SYNC_LOCAL_ID_INDEX = 10;
        final int SYNC_DIRTY_INDEX = 11;
        final int SYNC_ERROR_INDEX = 12;
        String updatePhotosLocalVersion = "local_version1";
        try {
            Context context = getInstrumentation().getTargetContext();
            InputStream inputStream = context.getResources().openRawResource(
                    com.android.cts.stub.R.drawable.testimage);
            int size = inputStream.available();
            byte[] data =  new byte[size];
            inputStream.read(data);
            BitmapDrawable sourceDrawable = (BitmapDrawable) context.getResources().getDrawable(
                    com.android.cts.stub.R.drawable.testimage);
            ContentValues value = new ContentValues();
            value.put(Photos.PERSON_ID, 1);
            value.put(Photos.LOCAL_VERSION, "local_version0");
            value.put(Photos.DATA, data);
            try {
                mProvider.insert(Photos.CONTENT_URI, value);
                fail("Should throw out UnsupportedOperationException.");
            } catch (UnsupportedOperationException e) {
            }
            value.clear();
            value.put(PeopleColumns.NAME, "name_photos_test_stub");
            Uri peopleUri = mProvider.insert(People.CONTENT_URI, value);
            int peopleId = Integer.parseInt(peopleUri.getPathSegments().get(1));
            Cursor cursor = mProvider.query(Photos.CONTENT_URI,
                    PHOTOS_PROJECTION, Photos.PERSON_ID + " = " + peopleId,
                    null, null);
            assertTrue(cursor.moveToNext());
            assertEquals(0, cursor.getInt(EXISTS_ON_SERVER_INDEX));
            assertEquals(peopleId, cursor.getInt(PERSON_ID_INDEX));
            assertNull(cursor.getString(LOCAL_VERSION_INDEX));
            assertNull(cursor.getString(DATA_INDEX));
            int id = cursor.getInt(ID_INDEX);
            cursor.close();
            value.clear();
            value.put(Photos.LOCAL_VERSION, updatePhotosLocalVersion);
            value.put(Photos.DATA, data);
            value.put(Photos.EXISTS_ON_SERVER, 1);
            Uri uri = ContentUris.withAppendedId(Photos.CONTENT_URI, id);
            mProvider.update(uri, value, null, null);
            cursor = mProvider.query(Photos.CONTENT_URI, PHOTOS_PROJECTION,
                    Photos._ID + " = " + id, null, null);
            assertTrue(cursor.moveToNext());
            assertEquals(1, cursor.getInt(EXISTS_ON_SERVER_INDEX));
            assertEquals(peopleId, cursor.getInt(PERSON_ID_INDEX));
            assertEquals(updatePhotosLocalVersion, cursor.getString(LOCAL_VERSION_INDEX));
            byte resultData[] = cursor.getBlob(DATA_INDEX);
            InputStream resultInputStream = new ByteArrayInputStream(resultData);
            Bitmap bitmap = BitmapFactory.decodeStream(resultInputStream, null, null);
            assertEquals(sourceDrawable.getIntrinsicWidth(), bitmap.getWidth());
            assertEquals(sourceDrawable.getIntrinsicHeight(), bitmap.getHeight());
            cursor.close();
            mProvider.delete(peopleUri, null, null);
            cursor = mProvider.query(Photos.CONTENT_URI, PHOTOS_PROJECTION,
                    Groups._ID + " = " + id, null, null);
            assertEquals(0, cursor.getCount());
            mProvider.delete(peopleUri, null, null);
        } catch (RemoteException e) {
            fail("Unexpected RemoteException");
        } catch (IOException e) {
            fail("Unexpected IOException");
        }
    }
    public void testPhonesTable() {
        final String[] PHONES_PROJECTION = new String[] {
                Phones._ID, Phones.PERSON_ID, Phones.TYPE, Phones.NUMBER,
                Phones.NUMBER_KEY, Phones.LABEL, Phones.ISPRIMARY};
        final int ID_INDEX = 0;
        final int PERSON_ID_INDEX = 1;
        final int TYPE_INDEX = 2;
        final int NUMBER_INDEX = 3;
        final int NUMBER_KEY_INDEX = 4;
        final int LABEL_INDEX = 5;
        final int ISPRIMARY_INDEX = 6;
        String insertPhonesNumber = "0123456789";
        String updatePhonesNumber = "987*654yu3211+";
        String customeLabel = "custom_label";
        try {
            ContentValues value = new ContentValues();
            value.put(PeopleColumns.NAME, "name_phones_test_stub");
            Uri peopleUri = mProvider.insert(People.CONTENT_URI, value);
            int peopleId = Integer.parseInt(peopleUri.getPathSegments().get(1));
            value.clear();
            value.put(Phones.PERSON_ID, peopleId);
            value.put(Phones.TYPE, Phones.TYPE_HOME);
            value.put(Phones.NUMBER, insertPhonesNumber);
            value.put(Phones.ISPRIMARY, 1);
            Uri uri = mProvider.insert(Phones.CONTENT_URI, value);
            Cursor cursor = mProvider.query(Phones.CONTENT_URI,
                    PHONES_PROJECTION, Phones.PERSON_ID + " = " + peopleId,
                    null, null);
            assertTrue(cursor.moveToNext());
            assertEquals(peopleId, cursor.getInt(PERSON_ID_INDEX));
            assertEquals(Phones.TYPE_HOME, cursor.getInt(TYPE_INDEX));
            assertEquals(insertPhonesNumber, cursor.getString(NUMBER_INDEX));
            assertEquals(PhoneNumberUtils.getStrippedReversed(insertPhonesNumber),
                    cursor.getString(NUMBER_KEY_INDEX));
            assertNull(cursor.getString(LABEL_INDEX));
            assertEquals(1, cursor.getInt(ISPRIMARY_INDEX));
            int id = cursor.getInt(ID_INDEX);
            cursor.close();
            value.clear();
            value.put(Phones.TYPE, Phones.TYPE_CUSTOM);
            value.put(Phones.NUMBER, updatePhonesNumber);
            value.put(Phones.LABEL, customeLabel);
            mProvider.update(uri, value, null, null);
            cursor = mProvider.query(Phones.CONTENT_URI, PHONES_PROJECTION,
                    "phones._id = " + id, null, null);
            assertTrue(cursor.moveToNext());
            assertEquals(peopleId, cursor.getInt(PERSON_ID_INDEX));
            assertEquals(Phones.TYPE_CUSTOM, cursor.getInt(TYPE_INDEX));
            assertEquals(updatePhonesNumber, cursor.getString(NUMBER_INDEX));
            assertEquals(PhoneNumberUtils.getStrippedReversed(updatePhonesNumber),
                    cursor.getString(NUMBER_KEY_INDEX));
            assertEquals(customeLabel, cursor.getString(LABEL_INDEX));
            assertEquals(1, cursor.getInt(ISPRIMARY_INDEX));
            cursor.close();
            mProvider.delete(uri, null, null);
            cursor = mProvider.query(Phones.CONTENT_URI, PHONES_PROJECTION,
                    Phones.PERSON_ID + " = " + peopleId, null, null);
            assertEquals(0, cursor.getCount());
            mProvider.delete(peopleUri, null, null);
        } catch (RemoteException e) {
            fail("Unexpected RemoteException");
        }
    }
    public void testOrganizationsTable() {
        final String[] ORGANIZATIONS_PROJECTION = new String[] {
                Organizations._ID, Organizations.COMPANY, Organizations.TITLE,
                Organizations.ISPRIMARY, Organizations.TYPE, Organizations.LABEL,
                Organizations.PERSON_ID};
        final int ID_INDEX = 0;
        final int COMPANY_INDEX = 1;
        final int TITLE_INDEX = 2;
        final int ISPRIMARY_INDEX = 3;
        final int TYPE_INDEX = 4;
        final int LABEL_INDEX = 5;
        final int PERSON_ID_INDEX = 6;
        String insertOrganizationsCompany = "company_insert";
        String insertOrganizationsTitle = "title_insert";
        String updateOrganizationsCompany = "company_update";
        String updateOrganizationsTitle = "title_update";
        String customOrganizationsLabel = "custom_label";
        try {
            ContentValues value = new ContentValues();
            value.put(PeopleColumns.NAME, "name_organizations_test_stub");
            Uri peopleUri = mProvider.insert(People.CONTENT_URI, value);
            int peopleId = Integer.parseInt(peopleUri.getPathSegments().get(1));
            value.clear();
            value.put(Organizations.COMPANY, insertOrganizationsCompany);
            value.put(Organizations.TITLE, insertOrganizationsTitle);
            value.put(Organizations.TYPE, Organizations.TYPE_WORK);
            value.put(Organizations.PERSON_ID, peopleId);
            value.put(Organizations.ISPRIMARY, 1);
            Uri uri = mProvider.insert(Organizations.CONTENT_URI, value);
            Cursor cursor = mProvider.query(
                    Organizations.CONTENT_URI, ORGANIZATIONS_PROJECTION,
                    Organizations.PERSON_ID + " = " + peopleId,
                    null, null);
            assertTrue(cursor.moveToNext());
            assertEquals(insertOrganizationsCompany, cursor.getString(COMPANY_INDEX));
            assertEquals(insertOrganizationsTitle, cursor.getString(TITLE_INDEX));
            assertEquals(1, cursor.getInt(ISPRIMARY_INDEX));
            assertEquals(Organizations.TYPE_WORK, cursor.getInt(TYPE_INDEX));
            assertNull(cursor.getString(LABEL_INDEX));
            assertEquals(peopleId, cursor.getInt(PERSON_ID_INDEX));
            int id = cursor.getInt(ID_INDEX);
            cursor.close();
            value.clear();
            value.put(Organizations.COMPANY, updateOrganizationsCompany);
            value.put(Organizations.TITLE, updateOrganizationsTitle);
            value.put(Organizations.TYPE, Organizations.TYPE_CUSTOM);
            value.put(Organizations.LABEL, customOrganizationsLabel);
            mProvider.update(uri, value, null, null);
            cursor = mProvider.query(Organizations.CONTENT_URI, ORGANIZATIONS_PROJECTION,
                    "organizations._id" + " = " + id, null, null);
            assertTrue(cursor.moveToNext());
            assertEquals(updateOrganizationsCompany, cursor.getString(COMPANY_INDEX));
            assertEquals(updateOrganizationsTitle, cursor.getString(TITLE_INDEX));
            assertEquals(1, cursor.getInt(ISPRIMARY_INDEX));
            assertEquals(Organizations.TYPE_CUSTOM, cursor.getInt(TYPE_INDEX));
            assertEquals(customOrganizationsLabel, cursor.getString(LABEL_INDEX));
            assertEquals(peopleId, cursor.getInt(PERSON_ID_INDEX));
            cursor.close();
            mProvider.delete(uri, null, null);
            cursor = mProvider.query(Organizations.CONTENT_URI, ORGANIZATIONS_PROJECTION,
                    Organizations.PERSON_ID + " = " + peopleId, null, null);
            assertEquals(0, cursor.getCount());
            mProvider.delete(peopleUri, null, null);
        } catch (RemoteException e) {
            fail("Unexpected RemoteException");
        }
    }
    public void testCallsTable() {
        final String[] CALLS_PROJECTION = new String[] {
                Calls._ID, Calls.NUMBER, Calls.DATE, Calls.DURATION, Calls.TYPE,
                Calls.NEW, Calls.CACHED_NAME, Calls.CACHED_NUMBER_TYPE,
                Calls.CACHED_NUMBER_LABEL};
        final int ID_INDEX = 0;
        final int NUMBER_INDEX = 1;
        final int DATE_INDEX = 2;
        final int DURATION_INDEX = 3;
        final int TYPE_INDEX = 4;
        final int NEW_INDEX = 5;
        final int CACHED_NAME_INDEX = 6;
        final int CACHED_NUMBER_TYPE_INDEX = 7;
        final int CACHED_NUMBER_LABEL_INDEX = 8;
        String insertCallsNumber = "0123456789";
        int insertCallsDuration = 120;
        String insertCallsName = "cached_name_insert";
        String insertCallsNumberLabel = "cached_label_insert";
        String updateCallsNumber = "9876543210";
        int updateCallsDuration = 310;
        String updateCallsName = "cached_name_update";
        String updateCallsNumberLabel = "cached_label_update";
        try {
            int insertDate = (int) new Date().getTime();
            ContentValues value = new ContentValues();
            value.put(Calls.NUMBER, insertCallsNumber);
            value.put(Calls.DATE, insertDate);
            value.put(Calls.DURATION, insertCallsDuration);
            value.put(Calls.TYPE, Calls.INCOMING_TYPE);
            value.put(Calls.NEW, 0);
            value.put(Calls.CACHED_NAME, insertCallsName);
            value.put(Calls.CACHED_NUMBER_TYPE, Phones.TYPE_HOME);
            value.put(Calls.CACHED_NUMBER_LABEL, insertCallsNumberLabel);
            Uri uri = mCallLogProvider.insert(Calls.CONTENT_URI, value);
            Cursor cursor = mCallLogProvider.query(
                    Calls.CONTENT_URI, CALLS_PROJECTION,
                    Calls.NUMBER + " = ?",
                    new String[] {insertCallsNumber}, null);
            assertTrue(cursor.moveToNext());
            assertEquals(insertCallsNumber, cursor.getString(NUMBER_INDEX));
            assertEquals(insertDate, cursor.getInt(DATE_INDEX));
            assertEquals(insertCallsDuration, cursor.getInt(DURATION_INDEX));
            assertEquals(Calls.INCOMING_TYPE, cursor.getInt(TYPE_INDEX));
            assertEquals(0, cursor.getInt(NEW_INDEX));
            assertEquals(insertCallsName, cursor.getString(CACHED_NAME_INDEX));
            assertEquals(Phones.TYPE_HOME, cursor.getInt(CACHED_NUMBER_TYPE_INDEX));
            assertEquals(insertCallsNumberLabel, cursor.getString(CACHED_NUMBER_LABEL_INDEX));
            int id = cursor.getInt(ID_INDEX);
            cursor.close();
            int now = (int) new Date().getTime();
            value.clear();
            value.put(Calls.NUMBER, updateCallsNumber);
            value.put(Calls.DATE, now);
            value.put(Calls.DURATION, updateCallsDuration);
            value.put(Calls.TYPE, Calls.MISSED_TYPE);
            value.put(Calls.NEW, 1);
            value.put(Calls.CACHED_NAME, updateCallsName);
            value.put(Calls.CACHED_NUMBER_TYPE, Phones.TYPE_CUSTOM);
            value.put(Calls.CACHED_NUMBER_LABEL, updateCallsNumberLabel);
            mCallLogProvider.update(uri, value, null, null);
            cursor = mCallLogProvider.query(Calls.CONTENT_URI, CALLS_PROJECTION,
                    Calls._ID + " = " + id, null, null);
            assertTrue(cursor.moveToNext());
            assertEquals(updateCallsNumber, cursor.getString(NUMBER_INDEX));
            assertEquals(now, cursor.getInt(DATE_INDEX));
            assertEquals(updateCallsDuration, cursor.getInt(DURATION_INDEX));
            assertEquals(Calls.MISSED_TYPE, cursor.getInt(TYPE_INDEX));
            assertEquals(1, cursor.getInt(NEW_INDEX));
            assertEquals(updateCallsName, cursor.getString(CACHED_NAME_INDEX));
            assertEquals(Phones.TYPE_CUSTOM, cursor.getInt(CACHED_NUMBER_TYPE_INDEX));
            assertEquals(updateCallsNumberLabel, cursor.getString(CACHED_NUMBER_LABEL_INDEX));
            cursor.close();
            mCallLogProvider.delete(Calls.CONTENT_URI, Calls._ID + " = " + id, null);
            cursor = mCallLogProvider.query(Calls.CONTENT_URI, CALLS_PROJECTION,
                    Calls._ID + " = " + id, null, null);
            assertEquals(0, cursor.getCount());
            cursor.close();
        } catch (RemoteException e) {
            fail("Unexpected RemoteException");
        }
    }
    public void testContactMethodsTable() {
        final String[] CONTACT_METHODS_PROJECTION = new String[] {
                ContactMethods._ID, ContactMethods.PERSON_ID, ContactMethods.KIND,
                ContactMethods.DATA, ContactMethods.AUX_DATA, ContactMethods.TYPE,
                ContactMethods.LABEL, ContactMethods.ISPRIMARY};
        final int ID_INDEX = 0;
        final int PERSON_ID_INDEX = 1;
        final int KIND_INDEX = 2;
        final int DATA_INDEX = 3;
        final int AUX_DATA_INDEX = 4;
        final int TYPE_INDEX = 5;
        final int LABEL_INDEX = 6;
        final int ISPRIMARY_INDEX = 7;
        int insertKind = Contacts.KIND_EMAIL;
        String insertData = "sample@gmail.com";
        String insertAuxData = "auxiliary_data_insert";
        String updateData = "elpmas@liamg.com";
        String updateAuxData = "auxiliary_data_update";
        String customLabel = "custom_label";
        try {
            ContentValues value = new ContentValues();
            value.put(PeopleColumns.NAME, "name_contact_methods_test_stub");
            Uri peopleUri = mProvider.insert(People.CONTENT_URI, value);
            int peopleId = Integer.parseInt(peopleUri.getPathSegments().get(1));
            value.clear();
            value.put(ContactMethods.PERSON_ID, peopleId);
            value.put(ContactMethods.KIND, insertKind);
            value.put(ContactMethods.DATA, insertData);
            value.put(ContactMethods.AUX_DATA, insertAuxData);
            value.put(ContactMethods.TYPE, ContactMethods.TYPE_WORK);
            value.put(ContactMethods.ISPRIMARY, 1);
            Uri uri = mProvider.insert(ContactMethods.CONTENT_URI, value);
            Cursor cursor = mProvider.query(
                    ContactMethods.CONTENT_URI, CONTACT_METHODS_PROJECTION,
                    ContactMethods.PERSON_ID + " = " + peopleId,
                    null, null);
            assertTrue(cursor.moveToNext());
            assertEquals(peopleId, cursor.getInt(PERSON_ID_INDEX));
            assertEquals(insertKind, cursor.getInt(KIND_INDEX));
            assertEquals(insertData, cursor.getString(DATA_INDEX));
            assertEquals(insertAuxData, cursor.getString(AUX_DATA_INDEX));
            assertEquals(ContactMethods.TYPE_WORK, cursor.getInt(TYPE_INDEX));
            assertNull(cursor.getString(LABEL_INDEX));
            assertEquals(1, cursor.getInt(ISPRIMARY_INDEX));
            int id = cursor.getInt(ID_INDEX);
            cursor.close();
            value.clear();
            value.put(ContactMethods.DATA, updateData);
            value.put(ContactMethods.AUX_DATA, updateAuxData);
            value.put(ContactMethods.TYPE, ContactMethods.TYPE_CUSTOM);
            value.put(ContactMethods.LABEL, customLabel);
            value.put(ContactMethods.ISPRIMARY, 1);
            mProvider.update(uri, value, null, null);
            cursor = mProvider.query(ContactMethods.CONTENT_URI,
                    CONTACT_METHODS_PROJECTION,
                    "contact_methods._id" + " = " + id, null, null);
            assertTrue(cursor.moveToNext());
            assertEquals(peopleId, cursor.getInt(PERSON_ID_INDEX));
            assertEquals(updateData, cursor.getString(DATA_INDEX));
            assertEquals(updateAuxData, cursor.getString(AUX_DATA_INDEX));
            assertEquals(ContactMethods.TYPE_CUSTOM, cursor.getInt(TYPE_INDEX));
            assertEquals(customLabel, cursor.getString(LABEL_INDEX));
            assertEquals(1, cursor.getInt(ISPRIMARY_INDEX));
            cursor.close();
            mProvider.delete(uri, null, null);
            cursor = mProvider.query(ContactMethods.CONTENT_URI,
                    CONTACT_METHODS_PROJECTION,
                    "contact_methods._id" + " = " + id, null, null);
            assertEquals(0, cursor.getCount());
            cursor.close();
            mProvider.delete(peopleUri, null, null);
        } catch (RemoteException e) {
            fail("Unexpected RemoteException");
        }
    }
    public void testSettingsTable() {
        final String[] SETTINGS_PROJECTION = new String[] {
                Settings._ID, Settings._SYNC_ACCOUNT, Settings._SYNC_ACCOUNT_TYPE,
                Settings.KEY, Settings.VALUE};
        final int ID_INDEX = 0;
        final int SYNC_ACCOUNT_NAME_INDEX = 1;
        final int SYNC_ACCOUNT_TYPE_INDEX = 2;
        final int KEY_INDEX = 3;
        final int VALUE_INDEX = 4;
        String insertKey = "key_insert";
        String insertValue = "value_insert";
        String updateKey = "key_update";
        String updateValue = "value_update";
        try {
            ContentValues value = new ContentValues();
            value.put(Settings.KEY, insertKey);
            value.put(Settings.VALUE, insertValue);
            try {
                mProvider.insert(Settings.CONTENT_URI, value);
                fail("Should throw out UnsupportedOperationException.");
            } catch (UnsupportedOperationException e) {
            }
            Settings.setSetting(mContentResolver, null, insertKey, insertValue);
            Cursor cursor = mProvider.query(
                    Settings.CONTENT_URI, SETTINGS_PROJECTION,
                    Settings.KEY + " = ?",
                    new String[] {insertKey}, null);
            assertTrue(cursor.moveToNext());
            assertNull(cursor.getString(SYNC_ACCOUNT_NAME_INDEX));
            assertNull(cursor.getString(SYNC_ACCOUNT_TYPE_INDEX));
            assertEquals(insertKey, cursor.getString(KEY_INDEX));
            assertEquals(insertValue, cursor.getString(VALUE_INDEX));
            int id = cursor.getInt(ID_INDEX);
            cursor.close();
            value.clear();
            value.put(Settings.KEY, updateKey);
            value.put(Settings.VALUE, updateValue);
            mProvider.update(Settings.CONTENT_URI, value, null, null);
            cursor = mProvider.query(
                    Settings.CONTENT_URI, SETTINGS_PROJECTION,
                    Settings.KEY + " = ?",
                    new String[] {updateKey}, null);
            assertTrue(cursor.moveToNext());
            assertNull(cursor.getString(SYNC_ACCOUNT_NAME_INDEX));
            assertNull(cursor.getString(SYNC_ACCOUNT_TYPE_INDEX));
            assertEquals(updateKey, cursor.getString(KEY_INDEX));
            assertEquals(updateValue, cursor.getString(VALUE_INDEX));
            cursor.close();
            cursor = mProvider.query(
                    Settings.CONTENT_URI, SETTINGS_PROJECTION,
                    Settings.KEY + " = ?",
                    new String[] {insertKey}, null);
            assertTrue(cursor.moveToNext());
            assertNull(cursor.getString(SYNC_ACCOUNT_NAME_INDEX));
            assertNull(cursor.getString(SYNC_ACCOUNT_TYPE_INDEX));
            assertEquals(insertKey, cursor.getString(KEY_INDEX));
            assertEquals(insertValue, cursor.getString(VALUE_INDEX));
            cursor.close();
            value.clear();
            value.put(Settings.KEY, insertKey);
            value.put(Settings.VALUE, updateValue);
            mProvider.update(Settings.CONTENT_URI, value, null, null);
            cursor = mProvider.query(
                    Settings.CONTENT_URI, SETTINGS_PROJECTION,
                    Settings.KEY + " = ?",
                    new String[] {insertKey}, null);
            assertTrue(cursor.moveToNext());
            assertNull(cursor.getString(SYNC_ACCOUNT_NAME_INDEX));
            assertNull(cursor.getString(SYNC_ACCOUNT_TYPE_INDEX));
            assertEquals(insertKey, cursor.getString(KEY_INDEX));
            assertEquals(updateValue, cursor.getString(VALUE_INDEX));
            cursor.close();
            cursor = mProvider.query(
                    Settings.CONTENT_URI, SETTINGS_PROJECTION,
                    Settings.KEY + " = ?",
                    new String[] {updateKey}, null);
            assertTrue(cursor.moveToNext());
            assertNull(cursor.getString(SYNC_ACCOUNT_NAME_INDEX));
            assertNull(cursor.getString(SYNC_ACCOUNT_TYPE_INDEX));
            assertEquals(updateKey, cursor.getString(KEY_INDEX));
            assertEquals(updateValue, cursor.getString(VALUE_INDEX));
            cursor.close();
            try {
                mProvider.delete(Settings.CONTENT_URI, Settings._ID + " = " + id, null);
                fail("Should throw out UnsupportedOperationException.");
            } catch (UnsupportedOperationException e) {
            }
        } catch (RemoteException e) {
            fail("Unexpected RemoteException");
        }
    }
    public void testExtensionsTable() {
        final String[] EXTENSIONS_PROJECTION = new String[] {
                Extensions._ID, Extensions.NAME,
                Extensions.VALUE, Extensions.PERSON_ID};
        final int NAME_INDEX = 1;
        final int VALUE_INDEX = 2;
        final int PERSON_ID_INDEX = 3;
        String insertName = "name_insert";
        String insertValue = "value_insert";
        String updateName = "name_update";
        String updateValue = "value_update";
        try {
            ContentValues value = new ContentValues();
            value.put(PeopleColumns.NAME, "name_extensions_test_stub");
            Uri peopleUri = mProvider.insert(People.CONTENT_URI, value);
            int peopleId = Integer.parseInt(peopleUri.getPathSegments().get(1));
            value.clear();
            value.put(Extensions.NAME, insertName);
            value.put(Extensions.VALUE, insertValue);
            value.put(Extensions.PERSON_ID, peopleId);
            Uri uri = mProvider.insert(Extensions.CONTENT_URI, value);
            Cursor cursor = mProvider.query(
                    Extensions.CONTENT_URI, EXTENSIONS_PROJECTION,
                    Extensions.PERSON_ID + " = " + peopleId,
                    null, null);
            assertTrue(cursor.moveToNext());
            assertEquals(insertName, cursor.getString(NAME_INDEX));
            assertEquals(insertValue, cursor.getString(VALUE_INDEX));
            assertEquals(peopleId, cursor.getInt(PERSON_ID_INDEX));
            cursor.close();
            value.clear();
            value.put(Extensions.NAME, updateName);
            value.put(Settings.VALUE, updateValue);
            mProvider.update(uri, value, null, null);
            cursor = mProvider.query(Extensions.CONTENT_URI,
                    EXTENSIONS_PROJECTION,
                    Extensions.PERSON_ID + " = " + peopleId,
                    null, null);
            assertTrue(cursor.moveToNext());
            assertEquals(updateName, cursor.getString(NAME_INDEX));
            assertEquals(updateValue, cursor.getString(VALUE_INDEX));
            assertEquals(peopleId, cursor.getInt(PERSON_ID_INDEX));
            cursor.close();
            mProvider.delete(uri, null, null);
            cursor = mProvider.query(Extensions.CONTENT_URI,
                    EXTENSIONS_PROJECTION,
                    Extensions.PERSON_ID + " = " + peopleId,
                    null, null);
            assertEquals(0, cursor.getCount());
            cursor.close();
        } catch (RemoteException e) {
            fail("Unexpected RemoteException");
        }
    }
    @KnownFailure(value="bug 2258907, needs investigation")
    public void testGroupMembershipTable() {
        final String[] GROUP_MEMBERSHIP_PROJECTION = new String[] {
                GroupMembership._ID, GroupMembership.PERSON_ID,
                GroupMembership.GROUP_ID, GroupMembership.GROUP_SYNC_ACCOUNT,
                GroupMembership.GROUP_SYNC_ID};
        final int ID_INDEX = 0;
        final int PERSON_ID_INDEX = 1;
        final int GROUP_ID_INDEX = 2;
        final int GROUP_SYNC_ACCOUNT_INDEX = 3;
        final int GROUP_SYNC_ID_INDEX = 4;
        try {
            ContentValues value = new ContentValues();
            value.put(PeopleColumns.NAME, "name_group_membership_test_stub");
            Uri peopleUri = mProvider.insert(People.CONTENT_URI, value);
            int peopleId = Integer.parseInt(peopleUri.getPathSegments().get(1));
            value.clear();
            value.put(GroupsColumns.NAME, "name_group_membership_test_stub1");
            Uri groupUri1 = mProvider.insert(Groups.CONTENT_URI, value);
            int groupId1 = Integer.parseInt(groupUri1.getPathSegments().get(1));
            value.clear();
            value.put(GroupsColumns.NAME, "name_group_membership_test_stub2");
            Uri groupUri2 = mProvider.insert(Groups.CONTENT_URI, value);
            int groupId2 = Integer.parseInt(groupUri2.getPathSegments().get(1));
            value.clear();
            value.put(GroupMembership.PERSON_ID, peopleId);
            value.put(GroupMembership.GROUP_ID, groupId1);
            Uri uri = mProvider.insert(GroupMembership.CONTENT_URI, value);
            Cursor cursor = mProvider.query(
                    GroupMembership.CONTENT_URI, GROUP_MEMBERSHIP_PROJECTION,
                    GroupMembership.PERSON_ID + " = " + peopleId,
                    null, null);
            assertTrue(cursor.moveToNext());
            assertEquals(peopleId, cursor.getInt(PERSON_ID_INDEX));
            assertEquals(groupId1, cursor.getInt(GROUP_ID_INDEX));
            assertNull(cursor.getString(GROUP_SYNC_ACCOUNT_INDEX));
            assertNull(cursor.getString(GROUP_SYNC_ID_INDEX));
            int id = cursor.getInt(ID_INDEX);
            cursor.close();
            value.clear();
            value.put(GroupMembership.GROUP_ID, groupId2);
            try {
                mProvider.update(uri, value, null, null);
                fail("Should throw out UnsupportedOperationException.");
            } catch (UnsupportedOperationException e) {
            }
            mProvider.delete(uri, null, null);
            cursor = mProvider.query(GroupMembership.CONTENT_URI,
                    GROUP_MEMBERSHIP_PROJECTION,
                    "groupmembership._id" + " = " + id,
                    null, null);
            assertEquals(0, cursor.getCount());
            cursor.close();
            mProvider.delete(peopleUri, null, null);
            mProvider.delete(groupUri1, null, null);
            mProvider.delete(groupUri2, null, null);
        } catch (RemoteException e) {
            fail("Unexpected RemoteException");
        }
    }
}