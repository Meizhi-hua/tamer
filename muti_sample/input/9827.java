final public class POSIX_Unicode {
    public static boolean isAlpha(int ch) {
        return Character.isAlphabetic(ch);
    }
    public static boolean isLower(int ch) {
        return Character.isLowerCase(ch);
    }
    public static boolean isUpper(int ch) {
        return Character.isUpperCase(ch);
    }
    public static boolean isSpace(int ch) {
        return ((((1 << Character.SPACE_SEPARATOR) |
                  (1 << Character.LINE_SEPARATOR) |
                  (1 << Character.PARAGRAPH_SEPARATOR)) >> Character.getType(ch)) & 1)
                   != 0 ||
               (ch >= 0x9 && ch <= 0xd) ||
               (ch == 0x85);
    }
    public static boolean isCntrl(int ch) {
        return Character.getType(ch) == Character.CONTROL;
    }
    public static boolean isPunct(int ch) {
        return ((((1 << Character.CONNECTOR_PUNCTUATION) |
                  (1 << Character.DASH_PUNCTUATION) |
                  (1 << Character.START_PUNCTUATION) |
                  (1 << Character.END_PUNCTUATION) |
                  (1 << Character.OTHER_PUNCTUATION) |
                  (1 << Character.INITIAL_QUOTE_PUNCTUATION) |
                  (1 << Character.FINAL_QUOTE_PUNCTUATION)) >> Character.getType(ch)) & 1)
              != 0;
    }
    public static boolean isHexDigit(int ch) {
        return Character.isDigit(ch) ||
               (ch >= 0x0030 && ch <= 0x0039) ||
               (ch >= 0x0041 && ch <= 0x0046) ||
               (ch >= 0x0061 && ch <= 0x0066) ||
               (ch >= 0xFF10 && ch <= 0xFF19) ||
               (ch >= 0xFF21 && ch <= 0xFF26) ||
               (ch >= 0xFF41 && ch <= 0xFF46);
    }
    public static boolean isDigit(int ch) {
        return Character.isDigit(ch);
    };
    public static boolean isAlnum(int ch) {
        return Character.isAlphabetic(ch) || Character.isDigit(ch);
    }
    public static boolean isBlank(int ch) {
        int type = Character.getType(ch);
        return isSpace(ch) &&
               ch != 0xa & ch != 0xb && ch !=0xc && ch != 0xd && ch != 0x85 &&
               type != Character.LINE_SEPARATOR &&
               type != Character.PARAGRAPH_SEPARATOR;
    }
    public static boolean isGraph(int ch) {
        int type = Character.getType(ch);
        return !(isSpace(ch) ||
                 Character.CONTROL == type ||
                 Character.SURROGATE == type ||
                 Character.UNASSIGNED == type);
    }
    public static boolean isPrint(int ch) {
        return (isGraph(ch) || isBlank(ch)) && !isCntrl(ch);
    }
    public static boolean isNoncharacterCodePoint(int ch) {
        return (ch & 0xfffe) == 0xfffe || (ch >= 0xfdd0 && ch <= 0xfdef);
    }
    public static boolean isWord(int ch) {
        return isAlpha(ch) ||
               ((((1 << Character.NON_SPACING_MARK) |
                  (1 << Character.ENCLOSING_MARK) |
                  (1 << Character.COMBINING_SPACING_MARK) |
                  (1 << Character.CONNECTOR_PUNCTUATION)) >> Character.getType(ch)) & 1)
               != 0 ||
               isDigit(ch);
    }
}
