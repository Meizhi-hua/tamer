public class NumberFormatProviderTest extends ProviderTest {
    com.foo.NumberFormatProviderImpl nfp = new com.foo.NumberFormatProviderImpl();
    List<Locale> availloc = Arrays.asList(NumberFormat.getAvailableLocales());
    List<Locale> providerloc = Arrays.asList(nfp.getAvailableLocales());
    List<Locale> jreloc = Arrays.asList(LocaleData.getAvailableLocales());
    public static void main(String[] s) {
        new NumberFormatProviderTest();
    }
    NumberFormatProviderTest() {
        availableLocalesTest();
        objectValidityTest();
        messageFormatTest();
    }
    void availableLocalesTest() {
        Set<Locale> localesFromAPI = new HashSet<Locale>(availloc);
        Set<Locale> localesExpected = new HashSet<Locale>(jreloc);
        localesExpected.addAll(providerloc);
        if (localesFromAPI.equals(localesExpected)) {
            System.out.println("availableLocalesTest passed.");
        } else {
            throw new RuntimeException("availableLocalesTest failed");
        }
    }
    void objectValidityTest() {
        for (Locale target: availloc) {
            ResourceBundle rb = LocaleData.getNumberFormatData(target);
            boolean jreSupportsLocale = jreloc.contains(target);
            String[] jreNumberPatterns = null;
            if (jreSupportsLocale) {
                try {
                    jreNumberPatterns = rb.getStringArray("NumberPatterns");
                } catch (MissingResourceException mre) {}
            }
            String resultCur = getPattern(NumberFormat.getCurrencyInstance(target));
            String resultInt = getPattern(NumberFormat.getIntegerInstance(target));
            String resultNum = getPattern(NumberFormat.getNumberInstance(target));
            String resultPer = getPattern(NumberFormat.getPercentInstance(target));
            String providersCur = null;
            String providersInt = null;
            String providersNum = null;
            String providersPer = null;
            if (providerloc.contains(target)) {
                NumberFormat dfCur = nfp.getCurrencyInstance(target);
                if (dfCur != null) {
                    providersCur = getPattern(dfCur);
                }
                NumberFormat dfInt = nfp.getIntegerInstance(target);
                if (dfInt != null) {
                    providersInt = getPattern(dfInt);
                }
                NumberFormat dfNum = nfp.getNumberInstance(target);
                if (dfNum != null) {
                    providersNum = getPattern(dfNum);
                }
                NumberFormat dfPer = nfp.getPercentInstance(target);
                if (dfPer != null) {
                    providersPer = getPattern(dfPer);
                }
            }
            String jresCur = null;
            String jresInt = null;
            String jresNum = null;
            String jresPer = null;
            if (jreSupportsLocale) {
                DecimalFormat dfCur = new DecimalFormat(jreNumberPatterns[1],
                    DecimalFormatSymbols.getInstance(target));
                if (dfCur != null) {
                    adjustForCurrencyDefaultFractionDigits(dfCur);
                    jresCur = dfCur.toPattern();
                }
                DecimalFormat dfInt = new DecimalFormat(jreNumberPatterns[0],
                    DecimalFormatSymbols.getInstance(target));
                if (dfInt != null) {
                    dfInt.setMaximumFractionDigits(0);
                    dfInt.setDecimalSeparatorAlwaysShown(false);
                    dfInt.setParseIntegerOnly(true);
                    jresInt = dfInt.toPattern();
                }
                DecimalFormat dfNum = new DecimalFormat(jreNumberPatterns[0],
                    DecimalFormatSymbols.getInstance(target));
                if (dfNum != null) {
                    jresNum = dfNum.toPattern();
                }
                DecimalFormat dfPer = new DecimalFormat(jreNumberPatterns[2],
                    DecimalFormatSymbols.getInstance(target));
                if (dfPer != null) {
                    jresPer = dfPer.toPattern();
                }
            }
            checkValidity(target, jresCur, providersCur, resultCur, jreSupportsLocale);
            checkValidity(target, jresInt, providersInt, resultInt, jreSupportsLocale);
            checkValidity(target, jresNum, providersNum, resultNum, jreSupportsLocale);
            checkValidity(target, jresPer, providersPer, resultPer, jreSupportsLocale);
        }
    }
    void adjustForCurrencyDefaultFractionDigits(DecimalFormat df) {
        DecimalFormatSymbols dfs = df.getDecimalFormatSymbols();
        Currency currency = dfs.getCurrency();
        if (currency == null) {
            try {
                currency = Currency.getInstance(dfs.getInternationalCurrencySymbol());
            } catch (IllegalArgumentException e) {
            }
        }
        if (currency != null) {
            int digits = currency.getDefaultFractionDigits();
            if (digits != -1) {
                int oldMinDigits = df.getMinimumFractionDigits();
                if (oldMinDigits == df.getMaximumFractionDigits()) {
                    df.setMinimumFractionDigits(digits);
                    df.setMaximumFractionDigits(digits);
                } else {
                    df.setMinimumFractionDigits(Math.min(digits, oldMinDigits));
                    df.setMaximumFractionDigits(digits);
                }
            }
        }
    }
    private static String getPattern(NumberFormat nf) {
        if (nf instanceof DecimalFormat) {
            return ((DecimalFormat)nf).toPattern();
        }
        if (nf instanceof FooNumberFormat) {
            return ((FooNumberFormat)nf).toPattern();
        }
        return null;
    }
    private static final String[] NUMBER_PATTERNS = {
        "num={0,number}",
        "num={0,number,currency}",
        "num={0,number,percent}",
        "num={0,number,integer}"
    };
    void messageFormatTest() {
        for (Locale target : providerloc) {
            for (String pattern : NUMBER_PATTERNS) {
                MessageFormat mf = new MessageFormat(pattern, target);
                String toPattern = mf.toPattern();
                if (!pattern.equals(toPattern)) {
                    throw new RuntimeException("MessageFormat.toPattern: got '"
                                               + toPattern
                                               + "', expected '" + pattern + "'");
                }
            }
        }
    }
}
