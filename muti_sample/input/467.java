class WindowsFileAttributeViews {
    private static class Basic extends AbstractBasicFileAttributeView {
        final WindowsPath file;
        final boolean followLinks;
        Basic(WindowsPath file, boolean followLinks) {
            this.file = file;
            this.followLinks = followLinks;
        }
        @Override
        public WindowsFileAttributes readAttributes() throws IOException {
            file.checkRead();
            try {
                return WindowsFileAttributes.get(file, followLinks);
            } catch (WindowsException x) {
                x.rethrowAsIOException(file);
                return null;    
            }
        }
        private long adjustForFatEpoch(long time) {
            final long FAT_EPOCH = 119600064000000000L;
            if (time != -1L && time < FAT_EPOCH) {
                return FAT_EPOCH;
            } else {
                return time;
            }
        }
        void setFileTimes(long createTime,
                          long lastAccessTime,
                          long lastWriteTime)
            throws IOException
        {
            long handle = -1L;
            try {
                int flags = FILE_FLAG_BACKUP_SEMANTICS;
                if (!followLinks && file.getFileSystem().supportsLinks())
                    flags |= FILE_FLAG_OPEN_REPARSE_POINT;
                handle = CreateFile(file.getPathForWin32Calls(),
                                    FILE_WRITE_ATTRIBUTES,
                                    (FILE_SHARE_READ | FILE_SHARE_WRITE | FILE_SHARE_DELETE),
                                    OPEN_EXISTING,
                                    flags);
            } catch (WindowsException x) {
                x.rethrowAsIOException(file);
            }
            try {
                SetFileTime(handle,
                            createTime,
                            lastAccessTime,
                            lastWriteTime);
            } catch (WindowsException x) {
                if (followLinks && x.lastError() == ERROR_INVALID_PARAMATER) {
                    try {
                        if (WindowsFileStore.create(file).type().equals("FAT")) {
                            SetFileTime(handle,
                                        adjustForFatEpoch(createTime),
                                        adjustForFatEpoch(lastAccessTime),
                                        adjustForFatEpoch(lastWriteTime));
                            x = null;
                        }
                    } catch (SecurityException ignore) {
                    } catch (WindowsException ignore) {
                    } catch (IOException ignore) {
                    }
                }
                if (x != null)
                    x.rethrowAsIOException(file);
            } finally {
                CloseHandle(handle);
            }
        }
        @Override
        public void setTimes(FileTime lastModifiedTime,
                             FileTime lastAccessTime,
                             FileTime createTime) throws IOException
        {
            if (lastModifiedTime == null && lastAccessTime == null &&
                createTime == null)
            {
                return;
            }
            file.checkWrite();
            long t1 = (createTime == null) ? -1L :
                WindowsFileAttributes.toWindowsTime(createTime);
            long t2 = (lastAccessTime == null) ? -1L :
                WindowsFileAttributes.toWindowsTime(lastAccessTime);
            long t3 = (lastModifiedTime == null) ? -1L :
                WindowsFileAttributes.toWindowsTime(lastModifiedTime);
            setFileTimes(t1, t2, t3);
        }
    }
    static class Dos extends Basic implements DosFileAttributeView {
        private static final String READONLY_NAME = "readonly";
        private static final String ARCHIVE_NAME = "archive";
        private static final String SYSTEM_NAME = "system";
        private static final String HIDDEN_NAME = "hidden";
        private static final String ATTRIBUTES_NAME = "attributes";
        static final Set<String> dosAttributeNames =
            Util.newSet(basicAttributeNames,
                        READONLY_NAME, ARCHIVE_NAME, SYSTEM_NAME,  HIDDEN_NAME, ATTRIBUTES_NAME);
        Dos(WindowsPath file, boolean followLinks) {
            super(file, followLinks);
        }
        @Override
        public String name() {
            return "dos";
        }
        @Override
        public void setAttribute(String attribute, Object value)
            throws IOException
        {
            if (attribute.equals(READONLY_NAME)) {
                setReadOnly((Boolean)value);
                return;
            }
            if (attribute.equals(ARCHIVE_NAME)) {
                setArchive((Boolean)value);
                return;
            }
            if (attribute.equals(SYSTEM_NAME)) {
                setSystem((Boolean)value);
                return;
            }
            if (attribute.equals(HIDDEN_NAME)) {
                setHidden((Boolean)value);
                return;
            }
            super.setAttribute(attribute, value);
        }
        @Override
        public Map<String,Object> readAttributes(String[] attributes)
            throws IOException
        {
            AttributesBuilder builder =
                AttributesBuilder.create(dosAttributeNames, attributes);
            WindowsFileAttributes attrs = readAttributes();
            addRequestedBasicAttributes(attrs, builder);
            if (builder.match(READONLY_NAME))
                builder.add(READONLY_NAME, attrs.isReadOnly());
            if (builder.match(ARCHIVE_NAME))
                builder.add(ARCHIVE_NAME, attrs.isArchive());
            if (builder.match(SYSTEM_NAME))
                builder.add(SYSTEM_NAME, attrs.isSystem());
            if (builder.match(HIDDEN_NAME))
                builder.add(HIDDEN_NAME, attrs.isHidden());
            if (builder.match(ATTRIBUTES_NAME))
                builder.add(ATTRIBUTES_NAME, attrs.attributes());
            return builder.unmodifiableMap();
        }
        private void updateAttributes(int flag, boolean enable)
            throws IOException
        {
            file.checkWrite();
            String path = WindowsLinkSupport.getFinalPath(file, followLinks);
            try {
                int oldValue = GetFileAttributes(path);
                int newValue = oldValue;
                if (enable) {
                    newValue |= flag;
                } else {
                    newValue &= ~flag;
                }
                if (newValue != oldValue) {
                    SetFileAttributes(path, newValue);
                }
            } catch (WindowsException x) {
                x.rethrowAsIOException(file);
            }
        }
        @Override
        public void setReadOnly(boolean value) throws IOException {
            updateAttributes(FILE_ATTRIBUTE_READONLY, value);
        }
        @Override
        public void setHidden(boolean value) throws IOException {
            updateAttributes(FILE_ATTRIBUTE_HIDDEN, value);
        }
        @Override
        public void setArchive(boolean value) throws IOException {
            updateAttributes(FILE_ATTRIBUTE_ARCHIVE, value);
        }
        @Override
        public void setSystem(boolean value) throws IOException {
            updateAttributes(FILE_ATTRIBUTE_SYSTEM, value);
        }
        void setAttributes(WindowsFileAttributes attrs)
            throws IOException
        {
            int flags = 0;
            if (attrs.isReadOnly()) flags |= FILE_ATTRIBUTE_READONLY;
            if (attrs.isHidden()) flags |= FILE_ATTRIBUTE_HIDDEN;
            if (attrs.isArchive()) flags |= FILE_ATTRIBUTE_ARCHIVE;
            if (attrs.isSystem()) flags |= FILE_ATTRIBUTE_SYSTEM;
            updateAttributes(flags, true);
            setFileTimes(
                WindowsFileAttributes.toWindowsTime(attrs.creationTime()),
                WindowsFileAttributes.toWindowsTime(attrs.lastModifiedTime()),
                WindowsFileAttributes.toWindowsTime(attrs.lastAccessTime()));
        }
    }
    static Basic createBasicView(WindowsPath file, boolean followLinks) {
        return new Basic(file, followLinks);
    }
    static Dos createDosView(WindowsPath file, boolean followLinks) {
        return new Dos(file, followLinks);
    }
}
