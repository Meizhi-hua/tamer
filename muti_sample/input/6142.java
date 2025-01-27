public class PackerImpl  extends TLGlobals implements Pack200.Packer {
    public PackerImpl() {}
    @SuppressWarnings("unchecked")
    public SortedMap properties() {
        return props;
    }
    public void pack(JarFile in, OutputStream out) throws IOException {
        assert(Utils.currentInstance.get() == null);
        TimeZone tz = (props.getBoolean(Utils.PACK_DEFAULT_TIMEZONE))
                      ? null
                      : TimeZone.getDefault();
        try {
            Utils.currentInstance.set(this);
            if (tz != null) TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
            if ("0".equals(props.getProperty(Pack200.Packer.EFFORT))) {
                Utils.copyJarFile(in, out);
            } else {
                (new DoPack()).run(in, out);
            }
        } finally {
            Utils.currentInstance.set(null);
            if (tz != null) TimeZone.setDefault(tz);
            in.close();
        }
    }
    public void pack(JarInputStream in, OutputStream out) throws IOException {
        assert(Utils.currentInstance.get() == null);
        TimeZone tz = (props.getBoolean(Utils.PACK_DEFAULT_TIMEZONE)) ? null :
            TimeZone.getDefault();
        try {
            Utils.currentInstance.set(this);
            if (tz != null) TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
            if ("0".equals(props.getProperty(Pack200.Packer.EFFORT))) {
                Utils.copyJarFile(in, out);
            } else {
                (new DoPack()).run(in, out);
            }
        } finally {
            Utils.currentInstance.set(null);
            if (tz != null) TimeZone.setDefault(tz);
            in.close();
        }
    }
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        props.addListener(listener);
    }
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        props.removeListener(listener);
    }
    @SuppressWarnings("unchecked")
    private class DoPack {
        final int verbose = props.getInteger(Utils.DEBUG_VERBOSE);
        {
            props.setInteger(Pack200.Packer.PROGRESS, 0);
            if (verbose > 0) Utils.log.info(props.toString());
        }
        final Package pkg = new Package();
        final String unknownAttrCommand;
        {
            String uaMode = props.getProperty(Pack200.Packer.UNKNOWN_ATTRIBUTE, Pack200.Packer.PASS);
            if (!(Pack200.Packer.STRIP.equals(uaMode) ||
                  Pack200.Packer.PASS.equals(uaMode) ||
                  Pack200.Packer.ERROR.equals(uaMode))) {
                throw new RuntimeException("Bad option: " + Pack200.Packer.UNKNOWN_ATTRIBUTE + " = " + uaMode);
            }
            unknownAttrCommand = uaMode.intern();
        }
        final Map<Attribute.Layout, Attribute> attrDefs;
        final Map<Attribute.Layout, String> attrCommands;
        {
            Map<Attribute.Layout, Attribute> lattrDefs   = new HashMap<>();
            Map<Attribute.Layout, String>  lattrCommands = new HashMap<>();
            String[] keys = {
                Pack200.Packer.CLASS_ATTRIBUTE_PFX,
                Pack200.Packer.FIELD_ATTRIBUTE_PFX,
                Pack200.Packer.METHOD_ATTRIBUTE_PFX,
                Pack200.Packer.CODE_ATTRIBUTE_PFX
            };
            int[] ctypes = {
                Constants.ATTR_CONTEXT_CLASS,
                Constants.ATTR_CONTEXT_FIELD,
                Constants.ATTR_CONTEXT_METHOD,
                Constants.ATTR_CONTEXT_CODE
            };
            for (int i = 0; i < ctypes.length; i++) {
                String pfx = keys[i];
                Map<Object, Object> map = props.prefixMap(pfx);
                for (Object k : map.keySet()) {
                    String key = (String)k;
                    assert(key.startsWith(pfx));
                    String name = key.substring(pfx.length());
                    String layout = props.getProperty(key);
                    Layout lkey = Attribute.keyForLookup(ctypes[i], name);
                    if (Pack200.Packer.STRIP.equals(layout) ||
                        Pack200.Packer.PASS.equals(layout) ||
                        Pack200.Packer.ERROR.equals(layout)) {
                        lattrCommands.put(lkey, layout.intern());
                    } else {
                        Attribute.define(lattrDefs, ctypes[i], name, layout);
                        if (verbose > 1) {
                            Utils.log.fine("Added layout for "+Constants.ATTR_CONTEXT_NAME[i]+" attribute "+name+" = "+layout);
                        }
                        assert(lattrDefs.containsKey(lkey));
                    }
                }
            }
            this.attrDefs = (lattrDefs.isEmpty()) ? null : lattrDefs;
            this.attrCommands = (lattrCommands.isEmpty()) ? null : lattrCommands;
        }
        final boolean keepFileOrder
            = props.getBoolean(Pack200.Packer.KEEP_FILE_ORDER);
        final boolean keepClassOrder
            = props.getBoolean(Utils.PACK_KEEP_CLASS_ORDER);
        final boolean keepModtime
            = Pack200.Packer.KEEP.equals(props.getProperty(Pack200.Packer.MODIFICATION_TIME));
        final boolean latestModtime
            = Pack200.Packer.LATEST.equals(props.getProperty(Pack200.Packer.MODIFICATION_TIME));
        final boolean keepDeflateHint
            = Pack200.Packer.KEEP.equals(props.getProperty(Pack200.Packer.DEFLATE_HINT));
        {
            if (!keepModtime && !latestModtime) {
                int modtime = props.getTime(Pack200.Packer.MODIFICATION_TIME);
                if (modtime != Constants.NO_MODTIME) {
                    pkg.default_modtime = modtime;
                }
            }
            if (!keepDeflateHint) {
                boolean deflate_hint = props.getBoolean(Pack200.Packer.DEFLATE_HINT);
                if (deflate_hint) {
                    pkg.default_options |= Constants.AO_DEFLATE_HINT;
                }
            }
        }
        long totalOutputSize = 0;
        int  segmentCount = 0;
        long segmentTotalSize = 0;
        long segmentSize = 0;  
        final long segmentLimit;
        {
            long limit;
            if (props.getProperty(Pack200.Packer.SEGMENT_LIMIT, "").equals(""))
                limit = -1;
            else
                limit = props.getLong(Pack200.Packer.SEGMENT_LIMIT);
            limit = Math.min(Integer.MAX_VALUE, limit);
            limit = Math.max(-1, limit);
            if (limit == -1)
                limit = Long.MAX_VALUE;
            segmentLimit = limit;
        }
        final List<String> passFiles;  
        {
            passFiles = props.getProperties(Pack200.Packer.PASS_FILE_PFX);
            for (ListIterator<String> i = passFiles.listIterator(); i.hasNext(); ) {
                String file = i.next();
                if (file == null) { i.remove(); continue; }
                file = Utils.getJarEntryName(file);  
                if (file.endsWith("/"))
                    file = file.substring(0, file.length()-1);
                i.set(file);
            }
            if (verbose > 0) Utils.log.info("passFiles = " + passFiles);
        }
        {
            int ver;
            if ((ver = props.getInteger(Utils.COM_PREFIX+"min.class.majver")) != 0)
                pkg.min_class_majver = (short) ver;
            if ((ver = props.getInteger(Utils.COM_PREFIX+"min.class.minver")) != 0)
                pkg.min_class_minver = (short) ver;
            if ((ver = props.getInteger(Utils.COM_PREFIX+"max.class.majver")) != 0)
                pkg.max_class_majver = (short) ver;
            if ((ver = props.getInteger(Utils.COM_PREFIX+"max.class.minver")) != 0)
                pkg.max_class_minver = (short) ver;
            if ((ver = props.getInteger(Utils.COM_PREFIX+"package.minver")) != 0)
                pkg.package_minver = (short) ver;
            if ((ver = props.getInteger(Utils.COM_PREFIX+"package.majver")) != 0)
                pkg.package_majver = (short) ver;
        }
        {
            int opt = props.getInteger(Utils.COM_PREFIX+"archive.options");
            if (opt != 0)
                pkg.default_options |= opt;
        }
        boolean isClassFile(String name) {
            if (!name.endsWith(".class"))  return false;
            for (String prefix = name; ; ) {
                if (passFiles.contains(prefix))  return false;
                int chop = prefix.lastIndexOf('/');
                if (chop < 0)  break;
                prefix = prefix.substring(0, chop);
            }
            return true;
        }
        boolean isMetaInfFile(String name) {
            return name.startsWith("/" + Utils.METAINF) ||
                        name.startsWith(Utils.METAINF);
        }
        private void makeNextPackage() {
            pkg.reset();
        }
        final class InFile {
            final String name;
            final JarFile jf;
            final JarEntry je;
            final File f;
            int modtime = Constants.NO_MODTIME;
            int options;
            InFile(String name) {
                this.name = Utils.getJarEntryName(name);
                this.f = new File(name);
                this.jf = null;
                this.je = null;
                int timeSecs = getModtime(f.lastModified());
                if (keepModtime && timeSecs != Constants.NO_MODTIME) {
                    this.modtime = timeSecs;
                } else if (latestModtime && timeSecs > pkg.default_modtime) {
                    pkg.default_modtime = timeSecs;
                }
            }
            InFile(JarFile jf, JarEntry je) {
                this.name = Utils.getJarEntryName(je.getName());
                this.f = null;
                this.jf = jf;
                this.je = je;
                int timeSecs = getModtime(je.getTime());
                if (keepModtime && timeSecs != Constants.NO_MODTIME) {
                     this.modtime = timeSecs;
                } else if (latestModtime && timeSecs > pkg.default_modtime) {
                    pkg.default_modtime = timeSecs;
                }
                if (keepDeflateHint && je.getMethod() == JarEntry.DEFLATED) {
                    options |= Constants.FO_DEFLATE_HINT;
                }
            }
            InFile(JarEntry je) {
                this(null, je);
            }
            long getInputLength() {
                long len = (je != null)? je.getSize(): f.length();
                assert(len >= 0) : this+".len="+len;
                return Math.max(0, len) + name.length() + 5;
            }
            int getModtime(long timeMillis) {
                long seconds = (timeMillis+500) / 1000;
                if ((int)seconds == seconds) {
                    return (int)seconds;
                } else {
                    Utils.log.warning("overflow in modtime for "+f);
                    return Constants.NO_MODTIME;
                }
            }
            void copyTo(Package.File file) {
                if (modtime != Constants.NO_MODTIME)
                    file.modtime = modtime;
                file.options |= options;
            }
            InputStream getInputStream() throws IOException {
                if (jf != null)
                    return jf.getInputStream(je);
                else
                    return new FileInputStream(f);
            }
            public String toString() {
                return name;
            }
        }
        private int nread = 0;  
        private void noteRead(InFile f) {
            nread++;
            if (verbose > 2)
                Utils.log.fine("...read "+f.name);
            if (verbose > 0 && (nread % 1000) == 0)
                Utils.log.info("Have read "+nread+" files...");
        }
        void run(JarInputStream in, OutputStream out) throws IOException {
            if (in.getManifest() != null) {
                ByteArrayOutputStream tmp = new ByteArrayOutputStream();
                in.getManifest().write(tmp);
                InputStream tmpIn = new ByteArrayInputStream(tmp.toByteArray());
                pkg.addFile(readFile(JarFile.MANIFEST_NAME, tmpIn));
            }
            for (JarEntry je; (je = in.getNextJarEntry()) != null; ) {
                InFile inFile = new InFile(je);
                String name = inFile.name;
                Package.File bits = readFile(name, in);
                Package.File file = null;
                long inflen = (isMetaInfFile(name))
                              ? 0L
                              : inFile.getInputLength();
                if ((segmentSize += inflen) > segmentLimit) {
                    segmentSize -= inflen;
                    int nextCount = -1;  
                    flushPartial(out, nextCount);
                }
                if (verbose > 1) {
                    Utils.log.fine("Reading " + name);
                }
                assert(je.isDirectory() == name.endsWith("/"));
                if (isClassFile(name)) {
                    file = readClass(name, bits.getInputStream());
                }
                if (file == null) {
                    file = bits;
                    pkg.addFile(file);
                }
                inFile.copyTo(file);
                noteRead(inFile);
            }
            flushAll(out);
        }
        void run(JarFile in, OutputStream out) throws IOException {
            List<InFile> inFiles = scanJar(in);
            if (verbose > 0)
                Utils.log.info("Reading " + inFiles.size() + " files...");
            int numDone = 0;
            for (InFile inFile : inFiles) {
                String name      = inFile.name;
                long inflen = (isMetaInfFile(name))
                               ? 0L
                               : inFile.getInputLength() ;
                if ((segmentSize += inflen) > segmentLimit) {
                    segmentSize -= inflen;
                    float filesDone = numDone+1;
                    float segsDone  = segmentCount+1;
                    float filesToDo = inFiles.size() - filesDone;
                    float segsToDo  = filesToDo * (segsDone/filesDone);
                    if (verbose > 1)
                        Utils.log.fine("Estimated segments to do: "+segsToDo);
                    flushPartial(out, (int) Math.ceil(segsToDo));
                }
                InputStream strm = inFile.getInputStream();
                if (verbose > 1)
                    Utils.log.fine("Reading " + name);
                Package.File file = null;
                if (isClassFile(name)) {
                    file = readClass(name, strm);
                    if (file == null) {
                        strm.close();
                        strm = inFile.getInputStream();
                    }
                }
                if (file == null) {
                    file = readFile(name, strm);
                    pkg.addFile(file);
                }
                inFile.copyTo(file);
                strm.close();  
                noteRead(inFile);
                numDone += 1;
            }
            flushAll(out);
        }
        Package.File readClass(String fname, InputStream in) throws IOException {
            Package.Class cls = pkg.new Class(fname);
            in = new BufferedInputStream(in);
            ClassReader reader = new ClassReader(cls, in);
            reader.setAttrDefs(attrDefs);
            reader.setAttrCommands(attrCommands);
            reader.unknownAttrCommand = unknownAttrCommand;
            try {
                reader.read();
            } catch (IOException ioe) {
                String message = "Passing class file uncompressed due to";
                if (ioe instanceof Attribute.FormatException) {
                    Attribute.FormatException ee = (Attribute.FormatException) ioe;
                    if (ee.layout.equals(Pack200.Packer.PASS)) {
                        Utils.log.info(ee.toString());
                        Utils.log.warning(message + " unrecognized attribute: " +
                                fname);
                        return null;
                    }
                } else if (ioe instanceof ClassReader.ClassFormatException) {
                    ClassReader.ClassFormatException ce = (ClassReader.ClassFormatException) ioe;
                    if (unknownAttrCommand.equals(Pack200.Packer.PASS)) {
                        Utils.log.info(ce.toString());
                        Utils.log.warning(message + " unknown class format: " +
                                fname);
                        return null;
                    }
                }
                throw ioe;
            }
            pkg.addClass(cls);
            return cls.file;
        }
        Package.File readFile(String fname, InputStream in) throws IOException {
            Package.File file = pkg.new File(fname);
            file.readFrom(in);
            if (file.isDirectory() && file.getFileLength() != 0)
                throw new IllegalArgumentException("Non-empty directory: "+file.getFileName());
            return file;
        }
        void flushPartial(OutputStream out, int nextCount) throws IOException {
            if (pkg.files.isEmpty() && pkg.classes.isEmpty()) {
                return;  
            }
            flushPackage(out, Math.max(1, nextCount));
            props.setInteger(Pack200.Packer.PROGRESS, 25);
            makeNextPackage();
            segmentCount += 1;
            segmentTotalSize += segmentSize;
            segmentSize = 0;
        }
        void flushAll(OutputStream out) throws IOException {
            props.setInteger(Pack200.Packer.PROGRESS, 50);
            flushPackage(out, 0);
            out.flush();
            props.setInteger(Pack200.Packer.PROGRESS, 100);
            segmentCount += 1;
            segmentTotalSize += segmentSize;
            segmentSize = 0;
            if (verbose > 0 && segmentCount > 1) {
                Utils.log.info("Transmitted "
                                 +segmentTotalSize+" input bytes in "
                                 +segmentCount+" segments totaling "
                                 +totalOutputSize+" bytes");
            }
        }
        void flushPackage(OutputStream out, int nextCount) throws IOException {
            int nfiles = pkg.files.size();
            if (!keepFileOrder) {
                if (verbose > 1)  Utils.log.fine("Reordering files.");
                boolean stripDirectories = true;
                pkg.reorderFiles(keepClassOrder, stripDirectories);
            } else {
                assert(pkg.files.containsAll(pkg.getClassStubs()));
                List<Package.File> res = pkg.files;
                assert((res = new ArrayList<>(pkg.files))
                       .retainAll(pkg.getClassStubs()) || true);
                assert(res.equals(pkg.getClassStubs()));
            }
            pkg.trimStubs();
            if (props.getBoolean(Utils.COM_PREFIX+"strip.debug"))        pkg.stripAttributeKind("Debug");
            if (props.getBoolean(Utils.COM_PREFIX+"strip.compile"))      pkg.stripAttributeKind("Compile");
            if (props.getBoolean(Utils.COM_PREFIX+"strip.constants"))    pkg.stripAttributeKind("Constant");
            if (props.getBoolean(Utils.COM_PREFIX+"strip.exceptions"))   pkg.stripAttributeKind("Exceptions");
            if (props.getBoolean(Utils.COM_PREFIX+"strip.innerclasses")) pkg.stripAttributeKind("InnerClasses");
            if (pkg.package_majver <= 0)  pkg.choosePackageVersion();
            PackageWriter pw = new PackageWriter(pkg, out);
            pw.archiveNextCount = nextCount;
            pw.write();
            out.flush();
            if (verbose > 0) {
                long outSize = pw.archiveSize0+pw.archiveSize1;
                totalOutputSize += outSize;
                long inSize = segmentSize;
                Utils.log.info("Transmitted "
                                 +nfiles+" files of "
                                 +inSize+" input bytes in a segment of "
                                 +outSize+" bytes");
            }
        }
        List<InFile> scanJar(JarFile jf) throws IOException {
            List<InFile> inFiles = new ArrayList<>();
            try {
                for (JarEntry je : Collections.list(jf.entries())) {
                    InFile inFile = new InFile(jf, je);
                    assert(je.isDirectory() == inFile.name.endsWith("/"));
                    inFiles.add(inFile);
                }
            } catch (IllegalStateException ise) {
                throw new IOException(ise.getLocalizedMessage(), ise);
            }
            return inFiles;
        }
    }
}
