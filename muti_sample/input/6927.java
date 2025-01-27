public abstract class ImageTests extends GraphicsTests {
    public static boolean hasVolatileImage;
    public static boolean hasCompatImage;
    static {
        try {
            hasVolatileImage = (VolatileImage.class != null);
        } catch (NoClassDefFoundError e) {
        }
        try {
            new Canvas().getGraphicsConfiguration();
            hasCompatImage = true;
        } catch (NoSuchMethodError e) {
        }
    }
    static Group imageroot;
    static Group.EnableSet imgsrcroot;
    static Group.EnableSet bufimgsrcroot;
    static Group imgtestroot;
    static Group imgoptionsroot;
    static Group imageOpRoot;
    static Group imageOpOptRoot;
    static Group imageOpTestRoot;
    static Group graphicsTestRoot;
    static Group bufImgOpTestRoot;
    static Group rasterOpTestRoot;
    static Option opList;
    static Option doTouchSrc;
    static String transNodeNames[] = {
        null, "opaque", "bitmask", "translucent",
    };
    static String transDescriptions[] = {
        null, "Opaque", "Bitmask", "Translucent",
    };
    public static void init() {
        imageroot = new Group(graphicsroot, "imaging",
                              "Imaging Benchmarks");
        imageroot.setTabbed();
        imgsrcroot = new Group.EnableSet(imageroot, "src",
                                         "Image Rendering Sources");
        imgsrcroot.setBordered(true);
        imgoptionsroot = new Group(imgsrcroot, "options",
                                "Image Source Options");
        imgoptionsroot.setBordered(true);
        doTouchSrc =
            new Option.Toggle(imgoptionsroot, "touchsrc",
                              "Touch src image before every operation",
                               Option.Toggle.Off);
        imgtestroot = new Group(imageroot, "tests",
                                "Image Rendering Tests");
        imgtestroot.setBordered(true);
        new OffScreen();
        if (hasGraphics2D) {
            if (hasCompatImage) {
                new CompatImg(Transparency.OPAQUE);
                new CompatImg(Transparency.BITMASK);
                new CompatImg(Transparency.TRANSLUCENT);
            }
            if (hasVolatileImage) {
                new VolatileImg();
            }
            bufimgsrcroot =
                new Group.EnableSet(imgsrcroot, "bufimg",
                                    "BufferedImage Rendering Sources");
            new BufImg(BufferedImage.TYPE_INT_RGB);
            new BufImg(BufferedImage.TYPE_INT_ARGB);
            new BufImg(BufferedImage.TYPE_BYTE_GRAY);
            new BufImg(BufferedImage.TYPE_3BYTE_BGR);
            new BmByteIndexBufImg();
            new BufImg(BufferedImage.TYPE_INT_RGB, true);
            new BufImg(BufferedImage.TYPE_INT_ARGB, true);
            new BufImg(BufferedImage.TYPE_3BYTE_BGR, true);
            imageOpRoot = new Group(imageroot, "imageops",
                                    "Image Op Benchmarks");
            imageOpOptRoot = new Group(imageOpRoot, "opts", "Options");
            imageOpTestRoot = new Group(imageOpRoot, "tests", "Tests");
            graphicsTestRoot = new Group(imageOpTestRoot, "graphics2d",
                                         "Graphics2D Tests");
            bufImgOpTestRoot = new Group(imageOpTestRoot, "bufimgop",
                                         "BufferedImageOp Tests");
            rasterOpTestRoot = new Group(imageOpTestRoot, "rasterop",
                                         "RasterOp Tests");
            ArrayList opStrs = new ArrayList();
            ArrayList opDescs = new ArrayList();
            opStrs.add("convolve3x3zero");
            opDescs.add("ConvolveOp (3x3 blur, zero)");
            opStrs.add("convolve3x3noop");
            opDescs.add("ConvolveOp (3x3 blur, noop)");
            opStrs.add("convolve5x5zero");
            opDescs.add("ConvolveOp (5x5 edge, zero)");
            opStrs.add("convolve5x5noop");
            opDescs.add("ConvolveOp (5x5 edge, noop)");
            opStrs.add("lookup1byte");
            opDescs.add("LookupOp (1 band, byte)");
            opStrs.add("lookup1short");
            opDescs.add("LookupOp (1 band, short)");
            opStrs.add("lookup3byte");
            opDescs.add("LookupOp (3 band, byte)");
            opStrs.add("lookup3short");
            opDescs.add("LookupOp (3 band, short)");
            opStrs.add("rescale1band");
            opDescs.add("RescaleOp (1 band)");
            opStrs.add("rescale3band");
            opDescs.add("RescaleOp (3 band)");
            String[] opStrArr = new String[opStrs.size()];
            opStrArr = (String[])opStrs.toArray(opStrArr);
            String[] opDescArr = new String[opDescs.size()];
            opDescArr = (String[])opDescs.toArray(opDescArr);
            opList =
                new Option.ObjectList(imageOpOptRoot,
                                      "op", "Operation",
                                      opStrArr, opStrArr,
                                      opStrArr, opDescArr,
                                      0x1);
            ((Option.ObjectList) opList).setNumRows(4);
            new DrawImageOp();
            new BufImgOpFilter(false);
            new BufImgOpFilter(true);
            new RasterOpFilter(false);
            new RasterOpFilter(true);
        }
        new DrawImage();
        new DrawImageBg();
        new DrawImageScale("up", 1.5f);
        new DrawImageScale("down", .75f);
        new DrawImageTransform();
    }
    public static class Context extends GraphicsTests.Context {
        boolean touchSrc;
        Image src;
        AffineTransform tx;
    }
    public ImageTests(Group parent, String nodeName, String description) {
        this(parent, nodeName, description, null);
    }
    public ImageTests(Group parent, String nodeName, String description,
                      Modifier.Filter srcFilter)
    {
        super(parent, nodeName, description);
        addDependency(imgsrcroot, srcFilter);
        addDependency(doTouchSrc);
    }
    public GraphicsTests.Context createContext() {
        return new ImageTests.Context();
    }
    public void initContext(TestEnvironment env, GraphicsTests.Context ctx) {
        super.initContext(env, ctx);
        ImageTests.Context ictx = (ImageTests.Context) ctx;
        ictx.src = env.getSrcImage();
        ictx.touchSrc = env.isEnabled(doTouchSrc);
    }
    public abstract static class TriStateImageType extends Group {
        Image theImage;
        public TriStateImageType(Group parent, String nodename, String desc,
                                 int transparency)
        {
            super(parent, nodename, desc);
            setHorizontal();
            new DrawableImage(this, Transparency.OPAQUE, true);
            new DrawableImage(this, Transparency.BITMASK,
                              (transparency != Transparency.OPAQUE));
            new DrawableImage(this, Transparency.TRANSLUCENT,
                              (transparency == Transparency.TRANSLUCENT));
        }
        public Image getImage(TestEnvironment env, int w, int h) {
            if (theImage == null ||
                theImage.getWidth(null) != w ||
                theImage.getHeight(null) != h)
            {
                theImage = makeImage(env, w, h);
            }
            return theImage;
        }
        public abstract Image makeImage(TestEnvironment env, int w, int h);
    }
    public static class OffScreen extends TriStateImageType {
        public OffScreen() {
            super(imgsrcroot, "offscr", "Offscreen Image", Transparency.OPAQUE);
        }
        public Image makeImage(TestEnvironment env, int w, int h) {
            Canvas c = env.getCanvas();
            return c.createImage(w, h);
        }
    }
    public static class VolatileImg extends TriStateImageType {
        public VolatileImg() {
            super(imgsrcroot, "volimg", "Volatile Image", Transparency.OPAQUE);
        }
        public Image makeImage(TestEnvironment env, int w, int h) {
            Canvas c = env.getCanvas();
            return c.createVolatileImage(w, h);
        }
    }
    public static class CompatImg extends TriStateImageType {
        int transparency;
        public CompatImg(int transparency) {
            super(imgsrcroot,
                  Destinations.CompatImg.ShortNames[transparency],
                  Destinations.CompatImg.LongDescriptions[transparency],
                  transparency);
            this.transparency = transparency;
        }
        public Image makeImage(TestEnvironment env, int w, int h) {
            Canvas c = env.getCanvas();
            GraphicsConfiguration gc = c.getGraphicsConfiguration();
            return gc.createCompatibleImage(w, h, transparency);
        }
    }
    public static class BufImg extends TriStateImageType {
        int type;
        boolean unmanaged;
        static int Transparencies[] = {
            Transparency.TRANSLUCENT, 
            Transparency.OPAQUE,      
            Transparency.TRANSLUCENT, 
            Transparency.TRANSLUCENT, 
            Transparency.OPAQUE,      
            Transparency.OPAQUE,      
            Transparency.TRANSLUCENT, 
            Transparency.TRANSLUCENT, 
            Transparency.OPAQUE,      
            Transparency.OPAQUE,      
            Transparency.OPAQUE,      
            Transparency.OPAQUE,      
            Transparency.OPAQUE,      
            Transparency.OPAQUE,      
        };
        public BufImg(int type) {
            this(type, false);
        }
        public BufImg(int type, boolean unmanaged) {
            super(bufimgsrcroot,
                  (unmanaged ? "unmanaged" : "") +
                  Destinations.BufImg.ShortNames[type],
                  (unmanaged ? "Unmanaged " : "") +
                  Destinations.BufImg.Descriptions[type],
                  Transparencies[type]);
            this.type = type;
            this.unmanaged = unmanaged;
        }
        public Image makeImage(TestEnvironment env, int w, int h) {
            BufferedImage img = new BufferedImage(w, h, type);
            if (unmanaged) {
                DataBuffer db = img.getRaster().getDataBuffer();
                if (db instanceof DataBufferInt) {
                    ((DataBufferInt)db).getData();
                } else if (db instanceof DataBufferShort) {
                    ((DataBufferShort)db).getData();
                } else if (db instanceof DataBufferByte) {
                    ((DataBufferByte)db).getData();
                } else {
                    try {
                        img.setAccelerationPriority(0.0f);
                    } catch (Throwable e) {}
                }
            }
            return img;
        }
    }
    public static class BmByteIndexBufImg extends TriStateImageType {
        static IndexColorModel icm;
        public BmByteIndexBufImg() {
            super(bufimgsrcroot,
                  "ByteIndexedBm",
                  "8-bit Transparent Indexed Image",
                  Transparency.BITMASK);
        }
        public Image makeImage(TestEnvironment env, int w, int h) {
            if (icm == null) {
                int cmap[] = new int[256];
                int i = 1;
                for (int r = 0; r < 256; r += 51) {
                    for (int g = 0; g < 256; g += 51) {
                        for (int b = 0; b < 256; b += 51) {
                            cmap[i++] = (0xff<<24)|(r<<16)|(g<<8)|b;
                        }
                    }
                }
                icm = new IndexColorModel(8, 256, cmap, 0, true, 255,
                                          DataBuffer.TYPE_BYTE);
            }
            return new BufferedImage(w, h, BufferedImage.TYPE_BYTE_INDEXED,
                                     icm);
        }
    }
    public static class DrawableImage extends Option.Enable {
        static Color transparentBlack  = makeAlphaColor(Color.black, 0);
        static Color translucentRed    = makeAlphaColor(Color.red, 192);
        static Color translucentGreen  = makeAlphaColor(Color.green, 128);
        static Color translucentYellow = makeAlphaColor(Color.yellow, 64);
        static Color colorsets[][] = new Color[][] {
            null,
            {
                Color.blue,       Color.red,
                Color.green,      Color.yellow,
                Color.blue,
            },
            {
                transparentBlack, Color.red,
                Color.green,      transparentBlack,
                transparentBlack,
            },
            {
                Color.blue,       translucentRed,
                translucentGreen, translucentYellow,
                translucentRed,
            },
        };
        TriStateImageType tsit;
        int transparency;
        boolean possible;
        public DrawableImage(TriStateImageType parent, int transparency,
                             boolean possible)
        {
            super(parent,
                  transNodeNames[transparency],
                  transDescriptions[transparency],
                  false);
            this.tsit = parent;
            this.transparency = transparency;
            this.possible = possible;
        }
        public int getTransparency() {
            return transparency;
        }
        public JComponent getJComponent() {
            JComponent comp = super.getJComponent();
            comp.setEnabled(possible);
            return comp;
        }
        public String setValueFromString(String value) {
            if (!possible && !value.equalsIgnoreCase("disabled")) {
                return "Bad Value";
            }
            return super.setValueFromString(value);
        }
        public void modifyTest(TestEnvironment env) {
            int size = env.getIntValue(sizeList);
            Image src = tsit.getImage(env, size, size);
            Graphics g = src.getGraphics();
            if (hasGraphics2D) {
                ((Graphics2D) g).setComposite(AlphaComposite.Src);
            }
            if (size == 1) {
                g.setColor(colorsets[transparency][4]);
                g.fillRect(0, 0, 1, 1);
            } else {
                int mid = size/2;
                g.setColor(colorsets[transparency][0]);
                g.fillRect(0, 0, mid, mid);
                g.setColor(colorsets[transparency][1]);
                g.fillRect(mid, 0, size-mid, mid);
                g.setColor(colorsets[transparency][2]);
                g.fillRect(0, mid, mid, size-mid);
                g.setColor(colorsets[transparency][3]);
                g.fillRect(mid, mid, size-mid, size-mid);
            }
            g.dispose();
            env.setSrcImage(src);
        }
        public void restoreTest(TestEnvironment env) {
            env.setSrcImage(null);
        }
        public String getAbbreviatedModifierDescription(Object value) {
            return "from "+getModifierValueName(value);
        }
        public String getModifierValueName(Object val) {
            return getParent().getNodeName()+" "+getNodeName();
        }
    }
    public static class DrawImage extends ImageTests {
        public DrawImage() {
            super(imgtestroot, "drawimage", "drawImage(img, x, y, obs);");
        }
        public void runTest(Object ctx, int numReps) {
            ImageTests.Context ictx = (ImageTests.Context) ctx;
            int x = ictx.initX;
            int y = ictx.initY;
            Graphics g = ictx.graphics;
            g.translate(ictx.orgX, ictx.orgY);
            Image src = ictx.src;
            if (ictx.animate) {
                if (ictx.touchSrc) {
                    Graphics srcG = src.getGraphics();
                    do {
                        srcG.fillRect(0, 0, 1, 1);
                        g.drawImage(src, x, y, null);
                        if ((x -= 3) < 0) x += ictx.maxX;
                        if ((y -= 1) < 0) y += ictx.maxY;
                    } while (--numReps > 0);
                } else {
                    do {
                        g.drawImage(src, x, y, null);
                        if ((x -= 3) < 0) x += ictx.maxX;
                        if ((y -= 1) < 0) y += ictx.maxY;
                    } while (--numReps > 0);
                }
            } else {
                if (ictx.touchSrc) {
                    Graphics srcG = src.getGraphics();
                    do {
                        srcG.fillRect(0, 0, 1, 1);
                        g.drawImage(src, x, y, null);
                    } while (--numReps > 0);
                } else {
                    do {
                        g.drawImage(src, x, y, null);
                    } while (--numReps > 0);
                }
            }
            g.translate(-ictx.orgX, -ictx.orgY);
        }
    }
    public static class DrawImageBg extends ImageTests {
        public DrawImageBg() {
            super(imgtestroot, "drawimagebg", "drawImage(img, x, y, bg, obs);",
                  new Modifier.Filter() {
                      public boolean isCompatible(Object val) {
                          DrawableImage di = (DrawableImage) val;
                          return (di.getTransparency() != Transparency.OPAQUE);
                      }
                  });
        }
        public void runTest(Object ctx, int numReps) {
            ImageTests.Context ictx = (ImageTests.Context) ctx;
            int x = ictx.initX;
            int y = ictx.initY;
            Graphics g = ictx.graphics;
            g.translate(ictx.orgX, ictx.orgY);
            Image src = ictx.src;
            Color bg = Color.orange;
            if (ictx.animate) {
                if (ictx.touchSrc) {
                    Graphics srcG = src.getGraphics();
                    do {
                        srcG.fillRect(0, 0, 1, 1);
                        g.drawImage(src, x, y, bg, null);
                        if ((x -= 3) < 0) x += ictx.maxX;
                        if ((y -= 1) < 0) y += ictx.maxY;
                    } while (--numReps > 0);
                } else {
                    do {
                        g.drawImage(src, x, y, bg, null);
                        if ((x -= 3) < 0) x += ictx.maxX;
                        if ((y -= 1) < 0) y += ictx.maxY;
                    } while (--numReps > 0);
                }
            } else {
                if (ictx.touchSrc) {
                    Graphics srcG = src.getGraphics();
                    do {
                        srcG.fillRect(0, 0, 1, 1);
                        g.drawImage(src, x, y, bg, null);
                    } while (--numReps > 0);
                } else {
                    do {
                        g.drawImage(src, x, y, bg, null);
                    } while (--numReps > 0);
                }
            }
            g.translate(-ictx.orgX, -ictx.orgY);
        }
    }
    public static class DrawImageScale extends ImageTests {
        float scale;
        public DrawImageScale(String dir, float scale) {
            super(imgtestroot, "drawimagescale"+dir,
                               "drawImage(img, x, y, w*"+scale+", h*"+scale+", obs);");
            this.scale = scale;
        }
        public Dimension getOutputSize(int w, int h) {
            int neww = (int) (w * scale);
            int newh = (int) (h * scale);
            if (neww == w && scale > 1f) neww = w+1;
            if (newh == h && scale > 1f) newh = h+1;
            return new Dimension(neww, newh);
        }
        public void runTest(Object ctx, int numReps) {
            ImageTests.Context ictx = (ImageTests.Context) ctx;
            int x = ictx.initX;
            int y = ictx.initY;
            int w = ictx.outdim.width;
            int h = ictx.outdim.height;
            Graphics g = ictx.graphics;
            g.translate(ictx.orgX, ictx.orgY);
            Image src = ictx.src;
            if (ictx.animate) {
                if (ictx.touchSrc) {
                    Graphics srcG = src.getGraphics();
                    do {
                        srcG.fillRect(0, 0, 1, 1);
                        g.drawImage(src, x, y, w, h, null);
                        if ((x -= 3) < 0) x += ictx.maxX;
                        if ((y -= 1) < 0) y += ictx.maxY;
                    } while (--numReps > 0);
                } else {
                    do {
                        g.drawImage(src, x, y, w, h, null);
                        if ((x -= 3) < 0) x += ictx.maxX;
                        if ((y -= 1) < 0) y += ictx.maxY;
                    } while (--numReps > 0);
                }
            } else {
                Graphics srcG = src.getGraphics();
                if (ictx.touchSrc) {
                    do {
                        srcG.fillRect(0, 0, 1, 1);
                        g.drawImage(src, x, y, w, h, null);
                    } while (--numReps > 0);
                } else {
                    do {
                        g.drawImage(src, x, y, w, h, null);
                    } while (--numReps > 0);
                }
            }
            g.translate(-ictx.orgX, -ictx.orgY);
        }
    }
    public static class DrawImageTransform extends ImageTests {
        public DrawImageTransform() {
            super(imgtestroot, "drawimagetxform", "drawImage(img, tx, obs);");
        }
        public Dimension getOutputSize(int w, int h) {
            int neww = (int) Math.ceil(w * 1.1);
            int newh = (int) Math.ceil(h * 1.1);
            return new Dimension(neww, newh);
        }
        public void initContext(TestEnvironment env, GraphicsTests.Context ctx)
        {
            super.initContext(env, ctx);
            ImageTests.Context ictx = (ImageTests.Context) ctx;
            ictx.tx = new AffineTransform();
        }
        public void runTest(Object ctx, int numReps) {
            ImageTests.Context ictx = (ImageTests.Context) ctx;
            int x = ictx.initX;
            int y = ictx.initY;
            Graphics2D g = (Graphics2D) ictx.graphics;
            g.translate(ictx.orgX, ictx.orgY);
            Image src = ictx.src;
            AffineTransform tx = ictx.tx;
            if (ictx.animate) {
                if (ictx.touchSrc) {
                    Graphics srcG = src.getGraphics();
                    do {
                        tx.setTransform(1.0, 0.1, 0.1, 1.0, x, y);
                        srcG.fillRect(0, 0, 1, 1);
                        g.drawImage(src, tx, null);
                        if ((x -= 3) < 0) x += ictx.maxX;
                        if ((y -= 1) < 0) y += ictx.maxY;
                    } while (--numReps > 0);
                } else {
                    do {
                        tx.setTransform(1.0, 0.1, 0.1, 1.0, x, y);
                        g.drawImage(src, tx, null);
                        if ((x -= 3) < 0) x += ictx.maxX;
                        if ((y -= 1) < 0) y += ictx.maxY;
                    } while (--numReps > 0);
                }
            } else {
                tx.setTransform(1.0, 0.1, 0.1, 1.0, x, y);
                if (ictx.touchSrc) {
                    Graphics srcG = src.getGraphics();
                    do {
                        srcG.fillRect(0, 0, 1, 1);
                        g.drawImage(src, tx, null);
                    } while (--numReps > 0);
                } else {
                    do {
                        g.drawImage(src, tx, null);
                    } while (--numReps > 0);
                }
            }
            g.translate(-ictx.orgX, -ictx.orgY);
        }
    }
    private static abstract class ImageOpTests extends ImageTests {
        ImageOpTests(Group parent, String nodeName, String desc) {
            super(parent, nodeName, desc,
                  new Modifier.Filter() {
                      public boolean isCompatible(Object val) {
                          DrawableImage di = (DrawableImage) val;
                          Group imgtype = di.getParent();
                          return
                              !(imgtype instanceof VolatileImg) &&
                              !(imgtype instanceof OffScreen);
                      }
                  });
            addDependencies(imageOpOptRoot, true);
        }
        private static class Context extends ImageTests.Context {
            BufferedImageOp bufImgOp;
            BufferedImage   bufSrc;
            BufferedImage   bufDst;
            RasterOp        rasterOp;
            Raster          rasSrc;
            WritableRaster  rasDst;
        }
        public GraphicsTests.Context createContext() {
            return new ImageOpTests.Context();
        }
        public void initContext(TestEnvironment env,
                                GraphicsTests.Context ctx)
        {
            super.initContext(env, ctx);
            ImageOpTests.Context ictx = (ImageOpTests.Context)ctx;
            ictx.bufSrc = (BufferedImage)ictx.src;
            String op = (String)env.getModifier(opList);
            if (op.startsWith("convolve")) {
                Kernel kernel;
                if (op.startsWith("convolve3x3")) {
                    float[] data = {
                        0.1f, 0.1f, 0.1f,
                        0.1f, 0.2f, 0.1f,
                        0.1f, 0.1f, 0.1f,
                    };
                    kernel = new Kernel(3, 3, data);
                } else { 
                    float[] data = {
                        -1.0f, -1.0f, -1.0f, -1.0f, -1.0f,
                        -1.0f, -1.0f, -1.0f, -1.0f, -1.0f,
                        -1.0f, -1.0f, 24.0f, -1.0f, -1.0f,
                        -1.0f, -1.0f, -1.0f, -1.0f, -1.0f,
                        -1.0f, -1.0f, -1.0f, -1.0f, -1.0f,
                    };
                    kernel = new Kernel(5, 5, data);
                }
                int edge = op.endsWith("zero") ?
                    ConvolveOp.EDGE_ZERO_FILL : ConvolveOp.EDGE_NO_OP;
                ictx.bufImgOp = new ConvolveOp(kernel, edge, null);
            } else if (op.startsWith("lookup")) {
                if (op.endsWith("byte")) {
                    byte invert[] = new byte[256];
                    byte ordered[] = new byte[256];
                    for (int j = 0; j < 256 ; j++) {
                        invert[j] = (byte)(255-j);
                        ordered[j] = (byte)j;
                    }
                    if (op.equals("lookup1byte")) {
                        ictx.bufImgOp =
                            new LookupOp(new ByteLookupTable(0, invert),
                                         null);
                    } else { 
                        byte[][] yellowInvert =
                            new byte[][] { invert, invert, ordered };
                        ictx.bufImgOp =
                            new LookupOp(new ByteLookupTable(0, yellowInvert),
                                         null);
                    }
                } else { 
                    short invert[] = new short[256];
                    short ordered[] = new short[256];
                    for (int j = 0; j < 256 ; j++) {
                        invert[j] = (short)((255-j) * 255);
                        ordered[j] = (short)(j * 255);
                    }
                    if (op.equals("lookup1short")) {
                        ictx.bufImgOp =
                            new LookupOp(new ShortLookupTable(0, invert),
                                         null);
                    } else { 
                        short[][] yellowInvert =
                            new short[][] { invert, invert, ordered };
                        ictx.bufImgOp =
                            new LookupOp(new ShortLookupTable(0, yellowInvert),
                                         null);
                    }
                }
            } else if (op.equals("rescale1band")) {
                ictx.bufImgOp = new RescaleOp(0.5f, 10.0f, null);
            } else if (op.equals("rescale3band")) {
                float[] scaleFactors = { 0.5f,  0.3f, 0.8f };
                float[] offsets      = { 5.0f, -7.5f, 1.0f };
                ictx.bufImgOp = new RescaleOp(scaleFactors, offsets, null);
            } else {
                throw new InternalError("Invalid image op");
            }
            ictx.rasterOp = (RasterOp)ictx.bufImgOp;
        }
    }
    private static class DrawImageOp extends ImageOpTests {
        DrawImageOp() {
            super(graphicsTestRoot, "drawimageop",
                  "drawImage(srcBufImg, op, x, y);");
        }
        public void runTest(Object ctx, int numReps) {
            ImageOpTests.Context ictx = (ImageOpTests.Context)ctx;
            int x = ictx.initX;
            int y = ictx.initY;
            BufferedImageOp op = ictx.bufImgOp;
            BufferedImage src = ictx.bufSrc;
            Graphics2D g2 = (Graphics2D)ictx.graphics;
            g2.translate(ictx.orgX, ictx.orgY);
            if (ictx.animate) {
                if (ictx.touchSrc) {
                    Graphics gSrc = src.getGraphics();
                    do {
                        gSrc.fillRect(0, 0, 1, 1);
                        g2.drawImage(src, op, x, y);
                        if ((x -= 3) < 0) x += ictx.maxX;
                        if ((y -= 1) < 0) y += ictx.maxY;
                    } while (--numReps > 0);
                } else {
                    do {
                        g2.drawImage(src, op, x, y);
                        if ((x -= 3) < 0) x += ictx.maxX;
                        if ((y -= 1) < 0) y += ictx.maxY;
                    } while (--numReps > 0);
                }
            } else {
                if (ictx.touchSrc) {
                    Graphics gSrc = src.getGraphics();
                    do {
                        gSrc.fillRect(0, 0, 1, 1);
                        g2.drawImage(src, op, x, y);
                    } while (--numReps > 0);
                } else {
                    do {
                        g2.drawImage(src, op, x, y);
                    } while (--numReps > 0);
                }
            }
            g2.translate(-ictx.orgX, -ictx.orgY);
        }
    }
    private static class BufImgOpFilter extends ImageOpTests {
        private boolean cached;
        BufImgOpFilter(boolean cached) {
            super(bufImgOpTestRoot,
                  "filter" + (cached ? "cached" : "null"),
                  "op.filter(srcBufImg, " +
                  (cached ? "cachedCompatibleDestImg" : "null") + ");");
            this.cached = cached;
        }
        public void initContext(TestEnvironment env,
                                GraphicsTests.Context ctx)
        {
            super.initContext(env, ctx);
            ImageOpTests.Context ictx = (ImageOpTests.Context)ctx;
            if (cached) {
                ictx.bufDst =
                    ictx.bufImgOp.createCompatibleDestImage(ictx.bufSrc, null);
            }
        }
        public void runTest(Object ctx, int numReps) {
            ImageOpTests.Context ictx = (ImageOpTests.Context)ctx;
            BufferedImageOp op = ictx.bufImgOp;
            BufferedImage src = ictx.bufSrc;
            BufferedImage dst = ictx.bufDst;
            if (ictx.touchSrc) {
                Graphics gSrc = src.getGraphics();
                do {
                    gSrc.fillRect(0, 0, 1, 1);
                    op.filter(src, dst);
                } while (--numReps > 0);
            } else {
                do {
                    op.filter(src, dst);
                } while (--numReps > 0);
            }
        }
    }
    private static class RasterOpFilter extends ImageOpTests {
        private boolean cached;
        RasterOpFilter(boolean cached) {
            super(rasterOpTestRoot,
                  "filter" + (cached ? "cached" : "null"),
                  "op.filter(srcRaster, " +
                  (cached ? "cachedCompatibleDestRaster" : "null") + ");");
            this.cached = cached;
        }
        public void initContext(TestEnvironment env,
                                GraphicsTests.Context ctx)
        {
            super.initContext(env, ctx);
            ImageOpTests.Context ictx = (ImageOpTests.Context)ctx;
            ictx.rasSrc = ictx.bufSrc.getRaster();
            if (cached) {
                ictx.bufDst =
                    ictx.bufImgOp.createCompatibleDestImage(ictx.bufSrc, null);
                ictx.rasDst = ictx.bufDst.getRaster();
            }
        }
        public void runTest(Object ctx, int numReps) {
            ImageOpTests.Context ictx = (ImageOpTests.Context)ctx;
            RasterOp op = ictx.rasterOp;
            Raster src = ictx.rasSrc;
            WritableRaster dst = ictx.rasDst;
            if (ictx.touchSrc) {
                Graphics gSrc = ictx.bufSrc.getGraphics();
                do {
                    gSrc.fillRect(0, 0, 1, 1);
                    op.filter(src, dst);
                } while (--numReps > 0);
            } else {
                do {
                    op.filter(src, dst);
                } while (--numReps > 0);
            }
        }
    }
}
