public class LocalStoreUnitTests extends AndroidTestCase {
    public static final String DB_NAME = "com.android.email.mail.store.LocalStoreUnitTests.db";
    private static final String SENDER = "sender@android.com";
    private static final String RECIPIENT_TO = "recipient-to@android.com";
    private static final String SUBJECT = "This is the subject";
    private static final String BODY = "This is the body.  This is also the body.";
    private static final String MESSAGE_ID = "Test-Message-ID";
    private static final String MESSAGE_ID_2 = "Test-Message-ID-Second";
    private static final int DATABASE_VERSION = 24;
    private static final String FOLDER_NAME = "TEST";
    private static final String MISSING_FOLDER_NAME = "TEST-NO-FOLDER";
    private String mLocalStoreUri = null;
    private LocalStore mStore = null;
    private LocalStore.LocalFolder mFolder = null;
    private File mCacheDir;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mLocalStoreUri = "local:
        mStore = (LocalStore) LocalStore.newInstance(mLocalStoreUri, getContext(), null);
        mFolder = (LocalStore.LocalFolder) mStore.getFolder(FOLDER_NAME);
        mCacheDir = getContext().getCacheDir();
        BinaryTempFileBody.setTempDirectory(mCacheDir);
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if (mFolder != null) {
            mFolder.close(false);
        }
        if (mStore != null) {
            mStore.delete();
        }
        URI uri = new URI(mLocalStoreUri);
        String path = uri.getPath();
        File attachmentsDir = new File(path + "_att");
        try {
            File[] attachments = attachmentsDir.listFiles();
            for (File attachment : attachments) {
                if (attachment.exists()) {
                    attachment.delete();
                }
            }
        } catch (RuntimeException e) { }
        try {
            if (attachmentsDir.exists()) {
                attachmentsDir.delete();
            }
        } catch (RuntimeException e) { }
        try {
            new File(path).delete();
        }
        catch (RuntimeException e) { }
    }
    public void testMessageId_1() throws MessagingException {
        final MimeMessage message = buildTestMessage(RECIPIENT_TO, SENDER, SUBJECT, BODY);
        message.setMessageId(MESSAGE_ID);
        mFolder.open(OpenMode.READ_WRITE, null);
        mFolder.appendMessages(new Message[]{ message });
        String localUid = message.getUid();
        MimeMessage retrieved = (MimeMessage) mFolder.getMessage(localUid);
        assertEquals(MESSAGE_ID, retrieved.getMessageId());
        Message[] retrievedArray = mFolder.getMessages(null);
        assertEquals(1, retrievedArray.length);
        MimeMessage retrievedEntry = (MimeMessage) retrievedArray[0];
        assertEquals(MESSAGE_ID, retrievedEntry.getMessageId());
    }
    public void testMessageId_2() throws MessagingException {
        final MimeMessage message = buildTestMessage(RECIPIENT_TO, SENDER, SUBJECT, BODY);
        message.setMessageId(MESSAGE_ID);
        mFolder.open(OpenMode.READ_WRITE, null);
        mFolder.appendMessages(new Message[]{ message });
        String localUid = message.getUid();
        MimeMessage retrieved = (MimeMessage) mFolder.getMessage(localUid);
        assertEquals(MESSAGE_ID, retrieved.getMessageId());
        retrieved.setMessageId(MESSAGE_ID_2);
        mFolder.updateMessage((LocalStore.LocalMessage)retrieved);
        Message[] retrievedArray = mFolder.getMessages(null);
        assertEquals(1, retrievedArray.length);
        MimeMessage retrievedEntry = (MimeMessage) retrievedArray[0];
        assertEquals(MESSAGE_ID_2, retrievedEntry.getMessageId());
    }
    private MimeMessage buildTestMessage(String to, String sender, String subject, String content) 
            throws MessagingException {
        MimeMessage message = new MimeMessage();
        if (to != null) {
            Address[] addresses = Address.parse(to);
            message.setRecipients(RecipientType.TO, addresses);
        }
        if (sender != null) {
            Address[] addresses = Address.parse(sender);
            message.setFrom(Address.parse(sender)[0]);
        }
        if (subject != null) {
            message.setSubject(subject);
        }
        if (content != null) {
            TextBody body = new TextBody(content);
            message.setBody(body);
        }
        return message;
    }
    public void testFetchModes() throws MessagingException {
        final String BODY_TEXT_PLAIN = "This is the body text.";
        final String BODY_TEXT_HTML = "But this is the HTML version of the body text.";
        MimeMessage message = buildTestMessage(RECIPIENT_TO, SENDER, SUBJECT, BODY);
        message.setMessageId(MESSAGE_ID);
        Body body = new MultipartBuilder("multipart/mixed")
            .addBodyPart(MessageTestUtils.bodyPart("image/tiff", "cid.4@android.com"))
            .addBodyPart(new MultipartBuilder("multipart/related")
                .addBodyPart(new MultipartBuilder("multipart/alternative")
                    .addBodyPart(MessageTestUtils.textPart("text/plain", BODY_TEXT_PLAIN))
                    .addBodyPart(MessageTestUtils.textPart("text/html", BODY_TEXT_HTML))
                    .buildBodyPart())
                .buildBodyPart())
            .addBodyPart(MessageTestUtils.bodyPart("image/gif", "cid.3@android.com"))
            .build();
        message.setBody(body);
        mFolder.open(OpenMode.READ_WRITE, null);
        mFolder.appendMessages(new Message[]{ message });
        Message[] messages = mFolder.getMessages(null);
        FetchProfile fp = new FetchProfile();
        fp.add(FetchProfile.Item.STRUCTURE);
        mFolder.fetch(messages, fp, null);
        Part textPart = MimeUtility.findFirstPartByMimeType(messages[0], "text/plain");
        Part htmlPart = MimeUtility.findFirstPartByMimeType(messages[0], "text/html");
        assertNull(MimeUtility.getTextFromPart(textPart));
        assertNull(MimeUtility.getTextFromPart(htmlPart));
        messages = mFolder.getMessages(null);
        fp.clear();
        fp.add(FetchProfile.Item.BODY);
        mFolder.fetch(messages, fp, null);
        textPart = MimeUtility.findFirstPartByMimeType(messages[0], "text/plain");
        htmlPart = MimeUtility.findFirstPartByMimeType(messages[0], "text/html");
        assertEquals(BODY_TEXT_PLAIN, MimeUtility.getTextFromPart(textPart));
        assertEquals(BODY_TEXT_HTML, MimeUtility.getTextFromPart(htmlPart));
    }
    public void testStorePersistentData() {
        final String TEST_KEY = "the.test.key";
        final String TEST_KEY_2 = "a.different.test.key";
        final String TEST_STRING = "This is the store's persistent data.";
        final String TEST_STRING_2 = "Rewrite the store data.";
        assertEquals("the-default", mStore.getPersistentString(TEST_KEY, "the-default"));
        mStore.setPersistentString(TEST_KEY, TEST_STRING);
        mStore.setPersistentString(TEST_KEY_2, TEST_STRING_2);
        assertEquals(TEST_STRING, mStore.getPersistentString(TEST_KEY, null));
        assertEquals(TEST_STRING_2, mStore.getPersistentString(TEST_KEY_2, null));
    }
    public void testStorePersistentCallbacks() throws MessagingException {
        final String TEST_KEY = "the.test.key";
        final String TEST_KEY_2 = "a.different.test.key";
        final String TEST_STRING = "This is the store's persistent data.";
        final String TEST_STRING_2 = "Rewrite the store data.";
        Store.PersistentDataCallbacks callbacks = mStore.getPersistentCallbacks();
        assertEquals("the-default", callbacks.getPersistentString(TEST_KEY, "the-default"));
        callbacks.setPersistentString(TEST_KEY, TEST_STRING);
        callbacks.setPersistentString(TEST_KEY_2, TEST_STRING_2);
        assertEquals(TEST_STRING, mStore.getPersistentString(TEST_KEY, null));
        assertEquals(TEST_STRING_2, mStore.getPersistentString(TEST_KEY_2, null));
    }
    public void testFolderPersistentStorage() throws MessagingException {
        mFolder.open(OpenMode.READ_WRITE, null);
        LocalStore.LocalFolder folder2 = (LocalStore.LocalFolder) mStore.getFolder("FOLDER-2");
        assertFalse(folder2.exists());
        folder2.create(FolderType.HOLDS_MESSAGES);
        folder2.open(OpenMode.READ_WRITE, null);
        Folder.PersistentDataCallbacks callbacks = mFolder.getPersistentCallbacks();
        Folder.PersistentDataCallbacks callbacks2 = folder2.getPersistentCallbacks();
        callbacks.setPersistentString("key1", "value-1-1");
        callbacks.setPersistentString("key2", "value-1-2");
        callbacks2.setPersistentString("key1", "value-2-1");
        callbacks2.setPersistentString("key2", "value-2-2");
        assertEquals("value-1-1", callbacks.getPersistentString("key1", null));
        assertEquals("value-1-2", callbacks.getPersistentString("key2", null));
        assertEquals("value-2-1", callbacks2.getPersistentString("key1", null));
        assertEquals("value-2-2", callbacks2.getPersistentString("key2", null));
        assertEquals("value-1-3", callbacks.getPersistentString("key3", "value-1-3"));
        assertEquals("value-2-3", callbacks2.getPersistentString("key3", "value-2-3"));
        callbacks.setPersistentString("key1", "value-1-1b");
        callbacks2.setPersistentString("key2", "value-2-2b");
        assertEquals("value-1-1b", callbacks.getPersistentString("key1", null));    
        assertEquals("value-1-2", callbacks.getPersistentString("key2", null));     
        assertEquals("value-2-1", callbacks2.getPersistentString("key1", null));    
        assertEquals("value-2-2b", callbacks2.getPersistentString("key2", null));   
    }
    public void testFolderPersistentBulkUpdate() throws MessagingException {
        mFolder.open(OpenMode.READ_WRITE, null);
        LocalStore.LocalFolder folder2 = (LocalStore.LocalFolder) mStore.getFolder("FOLDER-2");
        assertFalse(folder2.exists());
        folder2.create(FolderType.HOLDS_MESSAGES);
        folder2.open(OpenMode.READ_WRITE, null);
        Folder.PersistentDataCallbacks callbacks = mFolder.getPersistentCallbacks();
        Folder.PersistentDataCallbacks callbacks2 = folder2.getPersistentCallbacks();
        callbacks.setPersistentString("key1", "value-1-1");
        callbacks.setPersistentString("key2", "value-1-2");
        callbacks2.setPersistentString("key1", "value-2-1");
        callbacks2.setPersistentString("key2", "value-2-2");
        final MimeMessage message1 = buildTestMessage(RECIPIENT_TO, SENDER, SUBJECT, BODY);
        message1.setFlag(Flag.X_STORE_1, false);
        message1.setFlag(Flag.X_STORE_2, false);
        final MimeMessage message2 = buildTestMessage(RECIPIENT_TO, SENDER, SUBJECT, BODY);
        message2.setFlag(Flag.X_STORE_1, true);
        message2.setFlag(Flag.X_STORE_2, false);
        final MimeMessage message3 = buildTestMessage(RECIPIENT_TO, SENDER, SUBJECT, BODY);
        message3.setFlag(Flag.X_STORE_1, false);
        message3.setFlag(Flag.X_STORE_2, true);
        final MimeMessage message4 = buildTestMessage(RECIPIENT_TO, SENDER, SUBJECT, BODY);
        message4.setFlag(Flag.X_STORE_1, true);
        message4.setFlag(Flag.X_STORE_2, true);
        Message[] allOriginals = new Message[]{ message1, message2, message3, message4 };
        mFolder.appendMessages(allOriginals);
        callbacks.setPersistentStringAndMessageFlags("key1", "value-1-1a", 
                new Flag[]{ Flag.X_STORE_1 }, null);
        Message[] messages = mFolder.getMessages(null);
        for (Message msg : messages) {
            assertTrue(msg.isSet(Flag.X_STORE_1));
            if (msg.getUid().equals(message1.getUid())) assertFalse(msg.isSet(Flag.X_STORE_2));
            if (msg.getUid().equals(message2.getUid())) assertFalse(msg.isSet(Flag.X_STORE_2));
        }
        assertEquals("value-1-1a", callbacks.getPersistentString("key1", null));
        callbacks.setPersistentStringAndMessageFlags("key2", "value-1-2a", 
                null, new Flag[]{ Flag.X_STORE_2 });
        messages = mFolder.getMessages(null);
        for (Message msg : messages) {
            assertTrue(msg.isSet(Flag.X_STORE_1));
            assertFalse(msg.isSet(Flag.X_STORE_2));
        }
        assertEquals("value-1-2a", callbacks.getPersistentString("key2", null));        
    }
    public void testStoreFlags() throws MessagingException {
        final MimeMessage message = buildTestMessage(RECIPIENT_TO, SENDER, SUBJECT, BODY);
        message.setMessageId(MESSAGE_ID);
        message.setFlag(Flag.X_STORE_1, true);
        message.setFlag(Flag.X_STORE_2, false);
        mFolder.open(OpenMode.READ_WRITE, null);
        mFolder.appendMessages(new Message[]{ message });
        String localUid = message.getUid();
        MimeMessage retrieved = (MimeMessage) mFolder.getMessage(localUid);
        assertEquals(MESSAGE_ID, retrieved.getMessageId());
        assertTrue(message.isSet(Flag.X_STORE_1));
        assertFalse(message.isSet(Flag.X_STORE_2));
        retrieved.setFlag(Flag.X_STORE_1, false);
        retrieved.setFlag(Flag.X_STORE_2, true);
        mFolder.updateMessage((LocalStore.LocalMessage)retrieved);
        Message[] retrievedArray = mFolder.getMessages(null);
        assertEquals(1, retrievedArray.length);
        MimeMessage retrievedEntry = (MimeMessage) retrievedArray[0];
        assertEquals(MESSAGE_ID, retrieved.getMessageId());
        assertFalse(retrievedEntry.isSet(Flag.X_STORE_1));
        assertTrue(retrievedEntry.isSet(Flag.X_STORE_2));
    }
    public void testDownloadAndDeletedFlags() throws MessagingException {
        final MimeMessage message = buildTestMessage(RECIPIENT_TO, SENDER, SUBJECT, BODY);
        message.setMessageId(MESSAGE_ID);
        message.setFlag(Flag.X_STORE_1, true);
        message.setFlag(Flag.X_STORE_2, false);
        message.setFlag(Flag.X_DOWNLOADED_FULL, true);
        message.setFlag(Flag.X_DOWNLOADED_PARTIAL, false);
        message.setFlag(Flag.DELETED, false);
        mFolder.open(OpenMode.READ_WRITE, null);
        mFolder.appendMessages(new Message[]{ message });
        String localUid = message.getUid();
        MimeMessage retrieved = (MimeMessage) mFolder.getMessage(localUid);
        assertEquals(MESSAGE_ID, retrieved.getMessageId());
        assertTrue(retrieved.isSet(Flag.X_STORE_1));
        assertFalse(retrieved.isSet(Flag.X_STORE_2));
        assertTrue(retrieved.isSet(Flag.X_DOWNLOADED_FULL));
        assertFalse(retrieved.isSet(Flag.X_DOWNLOADED_PARTIAL));
        assertFalse(retrieved.isSet(Flag.DELETED));
        retrieved.setFlag(Flag.X_STORE_1, false);
        retrieved.setFlag(Flag.X_STORE_2, true);
        retrieved.setFlag(Flag.X_DOWNLOADED_FULL, false);
        retrieved.setFlag(Flag.X_DOWNLOADED_PARTIAL, true);
        mFolder.updateMessage((LocalStore.LocalMessage)retrieved);
        Message[] retrievedArray = mFolder.getMessages(null);
        assertEquals(1, retrievedArray.length);
        MimeMessage retrievedEntry = (MimeMessage) retrievedArray[0];
        assertEquals(MESSAGE_ID, retrievedEntry.getMessageId());
        assertFalse(retrievedEntry.isSet(Flag.X_STORE_1));
        assertTrue(retrievedEntry.isSet(Flag.X_STORE_2));
        assertFalse(retrievedEntry.isSet(Flag.X_DOWNLOADED_FULL));
        assertTrue(retrievedEntry.isSet(Flag.X_DOWNLOADED_PARTIAL));
        assertFalse(retrievedEntry.isSet(Flag.DELETED));
        retrievedEntry.setFlag(Flag.DELETED, true);
        mFolder.updateMessage((LocalStore.LocalMessage)retrievedEntry);
        Message[] retrievedArray2 = mFolder.getMessages(null);
        assertEquals(1, retrievedArray2.length);
        MimeMessage retrievedEntry2 = (MimeMessage) retrievedArray2[0];
        assertEquals(MESSAGE_ID, retrievedEntry2.getMessageId());
        assertFalse(retrievedEntry2.isSet(Flag.X_STORE_1));
        assertTrue(retrievedEntry2.isSet(Flag.X_STORE_2));
        assertFalse(retrievedEntry2.isSet(Flag.X_DOWNLOADED_FULL));
        assertTrue(retrievedEntry2.isSet(Flag.X_DOWNLOADED_PARTIAL));
        assertTrue(retrievedEntry2.isSet(Flag.DELETED));
    }
    public void testStoreFlagStorage() throws MessagingException, URISyntaxException {
        final MimeMessage message = buildTestMessage(RECIPIENT_TO, SENDER, SUBJECT, BODY);
        message.setMessageId(MESSAGE_ID);
        message.setFlag(Flag.SEEN, true);
        message.setFlag(Flag.FLAGGED, true);
        message.setFlag(Flag.X_STORE_1, true);
        message.setFlag(Flag.X_STORE_2, true);
        message.setFlag(Flag.X_DOWNLOADED_FULL, true);
        message.setFlag(Flag.X_DOWNLOADED_PARTIAL, true);
        message.setFlag(Flag.DELETED, true);
        mFolder.open(OpenMode.READ_WRITE, null);
        mFolder.appendMessages(new Message[]{ message });
        String localUid = message.getUid();
        long folderId = mFolder.getId();
        mFolder.close(false);
        final URI uri = new URI(mLocalStoreUri);
        final String dbPath = uri.getPath();
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbPath, null);
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(
                    "SELECT flags, store_flag_1, store_flag_2," +
                    " flag_downloaded_full, flag_downloaded_partial, flag_deleted" +
                    " FROM messages" + 
                    " WHERE uid = ? AND folder_id = ?",
                    new String[] {
                            localUid, Long.toString(folderId)
                    });
            assertTrue("appended message not found", cursor.moveToNext());
            String flagString = cursor.getString(0);
            String[] flags = flagString.split(",");
            assertEquals(2, flags.length);      
            for (String flag : flags) {
                assertFalse("storeFlag1 in string", flag.equals(Flag.X_STORE_1.toString()));
                assertFalse("storeFlag2 in string", flag.equals(Flag.X_STORE_2.toString()));
                assertFalse("flag_downloaded_full in string", 
                        flag.equals(Flag.X_DOWNLOADED_FULL.toString()));
                assertFalse("flag_downloaded_partial in string", 
                        flag.equals(Flag.X_DOWNLOADED_PARTIAL.toString()));
                assertFalse("flag_deleted in string", flag.equals(Flag.DELETED.toString()));
            }
            assertEquals(1, cursor.getInt(1));  
            assertEquals(1, cursor.getInt(2));  
            assertEquals(1, cursor.getInt(3));  
            assertEquals(1, cursor.getInt(4));  
            assertEquals(1, cursor.getInt(5));  
        }
        finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
    public void testGetMessagesFlags() throws MessagingException {
        final MimeMessage message1 = buildTestMessage(RECIPIENT_TO, SENDER, SUBJECT, BODY);
        message1.setFlag(Flag.X_STORE_1, false);
        message1.setFlag(Flag.X_STORE_2, false);
        final MimeMessage message2 = buildTestMessage(RECIPIENT_TO, SENDER, SUBJECT, BODY);
        message2.setFlag(Flag.X_STORE_1, true);
        message2.setFlag(Flag.X_STORE_2, false);
        final MimeMessage message3 = buildTestMessage(RECIPIENT_TO, SENDER, SUBJECT, BODY);
        message3.setFlag(Flag.X_STORE_1, false);
        message3.setFlag(Flag.X_STORE_2, true);
        final MimeMessage message4 = buildTestMessage(RECIPIENT_TO, SENDER, SUBJECT, BODY);
        message4.setFlag(Flag.X_STORE_1, true);
        message4.setFlag(Flag.X_STORE_2, true);
        final MimeMessage message5 = buildTestMessage(RECIPIENT_TO, SENDER, SUBJECT, BODY);
        message5.setFlag(Flag.X_DOWNLOADED_FULL, true);
        final MimeMessage message6 = buildTestMessage(RECIPIENT_TO, SENDER, SUBJECT, BODY);
        message6.setFlag(Flag.X_DOWNLOADED_PARTIAL, true);
        final MimeMessage message7 = buildTestMessage(RECIPIENT_TO, SENDER, SUBJECT, BODY);
        message7.setFlag(Flag.DELETED, true);
        Message[] allOriginals = new Message[] { 
                message1, message2, message3, message4, message5, message6, message7 };
        mFolder.open(OpenMode.READ_WRITE, null);
        mFolder.appendMessages(allOriginals);
        mFolder.close(false);
        mFolder.open(OpenMode.READ_WRITE, null);
        Message[] getAll1 = mFolder.getMessages(null, null, null);
        checkGottenMessages("null filters", allOriginals, getAll1);
        Message[] getAll2 = mFolder.getMessages(new Flag[0], new Flag[0], null);
        checkGottenMessages("empty filters", allOriginals, getAll2);
        Message[] getSome1 = mFolder.getMessages(new Flag[]{ Flag.X_STORE_1 }, null, null);
        checkGottenMessages("store_1 set", new Message[]{ message2, message4 }, getSome1);
        Message[] getSome2 = mFolder.getMessages(null, new Flag[]{ Flag.X_STORE_1 }, null);
        checkGottenMessages("store_1 clear", 
                new Message[]{ message1, message3, message5, message6, message7 }, getSome2);
        Message[] getSome3 = mFolder.getMessages(new Flag[]{ Flag.X_STORE_2 }, null, null);
        checkGottenMessages("store_2 set", new Message[]{ message3, message4 }, getSome3);
        Message[] getSome4 = mFolder.getMessages(null, new Flag[]{ Flag.X_STORE_2 }, null);
        checkGottenMessages("store_2 clear", 
                new Message[]{ message1, message2, message5, message6, message7 }, getSome4);
        Message[] getOne1 = mFolder.getMessages(new Flag[]{ Flag.X_DOWNLOADED_FULL }, null, null);
        checkGottenMessages("downloaded full", new Message[]{ message5 }, getOne1);
        Message[] getOne2 = mFolder.getMessages(new Flag[]{ Flag.X_DOWNLOADED_PARTIAL }, null,
                null);
        checkGottenMessages("downloaded partial", new Message[]{ message6 }, getOne2);
        Message[] getOne3 = mFolder.getMessages(new Flag[]{ Flag.DELETED }, null, null);
        checkGottenMessages("deleted", new Message[]{ message7 }, getOne3);
        Message[] getSingle1 = mFolder.getMessages(new Flag[]{ Flag.X_STORE_1, Flag.X_STORE_2 }, 
                null, null);
        checkGottenMessages("both set", new Message[]{ message4 }, getSingle1);
        Message[] getSingle2 = mFolder.getMessages(null,
                new Flag[]{ Flag.X_STORE_1, Flag.X_STORE_2 }, null);
        checkGottenMessages("both clear", new Message[]{ message1, message5, message6, message7 }, 
                getSingle2);
    }
    private void checkGottenMessages(String failMessage, Message[] expected, Message[] actual) {
        HashSet<String> expectedUids = new HashSet<String>();
        for (Message message : expected) {
            expectedUids.add(message.getUid());
        }
        HashSet<String> actualUids = new HashSet<String>();
        for (Message message : actual) {
            actualUids.add(message.getUid());
        }
        assertEquals(failMessage, expectedUids, actualUids);
    }
    public void testMessageCount() throws MessagingException {
        final MimeMessage message1 = buildTestMessage(RECIPIENT_TO, SENDER, SUBJECT, BODY);
        message1.setFlag(Flag.X_STORE_1, false);
        message1.setFlag(Flag.X_STORE_2, false);
        message1.setFlag(Flag.X_DOWNLOADED_FULL, true);
        final MimeMessage message2 = buildTestMessage(RECIPIENT_TO, SENDER, SUBJECT, BODY);
        message2.setFlag(Flag.X_STORE_1, true);
        message2.setFlag(Flag.X_STORE_2, false);
        final MimeMessage message3 = buildTestMessage(RECIPIENT_TO, SENDER, SUBJECT, BODY);
        message3.setFlag(Flag.X_STORE_1, false);
        message3.setFlag(Flag.X_STORE_2, true);
        message3.setFlag(Flag.X_DOWNLOADED_FULL, true);
        final MimeMessage message4 = buildTestMessage(RECIPIENT_TO, SENDER, SUBJECT, BODY);
        message4.setFlag(Flag.X_STORE_1, true);
        message4.setFlag(Flag.X_STORE_2, true);
        message4.setFlag(Flag.X_DOWNLOADED_FULL, true);
        final MimeMessage message5 = buildTestMessage(RECIPIENT_TO, SENDER, SUBJECT, BODY);
        message5.setFlag(Flag.X_DOWNLOADED_FULL, true);
        final MimeMessage message6 = buildTestMessage(RECIPIENT_TO, SENDER, SUBJECT, BODY);
        message6.setFlag(Flag.X_DOWNLOADED_PARTIAL, true);
        final MimeMessage message7 = buildTestMessage(RECIPIENT_TO, SENDER, SUBJECT, BODY);
        message7.setFlag(Flag.DELETED, true);
        Message[] allOriginals = new Message[] { 
                message1, message2, message3, message4, message5, message6, message7 };
        mFolder.open(OpenMode.READ_WRITE, null);
        mFolder.appendMessages(allOriginals);
        mFolder.close(false);
        mFolder.open(OpenMode.READ_WRITE, null);
        int allMessages = mFolder.getMessageCount();
        assertEquals("all messages", 7, allMessages);
        int storeFlag1 = mFolder.getMessageCount(new Flag[] { Flag.X_STORE_1 }, null);
        assertEquals("store flag 1", 2, storeFlag1);
        int storeFlag1NotFlag2 = mFolder.getMessageCount(
                new Flag[] { Flag.X_STORE_1 }, new Flag[] { Flag.X_STORE_2 });
        assertEquals("store flag 1, not 2", 1, storeFlag1NotFlag2);
        int downloadedFull = mFolder.getMessageCount(new Flag[] { Flag.X_DOWNLOADED_FULL }, null);
        assertEquals("downloaded full", 4, downloadedFull);
        int storeFlag2Full = mFolder.getMessageCount(
                new Flag[] { Flag.X_STORE_2, Flag.X_DOWNLOADED_FULL }, null);
        assertEquals("store flag 2, full", 2, storeFlag2Full);
        int notDeleted = mFolder.getMessageCount(null, new Flag[] { Flag.DELETED });
        assertEquals("not deleted", 6, notDeleted);
    }
    public void testUnreadMessages() throws MessagingException {
        mFolder.open(OpenMode.READ_WRITE, null);
        LocalStore.LocalFolder folder2 = (LocalStore.LocalFolder) mStore.getFolder("FOLDER-2");
        assertFalse(folder2.exists());
        folder2.create(FolderType.HOLDS_MESSAGES);
        folder2.open(OpenMode.READ_WRITE, null);
        mFolder.setUnreadMessageCount(400);
        folder2.setUnreadMessageCount(425);
        mFolder.close(false);
        folder2.close(false);
        mFolder.open(OpenMode.READ_WRITE, null);
        folder2.open(OpenMode.READ_WRITE, null);
        assertEquals(400, mFolder.getUnreadMessageCount());
        assertEquals(425, folder2.getUnreadMessageCount());
    }
    public void testUnreadMessagesConcurrent() throws MessagingException {
        mFolder.open(OpenMode.READ_WRITE, null);
        LocalStore.LocalFolder folder2 = (LocalStore.LocalFolder) mStore.getFolder(FOLDER_NAME);
        assertTrue(folder2.exists());
        folder2.open(OpenMode.READ_WRITE, null);
        mFolder.setUnreadMessageCount(450);
        assertEquals(450, folder2.getUnreadMessageCount());
    }
    public void testReadWriteVisibleLimits() throws MessagingException {
        mFolder.open(OpenMode.READ_WRITE, null);
        LocalStore.LocalFolder folder2 = (LocalStore.LocalFolder) mStore.getFolder("FOLDER-2");
        assertFalse(folder2.exists());
        folder2.create(FolderType.HOLDS_MESSAGES);
        folder2.open(OpenMode.READ_WRITE, null);
        mFolder.setVisibleLimit(100);
        folder2.setVisibleLimit(200);
        mFolder.close(false);
        folder2.close(false);
        mFolder.open(OpenMode.READ_WRITE, null);
        folder2.open(OpenMode.READ_WRITE, null);
        assertEquals(100, mFolder.getVisibleLimit());
        assertEquals(200, folder2.getVisibleLimit());
    }
    public void testVisibleLimitsConcurrent() throws MessagingException {
        mFolder.open(OpenMode.READ_WRITE, null);
        LocalStore.LocalFolder folder2 = (LocalStore.LocalFolder) mStore.getFolder(FOLDER_NAME);
        assertTrue(folder2.exists());
        folder2.open(OpenMode.READ_WRITE, null);
        mFolder.setVisibleLimit(300);
        assertEquals(300, folder2.getVisibleLimit());
    }
    public void testResetVisibleLimits() throws MessagingException {
        mFolder.open(OpenMode.READ_WRITE, null);
        LocalStore.LocalFolder folder2 = (LocalStore.LocalFolder) mStore.getFolder("FOLDER-2");
        assertFalse(folder2.exists());
        folder2.create(FolderType.HOLDS_MESSAGES);
        folder2.open(OpenMode.READ_WRITE, null);
        mFolder.setVisibleLimit(100);
        folder2.setVisibleLimit(200);
        mFolder.close(false);
        folder2.close(false);
        mFolder.open(OpenMode.READ_WRITE, null);
        folder2.open(OpenMode.READ_WRITE, null);
        mStore.resetVisibleLimits(Email.VISIBLE_LIMIT_DEFAULT);
        assertEquals(Email.VISIBLE_LIMIT_DEFAULT, mFolder.getVisibleLimit());
        assertEquals(Email.VISIBLE_LIMIT_DEFAULT, folder2.getVisibleLimit());
        mFolder.close(false);
        folder2.close(false);
    }
    public void testNoFolderRolesYet() throws MessagingException {
        Folder[] localFolders = mStore.getPersonalNamespaces();
        for (Folder folder : localFolders) {
            assertEquals(Folder.FolderRole.UNKNOWN, folder.getRole()); 
        }
    }
    public void testMissingFolderOpen() throws MessagingException {
        Folder noFolder = mStore.getFolder(MISSING_FOLDER_NAME);
        noFolder.open(OpenMode.READ_WRITE, null);
        noFolder.close(false);
    }
    public void testMissingFolderGetMessageCount() throws MessagingException {
        Folder noFolder = mStore.getFolder(MISSING_FOLDER_NAME);
        noFolder.open(OpenMode.READ_WRITE, null);
        Folder noFolder2 = mStore.getFolder(MISSING_FOLDER_NAME);
        noFolder2.delete(true);
        int count = noFolder.getMessageCount();
        assertEquals(0, count);
    }
    public void testMissingFolderGetUnreadMessageCount() throws MessagingException {
        Folder noFolder = mStore.getFolder(MISSING_FOLDER_NAME);
        noFolder.open(OpenMode.READ_WRITE, null);
        Folder noFolder2 = mStore.getFolder(MISSING_FOLDER_NAME);
        noFolder2.delete(true);
        try {
            noFolder.getUnreadMessageCount();
            fail("MessagingException expected");
        } catch (MessagingException me) {
        }
    }
    public void testMissingFolderGetVisibleLimit() throws MessagingException {
        LocalStore.LocalFolder noFolder = 
                (LocalStore.LocalFolder) mStore.getFolder(MISSING_FOLDER_NAME);
        noFolder.open(OpenMode.READ_WRITE, null);
        Folder noFolder2 = mStore.getFolder(MISSING_FOLDER_NAME);
        noFolder2.delete(true);
        try {
            noFolder.getVisibleLimit();
            fail("MessagingException expected");
        } catch (MessagingException me) {
        }
    }
    public void testExtendedHeader() throws MessagingException {
        MimeMessage message = new MimeMessage();
        message.setUid("message1");
        mFolder.appendMessages(new Message[] { message });
        message.setUid("message2");
        message.setExtendedHeader("X-Header1", "value1");
        message.setExtendedHeader("X-Header2", "value2\r\n value3\n value4\r\n");
        mFolder.appendMessages(new Message[] { message });
        LocalMessage message1 = (LocalMessage) mFolder.getMessage("message1");
        assertNull("none existent header", message1.getExtendedHeader("X-None-Existent"));
        LocalMessage message2 = (LocalMessage) mFolder.getMessage("message2");
        assertEquals("header 1", "value1", message2.getExtendedHeader("X-Header1"));
        assertEquals("header 2", "value2 value3 value4", message2.getExtendedHeader("X-Header2"));
        assertNull("header 3", message2.getExtendedHeader("X-Header3"));
    }
    public void testDbVersion() throws MessagingException, URISyntaxException {
        LocalStore.newInstance(mLocalStoreUri, getContext(), null);
        final URI uri = new URI(mLocalStoreUri);
        final String dbPath = uri.getPath();
        final SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbPath, null);
        assertEquals("database version should be latest", DATABASE_VERSION, db.getVersion());
        db.close();
    }
    private ContentValues cursorToContentValues(Cursor c, String[] schema) {
        if (c.getColumnCount() != schema.length) {
            throw new IndexOutOfBoundsException("schema length is not mach with cursor columns");
        }
        final ContentValues cv = new ContentValues();
        for (int i = 0, count = c.getColumnCount(); i < count; ++i) {
            final String key = c.getColumnName(i);
            final String type = schema[i];
            if (type == "text") {
                cv.put(key, c.getString(i));
            } else if (type == "integer" || type == "primary") {
                cv.put(key, c.getLong(i));
            } else if (type == "numeric" || type == "real") {
                cv.put(key, c.getDouble(i));
            } else if (type == "blob") {
                cv.put(key, c.getBlob(i));
            } else {
                throw new IllegalArgumentException("unsupported type at index " + i);
            }
        }
        return cv;
    }
    private HashSet<String> cursorToColumnNames(Cursor c) {
        HashSet<String> result = new HashSet<String>();
        for (int i = 0, count = c.getColumnCount(); i < count; ++i) {
            result.add(c.getColumnName(i));
        }
        return result;
    }
    public void testDbUpgrade18ToLatest() throws MessagingException, URISyntaxException {
        final URI uri = new URI(mLocalStoreUri);
        final String dbPath = uri.getPath();
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbPath, null);
        createSampleDb(db, 18);
        final ContentValues initialMessage = new ContentValues();
        initialMessage.put("folder_id", (long) 2);        
        initialMessage.put("internal_date", (long) 3);    
        final ContentValues expectedMessage = new ContentValues(initialMessage);
        expectedMessage.put("id", db.insert("messages", null, initialMessage));
        final ContentValues initialAttachment = new ContentValues();
        initialAttachment.put("message_id", (long) 4);    
        initialAttachment.put("mime_type", (String) "a"); 
        final ContentValues expectedAttachment = new ContentValues(initialAttachment);
        expectedAttachment.put("id", db.insert("attachments", null, initialAttachment));
        db.close();
        LocalStore.newInstance(mLocalStoreUri, getContext(), null);
        expectedMessage.put("message_id", (String) null);    
        expectedAttachment.put("content_id", (String) null); 
        db = SQLiteDatabase.openOrCreateDatabase(dbPath, null);
        assertEquals("database should be upgraded", DATABASE_VERSION, db.getVersion());
        Cursor c;
        checkAllTablesFound(db);
        c = db.query("messages",
                new String[] { "id", "folder_id", "internal_date", "message_id" },
                null, null, null, null, null);
        assertTrue("messages table should have one data", c.moveToNext());
        final ContentValues actualMessage = cursorToContentValues(c,
                new String[] { "primary", "integer", "integer", "text" });
        assertEquals("messages table cursor does not have expected values",
                expectedMessage, actualMessage);
        c.close();
        c = db.query("attachments",
                new String[] { "id", "message_id", "mime_type", "content_id" },
                null, null, null, null, null);
        assertTrue("attachments table should have one data", c.moveToNext());
        final ContentValues actualAttachment = cursorToContentValues(c,
                new String[] { "primary", "integer", "text", "text" });
        assertEquals("attachment table cursor does not have expected values",
                expectedAttachment, actualAttachment);
        c.close();
        db.close();
    }
    public void testDbUpgrade19ToLatest() throws MessagingException, URISyntaxException {
        final URI uri = new URI(mLocalStoreUri);
        final String dbPath = uri.getPath();
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbPath, null);
        createSampleDb(db, 19);
        final ContentValues initialMessage = new ContentValues();
        initialMessage.put("folder_id", (long) 2);      
        initialMessage.put("internal_date", (long) 3);  
        initialMessage.put("message_id", (String) "x"); 
        final ContentValues expectedMessage = new ContentValues(initialMessage);
        expectedMessage.put("id", db.insert("messages", null, initialMessage));
        final ContentValues initialAttachment = new ContentValues();
        initialAttachment.put("message_id", (long) 4);  
        initialAttachment.put("mime_type", (String) "a"); 
        final ContentValues expectedAttachment = new ContentValues(initialAttachment);
        expectedAttachment.put("id", db.insert("attachments", null, initialAttachment));
        db.close();
        LocalStore.newInstance(mLocalStoreUri, getContext(), null);
        expectedAttachment.put("content_id", (String) null);  
        db = SQLiteDatabase.openOrCreateDatabase(dbPath, null);
        assertEquals("database should be upgraded", DATABASE_VERSION, db.getVersion());
        Cursor c;
        checkAllTablesFound(db);
        c = db.query("messages",
                new String[] { "id", "folder_id", "internal_date", "message_id" },
                null, null, null, null, null);
        assertTrue("attachments table should have one data", c.moveToNext());
        final ContentValues actualMessage = cursorToContentValues(c,
                new String[] { "primary", "integer", "integer", "text" });
        assertEquals("messages table cursor does not have expected values",
                expectedMessage, actualMessage);
        c = db.query("attachments",
                new String[] { "id", "message_id", "mime_type", "content_id" },
                null, null, null, null, null);
        assertTrue("attachments table should have one data", c.moveToNext());
        final ContentValues actualAttachment = cursorToContentValues(c,
                        new String[] { "primary", "integer", "text", "text" });
        assertEquals("attachment table cursor does not have expected values",
                expectedAttachment, actualAttachment);
        db.close();
    }
    public void testDbUpgrade20ToLatest() throws MessagingException, URISyntaxException {
        final URI uri = new URI(mLocalStoreUri);
        final String dbPath = uri.getPath();
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbPath, null);
        createSampleDb(db, 20);
        db.close();
        LocalStore.newInstance(mLocalStoreUri, getContext(), null);
        db = SQLiteDatabase.openOrCreateDatabase(dbPath, null);
        assertEquals("database should be upgraded", DATABASE_VERSION, db.getVersion());
        checkAllTablesFound(db);
    }
    public void testDbUpgrade21ToLatest() throws MessagingException, URISyntaxException {
        final URI uri = new URI(mLocalStoreUri);
        final String dbPath = uri.getPath();
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbPath, null);
        createSampleDb(db, 21);
        db.close();
        LocalStore.newInstance(mLocalStoreUri, getContext(), null);
        db = SQLiteDatabase.openOrCreateDatabase(dbPath, null);
        assertEquals("database should be upgraded", DATABASE_VERSION, db.getVersion());
        checkAllTablesFound(db);
    }
    public void testDbUpgrade22ToLatest() throws MessagingException, URISyntaxException {
        final URI uri = new URI(mLocalStoreUri);
        final String dbPath = uri.getPath();
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbPath, null);
        createSampleDb(db, 22);
        final ContentValues inMessage1 = new ContentValues();
        inMessage1.put("message_id", (String) "x"); 
        inMessage1.put("flags", Flag.X_DOWNLOADED_FULL.toString());
        final ContentValues outMessage1 = new ContentValues(inMessage1);
        outMessage1.put("id", db.insert("messages", null, inMessage1));
        final ContentValues inMessage2 = new ContentValues();
        inMessage2.put("message_id", (String) "y"); 
        inMessage2.put("flags", Flag.X_DOWNLOADED_PARTIAL.toString());
        final ContentValues outMessage2 = new ContentValues(inMessage2);
        outMessage2.put("id", db.insert("messages", null, inMessage2));
        final ContentValues inMessage3 = new ContentValues();
        inMessage3.put("message_id", (String) "z"); 
        inMessage3.put("flags", Flag.DELETED.toString());
        final ContentValues outMessage3 = new ContentValues(inMessage3);
        outMessage3.put("id", db.insert("messages", null, inMessage3));
        db.close();
        LocalStore.newInstance(mLocalStoreUri, getContext(), null);
        db = SQLiteDatabase.openOrCreateDatabase(dbPath, null);
        assertEquals("database should be upgraded", DATABASE_VERSION, db.getVersion());
        checkAllTablesFound(db);
        String[] columns = new String[] { "id", "message_id", "flags", 
                "flag_downloaded_full", "flag_downloaded_partial", "flag_deleted" };
        Cursor c = db.query("messages", columns, null, null, null, null, null);
        for (int msgNum = 0; msgNum <= 2; ++msgNum) {
            assertTrue(c.moveToNext());
            ContentValues actualMessage = cursorToContentValues(c,
                    new String[] { "primary", "text", "text", "integer", "integer", "integer" });
            String messageId = actualMessage.getAsString("message_id");
            int outDlFull = actualMessage.getAsInteger("flag_downloaded_full");
            int outDlPartial = actualMessage.getAsInteger("flag_downloaded_partial");
            int outDeleted = actualMessage.getAsInteger("flag_deleted");
            if ("x".equals(messageId)) {
                assertTrue("converted flag_downloaded_full",
                        outDlFull == 1 && outDlPartial == 0 && outDeleted == 0);
            } else if ("y".equals(messageId)) {
                assertTrue("converted flag_downloaded_partial",
                        outDlFull == 0 && outDlPartial == 1 && outDeleted == 0);
            } else if ("z".equals(messageId)) {
                assertTrue("converted flag_deleted",
                        outDlFull == 0 && outDlPartial == 0 && outDeleted == 1);
            }
        }
        c.close();
    }
    public void testDbUpgrade23ToLatest() throws MessagingException, URISyntaxException {
        final URI uri = new URI(mLocalStoreUri);
        final String dbPath = uri.getPath();
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbPath, null);
        createSampleDb(db, 23);
        final ContentValues initialMessage = new ContentValues();
        initialMessage.put("folder_id", (long) 2);        
        initialMessage.put("internal_date", (long) 3);    
        final ContentValues expectedMessage = new ContentValues(initialMessage);
        expectedMessage.put("id", db.insert("messages", null, initialMessage));
        db.close();
        LocalStore.newInstance(mLocalStoreUri, getContext(), null);
        expectedMessage.put("message_id", (String) null);    
        db = SQLiteDatabase.openOrCreateDatabase(dbPath, null);
        assertEquals("database should be upgraded", DATABASE_VERSION, db.getVersion());
        Cursor c;
        checkAllTablesFound(db);
        c = db.query("messages",
                new String[] { "id", "folder_id", "internal_date", "message_id" },
                null, null, null, null, null);
        assertTrue("messages table should have one data", c.moveToNext());
        final ContentValues actualMessage = cursorToContentValues(c,
                new String[] { "primary", "integer", "integer", "text" });
        assertEquals("messages table cursor does not have expected values",
                expectedMessage, actualMessage);
        c.close();
        db.close();
    }
    private void checkAllTablesFound(SQLiteDatabase db) {
        Cursor c;
        HashSet<String> foundNames;
        ArrayList<String> expectedNames;
        c = db.query("messages",
                null,
                null, null, null, null, null);
        foundNames = cursorToColumnNames(c);
        expectedNames = new ArrayList<String>(Arrays.asList(
                new String[]{ "id", "folder_id", "uid", "subject", "date", "flags", "sender_list",
                        "to_list", "cc_list", "bcc_list", "reply_to_list",
                        "html_content", "text_content", "attachment_count",
                        "internal_date", "store_flag_1", "store_flag_2", "flag_downloaded_full",
                        "flag_downloaded_partial", "flag_deleted", "x_headers" }
                ));
        assertTrue("messages", foundNames.containsAll(expectedNames));
        c = db.query("attachments",
                null,
                null, null, null, null, null);
        foundNames = cursorToColumnNames(c);
        expectedNames = new ArrayList<String>(Arrays.asList(
                new String[]{ "id", "message_id",
                        "store_data", "content_uri", "size", "name",
                        "mime_type", "content_id" }
                ));
        assertTrue("attachments", foundNames.containsAll(expectedNames));
        c = db.query("remote_store_data",
                null,
                null, null, null, null, null);
        foundNames = cursorToColumnNames(c);
        expectedNames = new ArrayList<String>(Arrays.asList(
                new String[]{ "id", "folder_id", "data_key", "data" }
                ));
        assertTrue("remote_store_data", foundNames.containsAll(expectedNames));
    }
    private static void createSampleDb(SQLiteDatabase db, int version) {
        db.execSQL("DROP TABLE IF EXISTS messages");
        db.execSQL("CREATE TABLE messages (id INTEGER PRIMARY KEY, folder_id INTEGER, " +
                   "uid TEXT, subject TEXT, date INTEGER, flags TEXT, sender_list TEXT, " +
                   "to_list TEXT, cc_list TEXT, bcc_list TEXT, reply_to_list TEXT, " +
                   "html_content TEXT, text_content TEXT, attachment_count INTEGER, " +
                   "internal_date INTEGER" +
                   ((version >= 19) ? ", message_id TEXT" : "") +
                   ((version >= 22) ? ", store_flag_1 INTEGER, store_flag_2 INTEGER" : "") +
                   ((version >= 23) ? 
                           ", flag_downloaded_full INTEGER, flag_downloaded_partial INTEGER" : "") +
                   ((version >= 23) ? ", flag_deleted INTEGER" : "") +
                   ((version >= 24) ? ", x_headers TEXT" : "") +
                   ")");
        db.execSQL("DROP TABLE IF EXISTS attachments");
        db.execSQL("CREATE TABLE attachments (id INTEGER PRIMARY KEY, message_id INTEGER," +
                   "store_data TEXT, content_uri TEXT, size INTEGER, name TEXT," +
                   "mime_type TEXT" +
                   ((version >= 20) ? ", content_id" : "") +
                   ")");
        if (version >= 21) {
            db.execSQL("DROP TABLE IF EXISTS remote_store_data");
            db.execSQL("CREATE TABLE remote_store_data "
                    + "(id INTEGER PRIMARY KEY, folder_id INTEGER, "
                    + "data_key TEXT, data TEXT)");
        }
        db.setVersion(version);
    }
}
