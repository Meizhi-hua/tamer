public class IsValueTest {
    private static String failed;
    public static void main(String[] args) throws Exception {
        CompositeType ctOld =
            new CompositeType("same.type.name", "old",
                new String[] {"int", "string"},
                new String[] {"an int", "a string"},
                new OpenType[] {SimpleType.INTEGER, SimpleType.STRING});
        CompositeType ctNew =
            new CompositeType("same.type.name", "new",
                new String[] {"int", "int2", "string"},
                new String[] {"an int", "another int", "a string"},
                new OpenType[] {SimpleType.INTEGER, SimpleType.INTEGER, SimpleType.STRING});
        CompositeData cdOld =
            new CompositeDataSupport(ctOld,
                new String[] {"string", "int"},
                new Object[] {"bar", 17});
        CompositeData cdNew =
            new CompositeDataSupport(ctNew,
                new String[] {"int2", "int", "string"},
                new Object[] {4, 3, "foo"});
        check(ctOld.isValue(cdNew), "isValue: " + ctOld + "[" + cdNew + "]");
        check(!ctNew.isValue(cdOld), "isValue: " + ctNew + "[" + cdOld + "]");
        CompositeType ctWrapOld =
            new CompositeType("wrapper", "wrapper",
                new String[] {"wrapped"},
                new String[] {"wrapped"},
                new OpenType[] {ctOld});
        try {
            new CompositeDataSupport(ctWrapOld,
                new String[] {"wrapped"},
                new Object[] {cdNew});
            check(true, "CompositeDataSupport containing CompositeDataSupport");
        } catch (Exception e) {
            e.printStackTrace(System.out);
            check(false, "CompositeDataSupport containing CompositeDataSupport: " + e);
        }
        CompositeType ctWrapNew =
            new CompositeType("wrapper", "wrapper",
                new String[] {"wrapped"},
                new String[] {"wrapped"},
                new OpenType[] {ctNew});
        try {
            new CompositeDataSupport(ctWrapNew,
                new String[] {"wrapped"},
                new Object[] {cdOld});
            check(false, "CompositeDataSupport containing old did not get exception");
        } catch (OpenDataException e) {
            check(true, "CompositeDataSupport containing old got expected exception: " + e);
        }
        TabularType ttOld =
            new TabularType("tabular", "tabular", ctOld, new String[] {"int"});
        TabularData tdOld =
            new TabularDataSupport(ttOld);
        try {
            tdOld.put(cdNew);
            check(true, "TabularDataSupport adding extended CompositeData");
        } catch (Exception e) {
            e.printStackTrace(System.out);
            check(false, "TabularDataSupport adding extended CompositeData: " + e);
        }
        TabularType ttNew =
            new TabularType("tabular", "tabular", ctNew, new String[] {"int"});
        TabularData tdNew =
            new TabularDataSupport(ttNew);
        CompositeType cttWrap =
            new CompositeType("wrapTT", "wrapTT",
                new String[] {"wrapped"},
                new String[] {"wrapped"},
                new OpenType[] {ttOld});
        try {
            new CompositeDataSupport(cttWrap,
                new String[] {"wrapped"},
                new Object[] {tdNew});
            check(true, "CompositeDataSupport adding extended TabularData");
        } catch (Exception e) {
            e.printStackTrace(System.out);
            check(false, "CompositeDataSupport adding extended TabularData: " + e);
        }
        if (failed != null)
            throw new Exception("TEST FAILED: " + failed);
    }
    private static void check(boolean value, String what) {
        if (value)
            System.out.println("OK: " + what);
        else {
            failed = what;
            System.out.println("FAILED: " + what);
        }
    }
}
