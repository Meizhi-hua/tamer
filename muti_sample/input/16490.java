public class WEmbeddedFrame extends EmbeddedFrame {
    static {
        initIDs();
    }
    private long handle;
    private int bandWidth = 0;
    private int bandHeight = 0;
    private int imgWid = 0;
    private int imgHgt = 0;
    private static int pScale = 0;
    private static final int MAX_BAND_SIZE = (1024*30);
    private static String printScale = (String) java.security.AccessController
       .doPrivileged(new GetPropertyAction("sun.java2d.print.pluginscalefactor"));
    public WEmbeddedFrame() {
        this((long)0);
    }
    @Deprecated
    public WEmbeddedFrame(int handle) {
        this((long)handle);
    }
    public WEmbeddedFrame(long handle) {
        this.handle = handle;
        if (handle != 0) {
            addNotify();
            show();
        }
    }
    public void addNotify() {
        if (getPeer() == null) {
            WToolkit toolkit = (WToolkit)Toolkit.getDefaultToolkit();
            setPeer(toolkit.createEmbeddedFrame(this));
        }
        super.addNotify();
    }
    public long getEmbedderHandle() {
        return handle;
    }
    void print(long hdc) {
        BufferedImage bandImage = null;
        int xscale = 1;
        int yscale = 1;
        if (isPrinterDC(hdc)) {
            xscale = yscale = getPrintScaleFactor();
        }
        int frameHeight = getHeight();
        if (bandImage == null) {
            bandWidth = getWidth();
            if (bandWidth % 4 != 0) {
                bandWidth += (4 - (bandWidth % 4));
            }
            if (bandWidth <= 0) {
                return;
            }
            bandHeight = Math.min(MAX_BAND_SIZE/bandWidth, frameHeight);
            imgWid = (int)(bandWidth * xscale);
            imgHgt = (int)(bandHeight * yscale);
            bandImage = new BufferedImage(imgWid, imgHgt,
                                          BufferedImage.TYPE_3BYTE_BGR);
        }
        Graphics clearGraphics = bandImage.getGraphics();
        clearGraphics.setColor(Color.white);
        Graphics2D g2d = (Graphics2D)bandImage.getGraphics();
        g2d.translate(0, imgHgt);
        g2d.scale(xscale, -yscale);
        ByteInterleavedRaster ras = (ByteInterleavedRaster)bandImage.getRaster();
        byte[] data = ras.getDataStorage();
        for (int bandTop = 0; bandTop < frameHeight; bandTop += bandHeight) {
            clearGraphics.fillRect(0, 0, bandWidth, bandHeight);
            printComponents(g2d);
            int imageOffset =0;
            int currBandHeight = bandHeight;
            int currImgHeight = imgHgt;
            if ((bandTop+bandHeight) > frameHeight) {
                currBandHeight = frameHeight - bandTop;
                currImgHeight = (int)(currBandHeight*yscale);
                imageOffset = imgWid*(imgHgt-currImgHeight)*3;
            }
            printBand(hdc, data, imageOffset,
                      0, 0, imgWid, currImgHeight,
                      0, bandTop, bandWidth, currBandHeight);
            g2d.translate(0, -bandHeight);
        }
    }
    protected static int getPrintScaleFactor() {
        if (pScale != 0)
            return pScale;
        if (printScale == null) {
            printScale = (String) java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction() {
                    public Object run() {
                        return System.getenv("JAVA2D_PLUGIN_PRINT_SCALE");
                    }
                }
            );
        }
        int default_printDC_scale = 4;
        int scale = default_printDC_scale;
        if (printScale != null) {
            try {
                scale = Integer.parseInt(printScale);
                if (scale > 8 || scale < 1) {
                    scale = default_printDC_scale;
                }
            } catch (NumberFormatException nfe) {
            }
        }
        pScale = scale;
        return pScale;
    }
    protected native boolean isPrinterDC(long hdc);
    protected native void printBand(long hdc, byte[] data, int offset,
                                    int sx, int sy, int swidth, int sheight,
                                    int dx, int dy, int dwidth, int dheight);
    private static native void initIDs();
    public void activateEmbeddingTopLevel() {
    }
    public void synthesizeWindowActivation(final boolean doActivate) {
        if (!doActivate || EventQueue.isDispatchThread()) {
            ((WEmbeddedFramePeer)getPeer()).synthesizeWmActivate(doActivate);
        } else {
            EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        ((WEmbeddedFramePeer)getPeer()).synthesizeWmActivate(true);
                    }
                });
        }
    }
    public void registerAccelerator(AWTKeyStroke stroke) {}
    public void unregisterAccelerator(AWTKeyStroke stroke) {}
    public void notifyModalBlocked(Dialog blocker, boolean blocked) {
        try {
            ComponentPeer thisPeer = (ComponentPeer)WToolkit.targetToPeer(this);
            ComponentPeer blockerPeer = (ComponentPeer)WToolkit.targetToPeer(blocker);
            notifyModalBlockedImpl((WEmbeddedFramePeer)thisPeer,
                                   (WWindowPeer)blockerPeer, blocked);
        } catch (Exception z) {
            z.printStackTrace(System.err);
        }
    }
    native void notifyModalBlockedImpl(WEmbeddedFramePeer peer, WWindowPeer blockerPeer, boolean blocked);
}
