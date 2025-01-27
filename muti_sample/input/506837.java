public class TextToSpeechSettings extends PreferenceActivity implements
        Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener,
        TextToSpeech.OnInitListener {
    private static final String TAG = "TextToSpeechSettings";
    private static final String SYSTEM_TTS = "com.svox.pico";
    private static final String KEY_TTS_PLAY_EXAMPLE = "tts_play_example";
    private static final String KEY_TTS_INSTALL_DATA = "tts_install_data";
    private static final String KEY_TTS_USE_DEFAULT = "toggle_use_default_tts_settings";
    private static final String KEY_TTS_DEFAULT_RATE = "tts_default_rate";
    private static final String KEY_TTS_DEFAULT_LANG = "tts_default_lang";
    private static final String KEY_TTS_DEFAULT_COUNTRY = "tts_default_country";
    private static final String KEY_TTS_DEFAULT_VARIANT = "tts_default_variant";
    private static final String KEY_TTS_DEFAULT_SYNTH = "tts_default_synth";
    private static final String KEY_PLUGIN_ENABLED_PREFIX = "ENABLED_";
    private static final String KEY_PLUGIN_SETTINGS_PREFIX = "SETTINGS_";
    private static final String DEFAULT_LANG_VAL = "eng";
    private static final String DEFAULT_COUNTRY_VAL = "USA";
    private static final String DEFAULT_VARIANT_VAL = "";
    private static final String LOCALE_DELIMITER = "-";
    private static final String FALLBACK_TTS_DEFAULT_SYNTH =
            TextToSpeech.Engine.DEFAULT_SYNTH;
    private Preference         mPlayExample = null;
    private Preference         mInstallData = null;
    private CheckBoxPreference mUseDefaultPref = null;
    private ListPreference     mDefaultRatePref = null;
    private ListPreference     mDefaultLocPref = null;
    private ListPreference     mDefaultSynthPref = null;
    private String             mDefaultLanguage = null;
    private String             mDefaultCountry = null;
    private String             mDefaultLocVariant = null;
    private String             mDefaultEng = "";
    private int                mDefaultRate = TextToSpeech.Engine.DEFAULT_RATE;
    private String[] mDemoStrings;
    private int      mDemoStringIndex = 0;
    private boolean mEnableDemo = false;
    private boolean mVoicesMissing = false;
    private TextToSpeech mTts = null;
    private boolean mTtsStarted = false;
    private static final int VOICE_DATA_INTEGRITY_CHECK = 1977;
    private static final int GET_SAMPLE_TEXT = 1983;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.tts_settings);
        addEngineSpecificSettings();
        mDemoStrings = getResources().getStringArray(R.array.tts_demo_strings);
        setVolumeControlStream(TextToSpeech.Engine.DEFAULT_STREAM);
        mEnableDemo = false;
        mTtsStarted = false;
        Locale currentLocale = Locale.getDefault();
        mDefaultLanguage = currentLocale.getISO3Language();
        mDefaultCountry = currentLocale.getISO3Country();
        mDefaultLocVariant = currentLocale.getVariant();
        mTts = new TextToSpeech(this, this);
    }
    @Override
    protected void onStart() {
        super.onStart();
        if (mTtsStarted){
            initClickers();
            updateWidgetState();
            checkVoiceData();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTts != null) {
            mTts.shutdown();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        if ((mDefaultRatePref != null) && (mDefaultRatePref.getDialog() != null)) {
            mDefaultRatePref.getDialog().dismiss();
        }
        if ((mDefaultLocPref != null) && (mDefaultLocPref.getDialog() != null)) {
            mDefaultLocPref.getDialog().dismiss();
        }
        if ((mDefaultSynthPref != null) && (mDefaultSynthPref.getDialog() != null)) {
            mDefaultSynthPref.getDialog().dismiss();
        }
    }
    private void addEngineSpecificSettings() {
        PreferenceGroup enginesCategory = (PreferenceGroup) findPreference("tts_engines_section");
        Intent intent = new Intent("android.intent.action.START_TTS_ENGINE");
        ResolveInfo[] enginesArray = new ResolveInfo[0];
        PackageManager pm = getPackageManager();
        enginesArray = pm.queryIntentActivities(intent, 0).toArray(enginesArray);
        for (int i = 0; i < enginesArray.length; i++) {
            String prefKey = "";
            final String pluginPackageName = enginesArray[i].activityInfo.packageName;
            if (!enginesArray[i].activityInfo.packageName.equals(SYSTEM_TTS)) {
                CheckBoxPreference chkbxPref = new CheckBoxPreference(this);
                prefKey = KEY_PLUGIN_ENABLED_PREFIX + pluginPackageName;
                chkbxPref.setKey(prefKey);
                chkbxPref.setTitle(enginesArray[i].loadLabel(pm));
                enginesCategory.addPreference(chkbxPref);
            }
            if (pluginHasSettings(pluginPackageName)) {
                Preference pref = new Preference(this);
                prefKey = KEY_PLUGIN_SETTINGS_PREFIX + pluginPackageName;
                pref.setKey(prefKey);
                pref.setTitle(enginesArray[i].loadLabel(pm));
                CharSequence settingsLabel = getResources().getString(
                        R.string.tts_engine_name_settings, enginesArray[i].loadLabel(pm));
                pref.setSummary(settingsLabel);
                pref.setOnPreferenceClickListener(new OnPreferenceClickListener(){
                            public boolean onPreferenceClick(Preference preference){
                                Intent i = new Intent();
                                i.setClassName(pluginPackageName,
                                        pluginPackageName + ".EngineSettings");
                                startActivity(i);
                                return true;
                            }
                        });
                enginesCategory.addPreference(pref);
            }
        }
    }
    private boolean pluginHasSettings(String pluginPackageName) {
        PackageManager pm = getPackageManager();
        Intent i = new Intent();
        i.setClassName(pluginPackageName, pluginPackageName + ".EngineSettings");
        if (pm.resolveActivity(i, PackageManager.MATCH_DEFAULT_ONLY) != null){
            return true;
        }
        return false;
    }
    private void initClickers() {
        mPlayExample = findPreference(KEY_TTS_PLAY_EXAMPLE);
        mPlayExample.setOnPreferenceClickListener(this);
        mInstallData = findPreference(KEY_TTS_INSTALL_DATA);
        mInstallData.setOnPreferenceClickListener(this);
    }
    private void initDefaultSettings() {
        ContentResolver resolver = getContentResolver();
        int useDefault = 0;
        mUseDefaultPref = (CheckBoxPreference) findPreference(KEY_TTS_USE_DEFAULT);
        try {
            useDefault = Settings.Secure.getInt(resolver, TTS_USE_DEFAULTS);
        } catch (SettingNotFoundException e) {
            useDefault = TextToSpeech.Engine.USE_DEFAULTS;
            Settings.Secure.putInt(resolver, TTS_USE_DEFAULTS, useDefault);
        }
        mUseDefaultPref.setChecked(useDefault == 1);
        mUseDefaultPref.setOnPreferenceChangeListener(this);
        mDefaultSynthPref = (ListPreference) findPreference(KEY_TTS_DEFAULT_SYNTH);
        loadEngines();
        mDefaultSynthPref.setOnPreferenceChangeListener(this);
        String engine = Settings.Secure.getString(resolver, TTS_DEFAULT_SYNTH);
        if (engine == null) {
            engine = FALLBACK_TTS_DEFAULT_SYNTH;
            Settings.Secure.putString(resolver, TTS_DEFAULT_SYNTH, engine);
        }
        mDefaultEng = engine;
        mDefaultRatePref = (ListPreference) findPreference(KEY_TTS_DEFAULT_RATE);
        try {
            mDefaultRate = Settings.Secure.getInt(resolver, TTS_DEFAULT_RATE);
        } catch (SettingNotFoundException e) {
            mDefaultRate = TextToSpeech.Engine.DEFAULT_RATE;
            Settings.Secure.putInt(resolver, TTS_DEFAULT_RATE, mDefaultRate);
        }
        mDefaultRatePref.setValue(String.valueOf(mDefaultRate));
        mDefaultRatePref.setOnPreferenceChangeListener(this);
        mDefaultLocPref = (ListPreference) findPreference(KEY_TTS_DEFAULT_LANG);
        initDefaultLang();
        mDefaultLocPref.setOnPreferenceChangeListener(this);
    }
    private void checkVoiceData() {
        PackageManager pm = getPackageManager();
        Intent intent = new Intent();
        intent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
        for (int i = 0; i < resolveInfos.size(); i++) {
            ActivityInfo currentActivityInfo = resolveInfos.get(i).activityInfo;
            if (mDefaultEng.equals(currentActivityInfo.packageName)) {
                intent.setClassName(mDefaultEng, currentActivityInfo.name);
                this.startActivityForResult(intent, VOICE_DATA_INTEGRITY_CHECK);
            }
        }
    }
    private void installVoiceData() {
        PackageManager pm = getPackageManager();
        Intent intent = new Intent();
        intent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
        for (int i = 0; i < resolveInfos.size(); i++) {
            ActivityInfo currentActivityInfo = resolveInfos.get(i).activityInfo;
            if (mDefaultEng.equals(currentActivityInfo.packageName)) {
                intent.setClassName(mDefaultEng, currentActivityInfo.name);
                this.startActivity(intent);
            }
        }
    }
    private void getSampleText() {
        PackageManager pm = getPackageManager();
        Intent intent = new Intent();
        intent.setAction("android.speech.tts.engine.GET_SAMPLE_TEXT");
        intent.putExtra("language", mDefaultLanguage);
        intent.putExtra("country", mDefaultCountry);
        intent.putExtra("variant", mDefaultLocVariant);
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
        for (int i = 0; i < resolveInfos.size(); i++) {
            ActivityInfo currentActivityInfo = resolveInfos.get(i).activityInfo;
            if (mDefaultEng.equals(currentActivityInfo.packageName)) {
                intent.setClassName(mDefaultEng, currentActivityInfo.name);
                this.startActivityForResult(intent, GET_SAMPLE_TEXT);
            }
        }
    }
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            mEnableDemo = true;
            if (mDefaultLanguage == null) {
                mDefaultLanguage = Locale.getDefault().getISO3Language();
            }
            if (mDefaultCountry == null) {
                mDefaultCountry = Locale.getDefault().getISO3Country();
            }
            if (mDefaultLocVariant == null) {
                mDefaultLocVariant = new String();
            }
            mTts.setLanguage(new Locale(mDefaultLanguage, mDefaultCountry, mDefaultLocVariant));
            mTts.setSpeechRate((float)(mDefaultRate/100.0f));
            initDefaultSettings();
            initClickers();
            updateWidgetState();
            checkVoiceData();
            mTtsStarted = true;
            Log.v(TAG, "TTS engine for settings screen initialized.");
        } else {
            Log.v(TAG, "TTS engine for settings screen failed to initialize successfully.");
            mEnableDemo = false;
        }
        updateWidgetState();
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VOICE_DATA_INTEGRITY_CHECK) {
            if (data == null){
                mEnableDemo = false;
                mVoicesMissing = false;
                updateWidgetState();
                return;
            }
            ArrayList<String> available =
                    data.getStringArrayListExtra(TextToSpeech.Engine.EXTRA_AVAILABLE_VOICES);
            ArrayList<String> unavailable =
                    data.getStringArrayListExtra(TextToSpeech.Engine.EXTRA_UNAVAILABLE_VOICES);
            if ((available == null) || (unavailable == null)){
                mEnableDemo = false;
                mVoicesMissing = false;
                updateWidgetState();
                return;
            }
            if (available.size() > 0){
                if (mTts == null) {
                    mTts = new TextToSpeech(this, this);
                }
                ListPreference ttsLanguagePref =
                        (ListPreference) findPreference("tts_default_lang");
                CharSequence[] entries = new CharSequence[available.size()];
                CharSequence[] entryValues = new CharSequence[available.size()];
                int selectedLanguageIndex = -1;
                String selectedLanguagePref = mDefaultLanguage;
                if (mDefaultCountry.length() > 0) {
                    selectedLanguagePref = selectedLanguagePref + LOCALE_DELIMITER +
                            mDefaultCountry;
                }
                if (mDefaultLocVariant.length() > 0) {
                    selectedLanguagePref = selectedLanguagePref + LOCALE_DELIMITER +
                            mDefaultLocVariant;
                }
                for (int i = 0; i < available.size(); i++) {
                    String[] langCountryVariant = available.get(i).split("-");
                    Locale loc = null;
                    if (langCountryVariant.length == 1){
                        loc = new Locale(langCountryVariant[0]);
                    } else if (langCountryVariant.length == 2){
                        loc = new Locale(langCountryVariant[0], langCountryVariant[1]);
                    } else if (langCountryVariant.length == 3){
                        loc = new Locale(langCountryVariant[0], langCountryVariant[1],
                                         langCountryVariant[2]);
                    }
                    if (loc != null){
                        entries[i] = loc.getDisplayName();
                        entryValues[i] = available.get(i);
                        if (entryValues[i].equals(selectedLanguagePref)) {
                            selectedLanguageIndex = i;
                        }
                    }
                }
                ttsLanguagePref.setEntries(entries);
                ttsLanguagePref.setEntryValues(entryValues);
                if (selectedLanguageIndex > -1) {
                    ttsLanguagePref.setValueIndex(selectedLanguageIndex);
                }
                mEnableDemo = true;
                int languageResult = mTts.setLanguage(
                        new Locale(mDefaultLanguage, mDefaultCountry, mDefaultLocVariant));
                if (languageResult < TextToSpeech.LANG_AVAILABLE){
                    Locale currentLocale = Locale.getDefault();
                    mDefaultLanguage = currentLocale.getISO3Language();
                    mDefaultCountry = currentLocale.getISO3Country();
                    mDefaultLocVariant = currentLocale.getVariant();
                    languageResult = mTts.setLanguage(
                            new Locale(mDefaultLanguage, mDefaultCountry, mDefaultLocVariant));
                    if (languageResult < TextToSpeech.LANG_AVAILABLE){
                        parseLocaleInfo(ttsLanguagePref.getEntryValues()[0].toString());
                        mTts.setLanguage(
                                new Locale(mDefaultLanguage, mDefaultCountry, mDefaultLocVariant));
                    }
                    ContentResolver resolver = getContentResolver();
                    Settings.Secure.putString(resolver, TTS_DEFAULT_LANG, mDefaultLanguage);
                    Settings.Secure.putString(resolver, TTS_DEFAULT_COUNTRY, mDefaultCountry);
                    Settings.Secure.putString(resolver, TTS_DEFAULT_VARIANT, mDefaultLocVariant);
                }
            } else {
                mEnableDemo = false;
            }
            if (unavailable.size() > 0){
                mVoicesMissing = true;
            } else {
                mVoicesMissing = false;
            }
            updateWidgetState();
        } else if (requestCode == GET_SAMPLE_TEXT) {
            if (resultCode == TextToSpeech.LANG_AVAILABLE) {
                String sample = getString(R.string.tts_demo);
                if ((data != null) && (data.getStringExtra("sampleText") != null)) {
                    sample = data.getStringExtra("sampleText");
                }
                if (mTts != null) {
                    mTts.speak(sample, TextToSpeech.QUEUE_FLUSH, null);
                }
            } else {
                Log.e(TAG, "Did not have a sample string for the requested language");
            }
        }
    }
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (KEY_TTS_USE_DEFAULT.equals(preference.getKey())) {
            int value = (Boolean)objValue ? 1 : 0;
            Settings.Secure.putInt(getContentResolver(), TTS_USE_DEFAULTS,
                    value);
            Log.i(TAG, "TTS use default settings is "+objValue.toString());
        } else if (KEY_TTS_DEFAULT_RATE.equals(preference.getKey())) {
            mDefaultRate = Integer.parseInt((String) objValue);
            try {
                Settings.Secure.putInt(getContentResolver(),
                        TTS_DEFAULT_RATE, mDefaultRate);
                if (mTts != null) {
                    mTts.setSpeechRate((float)(mDefaultRate/100.0f));
                }
                Log.i(TAG, "TTS default rate is " + mDefaultRate);
            } catch (NumberFormatException e) {
                Log.e(TAG, "could not persist default TTS rate setting", e);
            }
        } else if (KEY_TTS_DEFAULT_LANG.equals(preference.getKey())) {
            ContentResolver resolver = getContentResolver();
            parseLocaleInfo((String) objValue);
            Settings.Secure.putString(resolver, TTS_DEFAULT_LANG, mDefaultLanguage);
            Settings.Secure.putString(resolver, TTS_DEFAULT_COUNTRY, mDefaultCountry);
            Settings.Secure.putString(resolver, TTS_DEFAULT_VARIANT, mDefaultLocVariant);
            Log.v(TAG, "TTS default lang/country/variant set to "
                    + mDefaultLanguage + "/" + mDefaultCountry + "/" + mDefaultLocVariant);
            if (mTts != null) {
                mTts.setLanguage(new Locale(mDefaultLanguage, mDefaultCountry, mDefaultLocVariant));
            }
            int newIndex = mDefaultLocPref.findIndexOfValue((String)objValue);
            Log.v("Settings", " selected is " + newIndex);
            mDemoStringIndex = newIndex > -1 ? newIndex : 0;
        } else if (KEY_TTS_DEFAULT_SYNTH.equals(preference.getKey())) {
            mDefaultEng = objValue.toString();
            Settings.Secure.putString(getContentResolver(), TTS_DEFAULT_SYNTH, mDefaultEng);
            if (mTts != null) {
                mTts.setEngineByPackageName(mDefaultEng);
                mEnableDemo = false;
                mVoicesMissing = false;
                updateWidgetState();
                checkVoiceData();
            }
            Log.v("Settings", "The default synth is: " + objValue.toString());
        }
        return true;
    }
    public boolean onPreferenceClick(Preference preference) {
        if (preference == mPlayExample) {
            getSampleText();
            return true;
        }
        if (preference == mInstallData) {
            installVoiceData();
            finish();
            return true;
        }
        return false;
    }
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (Utils.isMonkeyRunning()) {
            return false;
        }
        if (preference instanceof CheckBoxPreference) {
            final CheckBoxPreference chkPref = (CheckBoxPreference) preference;
            if (!chkPref.getKey().equals(KEY_TTS_USE_DEFAULT)){
                if (chkPref.isChecked()) {
                    chkPref.setChecked(false);
                    AlertDialog d = (new AlertDialog.Builder(this))
                            .setTitle(android.R.string.dialog_alert_title)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setMessage(getString(R.string.tts_engine_security_warning,
                                    chkPref.getTitle()))
                            .setCancelable(true)
                            .setPositiveButton(android.R.string.ok,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            chkPref.setChecked(true);
                                            loadEngines();
                                        }
                            })
                            .setNegativeButton(android.R.string.cancel,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                            })
                            .create();
                    d.show();
                } else {
                    loadEngines();
                }
                return true;
            }
        }
        return false;
    }
    private void updateWidgetState() {
        mPlayExample.setEnabled(mEnableDemo);
        mUseDefaultPref.setEnabled(mEnableDemo);
        mDefaultRatePref.setEnabled(mEnableDemo);
        mDefaultLocPref.setEnabled(mEnableDemo);
        mInstallData.setEnabled(mVoicesMissing);
    }
    private void parseLocaleInfo(String locale) {
        StringTokenizer tokenizer = new StringTokenizer(locale, LOCALE_DELIMITER);
        mDefaultLanguage = "";
        mDefaultCountry = "";
        mDefaultLocVariant = "";
        if (tokenizer.hasMoreTokens()) {
            mDefaultLanguage = tokenizer.nextToken().trim();
        }
        if (tokenizer.hasMoreTokens()) {
            mDefaultCountry = tokenizer.nextToken().trim();
        }
        if (tokenizer.hasMoreTokens()) {
            mDefaultLocVariant = tokenizer.nextToken().trim();
        }
    }
    private void initDefaultLang() {
        if (!hasLangPref()) {
            if (isCurrentLocSupported()) {
                useCurrentLocAsDefault();
            } else {
                useSupportedLocAsDefault();
            }
        }
        ContentResolver resolver = getContentResolver();
        mDefaultLanguage = Settings.Secure.getString(resolver, TTS_DEFAULT_LANG);
        mDefaultCountry = Settings.Secure.getString(resolver, TTS_DEFAULT_COUNTRY);
        mDefaultLocVariant = Settings.Secure.getString(resolver, TTS_DEFAULT_VARIANT);
        mDemoStringIndex = mDefaultLocPref.findIndexOfValue(mDefaultLanguage + LOCALE_DELIMITER
                + mDefaultCountry);
        if (mDemoStringIndex > -1){
            mDefaultLocPref.setValueIndex(mDemoStringIndex);
        }
    }
    private boolean hasLangPref() {
        ContentResolver resolver = getContentResolver();
        String language = Settings.Secure.getString(resolver, TTS_DEFAULT_LANG);
        if ((language == null) || (language.length() < 1)) {
            return false;
        }
        String country = Settings.Secure.getString(resolver, TTS_DEFAULT_COUNTRY);
        if (country == null) {
            return false;
        }
        String variant = Settings.Secure.getString(resolver, TTS_DEFAULT_VARIANT);
        if (variant == null) {
            return false;
        }
        return true;
    }
    private boolean isCurrentLocSupported() {
        String currentLocID = Locale.getDefault().getISO3Language() + LOCALE_DELIMITER
                + Locale.getDefault().getISO3Country();
        return (mDefaultLocPref.findIndexOfValue(currentLocID) > -1);
    }
    private void useCurrentLocAsDefault() {
        Locale currentLocale = Locale.getDefault();
        ContentResolver resolver = getContentResolver();
        Settings.Secure.putString(resolver, TTS_DEFAULT_LANG, currentLocale.getISO3Language());
        Settings.Secure.putString(resolver, TTS_DEFAULT_COUNTRY, currentLocale.getISO3Country());
        Settings.Secure.putString(resolver, TTS_DEFAULT_VARIANT, currentLocale.getVariant());
    }
    private void useSupportedLocAsDefault() {
        ContentResolver resolver = getContentResolver();
        Settings.Secure.putString(resolver, TTS_DEFAULT_LANG, DEFAULT_LANG_VAL);
        Settings.Secure.putString(resolver, TTS_DEFAULT_COUNTRY, DEFAULT_COUNTRY_VAL);
        Settings.Secure.putString(resolver, TTS_DEFAULT_VARIANT, DEFAULT_VARIANT_VAL);
    }
    private void loadEngines() {
        mDefaultSynthPref = (ListPreference) findPreference(KEY_TTS_DEFAULT_SYNTH);
        Intent intent = new Intent("android.intent.action.START_TTS_ENGINE");
        ResolveInfo[] enginesArray = new ResolveInfo[0];
        PackageManager pm = getPackageManager();
        enginesArray = pm.queryIntentActivities(intent, 0).toArray(enginesArray);
        ArrayList<CharSequence> entries = new ArrayList<CharSequence>();
        ArrayList<CharSequence> values = new ArrayList<CharSequence>();
        String enabledEngines = "";
        for (int i = 0; i < enginesArray.length; i++) {
            String pluginPackageName = enginesArray[i].activityInfo.packageName;
            if (pluginPackageName.equals(SYSTEM_TTS)) {
                entries.add(enginesArray[i].loadLabel(pm));
                values.add(pluginPackageName);
            } else {
                CheckBoxPreference pref = (CheckBoxPreference) findPreference(
                        KEY_PLUGIN_ENABLED_PREFIX + pluginPackageName);
                if ((pref != null) && pref.isChecked()){
                    entries.add(enginesArray[i].loadLabel(pm));
                    values.add(pluginPackageName);
                    enabledEngines = enabledEngines + pluginPackageName + " ";
                }
            }
        }
        ContentResolver resolver = getContentResolver();
        Settings.Secure.putString(resolver, TTS_ENABLED_PLUGINS, enabledEngines);
        CharSequence entriesArray[] = new CharSequence[entries.size()];
        CharSequence valuesArray[] = new CharSequence[values.size()];
        mDefaultSynthPref.setEntries(entries.toArray(entriesArray));
        mDefaultSynthPref.setEntryValues(values.toArray(valuesArray));
        String selectedEngine = Settings.Secure.getString(getContentResolver(), TTS_DEFAULT_SYNTH);
        int selectedEngineIndex = mDefaultSynthPref.findIndexOfValue(selectedEngine);
        if (selectedEngineIndex == -1){
            selectedEngineIndex = mDefaultSynthPref.findIndexOfValue(SYSTEM_TTS);
        }
        mDefaultSynthPref.setValueIndex(selectedEngineIndex);
    }
}
