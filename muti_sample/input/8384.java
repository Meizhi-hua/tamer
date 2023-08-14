public class WindowPropertyGetter {
    private static Unsafe unsafe = XlibWrapper.unsafe;
    private final long actual_type = unsafe.allocateMemory(8);
    private final long actual_format = unsafe.allocateMemory(4);
    private final long nitems_ptr = unsafe.allocateMemory(8);
    private final long bytes_after = unsafe.allocateMemory(8);
    private final long data = unsafe.allocateMemory(8);
    private final long window;
    private final XAtom property;
    private final long offset;
    private final long length;
    private final boolean auto_delete;
    private final long type;
    private boolean executed = false;
    public WindowPropertyGetter(long window, XAtom property, long offset,
                                long length, boolean auto_delete, long type)
    {
        if (property.getAtom() == 0) {
            throw new IllegalArgumentException("Property ATOM should be initialized first:" + property);
        }
        if (window == 0) {
            throw new IllegalArgumentException("Window must not be zero");
        }
        this.window = window;
        this.property = property;
        this.offset = offset;
        this.length = length;
        this.auto_delete = auto_delete;
        this.type = type;
        Native.putLong(data, 0);
        sun.java2d.Disposer.addRecord(this, disposer = new UnsafeXDisposerRecord("WindowPropertyGetter", new long[] {actual_type,
                                                                                 actual_format, nitems_ptr, bytes_after}, new long[] {data}));
    }
    UnsafeXDisposerRecord disposer;
    public WindowPropertyGetter(long window, XAtom property, long offset,
                                long length, boolean auto_delete, XAtom type)
    {
        this(window, property, offset, length, auto_delete, type.getAtom());
    }
    public int execute() {
        return execute(null);
    }
    public int execute(XErrorHandler errorHandler) {
        XToolkit.awtLock();
        try {
            if (isDisposed()) {
                throw new IllegalStateException("Disposed");
            }
            if (executed) {
                throw new IllegalStateException("Already executed");
            }
            executed = true;
            if (isCachingSupported() && isCached()) {
                readFromCache();
                return XConstants.Success;
            }
            if (errorHandler instanceof XErrorHandler.IgnoreBadWindowHandler) {
                errorHandler = null;
            }
            if (errorHandler != null) {
                XToolkit.WITH_XERROR_HANDLER(errorHandler);
            }
            Native.putLong(data, 0);
            int status = XlibWrapper.XGetWindowProperty(XToolkit.getDisplay(), window, property.getAtom(),
                                                        offset, length, (auto_delete?1:0), type,
                                                        actual_type, actual_format, nitems_ptr,
                                                        bytes_after, data);
            if (isCachingSupported() &&  status == XConstants.Success && getData() != 0 && isCacheableProperty(property)) {
                cacheProperty();
            }
            if (errorHandler != null) {
                XToolkit.RESTORE_XERROR_HANDLER();
            }
            return status;
        } finally {
            XToolkit.awtUnlock();
        }
    }
    public boolean isExecuted() {
        return executed;
    }
    public boolean isDisposed() {
        return disposer.disposed;
    }
    public int getActualFormat() {
        if (isDisposed()) {
            throw new IllegalStateException("Disposed");
        }
        if (!executed) {
            throw new IllegalStateException("Not executed");
        }
        return unsafe.getInt(actual_format);
    }
    public long getActualType() {
        if (isDisposed()) {
            throw new IllegalStateException("Disposed");
        }
        if (!executed) {
            throw new IllegalStateException("Not executed");
        }
        return XAtom.getAtom(actual_type);
    }
    public int getNumberOfItems() {
        if (isDisposed()) {
            throw new IllegalStateException("Disposed");
        }
        if (!executed) {
            throw new IllegalStateException("Not executed");
        }
        return (int)Native.getLong(nitems_ptr);
    }
    public long getData() {
        if (isDisposed()) {
            throw new IllegalStateException("Disposed");
        }
        return Native.getLong(data);
    }
    public long getBytesAfter() {
        if (isDisposed()) {
            throw new IllegalStateException("Disposed");
        }
        if (!executed) {
            throw new IllegalStateException("Not executed");
        }
        return Native.getLong(bytes_after);
    }
    public void dispose() {
        XToolkit.awtLock();
        try {
            if (isDisposed()) {
                return;
            }
            disposer.dispose();
        } finally {
            XToolkit.awtUnlock();
        }
    }
    static boolean isCachingSupported() {
        return XPropertyCache.isCachingSupported();
    }
    static Set<XAtom> cacheableProperties = new HashSet<XAtom>(Arrays.asList(new XAtom[] {
            XAtom.get("_NET_WM_STATE"), XAtom.get("WM_STATE"), XAtom.get("_MOTIF_WM_HINTS")}));
    static boolean isCacheableProperty(XAtom property) {
        return cacheableProperties.contains(property);
    }
    boolean isCached() {
        return XPropertyCache.isCached(window, property);
    }
    int getDataLength() {
        return getActualFormat() / 8 * getNumberOfItems();
    }
    void readFromCache() {
        property.putAtom(actual_type);
        XPropertyCache.PropertyCacheEntry entry = XPropertyCache.getCacheEntry(window, property);
        Native.putInt(actual_format, entry.getFormat());
        Native.putLong(nitems_ptr, entry.getNumberOfItems());
        Native.putLong(bytes_after, entry.getBytesAfter());
        Native.putLong(data, unsafe.allocateMemory(getDataLength()));
        XlibWrapper.memcpy(getData(), entry.getData(), getDataLength());
    }
    void cacheProperty() {
        XPropertyCache.storeCache(
            new XPropertyCache.PropertyCacheEntry(getActualFormat(),
                                                  getNumberOfItems(),
                                                  getBytesAfter(),
                                                  getData(),
                                                  getDataLength()),
            window,
            property);
    }
}