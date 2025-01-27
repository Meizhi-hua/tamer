public class GroupsTest extends BaseContactsProvider2Test {
    private static final String GROUP_GREY = "Grey";
    private static final String GROUP_RED = "Red";
    private static final String GROUP_GREEN = "Green";
    private static final String GROUP_BLUE = "Blue";
    private static final String PERSON_ALPHA = "Alpha";
    private static final String PERSON_BRAVO = "Bravo";
    private static final String PERSON_CHARLIE = "Charlie";
    private static final String PERSON_DELTA = "Delta";
    private static final String PHONE_ALPHA = "555-1111";
    private static final String PHONE_BRAVO_1 = "555-2222";
    private static final String PHONE_BRAVO_2 = "555-3333";
    private static final String PHONE_CHARLIE_1 = "555-4444";
    private static final String PHONE_CHARLIE_2 = "555-5555";
    @LargeTest
    public void testGroupSummary() {
        ((SynchronousContactsProvider2)mActor.provider).wipeData();
        long groupGrey = mActor.createGroup(GROUP_GREY);
        long groupRed = mActor.createGroup(GROUP_RED);
        long groupGreen = mActor.createGroup(GROUP_GREEN);
        long groupBlue = mActor.createGroup(GROUP_BLUE);
        long contactAlpha = mActor.createContact(false, PERSON_ALPHA);
        long contactBravo = mActor.createContact(false, PERSON_BRAVO);
        long contactCharlie = mActor.createContact(false, PERSON_CHARLIE);
        long contactCharlieDupe = mActor.createContact(false, PERSON_CHARLIE);
        long contactDelta = mActor.createContact(false, PERSON_DELTA);
        assertAggregated(contactCharlie, contactCharlieDupe);
        mActor.createPhone(contactAlpha, PHONE_ALPHA);
        mActor.createPhone(contactBravo, PHONE_BRAVO_1);
        mActor.createPhone(contactBravo, PHONE_BRAVO_2);
        mActor.createPhone(contactCharlie, PHONE_CHARLIE_1);
        mActor.createPhone(contactCharlieDupe, PHONE_CHARLIE_2);
        mActor.createGroupMembership(contactAlpha, groupGrey);
        mActor.createGroupMembership(contactBravo, groupGrey);
        mActor.createGroupMembership(contactCharlie, groupGrey);
        mActor.createGroupMembership(contactDelta, groupGrey);
        mActor.createGroupMembership(contactAlpha, groupRed);
        mActor.createGroupMembership(contactBravo, groupRed);
        mActor.createGroupMembership(contactCharlie, groupRed);
        mActor.createGroupMembership(contactDelta, groupGreen);
        final Cursor cursor = mActor.resolver.query(Groups.CONTENT_SUMMARY_URI,
                Projections.PROJ_SUMMARY, null, null, null);
        assertTrue("Didn't return summary for all groups", (cursor.getCount() == 4));
        while (cursor.moveToNext()) {
            final long groupId = cursor.getLong(Projections.COL_ID);
            final int summaryCount = cursor.getInt(Projections.COL_SUMMARY_COUNT);
            final int summaryWithPhones = cursor.getInt(Projections.COL_SUMMARY_WITH_PHONES);
            if (groupId == groupGrey) {
                assertEquals("Incorrect Grey count", 4, summaryCount);
                assertEquals("Incorrect Grey with phones count", 3, summaryWithPhones);
            } else if (groupId == groupRed) {
                assertEquals("Incorrect Red count", 3, summaryCount);
                assertEquals("Incorrect Red with phones count", 3, summaryWithPhones);
            } else if (groupId == groupGreen) {
                assertEquals("Incorrect Green count", 1, summaryCount);
                assertEquals("Incorrect Green with phones count", 0, summaryWithPhones);
            } else if (groupId == groupBlue) {
                assertEquals("Incorrect Blue count", 0, summaryCount);
                assertEquals("Incorrect Blue with phones count", 0, summaryWithPhones);
            } else {
                fail("Unrecognized group in summary cursor");
            }
        }
        cursor.close();
    }
    @MediumTest
    public void testGroupDirtySetOnChange() {
        Uri uri = ContentUris.withAppendedId(Groups.CONTENT_URI,
                createGroup(mAccount, "gsid1", "title1"));
        assertDirty(uri, true);
        clearDirty(uri);
        assertDirty(uri, false);
    }
    @MediumTest
    public void testMarkAsDirtyParameter() {
        Uri uri = ContentUris.withAppendedId(Groups.CONTENT_URI,
                createGroup(mAccount, "gsid1", "title1"));
        clearDirty(uri);
        Uri updateUri = setCallerIsSyncAdapter(uri, mAccount);
        ContentValues values = new ContentValues();
        values.put(Groups.NOTES, "New notes");
        mResolver.update(updateUri, values, null, null);
        assertDirty(uri, false);
    }
    @MediumTest
    public void testGroupDirtyClearedWhenSetExplicitly() {
        Uri uri = ContentUris.withAppendedId(Groups.CONTENT_URI,
                createGroup(mAccount, "gsid1", "title1"));
        assertDirty(uri, true);
        ContentValues values = new ContentValues();
        values.put(Groups.DIRTY, 0);
        values.put(Groups.NOTES, "other notes");
        assertEquals(1, mResolver.update(uri, values, null, null));
        assertDirty(uri, false);
    }
    @MediumTest
    public void testGroupDeletion1() {
        long groupId = createGroup(mAccount, "g1", "gt1");
        Uri uri = ContentUris.withAppendedId(Groups.CONTENT_URI, groupId);
        assertEquals(1, getCount(uri, null, null));
        mResolver.delete(uri, null, null);
        assertEquals(1, getCount(uri, null, null));
        assertStoredValue(uri, Groups.DELETED, "1");
        Uri permanentDeletionUri = setCallerIsSyncAdapter(uri, mAccount);
        mResolver.delete(permanentDeletionUri, null, null);
        assertEquals(0, getCount(uri, null, null));
    }
    @MediumTest
    public void testGroupDeletion2() {
        long groupId = createGroup(mAccount, "g1", "gt1");
        Uri uri = ContentUris.withAppendedId(Groups.CONTENT_URI, groupId);
        assertEquals(1, getCount(uri, null, null));
        Uri permanentDeletionUri = setCallerIsSyncAdapter(uri, mAccount);
        mResolver.delete(permanentDeletionUri, null, null);
        assertEquals(0, getCount(uri, null, null));
    }
    @MediumTest
    public void testGroupVersionUpdates() {
        Uri uri = ContentUris.withAppendedId(Groups.CONTENT_URI,
                createGroup(mAccount, "gsid1", "title1"));
        long version = getVersion(uri);
        ContentValues values = new ContentValues();
        values.put(Groups.TITLE, "title2");
        mResolver.update(uri, values, null, null);
        assertEquals(version + 1, getVersion(uri));
    }
    private interface Projections {
        public static final String[] PROJ_SUMMARY = new String[] {
            Groups._ID,
            Groups.SUMMARY_COUNT,
            Groups.SUMMARY_WITH_PHONES,
        };
        public static final int COL_ID = 0;
        public static final int COL_SUMMARY_COUNT = 1;
        public static final int COL_SUMMARY_WITH_PHONES = 2;
    }
    private static final Account sTestAccount = new Account("user@example.com", "com.example");
    private static final Account sSecondAccount = new Account("other@example.net", "net.example");
    private static final String GROUP_ID = "testgroup";
    public void assertRawContactVisible(long rawContactId, boolean expected) {
        final long contactId = this.queryContactId(rawContactId);
        assertContactVisible(contactId, expected);
    }
    public void assertContactVisible(long contactId, boolean expected) {
        final Cursor cursor = mResolver.query(Contacts.CONTENT_URI, new String[] {
            Contacts.IN_VISIBLE_GROUP
        }, Contacts._ID + "=" + contactId, null, null);
        assertTrue("Contact not found", cursor.moveToFirst());
        final boolean actual = (cursor.getInt(0) != 0);
        cursor.close();
        assertEquals("Unexpected visibility", expected, actual);
    }
    public ContentProviderOperation buildVisibleAssert(long contactId, boolean visible) {
        return ContentProviderOperation.newAssertQuery(Contacts.CONTENT_URI).withSelection(
                Contacts._ID + "=" + contactId + " AND " + Contacts.IN_VISIBLE_GROUP + "="
                        + (visible ? 1 : 0), null).withExpectedCount(1).build();
    }
    @LargeTest
    public void testDelayVisibleTransaction() throws RemoteException, OperationApplicationException {
        final ContentValues values = new ContentValues();
        final long groupId = this.createGroup(sTestAccount, GROUP_ID, GROUP_ID, 1);
        final Uri groupUri = ContentUris.withAppendedId(Groups.CONTENT_URI, groupId);
        final long rawContactId = this.createRawContact(sTestAccount);
        final long contactId = this.queryContactId(rawContactId);
        final Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId);
        this.insertGroupMembership(rawContactId, groupId);
        final ArrayList<ContentProviderOperation> oper = Lists.newArrayList();
        oper.add(buildVisibleAssert(contactId, true));
        oper.add(ContentProviderOperation.newUpdate(groupUri).withValue(Groups.GROUP_VISIBLE, 0)
                .build());
        oper.add(buildVisibleAssert(contactId, true));
        mResolver.applyBatch(ContactsContract.AUTHORITY, oper);
        oper.clear();
        oper.add(buildVisibleAssert(contactId, false));
        mResolver.applyBatch(ContactsContract.AUTHORITY, oper);
    }
    public void testLocalSingleVisible() {
        final long rawContactId = this.createRawContact();
        assertRawContactVisible(rawContactId, true);
    }
    public void testLocalMixedVisible() {
        final long rawContactId1 = this.createRawContact();
        final long rawContactId2 = this.createRawContact(sTestAccount);
        final long groupId = this.createGroup(sTestAccount, GROUP_ID, GROUP_ID, 0);
        this.insertGroupMembership(rawContactId2, groupId);
        assertNotAggregated(rawContactId1, rawContactId2);
        assertRawContactVisible(rawContactId1, true);
        assertRawContactVisible(rawContactId2, false);
        final ContentValues values = new ContentValues();
        values.put(AggregationExceptions.TYPE, AggregationExceptions.TYPE_KEEP_TOGETHER);
        values.put(AggregationExceptions.RAW_CONTACT_ID1, rawContactId1);
        values.put(AggregationExceptions.RAW_CONTACT_ID2, rawContactId2);
        mResolver.update(AggregationExceptions.CONTENT_URI, values, null, null);
        assertRawContactVisible(rawContactId1, true);
        assertRawContactVisible(rawContactId2, true);
    }
    public void testUngroupedVisible() {
        final long rawContactId = this.createRawContact(sTestAccount);
        final ContentValues values = new ContentValues();
        values.put(Settings.ACCOUNT_NAME, sTestAccount.name);
        values.put(Settings.ACCOUNT_TYPE, sTestAccount.type);
        values.put(Settings.UNGROUPED_VISIBLE, 0);
        mResolver.insert(Settings.CONTENT_URI, values);
        assertRawContactVisible(rawContactId, false);
        values.clear();
        values.put(Settings.UNGROUPED_VISIBLE, 1);
        mResolver.update(Settings.CONTENT_URI, values, Settings.ACCOUNT_NAME + "=? AND "
                + Settings.ACCOUNT_TYPE + "=?", new String[] {
                sTestAccount.name, sTestAccount.type
        });
        assertRawContactVisible(rawContactId, true);
    }
    public void testMultipleSourcesVisible() {
        final long rawContactId1 = this.createRawContact(sTestAccount);
        final long rawContactId2 = this.createRawContact(sSecondAccount);
        final long groupId = this.createGroup(sTestAccount, GROUP_ID, GROUP_ID, 0);
        this.insertGroupMembership(rawContactId1, groupId);
        assertRawContactVisible(rawContactId1, false);
        assertRawContactVisible(rawContactId2, false);
        final ContentValues values = new ContentValues();
        values.put(Groups.GROUP_VISIBLE, 1);
        mResolver.update(Groups.CONTENT_URI, values, Groups._ID + "=" + groupId, null);
        assertRawContactVisible(rawContactId1, true);
        assertRawContactVisible(rawContactId2, false);
        values.clear();
        values.put(AggregationExceptions.TYPE, AggregationExceptions.TYPE_KEEP_TOGETHER);
        values.put(AggregationExceptions.RAW_CONTACT_ID1, rawContactId1);
        values.put(AggregationExceptions.RAW_CONTACT_ID2, rawContactId2);
        mResolver.update(AggregationExceptions.CONTENT_URI, values, null, null);
        assertRawContactVisible(rawContactId1, true);
        assertRawContactVisible(rawContactId2, true);
        values.clear();
        values.put(Groups.GROUP_VISIBLE, 0);
        mResolver.update(Groups.CONTENT_URI, values, Groups._ID + "=" + groupId, null);
        assertRawContactVisible(rawContactId1, false);
        assertRawContactVisible(rawContactId2, false);
        values.clear();
        values.put(Settings.ACCOUNT_NAME, sTestAccount.name);
        values.put(Settings.ACCOUNT_TYPE, sTestAccount.type);
        values.put(Settings.UNGROUPED_VISIBLE, 1);
        mResolver.insert(Settings.CONTENT_URI, values);
        assertRawContactVisible(rawContactId1, false);
        assertRawContactVisible(rawContactId2, false);
        values.clear();
        values.put(Settings.ACCOUNT_NAME, sSecondAccount.name);
        values.put(Settings.ACCOUNT_TYPE, sSecondAccount.type);
        values.put(Settings.UNGROUPED_VISIBLE, 1);
        mResolver.insert(Settings.CONTENT_URI, values);
        assertRawContactVisible(rawContactId1, true);
        assertRawContactVisible(rawContactId2, true);
    }
}
