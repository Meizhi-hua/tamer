public class Bug6317929 {
    static Locale[] locales2Test = new Locale[] {
        new Locale("en"),
        new Locale("de"),
        new Locale("es"),
        new Locale("fr"),
        new Locale("it"),
        new Locale("ja"),
        new Locale("ko"),
        new Locale("sv"),
        new Locale("zh","CN"),
        new Locale("zh","TW")
    };
    public static void main(String[] args) {
        Locale tzLocale;
        TimeZone Coral_Harbour = TimeZone.getTimeZone("America/Coral_Harbour");
        tzLocale = locales2Test[0];
        if (!Coral_Harbour.getDisplayName(false, TimeZone.LONG, tzLocale).equals
           ("Eastern Standard Time"))
            throw new RuntimeException("\n" + tzLocale + ": LONG, " +
                                       "non-daylight saving name for " +
                                       "America/Coral_Harbour should be " +
                                       "\"Eastern Standard Time\"");
        tzLocale = locales2Test[1];
        if (!Coral_Harbour.getDisplayName(false, TimeZone.LONG, tzLocale).equals
           ("\u00d6stliche Normalzeit"))
            throw new RuntimeException("\n" + tzLocale + ": LONG, " +
                                       "non-daylight saving name for " +
                                       "America/Coral_Harbour should be " +
                                       "\"\u00d6stliche Normalzeit\"");
        tzLocale = locales2Test[2];
        if (!Coral_Harbour.getDisplayName(false, TimeZone.LONG, tzLocale).equals
           ("Hora est\u00e1ndar Oriental"))
            throw new RuntimeException("\n" + tzLocale + ": LONG, " +
                                       "non-daylight saving name for " +
                                       "America/Coral_Harbour should be " +
                                       "\"Hora est\u00e1ndar Oriental\"");
        tzLocale = locales2Test[3];
        if (!Coral_Harbour.getDisplayName(false, TimeZone.LONG, tzLocale).equals
           ("Heure normale de l'Est"))
            throw new RuntimeException("\n" + tzLocale + ": LONG, " +
                                       "non-daylight saving name for " +
                                       "America/Coral_Harbour should be " +
                                       "\"Heure normale de l'Est\"");
        tzLocale = locales2Test[4];
        if (!Coral_Harbour.getDisplayName(false, TimeZone.LONG, tzLocale).equals
           ("Ora solare USA orientale"))
            throw new RuntimeException("\n" + tzLocale + ": LONG, " +
                                       "non-daylight saving name for " +
                                       "America/Coral_Harbour should be " +
                                       "\"Ora solare USA orientale\"");
        tzLocale = locales2Test[5];
        if (!Coral_Harbour.getDisplayName(false, TimeZone.LONG, tzLocale).equals
           ("\u6771\u90e8\u6a19\u6e96\u6642"))
            throw new RuntimeException("\n" + tzLocale + ": LONG, " +
                                       "non-daylight saving name for " +
                                       "America/Coral_Harbour should be " +
                                       "\"\u6771\u90e8\u6a19\u6e96\u6642\"");
        tzLocale = locales2Test[6];
        if (!Coral_Harbour.getDisplayName(false, TimeZone.LONG, tzLocale).equals
           ("\ub3d9\ubd80 \ud45c\uc900\uc2dc"))
            throw new RuntimeException("\n" + tzLocale + ": LONG, " +
                                       "non-daylight saving name for " +
                                       "America/Coral_Harbour should be " +
                                       "\"\ub3d9\ubd80 \ud45c\uc900\uc2dc\"");
        tzLocale = locales2Test[7];
        if (!Coral_Harbour.getDisplayName(false, TimeZone.LONG, tzLocale).equals
           ("Eastern, normaltid"))
            throw new RuntimeException("\n" + tzLocale + ": LONG, " +
                                       "non-daylight saving name for " +
                                       "America/Coral_Harbour should be " +
                                       "\"Eastern, normaltid\"");
        tzLocale = locales2Test[8];
        if (!Coral_Harbour.getDisplayName(false, TimeZone.LONG, tzLocale).equals
           ("\u4e1c\u90e8\u6807\u51c6\u65f6\u95f4"))
            throw new RuntimeException("\n" + tzLocale + ": LONG, " +
                                       "non-daylight saving name for " +
                                       "America/Coral_Harbour should be " +
                                       "\"\u4e1c\u90e8\u6807\u51c6\u65f6\u95f4\"");
        tzLocale = locales2Test[9];
        if (!Coral_Harbour.getDisplayName(false, TimeZone.LONG, tzLocale).equals
           ("\u6771\u65b9\u6a19\u6e96\u6642\u9593"))
            throw new RuntimeException("\n" + tzLocale + ": LONG, " +
                                       "non-daylight saving name for " +
                                       "America/Coral_Harbour should be " +
                                       "\"\u6771\u65b9\u6a19\u6e96\u6642\u9593\"");
        TimeZone Currie = TimeZone.getTimeZone("Australia/Currie");
        tzLocale = locales2Test[0];
        if (!Currie.getDisplayName(false, TimeZone.LONG, tzLocale).equals
           ("Eastern Standard Time (New South Wales)"))
            throw new RuntimeException("\n" + tzLocale + ": LONG, " +
                                       "non-daylight saving name for " +
                                       "Australia/Currie should be " +
                                       "\"Eastern Standard Time " +
                                       "(New South Wales)\"");
        tzLocale = locales2Test[1];
        if (!Currie.getDisplayName(false, TimeZone.LONG, tzLocale).equals
           ("\u00d6stliche Normalzeit (New South Wales)"))
            throw new RuntimeException("\n" + tzLocale + ": LONG, " +
                                       "non-daylight saving name for " +
                                       "Australia/Currie should be " +
                                       "\"\u00d6stliche Normalzeit " +
                                       "(New South Wales)\"");
        tzLocale = locales2Test[2];
        if (!Currie.getDisplayName(false, TimeZone.LONG, tzLocale).equals
           ("Hora est\u00e1ndar Oriental (Nueva Gales del Sur)"))
            throw new RuntimeException("\n" + tzLocale + ": LONG, " +
                                       "non-daylight saving name for " +
                                       "Australia/Currie should be " +
                                       "\"Hora est\u00e1ndar Oriental " +
                                       "(Nueva Gales del Sur)\"");
        tzLocale = locales2Test[3];
        if (!Currie.getDisplayName(false, TimeZone.LONG, tzLocale).equals
           ("Heure normale de l'Est (Nouvelle-Galles du Sud)"))
            throw new RuntimeException("\n" + tzLocale + ": LONG, " +
                                       "non-daylight saving name for " +
                                       "Australia/Currie should be " +
                                       "\"Heure normale de l'Est " +
                                       "(Nouvelle-Galles du Sud)\"");
        tzLocale = locales2Test[4];
        if (!Currie.getDisplayName(false, TimeZone.LONG, tzLocale).equals
           ("Ora solare dell'Australia orientale (Nuovo Galles del Sud)"))
            throw new RuntimeException("\n" + tzLocale + ": LONG, " +
                                       "non-daylight saving name for " +
                                       "Australia/Currie should be " +
                                       "\"Ora solare dell'Australia orientale " +
                                       "(Nuovo Galles del Sud)\"");
        tzLocale = locales2Test[5];
        if (!Currie.getDisplayName(false, TimeZone.LONG, tzLocale).equals
           ("\u6771\u90e8\u6a19\u6e96\u6642 " +
            "(\u30cb\u30e5\u30fc\u30b5\u30a6\u30b9\u30a6\u30a7\u30fc\u30eb\u30ba)"))
            throw new RuntimeException("\n" + tzLocale + ": LONG, " +
                                       "non-daylight saving name for " +
                                       "Australia/Currie should be " +
                                       "\"\u6771\u90e8\u6a19\u6e96\u6642 " +
                                       "(\u30cb\u30e5\u30fc\u30b5\u30a6\u30b9" +
                                       "\u30a6\u30a7\u30fc\u30eb\u30ba)\"");
        tzLocale = locales2Test[6];
        if (!Currie.getDisplayName(false, TimeZone.LONG, tzLocale).equals
           ("\ub3d9\ubd80 \ud45c\uc900\uc2dc(\ub274 \uc0ac\uc6b0\uc2a4 \uc6e8\uc77c\uc988)"))
            throw new RuntimeException("\n" + tzLocale + ": LONG, " +
                                       "non-daylight saving name for " +
                                       "Australia/Currie should be " +
                                       "\"\ub3d9\ubd80 \ud45c\uc900\uc2dc" +
                                       "(\ub274 \uc0ac\uc6b0\uc2a4 \uc6e8\uc77c\uc988)\"");
        tzLocale = locales2Test[7];
        if (!Currie.getDisplayName(false, TimeZone.LONG, tzLocale).equals
           ("Eastern, normaltid (Nya Sydwales)"))
            throw new RuntimeException("\n" + tzLocale + ": LONG, " +
                                       "non-daylight saving name for " +
                                       "Australia/Currie should be " +
                                       "\"Eastern, normaltid " +
                                       "(Nya Sydwales)\"");
        tzLocale = locales2Test[8];
        if (!Currie.getDisplayName(false, TimeZone.LONG, tzLocale).equals
           ("\u4e1c\u90e8\u6807\u51c6\u65f6\u95f4\uff08\u65b0\u5357\u5a01\u5c14\u65af\uff09"))
            throw new RuntimeException("\n" + tzLocale + ": LONG, " +
                                       "non-daylight saving name for " +
                                       "Australia/Currie should be " +
                                       "\"\u4e1c\u90e8\u6807\u51c6\u65f6\u95f4 " +
                                       "\uff08\u65b0\u5357\u5a01\u5c14\u65af\uff09\"");
        tzLocale = locales2Test[9];
        if (!Currie.getDisplayName(false, TimeZone.LONG, tzLocale).equals
           ("\u6771\u65b9\u6a19\u6e96\u6642\u9593 (\u65b0\u5357\u5a01\u723e\u65af)"))
            throw new RuntimeException("\n" + tzLocale + ": LONG, " +
                                       "non-daylight saving name for " +
                                       "Australia/Currie should be " +
                                       "\"\u6771\u65b9\u6a19\u6e96\u6642\u9593 " +
                                       "(\u65b0\u5357\u5a01\u723e\u65af)\"");
   }
}
