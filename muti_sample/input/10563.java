class WindowsFileSystem
    extends FileSystem
{
    private final WindowsFileSystemProvider provider;
    private final String defaultDirectory;
    private final String defaultRoot;
    private final boolean supportsLinks;
    private final boolean supportsStreamEnumeration;
    WindowsFileSystem(WindowsFileSystemProvider provider,
                      String dir)
    {
        this.provider = provider;
        WindowsPathParser.Result result = WindowsPathParser.parse(dir);
        if ((result.type() != WindowsPathType.ABSOLUTE) &&
            (result.type() != WindowsPathType.UNC))
            throw new AssertionError("Default directory is not an absolute path");
        this.defaultDirectory = result.path();
        this.defaultRoot = result.root();
        PrivilegedAction<String> pa = new GetPropertyAction("os.version");
        String osversion = AccessController.doPrivileged(pa);
        String[] vers = Util.split(osversion, '.');
        int major = Integer.parseInt(vers[0]);
        int minor = Integer.parseInt(vers[1]);
        supportsLinks = (major >= 6);
        supportsStreamEnumeration = (major >= 6) || (major == 5 && minor >= 2);
    }
    String defaultDirectory() {
        return defaultDirectory;
    }
    String defaultRoot() {
        return defaultRoot;
    }
    boolean supportsLinks() {
        return supportsLinks;
    }
    boolean supportsStreamEnumeration() {
        return supportsStreamEnumeration;
    }
    @Override
    public FileSystemProvider provider() {
        return provider;
    }
    @Override
    public String getSeparator() {
        return "\\";
    }
    @Override
    public boolean isOpen() {
        return true;
    }
    @Override
    public boolean isReadOnly() {
        return false;
    }
    @Override
    public void close() throws IOException {
        throw new UnsupportedOperationException();
    }
    @Override
    public Iterable<Path> getRootDirectories() {
        int drives = 0;
        try {
            drives = WindowsNativeDispatcher.GetLogicalDrives();
        } catch (WindowsException x) {
            throw new AssertionError(x.getMessage());
        }
        ArrayList<Path> result = new ArrayList<>();
        SecurityManager sm = System.getSecurityManager();
        for (int i = 0; i <= 25; i++) {  
            if ((drives & (1 << i)) != 0) {
                StringBuilder sb = new StringBuilder(3);
                sb.append((char)('A' + i));
                sb.append(":\\");
                String root = sb.toString();
                if (sm != null) {
                    try {
                        sm.checkRead(root);
                    } catch (SecurityException x) {
                        continue;
                    }
                }
                result.add(WindowsPath.createFromNormalizedPath(this, root));
            }
        }
        return Collections.unmodifiableList(result);
    }
    private class FileStoreIterator implements Iterator<FileStore> {
        private final Iterator<Path> roots;
        private FileStore next;
        FileStoreIterator() {
            this.roots = getRootDirectories().iterator();
        }
        private FileStore readNext() {
            assert Thread.holdsLock(this);
            for (;;) {
                if (!roots.hasNext())
                    return null;
                WindowsPath root = (WindowsPath)roots.next();
                try {
                    root.checkRead();
                } catch (SecurityException x) {
                    continue;
                }
                try {
                    FileStore fs = WindowsFileStore.create(root.toString(), true);
                    if (fs != null)
                        return fs;
                } catch (IOException ioe) {
                }
            }
        }
        @Override
        public synchronized boolean hasNext() {
            if (next != null)
                return true;
            next = readNext();
            return next != null;
        }
        @Override
        public synchronized FileStore next() {
            if (next == null)
                next = readNext();
            if (next == null) {
                throw new NoSuchElementException();
            } else {
                FileStore result = next;
                next = null;
                return result;
            }
        }
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
    @Override
    public Iterable<FileStore> getFileStores() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            try {
                sm.checkPermission(new RuntimePermission("getFileStoreAttributes"));
            } catch (SecurityException se) {
                return Collections.emptyList();
            }
        }
        return new Iterable<FileStore>() {
            public Iterator<FileStore> iterator() {
                return new FileStoreIterator();
            }
        };
    }
    private static final Set<String> supportedFileAttributeViews = Collections
        .unmodifiableSet(new HashSet<String>(Arrays.asList("basic", "dos", "acl", "owner", "user")));
    @Override
    public Set<String> supportedFileAttributeViews() {
        return supportedFileAttributeViews;
    }
    @Override
    public final Path getPath(String first, String... more) {
        String path;
        if (more.length == 0) {
            path = first;
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(first);
            for (String segment: more) {
                if (segment.length() > 0) {
                    if (sb.length() > 0)
                        sb.append('\\');
                    sb.append(segment);
                }
            }
            path = sb.toString();
        }
        return WindowsPath.parse(this, path);
    }
    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService() {
        return LookupService.instance;
    }
    private static class LookupService {
        static final UserPrincipalLookupService instance =
            new UserPrincipalLookupService() {
                @Override
                public UserPrincipal lookupPrincipalByName(String name)
                    throws IOException
                {
                    return WindowsUserPrincipals.lookup(name);
                }
                @Override
                public GroupPrincipal lookupPrincipalByGroupName(String group)
                    throws IOException
                {
                    UserPrincipal user = WindowsUserPrincipals.lookup(group);
                    if (!(user instanceof GroupPrincipal))
                        throw new UserPrincipalNotFoundException(group);
                    return (GroupPrincipal)user;
                }
            };
    }
    @Override
    public PathMatcher getPathMatcher(String syntaxAndInput) {
        int pos = syntaxAndInput.indexOf(':');
        if (pos <= 0 || pos == syntaxAndInput.length())
            throw new IllegalArgumentException();
        String syntax = syntaxAndInput.substring(0, pos);
        String input = syntaxAndInput.substring(pos+1);
        String expr;
        if (syntax.equals(GLOB_SYNTAX)) {
            expr = Globs.toWindowsRegexPattern(input);
        } else {
            if (syntax.equals(REGEX_SYNTAX)) {
                expr = input;
            } else {
                throw new UnsupportedOperationException("Syntax '" + syntax +
                    "' not recognized");
            }
        }
        final Pattern pattern = Pattern.compile(expr,
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        return new PathMatcher() {
            @Override
            public boolean matches(Path path) {
                return pattern.matcher(path.toString()).matches();
            }
        };
    }
    private static final String GLOB_SYNTAX = "glob";
    private static final String REGEX_SYNTAX = "regex";
    @Override
    public WatchService newWatchService()
        throws IOException
    {
        return new WindowsWatchService(this);
    }
}
