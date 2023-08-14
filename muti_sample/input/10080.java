public class bug4122700 {
    public static void main(String[] args) throws Exception {
        Locale[] systemLocales = Locale.getAvailableLocales();
        if (systemLocales.length == 0)
            throw new Exception("Available locale list is empty!");
        System.out.println("Found " + systemLocales.length + " locales:");
        Locale[] locales = new Locale[systemLocales.length];
        for (int i = 0; i < locales.length; i++) {
            Locale lowest = null;
            for (int j = 0; j < systemLocales.length; j++) {
                if (i > 0 && locales[i - 1].toString().compareTo(systemLocales[j].toString()) >= 0)
                    continue;
                if (lowest == null || systemLocales[j].toString().compareTo(lowest.toString()) < 0)
                    lowest = systemLocales[j];
            }
            locales[i] = lowest;
        }
        for (int i = 0; i < locales.length; i++) {
            if (locales[i].getCountry().length() == 0)
                System.out.println("    " + locales[i].getDisplayLanguage() + ":");
            else {
                if (locales[i].getVariant().length() == 0)
                    System.out.println("        " + locales[i].getDisplayCountry());
                else
                    System.out.println("        " + locales[i].getDisplayCountry() + ", "
                                    + locales[i].getDisplayVariant());
            }
        }
    }
}