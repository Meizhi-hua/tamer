public class OpenWnnEngineJAJP implements WnnEngine {
    private int mDictType = DIC_LANG_INIT;
    public static final int DIC_LANG_INIT = 0;
    public static final int DIC_LANG_JP = 0;
    public static final int DIC_LANG_EN = 1;
    public static final int DIC_LANG_JP_PERSON_NAME = 2;
    public static final int DIC_USERDIC = 3;
    public static final int DIC_LANG_JP_EISUKANA = 4;
    public static final int DIC_LANG_EN_EMAIL_ADDRESS = 5;
    public static final int DIC_LANG_JP_POSTAL_ADDRESS = 6;
    private int mKeyboardType = KEYBOARD_UNDEF;
    public static final int KEYBOARD_UNDEF = 0;
    public static final int KEYBOARD_KEYPAD12 = 1;
    public static final int KEYBOARD_QWERTY = 2;
    public static final int FREQ_LEARN = 600;
    public static final int FREQ_USER = 500;
    public static final int MAX_OUTPUT_LENGTH = 50;
    public static final int PREDICT_LIMIT = 100;
    private WnnDictionary mDictionaryJP;
    private ArrayList<WnnWord> mConvResult;
    private HashMap<String, WnnWord> mCandTable;
    private String mInputHiragana;
    private String mInputRomaji;
    private int mOutputNum;
    private int mGetCandidateFrom;
    private WnnWord mPreviousWord;
    private OpenWnnClauseConverterJAJP mClauseConverter;
    private KanaConverter mKanaConverter;
    private boolean mExactMatchMode;
    private boolean mSingleClauseMode;
    private WnnSentence mConvertSentence;
    private CandidateFilter mFilter = null;
    public OpenWnnEngineJAJP(String writableDictionaryName) {
        mDictionaryJP = new OpenWnnDictionaryImpl(
                "/data/data/jp.co.omronsoft.openwnn/lib/libWnnJpnDic.so",
                writableDictionaryName );
        if (!mDictionaryJP.isActive()) {
            mDictionaryJP = new OpenWnnDictionaryImpl(
                    "/system/lib/libWnnJpnDic.so",
                    writableDictionaryName );
        }
        mDictionaryJP.clearDictionary();
        mDictionaryJP.clearApproxPattern();
        mDictionaryJP.setInUseState(false);
        mConvResult = new ArrayList<WnnWord>();
        mCandTable = new HashMap<String, WnnWord>();
        mClauseConverter = new OpenWnnClauseConverterJAJP();
        mKanaConverter = new KanaConverter();
    }
    private void setDictionaryForPrediction(int strlen) {
        WnnDictionary dict = mDictionaryJP;
        dict.clearDictionary();
        if (mDictType != DIC_LANG_JP_EISUKANA) {
            dict.clearApproxPattern();
            if (strlen == 0) {
                dict.setDictionary(2, 245, 245);
                dict.setDictionary(3, 100, 244);
                dict.setDictionary(WnnDictionary.INDEX_LEARN_DICTIONARY, FREQ_LEARN, FREQ_LEARN);
            } else {
                dict.setDictionary(0, 100, 400);
                if (strlen > 1) {
                    dict.setDictionary(1, 100, 400);
                }
                dict.setDictionary(2, 245, 245);
                dict.setDictionary(3, 100, 244);
                dict.setDictionary(WnnDictionary.INDEX_USER_DICTIONARY, FREQ_USER, FREQ_USER);
                dict.setDictionary(WnnDictionary.INDEX_LEARN_DICTIONARY, FREQ_LEARN, FREQ_LEARN);
                if (mKeyboardType != KEYBOARD_QWERTY) {
                    dict.setApproxPattern(WnnDictionary.APPROX_PATTERN_JAJP_12KEY_NORMAL);
                }
            }
        }
    }
    private WnnWord getCandidate(int index) {
        WnnWord word;
        if (mGetCandidateFrom == 0) {
            if (mDictType == OpenWnnEngineJAJP.DIC_LANG_JP_EISUKANA) {
                mGetCandidateFrom = 2;
            } else if (mSingleClauseMode) {
                mGetCandidateFrom = 1;
            } else {
                if (mConvResult.size() < PREDICT_LIMIT) {
                    while (index >= mConvResult.size()) {
                        if ((word = mDictionaryJP.getNextWord()) == null) {
                            mGetCandidateFrom = 1;
                            break;
                        }
                        if (!mExactMatchMode || mInputHiragana.equals(word.stroke)) {
                            addCandidate(word);
                            if (mConvResult.size() >= PREDICT_LIMIT) {
                                mGetCandidateFrom = 1;
                                break;
                            }
                        }
                    }
                } else {
                    mGetCandidateFrom = 1;
                }
            }
        }
        if (mGetCandidateFrom == 1) {
            Iterator<?> convResult = mClauseConverter.convert(mInputHiragana);
            if (convResult != null) {
                while (convResult.hasNext()) {
                    addCandidate((WnnWord)convResult.next());
                }
            }
            mGetCandidateFrom = 2;
        }
        if (mGetCandidateFrom == 2) {
            List<WnnWord> addCandidateList
            = mKanaConverter.createPseudoCandidateList(mInputHiragana, mInputRomaji, mKeyboardType);
            Iterator<WnnWord> it = addCandidateList.iterator();
            while(it.hasNext()) {
                addCandidate(it.next());
            }
            mGetCandidateFrom = 3;
        }
        if (index >= mConvResult.size()) {
            return null;
        }
        return (WnnWord)mConvResult.get(index);
    }
    private boolean addCandidate(WnnWord word) {
        if (word.candidate == null || mCandTable.containsKey(word.candidate)
                || word.candidate.length() > MAX_OUTPUT_LENGTH) {
            return false;
        }
        if (mFilter != null && !mFilter.isAllowed(word)) {
            return false;
        }
        mCandTable.put(word.candidate, word);
        mConvResult.add(word);
        return true;
    }
    private void clearCandidates() {
        mConvResult.clear();
        mCandTable.clear();
        mOutputNum = 0;
        mInputHiragana = null;
        mInputRomaji = null;
        mGetCandidateFrom = 0;
        mSingleClauseMode = false;
    }
    public boolean setDictionary(int type) {
        mDictType = type;
        return true;
    }
    private int setSearchKey(ComposingText text, int maxLen) {
        String input = text.toString(ComposingText.LAYER1);
        if (0 <= maxLen && maxLen <= input.length()) {
            input = input.substring(0, maxLen);
            mExactMatchMode = true;
        } else {
            mExactMatchMode = false;
        }
        if (input.length() == 0) {
            mInputHiragana = "";
            mInputRomaji = "";
            return 0;
        }
        mInputHiragana = input;
        mInputRomaji = text.toString(ComposingText.LAYER0);
        return input.length();
    }
    public void clearPreviousWord() {
        mPreviousWord = null;
    }
    public void setKeyboardType(int keyboardType) {
        mKeyboardType = keyboardType;
    }
    public void setFilter(CandidateFilter filter) {
        mFilter = filter;
        mClauseConverter.setFilter(filter);
    }
    public void init() {
        clearPreviousWord();
        mClauseConverter.setDictionary(mDictionaryJP);
        mKanaConverter.setDictionary(mDictionaryJP);
    }
    public void close() {}
    public int predict(ComposingText text, int minLen, int maxLen) {
        clearCandidates();
        if (text == null) { return 0; }
        int len = setSearchKey(text, maxLen);
        setDictionaryForPrediction(len);
        mDictionaryJP.setInUseState( true );
        if (len == 0) {
            return mDictionaryJP.searchWord(WnnDictionary.SEARCH_LINK, WnnDictionary.ORDER_BY_FREQUENCY,
                                            mInputHiragana, mPreviousWord);
        } else {
            if (mExactMatchMode) {
                mDictionaryJP.searchWord(WnnDictionary.SEARCH_EXACT, WnnDictionary.ORDER_BY_FREQUENCY,
                                         mInputHiragana);
            } else {
                mDictionaryJP.searchWord(WnnDictionary.SEARCH_PREFIX, WnnDictionary.ORDER_BY_FREQUENCY,
                                         mInputHiragana);
            }
            return 1;
        }
    }
    public int convert(ComposingText text) {
        clearCandidates();
        if (text == null) {
            return 0;
        }
        mDictionaryJP.setInUseState( true );
        int cursor = text.getCursor(ComposingText.LAYER1);
        String input;
        WnnClause head = null;
        if (cursor > 0) {
            input = text.toString(ComposingText.LAYER1, 0, cursor - 1);
            Iterator headCandidates = mClauseConverter.convert(input);
            if ((headCandidates == null) || (!headCandidates.hasNext())) {
                return 0;
            }
            head = new WnnClause(input, (WnnWord)headCandidates.next());
            input = text.toString(ComposingText.LAYER1, cursor, text.size(ComposingText.LAYER1) - 1);
        } else {
            input = text.toString(ComposingText.LAYER1);
        }
        WnnSentence sentence = null;
        if (input.length() != 0) {
            sentence = mClauseConverter.consecutiveClauseConvert(input);
        }
        if (head != null) {
            sentence = new WnnSentence(head, sentence);
        }
        if (sentence == null) {
            return 0;
        }
        StrSegmentClause[] ss = new StrSegmentClause[sentence.elements.size()];
        int pos = 0;
        int idx = 0;
        Iterator<WnnClause> it = sentence.elements.iterator();
        while(it.hasNext()) {
            WnnClause clause = (WnnClause)it.next();
            int len = clause.stroke.length();
            ss[idx] = new StrSegmentClause(clause, pos, pos + len - 1);
            pos += len;
            idx += 1;
        }
        text.setCursor(ComposingText.LAYER2, text.size(ComposingText.LAYER2));
        text.replaceStrSegment(ComposingText.LAYER2, ss, 
                               text.getCursor(ComposingText.LAYER2));
        mConvertSentence = sentence;
        return 0;
    }
    public int searchWords(String key) {
        clearCandidates();
        return 0;
    }
    public int searchWords(WnnWord word) {
        clearCandidates();
        return 0;
    }
    public WnnWord getNextCandidate() {
        if (mInputHiragana == null) {
            return null;
        }
        WnnWord word = getCandidate(mOutputNum);
        if (word != null) {
            mOutputNum++;
        }
        return word;
    }
    public boolean learn(WnnWord word) {
        int ret = -1;
        if (word.partOfSpeech.right == 0) {
            word.partOfSpeech = mDictionaryJP.getPOS(WnnDictionary.POS_TYPE_MEISI);
        }
        WnnDictionary dict = mDictionaryJP;
        if (word instanceof WnnSentence) {
            Iterator<WnnClause> clauses = ((WnnSentence)word).elements.iterator();
            while (clauses.hasNext()) {
                WnnWord wd = clauses.next();
                if (mPreviousWord != null) {
                    ret = dict.learnWord(wd, mPreviousWord);
                } else {
                    ret = dict.learnWord(wd);
                }
                mPreviousWord = wd;
                if (ret != 0) {
                    break;
                }
            }
        } else {
            if (mPreviousWord != null) {
                ret = dict.learnWord(word, mPreviousWord);
            } else {
                ret = dict.learnWord(word);
            }
            mPreviousWord = word;
            mClauseConverter.setDictionary(dict);
        }
        return (ret == 0);
    }
    public int addWord(WnnWord word) {
        mDictionaryJP.setInUseState( true );
        if (word.partOfSpeech.right == 0) {
            word.partOfSpeech = mDictionaryJP.getPOS(WnnDictionary.POS_TYPE_MEISI);
        }
        mDictionaryJP.addWordToUserDictionary(word);
        mDictionaryJP.setInUseState( false );
        return 0;
    }
    public boolean deleteWord(WnnWord word) {
        mDictionaryJP.setInUseState( true );
        mDictionaryJP.removeWordFromUserDictionary(word);
        mDictionaryJP.setInUseState( false );
        return false;
    }
    public void setPreferences(SharedPreferences pref) {}
    public void breakSequence()  {
        clearPreviousWord();
    }
    public int makeCandidateListOf(int clausePosition)  {
        clearCandidates();
        if ((mConvertSentence == null) || (mConvertSentence.elements.size() <= clausePosition)) {
            return 0;
        }
        mSingleClauseMode = true;
        WnnClause clause = mConvertSentence.elements.get(clausePosition);
        mInputHiragana = clause.stroke;
        mInputRomaji = clause.candidate;
        return 1;
    }
    public boolean initializeDictionary(int dictionary)  {
        switch( dictionary ) {
        case WnnEngine.DICTIONARY_TYPE_LEARN:
            mDictionaryJP.setInUseState( true );
            mDictionaryJP.clearLearnDictionary();
            mDictionaryJP.setInUseState( false );
            return true;
        case WnnEngine.DICTIONARY_TYPE_USER:
            mDictionaryJP.setInUseState( true );
            mDictionaryJP.clearUserDictionary();
            mDictionaryJP.setInUseState( false );
            return true;
        }
        return false;
    }
    public boolean initializeDictionary(int dictionary, int type) {
        return initializeDictionary(dictionary);
    }
    public WnnWord[] getUserDictionaryWords( ) {
        mDictionaryJP.setInUseState(true);
        WnnWord[] result = mDictionaryJP.getUserDictionaryWords( );
        mDictionaryJP.setInUseState(false);
        Arrays.sort(result, new WnnWordComparator());
        return result;
    }
    private class WnnWordComparator implements java.util.Comparator {
        public int compare(Object object1, Object object2) {
            WnnWord wnnWord1 = (WnnWord) object1;
            WnnWord wnnWord2 = (WnnWord) object2;
            return wnnWord1.stroke.compareTo(wnnWord2.stroke);
        }
    }
}
