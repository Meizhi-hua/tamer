public class PrintCharacterRanges {
  static class BooleanRange {
    private int begin;
    private int end;
    BooleanRange(int begin, int end) {
      this.begin = begin;
      this.end   = end;
    }
    int begin() { return begin; }
    int end()   { return end;   }
  }
  private static List recoverBooleanRanges(String methodName) throws Exception {
    List result = new ArrayList();
    int currentRangeStart = -1;
    Method method = Character.class.getDeclaredMethod(methodName, new Class[] { Character.TYPE });
    if (method == null) {
      throw new RuntimeException("No method \"" + methodName + "\"(C) found");
    }
    for (int i = 0; i <= 255; i++) {
      boolean methodRes = ((Boolean) method.invoke(null, new Object[] { new Character((char) i) })).booleanValue();
      if (methodRes) {
        if (currentRangeStart < 0) {
          currentRangeStart = i;
        }
        if (i == 255) {
          result.add(new BooleanRange(currentRangeStart, i));
        }
      } else {
        if (currentRangeStart >= 0) {
          result.add(new BooleanRange(currentRangeStart, i - 1));
          currentRangeStart = -1;
        }
      }
    }
    return result;
  }
  private static String describe(int num) {
    StringBuffer s = new StringBuffer();
    s.append(num);
    s.append(" ('");
    if (num > 32 && num < 123) {
      s.append((char) num);
    } else {
      s.append("\\u");
      String hex = Long.toHexString(num).toUpperCase();
      for (int i = 0; i < (4 - hex.length()); i++) {
        s.append('0');
      }
      s.append(hex);
    }
    s.append("')");
    return s.toString();
  }
  private static void printBooleanRanges(List ranges, String methodName) {
    System.out.print(methodName + ":");
    for (Iterator iter = ranges.iterator(); iter.hasNext();) {
      BooleanRange range = (BooleanRange) iter.next();
      System.out.print(" [ " + describe(range.begin()) + ", " + describe(range.end()) + " ]");
    }
    System.out.println("");
  }
  private static void recoverAndPrintBooleanRanges(String methodName) throws Exception {
    List ranges = recoverBooleanRanges(methodName);
    printBooleanRanges(ranges, methodName);
  }
  static class ShiftRange {
    private int begin;
    private int end;
    private int offset;
    ShiftRange(int begin, int end, int offset) {
      this.begin  = begin;
      this.end    = end;
      this.offset = offset;
    }
    int begin()  { return begin;  }
    int end()    { return end;    }
    int offset() { return offset; }
  }
  private static List recoverShiftRanges(String methodName) throws Exception {
    List result = new ArrayList();
    int currentRangeStart = -1;
    int currentRangeOffset = -1;
    Method method = Character.class.getDeclaredMethod(methodName, new Class[] { Character.TYPE });
    if (method == null) {
      throw new RuntimeException("No method \"" + methodName + "\"(C) found");
    }
    for (int i = 0; i <= 255; i++) {
      char methodRes = ((Character) method.invoke(null, new Object[] { new Character((char) i) })).charValue();
      if (methodRes != i) {
        int offset = methodRes - i;
        if (currentRangeStart < 0) {
          currentRangeStart = i;
        } else if (offset != currentRangeOffset) {
          result.add(new ShiftRange(currentRangeStart, i - 1, currentRangeOffset));
          currentRangeStart = i;
        }
        currentRangeOffset = offset;
        if (i == 255) {
          result.add(new ShiftRange(currentRangeStart, i, currentRangeOffset));
        }
      } else {
        if (currentRangeStart >= 0) {
          result.add(new ShiftRange(currentRangeStart, i - 1, currentRangeOffset));
          currentRangeStart = -1;
        }
      }
    }
    return result;
  }
  private static void printShiftRanges(List ranges, String methodName) {
    System.out.print(methodName + ":");
    boolean isFirst = true;
    for (Iterator iter = ranges.iterator(); iter.hasNext();) {
      ShiftRange range = (ShiftRange) iter.next();
      if (isFirst) {
        isFirst = false;
      } else {
        System.out.print(", ");
      }
      System.out.print(" [ " + describe(range.begin()) + ", " + describe(range.end()) + " ] -> [ " +
                       describe((range.begin() + range.offset())) + ", " + describe((range.end() + range.offset())) + " ] (" +
                       range.offset() + ")");
    }
    System.out.println("");
  }
  private static void recoverAndPrintShiftRanges(String methodName) throws Exception {
    List ranges = recoverShiftRanges(methodName);
    printShiftRanges(ranges, methodName);
  }
  public static void main(String[] args) {
    try {
      recoverAndPrintBooleanRanges("isDefined");
      recoverAndPrintBooleanRanges("isDigit");
      recoverAndPrintBooleanRanges("isIdentifierIgnorable");
      recoverAndPrintBooleanRanges("isISOControl");
      recoverAndPrintBooleanRanges("isJavaIdentifierPart");
      recoverAndPrintBooleanRanges("isJavaIdentifierStart");
      recoverAndPrintBooleanRanges("isLetter");
      recoverAndPrintBooleanRanges("isLetterOrDigit");
      recoverAndPrintBooleanRanges("isLowerCase");
      recoverAndPrintBooleanRanges("isMirrored");
      recoverAndPrintBooleanRanges("isSpaceChar");
      recoverAndPrintBooleanRanges("isTitleCase");
      recoverAndPrintBooleanRanges("isUnicodeIdentifierPart");
      recoverAndPrintBooleanRanges("isUnicodeIdentifierStart");
      recoverAndPrintBooleanRanges("isUpperCase");
      recoverAndPrintBooleanRanges("isWhitespace");
      recoverAndPrintShiftRanges("toUpperCase");
      recoverAndPrintShiftRanges("toLowerCase");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
