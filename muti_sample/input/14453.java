public class GDIWindowSurfaceData extends SurfaceData {
    private WComponentPeer peer;
    private Win32GraphicsConfig graphicsConfig;
    private RenderLoops solidloops;
    public static final String
        DESC_GDI                = "GDI";
    public static final SurfaceType AnyGdi =
        SurfaceType.IntRgb.deriveSubType(DESC_GDI);
    public static final SurfaceType IntRgbGdi =
        SurfaceType.IntRgb.deriveSubType(DESC_GDI);
    public static final SurfaceType Ushort565RgbGdi =
        SurfaceType.Ushort565Rgb.deriveSubType(DESC_GDI);
    public static final SurfaceType Ushort555RgbGdi =
        SurfaceType.Ushort555Rgb.deriveSubType(DESC_GDI);
    public static final SurfaceType ThreeByteBgrGdi =
        SurfaceType.ThreeByteBgr.deriveSubType(DESC_GDI);
    private static native void initIDs(Class xorComp);
    static {
        initIDs(XORComposite.class);
        if (WindowsFlags.isGdiBlitEnabled()) {
            GDIBlitLoops.register();
        }
    }
    public static SurfaceType getSurfaceType(ColorModel cm) {
        switch (cm.getPixelSize()) {
        case 32:
        case 24:
            if (cm instanceof DirectColorModel) {
                if (((DirectColorModel)cm).getRedMask() == 0xff0000) {
                    return IntRgbGdi;
                } else {
                    return SurfaceType.IntRgbx;
                }
            } else {
                return ThreeByteBgrGdi;
            }
        case 15:
            return Ushort555RgbGdi;
        case 16:
            if ((cm instanceof DirectColorModel) &&
                (((DirectColorModel)cm).getBlueMask() == 0x3e))
            {
                return SurfaceType.Ushort555Rgbx;
            } else {
                return Ushort565RgbGdi;
            }
        case 8:
            if (cm.getColorSpace().getType() == ColorSpace.TYPE_GRAY &&
                cm instanceof ComponentColorModel) {
                return SurfaceType.ByteGray;
            } else if (cm instanceof IndexColorModel &&
                       isOpaqueGray((IndexColorModel)cm)) {
                return SurfaceType.Index8Gray;
            } else {
                return SurfaceType.ByteIndexedOpaque;
            }
        default:
            throw new sun.java2d.InvalidPipeException("Unsupported bit " +
                                                      "depth: " +
                                                      cm.getPixelSize());
        }
    }
    public static GDIWindowSurfaceData createData(WComponentPeer peer) {
        SurfaceType sType = getSurfaceType(peer.getDeviceColorModel());
        return new GDIWindowSurfaceData(peer, sType);
    }
    @Override
    public SurfaceDataProxy makeProxyFor(SurfaceData srcData) {
        return SurfaceDataProxy.UNCACHED;
    }
    public Raster getRaster(int x, int y, int w, int h) {
        throw new InternalError("not implemented yet");
    }
    protected static GDIRenderer gdiPipe;
    protected static PixelToShapeConverter gdiTxPipe;
    static {
        gdiPipe = new GDIRenderer();
        if (GraphicsPrimitive.tracingEnabled()) {
            gdiPipe = gdiPipe.traceWrap();
        }
        gdiTxPipe = new PixelToShapeConverter(gdiPipe);
    }
    public void validatePipe(SunGraphics2D sg2d) {
        if (sg2d.antialiasHint != SunHints.INTVAL_ANTIALIAS_ON &&
            sg2d.paintState <= sg2d.PAINT_ALPHACOLOR &&
            (sg2d.compositeState <= sg2d.COMP_ISCOPY ||
             sg2d.compositeState == sg2d.COMP_XOR))
        {
            if (sg2d.clipState == sg2d.CLIP_SHAPE) {
                super.validatePipe(sg2d);
            } else {
                switch (sg2d.textAntialiasHint) {
                case SunHints.INTVAL_TEXT_ANTIALIAS_DEFAULT:
                case SunHints.INTVAL_TEXT_ANTIALIAS_OFF:
                    sg2d.textpipe = solidTextRenderer;
                    break;
                case SunHints.INTVAL_TEXT_ANTIALIAS_ON:
                    sg2d.textpipe = aaTextRenderer;
                    break;
                default:
                    switch (sg2d.getFontInfo().aaHint) {
                    case SunHints.INTVAL_TEXT_ANTIALIAS_LCD_HRGB:
                    case SunHints.INTVAL_TEXT_ANTIALIAS_LCD_VRGB:
                        sg2d.textpipe = lcdTextRenderer;
                        break;
                    case SunHints.INTVAL_TEXT_ANTIALIAS_ON:
                        sg2d.textpipe = aaTextRenderer;
                        break;
                    default:
                        sg2d.textpipe = solidTextRenderer;
                    }
                }
            }
            sg2d.imagepipe = imagepipe;
            if (sg2d.transformState >= sg2d.TRANSFORM_TRANSLATESCALE) {
                sg2d.drawpipe = gdiTxPipe;
                sg2d.fillpipe = gdiTxPipe;
            } else if (sg2d.strokeState != sg2d.STROKE_THIN){
                sg2d.drawpipe = gdiTxPipe;
                sg2d.fillpipe = gdiPipe;
            } else {
                sg2d.drawpipe = gdiPipe;
                sg2d.fillpipe = gdiPipe;
            }
            sg2d.shapepipe = gdiPipe;
            if (sg2d.loops == null) {
                sg2d.loops = getRenderLoops(sg2d);
            }
        } else {
            super.validatePipe(sg2d);
        }
    }
    public RenderLoops getRenderLoops(SunGraphics2D sg2d) {
        if (sg2d.paintState <= sg2d.PAINT_ALPHACOLOR &&
            sg2d.compositeState <= sg2d.COMP_ISCOPY)
        {
            return solidloops;
        }
        return super.getRenderLoops(sg2d);
    }
    public GraphicsConfiguration getDeviceConfiguration() {
        return graphicsConfig;
    }
    private native void initOps(WComponentPeer peer, int depth, int redMask,
                                int greenMask, int blueMask, int screen);
    private GDIWindowSurfaceData(WComponentPeer peer, SurfaceType sType) {
        super(sType, peer.getDeviceColorModel());
        ColorModel cm = peer.getDeviceColorModel();
        this.peer = peer;
        int rMask = 0, gMask = 0, bMask = 0;
        int depth;
        switch (cm.getPixelSize()) {
        case 32:
        case 24:
            if (cm instanceof DirectColorModel) {
                depth = 32;
            } else {
                depth = 24;
            }
            break;
        default:
            depth = cm.getPixelSize();
        }
        if (cm instanceof DirectColorModel) {
            DirectColorModel dcm = (DirectColorModel)cm;
            rMask = dcm.getRedMask();
            gMask = dcm.getGreenMask();
            bMask = dcm.getBlueMask();
        }
        this.graphicsConfig =
            (Win32GraphicsConfig) peer.getGraphicsConfiguration();
        this.solidloops = graphicsConfig.getSolidLoops(sType);
        Win32GraphicsDevice gd =
            (Win32GraphicsDevice)graphicsConfig.getDevice();
        initOps(peer, depth, rMask, gMask, bMask, gd.getScreen());
        setBlitProxyKey(graphicsConfig.getProxyKey());
    }
    @Override
    public SurfaceData getReplacement() {
        ScreenUpdateManager mgr = ScreenUpdateManager.getInstance();
        return mgr.getReplacementScreenSurface(peer, this);
    }
    public Rectangle getBounds() {
        Rectangle r = peer.getBounds();
        r.x = r.y = 0;
        return r;
    }
    public boolean copyArea(SunGraphics2D sg2d,
                            int x, int y, int w, int h, int dx, int dy)
    {
        CompositeType comptype = sg2d.imageComp;
        if (sg2d.transformState < sg2d.TRANSFORM_TRANSLATESCALE &&
            sg2d.clipState != sg2d.CLIP_SHAPE &&
            (CompositeType.SrcOverNoEa.equals(comptype) ||
             CompositeType.SrcNoEa.equals(comptype)))
        {
            x += sg2d.transX;
            y += sg2d.transY;
            int dstx1 = x + dx;
            int dsty1 = y + dy;
            int dstx2 = dstx1 + w;
            int dsty2 = dsty1 + h;
            Region clip = sg2d.getCompClip();
            if (dstx1 < clip.getLoX()) dstx1 = clip.getLoX();
            if (dsty1 < clip.getLoY()) dsty1 = clip.getLoY();
            if (dstx2 > clip.getHiX()) dstx2 = clip.getHiX();
            if (dsty2 > clip.getHiY()) dsty2 = clip.getHiY();
            if (dstx1 < dstx2 && dsty1 < dsty2) {
                gdiPipe.devCopyArea(this, dstx1 - dx, dsty1 - dy,
                                    dx, dy,
                                    dstx2 - dstx1, dsty2 - dsty1);
            }
            return true;
        }
        return false;
    }
    private native void invalidateSD();
    @Override
    public void invalidate() {
        if (isValid()) {
            invalidateSD();
            super.invalidate();
        }
    }
    @Override
    public Object getDestination() {
        return peer.getTarget();
    }
    public WComponentPeer getPeer() {
        return peer;
    }
}
