public class WFontConfiguration extends FontConfiguration {
    private boolean useCompatibilityFallbacks;
    public WFontConfiguration(SunFontManager fm) {
        super(fm);
        useCompatibilityFallbacks = "windows-1252".equals(encoding);
        initTables(encoding);
    }
    public WFontConfiguration(SunFontManager fm,
                              boolean preferLocaleFonts,
                              boolean preferPropFonts) {
        super(fm, preferLocaleFonts, preferPropFonts);
        useCompatibilityFallbacks = "windows-1252".equals(encoding);
    }
    protected void initReorderMap() {
        if (encoding.equalsIgnoreCase("windows-31j")) {
            localeMap = new Hashtable();
            localeMap.put("dialoginput.plain.japanese", "MS Mincho");
            localeMap.put("dialoginput.bold.japanese", "MS Mincho");
            localeMap.put("dialoginput.italic.japanese", "MS Mincho");
            localeMap.put("dialoginput.bolditalic.japanese", "MS Mincho");
        }
        reorderMap = new HashMap();
        reorderMap.put("UTF-8.hi", "devanagari");
        reorderMap.put("windows-1255", "hebrew");
        reorderMap.put("x-windows-874", "thai");
        reorderMap.put("windows-31j", "japanese");
        reorderMap.put("x-windows-949", "korean");
        reorderMap.put("GBK", "chinese-ms936");
        reorderMap.put("GB18030", "chinese-gb18030");
        reorderMap.put("x-windows-950", "chinese-ms950");
        reorderMap.put("x-MS950-HKSCS", split("chinese-ms950,chinese-hkscs"));
    }
    protected void setOsNameAndVersion(){
        super.setOsNameAndVersion();
        if (osName.startsWith("Windows")){
            int p, q;
            p = osName.indexOf(' ');
            if (p == -1){
                osName = null;
            }
            else{
                q = osName.indexOf(' ', p + 1);
                if (q == -1){
                    osName = osName.substring(p + 1);
                }
                else{
                    osName = osName.substring(p + 1, q);
                }
            }
            osVersion = null;
        }
    }
    public String getFallbackFamilyName(String fontName, String defaultFallback) {
        if (useCompatibilityFallbacks) {
            String compatibilityName = getCompatibilityFamilyName(fontName);
            if (compatibilityName != null) {
                return compatibilityName;
            }
        }
        return defaultFallback;
    }
    protected String makeAWTFontName(String platformFontName, String characterSubsetName) {
        String windowsCharset = (String) subsetCharsetMap.get(characterSubsetName);
        if (windowsCharset == null) {
            windowsCharset = "DEFAULT_CHARSET";
        }
        return platformFontName + "," + windowsCharset;
    }
    protected String getEncoding(String awtFontName, String characterSubsetName) {
        String encoding = (String) subsetEncodingMap.get(characterSubsetName);
        if (encoding == null) {
            encoding = "default";
        }
        return encoding;
    }
    protected Charset getDefaultFontCharset(String fontName) {
        return new WDefaultFontCharset(fontName);
    }
    public String getFaceNameFromComponentFontName(String componentFontName) {
        return componentFontName;
    }
    protected String getFileNameFromComponentFontName(String componentFontName) {
        return getFileNameFromPlatformName(componentFontName);
    }
    public String getTextComponentFontName(String familyName, int style) {
        FontDescriptor[] fontDescriptors = getFontDescriptors(familyName, style);
        String fontName = findFontWithCharset(fontDescriptors, textInputCharset);
        if (fontName == null) {
            fontName = findFontWithCharset(fontDescriptors, "DEFAULT_CHARSET");
        }
        return fontName;
    }
    private String findFontWithCharset(FontDescriptor[] fontDescriptors, String charset) {
        String fontName = null;
        for (int i = 0; i < fontDescriptors.length; i++) {
            String componentFontName = fontDescriptors[i].getNativeName();
            if (componentFontName.endsWith(charset)) {
                fontName = componentFontName;
            }
        }
        return fontName;
    }
    private static HashMap subsetCharsetMap = new HashMap();
    private static HashMap subsetEncodingMap = new HashMap();
    private static String textInputCharset;
    private void initTables(String defaultEncoding) {
        subsetCharsetMap.put("alphabetic", "ANSI_CHARSET");
        subsetCharsetMap.put("alphabetic/1252", "ANSI_CHARSET");
        subsetCharsetMap.put("alphabetic/default", "DEFAULT_CHARSET");
        subsetCharsetMap.put("arabic", "ARABIC_CHARSET");
        subsetCharsetMap.put("chinese-ms936", "GB2312_CHARSET");
        subsetCharsetMap.put("chinese-gb18030", "GB2312_CHARSET");
        subsetCharsetMap.put("chinese-ms950", "CHINESEBIG5_CHARSET");
        subsetCharsetMap.put("chinese-hkscs", "CHINESEBIG5_CHARSET");
        subsetCharsetMap.put("cyrillic", "RUSSIAN_CHARSET");
        subsetCharsetMap.put("devanagari", "DEFAULT_CHARSET");
        subsetCharsetMap.put("dingbats", "SYMBOL_CHARSET");
        subsetCharsetMap.put("greek", "GREEK_CHARSET");
        subsetCharsetMap.put("hebrew", "HEBREW_CHARSET");
        subsetCharsetMap.put("japanese", "SHIFTJIS_CHARSET");
        subsetCharsetMap.put("korean", "HANGEUL_CHARSET");
        subsetCharsetMap.put("latin", "ANSI_CHARSET");
        subsetCharsetMap.put("symbol", "SYMBOL_CHARSET");
        subsetCharsetMap.put("thai", "THAI_CHARSET");
        subsetEncodingMap.put("alphabetic", "default");
        subsetEncodingMap.put("alphabetic/1252", "windows-1252");
        subsetEncodingMap.put("alphabetic/default", defaultEncoding);
        subsetEncodingMap.put("arabic", "windows-1256");
        subsetEncodingMap.put("chinese-ms936", "GBK");
        subsetEncodingMap.put("chinese-gb18030", "GB18030");
        if ("x-MS950-HKSCS".equals(defaultEncoding)) {
            subsetEncodingMap.put("chinese-ms950", "x-MS950-HKSCS");
        } else {
            subsetEncodingMap.put("chinese-ms950", "x-windows-950"); 
        }
        subsetEncodingMap.put("chinese-hkscs", "sun.awt.HKSCS");
        subsetEncodingMap.put("cyrillic", "windows-1251");
        subsetEncodingMap.put("devanagari", "UTF-16LE");
        subsetEncodingMap.put("dingbats", "sun.awt.windows.WingDings");
        subsetEncodingMap.put("greek", "windows-1253");
        subsetEncodingMap.put("hebrew", "windows-1255");
        subsetEncodingMap.put("japanese", "windows-31j");
        subsetEncodingMap.put("korean", "x-windows-949");
        subsetEncodingMap.put("latin", "windows-1252");
        subsetEncodingMap.put("symbol", "sun.awt.Symbol");
        subsetEncodingMap.put("thai", "x-windows-874");
        if ("windows-1256".equals(defaultEncoding)) {
            textInputCharset = "ARABIC_CHARSET";
        } else if ("GBK".equals(defaultEncoding)) {
            textInputCharset = "GB2312_CHARSET";
        } else if ("GB18030".equals(defaultEncoding)) {
            textInputCharset = "GB2312_CHARSET";
        } else if ("x-windows-950".equals(defaultEncoding)) {
            textInputCharset = "CHINESEBIG5_CHARSET";
        } else if ("x-MS950-HKSCS".equals(defaultEncoding)) {
            textInputCharset = "CHINESEBIG5_CHARSET";
        } else if ("windows-1251".equals(defaultEncoding)) {
            textInputCharset = "RUSSIAN_CHARSET";
        } else if ("UTF-8".equals(defaultEncoding)) {
            textInputCharset = "DEFAULT_CHARSET";
        } else if ("windows-1253".equals(defaultEncoding)) {
            textInputCharset = "GREEK_CHARSET";
        } else if ("windows-1255".equals(defaultEncoding)) {
            textInputCharset = "HEBREW_CHARSET";
        } else if ("windows-31j".equals(defaultEncoding)) {
            textInputCharset = "SHIFTJIS_CHARSET";
        } else if ("x-windows-949".equals(defaultEncoding)) {
            textInputCharset = "HANGEUL_CHARSET";
        } else if ("x-windows-874".equals(defaultEncoding)) {
            textInputCharset = "THAI_CHARSET";
        } else {
            textInputCharset = "DEFAULT_CHARSET";
        }
    }
}
