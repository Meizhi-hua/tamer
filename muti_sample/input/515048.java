public class XResourceBundle extends ListResourceBundle
{
  public static final String ERROR_RESOURCES =
    "org.apache.xalan.res.XSLTErrorResources", XSLT_RESOURCE =
    "org.apache.xml.utils.res.XResourceBundle", LANG_BUNDLE_NAME =
    "org.apache.xml.utils.res.XResources", MULT_ORDER =
    "multiplierOrder", MULT_PRECEDES = "precedes", MULT_FOLLOWS =
    "follows", LANG_ORIENTATION = "orientation", LANG_RIGHTTOLEFT =
    "rightToLeft", LANG_LEFTTORIGHT = "leftToRight", LANG_NUMBERING =
    "numbering", LANG_ADDITIVE = "additive", LANG_MULT_ADD =
    "multiplicative-additive", LANG_MULTIPLIER =
    "multiplier", LANG_MULTIPLIER_CHAR =
    "multiplierChar", LANG_NUMBERGROUPS = "numberGroups", LANG_NUM_TABLES =
    "tables", LANG_ALPHABET = "alphabet", LANG_TRAD_ALPHABET = "tradAlphabet";
  public static final XResourceBundle loadResourceBundle(
          String className, Locale locale) throws MissingResourceException
  {
    String suffix = getResourceSuffix(locale);
    try
    {
      String resourceName = className + suffix;
      return (XResourceBundle) ResourceBundle.getBundle(resourceName, locale);
    }
    catch (MissingResourceException e)
    {
      try  
      {
        return (XResourceBundle) ResourceBundle.getBundle(
          XSLT_RESOURCE, new Locale("en", "US"));
      }
      catch (MissingResourceException e2)
      {
        throw new MissingResourceException(
          "Could not load any resource bundles.", className, "");
      }
    }
  }
  private static final String getResourceSuffix(Locale locale)
  {
    String lang = locale.getLanguage();
    String country = locale.getCountry();
    String variant = locale.getVariant();
    String suffix = "_" + locale.getLanguage();
    if (lang.equals("zh"))
      suffix += "_" + country;
    if (country.equals("JP"))
      suffix += "_" + country + "_" + variant;
    return suffix;
  }
  public Object[][] getContents()
  {
    return new Object[][]
  {
    { "ui_language", "en" }, { "help_language", "en" }, { "language", "en" },
    { "alphabet", new CharArrayWrapper(new char[]{ 'A', 'B', 'C', 'D', 'E', 'F', 'G',
         'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 
         'V', 'W', 'X', 'Y', 'Z' })},
    { "tradAlphabet", new CharArrayWrapper(new char[]{ 'A', 'B', 'C', 'D', 'E', 'F', 
         'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 
         'U', 'V', 'W', 'X', 'Y', 'Z' }) },
    { "orientation", "LeftToRight" },
    { "numbering", "additive" },
  };
  }
}
