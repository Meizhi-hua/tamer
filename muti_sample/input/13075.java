public class XQueryTree {
        private static Unsafe unsafe = XlibWrapper.unsafe;
        private boolean __executed = false;
        long _w;
        long root_ptr = unsafe.allocateMemory(Native.getLongSize());
        long parent_ptr = unsafe.allocateMemory(Native.getLongSize());
        long children_ptr = unsafe.allocateMemory(Native.getLongSize());
        long nchildren_ptr = unsafe.allocateMemory(Native.getIntSize());
    UnsafeXDisposerRecord disposer;
        public XQueryTree(
                long w  )
        {
                set_w(w);
                sun.java2d.Disposer.addRecord(this, disposer = new UnsafeXDisposerRecord("XQueryTree",
                                                                                         new long[]{root_ptr, parent_ptr, nchildren_ptr},
                                                                                         new long[] {children_ptr}));
                set_children(0);
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
                        if (__executed) {
                            throw new IllegalStateException("Already executed");
                        }
                        __executed = true;
                        if (errorHandler != null) {
                            XToolkit.WITH_XERROR_HANDLER(errorHandler);
                        }
                        Native.putLong(children_ptr, 0);
                        int status =
                        XlibWrapper.XQueryTree(XToolkit.getDisplay(),
                                get_w(),
                                root_ptr,
                                parent_ptr,
                                children_ptr,
                                nchildren_ptr                   );
                        if (errorHandler != null) {
                            XToolkit.RESTORE_XERROR_HANDLER();
                        }
                        return status;
                } finally {
                    XToolkit.awtUnlock();
                }
        }
        public boolean isExecuted() {
            return __executed;
        }
        public boolean isDisposed() {
            return disposer.disposed;
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
        public long get_w() {
                if (isDisposed()) {
                    throw new IllegalStateException("Disposed");
                }
                if (!__executed) {
                    throw new IllegalStateException("Not executed");
                }
                return _w;
        }
        public void set_w(long data) {
                _w = data;
        }
        public long get_root() {
                if (isDisposed()) {
                    throw new IllegalStateException("Disposed");
                }
                if (!__executed) {
                    throw new IllegalStateException("Not executed");
                }
                return Native.getLong(root_ptr);
        }
        public void set_root(long data) {
                Native.putLong(root_ptr, data);
        }
        public long get_parent() {
                if (isDisposed()) {
                    throw new IllegalStateException("Disposed");
                }
                if (!__executed) {
                    throw new IllegalStateException("Not executed");
                }
                return Native.getLong(parent_ptr);
        }
        public void set_parent(long data) {
                Native.putLong(parent_ptr, data);
        }
        public long get_children() {
                if (isDisposed()) {
                    throw new IllegalStateException("Disposed");
                }
                if (!__executed) {
                    throw new IllegalStateException("Not executed");
                }
                return Native.getLong(children_ptr);
        }
        public void set_children(long data) {
                Native.putLong(children_ptr, data);
        }
        public int get_nchildren() {
                if (isDisposed()) {
                    throw new IllegalStateException("Disposed");
                }
                if (!__executed) {
                    throw new IllegalStateException("Not executed");
                }
                return Native.getInt(nchildren_ptr);
        }
        public void set_nchildren(int data) {
                Native.putInt(nchildren_ptr, data);
        }
}
