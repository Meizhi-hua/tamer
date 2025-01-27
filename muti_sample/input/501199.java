public class XPATHMessages extends XMLMessages
{
  private static ListResourceBundle XPATHBundle = new XPATHErrorResources();
  private static final String XPATH_ERROR_RESOURCES =
    "org.apache.xpath.res.XPATHErrorResources";
  public static final String createXPATHMessage(String msgKey, Object args[])  
  {
      return createXPATHMsg(XPATHBundle, msgKey, args);
  }
  public static final String createXPATHWarning(String msgKey, Object args[])  
  {
      return createXPATHMsg(XPATHBundle, msgKey, args);
  }
  public static final String createXPATHMsg(ListResourceBundle fResourceBundle,
                                            String msgKey, Object args[])  
  {
    String fmsg = null;
    boolean throwex = false;
    String msg = null;
    if (msgKey != null)
      msg = fResourceBundle.getString(msgKey); 
    if (msg == null)
    {
      msg = fResourceBundle.getString(XPATHErrorResources.BAD_CODE);
      throwex = true;
    }
    if (args != null)
    {
      try
      {
        int n = args.length;
        for (int i = 0; i < n; i++)
        {
          if (null == args[i])
            args[i] = "";
        }
        fmsg = java.text.MessageFormat.format(msg, args);
      }
      catch (Exception e)
      {
        fmsg = fResourceBundle.getString(XPATHErrorResources.FORMAT_FAILED);
        fmsg += " " + msg;
      }
    }
    else
      fmsg = msg;
    if (throwex)
    {
      throw new RuntimeException(fmsg);
    }
    return fmsg;
  }
}
