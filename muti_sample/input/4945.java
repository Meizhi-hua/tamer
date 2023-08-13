public class SecureDS {
    static boolean supportsLinks;
    public static void main(String[] args) throws IOException {
        Path dir = TestUtil.createTemporaryDirectory();
        try {
            DirectoryStream<Path> stream = newDirectoryStream(dir);
            stream.close();
            if (!(stream instanceof SecureDirectoryStream)) {
                System.out.println("SecureDirectoryStream not supported.");
                return;
            }
            supportsLinks = TestUtil.supportsLinks(dir);
            doBasicTests(dir);
            doMoveTests(dir);
            miscTests(dir);
        } finally {
            TestUtil.removeAll(dir);
        }
    }
    static void doBasicTests(Path dir) throws IOException {
        Path dir1 = createDirectory(dir.resolve("dir1"));
        Path dir2 = dir.resolve("dir2");
        Path fileEntry = Paths.get("myfile");
        createFile(dir1.resolve(fileEntry));
        Path dirEntry = Paths.get("mydir");
        createDirectory(dir1.resolve(dirEntry));
        Path link1Entry = Paths.get("myfilelink");
        if (supportsLinks)
            createSymbolicLink(dir1.resolve(link1Entry), fileEntry);
        Path link2Entry = Paths.get("mydirlink");
        if (supportsLinks)
            createSymbolicLink(dir1.resolve(link2Entry), dirEntry);
        SecureDirectoryStream<Path> stream =
            (SecureDirectoryStream<Path>)newDirectoryStream(dir1);
        move(dir1, dir2);
        int count = 0;
        for (Path entry: stream) { count++; }
        assertTrue(count == (supportsLinks ? 4 : 2));
        assertTrue(stream
            .getFileAttributeView(BasicFileAttributeView.class)
                .readAttributes()
                    .isDirectory());
        assertTrue(stream
            .getFileAttributeView(fileEntry, BasicFileAttributeView.class)
                .readAttributes()
                    .isRegularFile());
        assertTrue(stream
            .getFileAttributeView(fileEntry, BasicFileAttributeView.class, NOFOLLOW_LINKS)
                .readAttributes()
                    .isRegularFile());
        assertTrue(stream
            .getFileAttributeView(dirEntry, BasicFileAttributeView.class)
                .readAttributes()
                    .isDirectory());
        assertTrue(stream
            .getFileAttributeView(dirEntry, BasicFileAttributeView.class, NOFOLLOW_LINKS)
                .readAttributes()
                    .isDirectory());
        if (supportsLinks) {
            assertTrue(stream
                .getFileAttributeView(link1Entry, BasicFileAttributeView.class)
                    .readAttributes()
                        .isRegularFile());
            assertTrue(stream
                .getFileAttributeView(link1Entry, BasicFileAttributeView.class, NOFOLLOW_LINKS)
                    .readAttributes()
                        .isSymbolicLink());
            assertTrue(stream
                .getFileAttributeView(link2Entry, BasicFileAttributeView.class)
                    .readAttributes()
                        .isDirectory());
            assertTrue(stream
                .getFileAttributeView(link2Entry, BasicFileAttributeView.class, NOFOLLOW_LINKS)
                    .readAttributes()
                        .isSymbolicLink());
        }
        Set<StandardOpenOption> opts = Collections.emptySet();
        stream.newByteChannel(fileEntry, opts).close();
        if (supportsLinks) {
            stream.newByteChannel(link1Entry, opts).close();
            try {
                Set<OpenOption> mixed = new HashSet<>();
                mixed.add(READ);
                mixed.add(NOFOLLOW_LINKS);
                stream.newByteChannel(link1Entry, mixed).close();
                shouldNotGetHere();
            } catch (IOException x) { }
        }
        stream.newDirectoryStream(dirEntry).close();
        stream.newDirectoryStream(dirEntry, LinkOption.NOFOLLOW_LINKS).close();
        if (supportsLinks) {
            stream.newDirectoryStream(link2Entry).close();
            try {
                stream.newDirectoryStream(link2Entry, LinkOption.NOFOLLOW_LINKS)
                    .close();
                shouldNotGetHere();
            } catch (IOException x) { }
        }
        if (supportsLinks) {
            stream.deleteFile(link1Entry);
            stream.deleteFile(link2Entry);
        }
        stream.deleteDirectory(dirEntry);
        stream.deleteFile(fileEntry);
        stream.close();
        delete(dir2);
    }
    static void doMoveTests(Path dir) throws IOException {
        Path dir1 = createDirectory(dir.resolve("dir1"));
        Path dir2 = createDirectory(dir.resolve("dir2"));
        Path fileEntry = Paths.get("myfile");
        createFile(dir1.resolve(fileEntry));
        Path dirEntry = Paths.get("mydir");
        createDirectory(dir1.resolve(dirEntry));
        Path linkEntry = Paths.get("mylink");
        if (supportsLinks)
            createSymbolicLink(dir1.resolve(linkEntry), Paths.get("missing"));
        Path target = Paths.get("newfile");
        SecureDirectoryStream<Path> stream1 =
            (SecureDirectoryStream<Path>)newDirectoryStream(dir1);
        SecureDirectoryStream<Path> stream2 =
            (SecureDirectoryStream<Path>)newDirectoryStream(dir2);
        stream1.move(fileEntry, stream2, target);
        assertTrue(notExists(dir1.resolve(fileEntry)));
        assertTrue(exists(dir2.resolve(target)));
        stream2.deleteFile(target);
        stream1.move(dirEntry, stream2, target);
        assertTrue(notExists(dir1.resolve(dirEntry)));
        assertTrue(exists(dir2.resolve(target)));
        stream2.deleteDirectory(target);
        if (supportsLinks) {
            stream1.move(linkEntry, stream2, target);
            assertTrue(isSymbolicLink(dir2.resolve(target)));
            stream2.deleteFile(target);
        }
        String testDirAsString = System.getProperty("test.dir");
        if (testDirAsString != null) {
            Path testDir = Paths.get(testDirAsString);
            if (!getFileStore(dir1).equals(getFileStore(testDir))) {
                SecureDirectoryStream<Path> ts =
                    (SecureDirectoryStream<Path>)newDirectoryStream(testDir);
                createFile(dir1.resolve(fileEntry));
                try {
                    stream1.move(fileEntry, ts, target);
                    shouldNotGetHere();
                } catch (AtomicMoveNotSupportedException x) { }
                ts.close();
                stream1.deleteFile(fileEntry);
            }
        }
        delete(dir1);
        delete(dir2);
    }
    static void miscTests(Path dir) throws IOException {
        Path file = Paths.get("file");
        createFile(dir.resolve(file));
        SecureDirectoryStream<Path> stream =
            (SecureDirectoryStream<Path>)newDirectoryStream(dir);
        try {
            stream.getFileAttributeView(null);
            shouldNotGetHere();
        } catch (NullPointerException x) { }
        try {
            stream.getFileAttributeView(null, BasicFileAttributeView.class);
            shouldNotGetHere();
        } catch (NullPointerException x) { }
        try {
            stream.getFileAttributeView(file, null);
            shouldNotGetHere();
        } catch (NullPointerException x) { }
        try {
            stream.newByteChannel(null, EnumSet.of(CREATE,WRITE));
            shouldNotGetHere();
        } catch (NullPointerException x) { }
        try {
            stream.newByteChannel(null, EnumSet.of(CREATE,WRITE,null));
            shouldNotGetHere();
        } catch (NullPointerException x) { }
        try {
            stream.newByteChannel(file, null);
            shouldNotGetHere();
        } catch (NullPointerException x) { }
        try {
            stream.move(null, stream, file);
            shouldNotGetHere();
        } catch (NullPointerException x) { }
        try {
            stream.move(file, null, file);
            shouldNotGetHere();
        } catch (NullPointerException x) { }
        try {
            stream.move(file, stream, null);
            shouldNotGetHere();
        } catch (NullPointerException x) { }
        try {
            stream.newDirectoryStream(null);
            shouldNotGetHere();
        } catch (NullPointerException x) { }
        try {
            stream.deleteFile(null);
            shouldNotGetHere();
        } catch (NullPointerException x) { }
        try {
            stream.deleteDirectory(null);
            shouldNotGetHere();
        } catch (NullPointerException x) { }
        stream.close();
        stream.close();     
        try {
            stream.newDirectoryStream(file);
            shouldNotGetHere();
        } catch (ClosedDirectoryStreamException x) { }
        try {
            stream.newByteChannel(file, EnumSet.of(READ));
            shouldNotGetHere();
        } catch (ClosedDirectoryStreamException x) { }
        try {
            stream.move(file, stream, file);
            shouldNotGetHere();
        } catch (ClosedDirectoryStreamException x) { }
        try {
            stream.deleteFile(file);
            shouldNotGetHere();
        } catch (ClosedDirectoryStreamException x) { }
        delete(dir.resolve(file));
    }
    static void assertTrue(boolean b) {
        if (!b) throw new RuntimeException("Assertion failed");
    }
    static void shouldNotGetHere() {
        assertTrue(false);
    }
}
