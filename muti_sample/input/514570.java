public class StringTest extends PerformanceTestBase {
    public static final int ITERATIONS = 1000;
    public static final String STATIC_STRING_01 = "Hello Android";
    public static final String STATIC_STRING_02 =
            "Remember, today is the tomorrow you worried about yesterday";
    public static final char[] STATIC_CHAR_ARRAY =
            {'N', 'A', 'N', 'D', 'R', 'O', 'I', 'D'};
    public static StringBuffer STATIC_SBUF = new StringBuffer(STATIC_STRING_02);
    @Override
    public int startPerformance(PerformanceTestCase.Intermediates intermediates) {
        intermediates.setInternalIterations(ITERATIONS);
        return 0;
    }
    public void testStringCreate() {
        String rString;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            rString = new String();
            rString = new String();
            rString = new String();
            rString = new String();
            rString = new String();
            rString = new String();
            rString = new String();
            rString = new String();
            rString = new String();
            rString = new String();
        }
    }
    public void testStringCreate1() {
        String rString, str = STATIC_STRING_01;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            rString = new String(str);
            rString = new String(str);
            rString = new String(str);
            rString = new String(str);
            rString = new String(str);
            rString = new String(str);
            rString = new String(str);
            rString = new String(str);
            rString = new String(str);
            rString = new String(str); 
        }
    }
    public void testStringEquals() {
        String mString = new String(STATIC_STRING_01);
        String str = STATIC_STRING_01;
        boolean result;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            result = mString.equals(str);
            result = mString.equals(str);
            result = mString.equals(str);
            result = mString.equals(str);
            result = mString.equals(str);
            result = mString.equals(str);
            result = mString.equals(str);
            result = mString.equals(str);
            result = mString.equals(str);
            result = mString.equals(str);
        }
    }
    public void testStringContentEquals() {
        StringBuffer sBuf = new StringBuffer(STATIC_STRING_01);
        String str = STATIC_STRING_01;
        boolean result;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            result = str.contentEquals(sBuf);
            result = str.contentEquals(sBuf);
            result = str.contentEquals(sBuf);
            result = str.contentEquals(sBuf);
            result = str.contentEquals(sBuf);
            result = str.contentEquals(sBuf);
            result = str.contentEquals(sBuf);
            result = str.contentEquals(sBuf);
            result = str.contentEquals(sBuf);
            result = str.contentEquals(sBuf);
        }
    }
    public void testStringCompareTo() {
        String str1 = new String(STATIC_STRING_01);
        String str2 = STATIC_STRING_01;
        int result;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            result = str1.compareTo(str2);
            result = str1.compareTo(str2);
            result = str1.compareTo(str2);
            result = str1.compareTo(str2);
            result = str1.compareTo(str2);
            result = str1.compareTo(str2);
            result = str1.compareTo(str2);
            result = str1.compareTo(str2);
            result = str1.compareTo(str2);
            result = str1.compareTo(str2);
        }
    }
    public void testStringCompareToIgnoreCase() {
        String mString = new String(STATIC_STRING_01);
        String str2 = STATIC_STRING_01;
        int result;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            result = mString.compareToIgnoreCase(str2);
            result = mString.compareToIgnoreCase(str2);
            result = mString.compareToIgnoreCase(str2);
            result = mString.compareToIgnoreCase(str2);
            result = mString.compareToIgnoreCase(str2);
            result = mString.compareToIgnoreCase(str2);
            result = mString.compareToIgnoreCase(str2);
            result = mString.compareToIgnoreCase(str2);
            result = mString.compareToIgnoreCase(str2);
            result = mString.compareToIgnoreCase(str2);
        }
    }
    public void testStringstartsWith() {
        boolean result;
        String str1 = STATIC_STRING_02, str2 = "Rem";
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            result = str1.startsWith(str2);
            result = str1.startsWith(str2);
            result = str1.startsWith(str2);
            result = str1.startsWith(str2);
            result = str1.startsWith(str2);
            result = str1.startsWith(str2);
            result = str1.startsWith(str2);
            result = str1.startsWith(str2);
            result = str1.startsWith(str2);
            result = str1.startsWith(str2);
        }
    }
    public void testStringstartsWith1() {
        String str1 = STATIC_STRING_02, str2 = "tom";
        int pos = 10;
        boolean result;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            result = str1.startsWith(str2, pos);
            result = str1.startsWith(str2, pos);
            result = str1.startsWith(str2, pos);
            result = str1.startsWith(str2, pos);
            result = str1.startsWith(str2, pos);
            result = str1.startsWith(str2, pos);
            result = str1.startsWith(str2, pos);
            result = str1.startsWith(str2, pos);
            result = str1.startsWith(str2, pos);
            result = str1.startsWith(str2, pos);
        }
    }
    public void testStringendsWith() {
        String str = STATIC_STRING_02, str1 = "day";
        boolean result;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            result = str.endsWith(str1);
            result = str.endsWith(str1);
            result = str.endsWith(str1);
            result = str.endsWith(str1);
            result = str.endsWith(str1);
            result = str.endsWith(str1);
            result = str.endsWith(str1);
            result = str.endsWith(str1);
            result = str.endsWith(str1);
            result = str.endsWith(str1);
        }
    }
    public void testStringindexOf() {
        boolean result;
        String str = STATIC_STRING_02, str1 = "tomo";
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            result = str.indexOf(str1) > 0;
            result = str.indexOf(str1) > 0;
            result = str.indexOf(str1) > 0;
            result = str.indexOf(str1) > 0;
            result = str.indexOf(str1) > 0;
            result = str.indexOf(str1) > 0;
            result = str.indexOf(str1) > 0;
            result = str.indexOf(str1) > 0;
            result = str.indexOf(str1) > 0;
            result = str.indexOf(str1) > 0;
        }
    }
    public void testStringindexOf1() {
        int index;
        String str = STATIC_STRING_02;
        char c = 't';
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            index = str.indexOf(c);
            index = str.indexOf(c);
            index = str.indexOf(c);
            index = str.indexOf(c);
            index = str.indexOf(c);
            index = str.indexOf(c);
            index = str.indexOf(c);
            index = str.indexOf(c);
            index = str.indexOf(c);
            index = str.indexOf(c);
        }
    }
    public void testStringindexOf2() {
        int index, pos = 12;
        String str = STATIC_STRING_02, str1 = "tom";
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            index = str.indexOf(str1, pos);
            index = str.indexOf(str1, pos);
            index = str.indexOf(str1, pos);
            index = str.indexOf(str1, pos);
            index = str.indexOf(str1, pos);
            index = str.indexOf(str1, pos);
            index = str.indexOf(str1, pos);
            index = str.indexOf(str1, pos);
            index = str.indexOf(str1, pos);
            index = str.indexOf(str1, pos);
        }
    }
    public void testStringlastIndexOf() {
        int index;
        char c = 't';
        String str = STATIC_STRING_02;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            index = str.lastIndexOf(c);
            index = str.lastIndexOf(c);
            index = str.lastIndexOf(c);
            index = str.lastIndexOf(c);
            index = str.lastIndexOf(c);
            index = str.lastIndexOf(c);
            index = str.lastIndexOf(c);
            index = str.lastIndexOf(c);
            index = str.lastIndexOf(c);
            index = str.lastIndexOf(c);
        }
    }
    public void testStringlastIndexOf1() {
        int index, pos = 36;
        String str = STATIC_STRING_02, str1 = "tom";
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            index = str.lastIndexOf(str1, pos);
            index = str.lastIndexOf(str1, pos);
            index = str.lastIndexOf(str1, pos);
            index = str.lastIndexOf(str1, pos);
            index = str.lastIndexOf(str1, pos);
            index = str.lastIndexOf(str1, pos);
            index = str.lastIndexOf(str1, pos);
            index = str.lastIndexOf(str1, pos);
            index = str.lastIndexOf(str1, pos);
            index = str.lastIndexOf(str1, pos);
        }
    }
    public void testStringcontains() {
        boolean result;
        String str = STATIC_STRING_02, str1 = "tomo";
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            result = str.contains(str1);
            result = str.contains(str1);
            result = str.contains(str1);
            result = str.contains(str1);
            result = str.contains(str1);
            result = str.contains(str1);
            result = str.contains(str1);
            result = str.contains(str1);
            result = str.contains(str1);
            result = str.contains(str1);
        }
    }
    public void testStringsubstring() {
        String rString;
        String str = STATIC_STRING_02;
        int index = 10;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            rString = str.substring(index);
            rString = str.substring(index);
            rString = str.substring(index);
            rString = str.substring(index);
            rString = str.substring(index);
            rString = str.substring(index);
            rString = str.substring(index);
            rString = str.substring(index);
            rString = str.substring(index);
            rString = str.substring(index);
        }
    }
    public void testStringsubstring1() {
        String rString;
        String str = STATIC_STRING_02;
        int start = 10, end = 48;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            rString = str.substring(start, end);
            rString = str.substring(start, end);
            rString = str.substring(start, end);
            rString = str.substring(start, end);
            rString = str.substring(start, end);
            rString = str.substring(start, end);
            rString = str.substring(start, end);
            rString = str.substring(start, end);
            rString = str.substring(start, end);
            rString = str.substring(start, end);
        }
    }
    public void testStringvalueOf() {
        String rString;
        char[] cArray = STATIC_CHAR_ARRAY;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            rString = String.valueOf(cArray);
            rString = String.valueOf(cArray);
            rString = String.valueOf(cArray);
            rString = String.valueOf(cArray);
            rString = String.valueOf(cArray);
            rString = String.valueOf(cArray);
            rString = String.valueOf(cArray);
            rString = String.valueOf(cArray);
            rString = String.valueOf(cArray);
            rString = String.valueOf(cArray);
        }
    }
    public void testStringvalueOf1() {
        String rString;
        char[] cArray = STATIC_CHAR_ARRAY;
        int start = 1, end = 7;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            rString = String.valueOf(cArray, start, end);
            rString = String.valueOf(cArray, start, end);
            rString = String.valueOf(cArray, start, end);
            rString = String.valueOf(cArray, start, end);
            rString = String.valueOf(cArray, start, end);
            rString = String.valueOf(cArray, start, end);
            rString = String.valueOf(cArray, start, end);
            rString = String.valueOf(cArray, start, end);
            rString = String.valueOf(cArray, start, end);
            rString = String.valueOf(cArray, start, end);
        }
    }
    public void testStringtoCharArray() {
        char[] cArray;
        String str = STATIC_STRING_02;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            cArray = str.toCharArray();
            cArray = str.toCharArray();
            cArray = str.toCharArray();
            cArray = str.toCharArray();
            cArray = str.toCharArray();
            cArray = str.toCharArray();
            cArray = str.toCharArray();
            cArray = str.toCharArray();
            cArray = str.toCharArray();
            cArray = str.toCharArray();
        }
    }
    public void testStringlength() {
        int len;
        String str = STATIC_STRING_02;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            len = str.length();
            len = str.length();
            len = str.length();
            len = str.length();
            len = str.length();
            len = str.length();
            len = str.length();
            len = str.length();
            len = str.length();
            len = str.length();
        }
    }
    public void testStringhashCode() {
        int index;
        String str = STATIC_STRING_02;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            index = str.hashCode();
            index = str.hashCode();
            index = str.hashCode();
            index = str.hashCode();
            index = str.hashCode();
            index = str.hashCode();
            index = str.hashCode();
            index = str.hashCode();
            index = str.hashCode();
            index = str.hashCode();
        }
    }
    public void testStringreplace() {
        String rString;
        String str = STATIC_STRING_02;
        char c1 = ' ', c2 = ' ';
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            rString = str.replace(c1, c2);
            rString = str.replace(c1, c2);
            rString = str.replace(c1, c2);
            rString = str.replace(c1, c2);
            rString = str.replace(c1, c2);
            rString = str.replace(c1, c2);
            rString = str.replace(c1, c2);
            rString = str.replace(c1, c2);
            rString = str.replace(c1, c2);
            rString = str.replace(c1, c2);
        }
    }
    public void testStringreplaceAll() {
        String rString;
        String str = STATIC_STRING_02, str1 = " ", str2 = "/";
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            rString = str.replaceAll(str1, str2);
            rString = str.replaceAll(str1, str2);
            rString = str.replaceAll(str1, str2);
            rString = str.replaceAll(str1, str2);
            rString = str.replaceAll(str1, str2);
            rString = str.replaceAll(str1, str2);
            rString = str.replaceAll(str1, str2);
            rString = str.replaceAll(str1, str2);
            rString = str.replaceAll(str1, str2);
            rString = str.replaceAll(str1, str2);
        }
    }
    public void testStringtoString() {
        StringBuffer sBuf = new StringBuffer(STATIC_STRING_02);
        String rString;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            rString = sBuf.toString();
            rString = sBuf.toString();
            rString = sBuf.toString();
            rString = sBuf.toString();
            rString = sBuf.toString();
            rString = sBuf.toString();
            rString = sBuf.toString();
            rString = sBuf.toString();
            rString = sBuf.toString();
            rString = sBuf.toString();
        }
    }
    public void testStringsplit() {
        String[] strings;
        String str1 = STATIC_STRING_02, str = " ";
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            strings = str1.split(str);
            strings = str1.split(str);
            strings = str1.split(str);
            strings = str1.split(str);
            strings = str1.split(str);
            strings = str1.split(str);
            strings = str1.split(str);
            strings = str1.split(str);
            strings = str1.split(str);
            strings = str1.split(str);
        }
    }
    public void testStringsplit1() {
        String str = STATIC_STRING_02, str1 = " ";
        String[] strings;
        int pos = 8;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            strings = str.split(str1, pos);
            strings = str.split(str1, pos);
            strings = str.split(str1, pos);
            strings = str.split(str1, pos);
            strings = str.split(str1, pos);
            strings = str.split(str1, pos);
            strings = str.split(str1, pos);
            strings = str.split(str1, pos);
            strings = str.split(str1, pos);
            strings = str.split(str1, pos);
        }
    }
    public void testStringgetBytes() {
        byte[] bytes;
        String str = STATIC_STRING_02;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            bytes = str.getBytes();
            bytes = str.getBytes();
            bytes = str.getBytes();
            bytes = str.getBytes();
            bytes = str.getBytes();
            bytes = str.getBytes();
            bytes = str.getBytes();
            bytes = str.getBytes();
            bytes = str.getBytes();
            bytes = str.getBytes();
        }
    }
    public void testStringcopyValueOf() {
        String rString;
        char[] cArray = STATIC_CHAR_ARRAY;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            rString = String.copyValueOf(cArray);
            rString = String.copyValueOf(cArray);
            rString = String.copyValueOf(cArray);
            rString = String.copyValueOf(cArray);
            rString = String.copyValueOf(cArray);
            rString = String.copyValueOf(cArray);
            rString = String.copyValueOf(cArray);
            rString = String.copyValueOf(cArray);
            rString = String.copyValueOf(cArray);
            rString = String.copyValueOf(cArray);
        }
    }
    public void testStringcopyValueOf1() {
        String rString;
        int start = 1, end = 7;
        char[] cArray = STATIC_CHAR_ARRAY;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            rString = String.copyValueOf(cArray, start, end);
            rString = String.copyValueOf(cArray, start, end);
            rString = String.copyValueOf(cArray, start, end);
            rString = String.copyValueOf(cArray, start, end);
            rString = String.copyValueOf(cArray, start, end);
            rString = String.copyValueOf(cArray, start, end);
            rString = String.copyValueOf(cArray, start, end);
            rString = String.copyValueOf(cArray, start, end);
            rString = String.copyValueOf(cArray, start, end);
            rString = String.copyValueOf(cArray, start, end);
        }
    }
    public void testStringtrim() {
        String mString =
                new String(
                        "                            HELLO ANDROID                                                ");
        String rString;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            rString = mString.trim();
            rString = mString.trim();
            rString = mString.trim();
            rString = mString.trim();
            rString = mString.trim();
            rString = mString.trim();
            rString = mString.trim();
            rString = mString.trim();
            rString = mString.trim();
            rString = mString.trim();
        }
    }
    public void testStringgetChars() {
        char[] cArray = STATIC_CHAR_ARRAY;
        String str = STATIC_STRING_01;
        int value1 = 7, value2 = 12, value3 = 1;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            str.getChars(value1, value2, cArray, value3);
            str.getChars(value1, value2, cArray, value3);
            str.getChars(value1, value2, cArray, value3);
            str.getChars(value1, value2, cArray, value3);
            str.getChars(value1, value2, cArray, value3);
            str.getChars(value1, value2, cArray, value3);
            str.getChars(value1, value2, cArray, value3);
            str.getChars(value1, value2, cArray, value3);
            str.getChars(value1, value2, cArray, value3);
            str.getChars(value1, value2, cArray, value3);
        }
    }
    public void testStringtoUpperCase() {
        String rString, str = STATIC_STRING_02;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            rString = str.toUpperCase();
            rString = str.toUpperCase();
            rString = str.toUpperCase();
            rString = str.toUpperCase();
            rString = str.toUpperCase();
            rString = str.toUpperCase();
            rString = str.toUpperCase();
            rString = str.toUpperCase();
            rString = str.toUpperCase();
            rString = str.toUpperCase();
        }
    }
    public void testStringtoUpperCase1() {
        Locale locale = new Locale("tr");
        String str = STATIC_STRING_02;
        String rString;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            rString = str.toUpperCase(locale);
            rString = str.toUpperCase(locale);
            rString = str.toUpperCase(locale);
            rString = str.toUpperCase(locale);
            rString = str.toUpperCase(locale);
            rString = str.toUpperCase(locale);
            rString = str.toUpperCase(locale);
            rString = str.toUpperCase(locale);
            rString = str.toUpperCase(locale);
            rString = str.toUpperCase(locale);
        }
    }
    public void StringtoLowerCase() {
        String rString, str = STATIC_STRING_02;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            rString = str.toLowerCase();
            rString = str.toLowerCase();
            rString = str.toLowerCase();
            rString = str.toLowerCase();
            rString = str.toLowerCase();
            rString = str.toLowerCase();
            rString = str.toLowerCase();
            rString = str.toLowerCase();
            rString = str.toLowerCase();
            rString = str.toLowerCase();
        }
    }
    public void testStringtoLowerCase1() {
        Locale locale = new Locale("tr");
        String rString, str = STATIC_STRING_02;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            rString = str.toLowerCase(locale);
            rString = str.toLowerCase(locale);
            rString = str.toLowerCase(locale);
            rString = str.toLowerCase(locale);
            rString = str.toLowerCase(locale);
            rString = str.toLowerCase(locale);
            rString = str.toLowerCase(locale);
            rString = str.toLowerCase(locale);
            rString = str.toLowerCase(locale);
            rString = str.toLowerCase(locale);
        }
    }
    public void testStringcharAt() {
        String str = STATIC_STRING_02;
        int index, pos = 21;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            index = str.charAt(pos);
            index = str.charAt(pos);
            index = str.charAt(pos);
            index = str.charAt(pos);
            index = str.charAt(pos);
            index = str.charAt(pos);
            index = str.charAt(pos);
            index = str.charAt(pos);
            index = str.charAt(pos);
            index = str.charAt(pos);
        }
    }
    public void testStringConcat() {
        String mString, str1 = STATIC_STRING_01, str2 = STATIC_STRING_02;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            mString = str1.concat(str2);
            mString = str1.concat(str2);
            mString = str1.concat(str2);
            mString = str1.concat(str2);
            mString = str1.concat(str2);
            mString = str1.concat(str2);
            mString = str1.concat(str2);
            mString = str1.concat(str2);
            mString = str1.concat(str2);
            mString = str1.concat(str2);
        }
    }
    public void testStringBufferAppend() {
        StringBuffer sBuf = new StringBuffer(" ");
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            sBuf.append(i);
            sBuf.append(i);
            sBuf.append(i);
            sBuf.append(i);
            sBuf.append(i);
            sBuf.append(i);
            sBuf.append(i);
            sBuf.append(i);
            sBuf.append(i);
            sBuf.append(i);
        }
    }
    public void testStringBufferInsert() {
        StringBuffer sBuf = new StringBuffer(" ");
        int index = sBuf.length();
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            sBuf.insert(index, i);
            sBuf.insert(index, i);
            sBuf.insert(index, i);
            sBuf.insert(index, i);
            sBuf.insert(index, i);
            sBuf.insert(index, i);
            sBuf.insert(index, i);
            sBuf.insert(index, i);
            sBuf.insert(index, i);
            sBuf.insert(index, i);
        }
    }
    public void testStringBufferReverse() {
        StringBuffer sBuf = STATIC_SBUF;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            sBuf.reverse();
            sBuf.reverse();
            sBuf.reverse();
            sBuf.reverse();
            sBuf.reverse();
            sBuf.reverse();
            sBuf.reverse();
            sBuf.reverse();
            sBuf.reverse();
            sBuf.reverse();
        }
    }
    public void testStringBufferSubstring() {
        StringBuffer sBuf = STATIC_SBUF;
        String rString;
        int index = 0;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            rString = sBuf.substring(index);
            rString = sBuf.substring(index);
            rString = sBuf.substring(index);
            rString = sBuf.substring(index);
            rString = sBuf.substring(index);
            rString = sBuf.substring(index);
            rString = sBuf.substring(index);
            rString = sBuf.substring(index);
            rString = sBuf.substring(index);
            rString = sBuf.substring(index);
        }
    }
    public void testStringBufferSubstring1() {
        StringBuffer sBuf = STATIC_SBUF;
        String rString;
        int start = 5, end = 25;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            rString = sBuf.substring(start, end);
            rString = sBuf.substring(start, end);
            rString = sBuf.substring(start, end);
            rString = sBuf.substring(start, end);
            rString = sBuf.substring(start, end);
            rString = sBuf.substring(start, end);
            rString = sBuf.substring(start, end);
            rString = sBuf.substring(start, end);
            rString = sBuf.substring(start, end);
            rString = sBuf.substring(start, end);
        }
    }
    public void testStringBufferReplace() {
        StringBuffer sBuf = STATIC_SBUF;
        int start = 3, end = 6;
        String str = "ind";
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            sBuf.replace(start, end, str);
            sBuf.replace(start, end, str);
            sBuf.replace(start, end, str);
            sBuf.replace(start, end, str);
            sBuf.replace(start, end, str);
            sBuf.replace(start, end, str);
            sBuf.replace(start, end, str);
            sBuf.replace(start, end, str);
            sBuf.replace(start, end, str);
            sBuf.replace(start, end, str);
        }
    }
    public void testStringBufferIndexOf() {
        StringBuffer sBuf = STATIC_SBUF;
        String str = "t";
        int index;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            index = sBuf.indexOf(str);
            index = sBuf.indexOf(str);
            index = sBuf.indexOf(str);
            index = sBuf.indexOf(str);
            index = sBuf.indexOf(str);
            index = sBuf.indexOf(str);
            index = sBuf.indexOf(str);
            index = sBuf.indexOf(str);
            index = sBuf.indexOf(str);
            index = sBuf.indexOf(str);
        }
    }
    public void testStringBufferIndexOf1() {
        StringBuffer sBuf = STATIC_SBUF;
        String str = "tom";
        int index, pos = 12;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            index = sBuf.indexOf(str, pos);
            index = sBuf.indexOf(str, pos);
            index = sBuf.indexOf(str, pos);
            index = sBuf.indexOf(str, pos);
            index = sBuf.indexOf(str, pos);
            index = sBuf.indexOf(str, pos);
            index = sBuf.indexOf(str, pos);
            index = sBuf.indexOf(str, pos);
            index = sBuf.indexOf(str, pos);
            index = sBuf.indexOf(str, pos);
        }
    }
    public void testStringBufferLastIndexOf() {
        StringBuffer sBuf = STATIC_SBUF;
        String str = "t";
        int index;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            index = sBuf.lastIndexOf(str);
            index = sBuf.lastIndexOf(str);
            index = sBuf.lastIndexOf(str);
            index = sBuf.lastIndexOf(str);
            index = sBuf.lastIndexOf(str);
            index = sBuf.lastIndexOf(str);
            index = sBuf.lastIndexOf(str);
            index = sBuf.lastIndexOf(str);
            index = sBuf.lastIndexOf(str);
            index = sBuf.lastIndexOf(str);
        }
    }
    public void testStringBufferLastIndexOf1() {
        StringBuffer sBuf = STATIC_SBUF;
        int index, pos = 36;
        String str = "tom";
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            index = sBuf.lastIndexOf(str, pos);
            index = sBuf.lastIndexOf(str, pos);
            index = sBuf.lastIndexOf(str, pos);
            index = sBuf.lastIndexOf(str, pos);
            index = sBuf.lastIndexOf(str, pos);
            index = sBuf.lastIndexOf(str, pos);
            index = sBuf.lastIndexOf(str, pos);
            index = sBuf.lastIndexOf(str, pos);
            index = sBuf.lastIndexOf(str, pos);
            index = sBuf.lastIndexOf(str, pos);
        }
    }
}
