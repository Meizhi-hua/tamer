final public class XlibWrapper
{
    static Unsafe unsafe = Unsafe.getUnsafe();
    static final int MAXSIZE = 32767;
    static final int MINSIZE = 1;
    private XlibWrapper()
    {
    }
    public final static String eventToString[]=
    {"<none:0>", "<none:1>", "KeyPress", "KeyRelease", "ButtonPress", "ButtonRelease",
     "MotionNotify", "EnterNotify", "LeaveNotify", "FocusIn", "FocusOut",
     "KeymapNotify", "Expose", "GraphicsExpose", "NoExpose", "VisibilityNotify",
     "CreateNotify", "DestroyNotify", "UnmapNotify", "MapNotify", "MapRequest",
     "ReparentNotify", "ConfigureNotify", "ConfigureRequest", "GravityNotify",
     "ResizeRequest", "CirculateNotify", "CirculateRequest", "PropertyNotify",
     "SelectionClear", "SelectionRequest", "SelectionNotify", "ColormapNotify",
     "ClientMessage", "MappingNotify", "LASTEvent"};
    static native void XFree(long ptr);
    static native void memcpy(long dest_ptr, long src_ptr, long length);
    static native long getAddress(Object o);
    static native void copyIntArray(long dest_ptr, Object array, int size_bytes);
    static native void copyLongArray(long dest_ptr, Object array, int size_bytes);
    static native byte[] getStringBytes(long str_ptr);
    static  native long XOpenDisplay(long display);
    static  native void XCloseDisplay(long display);
    static  native long XDisplayString(long display);
    static  native void XSetCloseDownMode(long display, int close_mode);
    static  native long DefaultScreen(long display);
    static native long ScreenOfDisplay(long display, long screen_number);
    static native int DoesBackingStore(long screen);
    static native  long DisplayWidth(long display, long screen);
    static native  long DisplayWidthMM(long display, long screen);
    static native long DisplayHeight(long display, long screen);
    static native long DisplayHeightMM(long display, long screen);
    static  native long RootWindow(long display, long screen_number);
    static native int ScreenCount(long display);
    static native long XCreateWindow(long display, long parent, int x,int  y, int width, int height, int border_width, int depth, long wclass, long visual, long valuemask, long attributes);
    static native void XDestroyWindow(long display, long window);
    static native int XGrabPointer(long display, long grab_window,
                                   int owner_events, int event_mask, int pointer_mode,
                                   int keyboard_mode, long confine_to, long cursor, long time);
    static native void XUngrabPointer(long display, long time);
    static native int XGrabKeyboard(long display, long grab_window,
                                    int owner_events, int pointer_mode,
                                    int keyboard_mode, long time);
    static native void XUngrabKeyboard(long display, long time);
    static native void XGrabServer(long display);
    static native void XUngrabServer(long display);
    static  native void XMapWindow(long display, long window);
    static  native void XMapRaised(long display, long window);
    static  native void XRaiseWindow(long display, long window);
    static native void XLowerWindow(long display, long window);
    static native void XRestackWindows(long display, long windows, int length);
    static native void XConfigureWindow(long display, long window,
            long value_mask, long values);
    static native void XSetInputFocus(long display, long window);
    static native void XSetInputFocus2(long display, long window, long time);
    static native long XGetInputFocus(long display);
    static  native void XUnmapWindow(long display, long window);
    static  native void XSelectInput(long display, long window, long event_mask);
    static  native void XNextEvent(long display,long ptr);
    static native void XMaskEvent(long display, long event_mask, long event_return);
    static native void XWindowEvent(long display, long window, long event_mask, long event_return);
    static native boolean XFilterEvent(long ptr, long window);
static native boolean XSupportsLocale();
static native String XSetLocaleModifiers(String modifier_list);
    static  native int XTranslateCoordinates(
                                             long display, long src_w, long dest_w,
                                             long src_x, long src_y,
                                             long dest_x_return, long dest_y_return,
                                             long child_return);
    static  native void XPeekEvent(long display,long ptr);
    static native void XFlush(long display);
    static native void XSync(long display,int discard);
    static native void XMoveResizeWindow(long display, long window, int x, int y, int width, int height);
    static native void XResizeWindow(long display, long window, int width, int height);
    static native void XMoveWindow(long display, long window, int x, int y);
 static native boolean  XQueryPointer (long display, long window, long root_return, long child_return, long root_x_return, long root_y_return, long win_x_return, long win_y_return, long mask_return);
 static native void XFreeCursor(long display, long cursor);
    static native void XSetWindowBackground(long display, long window, long background_pixel);
    static native int XEventsQueued(long display, int mode);
    static native int XInternAtoms(long display, String[] names, boolean only_if_exists, long atoms);
    static native void SetProperty(long display, long window, long atom, String str);
    static native String GetProperty(long display ,long window, long atom);
    static native long InternAtom(long display, String string, int only_if_exists);
    static native int XGetWindowProperty(long display, long window, long atom,
                                         long long_offset, long long_length,
                                         long delete, long req_type, long actualy_type,
                                         long actualy_format, long nitems_ptr,
                                         long bytes_after, long data_ptr);
    native static void XChangePropertyImpl(long display, long window, long atom,
                                           long type, int format, int mode, long data,
                                           int nelements);
    static void XChangeProperty(long display, long window, long atom,
                                long type, int format, int mode, long data,
                                int nelements) {
        if (XPropertyCache.isCachingSupported() &&
            XToolkit.windowToXWindow(window) != null &&
            WindowPropertyGetter.isCacheableProperty(XAtom.get(atom)) &&
            mode == XConstants.PropModeReplace)
        {
            int length = (format / 8) * nelements;
            XPropertyCache.storeCache(
                new XPropertyCache.PropertyCacheEntry(format,
                                                      nelements,
                                                      0,
                                                      data,
                                                      length),
                window,
                XAtom.get(atom));
        }
        XChangePropertyImpl(display, window, atom, type, format, mode, data, nelements);
    }
    static native void XChangePropertyS(long display, long window, long atom,
                                       long type, int format, int mode, String value);
    static native void XDeleteProperty(long display, long window, long atom);
    static native void XSetTransientFor(long display, long window, long transient_for_window);
    static native void XSetWMHints(long display, long window, long wmhints);
    static native void XGetWMHints(long display, long window, long wmhints);
    static native long XAllocWMHints();
    static native int XGetPointerMapping(long display, long map, int buttonNumber);
    static native String XGetDefault(long display, String program, String option);
    static native long getScreenOfWindow(long display, long window);
    static native long XScreenNumberOfScreen(long screen);
    static native int XIconifyWindow(long display, long window, long screenNumber);
    static native String ServerVendor(long display);
    static native int VendorRelease(long display);
    static native boolean IsXsunKPBehavior(long display);
    static native boolean IsSunKeyboard(long display);
    static native boolean IsKanaKeyboard(long display);
    static native void XBell(long display, int percent);
    static native int XCreateFontCursor(long display, int shape);
    static native long XCreateBitmapFromData(long display, long drawable, long data, int width, int height);
   static native void XFreePixmap(long display, long pixmap);
   static native long XCreatePixmapCursor(long display, long source, long mask, long fore, long back, int x, int y);
    static native boolean XQueryBestCursor(long display, long drawable, int width, int height, long width_return, long height_return);
    static native boolean XAllocColor( long display, long colormap, long screen_in_out);
    static native long SetToolkitErrorHandler();
    static native void XSetErrorHandler(long handler);
    static native int CallErrorHandler(long handler, long display, long event_ptr);
    static native void XChangeWindowAttributes(long display, long window, long valuemask, long attributes);
    static native int XGetWindowAttributes(long display, long window, long attr_ptr);
    static native int XGetGeometry(long display, long drawable, long root_return, long x_return, long y_return,
                                   long width_return, long height_return, long border_width_return, long depth_return);
    static native int XGetWMNormalHints(long display, long window, long hints, long supplied_return);
    static native void XSetWMNormalHints(long display, long window, long hints);
    static native void XSetMinMaxHints(long display, long window, int x, int y, int width, int height, long flags);
    static native long XAllocSizeHints();
    static native int XSendEvent(long display, long window, boolean propagate, long event_mask, long event);
    static native void XPutBackEvent(long display, long event);
    static native int XQueryTree(long display, long window, long root_return, long parent_return, long children_return, long nchildren_return);
    static native long XGetVisualInfo(long display, long vinfo_mask, long vinfo_template, long nitems_return);
    static native void XReparentWindow(long display, long window, long parent, int x, int y);
    static native void XConvertSelection(long display, long selection,
                                         long target, long property,
                                         long requestor, long time);
    static native void XSetSelectionOwner(long display, long selection,
                                          long owner, long time);
    static native long XGetSelectionOwner(long display, long selection);
    static native String XGetAtomName(long display, long atom);
    static native long XMaxRequestSize(long display);
    static native long XCreatePixmap(long display, long drawable, int width, int height, int depth);
    static native long XCreateImage(long display, long visual_ptr, int depth, int format,
                                    int offset, long data, int width, int height, int bitmap_pad,
                                    int bytes_per_line);
    static native void XDestroyImage(long image);
    static native void XPutImage(long display, long drawable, long gc, long image,
                                 int src_x, int src_y, int dest_x, int dest_y,
                                 int width, int height);
    static native long XCreateGC(long display, long drawable, long valuemask, long values);
    static native void XFreeGC(long display, long gc);
    static native void XSetWindowBackgroundPixmap(long display, long window, long pixmap);
    static native void XClearWindow(long display, long window);
    static native int XGetIconSizes(long display, long window, long ret_sizes, long ret_count);
    static native int XdbeQueryExtension(long display, long major_version_return,
                                         long minor_version_return);
    static native boolean XQueryExtension(long display, String name, long mop_return,
                                         long feve_return, long err_return);
    static native boolean IsKeypadKey(long keysym);
    static native long XdbeAllocateBackBufferName(long display, long window, int swap_action);
    static native int XdbeDeallocateBackBufferName(long display, long buffer);
    static native int XdbeBeginIdiom(long display);
    static native int XdbeEndIdiom(long display);
    static native int XdbeSwapBuffers(long display, long swap_info, int num_windows);
    static native void XQueryKeymap(long display, long vector);
    static native long XKeycodeToKeysym(long display, int keycode, int index);
    static native int XKeysymToKeycode(long display, long keysym);
    static native int XkbGetEffectiveGroup(long display);
    static native long XkbKeycodeToKeysym(long display, int keycode, int group, int level);
    static native void XkbSelectEvents(long display, long device, long bits_to_change, long values_for_bits);
    static native void XkbSelectEventDetails(long display, long device, long event_type,
                                              long bits_to_change, long values_for_bits);
    static native boolean XkbQueryExtension(long display, long opcode_rtrn, long event_rtrn,
              long error_rtrn, long major_in_out, long minor_in_out);
    static native boolean XkbLibraryVersion(long lib_major_in_out, long lib_minor_in_out);
    static native long XkbGetMap(long display, long which, long device_spec);
    static native long XkbGetUpdatedMap(long display, long which, long xkb);
    static native void XkbFreeKeyboard(long xkb, long which, boolean free_all);
    static native boolean XkbTranslateKeyCode(long xkb, int keycode, long mods, long mods_rtrn, long keysym_rtrn);
    static native void XConvertCase(long keysym,
                                    long keysym_lowercase,
                                    long keysym_uppercase);
    static native long XGetModifierMapping(long display);
    static native void XFreeModifiermap(long keymap);
    static native void XRefreshKeyboardMapping(long event);
    static native void XChangeActivePointerGrab(long display, int mask,
                                                long cursor, long time);
    public static native int XSynchronize(long display, boolean onoff);
    static native boolean XNextSecondaryLoopEvent(long display, long ptr);
    static native void ExitSecondaryLoop();
    static native String[] XTextPropertyToStringList(byte[] bytes, long encoding_atom);
    static native boolean XShapeQueryExtension(long display, long event_base_return, long error_base_return);
    static native void SetRectangularShape(long display, long window,
            int lox, int loy, int hix, int hiy,
            sun.java2d.pipe.Region region);
    static native void SetBitmapShape(long display, long window,
             int width, int height, int[] bitmap);
    static native void SetZOrder(long display, long window, long above);
    final static long lbuffer = unsafe.allocateMemory(64);  
    final static long ibuffer = unsafe.allocateMemory(32);  
    static final long larg1 = lbuffer;
    static final long larg2 = larg1+8;
    static final long larg3 = larg2+8;
    static final long larg4 = larg3+8;
    static final long larg5 = larg4+8;
    static final long larg6 = larg5+8;
    static final long larg7 = larg6+8;
    static final long larg8 = larg7+8;
    static final long iarg1 = ibuffer;
    static final long iarg2 = iarg1+4;
    static final long iarg3 = iarg2+4;
    static final long iarg4 = iarg3+4;
    static final long iarg5 = iarg4+4;
    static final long iarg6 = iarg5+4;
    static final long iarg7 = iarg6+4;
    static final long iarg8 = iarg7+4;
    static int dataModel;
    static final boolean isBuildInternal;
    static {
        String dataModelProp = (String)AccessController.doPrivileged(
            new PrivilegedAction() {
                    public Object run() {
                        return System.getProperty("sun.arch.data.model");
                    }
                });
        try {
            dataModel = Integer.parseInt(dataModelProp);
        } catch (Exception e) {
            dataModel = 32;
        }
        isBuildInternal = getBuildInternal();
    }
    static int getDataModel() {
        return dataModel;
    }
    static String hintsToString(long flags) {
        StringBuffer buf = new StringBuffer();
        if ((flags & XUtilConstants.PMaxSize) != 0) {
            buf.append("PMaxSize ");
        }
        if ((flags & XUtilConstants.PMinSize) != 0) {
            buf.append("PMinSize ");
        }
        if ((flags & XUtilConstants.USSize) != 0) {
            buf.append("USSize ");
        }
        if ((flags & XUtilConstants.USPosition) != 0) {
            buf.append("USPosition ");
        }
        if ((flags & XUtilConstants.PPosition) != 0) {
            buf.append("PPosition ");
        }
        if ((flags & XUtilConstants.PSize) != 0) {
            buf.append("PSize ");
        }
        if ((flags & XUtilConstants.PWinGravity) != 0) {
            buf.append("PWinGravity ");
        }
        return buf.toString();
    }
    static String getEventToString( int type ) {
        if( (type >= 0) && (type < eventToString.length)) {
            return eventToString[type];
        }else if( type == XToolkit.getXKBBaseEventCode() ) {
            return "XkbEvent";
        }
        return eventToString[0];
    }
    private static boolean getBuildInternal() {
        String javaVersion = XToolkit.getSystemProperty("java.version");
        return javaVersion != null && javaVersion.contains("internal");
    }
    static native void PrintXErrorEvent(long display, long event_ptr);
}
