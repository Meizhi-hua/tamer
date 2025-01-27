public class XEmbedHelper {
    private static final PlatformLogger xembedLog = PlatformLogger.getLogger("sun.awt.X11.xembed");
    final static Unsafe unsafe = Unsafe.getUnsafe();
    final static int XEMBED_VERSION = 0,
        XEMBED_MAPPED = (1 << 0);
    final static int XEMBED_EMBEDDED_NOTIFY     =       0;
    final static int XEMBED_WINDOW_ACTIVATE  =  1;
    final static int XEMBED_WINDOW_DEACTIVATE =         2;
    final static int XEMBED_REQUEST_FOCUS               =3;
    final static int XEMBED_FOCUS_IN    =       4;
    final static int XEMBED_FOCUS_OUT   =       5;
    final static int XEMBED_FOCUS_NEXT  =       6;
    final static int XEMBED_FOCUS_PREV  =       7;
    final static int XEMBED_GRAB_KEY = 8;
    final static int XEMBED_UNGRAB_KEY = 9;
    final static int XEMBED_MODALITY_ON         =       10;
    final static int XEMBED_MODALITY_OFF        =       11;
    final static int XEMBED_REGISTER_ACCELERATOR =    12;
    final static int XEMBED_UNREGISTER_ACCELERATOR=   13;
    final static int XEMBED_ACTIVATE_ACCELERATOR  =   14;
    final static int NON_STANDARD_XEMBED_GTK_GRAB_KEY = 108;
    final static int NON_STANDARD_XEMBED_GTK_UNGRAB_KEY = 109;
    final static int XEMBED_FOCUS_CURRENT       =       0;
    final static int XEMBED_FOCUS_FIRST         =       1;
    final static int XEMBED_FOCUS_LAST  =       2;
    final static int XEMBED_MODIFIER_SHIFT   = (1 << 0);
    final static int XEMBED_MODIFIER_CONTROL = (1 << 1);
    final static int XEMBED_MODIFIER_ALT     = (1 << 2);
    final static int XEMBED_MODIFIER_SUPER   = (1 << 3);
    final static int XEMBED_MODIFIER_HYPER   = (1 << 4);
    static XAtom XEmbedInfo;
    static XAtom XEmbed;
    XEmbedHelper() {
        if (XEmbed == null) {
            XEmbed = XAtom.get("_XEMBED");
            if (xembedLog.isLoggable(PlatformLogger.FINER)) xembedLog.finer("Created atom " + XEmbed.toString());
        }
        if (XEmbedInfo == null) {
            XEmbedInfo = XAtom.get("_XEMBED_INFO");
            if (xembedLog.isLoggable(PlatformLogger.FINER)) xembedLog.finer("Created atom " + XEmbedInfo.toString());
        }
    }
    void sendMessage(long window, int message) {
        sendMessage(window, message, 0, 0, 0);
    }
    void sendMessage(long window, int message, long detail, long data1, long data2) {
        XClientMessageEvent msg = new XClientMessageEvent();
        msg.set_type((int)XConstants.ClientMessage);
        msg.set_window(window);
        msg.set_message_type(XEmbed.getAtom());
        msg.set_format(32);
        msg.set_data(0, XToolkit.getCurrentServerTime());
        msg.set_data(1, message);
        msg.set_data(2, detail);
        msg.set_data(3, data1);
        msg.set_data(4, data2);
        XToolkit.awtLock();
        try {
            if (xembedLog.isLoggable(PlatformLogger.FINE)) xembedLog.fine("Sending " + XEmbedMessageToString(msg));
            XlibWrapper.XSendEvent(XToolkit.getDisplay(), window, false, XConstants.NoEventMask, msg.pData);
        }
        finally {
            XToolkit.awtUnlock();
        }
        msg.dispose();
    }
    static String msgidToString(int msg_id) {
        switch (msg_id) {
          case XEMBED_EMBEDDED_NOTIFY:
              return "XEMBED_EMBEDDED_NOTIFY";
          case XEMBED_WINDOW_ACTIVATE:
              return "XEMBED_WINDOW_ACTIVATE";
          case XEMBED_WINDOW_DEACTIVATE:
              return "XEMBED_WINDOW_DEACTIVATE";
          case XEMBED_FOCUS_IN:
              return "XEMBED_FOCUS_IN";
          case XEMBED_FOCUS_OUT:
              return "XEMBED_FOCUS_OUT";
          case XEMBED_REQUEST_FOCUS:
              return "XEMBED_REQUEST_FOCUS";
          case XEMBED_FOCUS_NEXT:
              return "XEMBED_FOCUS_NEXT";
          case XEMBED_FOCUS_PREV:
              return "XEMBED_FOCUS_PREV";
          case XEMBED_MODALITY_ON:
              return "XEMBED_MODALITY_ON";
          case XEMBED_MODALITY_OFF:
              return "XEMBED_MODALITY_OFF";
          case XEMBED_REGISTER_ACCELERATOR:
              return "XEMBED_REGISTER_ACCELERATOR";
          case XEMBED_UNREGISTER_ACCELERATOR:
              return "XEMBED_UNREGISTER_ACCELERATOR";
          case XEMBED_ACTIVATE_ACCELERATOR:
              return "XEMBED_ACTIVATE_ACCELERATOR";
          case XEMBED_GRAB_KEY:
              return "XEMBED_GRAB_KEY";
          case XEMBED_UNGRAB_KEY:
              return "XEMBED_UNGRAB_KEY";
          case NON_STANDARD_XEMBED_GTK_UNGRAB_KEY:
              return "NON_STANDARD_XEMBED_GTK_UNGRAB_KEY";
          case NON_STANDARD_XEMBED_GTK_GRAB_KEY:
              return "NON_STANDARD_XEMBED_GTK_GRAB_KEY";
          case XConstants.KeyPress | XEmbedServerTester.SYSTEM_EVENT_MASK:
              return "KeyPress";
          case XConstants.MapNotify | XEmbedServerTester.SYSTEM_EVENT_MASK:
              return "MapNotify";
          case XConstants.PropertyNotify | XEmbedServerTester.SYSTEM_EVENT_MASK:
              return "PropertyNotify";
          default:
              return "unknown XEMBED id " + msg_id;
        }
    }
    static String focusIdToString(int focus_id) {
        switch(focus_id) {
          case XEMBED_FOCUS_CURRENT:
              return "XEMBED_FOCUS_CURRENT";
          case XEMBED_FOCUS_FIRST:
              return "XEMBED_FOCUS_FIRST";
          case XEMBED_FOCUS_LAST:
              return "XEMBED_FOCUS_LAST";
          default:
              return "unknown focus id " + focus_id;
        }
    }
    static String XEmbedMessageToString(XClientMessageEvent msg) {
        return ("XEmbed message to " + Long.toHexString(msg.get_window()) + ": " + msgidToString((int)msg.get_data(1)) +
                ", detail: " + msg.get_data(2) +
                ", data:[" + msg.get_data(3) + "," + msg.get_data(4) + "]");
    }
    int getModifiers(int state) {
        int mods = 0;
        if ((state & XEMBED_MODIFIER_SHIFT) != 0) {
            mods |= InputEvent.SHIFT_DOWN_MASK;
        }
        if ((state & XEMBED_MODIFIER_CONTROL) != 0) {
            mods |= InputEvent.CTRL_DOWN_MASK;
        }
        if ((state & XEMBED_MODIFIER_ALT) != 0) {
            mods |= InputEvent.ALT_DOWN_MASK;
        }
        if ((state & XEMBED_MODIFIER_SUPER) != 0) {
            mods |= InputEvent.ALT_DOWN_MASK;
        }
        return mods;
    }
    AWTKeyStroke getKeyStrokeForKeySym(long keysym, long state) {
        XBaseWindow.checkSecurity();
        int keycode;
        XToolkit.awtLock();
        try {
            XKeysym.Keysym2JavaKeycode kc = XKeysym.getJavaKeycode( keysym );
            if(kc == null) {
                keycode = java.awt.event.KeyEvent.VK_UNDEFINED;
            }else{
                keycode = kc.getJavaKeycode();
            }
        } finally {
            XToolkit.awtUnlock();
        }
        int modifiers = getModifiers((int)state);
        return AWTKeyStroke.getAWTKeyStroke(keycode, modifiers);
    }
}
