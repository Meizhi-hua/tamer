@TestTargetClass(PBEKeySpec.class)
public class PBEKeySpecTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "PBEKeySpec",
        args = {char[].class}
    )
    public void testPBEKeySpec1() {
        try {
            PBEKeySpec pbeks = new PBEKeySpec(null);
            assertTrue("An empty char[] should be used in case of null "
                        + "char array.", pbeks.getPassword().length == 0);
        } catch (NullPointerException e) {
            fail("Unexpected NullPointerException was thrown.");
        }
        char[] password = new char[] {'1', '2', '3', '4', '5'};
        PBEKeySpec pbeks = new PBEKeySpec(password);
        password[0] ++;
        assertFalse("The change of password specified in the constructor "
                    + "should not cause the change of internal array.",
                    password[0] == pbeks.getPassword()[0]);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "PBEKeySpec",
        args = {char[].class, byte[].class, int.class, int.class}
    )
    public void testPBEKeySpec2() {
        char[] password = new char[] {'1', '2', '3', '4', '5'};
        byte[] salt = new byte[] {1, 2, 3, 4, 5};
        int iterationCount = 10;
        int keyLength = 10;
        try {
            PBEKeySpec pbeks = new PBEKeySpec(null, salt,
                                                iterationCount, keyLength);
            assertTrue("An empty char[] should be used in case of null input "
                        + "char array.", pbeks.getPassword().length == 0);
        } catch (IllegalArgumentException e) {
            fail("Unexpected IllegalArgumentException was thrown.");
        } catch (NullPointerException e) {
            fail("Unexpected NullPointerException was thrown.");
        }
        try {
            new PBEKeySpec(password, null, iterationCount, keyLength);
            fail("A NullPointerException should be was thrown "
                    + "in the case of null salt.");
        } catch (IllegalArgumentException e) {
            fail("Unexpected IllegalArgumentException was thrown.");
        } catch (NullPointerException e) {
        }
        try {
            new PBEKeySpec(password, new byte [0], iterationCount, keyLength);
            fail("An IllegalArgumentException should be thrown "
                    + "in the case of empty salt.");
        } catch (IllegalArgumentException e) {
        }
        try {
            new PBEKeySpec(password, salt, -1, keyLength);
            fail("An IllegalArgumentException should be thrown "
                    + "in the case of negative iterationCount.");
        } catch (IllegalArgumentException e) {
        }
        try {
            new PBEKeySpec(password, salt, iterationCount, -1);
            fail("An IllegalArgumentException should be thrown "
                    + "in the case of negative keyLength.");
        } catch (IllegalArgumentException e) {
        }
        try {
            new PBEKeySpec(password, salt, 0, keyLength);
            fail("An IllegalArgumentException should be thrown "
                    + "in the case of zero iterationCount.");
        } catch (IllegalArgumentException e) {
        }
        try {
            new PBEKeySpec(password, salt, iterationCount, 0);
            fail("An IllegalArgumentException should be thrown "
                    + "in the case of zero keyLength.");
        } catch (IllegalArgumentException e) {
        }
        PBEKeySpec pbeks = new PBEKeySpec(password, salt,
                                                iterationCount, keyLength);
        password[0] ++;
        assertFalse("The change of password specified in the constructor "
                    + "should not cause the change of internal array.",
                    password[0] == pbeks.getPassword()[0]);
        salt[0] ++;
        assertFalse("The change of salt specified in the constructor "
                    + " should not cause the change of internal array.",
                    salt[0] == pbeks.getSalt()[0]);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "PBEKeySpec",
        args = {char[].class, byte[].class, int.class}
    )
    public void testPBEKeySpec3() {
        char[] password = new char[] {'1', '2', '3', '4', '5'};
        byte[] salt = new byte[] {1, 2, 3, 4, 5};
        int iterationCount = 10;
        try {
            PBEKeySpec pbeks = new PBEKeySpec(null, salt, iterationCount);
            assertTrue("An empty char[] should be used in case of null input "
                        + "char array.", pbeks.getPassword().length == 0);
        } catch (IllegalArgumentException e) {
            fail("Unexpected IllegalArgumentException was thrown.");
        } catch (NullPointerException e) {
            fail("Unexpected NullPointerException was thrown.");
        }
        try {
            new PBEKeySpec(password, null, iterationCount);
            fail("A NullPointerException should be was thrown "
                    + "in the case of null salt.");
        } catch (IllegalArgumentException e) {
            fail("Unexpected IllegalArgumentException was thrown.");
        } catch (NullPointerException e) {
        }
        try {
            new PBEKeySpec(password, new byte [0],
                                                iterationCount);
            fail("An IllegalArgumentException should be thrown "
                    + "in the case of empty salt.");
        } catch (IllegalArgumentException e) {
        }
        try {
            new PBEKeySpec(password, salt, -1);
            fail("An IllegalArgumentException should be thrown "
                    + "in the case of negative iterationCount.");
        } catch (IllegalArgumentException e) {
        }
        try {
            new PBEKeySpec(password, salt, 0);
            fail("An IllegalArgumentException should be thrown "
                    + "in the case of zero iterationCount.");
        } catch (IllegalArgumentException e) {
        }
        PBEKeySpec pbeks = new PBEKeySpec(password, salt, iterationCount);
        password[0] ++;
        assertFalse("The change of password specified in the constructor "
                    + "should not cause the change of internal array.",
                    password[0] == pbeks.getPassword()[0]);
        salt[0] ++;
        assertFalse("The change of salt specified in the constructor "
                    + " should not cause the change of internal array.",
                    salt[0] == pbeks.getSalt()[0]);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "clearPassword",
        args = {}
    )
    public void testClearPassword() {
        char[] password = new char[] {'1', '2', '3', '4', '5'};
        PBEKeySpec pbeks = new PBEKeySpec(password);
        pbeks.clearPassword();
        try {
            pbeks.getPassword();
            fail("An IllegalStateException should be was thrown "
                    + "after the clearing the password.");
        } catch (IllegalStateException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Exception was checked in testClearPassword() method.",
        method = "getPassword",
        args = {}
    )
    public void testGetPassword() {
        char[] password = new char[] {'1', '2', '3', '4', '5'};
        PBEKeySpec pbeks = new PBEKeySpec(password);
        char[] result = pbeks.getPassword();
        if (! Arrays.equals(password, result)) {
            fail("The returned password is not equal to the specified "
                    + "in the constructor.");
        }
        result[0] ++;
        assertFalse("The change of returned by getPassword() method password "
                    + "should not cause the change of internal array.",
                    result[0] == pbeks.getPassword()[0]);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSalt",
        args = {}
    )
    public void testGetSalt() {
        char[] password = new char[] {'1', '2', '3', '4', '5'};
        byte[] salt = new byte[] {1, 2, 3, 4, 5};
        int iterationCount = 10;
        PBEKeySpec pbeks = new PBEKeySpec(password, salt, iterationCount);
        byte[] result = pbeks.getSalt();
        if (! Arrays.equals(salt, result)) {
            fail("The returned salt is not equal to the specified "
                    + "in the constructor.");
        }
        result[0] ++;
        assertFalse("The change of returned by getSalt() method salt"
                    + "should not cause the change of internal array.",
                    result[0] == pbeks.getSalt()[0]);
        pbeks = new PBEKeySpec(password);
        assertNull("The getSalt() method should return null if the salt "
                    + "is not specified.", pbeks.getSalt());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getIterationCount",
        args = {}
    )
    public void testGetIterationCount() {
        char[] password = new char[] {'1', '2', '3', '4', '5'};
        byte[] salt = new byte[] {1, 2, 3, 4, 5};
        int iterationCount = 10;
        PBEKeySpec pbeks = new PBEKeySpec(password, salt, iterationCount);
        assertTrue("The returned iterationCount is not equal to the specified "
                + "in the constructor.",
                pbeks.getIterationCount() == iterationCount);
        pbeks = new PBEKeySpec(password);
        assertTrue("The getIterationCount() method should return 0 "
                    + "if the iterationCount is not specified.",
                    pbeks.getIterationCount() == 0);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getKeyLength",
        args = {}
    )
    public void testGetKeyLength() {
        char[] password = new char[] {'1', '2', '3', '4', '5'};
        byte[] salt = new byte[] {1, 2, 3, 4, 5};
        int iterationCount = 10;
        int keyLength = 10;
        PBEKeySpec pbeks = new PBEKeySpec(password, salt,
                                                iterationCount, keyLength);
        assertTrue("The returned keyLength is not equal to the value specified "
                + "in the constructor.",
                pbeks.getKeyLength() == keyLength);
        pbeks = new PBEKeySpec(password);
        assertTrue("The getKeyLength() method should return 0 "
                    + "if the keyLength is not specified.",
                    pbeks.getKeyLength() == 0);
    }
    public static Test suite() {
        return new TestSuite(PBEKeySpecTest.class);
    }
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
