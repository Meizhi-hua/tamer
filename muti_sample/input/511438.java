@TestTargetClass(Address.class)
public class AddressTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test constructor",
        method = "Address",
        args = {java.util.Locale.class}
    )
    public void testConstructor() {
        new Address(Locale.ENGLISH);
        new Address(Locale.FRANCE);
        new Address(null);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test describeContents()",
        method = "describeContents",
        args = {}
    )
    public void testDescribeContents() {
        Address address = new Address(Locale.GERMAN);
        assertEquals(0, address.describeContents());
        Bundle extras = new Bundle();
        extras.putParcelable("key1", new MockParcelable());
        address.setExtras(extras);
        assertEquals(extras.describeContents(), address.describeContents());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test setAdminArea(String) and getAdminArea()",
            method = "setAdminArea",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test setAdminArea(String) and getAdminArea()",
            method = "getAdminArea",
            args = {}
        )
    })
    public void testAccessAdminArea() {
        Address address = new Address(Locale.ITALY);
        String adminArea = "CA";
        address.setAdminArea(adminArea);
        assertEquals(adminArea, address.getAdminArea());
        address.setAdminArea(null);
        assertNull(address.getAdminArea());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test setCountryCode(String) and getCountryCode()",
            method = "setCountryCode",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test setCountryCode(String) and getCountryCode()",
            method = "getCountryCode",
            args = {}
        )
    })
    public void testAccessCountryCode() {
        Address address = new Address(Locale.JAPAN);
        String countryCode = "US";
        address.setCountryCode(countryCode);
        assertEquals(countryCode, address.getCountryCode());
        address.setCountryCode(null);
        assertNull(address.getCountryCode());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test setCountryName(String) and getCountryName()",
            method = "setCountryName",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test setCountryName(String) and getCountryName()",
            method = "getCountryName",
            args = {}
        )
    })
    public void testAccessCountryName() {
        Address address = new Address(Locale.KOREA);
        String countryName = "China";
        address.setCountryName(countryName);
        assertEquals(countryName, address.getCountryName());
        address.setCountryName(null);
        assertNull(address.getCountryName());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test setExtras(Bundle) and getExtras()",
            method = "setExtras",
            args = {android.os.Bundle.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test setExtras(Bundle) and getExtras()",
            method = "getExtras",
            args = {}
        )
    })
    public void testAccessExtras() {
        Address address = new Address(Locale.TAIWAN);
        Bundle extras = new Bundle();
        extras.putBoolean("key1", false);
        byte b = 10;
        extras.putByte("key2", b);
        address.setExtras(extras);
        Bundle actual = address.getExtras();
        assertFalse(actual.getBoolean("key1"));
        assertEquals(b, actual.getByte("key2"));
        address.setExtras(null);
        assertNull(address.getExtras());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test setFeatureName(String) and getFeatureName()",
            method = "setFeatureName",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test setFeatureName(String) and getFeatureName()",
            method = "getFeatureName",
            args = {}
        )
    })
    public void testAccessFeatureName() {
        Address address = new Address(Locale.SIMPLIFIED_CHINESE);
        String featureName = "Golden Gate Bridge";
        address.setFeatureName(featureName);
        assertEquals(featureName, address.getFeatureName());
        address.setFeatureName(null);
        assertNull(address.getFeatureName());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test setLatitude(double) and getLatitude(), clearLatitude(), hasLatitude()",
            method = "setLatitude",
            args = {double.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test setLatitude(double) and getLatitude(), clearLatitude(), hasLatitude()",
            method = "getLatitude",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test setLatitude(double) and getLatitude(), clearLatitude(), hasLatitude()",
            method = "clearLatitude",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test setLatitude(double) and getLatitude(), clearLatitude(), hasLatitude()",
            method = "hasLatitude",
            args = {}
        )
    })
    public void testAccessLatitude() {
        Address address = new Address(Locale.CHINA);
        assertFalse(address.hasLatitude());
        double latitude = 1.23456789;
        address.setLatitude(latitude);
        assertTrue(address.hasLatitude());
        assertEquals(latitude, address.getLatitude());
        address.clearLatitude();
        assertFalse(address.hasLatitude());
        try {
            address.getLatitude();
            fail("should throw IllegalStateException.");
        } catch (IllegalStateException e) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setLongitude",
            args = {double.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getLongitude",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "clearLongitude",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "hasLongitude",
            args = {}
        )
    })
    public void testAccessLongitude() {
        Address address = new Address(Locale.CHINA);
        assertFalse(address.hasLongitude());
        double longitude = 1.23456789;
        address.setLongitude(longitude);
        assertTrue(address.hasLongitude());
        assertEquals(longitude, address.getLongitude());
        address.clearLongitude();
        assertFalse(address.hasLongitude());
        try {
            address.getLongitude();
            fail("should throw IllegalStateException.");
        } catch (IllegalStateException e) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test setPhone(String) and getPhone()",
            method = "setPhone",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test setPhone(String) and getPhone()",
            method = "getPhone",
            args = {}
        )
    })
    @ToBeFixed(bug = "", explanation = "getPhone() should never throw IllegalStateException. " +
            "Should remove @throws clause from its javadoc")
    public void testAccessPhone() {
        Address address = new Address(Locale.CHINA);
        String phone = "+86-13512345678";
        address.setPhone(phone);
        assertEquals(phone, address.getPhone());
        address.setPhone(null);
        assertNull(address.getPhone());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test setPostalCode(String) and getPostalCode()",
            method = "setPostalCode",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test setPostalCode(String) and getPostalCode()",
            method = "getPostalCode",
            args = {}
        )
    })
    public void testAccessPostalCode() {
        Address address = new Address(Locale.CHINA);
        String postalCode = "93110";
        address.setPostalCode(postalCode);
        assertEquals(postalCode, address.getPostalCode());
        address.setPostalCode(null);
        assertNull(address.getPostalCode());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test setThoroughfare(String) and getThoroughfare()",
            method = "setThoroughfare",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test setThoroughfare(String) and getThoroughfare()",
            method = "getThoroughfare",
            args = {}
        )
    })
    public void testAccessThoroughfare() {
        Address address = new Address(Locale.CHINA);
        String thoroughfare = "1600 Ampitheater Parkway";
        address.setThoroughfare(thoroughfare);
        assertEquals(thoroughfare, address.getThoroughfare());
        address.setThoroughfare(null);
        assertNull(address.getThoroughfare());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test setUrl(String) and getUrl()",
            method = "setUrl",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test setUrl(String) and getUrl()",
            method = "getUrl",
            args = {}
        )
    })
    public void testAccessUrl() {
        Address address = new Address(Locale.CHINA);
        String Url = "Url";
        address.setUrl(Url);
        assertEquals(Url, address.getUrl());
        address.setUrl(null);
        assertNull(address.getUrl());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test setSubAdminArea(String) and getSubAdminArea()",
            method = "setSubAdminArea",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test setSubAdminArea(String) and getSubAdminArea()",
            method = "getSubAdminArea",
            args = {}
        )
    })
    public void testAccessSubAdminArea() {
        Address address = new Address(Locale.CHINA);
        String subAdminArea = "Santa Clara County";
        address.setSubAdminArea(subAdminArea);
        assertEquals(subAdminArea, address.getSubAdminArea());
        address.setSubAdminArea(null);
        assertNull(address.getSubAdminArea());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test toString()",
        method = "toString",
        args = {}
    )
    public void testToString() {
        Address address = new Address(Locale.CHINA);
        address.setUrl("www.google.com");
        address.setPostalCode("95120");
        String expected = "Address[addressLines=[],feature=null,admin=null,sub-admin=null," +
                "locality=null,thoroughfare=null,postalCode=95120,countryCode=null," +
                "countryName=null,hasLatitude=false,latitude=0.0,hasLongitude=false," +
                "longitude=0.0,phone=null,url=www.google.com,extras=null]";
        assertEquals(expected, address.toString());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setAddressLine",
            args = {int.class, java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getMaxAddressLineIndex",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getAddressLine",
            args = {int.class}
        )
    })
    public void testAddressLine() {
        Address address = new Address(Locale.CHINA);
        try {
            address.setAddressLine(-1, null);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            address.getAddressLine(-1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        address.setAddressLine(0, null);
        assertNull(address.getAddressLine(0));
        assertEquals(0, address.getMaxAddressLineIndex());
        final String line1 = "1";
        address.setAddressLine(0, line1);
        assertEquals(line1, address.getAddressLine(0));
        assertEquals(0, address.getMaxAddressLineIndex());
        final String line2 = "2";
        address.setAddressLine(5, line2);
        assertEquals(line2, address.getAddressLine(5));
        assertEquals(5, address.getMaxAddressLineIndex());
        address.setAddressLine(2, null);
        assertNull(address.getAddressLine(2));
        assertEquals(5, address.getMaxAddressLineIndex());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test getLocale()",
        method = "getLocale",
        args = {}
    )
    public void testGetLocale() {
        Locale locale = Locale.US;
        Address address = new Address(locale);
        assertSame(locale, address.getLocale());
        locale = Locale.UK;
        address = new Address(locale);
        assertSame(locale, address.getLocale());
        address = new Address(null);
        assertNull(address.getLocale());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test setLocality(String) and getLocality()",
            method = "setLocality",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test setLocality(String) and getLocality()",
            method = "getLocality",
            args = {}
        )
    })
    public void testAccessLocality() {
        Address address = new Address(Locale.PRC);
        String locality = "Hollywood";
        address.setLocality(locality);
        assertEquals(locality, address.getLocality());
        address.setLocality(null);
        assertNull(address.getLocality());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test writeToParcel(Parcel, int), this function ignores the parameter 'flag'.",
        method = "writeToParcel",
        args = {android.os.Parcel.class, int.class}
    )
    public void testWriteToParcel() {
        Locale locale = Locale.KOREA;
        Address address = new Address(locale);
        Parcel parcel = Parcel.obtain();
        address.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        assertEquals(locale.getLanguage(), parcel.readString());
        assertEquals(locale.getCountry(), parcel.readString());
        assertEquals(0, parcel.readInt());
        assertEquals(address.getFeatureName(), parcel.readString());
        assertEquals(address.getAdminArea(), parcel.readString());
        assertEquals(address.getSubAdminArea(), parcel.readString());
        assertEquals(address.getLocality(), parcel.readString());
        assertEquals(address.getThoroughfare(), parcel.readString());
        assertEquals(address.getPostalCode(), parcel.readString());
        assertEquals(address.getCountryCode(), parcel.readString());
        assertEquals(address.getCountryName(), parcel.readString());
        assertEquals(0, parcel.readInt());
        assertEquals(0, parcel.readInt());
        assertEquals(address.getPhone(), parcel.readString());
        assertEquals(address.getUrl(), parcel.readString());
        assertEquals(address.getExtras(), parcel.readBundle());
    }
    private class MockParcelable implements Parcelable {
        public int describeContents() {
            return Parcelable.CONTENTS_FILE_DESCRIPTOR;
        }
        public void writeToParcel(Parcel dest, int flags) {
        }
    }
}
