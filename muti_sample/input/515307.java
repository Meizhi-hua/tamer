@TestTargetClass(android.provider.Contacts.People.class)
public class Contacts_PeopleTest extends InstrumentationTestCase {
    private ContentResolver mContentResolver;
    private IContentProvider mProvider;
    private ArrayList<Uri> mPeopleRowsAdded;
    private ArrayList<Uri> mGroupRowsAdded;
    private ArrayList<Uri> mRowsAdded;
    private static final String[] PEOPLE_PROJECTION = new String[] {
            People._ID,
            People.LAST_TIME_CONTACTED
        };
    private static final int PEOPLE_ID_INDEX = 0;
    private static final int PEOPLE_LAST_CONTACTED_INDEX = 1;
    private static final int MEMBERSHIP_PERSON_ID_INDEX = 1;
    private static final int MEMBERSHIP_GROUP_ID_INDEX = 7;
    private static final String[] GROUPS_PROJECTION = new String[] {
        Groups._ID,
        Groups.NAME
    };
    private static final int GROUPS_ID_INDEX = 0;
    private static final int GROUPS_NAME_INDEX = 1;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContentResolver = getInstrumentation().getTargetContext().getContentResolver();
        mProvider = mContentResolver.acquireProvider(Contacts.AUTHORITY);
        mPeopleRowsAdded = new ArrayList<Uri>();
        mGroupRowsAdded = new ArrayList<Uri>();
        mRowsAdded = new ArrayList<Uri>();
        for (int i=0; i<3; i++) {
            ContentValues value = new ContentValues();
            value.put(People.NAME, "test_people_" + i);
            value.put(People.TIMES_CONTACTED, 0);
            value.put(People.LAST_TIME_CONTACTED, 0);
            mPeopleRowsAdded.add(mProvider.insert(People.CONTENT_URI, value));
        }
        ContentValues value = new ContentValues();
        value.put(Groups.NAME, "test_group_0");
        mGroupRowsAdded.add(mProvider.insert(Groups.CONTENT_URI, value));
        value.put(Groups.NAME, "test_group_1");
        mGroupRowsAdded.add(mProvider.insert(Groups.CONTENT_URI, value));
    }
    @Override
    protected void tearDown() throws Exception {
        for (Uri row : mRowsAdded) {
            mProvider.delete(row, null, null);
        }
        mRowsAdded.clear();
        for (Uri row : mPeopleRowsAdded) {
            mProvider.delete(row, null, null);
        }
        mPeopleRowsAdded.clear();
        for (Uri row : mGroupRowsAdded) {
            mProvider.delete(row, null, null);
        }
        mGroupRowsAdded.clear();
        super.tearDown();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test methods which add person to a group",
            method = "addToMyContactsGroup",
            args = {android.content.ContentResolver.class, long.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test methods which add person to a group",
            method = "addToGroup",
            args = {android.content.ContentResolver.class, long.class, long.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test methods which add person to a group",
            method = "addToGroup",
            args = {android.content.ContentResolver.class, long.class, java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test methods which add person to a group",
            method = "queryGroups",
            args = {android.content.ContentResolver.class, long.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test methods which add person to a group",
            method = "createPersonInMyContactsGroup",
            args = {android.content.ContentResolver.class, android.content.ContentValues.class}
        )
    })
    @BrokenTest("GROUP_MY_CONTACTS does not exist")
    public void testAddToGroup() {
        Cursor cursor;
        try {
            cursor = mProvider.query(mPeopleRowsAdded.get(0), PEOPLE_PROJECTION,
                    null, null, null);
            cursor.moveToFirst();
            int personId = cursor.getInt(PEOPLE_ID_INDEX);
            cursor.close();
            mRowsAdded.add(People.addToMyContactsGroup(mContentResolver, personId));
            cursor = mProvider.query(Groups.CONTENT_URI, GROUPS_PROJECTION,
                    Groups.NAME + "='" + Groups.GROUP_MY_CONTACTS + "'", null, null);
            cursor.moveToFirst();
            int groupId = cursor.getInt(GROUPS_ID_INDEX);
            cursor.close();
            cursor = People.queryGroups(mContentResolver, personId);
            cursor.moveToFirst();
            assertEquals(personId, cursor.getInt(MEMBERSHIP_PERSON_ID_INDEX));
            assertEquals(groupId, cursor.getInt(MEMBERSHIP_GROUP_ID_INDEX));
            cursor.close();
            ContentValues values = new ContentValues();
            values.put(People.NAME, "test_people_create");
            values.put(People.TIMES_CONTACTED, 0);
            values.put(People.LAST_TIME_CONTACTED, 0);
            mRowsAdded.add(People.createPersonInMyContactsGroup(mContentResolver, values));
            cursor = mProvider.query(People.CONTENT_URI, PEOPLE_PROJECTION,
                    People.NAME + " = 'test_people_create'", null, null);
            cursor.moveToFirst();
            personId = cursor.getInt(PEOPLE_ID_INDEX);
            mRowsAdded.add(ContentUris.withAppendedId(People.CONTENT_URI, personId));
            cursor.close();
            cursor = mProvider.query(Groups.CONTENT_URI, GROUPS_PROJECTION,
                    Groups.NAME + "='" + Groups.GROUP_MY_CONTACTS + "'", null, null);
            cursor.moveToFirst();
            groupId = cursor.getInt(GROUPS_ID_INDEX);
            cursor.close();
            cursor = People.queryGroups(mContentResolver, personId);
            cursor.moveToFirst();
            assertEquals(personId, cursor.getInt(MEMBERSHIP_PERSON_ID_INDEX));
            assertEquals(groupId, cursor.getInt(MEMBERSHIP_GROUP_ID_INDEX));
            cursor.close();
            cursor = mProvider.query(mPeopleRowsAdded.get(1), PEOPLE_PROJECTION,
                    null, null, null);
            cursor.moveToFirst();
            personId = cursor.getInt(PEOPLE_ID_INDEX);
            cursor.close();
            cursor = mProvider.query(mGroupRowsAdded.get(0), GROUPS_PROJECTION,
                    null, null, null);
            cursor.moveToFirst();
            groupId = cursor.getInt(GROUPS_ID_INDEX);
            cursor.close();
            mRowsAdded.add(People.addToGroup(mContentResolver, personId, groupId));
            cursor = People.queryGroups(mContentResolver, personId);
            cursor.moveToFirst();
            assertEquals(personId, cursor.getInt(MEMBERSHIP_PERSON_ID_INDEX));
            assertEquals(groupId, cursor.getInt(MEMBERSHIP_GROUP_ID_INDEX));
            cursor.close();
            cursor = mProvider.query(mPeopleRowsAdded.get(2), PEOPLE_PROJECTION,
                    null, null, null);
            cursor.moveToFirst();
            personId = cursor.getInt(PEOPLE_ID_INDEX);
            cursor.close();
            String groupName = "test_group_1";
            mRowsAdded.add(People.addToGroup(mContentResolver, personId, groupName));
            cursor = People.queryGroups(mContentResolver, personId);
            cursor.moveToFirst();
            assertEquals(personId, cursor.getInt(MEMBERSHIP_PERSON_ID_INDEX));
            groupId = cursor.getInt(MEMBERSHIP_GROUP_ID_INDEX);
            cursor.close();
            cursor = mProvider.query(Groups.CONTENT_URI, GROUPS_PROJECTION,
                    Groups._ID + "=" + groupId, null, null);
            cursor.moveToFirst();
            assertEquals(groupName, cursor.getString(GROUPS_NAME_INDEX));
            cursor.close();
        } catch (RemoteException e) {
            fail("Unexpected RemoteException");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test markAsContacted(ContentResolver resolver, long personId)",
        method = "markAsContacted",
        args = {android.content.ContentResolver.class, long.class}
    )
    public void testMarkAsContacted() {
        Cursor cursor;
        try {
            cursor = mProvider.query(mPeopleRowsAdded.get(0), PEOPLE_PROJECTION,
                    null, null, null);
            cursor.moveToFirst();
            int personId = cursor.getInt(PEOPLE_ID_INDEX);
            long oldLastContacted = cursor.getLong(PEOPLE_LAST_CONTACTED_INDEX);
            cursor.close();
            People.markAsContacted(mContentResolver, personId);
            cursor = mProvider.query(mPeopleRowsAdded.get(0), PEOPLE_PROJECTION,
                    null, null, null);
            cursor.moveToFirst();
            long lastContacted = cursor.getLong(PEOPLE_LAST_CONTACTED_INDEX);
            assertTrue(oldLastContacted < lastContacted);
            oldLastContacted = lastContacted;
            cursor.close();
            People.markAsContacted(mContentResolver, personId);
            cursor = mProvider.query(mPeopleRowsAdded.get(0), PEOPLE_PROJECTION,
                    null, null, null);
            cursor.moveToFirst();
            lastContacted = cursor.getLong(PEOPLE_LAST_CONTACTED_INDEX);
            assertTrue(oldLastContacted < lastContacted);
            cursor.close();
        } catch (RemoteException e) {
            fail("Unexpected RemoteException");
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test methods access the photo data of person",
            method = "setPhotoData",
            args = {android.content.ContentResolver.class, android.net.Uri.class, byte[].class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test methods access the photo data of person",
            method = "loadContactPhoto",
            args = {android.content.Context.class, android.net.Uri.class, int.class, 
                    android.graphics.BitmapFactory.Options.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test methods access the photo data of person",
            method = "openContactPhotoInputStream",
            args = {android.content.ContentResolver.class, android.net.Uri.class}
        )
    })
    @BrokenTest("photoStream is null after setting photo data")
    public void testAccessPhotoData() {
        Context context = getInstrumentation().getTargetContext();
        try {
            InputStream inputStream = context.getResources().openRawResource(
                    com.android.cts.stub.R.drawable.testimage);
            int size = inputStream.available();
            byte[] data =  new byte[size];
            inputStream.read(data);
            People.setPhotoData(mContentResolver, mPeopleRowsAdded.get(0), data);
            InputStream photoStream = People.openContactPhotoInputStream(
                    mContentResolver, mPeopleRowsAdded.get(0));
            assertNotNull(photoStream);
            Bitmap bitmap = BitmapFactory.decodeStream(photoStream, null, null);
            assertEquals(212, bitmap.getWidth());
            assertEquals(142, bitmap.getHeight());
            photoStream = People.openContactPhotoInputStream(mContentResolver,
                    mPeopleRowsAdded.get(1));
            assertNull(photoStream);
            bitmap = People.loadContactPhoto(context, mPeopleRowsAdded.get(0),
                    com.android.cts.stub.R.drawable.size_48x48, null);
            assertEquals(212, bitmap.getWidth());
            assertEquals(142, bitmap.getHeight());
            bitmap = People.loadContactPhoto(context, null,
                    com.android.cts.stub.R.drawable.size_48x48, null);
            assertEquals(48, bitmap.getWidth());
            assertEquals(48, bitmap.getHeight());
        } catch (IOException e) {
            fail("Unexpected IOException");
        }
    }
}
