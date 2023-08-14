public class XResources_he extends XResourceBundle
{
  public Object[][] getContents()
  {
    return new Object[][]
  {
    { "ui_language", "he" }, { "help_language", "he" }, { "language", "he" },
    { "alphabet", new CharArrayWrapper(
      new char[]{ 0x05D0, 0x05D1, 0x05D2, 0x05D3, 0x05D4, 0x05D5, 0x05D6,
                  0x05D7, 0x05D8, 0x05D9, 0x05DA, 0x05DB, 0x05DC, 0x05DD,
                  0x05DE, 0x05DF, 0x05E0, 0x05E1 }) },
    { "tradAlphabet", new CharArrayWrapper(
      new char[]{ 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
                  'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
                  'Y', 'Z' }) },
    { "orientation", "RightToLeft" },
    { "numbering", "additive" },
    { "numberGroups", new IntArrayWrapper(new int[]{ 10, 1 }) },
    { "digits", new CharArrayWrapper(
      new char[]{ 0x05D0, 0x05D1, 0x05D2, 0x05D3, 0x05D4, 0x05D5, 0x05D6,
                  0x05D7, 0x05D8 }) },
    { "tens", new CharArrayWrapper(
      new char[]{ 0x05D9, 0x05DA, 0x05DB, 0x05DC, 0x05DD, 0x05DE, 0x05DF,
                  0x05E0, 0x05E1 }) },
    { "tables", new StringArrayWrapper(new String[]{ "tens", "digits" }) }
  };
  }
}