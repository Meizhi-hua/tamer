public class CheckPermissions {
    static class Checks {
        private List<Permission> permissionsChecked = new ArrayList<>();
        private Set<String>  propertiesChecked = new HashSet<>();
        private List<String> readsChecked   = new ArrayList<>();
        private List<String> writesChecked  = new ArrayList<>();
        private List<String> deletesChecked = new ArrayList<>();
        private List<String> execsChecked   = new ArrayList<>();
        List<Permission> permissionsChecked()  { return permissionsChecked; }
        Set<String> propertiesChecked()        { return propertiesChecked; }
        List<String> readsChecked()            { return readsChecked; }
        List<String> writesChecked()           { return writesChecked; }
        List<String> deletesChecked()          { return deletesChecked; }
        List<String> execsChecked()            { return execsChecked; }
    }
    static ThreadLocal<Checks> myChecks =
        new ThreadLocal<Checks>() {
            @Override protected Checks initialValue() {
                return null;
            }
        };
    static void prepare() {
        myChecks.set(new Checks());
    }
    static void assertCheckPermission(Class<? extends Permission> type,
                                      String name)
    {
        for (Permission perm: myChecks.get().permissionsChecked()) {
            if (type.isInstance(perm) && perm.getName().equals(name))
                return;
        }
        throw new RuntimeException(type.getName() + "(\"" + name + "\") not checked");
    }
    static void assertCheckPropertyAccess(String key) {
        if (!myChecks.get().propertiesChecked().contains(key))
            throw new RuntimeException("Property " + key + " not checked");
    }
    static void assertChecked(Path file, List<String> list) {
        String s = file.toString();
        for (String f: list) {
            if (f.endsWith(s))
                return;
        }
        throw new RuntimeException("Access not checked");
    }
    static void assertCheckRead(Path file) {
        assertChecked(file, myChecks.get().readsChecked());
    }
    static void assertCheckWrite(Path file) {
        assertChecked(file, myChecks.get().writesChecked());
    }
    static void assertCheckWriteToDirectory(Path dir) {
        String s = dir.toString();
        List<String> list = myChecks.get().writesChecked();
        for (String f: list) {
            if (f.startsWith(s)) {
                return;
            }
        }
        throw new RuntimeException("Access not checked");
    }
    static void assertCheckDelete(Path file) {
        assertChecked(file, myChecks.get().deletesChecked());
    }
    static void assertCheckExec(Path file) {
        assertChecked(file, myChecks.get().execsChecked());
    }
    static class LoggingSecurityManager extends SecurityManager {
        static void install() {
            System.setSecurityManager(new LoggingSecurityManager());
        }
        @Override
        public void checkPermission(Permission perm) {
            Checks checks = myChecks.get();
            if (checks != null)
                checks.permissionsChecked().add(perm);
        }
        @Override
        public void checkPropertyAccess(String key) {
            Checks checks = myChecks.get();
            if (checks != null)
                checks.propertiesChecked().add(key);
        }
        @Override
        public void checkRead(String file) {
            Checks checks = myChecks.get();
            if (checks != null)
                checks.readsChecked().add(file);
        }
        @Override
        public void checkWrite(String file) {
            Checks checks = myChecks.get();
            if (checks != null)
                checks.writesChecked().add(file);
        }
        @Override
        public void checkDelete(String file) {
            Checks checks = myChecks.get();
            if (checks != null)
                checks.deletesChecked().add(file);
        }
        @Override
        public void checkExec(String file) {
            Checks checks = myChecks.get();
            if (checks != null)
                checks.execsChecked().add(file);
        }
    }
    static void testBasicFileAttributeView(BasicFileAttributeView view, Path file)
        throws IOException
    {
        prepare();
        view.readAttributes();
        assertCheckRead(file);
        prepare();
        FileTime now = FileTime.fromMillis(System.currentTimeMillis());
        view.setTimes(null, now, now);
        assertCheckWrite(file);
    }
    static void testPosixFileAttributeView(PosixFileAttributeView view, Path file)
        throws IOException
    {
        prepare();
        PosixFileAttributes attrs = view.readAttributes();
        assertCheckRead(file);
        assertCheckPermission(RuntimePermission.class, "accessUserInformation");
        prepare();
        view.setPermissions(attrs.permissions());
        assertCheckWrite(file);
        assertCheckPermission(RuntimePermission.class, "accessUserInformation");
        prepare();
        view.setOwner(attrs.owner());
        assertCheckWrite(file);
        assertCheckPermission(RuntimePermission.class, "accessUserInformation");
        prepare();
        view.setOwner(attrs.owner());
        assertCheckWrite(file);
        assertCheckPermission(RuntimePermission.class, "accessUserInformation");
    }
    public static void main(String[] args) throws IOException {
        final Path testdir = Paths.get(System.getProperty("test.dir", ".")).toAbsolutePath();
        final Path tmpdir = Paths.get(System.getProperty("java.io.tmpdir"));
        Path file = createFile(testdir.resolve("file1234"));
        try {
            LoggingSecurityManager.install();
            prepare();
            exists(file);
            assertCheckRead(file);
            prepare();
            isReadable(file);
            assertCheckRead(file);
            prepare();
            isWritable(file);
            assertCheckWrite(file);
            prepare();
            isExecutable(file);
            assertCheckExec(file);
            Path target = testdir.resolve("target1234");
            prepare();
            copy(file, target);
            try {
                assertCheckRead(file);
                assertCheckWrite(target);
            } finally {
                delete(target);
            }
            if (TestUtil.supportsLinks(testdir)) {
                Path link = testdir.resolve("link1234");
                createSymbolicLink(link, file);
                try {
                    prepare();
                    copy(link, target, LinkOption.NOFOLLOW_LINKS);
                    try {
                        assertCheckRead(link);
                        assertCheckWrite(target);
                        assertCheckPermission(LinkPermission.class, "symbolic");
                    } finally {
                        delete(target);
                    }
                } finally {
                    delete(link);
                }
            }
            Path subdir = testdir.resolve("subdir1234");
            prepare();
            createDirectory(subdir);
            try {
                assertCheckWrite(subdir);
            } finally {
                delete(subdir);
            }
            Path fileToCreate = testdir.resolve("file7890");
            prepare();
            createFile(fileToCreate);
            try {
                assertCheckWrite(fileToCreate);
            } finally {
                delete(fileToCreate);
            }
            if (TestUtil.supportsLinks(testdir)) {
                prepare();
                Path link = testdir.resolve("link1234");
                createSymbolicLink(link, file);
                try {
                    assertCheckWrite(link);
                    assertCheckPermission(LinkPermission.class, "symbolic");
                } finally {
                    delete(link);
                }
            }
            if (TestUtil.supportsLinks(testdir)) {
                prepare();
                Path link = testdir.resolve("entry234");
                createLink(link, file);
                try {
                    assertCheckWrite(link);
                    assertCheckPermission(LinkPermission.class, "hard");
                } finally {
                    delete(link);
                }
            }
            prepare();
            Path tmpfile1 = createTempFile("foo", null);
            try {
                assertCheckWriteToDirectory(tmpdir);
            } finally {
                delete(tmpfile1);
            }
            prepare();
            Path tmpfile2 = createTempFile(testdir, "foo", ".tmp");
            try {
                assertCheckWriteToDirectory(testdir);
            } finally {
                delete(tmpfile2);
            }
            prepare();
            Path tmpdir1 = createTempDirectory("foo");
            try {
                assertCheckWriteToDirectory(tmpdir);
            } finally {
                delete(tmpdir1);
            }
            prepare();
            Path tmpdir2 = createTempDirectory(testdir, "foo");
            try {
                assertCheckWriteToDirectory(testdir);
            } finally {
                delete(tmpdir2);
            }
            Path fileToDelete = testdir.resolve("file7890");
            createFile(fileToDelete);
            prepare();
            delete(fileToDelete);
            assertCheckDelete(fileToDelete);
            createFile(fileToDelete);
            prepare();
            deleteIfExists(fileToDelete);   
            assertCheckDelete(fileToDelete);
            prepare();
            deleteIfExists(fileToDelete);   
            assertCheckDelete(fileToDelete);
            prepare();
            exists(file);
            assertCheckRead(file);
            prepare();
            notExists(file);
            assertCheckRead(file);
            prepare();
            getFileStore(file);
            assertCheckRead(file);
            assertCheckPermission(RuntimePermission.class, "getFileStoreAttributes");
            prepare();
            isSameFile(file, testdir);
            assertCheckRead(file);
            assertCheckRead(testdir);
            Path target2 = testdir.resolve("target1234");
            prepare();
            move(file, target2);
            try {
                assertCheckWrite(file);
                assertCheckWrite(target2);
            } finally {
                move(target2, file);
            }
            prepare();
            try (SeekableByteChannel sbc = newByteChannel(file)) {
                assertCheckRead(file);
            }
            prepare();
            try (SeekableByteChannel sbc = newByteChannel(file, WRITE)) {
                assertCheckWrite(file);
            }
            prepare();
            try (SeekableByteChannel sbc = newByteChannel(file, READ, WRITE)) {
                assertCheckRead(file);
                assertCheckWrite(file);
            }
            prepare();
            try (SeekableByteChannel sbc = newByteChannel(file, DELETE_ON_CLOSE)) {
                assertCheckRead(file);
                assertCheckDelete(file);
            }
            createFile(file); 
            prepare();
            try (InputStream in = newInputStream(file)) {
                assertCheckRead(file);
            }
            prepare();
            try (OutputStream out = newOutputStream(file)) {
                assertCheckWrite(file);
            }
            prepare();
            try (DirectoryStream<Path> stream = newDirectoryStream(testdir)) {
                assertCheckRead(testdir);
                if (stream instanceof SecureDirectoryStream<?>) {
                    Path entry;
                    SecureDirectoryStream<Path> sds =
                        (SecureDirectoryStream<Path>)stream;
                    entry = file.getFileName();
                    prepare();
                    try (SeekableByteChannel sbc = sds.newByteChannel(entry, EnumSet.of(READ))) {
                        assertCheckRead(file);
                    }
                    prepare();
                    try (SeekableByteChannel sbc = sds.newByteChannel(entry, EnumSet.of(WRITE))) {
                        assertCheckWrite(file);
                    }
                    entry = file.getFileName();
                    prepare();
                    sds.deleteFile(entry);
                    assertCheckDelete(file);
                    createFile(testdir.resolve(entry));  
                    entry = Paths.get("subdir1234");
                    createDirectory(testdir.resolve(entry));
                    prepare();
                    sds.deleteDirectory(entry);
                    assertCheckDelete(testdir.resolve(entry));
                    entry = Paths.get("tempname1234");
                    prepare();
                    sds.move(file.getFileName(), sds, entry);
                    assertCheckWrite(file);
                    assertCheckWrite(testdir.resolve(entry));
                    sds.move(entry, sds, file.getFileName());  
                    entry = Paths.get("subdir1234");
                    createDirectory(testdir.resolve(entry));
                    try {
                        prepare();
                        sds.newDirectoryStream(entry).close();
                        assertCheckRead(testdir.resolve(entry));
                    } finally {
                        delete(testdir.resolve(entry));
                    }
                    testBasicFileAttributeView(sds
                        .getFileAttributeView(BasicFileAttributeView.class), testdir);
                    testPosixFileAttributeView(sds
                        .getFileAttributeView(PosixFileAttributeView.class), testdir);
                    entry = file.getFileName();
                    testBasicFileAttributeView(sds
                        .getFileAttributeView(entry, BasicFileAttributeView.class), file);
                    testPosixFileAttributeView(sds
                        .getFileAttributeView(entry, PosixFileAttributeView.class), file);
                } else {
                    System.out.println("SecureDirectoryStream not tested");
                }
            }
            prepare();
            file.getFileName().toAbsolutePath();
            assertCheckPropertyAccess("user.dir");
            prepare();
            file.toRealPath();
            assertCheckRead(file);
            prepare();
            file.toRealPath(LinkOption.NOFOLLOW_LINKS);
            assertCheckRead(file);
            prepare();
            Paths.get(".").toRealPath();
            assertCheckPropertyAccess("user.dir");
            prepare();
            Paths.get(".").toRealPath(LinkOption.NOFOLLOW_LINKS);
            assertCheckPropertyAccess("user.dir");
            try (WatchService watcher = FileSystems.getDefault().newWatchService()) {
                prepare();
                testdir.register(watcher, StandardWatchEventKinds.ENTRY_DELETE);
                assertCheckRead(testdir);
            }
            prepare();
            getAttribute(file, "size");
            assertCheckRead(file);
            prepare();
            setAttribute(file, "lastModifiedTime",
                FileTime.fromMillis(System.currentTimeMillis()));
            assertCheckWrite(file);
            prepare();
            readAttributes(file, "*");
            assertCheckRead(file);
            testBasicFileAttributeView(
                getFileAttributeView(file, BasicFileAttributeView.class), file);
            {
                PosixFileAttributeView view =
                    getFileAttributeView(file, PosixFileAttributeView.class);
                if (view != null &&
                    getFileStore(file).supportsFileAttributeView(PosixFileAttributeView.class))
                {
                    testPosixFileAttributeView(view, file);
                } else {
                    System.out.println("PosixFileAttributeView not tested");
                }
            }
            {
                DosFileAttributeView view =
                    getFileAttributeView(file, DosFileAttributeView.class);
                if (view != null &&
                    getFileStore(file).supportsFileAttributeView(DosFileAttributeView.class))
                {
                    prepare();
                    view.readAttributes();
                    assertCheckRead(file);
                    prepare();
                    view.setArchive(false);
                    assertCheckWrite(file);
                    prepare();
                    view.setHidden(false);
                    assertCheckWrite(file);
                    prepare();
                    view.setReadOnly(false);
                    assertCheckWrite(file);
                    prepare();
                    view.setSystem(false);
                    assertCheckWrite(file);
                } else {
                    System.out.println("DosFileAttributeView not tested");
                }
            }
            {
                FileOwnerAttributeView view =
                    getFileAttributeView(file, FileOwnerAttributeView.class);
                if (view != null &&
                    getFileStore(file).supportsFileAttributeView(FileOwnerAttributeView.class))
                {
                    prepare();
                    UserPrincipal owner = view.getOwner();
                    assertCheckRead(file);
                    assertCheckPermission(RuntimePermission.class, "accessUserInformation");
                    prepare();
                    view.setOwner(owner);
                    assertCheckWrite(file);
                    assertCheckPermission(RuntimePermission.class, "accessUserInformation");
                } else {
                    System.out.println("FileOwnerAttributeView not tested");
                }
            }
            {
                UserDefinedFileAttributeView view =
                    getFileAttributeView(file, UserDefinedFileAttributeView.class);
                if (view != null &&
                    getFileStore(file).supportsFileAttributeView(UserDefinedFileAttributeView.class))
                {
                    prepare();
                    view.write("test", ByteBuffer.wrap(new byte[100]));
                    assertCheckWrite(file);
                    assertCheckPermission(RuntimePermission.class,
                                               "accessUserDefinedAttributes");
                    prepare();
                    view.read("test", ByteBuffer.allocate(100));
                    assertCheckRead(file);
                    assertCheckPermission(RuntimePermission.class,
                                               "accessUserDefinedAttributes");
                    prepare();
                    view.size("test");
                    assertCheckRead(file);
                    assertCheckPermission(RuntimePermission.class,
                                               "accessUserDefinedAttributes");
                    prepare();
                    view.list();
                    assertCheckRead(file);
                    assertCheckPermission(RuntimePermission.class,
                                               "accessUserDefinedAttributes");
                    prepare();
                    view.delete("test");
                    assertCheckWrite(file);
                    assertCheckPermission(RuntimePermission.class,
                                               "accessUserDefinedAttributes");
                } else {
                    System.out.println("UserDefinedFileAttributeView not tested");
                }
            }
            {
                AclFileAttributeView view =
                    getFileAttributeView(file, AclFileAttributeView.class);
                if (view != null &&
                    getFileStore(file).supportsFileAttributeView(AclFileAttributeView.class))
                {
                    prepare();
                    List<AclEntry> acl = view.getAcl();
                    assertCheckRead(file);
                    assertCheckPermission(RuntimePermission.class, "accessUserInformation");
                    prepare();
                    view.setAcl(acl);
                    assertCheckWrite(file);
                    assertCheckPermission(RuntimePermission.class, "accessUserInformation");
                } else {
                    System.out.println("AclFileAttributeView not tested");
                }
            }
            UserPrincipalLookupService lookupService =
                FileSystems.getDefault().getUserPrincipalLookupService();
            UserPrincipal owner = getOwner(file);
            prepare();
            lookupService.lookupPrincipalByName(owner.getName());
            assertCheckPermission(RuntimePermission.class,
                                       "lookupUserInformation");
            try {
                UserPrincipal group = readAttributes(file, PosixFileAttributes.class).group();
                prepare();
                lookupService.lookupPrincipalByGroupName(group.getName());
                assertCheckPermission(RuntimePermission.class,
                                           "lookupUserInformation");
            } catch (UnsupportedOperationException ignore) {
                System.out.println("lookupPrincipalByGroupName not tested");
            }
        } finally {
            deleteIfExists(file);
        }
    }
}
