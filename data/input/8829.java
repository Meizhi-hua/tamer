public class UnicodeSpec {
    private static final int MAP_UNDEFINED = 0xFFFFFFFF;
    public UnicodeSpec() {
                this(0xffff);
    }
    public UnicodeSpec(int codePoint) {
        this.codePoint = codePoint;
        generalCategory = UNASSIGNED;
        bidiCategory = DIRECTIONALITY_UNDEFINED;
        mirrored = false;
        titleMap = MAP_UNDEFINED;
        upperMap = MAP_UNDEFINED;
        lowerMap = MAP_UNDEFINED;
        decimalValue = -1;
        digitValue = -1;
        numericValue = "";
                oldName = null;
                comment = null;
                name = null;
    }
    public String toString() {
        StringBuffer result = new StringBuffer(hex6(codePoint));
        if (getUpperMap() != MAP_UNDEFINED) {
            result.append(", upper=").append(hex6(upperMap));
        }
        if (getLowerMap() != MAP_UNDEFINED) {
            result.append(", lower=").append(hex6(lowerMap));
        }
        if (getTitleMap() != MAP_UNDEFINED) {
            result.append(", title=").append(hex6(titleMap));
        }
        return result.toString();
    }
    static String hex4(int n) {
        String q = Integer.toHexString(n & 0xFFFF).toUpperCase();
        return "0000".substring(Math.min(4, q.length())) + q;
    }
        static String hex6(int n) {
                String str = Integer.toHexString(n & 0xFFFFFF).toUpperCase();
                return "000000".substring(Math.min(6, str.length())) + str;
        }
    public static UnicodeSpec parse(String s) {
        UnicodeSpec spec = null;
        String[] tokens = null;
        try {
                        tokens = tokenSeparator.split(s, REQUIRED_FIELDS);
            spec = new UnicodeSpec();
            spec.setCodePoint(parseCodePoint(tokens[FIELD_VALUE]));
            spec.setName(parseName(tokens[FIELD_NAME]));
            spec.setGeneralCategory(parseGeneralCategory(tokens[FIELD_CATEGORY]));
            spec.setBidiCategory(parseBidiCategory(tokens[FIELD_BIDI]));
            spec.setCombiningClass(parseCombiningClass(tokens[FIELD_CLASS]));
            spec.setDecomposition(parseDecomposition(tokens[FIELD_DECOMPOSITION]));
            spec.setDecimalValue(parseDecimalValue(tokens[FIELD_DECIMAL]));
            spec.setDigitValue(parseDigitValue(tokens[FIELD_DIGIT]));
            spec.setNumericValue(parseNumericValue(tokens[FIELD_NUMERIC]));
            spec.setMirrored(parseMirrored(tokens[FIELD_MIRRORED]));
            spec.setOldName(parseOldName(tokens[FIELD_OLDNAME]));
            spec.setComment(parseComment(tokens[FIELD_COMMENT]));
            spec.setUpperMap(parseUpperMap(tokens[FIELD_UPPERCASE]));
            spec.setLowerMap(parseLowerMap(tokens[FIELD_LOWERCASE]));
            spec.setTitleMap(parseTitleMap(tokens[FIELD_TITLECASE]));
        }
        catch(Exception e) {
            spec = null;
            System.out.println("Error parsing spec line.");
        }
        return spec;
    }
    public static int parseCodePoint(String s) throws NumberFormatException {
        return Integer.parseInt(s, 16);
    }
    public static String parseName(String s) throws Exception {
        if (s==null) throw new Exception("Cannot parse name.");
        return s;
    }
    public static byte parseGeneralCategory(String s) throws Exception {
        byte category = GENERAL_CATEGORY_COUNT;
        for (byte x=0; x<generalCategoryList.length; x++) {
            if (s.equals(generalCategoryList[x][SHORT])) {
                category = x;
                break;
            }
        }
        if (category >= GENERAL_CATEGORY_COUNT) {
            throw new Exception("Could not parse general category.");
        }
        return category;
    }
    public static byte parseBidiCategory(String s) throws Exception {
        byte category = DIRECTIONALITY_CATEGORY_COUNT;
        for (byte x=0; x<bidiCategoryList.length; x++) {
            if (s.equals(bidiCategoryList[x][SHORT])) {
                category = x;
                break;
            }
        }
        if (category >= DIRECTIONALITY_CATEGORY_COUNT) {
            throw new Exception("Could not parse bidi category.");
        }
        return category;
    }
    public static int parseCombiningClass(String s) throws Exception {
        int combining = -1;
        if (s.length()>0) {
            combining = Integer.parseInt(s, 10);
        }
        return combining;
    }
    public static String parseDecomposition(String s) throws Exception {
        if (s==null) throw new Exception("Cannot parse decomposition.");
        return s;
    }
    public static int parseDecimalValue(String s) throws NumberFormatException {
        int value = -1;
        if (s.length() > 0) {
            value = Integer.parseInt(s, 10);
        }
        return value;
    }
    public static int parseDigitValue(String s) throws NumberFormatException {
        int value = -1;
        if (s.length() > 0) {
            value = Integer.parseInt(s, 10);
        }
        return value;
    }
    public static String parseNumericValue(String s) throws Exception {
        if (s == null) throw new Exception("Cannot parse numeric value.");
        return s;
    }
    public static String parseComment(String s) throws Exception {
        if (s == null) throw new Exception("Cannot parse comment.");
        return s;
    }
    public static boolean parseMirrored(String s) throws Exception {
        boolean mirrored;
        if (s.length() == 1) {
            if (s.charAt(0) == 'Y') {mirrored = true;}
            else if (s.charAt(0) == 'N') {mirrored = false;}
            else {throw new Exception("Cannot parse mirrored property.");}
        }
        else { throw new Exception("Cannot parse mirrored property.");}
        return mirrored;
    }
    public static String parseOldName(String s) throws Exception {
        if (s == null) throw new Exception("Cannot parse old name");
        return s;
    }
    public static int parseUpperMap(String s) throws NumberFormatException {
        int upperCase = MAP_UNDEFINED;
                int length = s.length();
        if (length >= 4 && length <=6) {
            upperCase = Integer.parseInt(s, 16);
        }
        else if (s.length() != 0) {
            throw new NumberFormatException();
        }
        return upperCase;
    }
    public static int parseLowerMap(String s) throws NumberFormatException {
        int lowerCase = MAP_UNDEFINED;
                int length = s.length();
        if (length >= 4 && length <= 6) {
            lowerCase = Integer.parseInt(s, 16);
        }
        else if (s.length() != 0) {
            throw new NumberFormatException();
        }
        return lowerCase;
    }
    public static int parseTitleMap(String s) throws NumberFormatException {
        int titleCase = MAP_UNDEFINED;
                int length = s.length();
        if (length >= 4 && length <= 6) {
            titleCase = Integer.parseInt(s, 16);
        }
        else if (s.length() != 0) {
            throw new NumberFormatException();
        }
        return titleCase;
    }
    public static UnicodeSpec[] readSpecFile(File file, int plane) throws FileNotFoundException {
                ArrayList list = new ArrayList(3000);
        UnicodeSpec[] result = null;
        int count = 0;
        BufferedReader f = new BufferedReader(new FileReader(file));
                String line = null;
        loop:
        while(true) {
            try {
                line = f.readLine();
            }
            catch (IOException e) {
                                break loop;
                        }
            if (line == null) break loop;
            UnicodeSpec item = parse(line.trim());
                        int specPlane = (int)(item.getCodePoint() >>> 16);
                        if (specPlane < plane) continue;
                        if (specPlane > plane) break;
            if (item != null) {
                                list.add(item);
            }
        }
                result = new UnicodeSpec[list.size()];
                list.toArray(result);
        return result;
    }
    void setCodePoint(int value) {
        codePoint = value;
    }
    public int getCodePoint() {
        return codePoint;
    }
    void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
    void setGeneralCategory(byte category) {
        generalCategory = category;
    }
    public byte getGeneralCategory() {
        return generalCategory;
    }
    void setBidiCategory(byte category) {
        bidiCategory = category;
    }
    public byte getBidiCategory() {
        return bidiCategory;
    }
    void setCombiningClass(int combiningClass) {
        this.combiningClass = combiningClass;
    }
    public int getCombiningClass() {
        return combiningClass;
    }
    void setDecomposition(String decomposition) {
        this.decomposition = decomposition;
    }
    public String getDecomposition() {
         return decomposition;
    }
    void setDecimalValue(int value) {
        decimalValue = value;
    }
    public int getDecimalValue() {
        return decimalValue;
    }
    public boolean isDecimalValue() {
        return decimalValue != -1;
    }
    void setDigitValue(int value) {
        digitValue = value;
    }
    public int getDigitValue() {
        return digitValue;
    }
    public boolean isDigitValue() {
        return digitValue != -1;
    }
    void setNumericValue(String value) {
        numericValue = value;
    }
    public String getNumericValue() {
        return numericValue;
    }
    public boolean isNumericValue() {
        return numericValue.length() > 0;
    }
    void setMirrored(boolean value) {
        mirrored = value;
    }
    public boolean isMirrored() {
        return mirrored;
    }
    void setOldName(String name) {
        oldName = name;
    }
    public String getOldName() {
        return oldName;
    }
    void setComment(String comment) {
        this.comment = comment;
    }
    public String getComment() {
        return comment;
    }
    void setUpperMap(int ch) {
        upperMap = ch;
    };
    public int getUpperMap() {
        return upperMap;
    }
    public boolean hasUpperMap() {
        return upperMap != MAP_UNDEFINED;
    }
    void setLowerMap(int ch) {
        lowerMap = ch;
    }
    public int getLowerMap() {
        return lowerMap;
    }
    public boolean hasLowerMap() {
        return lowerMap != MAP_UNDEFINED;
    }
    void setTitleMap(int ch) {
        titleMap = ch;
    }
    public int getTitleMap() {
        return titleMap;
    }
    public boolean hasTitleMap() {
        return titleMap != MAP_UNDEFINED;
    }
    int codePoint;         
    String name;            
    byte generalCategory;   
    byte bidiCategory;      
    int combiningClass;     
    String decomposition;   
    int decimalValue;       
    int digitValue;         
    String numericValue;    
    boolean mirrored;       
    String oldName;
    String comment;
    int upperMap;
    int lowerMap;
    int titleMap;
    static final int REQUIRED_FIELDS = 15;
    public static final byte
        UNASSIGNED                  =  0, 
        UPPERCASE_LETTER            =  1, 
        LOWERCASE_LETTER            =  2, 
        TITLECASE_LETTER            =  3, 
        MODIFIER_LETTER             =  4, 
        OTHER_LETTER                =  5, 
        NON_SPACING_MARK            =  6, 
        ENCLOSING_MARK              =  7, 
        COMBINING_SPACING_MARK      =  8, 
        DECIMAL_DIGIT_NUMBER        =  9, 
        LETTER_NUMBER               = 10, 
        OTHER_NUMBER                = 11, 
        SPACE_SEPARATOR             = 12, 
        LINE_SEPARATOR              = 13, 
        PARAGRAPH_SEPARATOR         = 14, 
        CONTROL                     = 15, 
        FORMAT                      = 16, 
        PRIVATE_USE                 = 18, 
        SURROGATE                   = 19, 
        DASH_PUNCTUATION            = 20, 
        START_PUNCTUATION           = 21, 
        END_PUNCTUATION             = 22, 
        CONNECTOR_PUNCTUATION       = 23, 
        OTHER_PUNCTUATION           = 24, 
        MATH_SYMBOL                 = 25, 
        CURRENCY_SYMBOL             = 26, 
        MODIFIER_SYMBOL             = 27, 
        OTHER_SYMBOL                = 28, 
        INITIAL_QUOTE_PUNCTUATION   = 29, 
        FINAL_QUOTE_PUNCTUATION     = 30, 
        GENERAL_CATEGORY_COUNT      = 31; 
    static final byte SHORT = 0, LONG = 1;
    static final String[][] generalCategoryList = {
        {"Cn", "UNASSIGNED"},
        {"Lu", "UPPERCASE_LETTER"},
        {"Ll", "LOWERCASE_LETTER"},
        {"Lt", "TITLECASE_LETTER"},
        {"Lm", "MODIFIER_LETTER"},
        {"Lo", "OTHER_LETTER"},
        {"Mn", "NON_SPACING_MARK"},
        {"Me", "ENCLOSING_MARK"},
        {"Mc", "COMBINING_SPACING_MARK"},
        {"Nd", "DECIMAL_DIGIT_NUMBER"},
        {"Nl", "LETTER_NUMBER"},
        {"No", "OTHER_NUMBER"},
        {"Zs", "SPACE_SEPARATOR"},
        {"Zl", "LINE_SEPARATOR"},
        {"Zp", "PARAGRAPH_SEPARATOR"},
        {"Cc", "CONTROL"},
        {"Cf", "FORMAT"},
        {"xx", "unused"},
        {"Co", "PRIVATE_USE"},
        {"Cs", "SURROGATE"},
        {"Pd", "DASH_PUNCTUATION"},
        {"Ps", "START_PUNCTUATION"},
        {"Pe", "END_PUNCTUATION"},
        {"Pc", "CONNECTOR_PUNCTUATION"},
        {"Po", "OTHER_PUNCTUATION"},
        {"Sm", "MATH_SYMBOL"},
        {"Sc", "CURRENCY_SYMBOL"},
        {"Sk", "MODIFIER_SYMBOL"},
        {"So", "OTHER_SYMBOL"},
        {"Pi", "INITIAL_QUOTE_PUNCTUATION"},
        {"Pf", "FINAL_QUOTE_PUNCTUATION"}
    };
    public static final byte
                DIRECTIONALITY_UNDEFINED                  = -1,
        DIRECTIONALITY_LEFT_TO_RIGHT              =  0, 
        DIRECTIONALITY_RIGHT_TO_LEFT              =  1, 
        DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC       =  2, 
        DIRECTIONALITY_EUROPEAN_NUMBER            =  3, 
        DIRECTIONALITY_EUROPEAN_NUMBER_SEPARATOR  =  4, 
        DIRECTIONALITY_EUROPEAN_NUMBER_TERMINATOR =  5, 
        DIRECTIONALITY_ARABIC_NUMBER              =  6, 
        DIRECTIONALITY_COMMON_NUMBER_SEPARATOR    =  7, 
        DIRECTIONALITY_NONSPACING_MARK            =  8, 
        DIRECTIONALITY_BOUNDARY_NEUTRAL           =  9, 
        DIRECTIONALITY_PARAGRAPH_SEPARATOR        = 10, 
        DIRECTIONALITY_SEGMENT_SEPARATOR          = 11, 
        DIRECTIONALITY_WHITESPACE                 = 12, 
        DIRECTIONALITY_OTHER_NEUTRALS              = 13, 
        DIRECTIONALITY_LEFT_TO_RIGHT_EMBEDDING    = 14, 
        DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE     = 15, 
        DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING    = 16, 
        DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE     = 17, 
        DIRECTIONALITY_POP_DIRECTIONAL_FORMAT     = 18, 
        DIRECTIONALITY_CATEGORY_COUNT             = 19; 
    static final String[][] bidiCategoryList = {
        {"L", "DIRECTIONALITY_LEFT_TO_RIGHT"},
        {"R", "DIRECTIONALITY_RIGHT_TO_LEFT"},
        {"AL", "DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC"},
        {"EN", "DIRECTIONALITY_EUROPEAN_NUMBER"},
        {"ES", "DIRECTIONALITY_EUROPEAN_NUMBER_SEPARATOR"},
        {"ET", "DIRECTIONALITY_EUROPEAN_NUMBER_TERMINATOR"},
        {"AN", "DIRECTIONALITY_ARABIC_NUMBER"},
        {"CS", "DIRECTIONALITY_COMMON_NUMBER_SEPARATOR"},
        {"NSM", "DIRECTIONALITY_NONSPACING_MARK"},
        {"BN", "DIRECTIONALITY_BOUNDARY_NEUTRAL"},
        {"B", "DIRECTIONALITY_PARAGRAPH_SEPARATOR"},
        {"S", "DIRECTIONALITY_SEGMENT_SEPARATOR"},
        {"WS", "DIRECTIONALITY_WHITESPACE"},
        {"ON", "DIRECTIONALITY_OTHER_NEUTRALS"},
        {"LRE", "DIRECTIONALITY_LEFT_TO_RIGHT_EMBEDDING"},
        {"LRO", "DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE"},
        {"RLE", "DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING"},
        {"RLO", "DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE"},
        {"PDF", "DIRECTIONALITY_POP_DIRECTIONAL_FORMAT"},
    };
    static final byte
        FIELD_VALUE         = 0,
        FIELD_NAME          = 1,
        FIELD_CATEGORY      = 2,
        FIELD_CLASS         = 3,
        FIELD_BIDI          = 4,
        FIELD_DECOMPOSITION = 5,
        FIELD_DECIMAL       = 6,
        FIELD_DIGIT         = 7,
        FIELD_NUMERIC       = 8,
        FIELD_MIRRORED      = 9,
        FIELD_OLDNAME       = 10,
        FIELD_COMMENT       = 11,
        FIELD_UPPERCASE     = 12,
        FIELD_LOWERCASE     = 13,
        FIELD_TITLECASE     = 14;
        static final Pattern tokenSeparator = Pattern.compile(";");
        public static void main(String[] args) {
                UnicodeSpec[] spec = null;
                if (args.length == 2 ) {
                        try {
                                File file = new File(args[0]);
                                int plane = Integer.parseInt(args[1]);
                                spec = UnicodeSpec.readSpecFile(file, plane);
                                System.out.println("UnicodeSpec[" + spec.length + "]:");
                                for (int x=0; x<spec.length; x++) {
                                        System.out.println(spec[x].toString());
                                }
                        }
                        catch(Exception e) {
                                e.printStackTrace();
                        }
                }
        }
}
