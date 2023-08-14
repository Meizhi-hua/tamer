public class Bug4823811 {
    private static Locale localeEG = new Locale("ar", "EG");
    private static Locale localeUS = Locale.US;
    private static String JuneInArabic = "\u064a\u0648\u0646\u064a\u0648";
    private static String JulyInArabic = "\u064a\u0648\u0644\u064a\u0648";
    private static String JuneInEnglish = "June";
    private static String JulyInEnglish = "July";
    private static String BORDER =
        "============================================================";
    private static int ERA = Calendar.ERA;
    private static int BC = GregorianCalendar.BC;
    private static int APR = Calendar.APRIL;
    private static int MAY = Calendar.MAY;
    private static int JUN = Calendar.JUNE;
    private static int JUL = Calendar.JULY;
    private static String[] patterns = {
        "yyyy MMMM d H m s",
        "yyyy MM dd hh mm ss",
        "yyyy M d h m s",
        " yyyy M d h m s",
        "yyyy M d h m s ",
        "s m h d M yyyy",
        " s m h d M yyyy",
        "s m h d M yyyy ",
    };
    private static char originalMinusSign1 = ':';
    private static char originalMinusSign2 = '\uff0d';  
    private static String[] delimiters = {"-", "/", ":", "/", "\uff0d", "/"};
    private static String[][] specialDelimiters = {
        {"--", "-/", "::", ":/", "\uff0d\uff0d", "\uff0d/"},
        {"--", "/-", "::", "/:", "\uff0d\uff0d", "/\uff0d"},
    };
    private static String[][] datesToParse = {
        {"2008 JULY 20 3 12 83",
         "2008  JULY 20 3 12 83",
         "2008 JULY  20 3 12 83"},
        {"2008 07 20 03 12 83",
         "2008  07 20 03 12 83",
         "2008 07  20 03 12 83"},
        {"2008 7 20 3 12 83",
         "2008  7 20  3 12 83",
         "2008 7  20  3 12 83"},
        {" 2008 7 20 3 12 83",
         "  2008 7 20 3 12 83",
         " 2008  7 20 3 12 83",
         "2008 7 20 3 12 83"},
        {"2008 7 20 3 12 83 ",
         "2008 7 20 3 12 83  ",
         "2008 7 20 3 12 83"},
        {"83 12 3 20 7 2008",
         "83  12 3  20 7 2008",
         "83 12  3  20 7 2008"},
        {" 83 12 3 20 7 2008",
         "  83 12 3 20 7 2008",
         " 83  12 3 20 7 2008",
         "83 12 3 20 7 2008"},
        {"83 12 3 20 7 2008 ",
         "83 12 3 20 7 2008  ",
         "83 12 3 20 7 2008"},
    };
    private static String[][] formattedDatesEG = {
        {"2008 JULY 20 3 13 23",
         "2009 JULY 20 3 13 23",
         null},
        {"2008 07 20 03 13 23",
         "2009 07 20 03 13 23",
         "2007 05 20 03 13 23"},
        {"2008 7 20 3 13 23",
         "2009 6 10 3 13 23",
         "2007 4 10 3 13 23"},
        {" 2008 7 20 3 13 23",
         null,
         " 2009 7 20 3 13 23",
         null},
        {"2008 7 20 3 13 23 ",
         "2008 7 20 3 10 37 ",
         null},
        {"23 13 3 20 7 2008",
         "37 10 9 19 7 2008",
         "23 49 8 19 7 2008"},
        {" 23 13 3 20 7 2008",
         null,
         " 37 10 3 20 7 2008",
         null},
        {"23 13 3 20 7 2008 ",
         "23 13 3 20 7 2009 ",
         null},
    };
    private static String[][] formattedDatesUS = {
        {"2008 JULY 20 3 13 23",
         null,
         "2008 JUNE 10 3 13 23"},
        {"2008 07 20 03 13 23",
         "2007 05 20 03 13 23",
         "2008 06 10 03 13 23"},
        {"2008 7 20 3 13 23",
         "2007 5 19 9 13 23",
         "2008 6 9 9 13 23"},
        {" 2008 7 20 3 13 23",
         " 2009 7 20 3 13 23",
         " 2007 5 20 3 13 23",
         null},
        {"2008 7 20 3 13 23 ",
         "2008 7 20 3 13 23 ",
         null},
        {"23 13 3 20 7 2008",
         "23 49 2 10 6 2008",
         "23 13 9 9 6 2008"},
        {" 23 13 3 20 7 2008",
         " 37 10 3 20 7 2008",
         " 23 49 2 20 7 2008",
         null},
        {"23 13 3 20 7 2008 ",
         "23 13 3 20 7 2008 ",
         null},
    };
    private static GregorianCalendar[][] datesEG = {
        {new GregorianCalendar( 2008, JUL,  20,  3,  12,  83),
         new GregorianCalendar(-2008, JUL,  20,  3,  12,  83),
         null},
        {new GregorianCalendar( 2008, JUL,  20,  3,  12,  83),
         new GregorianCalendar(-2008, JUL,  20,  3,  12,  83),
         new GregorianCalendar( 2007, MAY,  20,  3,  12,  83)},
        {new GregorianCalendar( 2008, JUL,  20,  3,  12,  83),
         new GregorianCalendar(-2008, JUL, -20,  3,  12,  83),
         new GregorianCalendar( 2007, APR,  10,  3,  12,  83)},
        {new GregorianCalendar( 2008, JUL,  20,  3,  12,  83),
         null,
         new GregorianCalendar(-2008, JUL,  20,  3,  12,  83),
         null},
        {new GregorianCalendar( 2008, JUL,  20,  3,  12,  83),
         new GregorianCalendar( 2008, JUL,  20,  3,  12, -83),
         null},
        {new GregorianCalendar( 2008, JUL,  20,  3,  12,  83),
         new GregorianCalendar( 2008, JUL,  20, -3,  12, -83),
         new GregorianCalendar( 2008, JUL,  20, -3, -12,  83)},
        {new GregorianCalendar( 2008, JUL,  20,  3,  12,  83),
         null,
         new GregorianCalendar( 2008, JUL,  20,  3,  12, -83),
         null},
        {new GregorianCalendar( 2008, JUL,  20,  3,  12,  83),
         new GregorianCalendar(-2008, JUL,  20,  3,  12,  83),
         null},
    };
    private static GregorianCalendar[][] datesUS = {
        {new GregorianCalendar( 2008, JUL,  20,  3,  12,  83),
         null,
         new GregorianCalendar( 2008, JUN,  10,  3,  12,  83)},
        {new GregorianCalendar( 2008, JUL,  20,  3,  12,  83),
         new GregorianCalendar( 2007, MAY,  20,  3,  12,  83),
         new GregorianCalendar( 2008, JUN,  10,  3,  12,  83)},
        {new GregorianCalendar( 2008, JUL,  20,  3,  12,  83),
         new GregorianCalendar( 2007, MAY,  20, -3,  12,  83),
         new GregorianCalendar( 2008, JUL, -20, -3,  12,  83)},
        {new GregorianCalendar( 2008, JUL,  20,  3,  12,  83),
         new GregorianCalendar(-2008, JUL,  20,  3,  12,  83),
         new GregorianCalendar( 2007, MAY,  20,  3,  12,  83),
         null},
        {new GregorianCalendar( 2008, JUL,  20,  3,  12,  83),
         new GregorianCalendar( 2008, JUL,  20,  3,  12,  83),
         null},
        {new GregorianCalendar( 2008, JUL,  20,  3,  12,  83),
         new GregorianCalendar( 2008, JUL, -20,  3, -12,  83),
         new GregorianCalendar( 2008, JUL, -20, -3,  12,  83)},
        {new GregorianCalendar( 2008, JUL,  20,  3,  12,  83),
         new GregorianCalendar( 2008, JUL,  20,  3,  12, -83),
         new GregorianCalendar( 2008, JUL,  20,  3, -12,  83),
         null},
        {new GregorianCalendar( 2008, JUL,  20,  3,  12,  83),
         new GregorianCalendar( 2008, JUL,  20,  3,  12,  83),
         null},
    };
    private static boolean err = false;
    private static boolean verbose = false;
    public static void main(String[] args) {
        if (args.length == 1 && args[0].equals("-v")) {
            verbose = true;
        }
        Locale defaultLocale = Locale.getDefault();
        TimeZone defaultTimeZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tokyo"));
        try {
            testDateFormat1();
            testDateFormat2();
            testDateFormat3();
            testNumberFormat();
        }
        catch (Exception e) {
            err = true;
            System.err.println("Unexpected exception: " + e);
        }
        finally {
            Locale.setDefault(defaultLocale);
            TimeZone.setDefault(defaultTimeZone);
            if (err) {
                System.err.println(BORDER + " Test failed.");
                throw new RuntimeException("Date/Number formatting/parsing error.");
            } else {
                System.out.println(BORDER + " Test passed.");
            }
        }
    }
    private static void testDateFormat1() {
        for (int i = 0; i < patterns.length; i++) {
            System.out.println(BORDER);
            for (int j = 0; j <= 1; j++) {
                String pattern = patterns[i].replaceAll(" ", delimiters[j]);
                System.out.println("Pattern=\"" + pattern + "\"");
                System.out.println("*** DateFormat.format test in ar_EG");
                testDateFormatFormattingInRTL(pattern, i, j, null, localeEG, false);
                System.out.println("*** DateFormat.parse test in ar_EG");
                testDateFormatParsingInRTL(pattern, i, j, null, localeEG, false);
                System.out.println("*** DateFormat.format test in en_US");
                testDateFormatFormattingInLTR(pattern, i, j, null, localeUS, true);
                System.out.println("*** DateFormat.parse test in en_US");
                testDateFormatParsingInLTR(pattern, i, j, null, localeUS, true);
            }
        }
    }
    private static void testDateFormat2() {
        DecimalFormat dfEG = (DecimalFormat)NumberFormat.getInstance(localeEG);
        DecimalFormat dfUS = (DecimalFormat)NumberFormat.getInstance(localeUS);
        DecimalFormatSymbols dfsEG = dfEG.getDecimalFormatSymbols();
        DecimalFormatSymbols dfsUS = dfUS.getDecimalFormatSymbols();
        dfsEG.setMinusSign(originalMinusSign1);
        dfsUS.setMinusSign(originalMinusSign1);
        dfEG.setDecimalFormatSymbols(dfsUS);
        dfUS.setDecimalFormatSymbols(dfsEG);
        String patternEG = dfEG.toPattern();
        String patternUS = dfUS.toPattern();
        dfEG.applyPattern(patternUS);
        dfUS.applyPattern(patternEG);
        for (int i = 0; i < patterns.length; i++) {
            System.out.println(BORDER);
            for (int j = 2; j <= 3; j++) {
                String pattern = patterns[i].replaceAll(" ", delimiters[j]);
                System.out.println("Pattern=\"" + pattern + "\"");
                System.out.println("*** DateFormat.format test in modified en_US");
                testDateFormatFormattingInRTL(pattern, i, j, dfUS, localeUS, true);
                System.out.println("*** DateFormat.parse test in modified en_US");
                testDateFormatParsingInRTL(pattern, i, j, dfUS, localeUS, true);
                System.out.println("*** DateFormat.format test in modified ar_EG");
                testDateFormatFormattingInLTR(pattern, i, j, dfEG, localeEG, false);
                System.out.println("*** DateFormat.parse test in modified ar_EG");
                testDateFormatParsingInLTR(pattern, i, j, dfEG, localeEG, false);
            }
        }
    }
    private static void testDateFormat3() {
        DecimalFormat dfEG = (DecimalFormat)NumberFormat.getInstance(localeEG);
        DecimalFormat dfUS = (DecimalFormat)NumberFormat.getInstance(localeUS);
        DecimalFormatSymbols dfsEG = dfEG.getDecimalFormatSymbols();
        DecimalFormatSymbols dfsUS = dfUS.getDecimalFormatSymbols();
        dfsEG.setMinusSign(originalMinusSign2);
        dfsUS.setMinusSign(originalMinusSign2);
        dfEG.setDecimalFormatSymbols(dfsEG);
        dfUS.setDecimalFormatSymbols(dfsUS);
        for (int i = 0; i < patterns.length; i++) {
            System.out.println(BORDER);
            for (int j = 4; j <= 5; j++) {
                String pattern = patterns[i].replaceAll(" ", delimiters[j]);
                System.out.println("Pattern=\"" + pattern + "\"");
                System.out.println("*** DateFormat.format test in modified ar_EG");
                testDateFormatFormattingInRTL(pattern, i, j, dfEG, localeEG, false);
                System.out.println("*** DateFormat.parse test in modified ar_EG");
                testDateFormatParsingInRTL(pattern, i, j, dfEG, localeEG, false);
                System.out.println("*** DateFormat.format test in modified en_US");
                testDateFormatFormattingInLTR(pattern, i, j, dfUS, localeUS, true);
                System.out.println("*** DateFormat.parse test in modified en_US");
                testDateFormatParsingInLTR(pattern, i, j, dfUS, localeUS, true);
            }
        }
    }
    private static void testDateFormatFormattingInRTL(String pattern,
                                                      int basePattern,
                                                      int delimiter,
                                                      NumberFormat nf,
                                                      Locale locale,
                                                      boolean useEnglishMonthName) {
        Locale.setDefault(locale);
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        if (nf != null) {
            sdf.setNumberFormat(nf);
        }
        for (int i = 0; i < datesToParse[basePattern].length; i++) {
            if (datesEG[basePattern][i] == null) {
                continue;
            }
            String expected = formattedDatesEG[basePattern][i]
                              .replaceAll("JUNE", (useEnglishMonthName ?
                                                   JuneInEnglish : JuneInArabic))
                              .replaceAll("JULY", (useEnglishMonthName ?
                                                   JulyInEnglish : JulyInArabic))
                              .replaceAll(" ", delimiters[delimiter]);
            testDateFormatFormatting(sdf, pattern, datesEG[basePattern][i],
                expected, locale.toString());
        }
    }
    private static void testDateFormatFormattingInLTR(String pattern,
                                                      int basePattern,
                                                      int delimiter,
                                                      NumberFormat nf,
                                                      Locale locale,
                                                      boolean useEnglishMonthName) {
        Locale.setDefault(locale);
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        if (nf != null) {
            sdf.setNumberFormat(nf);
        }
        for (int i = 0; i < datesToParse[basePattern].length; i++) {
            if (datesUS[basePattern][i] == null) {
                continue;
            }
            String expected = formattedDatesUS[basePattern][i]
                              .replaceAll("JUNE", (useEnglishMonthName ?
                                                   JuneInEnglish : JuneInArabic))
                              .replaceAll("JULY", (useEnglishMonthName ?
                                                   JulyInEnglish : JulyInArabic))
                              .replaceAll(" ", delimiters[delimiter]);
            testDateFormatFormatting(sdf, pattern, datesUS[basePattern][i],
                expected, locale.toString());
        }
    }
    private static void testDateFormatFormatting(SimpleDateFormat sdf,
                                                 String pattern,
                                                 GregorianCalendar givenGC,
                                                 String expected,
                                                 String locale) {
        Date given = givenGC.getTime();
        String str = sdf.format(given);
        if (expected.equals(str)) {
            if (verbose) {
                System.out.print("  Passed: SimpleDateFormat(");
                System.out.print(locale + ", \"" + pattern + "\").format(");
                System.out.println(given + ")");
                System.out.print("      ---> \"" + str + "\" ");
                System.out.println((givenGC.get(ERA) == BC) ? "(BC)" : "(AD)");
            }
        } else {
            err = true;
            System.err.print("  Failed: Unexpected SimpleDateFormat(");
            System.out.print(locale + ", \"" + pattern + "\").format(");
            System.out.println(given + ") result.");
            System.out.println("      Expected: \"" + expected + "\"");
            System.out.print("      Got:      \"" + str + "\" ");
            System.out.println((givenGC.get(ERA) == BC) ? "(BC)" : "(AD)");
        }
    }
    private static void testDateFormatParsingInRTL(String pattern,
                                                   int basePattern,
                                                   int delimiter,
                                                   NumberFormat nf,
                                                   Locale locale,
                                                   boolean useEnglishMonthName) {
        Locale.setDefault(locale);
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        if (nf != null) {
            sdf.setNumberFormat(nf);
        }
        for (int i = 0; i < datesToParse[basePattern].length; i++) {
            String given = datesToParse[basePattern][i]
                           .replaceAll("  ", specialDelimiters[0][delimiter])
                           .replaceAll(" ", delimiters[delimiter]);
            testDateFormatParsing(sdf, pattern,
                given.replaceAll("JULY", (useEnglishMonthName ?
                                          JulyInEnglish :  JulyInArabic)),
                datesEG[basePattern][i], locale.toString());
        }
    }
    private static void testDateFormatParsingInLTR(String pattern,
                                                   int basePattern,
                                                   int delimiter,
                                                   NumberFormat nf,
                                                   Locale locale,
                                                   boolean useEnglishMonthName) {
        Locale.setDefault(locale);
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        if (nf != null) {
            sdf.setNumberFormat(nf);
        }
        for (int i = 0; i < datesToParse[basePattern].length; i++) {
            String given = datesToParse[basePattern][i]
                           .replaceAll("  ", specialDelimiters[1][delimiter])
                           .replaceAll(" ", delimiters[delimiter]);
            testDateFormatParsing(sdf, pattern,
                given.replaceAll("JULY", (useEnglishMonthName ?
                                          JulyInEnglish :  JulyInArabic)),
                datesUS[basePattern][i], locale.toString());
        }
    }
    private static void testDateFormatParsing(SimpleDateFormat sdf,
                                              String pattern,
                                              String given,
                                              GregorianCalendar expectedGC,
                                              String locale) {
        try {
            Date d = sdf.parse(given);
            if (expectedGC == null) {
                err = true;
                System.err.print("  Failed: SimpleDateFormat(" + locale);
                System.err.print(", \"" + pattern + "\").parse(\"" + given);
                System.err.println("\") should have thrown ParseException");
            } else if (expectedGC.getTime().equals(d)) {
                if (verbose) {
                    System.out.print("  Passed: SimpleDateFormat(" + locale);
                    System.out.print(", \"" + pattern + "\").parse(\"" + given);
                    System.out.println("\")");
                    System.out.print("      ---> " + d + " (" + d.getTime());
                    System.out.println(")");
                }
            } else {
                err = true;
                System.err.print("  Failed: SimpleDateFormat(" + locale);
                System.err.print(", \"" + pattern + "\").parse(\"" + given);
                System.err.println("\")");
                System.err.print("      Expected: " + expectedGC.getTime());
                System.err.println(" (" + d.getTime() + ")");
                System.err.print("      Got:      " + d + " (" + d.getTime());
                System.err.println(")");
                System.err.print("      Pattern:  \"");
                System.err.print(((DecimalFormat)sdf.getNumberFormat()).toPattern());
                System.err.println("\"");
            }
        }
        catch (ParseException pe) {
            if (expectedGC == null) {
                if (verbose) {
                    System.out.print("  Passed: SimpleDateFormat(" + locale);
                    System.out.print(", \"" + pattern + "\").parse(\"" + given);
                    System.out.println("\")");
                    System.out.println("      threw ParseException as expected");
                }
            } else {
                err = true;
                System.err.println("  Failed: Unexpected exception with");
                System.err.print("    SimpleDateFormat(" + locale);
                System.err.print(", \"" + pattern + "\").parse(\"");
                System.err.println(given + "\"):");
                System.err.println("      " + pe);
                System.err.print("      Pattern: \"");
                System.err.print(((DecimalFormat)sdf.getNumberFormat()).toPattern());
                System.err.println("\"");
                System.err.print("      Month 0: ");
                System.err.println(sdf.getDateFormatSymbols().getMonths()[0]);
            }
        }
    }
    private static void testNumberFormat() {
        NumberFormat nfEG = NumberFormat.getInstance(localeEG);
        NumberFormat nfUS = NumberFormat.getInstance(localeUS);
        System.out.println("*** DecimalFormat.format test in ar_EG");
        testNumberFormatFormatting(nfEG, -123456789, "123,456,789-", "ar_EG");
        testNumberFormatFormatting(nfEG, -456, "456-", "ar_EG");
        System.out.println("*** DecimalFormat.parse test in ar_EG");
        testNumberFormatParsing(nfEG, "123-", new Long(-123), "ar_EG");
        testNumberFormatParsing(nfEG, "123--", new Long(-123), "ar_EG");
        testNumberFormatParsingCheckException(nfEG, "-123", 0, "ar_EG");
        System.out.println("*** DecimalFormat.format test in en_US");
        testNumberFormatFormatting(nfUS, -123456789, "-123,456,789", "en_US");
        testNumberFormatFormatting(nfUS, -456, "-456", "en_US");
        System.out.println("*** DecimalFormat.parse test in en_US");
        testNumberFormatParsing(nfUS, "123-", new Long(123), "en_US");
        testNumberFormatParsing(nfUS, "-123", new Long(-123), "en_US");
        testNumberFormatParsingCheckException(nfUS, "--123", 0, "en_US");
    }
    private static void testNumberFormatFormatting(NumberFormat nf,
                                                   int given,
                                                   String expected,
                                                   String locale) {
        String str = nf.format(given);
        if (expected.equals(str)) {
            if (verbose) {
                System.out.print("  Passed: NumberFormat(" + locale);
                System.out.println(").format(" + given + ")");
                System.out.println("      ---> \"" + str + "\"");
            }
        } else {
            err = true;
            System.err.print("  Failed: Unexpected NumberFormat(" + locale);
            System.err.println(").format(" + given + ") result.");
            System.err.println("      Expected: \"" + expected + "\"");
            System.err.println("      Got:      \"" + str + "\"");
        }
    }
    private static void testNumberFormatParsing(NumberFormat nf,
                                                String given,
                                                Number expected,
                                                String locale) {
        try {
            Number n = nf.parse(given);
            if (n.equals(expected)) {
                if (verbose) {
                    System.out.print("  Passed: NumberFormat(" + locale);
                    System.out.println(").parse(\"" + given + "\")");
                    System.out.println("      ---> " + n);
                }
            } else {
                err = true;
                System.err.print("  Failed: Unexpected NumberFormat(" + locale);
                System.err.println(").parse(\"" + given + "\") result.");
                System.err.println("      Expected: " + expected);
                System.err.println("      Got:      " + n);
            }
        }
        catch (ParseException pe) {
            err = true;
            System.err.print("  Failed: Unexpected exception with NumberFormat(");
            System.err.println(locale + ").parse(\"" + given + "\") :");
            System.err.println("    " + pe);
        }
    }
    private static void testNumberFormatParsingCheckException(NumberFormat nf,
                                                              String given,
                                                              int expected,
                                                              String locale) {
        try {
            Number n = nf.parse(given);
            err = true;
            System.err.print("  Failed: NumberFormat(" + locale);
            System.err.println(").parse(\"" + given + "\")");
            System.err.println("      should have thrown ParseException");
        }
        catch (ParseException pe) {
            int errorOffset = pe.getErrorOffset();
            if (errorOffset == expected) {
                if (verbose) {
                    System.out.print("  Passed: NumberFormat(" + locale);
                    System.out.println(").parse(\"" + given + "\")");
                    System.out.print("      threw ParseException as expected, and its errorOffset was correct: ");
                    System.out.println(errorOffset);
                }
            } else {
                err = true;
                System.err.print("  Failed: NumberFormat(" + locale);
                System.err.println(").parse(\"" + given + "\")");
                System.err.print("      threw ParseException as expected, but its errorOffset was incorrect: ");
                System.err.println(errorOffset);
            }
        }
    }
}
