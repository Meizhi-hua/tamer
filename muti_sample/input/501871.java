public class DefaultSoftKeyboardJAJP extends DefaultSoftKeyboard {
    private static final boolean USE_ENGLISH_PREDICT = true;
    private static final int KEYCODE_SWITCH_FULL_HIRAGANA = -301;
    private static final int KEYCODE_SWITCH_FULL_KATAKANA = -302;
    private static final int KEYCODE_SWITCH_FULL_ALPHABET = -303;
    private static final int KEYCODE_SWITCH_FULL_NUMBER = -304;
    private static final int KEYCODE_SWITCH_HALF_KATAKANA = -306;
    private static final int KEYCODE_SWITCH_HALF_ALPHABET = -307;
    private static final int KEYCODE_SWITCH_HALF_NUMBER = -308;
    private static final int KEYCODE_SELECT_CASE = -309;
    private static final int KEYCODE_EISU_KANA = -305;
    private static final int KEYCODE_NOP = -310;
    private static final int[] JP_MODE_CYCLE_TABLE = {
        KEYMODE_JA_FULL_HIRAGANA, KEYMODE_JA_HALF_ALPHABET, KEYMODE_JA_HALF_NUMBER
    };
    private static final int INPUT_TYPE_TOGGLE = 1;
    private static final int INPUT_TYPE_INSTANT = 2;
    private static final int KEY_NUMBER_12KEY = 20;
    private static final String[][] JP_FULL_HIRAGANA_CYCLE_TABLE = {
        {"\u3042", "\u3044", "\u3046", "\u3048", "\u304a", "\u3041", "\u3043", "\u3045", "\u3047", "\u3049"},
        {"\u304b", "\u304d", "\u304f", "\u3051", "\u3053"},
        {"\u3055", "\u3057", "\u3059", "\u305b", "\u305d"},
        {"\u305f", "\u3061", "\u3064", "\u3066", "\u3068", "\u3063"},
        {"\u306a", "\u306b", "\u306c", "\u306d", "\u306e"},
        {"\u306f", "\u3072", "\u3075", "\u3078", "\u307b"},
        {"\u307e", "\u307f", "\u3080", "\u3081", "\u3082"},
        {"\u3084", "\u3086", "\u3088", "\u3083", "\u3085", "\u3087"},
        {"\u3089", "\u308a", "\u308b", "\u308c", "\u308d"},
        {"\u308f", "\u3092", "\u3093", "\u308e", "\u30fc"},
        {"\u3001", "\u3002", "\uff1f", "\uff01", "\u30fb", "\u3000"},
    };
    private static final HashMap<String, String> JP_FULL_HIRAGANA_REPLACE_TABLE = new HashMap<String, String>() {{
          put("\u3042", "\u3041"); put("\u3044", "\u3043"); put("\u3046", "\u3045"); put("\u3048", "\u3047"); put("\u304a", "\u3049");
          put("\u3041", "\u3042"); put("\u3043", "\u3044"); put("\u3045", "\u30f4"); put("\u3047", "\u3048"); put("\u3049", "\u304a");
          put("\u304b", "\u304c"); put("\u304d", "\u304e"); put("\u304f", "\u3050"); put("\u3051", "\u3052"); put("\u3053", "\u3054");
          put("\u304c", "\u304b"); put("\u304e", "\u304d"); put("\u3050", "\u304f"); put("\u3052", "\u3051"); put("\u3054", "\u3053");
          put("\u3055", "\u3056"); put("\u3057", "\u3058"); put("\u3059", "\u305a"); put("\u305b", "\u305c"); put("\u305d", "\u305e");
          put("\u3056", "\u3055"); put("\u3058", "\u3057"); put("\u305a", "\u3059"); put("\u305c", "\u305b"); put("\u305e", "\u305d");
          put("\u305f", "\u3060"); put("\u3061", "\u3062"); put("\u3064", "\u3063"); put("\u3066", "\u3067"); put("\u3068", "\u3069");
          put("\u3060", "\u305f"); put("\u3062", "\u3061"); put("\u3063", "\u3065"); put("\u3067", "\u3066"); put("\u3069", "\u3068");
          put("\u3065", "\u3064"); put("\u30f4", "\u3046");
          put("\u306f", "\u3070"); put("\u3072", "\u3073"); put("\u3075", "\u3076"); put("\u3078", "\u3079"); put("\u307b", "\u307c");
          put("\u3070", "\u3071"); put("\u3073", "\u3074"); put("\u3076", "\u3077"); put("\u3079", "\u307a"); put("\u307c", "\u307d");
          put("\u3071", "\u306f"); put("\u3074", "\u3072"); put("\u3077", "\u3075"); put("\u307a", "\u3078"); put("\u307d", "\u307b");
          put("\u3084", "\u3083"); put("\u3086", "\u3085"); put("\u3088", "\u3087");
          put("\u3083", "\u3084"); put("\u3085", "\u3086"); put("\u3087", "\u3088");
          put("\u308f", "\u308e");
          put("\u308e", "\u308f");
          put("\u309b", "\u309c");
          put("\u309c", "\u309b");
    }};
    private static final String[][] JP_FULL_KATAKANA_CYCLE_TABLE = {
        {"\u30a2", "\u30a4", "\u30a6", "\u30a8", "\u30aa", "\u30a1", "\u30a3",
         "\u30a5", "\u30a7", "\u30a9"},
        {"\u30ab", "\u30ad", "\u30af", "\u30b1", "\u30b3"},
        {"\u30b5", "\u30b7", "\u30b9", "\u30bb", "\u30bd"},
        {"\u30bf", "\u30c1", "\u30c4", "\u30c6", "\u30c8", "\u30c3"},
        {"\u30ca", "\u30cb", "\u30cc", "\u30cd", "\u30ce"},
        {"\u30cf", "\u30d2", "\u30d5", "\u30d8", "\u30db"},
        {"\u30de", "\u30df", "\u30e0", "\u30e1", "\u30e2"},
        {"\u30e4", "\u30e6", "\u30e8", "\u30e3", "\u30e5", "\u30e7"},
        {"\u30e9", "\u30ea", "\u30eb", "\u30ec", "\u30ed"},
        {"\u30ef", "\u30f2", "\u30f3", "\u30ee", "\u30fc"},
        {"\u3001", "\u3002", "\uff1f", "\uff01", "\u30fb", "\u3000"}
    };
    private static final HashMap<String,String> JP_FULL_KATAKANA_REPLACE_TABLE = new HashMap<String,String>() {{
        put("\u30a2", "\u30a1"); put("\u30a4", "\u30a3"); put("\u30a6", "\u30a5"); put("\u30a8", "\u30a7"); put("\u30aa", "\u30a9");
        put("\u30a1", "\u30a2"); put("\u30a3", "\u30a4"); put("\u30a5", "\u30f4"); put("\u30a7", "\u30a8"); put("\u30a9", "\u30aa");
        put("\u30ab", "\u30ac"); put("\u30ad", "\u30ae"); put("\u30af", "\u30b0"); put("\u30b1", "\u30b2"); put("\u30b3", "\u30b4");
        put("\u30ac", "\u30ab"); put("\u30ae", "\u30ad"); put("\u30b0", "\u30af"); put("\u30b2", "\u30b1"); put("\u30b4", "\u30b3");
        put("\u30b5", "\u30b6"); put("\u30b7", "\u30b8"); put("\u30b9", "\u30ba"); put("\u30bb", "\u30bc"); put("\u30bd", "\u30be");
        put("\u30b6", "\u30b5"); put("\u30b8", "\u30b7"); put("\u30ba", "\u30b9"); put("\u30bc", "\u30bb"); put("\u30be", "\u30bd");
        put("\u30bf", "\u30c0"); put("\u30c1", "\u30c2"); put("\u30c4", "\u30c3"); put("\u30c6", "\u30c7"); put("\u30c8", "\u30c9");
        put("\u30c0", "\u30bf"); put("\u30c2", "\u30c1"); put("\u30c3", "\u30c5"); put("\u30c7", "\u30c6"); put("\u30c9", "\u30c8");
        put("\u30c5", "\u30c4"); put("\u30f4", "\u30a6");
        put("\u30cf", "\u30d0"); put("\u30d2", "\u30d3"); put("\u30d5", "\u30d6"); put("\u30d8", "\u30d9"); put("\u30db", "\u30dc");
        put("\u30d0", "\u30d1"); put("\u30d3", "\u30d4"); put("\u30d6", "\u30d7"); put("\u30d9", "\u30da"); put("\u30dc", "\u30dd");
        put("\u30d1", "\u30cf"); put("\u30d4", "\u30d2"); put("\u30d7", "\u30d5"); put("\u30da", "\u30d8"); put("\u30dd", "\u30db");
        put("\u30e4", "\u30e3"); put("\u30e6", "\u30e5"); put("\u30e8", "\u30e7");
        put("\u30e3", "\u30e4"); put("\u30e5", "\u30e6"); put("\u30e7", "\u30e8");
        put("\u30ef", "\u30ee");
        put("\u30ee", "\u30ef");
    }};
    private static final String[][] JP_HALF_KATAKANA_CYCLE_TABLE = {
        {"\uff71", "\uff72", "\uff73", "\uff74", "\uff75", "\uff67", "\uff68", "\uff69", "\uff6a", "\uff6b"},
        {"\uff76", "\uff77", "\uff78", "\uff79", "\uff7a"},
        {"\uff7b", "\uff7c", "\uff7d", "\uff7e", "\uff7f"},
        {"\uff80", "\uff81", "\uff82", "\uff83", "\uff84", "\uff6f"},
        {"\uff85", "\uff86", "\uff87", "\uff88", "\uff89"},
        {"\uff8a", "\uff8b", "\uff8c", "\uff8d", "\uff8e"},
        {"\uff8f", "\uff90", "\uff91", "\uff92", "\uff93"},
        {"\uff94", "\uff95", "\uff96", "\uff6c", "\uff6d", "\uff6e"},
        {"\uff97", "\uff98", "\uff99", "\uff9a", "\uff9b"},
        {"\uff9c", "\uff66", "\uff9d", "\uff70"},
        {"\uff64", "\uff61", "?", "!", "\uff65", " "},
    };
    private static final HashMap<String,String> JP_HALF_KATAKANA_REPLACE_TABLE = new HashMap<String,String>() {{
        put("\uff71", "\uff67");  put("\uff72", "\uff68");  put("\uff73", "\uff69");  put("\uff74", "\uff6a");  put("\uff75", "\uff6b");
        put("\uff67", "\uff71");  put("\uff68", "\uff72");  put("\uff69", "\uff73\uff9e");  put("\uff6a", "\uff74");  put("\uff6b", "\uff75");
        put("\uff76", "\uff76\uff9e"); put("\uff77", "\uff77\uff9e"); put("\uff78", "\uff78\uff9e"); put("\uff79", "\uff79\uff9e"); put("\uff7a", "\uff7a\uff9e");
        put("\uff76\uff9e", "\uff76"); put("\uff77\uff9e", "\uff77"); put("\uff78\uff9e", "\uff78"); put("\uff79\uff9e", "\uff79"); put("\uff7a\uff9e", "\uff7a");
        put("\uff7b", "\uff7b\uff9e"); put("\uff7c", "\uff7c\uff9e"); put("\uff7d", "\uff7d\uff9e"); put("\uff7e", "\uff7e\uff9e"); put("\uff7f", "\uff7f\uff9e");
        put("\uff7b\uff9e", "\uff7b"); put("\uff7c\uff9e", "\uff7c"); put("\uff7d\uff9e", "\uff7d"); put("\uff7e\uff9e", "\uff7e"); put("\uff7f\uff9e", "\uff7f");
        put("\uff80", "\uff80\uff9e"); put("\uff81", "\uff81\uff9e"); put("\uff82", "\uff6f");  put("\uff83", "\uff83\uff9e"); put("\uff84", "\uff84\uff9e");
        put("\uff80\uff9e", "\uff80"); put("\uff81\uff9e", "\uff81"); put("\uff6f", "\uff82\uff9e"); put("\uff83\uff9e", "\uff83"); put("\uff84\uff9e", "\uff84");
        put("\uff82\uff9e", "\uff82");
        put("\uff8a", "\uff8a\uff9e"); put("\uff8b", "\uff8b\uff9e"); put("\uff8c", "\uff8c\uff9e"); put("\uff8d", "\uff8d\uff9e"); put("\uff8e", "\uff8e\uff9e");
        put("\uff8a\uff9e", "\uff8a\uff9f");put("\uff8b\uff9e", "\uff8b\uff9f");put("\uff8c\uff9e", "\uff8c\uff9f");put("\uff8d\uff9e", "\uff8d\uff9f");put("\uff8e\uff9e", "\uff8e\uff9f");
        put("\uff8a\uff9f", "\uff8a"); put("\uff8b\uff9f", "\uff8b"); put("\uff8c\uff9f", "\uff8c"); put("\uff8d\uff9f", "\uff8d"); put("\uff8e\uff9f", "\uff8e");
        put("\uff94", "\uff6c");  put("\uff95", "\uff6d");  put("\uff96", "\uff6e");
        put("\uff6c", "\uff94");  put("\uff6d", "\uff95");  put("\uff6e", "\uff96");
        put("\uff9c", "\uff9c"); put("\uff73\uff9e", "\uff73");
    }};
    private static final String[][] JP_FULL_ALPHABET_CYCLE_TABLE = {
        {"\uff0e", "\uff20", "\uff0d", "\uff3f", "\uff0f", "\uff1a", "\uff5e", "\uff11"},
        {"\uff41", "\uff42", "\uff43", "\uff21", "\uff22", "\uff23", "\uff12"},
        {"\uff44", "\uff45", "\uff46", "\uff24", "\uff25", "\uff26", "\uff13"},
        {"\uff47", "\uff48", "\uff49", "\uff27", "\uff28", "\uff29", "\uff14"},
        {"\uff4a", "\uff4b", "\uff4c", "\uff2a", "\uff2b", "\uff2c", "\uff15"},
        {"\uff4d", "\uff4e", "\uff4f", "\uff2d", "\uff2e", "\uff2f", "\uff16"},
        {"\uff50", "\uff51", "\uff52", "\uff53", "\uff30", "\uff31", "\uff32", "\uff33", "\uff17"},
        {"\uff54", "\uff55", "\uff56", "\uff34", "\uff35", "\uff36", "\uff18"},
        {"\uff57", "\uff58", "\uff59", "\uff5a", "\uff37", "\uff38", "\uff39", "\uff3a", "\uff19"},
        {"\uff0d", "\uff10"},
        {"\uff0c", "\uff0e", "\uff1f", "\uff01", "\u30fb", "\u3000"}
    };
    private static final HashMap<String,String> JP_FULL_ALPHABET_REPLACE_TABLE = new HashMap<String,String>() {{
        put("\uff21", "\uff41"); put("\uff22", "\uff42"); put("\uff23", "\uff43"); put("\uff24", "\uff44"); put("\uff25", "\uff45"); 
        put("\uff41", "\uff21"); put("\uff42", "\uff22"); put("\uff43", "\uff23"); put("\uff44", "\uff24"); put("\uff45", "\uff25"); 
        put("\uff26", "\uff46"); put("\uff27", "\uff47"); put("\uff28", "\uff48"); put("\uff29", "\uff49"); put("\uff2a", "\uff4a"); 
        put("\uff46", "\uff26"); put("\uff47", "\uff27"); put("\uff48", "\uff28"); put("\uff49", "\uff29"); put("\uff4a", "\uff2a"); 
        put("\uff2b", "\uff4b"); put("\uff2c", "\uff4c"); put("\uff2d", "\uff4d"); put("\uff2e", "\uff4e"); put("\uff2f", "\uff4f"); 
        put("\uff4b", "\uff2b"); put("\uff4c", "\uff2c"); put("\uff4d", "\uff2d"); put("\uff4e", "\uff2e"); put("\uff4f", "\uff2f"); 
        put("\uff30", "\uff50"); put("\uff31", "\uff51"); put("\uff32", "\uff52"); put("\uff33", "\uff53"); put("\uff34", "\uff54"); 
        put("\uff50", "\uff30"); put("\uff51", "\uff31"); put("\uff52", "\uff32"); put("\uff53", "\uff33"); put("\uff54", "\uff34"); 
        put("\uff35", "\uff55"); put("\uff36", "\uff56"); put("\uff37", "\uff57"); put("\uff38", "\uff58"); put("\uff39", "\uff59"); 
        put("\uff55", "\uff35"); put("\uff56", "\uff36"); put("\uff57", "\uff37"); put("\uff58", "\uff38"); put("\uff59", "\uff39"); 
        put("\uff3a", "\uff5a"); 
        put("\uff5a", "\uff3a"); 
    }};
    private static final String[][] JP_HALF_ALPHABET_CYCLE_TABLE = {
        {".", "@", "-", "_", "/", ":", "~", "1"},
        {"a", "b", "c", "A", "B", "C", "2"},
        {"d", "e", "f", "D", "E", "F", "3"},
        {"g", "h", "i", "G", "H", "I", "4"},
        {"j", "k", "l", "J", "K", "L", "5"},
        {"m", "n", "o", "M", "N", "O", "6"},
        {"p", "q", "r", "s", "P", "Q", "R", "S", "7"},
        {"t", "u", "v", "T", "U", "V", "8"},
        {"w", "x", "y", "z", "W", "X", "Y", "Z", "9"},
        {"-", "0"},
        {",", ".", "?", "!", ";", " "}
    };
    private static final HashMap<String,String> JP_HALF_ALPHABET_REPLACE_TABLE = new HashMap<String,String>() {{
        put("A", "a"); put("B", "b"); put("C", "c"); put("D", "d"); put("E", "e"); 
        put("a", "A"); put("b", "B"); put("c", "C"); put("d", "D"); put("e", "E"); 
        put("F", "f"); put("G", "g"); put("H", "h"); put("I", "i"); put("J", "j"); 
        put("f", "F"); put("g", "G"); put("h", "H"); put("i", "I"); put("j", "J"); 
        put("K", "k"); put("L", "l"); put("M", "m"); put("N", "n"); put("O", "o"); 
        put("k", "K"); put("l", "L"); put("m", "M"); put("n", "N"); put("o", "O"); 
        put("P", "p"); put("Q", "q"); put("R", "r"); put("S", "s"); put("T", "t"); 
        put("p", "P"); put("q", "Q"); put("r", "R"); put("s", "S"); put("t", "T"); 
        put("U", "u"); put("V", "v"); put("W", "w"); put("X", "x"); put("Y", "y"); 
        put("u", "U"); put("v", "V"); put("w", "W"); put("x", "X"); put("y", "Y"); 
        put("Z", "z"); 
        put("z", "Z"); 
    }};
    private static final char[] INSTANT_CHAR_CODE_FULL_NUMBER = 
        "\uff11\uff12\uff13\uff14\uff15\uff16\uff17\uff18\uff19\uff10\uff03\uff0a".toCharArray();
    private static final char[] INSTANT_CHAR_CODE_HALF_NUMBER = 
        "1234567890#*".toCharArray();
    private static final int INVALID_KEYMODE = -1;
    private static final int KEY_INDEX_CHANGE_MODE_12KEY = 15;
    private static final int KEY_INDEX_CHANGE_MODE_QWERTY = 29;
    private int mInputType = INPUT_TYPE_TOGGLE;
    private int mPrevInputKeyCode = 0;
    private char[] mCurrentInstantTable = null;
    private int[] mLimitedKeyMode = null;
    private int mPreferenceKeyMode = INVALID_KEYMODE;
    private int mLastInputType = 0;
    private boolean mEnableAutoCaps = true;
    private int mPopupResId = 0;
    private boolean mIsInputTypeNull = false;
    private SharedPreferences.Editor mPrefEditor = null;
    private Keyboard.Key mChangeModeKey = null;
    public DefaultSoftKeyboardJAJP() {
        mCurrentLanguage     = LANG_JA;
        mCurrentKeyboardType = KEYBOARD_12KEY;
        mShiftOn             = KEYBOARD_SHIFT_OFF;
        mCurrentKeyMode      = KEYMODE_JA_FULL_HIRAGANA;
    }
    @Override protected void createKeyboards(OpenWnn parent) {
        mKeyboard = new Keyboard[3][2][4][2][8][2];
        if (mHardKeyboardHidden) {
            if (mDisplayMode == DefaultSoftKeyboard.PORTRAIT) {
                createKeyboardsPortrait(parent);
            } else {
                createKeyboardsLandscape(parent);
            }
            if (mCurrentKeyboardType == KEYBOARD_12KEY) {
                mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.CHANGE_MODE,
                                              OpenWnnJAJP.ENGINE_MODE_OPT_TYPE_12KEY));
            } else {
                mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.CHANGE_MODE,
                                              OpenWnnJAJP.ENGINE_MODE_OPT_TYPE_QWERTY));
            }
        } else {
            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.CHANGE_MODE,
                                          OpenWnnJAJP.ENGINE_MODE_OPT_TYPE_QWERTY));
        }
    }
    private void commitText() {
        if (!mNoInput) {
            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.COMMIT_COMPOSING_TEXT));
        }
    }
    public void changeKeyMode(int keyMode) {
        int targetMode = filterKeyMode(keyMode);
        if (targetMode == INVALID_KEYMODE) {
            return;
        }
        commitText();
        if (mCapsLock) {
            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.INPUT_SOFT_KEY,
                                          new KeyEvent(KeyEvent.ACTION_UP,
                                                       KeyEvent.KEYCODE_SHIFT_LEFT)));
            mCapsLock = false;
        }
        mShiftOn = KEYBOARD_SHIFT_OFF;
        Keyboard kbd = getModeChangeKeyboard(targetMode);
        mCurrentKeyMode = targetMode;
        mPrevInputKeyCode = 0;
        int mode = OpenWnnEvent.Mode.DIRECT;
        switch (targetMode) {
        case KEYMODE_JA_FULL_HIRAGANA:
            mInputType = INPUT_TYPE_TOGGLE;
            mode = OpenWnnEvent.Mode.DEFAULT;
            break;
        case KEYMODE_JA_HALF_ALPHABET:
            if (USE_ENGLISH_PREDICT) {
                mInputType = INPUT_TYPE_TOGGLE;
                mode = OpenWnnEvent.Mode.NO_LV1_CONV;
            } else {
                mInputType = INPUT_TYPE_TOGGLE;
                mode = OpenWnnEvent.Mode.DIRECT;
            }
            break;
        case KEYMODE_JA_FULL_NUMBER:
            mInputType = INPUT_TYPE_INSTANT;
            mode = OpenWnnEvent.Mode.DIRECT;
            mCurrentInstantTable = INSTANT_CHAR_CODE_FULL_NUMBER;
            break;
        case KEYMODE_JA_HALF_NUMBER:
            mInputType = INPUT_TYPE_INSTANT;
            mode = OpenWnnEvent.Mode.DIRECT;
            mCurrentInstantTable = INSTANT_CHAR_CODE_HALF_NUMBER;
            break;
        case KEYMODE_JA_FULL_KATAKANA:
            mInputType = INPUT_TYPE_TOGGLE;
            mode = OpenWnnJAJP.ENGINE_MODE_FULL_KATAKANA;
            break;
        case KEYMODE_JA_FULL_ALPHABET:
            mInputType = INPUT_TYPE_TOGGLE;
            mode = OpenWnnEvent.Mode.DIRECT;
            break;
        case KEYMODE_JA_HALF_KATAKANA:
            mInputType = INPUT_TYPE_TOGGLE;
            mode = OpenWnnJAJP.ENGINE_MODE_HALF_KATAKANA;
            break;
        default:
            break;
        }
        setStatusIcon();
        changeKeyboard(kbd);
        mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.CHANGE_MODE, mode));
    }
     @Override public View initView(OpenWnn parent, int width, int height) {
        View view = super.initView(parent, width, height);
        changeKeyboard(mKeyboard[mCurrentLanguage][mDisplayMode][mCurrentKeyboardType][mShiftOn][mCurrentKeyMode][0]);
        return view;
     }
    @Override protected boolean changeKeyboard(Keyboard keyboard) {
        if (keyboard != null) {
            if (mIsInputTypeNull && mChangeModeKey != null) {
                mChangeModeKey.popupResId = mPopupResId;
            }
            List<Keyboard.Key> keys = keyboard.getKeys();
            int keyIndex = (KEY_NUMBER_12KEY < keys.size())
                ? KEY_INDEX_CHANGE_MODE_QWERTY : KEY_INDEX_CHANGE_MODE_12KEY;
            mChangeModeKey = keys.get(keyIndex);
            if (mIsInputTypeNull && mChangeModeKey != null) {
                mPopupResId = mChangeModeKey.popupResId;
                mChangeModeKey.popupResId = 0;
            }
        }
        return super.changeKeyboard(keyboard);
    }
    @Override public void changeKeyboardType(int type) {
        commitText();
        Keyboard kbd = getTypeChangeKeyboard(type);
        if (kbd != null) {
            mCurrentKeyboardType = type;
            mPrefEditor.putBoolean("opt_enable_qwerty", type == KEYBOARD_QWERTY);
            mPrefEditor.commit();
            changeKeyboard(kbd);
        }
        if (type == KEYBOARD_12KEY) {
            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.CHANGE_MODE, OpenWnnJAJP.ENGINE_MODE_OPT_TYPE_12KEY));
        } else {
            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.CHANGE_MODE, OpenWnnJAJP.ENGINE_MODE_OPT_TYPE_QWERTY));
        }
    }
    @Override public void onKey(int primaryCode, int[] keyCodes) {
        if (mDisableKeyInput) {
            return;
        }
        switch (primaryCode) {
        case KEYCODE_JP12_TOGGLE_MODE:
        case KEYCODE_QWERTY_TOGGLE_MODE:
            if (!mIsInputTypeNull) {
                nextKeyMode();
            }
            break;
        case DefaultSoftKeyboard.KEYCODE_QWERTY_BACKSPACE:
        case KEYCODE_JP12_BACKSPACE:
            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.INPUT_SOFT_KEY,
                                          new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL)));
            break;
        case DefaultSoftKeyboard.KEYCODE_QWERTY_SHIFT:
            toggleShiftLock();
            break;
        case DefaultSoftKeyboard.KEYCODE_QWERTY_ALT:
            processAltKey();
            break;
        case KEYCODE_QWERTY_ENTER:
        case KEYCODE_JP12_ENTER:
            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.INPUT_SOFT_KEY,
                                          new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER)));
            break;
        case KEYCODE_JP12_REVERSE:
            if (!mNoInput) {
                mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.TOGGLE_REVERSE_CHAR, mCurrentCycleTable));
            }
            break;
        case KEYCODE_QWERTY_KBD:
            changeKeyboardType(KEYBOARD_12KEY);
            break;
        case KEYCODE_JP12_KBD:
            changeKeyboardType(KEYBOARD_QWERTY);
            break;
        case KEYCODE_JP12_EMOJI:
        case KEYCODE_QWERTY_EMOJI:
                commitText();
                mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.CHANGE_MODE, OpenWnnJAJP.ENGINE_MODE_SYMBOL));
            break;
        case KEYCODE_JP12_1:
        case KEYCODE_JP12_2:
        case KEYCODE_JP12_3:
        case KEYCODE_JP12_4:
        case KEYCODE_JP12_5:
        case KEYCODE_JP12_6:
        case KEYCODE_JP12_7:
        case KEYCODE_JP12_8:
        case KEYCODE_JP12_9:
        case KEYCODE_JP12_0:
        case KEYCODE_JP12_SHARP:
            if (mInputType == INPUT_TYPE_INSTANT) {
                commitText();
                mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.INPUT_CHAR,
                                              mCurrentInstantTable[getTableIndex(primaryCode)]));
            } else {
                if ((mPrevInputKeyCode != primaryCode)) {
                    mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.TOUCH_OTHER_KEY));
                    if ((mCurrentKeyMode == KEYMODE_JA_HALF_ALPHABET)
                            && (primaryCode == KEYCODE_JP12_SHARP)) {
                        commitText();
                    }
                }
                String[][] cycleTable = getCycleTable();
                if (cycleTable == null) {
                    Log.e("OpenWnn", "not founds cycle table");
                } else {
                    int index = getTableIndex(primaryCode);
                    mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.TOGGLE_CHAR, cycleTable[index]));
                    mCurrentCycleTable = cycleTable[index];
                }
                mPrevInputKeyCode = primaryCode;
            }
            break;
        case KEYCODE_JP12_ASTER:
            if (mInputType == INPUT_TYPE_INSTANT) {
                commitText();
                mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.INPUT_CHAR,
                                              mCurrentInstantTable[getTableIndex(primaryCode)]));
            } else {
                if (!mNoInput) {
                    HashMap replaceTable = getReplaceTable();
                    if (replaceTable == null) {
                        Log.e("OpenWnn", "not founds replace table");
                    } else {
                        mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.REPLACE_CHAR, replaceTable));
                        mPrevInputKeyCode = primaryCode;
                    }
                }
            }
            break;
        case KEYCODE_SWITCH_FULL_HIRAGANA:
            changeKeyMode(KEYMODE_JA_FULL_HIRAGANA);
            break;
        case KEYCODE_SWITCH_FULL_KATAKANA:
            changeKeyMode(KEYMODE_JA_FULL_KATAKANA);
            break;
        case KEYCODE_SWITCH_FULL_ALPHABET:
            changeKeyMode(KEYMODE_JA_FULL_ALPHABET);
            break;
        case KEYCODE_SWITCH_FULL_NUMBER:
            changeKeyMode(KEYMODE_JA_FULL_NUMBER);
            break;
        case KEYCODE_SWITCH_HALF_KATAKANA:
            changeKeyMode(KEYMODE_JA_HALF_KATAKANA);
            break;
        case KEYCODE_SWITCH_HALF_ALPHABET: 
            changeKeyMode(KEYMODE_JA_HALF_ALPHABET);
            break;
        case KEYCODE_SWITCH_HALF_NUMBER:
            changeKeyMode(KEYMODE_JA_HALF_NUMBER);
            break;
        case KEYCODE_SELECT_CASE:
            int shifted = (mShiftOn == 0) ? 1 : 0;
            Keyboard newKeyboard = getShiftChangeKeyboard(shifted);
            if (newKeyboard != null) {
                mShiftOn = shifted;
                changeKeyboard(newKeyboard);
            }
            break;
        case KEYCODE_JP12_SPACE:
            if ((mCurrentKeyMode == KEYMODE_JA_FULL_HIRAGANA) && !mNoInput) {
                mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.CONVERT));
            } else {
                mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.INPUT_CHAR, ' '));
            }
            break;
        case KEYCODE_EISU_KANA:
            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.CHANGE_MODE, OpenWnnJAJP.ENGINE_MODE_EISU_KANA));
            break;
        case KEYCODE_JP12_CLOSE:
            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.INPUT_KEY,
                                          new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK)));
            break;
        case KEYCODE_JP12_LEFT:
            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.INPUT_SOFT_KEY,
                                          new KeyEvent(KeyEvent.ACTION_DOWN,
                                                       KeyEvent.KEYCODE_DPAD_LEFT)));
            break;
        case KEYCODE_JP12_RIGHT:
            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.INPUT_SOFT_KEY,
                                          new KeyEvent(KeyEvent.ACTION_DOWN,
                                                       KeyEvent.KEYCODE_DPAD_RIGHT)));
            break;
        case KEYCODE_NOP:
            break;
        default:
            if (primaryCode >= 0) {
                if (mKeyboardView.isShifted()) {
                    primaryCode = Character.toUpperCase(primaryCode);
                }
                mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.INPUT_CHAR, (char)primaryCode));
            }
            break;
        }
        if (!mCapsLock && (primaryCode != DefaultSoftKeyboard.KEYCODE_QWERTY_SHIFT)) {
            setShiftByEditorInfo();
        }
    }
    @Override public void setPreferences(SharedPreferences pref, EditorInfo editor) {
        mPrefEditor = pref.edit();
        boolean isQwerty = pref.getBoolean("opt_enable_qwerty", false);
        if (isQwerty && (mCurrentKeyboardType == KEYBOARD_12KEY)) {
            changeKeyboardType(KEYBOARD_QWERTY);
        }
        super.setPreferences(pref, editor);
        int inputType = editor.inputType;
        if (mHardKeyboardHidden) {
            if (inputType == EditorInfo.TYPE_NULL) {
                if (!mIsInputTypeNull) {
                    mIsInputTypeNull = true;
                    if (mChangeModeKey != null) {
                        mPopupResId = mChangeModeKey.popupResId;
                        mChangeModeKey.popupResId = 0;
                    }
                }
                return;
            }
            if (mIsInputTypeNull) {
                mIsInputTypeNull = false;
                if (mChangeModeKey != null) {
                    mChangeModeKey.popupResId = mPopupResId;
                }
            }
        }
        mEnableAutoCaps = pref.getBoolean("auto_caps", true);
        mLimitedKeyMode = null;
        mPreferenceKeyMode = INVALID_KEYMODE;
        mNoInput = true;
        mDisableKeyInput = false;
        mCapsLock = false;
        switch (inputType & EditorInfo.TYPE_MASK_CLASS) {
        case EditorInfo.TYPE_CLASS_NUMBER:
        case EditorInfo.TYPE_CLASS_DATETIME:
            mPreferenceKeyMode = KEYMODE_JA_HALF_NUMBER;
            break;
        case EditorInfo.TYPE_CLASS_PHONE:
            if (mHardKeyboardHidden) {
                mLimitedKeyMode = new int[] {KEYMODE_JA_HALF_PHONE};
            } else {
                mLimitedKeyMode = new int[] {KEYMODE_JA_HALF_ALPHABET};
            }
            break;
        case EditorInfo.TYPE_CLASS_TEXT:
            switch (inputType & EditorInfo.TYPE_MASK_VARIATION) {
            case EditorInfo.TYPE_TEXT_VARIATION_PASSWORD:
            case EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD:
                mLimitedKeyMode = new int[] {KEYMODE_JA_HALF_ALPHABET, KEYMODE_JA_HALF_NUMBER};
                break;
            case EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS:
            case EditorInfo.TYPE_TEXT_VARIATION_URI:
                mPreferenceKeyMode = KEYMODE_JA_HALF_ALPHABET;
                break;
            default:
                break;
            }
            break;
        default:
            break;
        }
        if (inputType != mLastInputType) {
            setDefaultKeyboard();
            mLastInputType = inputType;
        }
        setStatusIcon();
        setShiftByEditorInfo();
    }
    @Override public void onUpdateState(OpenWnn parent) {
        super.onUpdateState(parent);
        if (!mCapsLock) {
            setShiftByEditorInfo();
        }
    }
    public void setDefaultKeyboard() {
        Locale locale = Locale.getDefault();
        int keymode = KEYMODE_JA_FULL_HIRAGANA;
        if (mPreferenceKeyMode != INVALID_KEYMODE) {
            keymode = mPreferenceKeyMode;
        } else if (mLimitedKeyMode != null) {
            keymode = mLimitedKeyMode[0];
        } else {
            if (!locale.getLanguage().equals(Locale.JAPANESE.getLanguage())) {
                keymode = KEYMODE_JA_HALF_ALPHABET;
            }
        }
        changeKeyMode(keymode);
    }
    public void nextKeyMode() {
        boolean found = false;
        int index;
        for (index = 0; index < JP_MODE_CYCLE_TABLE.length; index++) {
            if (JP_MODE_CYCLE_TABLE[index] == mCurrentKeyMode) {
                found = true;
                break;
            }
        }
        if (!found) {
            setDefaultKeyboard();
        } else {
            int size = JP_MODE_CYCLE_TABLE.length;
            int keyMode = INVALID_KEYMODE;
            for (int i = 0; i < size; i++) {
                index = (++index) % size;
                keyMode = filterKeyMode(JP_MODE_CYCLE_TABLE[index]);
                if (keyMode != INVALID_KEYMODE) {
                    break;
                }
            }
            if (keyMode != INVALID_KEYMODE) {
                changeKeyMode(keyMode);
            }
        }
    }
    private void createKeyboardsPortrait(OpenWnn parent) {
        Keyboard[][] keyList;
        keyList = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF];
        keyList[KEYMODE_JA_FULL_HIRAGANA][0] = new Keyboard(parent, R.xml.keyboard_qwerty_jp);
        keyList[KEYMODE_JA_FULL_ALPHABET][0] = new Keyboard(parent, R.xml.keyboard_qwerty_jp_full_alphabet);
        keyList[KEYMODE_JA_FULL_NUMBER][0]   = new Keyboard(parent, R.xml.keyboard_qwerty_jp_full_symbols);
        keyList[KEYMODE_JA_FULL_KATAKANA][0] = new Keyboard(parent, R.xml.keyboard_qwerty_jp_full_katakana);
        keyList[KEYMODE_JA_HALF_ALPHABET][0] = new Keyboard(parent, R.xml.keyboard_qwerty_jp_half_alphabet);
        keyList[KEYMODE_JA_HALF_NUMBER][0]   = new Keyboard(parent, R.xml.keyboard_qwerty_jp_half_symbols);
        keyList[KEYMODE_JA_HALF_KATAKANA][0] = new Keyboard(parent, R.xml.keyboard_qwerty_jp_half_katakana);
        keyList[KEYMODE_JA_HALF_PHONE][0]    = new Keyboard(parent, R.xml.keyboard_12key_phone);
        keyList = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON];
        keyList[KEYMODE_JA_FULL_HIRAGANA][0] = new Keyboard(parent, R.xml.keyboard_qwerty_jp_shift);
        keyList[KEYMODE_JA_FULL_ALPHABET][0] = new Keyboard(parent, R.xml.keyboard_qwerty_jp_full_alphabet_shift);
        keyList[KEYMODE_JA_FULL_NUMBER][0]   = new Keyboard(parent, R.xml.keyboard_qwerty_jp_full_symbols_shift);
        keyList[KEYMODE_JA_FULL_KATAKANA][0] = new Keyboard(parent, R.xml.keyboard_qwerty_jp_full_katakana_shift);
        keyList[KEYMODE_JA_HALF_ALPHABET][0] = new Keyboard(parent, R.xml.keyboard_qwerty_jp_half_alphabet_shift);
        keyList[KEYMODE_JA_HALF_NUMBER][0]   = new Keyboard(parent, R.xml.keyboard_qwerty_jp_half_symbols_shift);
        keyList[KEYMODE_JA_HALF_KATAKANA][0] = new Keyboard(parent, R.xml.keyboard_qwerty_jp_half_katakana_shift);
        keyList[KEYMODE_JA_HALF_PHONE][0] =
            mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF][KEYMODE_JA_HALF_PHONE][0];
        keyList = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF];
        keyList[KEYMODE_JA_FULL_HIRAGANA][0] = new Keyboard(parent, R.xml.keyboard_12keyjp);
        keyList[KEYMODE_JA_FULL_HIRAGANA][1] = new Keyboard(parent, R.xml.keyboard_12keyjp_input);
        keyList[KEYMODE_JA_FULL_ALPHABET][0] = new Keyboard(parent, R.xml.keyboard_12key_full_alphabet);
        keyList[KEYMODE_JA_FULL_ALPHABET][1] = new Keyboard(parent, R.xml.keyboard_12key_full_alphabet_input);
        keyList[KEYMODE_JA_FULL_NUMBER][0]   = new Keyboard(parent, R.xml.keyboard_12key_full_num);
        keyList[KEYMODE_JA_FULL_KATAKANA][0] = new Keyboard(parent, R.xml.keyboard_12key_full_katakana);
        keyList[KEYMODE_JA_FULL_KATAKANA][1] = new Keyboard(parent, R.xml.keyboard_12key_full_katakana_input);
        keyList[KEYMODE_JA_HALF_ALPHABET][0] = new Keyboard(parent, R.xml.keyboard_12key_half_alphabet);
        keyList[KEYMODE_JA_HALF_ALPHABET][1] = new Keyboard(parent, R.xml.keyboard_12key_half_alphabet_input);
        keyList[KEYMODE_JA_HALF_NUMBER][0]   = new Keyboard(parent, R.xml.keyboard_12key_half_num);
        keyList[KEYMODE_JA_HALF_KATAKANA][0] = new Keyboard(parent, R.xml.keyboard_12key_half_katakana);
        keyList[KEYMODE_JA_HALF_KATAKANA][1] = new Keyboard(parent, R.xml.keyboard_12key_half_katakana_input);
        keyList[KEYMODE_JA_HALF_PHONE][0]    = new Keyboard(parent, R.xml.keyboard_12key_phone);
        keyList = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_ON];
        keyList[KEYMODE_JA_FULL_HIRAGANA]
            = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF][KEYMODE_JA_FULL_HIRAGANA];
        keyList[KEYMODE_JA_FULL_ALPHABET]
            = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF][KEYMODE_JA_FULL_ALPHABET];
        keyList[KEYMODE_JA_FULL_NUMBER]
            = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF][KEYMODE_JA_FULL_NUMBER];
        keyList[KEYMODE_JA_FULL_KATAKANA]
            = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF][KEYMODE_JA_FULL_KATAKANA];
        keyList[KEYMODE_JA_HALF_ALPHABET]
            = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF][KEYMODE_JA_HALF_ALPHABET];;
        keyList[KEYMODE_JA_HALF_NUMBER]
            = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF][KEYMODE_JA_HALF_NUMBER];
        keyList[KEYMODE_JA_HALF_KATAKANA]
            = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF][KEYMODE_JA_HALF_KATAKANA];
        keyList[KEYMODE_JA_HALF_PHONE]
            = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF][KEYMODE_JA_HALF_PHONE];
    }
    private void createKeyboardsLandscape(OpenWnn parent) {
    }
    private int getTableIndex(int keyCode) {
        int index =
            (keyCode == KEYCODE_JP12_1)     ?  0 :
            (keyCode == KEYCODE_JP12_2)     ?  1 :
            (keyCode == KEYCODE_JP12_3)     ?  2 :
            (keyCode == KEYCODE_JP12_4)     ?  3 :
            (keyCode == KEYCODE_JP12_5)     ?  4 :
            (keyCode == KEYCODE_JP12_6)     ?  5 :
            (keyCode == KEYCODE_JP12_7)     ?  6 :
            (keyCode == KEYCODE_JP12_8)     ?  7 :
            (keyCode == KEYCODE_JP12_9)     ?  8 :
            (keyCode == KEYCODE_JP12_0)     ?  9 :
            (keyCode == KEYCODE_JP12_SHARP) ? 10 :
            (keyCode == KEYCODE_JP12_ASTER) ? 11 :
            0;
        return index;
    }
    private String[][] getCycleTable() {
        String[][] cycleTable = null;
        switch (mCurrentKeyMode) {
        case KEYMODE_JA_FULL_HIRAGANA:
            cycleTable = JP_FULL_HIRAGANA_CYCLE_TABLE;
            break;
        case KEYMODE_JA_FULL_KATAKANA:
            cycleTable = JP_FULL_KATAKANA_CYCLE_TABLE;
            break;
        case KEYMODE_JA_FULL_ALPHABET:
            cycleTable = JP_FULL_ALPHABET_CYCLE_TABLE;
            break;
        case KEYMODE_JA_FULL_NUMBER:
        case KEYMODE_JA_HALF_NUMBER:
            break;
        case KEYMODE_JA_HALF_ALPHABET:
            cycleTable = JP_HALF_ALPHABET_CYCLE_TABLE;
            break;
        case KEYMODE_JA_HALF_KATAKANA:
            cycleTable = JP_HALF_KATAKANA_CYCLE_TABLE;
            break;
        default:
            break;
        }
        return cycleTable;
    }
    private HashMap getReplaceTable() {
        HashMap hashTable = null;
        switch (mCurrentKeyMode) {
        case KEYMODE_JA_FULL_HIRAGANA:
            hashTable = JP_FULL_HIRAGANA_REPLACE_TABLE;
            break;
        case KEYMODE_JA_FULL_KATAKANA:
            hashTable = JP_FULL_KATAKANA_REPLACE_TABLE;
            break;
        case KEYMODE_JA_FULL_ALPHABET:
            hashTable = JP_FULL_ALPHABET_REPLACE_TABLE;
            break;
        case KEYMODE_JA_FULL_NUMBER:
        case KEYMODE_JA_HALF_NUMBER:
            break;
        case KEYMODE_JA_HALF_ALPHABET:
            hashTable = JP_HALF_ALPHABET_REPLACE_TABLE;
            break;
        case KEYMODE_JA_HALF_KATAKANA:
            hashTable = JP_HALF_KATAKANA_REPLACE_TABLE;
            break;
        default:
            break;
        }
        return hashTable;
    }
    private void setStatusIcon() {
        int icon = 0;
        switch (mCurrentKeyMode) {
        case KEYMODE_JA_FULL_HIRAGANA:
            icon = R.drawable.immodeic_hiragana;
            break;
        case KEYMODE_JA_FULL_KATAKANA:
            icon = R.drawable.immodeic_full_kana;
            break;
        case KEYMODE_JA_FULL_ALPHABET:
            icon = R.drawable.immodeic_full_alphabet;
            break;
        case KEYMODE_JA_FULL_NUMBER:
            icon = R.drawable.immodeic_full_number;
            break;
        case KEYMODE_JA_HALF_KATAKANA:
            icon = R.drawable.immodeic_half_kana;
            break;
        case KEYMODE_JA_HALF_ALPHABET:
            icon = R.drawable.immodeic_half_alphabet;
            break;
        case KEYMODE_JA_HALF_NUMBER:
        case KEYMODE_JA_HALF_PHONE:
            icon = R.drawable.immodeic_half_number;
            break;
        default:
            break;
        }
        mWnn.showStatusIcon(icon);
    }
    protected int getShiftKeyState(EditorInfo editor) {
        InputConnection connection = mWnn.getCurrentInputConnection();
        if (connection != null) {
            int caps = connection.getCursorCapsMode(editor.inputType);
            return (caps == 0) ? 0 : 1;
        } else {
            return 0;
        }
    }
    private void setShiftByEditorInfo() {
        if (mEnableAutoCaps && (mCurrentKeyMode == KEYMODE_JA_HALF_ALPHABET)) {
            int shift = getShiftKeyState(mWnn.getCurrentInputEditorInfo());
            mShiftOn = shift;
            changeKeyboard(getShiftChangeKeyboard(shift));
        }
    }
    @Override public void setHardKeyboardHidden(boolean hidden) {
        if (mWnn != null) {
            if (!hidden) {
                mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.CHANGE_MODE,
                                              OpenWnnJAJP.ENGINE_MODE_OPT_TYPE_QWERTY));
            }
            if (mHardKeyboardHidden != hidden) {
                if ((mLimitedKeyMode != null)
                    || ((mCurrentKeyMode != KEYMODE_JA_FULL_HIRAGANA)
                        && (mCurrentKeyMode != KEYMODE_JA_HALF_ALPHABET))) {
                    mLastInputType = EditorInfo.TYPE_NULL;
                    if (mWnn.isInputViewShown()) {
                        setDefaultKeyboard();
                    }
                }
            }
        }
        super.setHardKeyboardHidden(hidden);
    }
    private int filterKeyMode(int keyMode) {
        int targetMode = keyMode;
        int[] limits = mLimitedKeyMode;
        if (!mHardKeyboardHidden) { 
            if ((targetMode != KEYMODE_JA_FULL_HIRAGANA)
                && (targetMode != KEYMODE_JA_HALF_ALPHABET)) {
                Locale locale = Locale.getDefault();
                int keymode = KEYMODE_JA_HALF_ALPHABET;
                if (locale.getLanguage().equals(Locale.JAPANESE.getLanguage())) {
                    switch (targetMode) {
                    case KEYMODE_JA_FULL_HIRAGANA:
                    case KEYMODE_JA_FULL_KATAKANA:
                    case KEYMODE_JA_HALF_KATAKANA:
                        keymode = KEYMODE_JA_FULL_HIRAGANA;
                        break;
                    default:
                        break;
                    }
                }
                targetMode = keymode;
            }
        } 
        if (limits != null) {
            boolean hasAccepted = false;
            boolean hasRequiredChange = true;
            int size = limits.length;
            int nowMode = mCurrentKeyMode;
            for (int i = 0; i < size; i++) {
                if (targetMode == limits[i]) {
                    hasAccepted = true;
                    break;
                }
                if (nowMode == limits[i]) {
                    hasRequiredChange = false;
                }
            }
            if (!hasAccepted) {
                if (hasRequiredChange) {
                    targetMode = mLimitedKeyMode[0];
                } else {
                    targetMode = INVALID_KEYMODE;
                }
            }
        }
        return targetMode;
    }
}
