public class CharSequences {
    public static CharSequence forAsciiBytes(final byte[] bytes) {
        return new CharSequence() {
            public char charAt(int index) {
                return (char) bytes[index];
            }
            public int length() {
                return bytes.length;
            }
            public CharSequence subSequence(int start, int end) {
                return forAsciiBytes(bytes, start, end);
            }
            public String toString() {
                return new String(bytes);
            }
        };
    }
    public static CharSequence forAsciiBytes(final byte[] bytes,
            final int start, final int end) {
        validate(start, end, bytes.length);
        return new CharSequence() {
            public char charAt(int index) {
                return (char) bytes[index + start];
            }
            public int length() {
                return end - start;
            }
            public CharSequence subSequence(int newStart, int newEnd) {
                newStart -= start;
                newEnd -= start;
                validate(newStart, newEnd, length());
                return forAsciiBytes(bytes, newStart, newEnd);
            }
            public String toString() {
                return new String(bytes, start, length());
            }
        };
    }
    static void validate(int start, int end, int length) {
        if (start < 0) throw new IndexOutOfBoundsException();
        if (end < 0) throw new IndexOutOfBoundsException();
        if (end > length) throw new IndexOutOfBoundsException();
        if (start > end) throw new IndexOutOfBoundsException();
    }
    public static boolean equals(CharSequence a, CharSequence b) {
        if (a.length() != b.length()) {
            return false;
        }
        int length = a.length();
        for (int i = 0; i < length; i++) {
            if (a.charAt(i) != b.charAt(i)) {
                return false;
            }
        }
        return true;
    }
    public static int compareToIgnoreCase(CharSequence me, CharSequence another) {
        int myLen = me.length(), anotherLen = another.length();
        int myPos = 0, anotherPos = 0, result;
        int end = (myLen < anotherLen) ? myLen : anotherLen;
        while (myPos < end) {
            if ((result = Character.toLowerCase(me.charAt(myPos++))
                    - Character.toLowerCase(another.charAt(anotherPos++))) != 0) {
                return result;
            }
        }
        return myLen - anotherLen;
    }
}
