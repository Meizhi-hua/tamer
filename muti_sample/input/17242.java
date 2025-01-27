public class ZipFSTester {
    public static void main(String[] args) throws Throwable {
        try (FileSystem fs = newZipFileSystem(Paths.get(args[0]),
                                              new HashMap<String, Object>()))
        {
            test0(fs);
            test1(fs);
            test2(fs);   
        }
    }
    static void test0(FileSystem fs)
        throws Exception
    {
        List<String> list = new LinkedList<>();
        try (ZipFile zf = new ZipFile(fs.toString())) {
            Enumeration<? extends ZipEntry> zes = zf.entries();
            while (zes.hasMoreElements()) {
                list.add(zes.nextElement().getName());
            }
            for (String pname : list) {
                Path path = fs.getPath(pname);
                if (!Files.exists(path))
                    throw new RuntimeException("path existence check failed!");
                while ((path = path.getParent()) != null) {
                    if (!Files.exists(path))
                        throw new RuntimeException("parent existence check failed!");
                }
            }
        }
    }
    static void test1(FileSystem fs0)
        throws Exception
    {
        Random rdm = new Random();
        Path tmpfsPath = getTempPath();
        Map<String, Object> env = new HashMap<String, Object>();
        env.put("create", "true");
        try (FileSystem copy = newZipFileSystem(tmpfsPath, env)) {
            z2zcopy(fs0, copy, "/", 0);
        }
        try (FileSystem fs = newZipFileSystem(tmpfsPath, new HashMap<String, Object>())) {
            FileSystemProvider provider = fs.provider();
            try (FileSystem fsPath = provider.newFileSystem(tmpfsPath, new HashMap<String, Object>())){}
            try (FileSystem fsUri = provider.newFileSystem(
                     new URI("jar", tmpfsPath.toUri().toString(), null),
                     new HashMap<String, Object>()))
            {
              throw new RuntimeException("newFileSystem(uri...) does not throw exception");
            } catch (FileSystemAlreadyExistsException fsaee) {}
            Path src = getTempPath();
            String tmpName = src.toString();
            OutputStream os = Files.newOutputStream(src);
            byte[] bits = new byte[12345];
            rdm.nextBytes(bits);
            os.write(bits);
            os.close();
            try {
                provider.newFileSystem(new File(System.getProperty("test.src", ".")).toPath(),
                                       new HashMap<String, Object>());
                throw new RuntimeException("newFileSystem() opens a directory as zipfs");
            } catch (UnsupportedOperationException uoe) {}
            try {
                provider.newFileSystem(src, new HashMap<String, Object>());
                throw new RuntimeException("newFileSystem() opens a non-zip file as zipfs");
            } catch (UnsupportedOperationException uoe) {}
            Path dst = getPathWithParents(fs, tmpName);
            Files.copy(src, dst);
            checkEqual(src, dst);
            Path dst2 = getPathWithParents(fs, "/xyz" + rdm.nextInt(100) +
                                           "/efg" + rdm.nextInt(100) + "/foo.class");
            Files.copy(dst, dst2);
            checkEqual(src, dst2);
            Files.delete(dst);
            if (Files.exists(dst))
                throw new RuntimeException("Failed!");
            Path dst3 = Paths.get(tmpName + "_Tmp");
            Files.move(dst2, dst3);
            checkEqual(src, dst3);
            if (Files.exists(dst2))
                throw new RuntimeException("Failed!");
            Files.delete(dst3);
            if (Files.exists(dst3))
                throw new RuntimeException("Failed!");
            Path parent = dst2.getParent();
            try {
                Files.newInputStream(parent);
                throw new RuntimeException("Failed");
            } catch (FileSystemException e) {
                e.printStackTrace();    
            }
            try {
                rmdirs(parent);
            } catch (IOException x) {
                x.printStackTrace();
            }
            fchCopy(src, dst);    
            checkEqual(src, dst);
            Path tmp = Paths.get(tmpName + "_Tmp");
            fchCopy(dst, tmp);   
            checkEqual(src, tmp);
            Files.delete(tmp);
            channel(fs, dst);
            Files.delete(dst);
            Files.delete(src);
        } finally {
            if (Files.exists(tmpfsPath))
                Files.delete(tmpfsPath);
        }
    }
    static void test2(FileSystem fs) throws Exception {
        Path fs1Path = getTempPath();
        Path fs2Path = getTempPath();
        Path fs3Path = getTempPath();
        Map<String, Object> env = new HashMap<String, Object>();
        env.put("create", "true");
        FileSystem fs0 = newZipFileSystem(fs1Path, env);
        final FileSystem fs2 = newZipFileSystem(fs2Path, env);
        final FileSystem fs3 = newZipFileSystem(fs3Path, env);
        System.out.println("copy src: fs -> fs0...");
        z2zcopy(fs, fs0, "/", 0);   
        fs0.close();                
        System.out.println("open fs0 as fs1");
        env = new HashMap<String, Object>();
        final FileSystem fs1 = newZipFileSystem(fs1Path, env);
        System.out.println("listing...");
        final ArrayList<String> files = new ArrayList<>();
        final ArrayList<String> dirs = new ArrayList<>();
        list(fs1.getPath("/"), files, dirs);
        Thread t0 = new Thread(new Runnable() {
            public void run() {
                List<String> list = new ArrayList<>(dirs);
                Collections.shuffle(list);
                for (String path : list) {
                    try {
                        z2zcopy(fs1, fs2, path, 0);
                    } catch (Exception x) {
                        x.printStackTrace();
                    }
                }
            }
        });
        Thread t1 = new Thread(new Runnable() {
            public void run() {
                List<String> list = new ArrayList<>(dirs);
                Collections.shuffle(list);
                for (String path : list) {
                    try {
                        z2zcopy(fs1, fs2, path, 1);
                    } catch (Exception x) {
                        x.printStackTrace();
                    }
                }
            }
        });
        Thread t2 = new Thread(new Runnable() {
            public void run() {
                List<String> list = new ArrayList<>(dirs);
                Collections.shuffle(list);
                for (String path : list) {
                    try {
                        z2zcopy(fs1, fs2, path, 2);
                    } catch (Exception x) {
                        x.printStackTrace();
                    }
                }
            }
        });
        Thread t3 = new Thread(new Runnable() {
            public void run() {
                List<String> list = new ArrayList<>(files);
                Collections.shuffle(list);
                while (!list.isEmpty()) {
                    Iterator<String> itr = list.iterator();
                    while (itr.hasNext()) {
                        String path = itr.next();
                        try {
                            if (Files.exists(fs2.getPath(path))) {
                                z2zmove(fs2, fs3, path);
                                itr.remove();
                            }
                        } catch (FileAlreadyExistsException x){
                            itr.remove();
                        } catch (Exception x) {
                            x.printStackTrace();
                        }
                    }
                }
            }
        });
        System.out.println("copying/removing...");
        t0.start(); t1.start(); t2.start(); t3.start();
        t0.join(); t1.join(); t2.join(); t3.join();
        System.out.println("closing: fs1, fs2");
        fs1.close();
        fs2.close();
        int failed = 0;
        System.out.println("checkEqual: fs vs fs3");
        for (String path : files) {
            try {
                checkEqual(fs.getPath(path), fs3.getPath(path));
            } catch (IOException x) {
                failed++;
            }
        }
        System.out.println("closing: fs3");
        fs3.close();
        System.out.println("opening: fs3 as fs4");
        FileSystem fs4 = newZipFileSystem(fs3Path, env);
        ArrayList<String> files2 = new ArrayList<>();
        ArrayList<String> dirs2 = new ArrayList<>();
        list(fs4.getPath("/"), files2, dirs2);
        System.out.println("checkEqual: fs vs fs4");
        for (String path : files2) {
            checkEqual(fs.getPath(path), fs4.getPath(path));
        }
        System.out.println("walking: fs4");
        walk(fs4.getPath("/"));
        System.out.println("closing: fs4");
        fs4.close();
        System.out.printf("failed=%d%n", failed);
        Files.delete(fs1Path);
        Files.delete(fs2Path);
        Files.delete(fs3Path);
    }
    private static FileSystem newZipFileSystem(Path path, Map<String, ?> env)
        throws Exception
    {
        return FileSystems.newFileSystem(
            new URI("jar", path.toUri().toString(), null), env, null);
    }
    private static Path getTempPath() throws IOException
    {
        File tmp = File.createTempFile("testzipfs_", "zip");
        tmp.delete();    
        return tmp.toPath();
    }
    private static void list(Path path, List<String> files, List<String> dirs )
        throws IOException
    {
        if (Files.isDirectory(path)) {
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(path)) {
                for (Path child : ds)
                    list(child, files, dirs);
            }
            dirs.add(path.toString());
        } else {
            files.add(path.toString());
        }
    }
    private static void z2zcopy(FileSystem src, FileSystem dst, String path,
                                int method)
        throws IOException
    {
        Path srcPath = src.getPath(path);
        Path dstPath = dst.getPath(path);
        if (Files.isDirectory(srcPath)) {
            if (!Files.exists(dstPath)) {
                try {
                    mkdirs(dstPath);
                } catch (FileAlreadyExistsException x) {}
            }
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(srcPath)) {
                for (Path child : ds) {
                    z2zcopy(src, dst,
                           path + (path.endsWith("/")?"":"/") + child.getFileName(),
                           method);
                }
            }
        } else {
            try {
                if (Files.exists(dstPath))
                    return;
                switch (method) {
                case 0:
                    Files.copy(srcPath, dstPath);
                    break;
                case 1:
                    chCopy(srcPath, dstPath);
                    break;
                case 2:
                    streamCopy(srcPath, dstPath);
                    break;
                }
            } catch (FileAlreadyExistsException x) {}
        }
    }
    private static void z2zmove(FileSystem src, FileSystem dst, String path)
        throws IOException
    {
        Path srcPath = src.getPath(path);
        Path dstPath = dst.getPath(path);
        if (Files.isDirectory(srcPath)) {
            if (!Files.exists(dstPath))
                mkdirs(dstPath);
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(srcPath)) {
                for (Path child : ds) {
                    z2zmove(src, dst,
                            path + (path.endsWith("/")?"":"/") + child.getFileName());
                }
            }
        } else {
            Path parent = dstPath.getParent();
            if (parent != null && Files.notExists(parent))
                mkdirs(parent);
            Files.move(srcPath, dstPath);
        }
    }
    private static void walk(Path path) throws IOException
    {
        Files.walkFileTree(
            path,
            new SimpleFileVisitor<Path>() {
                private int indent = 0;
                private void indent() {
                    int n = 0;
                    while (n++ < indent)
                        System.out.printf(" ");
                }
                @Override
                public FileVisitResult visitFile(Path file,
                                                 BasicFileAttributes attrs)
                {
                    indent();
                    System.out.printf("%s%n", file.getFileName().toString());
                    return FileVisitResult.CONTINUE;
                }
                @Override
                public FileVisitResult preVisitDirectory(Path dir,
                                                         BasicFileAttributes attrs)
                {
                    indent();
                    System.out.printf("[%s]%n", dir.toString());
                    indent += 2;
                    return FileVisitResult.CONTINUE;
                }
                @Override
                public FileVisitResult postVisitDirectory(Path dir,
                                                          IOException ioe)
                    throws IOException
                {
                    indent -= 2;
                    return FileVisitResult.CONTINUE;
                }
        });
    }
    private static void mkdirs(Path path) throws IOException {
        if (Files.exists(path))
            return;
        path = path.toAbsolutePath();
        Path parent = path.getParent();
        if (parent != null) {
            if (Files.notExists(parent))
                mkdirs(parent);
        }
        Files.createDirectory(path);
    }
    private static void rmdirs(Path path) throws IOException {
        while (path != null && path.getNameCount() != 0) {
            Files.delete(path);
            path = path.getParent();
        }
    }
    private static void checkEqual(Path src, Path dst) throws IOException
    {
        byte[] bufSrc = new byte[8192];
        byte[] bufDst = new byte[8192];
        try (InputStream isSrc = Files.newInputStream(src);
             InputStream isDst = Files.newInputStream(dst))
        {
            int nSrc = 0;
            while ((nSrc = isSrc.read(bufSrc)) != -1) {
                int nDst = 0;
                while (nDst < nSrc) {
                    int n = isDst.read(bufDst, nDst, nSrc - nDst);
                    if (n == -1) {
                        System.out.printf("checking <%s> vs <%s>...%n",
                                          src.toString(), dst.toString());
                        throw new RuntimeException("CHECK FAILED!");
                    }
                    nDst += n;
                }
                while (--nSrc >= 0) {
                    if (bufSrc[nSrc] != bufDst[nSrc]) {
                        System.out.printf("checking <%s> vs <%s>...%n",
                                          src.toString(), dst.toString());
                        throw new RuntimeException("CHECK FAILED!");
                    }
                    nSrc--;
                }
            }
        }
        try (SeekableByteChannel chSrc = Files.newByteChannel(src);
             SeekableByteChannel chDst = Files.newByteChannel(dst))
        {
            if (chSrc.size() != chDst.size()) {
                System.out.printf("src[%s].size=%d, dst[%s].size=%d%n",
                                  chSrc.toString(), chSrc.size(),
                                  chDst.toString(), chDst.size());
                throw new RuntimeException("CHECK FAILED!");
            }
            ByteBuffer bbSrc = ByteBuffer.allocate(8192);
            ByteBuffer bbDst = ByteBuffer.allocate(8192);
            int nSrc = 0;
            while ((nSrc = chSrc.read(bbSrc)) != -1) {
                int nDst = chDst.read(bbDst);
                if (nSrc != nDst) {
                    System.out.printf("checking <%s> vs <%s>...%n",
                                      src.toString(), dst.toString());
                    throw new RuntimeException("CHECK FAILED!");
                }
                while (--nSrc >= 0) {
                    if (bbSrc.get(nSrc) != bbDst.get(nSrc)) {
                        System.out.printf("checking <%s> vs <%s>...%n",
                                          src.toString(), dst.toString());
                        throw new RuntimeException("CHECK FAILED!");
                    }
                    nSrc--;
                }
                bbSrc.flip();
                bbDst.flip();
            }
        } catch (IOException x) {
            x.printStackTrace();
        }
    }
    private static void fchCopy(Path src, Path dst) throws IOException
    {
        Set<OpenOption> read = new HashSet<>();
        read.add(READ);
        Set<OpenOption> openwrite = new HashSet<>();
        openwrite.add(CREATE_NEW);
        openwrite.add(WRITE);
        try (FileChannel srcFc = src.getFileSystem()
                                    .provider()
                                    .newFileChannel(src, read);
             FileChannel dstFc = dst.getFileSystem()
                                    .provider()
                                    .newFileChannel(dst, openwrite))
        {
            ByteBuffer bb = ByteBuffer.allocate(8192);
            while (srcFc.read(bb) >= 0) {
                bb.flip();
                dstFc.write(bb);
                bb.clear();
            }
        }
    }
    private static void chCopy(Path src, Path dst) throws IOException
    {
        Set<OpenOption> read = new HashSet<>();
        read.add(READ);
        Set<OpenOption> openwrite = new HashSet<>();
        openwrite.add(CREATE_NEW);
        openwrite.add(WRITE);
        try (SeekableByteChannel srcCh = Files.newByteChannel(src, read);
             SeekableByteChannel dstCh = Files.newByteChannel(dst, openwrite))
        {
            ByteBuffer bb = ByteBuffer.allocate(8192);
            while (srcCh.read(bb) >= 0) {
                bb.flip();
                dstCh.write(bb);
                bb.clear();
            }
        }
    }
    private static void streamCopy(Path src, Path dst) throws IOException
    {
        byte[] buf = new byte[8192];
        try (InputStream isSrc = Files.newInputStream(src);
             OutputStream osDst = Files.newOutputStream(dst))
        {
            int n = 0;
            while ((n = isSrc.read(buf)) != -1) {
                osDst.write(buf, 0, n);
            }
        }
    }
    static void channel(FileSystem fs, Path path)
        throws Exception
    {
        System.out.println("test ByteChannel...");
        Set<OpenOption> read = new HashSet<>();
        read.add(READ);
        int n = 0;
        ByteBuffer bb = null;
        ByteBuffer bb2 = null;
        int N = 120;
        try (SeekableByteChannel sbc = Files.newByteChannel(path)) {
            System.out.printf("   sbc[0]: pos=%d, size=%d%n", sbc.position(), sbc.size());
            bb = ByteBuffer.allocate((int)sbc.size());
            n = sbc.read(bb);
            System.out.printf("   sbc[1]: read=%d, pos=%d, size=%d%n",
                              n, sbc.position(), sbc.size());
            bb2 = ByteBuffer.allocate((int)sbc.size());
        }
        try (SeekableByteChannel sbc = fs.provider().newFileChannel(path, read)) {
            sbc.position(N);
            System.out.printf("   sbc[2]: pos=%d, size=%d%n",
                              sbc.position(), sbc.size());
            bb2.limit(100);
            n = sbc.read(bb2);
            System.out.printf("   sbc[3]: read=%d, pos=%d, size=%d%n",
                              n, sbc.position(), sbc.size());
            System.out.printf("   sbc[4]: bb[%d]=%d, bb1[0]=%d%n",
                              N, bb.get(N) & 0xff, bb2.get(0) & 0xff);
        }
    }
    static Path getPathWithParents(FileSystem fs, String name)
        throws Exception
    {
        Path path = fs.getPath(name);
        Path parent = path.getParent();
        if (parent != null && Files.notExists(parent))
            mkdirs(parent);
        return path;
    }
}
