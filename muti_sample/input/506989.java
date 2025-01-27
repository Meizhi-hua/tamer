public class LocalePicker extends ListActivity {
    private static final String TAG = "LocalePicker";
    private static final boolean DEBUG = false;
    Loc[] mLocales;
    String[] mSpecialLocaleCodes;
    String[] mSpecialLocaleNames;
    private static class Loc implements Comparable {
        static Collator sCollator = Collator.getInstance();
        String label;
        Locale locale;
        public Loc(String label, Locale locale) {
            this.label = label;
            this.locale = locale;
        }
        @Override
        public String toString() {
            return this.label;
        }
        public int compareTo(Object o) {
            return sCollator.compare(this.label, ((Loc) o).label);
        }
    }
    int getContentView() {
        return R.layout.locale_picker;
    }
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(getContentView());
        mSpecialLocaleCodes = getResources().getStringArray(R.array.special_locale_codes);
        mSpecialLocaleNames = getResources().getStringArray(R.array.special_locale_names);
        String[] locales = getAssets().getLocales();
        Arrays.sort(locales);
        final int origSize = locales.length;
        Loc[] preprocess = new Loc[origSize];
        int finalSize = 0;
        for (int i = 0 ; i < origSize; i++ ) {
            String s = locales[i];
            int len = s.length();
            if (len == 5) {
                String language = s.substring(0, 2);
                String country = s.substring(3, 5);
                Locale l = new Locale(language, country);
                if (finalSize == 0) {
                    if (DEBUG) {
                        Log.v(TAG, "adding initial "+ toTitleCase(l.getDisplayLanguage(l)));
                    }
                    preprocess[finalSize++] =
                            new Loc(toTitleCase(l.getDisplayLanguage(l)), l);
                } else {
                    if (preprocess[finalSize-1].locale.getLanguage().equals(
                            language)) {
                        if (DEBUG) {
                            Log.v(TAG, "backing up and fixing "+
                                    preprocess[finalSize-1].label+" to "+
                                    getDisplayName(preprocess[finalSize-1].locale));
                        }
                        preprocess[finalSize-1].label = toTitleCase(
                                getDisplayName(preprocess[finalSize-1].locale));
                        if (DEBUG) {
                            Log.v(TAG, "  and adding "+ toTitleCase(getDisplayName(l)));
                        }
                        preprocess[finalSize++] =
                                new Loc(toTitleCase(getDisplayName(l)), l);
                    } else {
                        String displayName;
                        if (s.equals("zz_ZZ")) {
                            displayName = "Pseudo...";
                        } else {
                            displayName = toTitleCase(l.getDisplayLanguage(l));
                        }
                        if (DEBUG) {
                            Log.v(TAG, "adding "+displayName);
                        }
                        preprocess[finalSize++] = new Loc(displayName, l);
                    }
                }
            }
        }
        mLocales = new Loc[finalSize];
        for (int i = 0; i < finalSize ; i++) {
            mLocales[i] = preprocess[i];
        }
        Arrays.sort(mLocales);
        int layoutId = R.layout.locale_picker_item;
        int fieldId = R.id.locale;
        ArrayAdapter<Loc> adapter =
                new ArrayAdapter<Loc>(this, layoutId, fieldId, mLocales);
        getListView().setAdapter(adapter);
    }
    private static String toTitleCase(String s) {
        if (s.length() == 0) {
            return s;
        }
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
    private String getDisplayName(Locale l) {
        String code = l.toString();
        for (int i = 0; i < mSpecialLocaleCodes.length; i++) {
            if (mSpecialLocaleCodes[i].equals(code)) {
                return mSpecialLocaleNames[i];
            }
        }
        return l.getDisplayName(l);
    }
    @Override
    public void onResume() {
        super.onResume();
        getListView().requestFocus();
    }
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        try {
            IActivityManager am = ActivityManagerNative.getDefault();
            Configuration config = am.getConfiguration();
            Loc loc = mLocales[position];
            config.locale = loc.locale;
            config.userSetLocale = true;
            am.updateConfiguration(config);
            BackupManager.dataChanged("com.android.providers.settings");
        } catch (RemoteException e) {
        }
        finish();
    }
}
