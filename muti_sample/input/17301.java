public final class PackagePrefixChecker {
    private static final String PACKAGE_PREFIX = "org.omg.stub.";
    public static String packagePrefix(){ return PACKAGE_PREFIX;}
    public static String correctPackageName (String p){
        if (p==null) return p;
        if ( hasOffendingPrefix(p))
            {
               return PACKAGE_PREFIX+p;
            }
        return p;
    }
    public static boolean isOffendingPackage(String p){
        return
            !(p==null)
            &&
            ( false || hasOffendingPrefix(p) );
    }
    public static boolean hasOffendingPrefix(String p){
        return
            (      p.startsWith("java.") || p.equals("java")
                || p.startsWith("net.jini.") || p.equals("net.jini")
                || p.startsWith("jini.") || p.equals("jini")
                || p.startsWith("javax.") || p.equals("javax")
            );
    }
    public static boolean hasBeenPrefixed(String p){
        return p.startsWith(packagePrefix());
    }
    public static String withoutPackagePrefix(String p){
        if(hasBeenPrefixed(p)) return p.substring(packagePrefix().length());
        else return p;
    }
}
