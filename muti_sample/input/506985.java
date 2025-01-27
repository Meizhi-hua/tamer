@TestTargetClass(java.util.regex.Matcher.class)
public class Matcher2Test extends TestCase {
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies the basic functionality of toString() method.",
        method = "toString",
        args = {}
    )
    public void test_toString() {
        Pattern p = Pattern.compile("foo");
        Matcher m = p.matcher("bar");
        assertNotNull(m.toString());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies start, end, group, usePattern methods with wrong conditions, IllegalStateException should be thrown",
            method = "start",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies start, end, group, usePattern methods with wrong conditions, IllegalStateException should be thrown",
            method = "end",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies start, end, group, usePattern methods with wrong conditions, IllegalStateException should be thrown",
            method = "group",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies start, end, group, usePattern methods with wrong conditions, IllegalStateException should be thrown",
            method = "start",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies start, end, group, usePattern methods with wrong conditions, IllegalStateException should be thrown",
            method = "end",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies start, end, group, usePattern methods with wrong conditions, IllegalStateException should be thrown",
            method = "group",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies start, end, group, usePattern methods with wrong conditions, IllegalStateException should be thrown",
            method = "usePattern",
            args = {java.util.regex.Pattern.class}
        )
    })
    public void testErrorConditions() throws PatternSyntaxException {
        Pattern p = Pattern.compile("foo");
        Matcher m = p.matcher("bar");
        assertFalse(m.matches());
        try {
            m.start();
            fail("IllegalStateException expected");
        } catch (IllegalStateException e) {
        }
        try {
            m.end();
            fail("IllegalStateException expected");
        } catch (IllegalStateException e) {
        }
        try {
            m.group();
            fail("IllegalStateException expected");
        } catch (IllegalStateException e) {
        }
        try {
            m.start(1);
            fail("IllegalStateException expected");
        } catch (IllegalStateException e) {
        }
        try {
            m.end(1);
            fail("IllegalStateException expected");
        } catch (IllegalStateException e) {
        }
        try {
            m.group(1);
            fail("IllegalStateException expected");
        } catch (IllegalStateException e) {
        }
        try {
            m.usePattern(null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies end, start, group methods with wrong conditions, IndexOutOfBoundsException, IllegalStateException should be thrown",
            method = "start",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies end, start, group methods with wrong conditions, IndexOutOfBoundsException, IllegalStateException should be thrown",
            method = "end",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies end, start, group methods with wrong conditions, IndexOutOfBoundsException, IllegalStateException should be thrown",
            method = "start",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies end, start, group methods with wrong conditions, IndexOutOfBoundsException, IllegalStateException should be thrown",
            method = "end",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies end, start, group methods with wrong conditions, IndexOutOfBoundsException, IllegalStateException should be thrown",
            method = "group",
            args = {int.class}
        )
    })
    public void testErrorConditions2() throws PatternSyntaxException {
        Pattern p = Pattern.compile("(foo[0-9])(bar[a-z])");
        Matcher m = p.matcher("foo1barzfoo2baryfoozbar5");
        assertTrue(m.find());
        assertEquals(0, m.start());
        assertEquals(8, m.end());
        assertEquals(0, m.start(1));
        assertEquals(4, m.end(1));
        assertEquals(4, m.start(2));
        assertEquals(8, m.end(2));
        try {
            m.start(3);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            m.end(3);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            m.group(3);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            m.start(-1);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            m.end(-1);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            m.group(-1);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {
        }
        assertTrue(m.find());
        assertEquals(8, m.start());
        assertEquals(16, m.end());
        assertEquals(8, m.start(1));
        assertEquals(12, m.end(1));
        assertEquals(12, m.start(2));
        assertEquals(16, m.end(2));
        try {
            m.start(3);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            m.end(3);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            m.group(3);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            m.start(-1);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            m.end(-1);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            m.group(-1);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {
        }
        assertFalse(m.find());
        try {
            m.start(3);
            fail("IllegalStateException expected");
        } catch (IllegalStateException e) {
        }
        try {
            m.end(3);
            fail("IllegalStateException expected");
        } catch (IllegalStateException e) {
        }
        try {
            m.group(3);
            fail("IllegalStateException expected");
        } catch (IllegalStateException e) {
        }
        try {
            m.start(-1);
            fail("IllegalStateException expected");
        } catch (IllegalStateException e) {
        }
        try {
            m.end(-1);
            fail("IllegalStateException expected");
        } catch (IllegalStateException e) {
        }
        try {
            m.group(-1);
            fail("IllegalStateException expected");
        } catch (IllegalStateException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that IndexOutOfBoundsException exception is thrown while calling of replaceAll method with incorrect string.",
        method = "replaceAll",
        args = {java.lang.String.class}
    )    
    public void testReplacementBackSlash() {
        String str = "replace me";
        String replacedString = "me";
        String substitutionString = "\\";
        Pattern pat = Pattern.compile(replacedString);
        Matcher mat = pat.matcher(str);
        try {
            String res = mat.replaceAll(substitutionString);
            fail("IndexOutOfBoundsException should be thrown - " + res);
        } catch (Exception e) {
        }
    }
}
