public class DiscourseLoggerTest extends TestCase {
    private static byte[] b(String chars) {
        byte[] ret = new byte[chars.length()];
        for (int i = 0; i < chars.length(); i++) {
            ret[i] = (byte) chars.charAt(i);
        }
        return ret;
    }
    private static String[] s(String... strings) {
        return strings;
    }
    private static Object[] o(Object... objects) {
        return objects;
    }
    public void testDiscourseLogger() {
        checkDiscourseStore(4, o(), s());
        checkDiscourseStore(4,
                o(
                        "command"
                ),
                s(
                        "command"
                ));
        checkDiscourseStore(4,
                o(
                        "1a",
                        "2b",
                        "3",
                        "4dd"
                ),
                s(
                        "1a",
                        "2b",
                        "3",
                        "4dd"
                ));
        checkDiscourseStore(4,
                o(
                        "1",
                        "2",
                        "3",
                        "4",
                        "5"
                ),
                s(
                        "2",
                        "3",
                        "4",
                        "5"
                ));
        checkDiscourseStore(4,
                o(
                        b("A")
                ),
                s(
                        "A"
                ));
        checkDiscourseStore(4,
                o(
                        b("A\nB\nC")
                ),
                s(
                        "A",
                        "B",
                        "C"
                ));
        checkDiscourseStore(4,
                o(
                        b("A\nBCD\nC\nDEF\u0080\u0001G\r\n")
                ),
                s(
                        "A",
                        "BCD",
                        "C",
                        "DEF\\x80\\x01G"
                ));
        checkDiscourseStore(4,
                o(
                        "1",
                        b("2"),
                        "3",
                        b("4\n5\n"),
                        "6 7 8",
                        "7 a bbb ccc",
                        b("* aaa8\n* bbb9\n7 ccc  10")
                ),
                s(
                        "7 a bbb ccc",
                        "* aaa8",
                        "* bbb9",
                        "7 ccc  10"
                ));
    }
    private void checkDiscourseStore(int storeSize, Object[] discource, String[] expected) {
        DiscourseLogger store = new DiscourseLogger(storeSize);
        for (Object o : discource) {
            if (o instanceof String) {
                store.addSentCommand((String) o);
            } else if (o instanceof byte[]) {
                for (byte b : (byte[]) o) {
                    store.addReceivedByte(b);
                }
            } else {
                fail("Invalid argument.  Test broken.");
            }
        }
        MoreAsserts.assertEquals(expected, store.getLines());
        store.logLastDiscourse();
        assertEquals(0, store.getLines().length);
    }
}
