public final class OutputPropertyUtils
{
    public static boolean getBooleanProperty(String key, Properties props)
    {
        String s = props.getProperty(key);
        if (null == s || !s.equals("yes"))
            return false;
        else
            return true;
    }
    public static int getIntProperty(String key, Properties props)
    {
        String s = props.getProperty(key);
        if (null == s)
            return 0;
        else
            return Integer.parseInt(s);
    }
}
