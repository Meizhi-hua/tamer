public class ContactLookupKeyTest extends BaseContactsProvider2Test {
    public void testLookupKeyUsingDisplayNameAndNoAccount() {
        long rawContactId1 = createRawContactWithName("John", "Doe");
        long rawContactId2 = createRawContactWithName("johndoe", null);
        assertAggregated(rawContactId1, rawContactId2);
        String normalizedName = NameNormalizer.normalize("johndoe");
        String expectedLookupKey = "0r" + rawContactId1 + "-" + normalizedName + ".0r"
                + rawContactId2 + "-" + normalizedName;
        long contactId = queryContactId(rawContactId1);
        assertStoredValue(ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId),
                Contacts.LOOKUP_KEY, expectedLookupKey);
        Uri lookupUri = Uri.withAppendedPath(Contacts.CONTENT_LOOKUP_URI, expectedLookupKey);
        assertStoredValue(lookupUri, Contacts._ID, contactId);
        assertStoredValue(ContentUris.withAppendedId(lookupUri, contactId),
                Contacts._ID, contactId);
        assertStoredValue(ContentUris.withAppendedId(lookupUri, contactId + 1),
                Contacts._ID, contactId);
    }
    public void testLookupKeyUsingSourceIdAndNoAccount() {
        long rawContactId1 = createRawContactWithName("John", "Doe");
        storeValue(RawContacts.CONTENT_URI, rawContactId1, RawContacts.SOURCE_ID, "123");
        long rawContactId2 = createRawContactWithName("johndoe", null);
        storeValue(RawContacts.CONTENT_URI, rawContactId2, RawContacts.SOURCE_ID, "4.5.6");
        assertAggregated(rawContactId1, rawContactId2);
        String expectedLookupKey = "0i123.0e4..5..6";
        long contactId = queryContactId(rawContactId1);
        assertStoredValue(ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId),
                Contacts.LOOKUP_KEY, expectedLookupKey);
        Uri lookupUri = Uri.withAppendedPath(Contacts.CONTENT_LOOKUP_URI, expectedLookupKey);
        assertStoredValue(lookupUri, Contacts._ID, contactId);
    }
    public void testLookupKeySameSourceIdDifferentAccounts() {
        long rawContactId1 = createRawContactWithName("Dear", "Doe");
        storeValue(RawContacts.CONTENT_URI, rawContactId1, RawContacts.ACCOUNT_TYPE, "foo");
        storeValue(RawContacts.CONTENT_URI, rawContactId1, RawContacts.ACCOUNT_NAME, "FOO");
        storeValue(RawContacts.CONTENT_URI, rawContactId1, RawContacts.SOURCE_ID, "1");
        long rawContactId2 = createRawContactWithName("Deer", "Dough");
        storeValue(RawContacts.CONTENT_URI, rawContactId2, RawContacts.ACCOUNT_TYPE, "bar");
        storeValue(RawContacts.CONTENT_URI, rawContactId2, RawContacts.ACCOUNT_NAME, "BAR");
        storeValue(RawContacts.CONTENT_URI, rawContactId2, RawContacts.SOURCE_ID, "1");
        assertNotAggregated(rawContactId1, rawContactId2);
        int accountHashCode1 = ContactLookupKey.getAccountHashCode("foo", "FOO");
        int accountHashCode2 = ContactLookupKey.getAccountHashCode("bar", "BAR");
        long contactId1 = queryContactId(rawContactId1);
        assertStoredValue(ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId1),
                Contacts.LOOKUP_KEY, accountHashCode1 + "i1");
        long contactId2 = queryContactId(rawContactId2);
        assertStoredValue(ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId2),
                Contacts.LOOKUP_KEY, accountHashCode2 + "i1");
        Uri lookupUri1 = Uri.withAppendedPath(Contacts.CONTENT_LOOKUP_URI, accountHashCode1 + "i1");
        assertStoredValue(lookupUri1, Contacts._ID, contactId1);
        Uri lookupUri2 = Uri.withAppendedPath(Contacts.CONTENT_LOOKUP_URI, accountHashCode2 + "i1");
        assertStoredValue(lookupUri2, Contacts._ID, contactId2);
    }
    public void testLookupKeyChoosingLargestContact() {
        long rawContactId1 = createRawContactWithName("John", "Doe");
        storeValue(RawContacts.CONTENT_URI, rawContactId1, RawContacts.SOURCE_ID, "1");
        long rawContactId2 = createRawContactWithName("John", "Doe");
        storeValue(RawContacts.CONTENT_URI, rawContactId2, RawContacts.SOURCE_ID, "2");
        long rawContactId3 = createRawContactWithName("John", "Doe");
        storeValue(RawContacts.CONTENT_URI, rawContactId3, RawContacts.SOURCE_ID, "3");
        String lookupKey = "0i1.0i2.0i3";
        long contactId = queryContactId(rawContactId1);
        assertStoredValue(ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId),
                Contacts.LOOKUP_KEY, lookupKey);
        setAggregationException(AggregationExceptions.TYPE_KEEP_SEPARATE, rawContactId1,
                rawContactId3);
        setAggregationException(AggregationExceptions.TYPE_KEEP_SEPARATE, rawContactId2,
                rawContactId3);
        assertAggregated(rawContactId1, rawContactId2);
        assertNotAggregated(rawContactId1, rawContactId3);
        assertNotAggregated(rawContactId2, rawContactId3);
        long largerContactId = queryContactId(rawContactId1);
        assertStoredValue(
                ContentUris.withAppendedId(Contacts.CONTENT_URI, largerContactId),
                Contacts.LOOKUP_KEY, "0i1.0i2");
        assertStoredValue(
                ContentUris.withAppendedId(Contacts.CONTENT_URI, queryContactId(rawContactId3)),
                Contacts.LOOKUP_KEY, "0i3");
        Uri lookupUri = Uri.withAppendedPath(Contacts.CONTENT_LOOKUP_URI, lookupKey);
        assertStoredValue(lookupUri, Contacts._ID, largerContactId);
    }
    public void testGetLookupUri() {
        long rawContactId1 = createRawContactWithName("John", "Doe");
        storeValue(RawContacts.CONTENT_URI, rawContactId1, RawContacts.SOURCE_ID, "1");
        long contactId = queryContactId(rawContactId1);
        String lookupUri = "content:
        Uri contentUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId);
        assertEquals(lookupUri,
                Contacts.getLookupUri(mResolver, contentUri).toString());
        Uri staleLookupUri = ContentUris.withAppendedId(
                Uri.withAppendedPath(Contacts.CONTENT_LOOKUP_URI, "0i1"),
                contactId+2);
        assertEquals(lookupUri,
                Contacts.getLookupUri(mResolver, staleLookupUri).toString());
    }
    public void testParseLookupKey() {
        assertLookupKey("123n1248AC",
                new int[]{123},
                new int[]{ContactLookupKey.LOOKUP_TYPE_DISPLAY_NAME},
                new String[]{"1248AC"});
        assertLookupKey("123r20-1248AC",
                new int[]{123},
                new int[]{ContactLookupKey.LOOKUP_TYPE_RAW_CONTACT_ID},
                new String[]{"1248AC"});
        assertLookupKey("0i1248AC-X",
                new int[]{0},
                new int[]{ContactLookupKey.LOOKUP_TYPE_SOURCE_ID},
                new String[]{"1248AC-X"});
        assertLookupKey("432e12..48AC",
                new int[]{432},
                new int[]{ContactLookupKey.LOOKUP_TYPE_SOURCE_ID},
                new String[]{"12.48AC"});
        assertLookupKey("123n1248AC.0i1248AC.432e12..48AC.123n1248AC.123r30-2184CA",
                new int[]{123, 0, 432, 123},
                new int[] {
                        ContactLookupKey.LOOKUP_TYPE_DISPLAY_NAME,
                        ContactLookupKey.LOOKUP_TYPE_SOURCE_ID,
                        ContactLookupKey.LOOKUP_TYPE_SOURCE_ID,
                        ContactLookupKey.LOOKUP_TYPE_DISPLAY_NAME,
                        ContactLookupKey.LOOKUP_TYPE_RAW_CONTACT_ID,
                },
                new String[]{"1248AC", "1248AC", "12.48AC", "1248AC", "2184CA"});
    }
    private void assertLookupKey(String lookupKey, int[] accountHashCodes, int[] types,
            String[] keys) {
        ContactLookupKey key = new ContactLookupKey();
        ArrayList<LookupKeySegment> list = key.parse(lookupKey);
        assertEquals(types.length, list.size());
        for (int i = 0; i < accountHashCodes.length; i++) {
            LookupKeySegment segment = list.get(i);
            assertEquals(accountHashCodes[i], segment.accountHashCode);
            assertEquals(types[i], segment.lookupType);
            assertEquals(keys[i], segment.key);
        }
    }
}
