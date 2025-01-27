public class BlitBg extends GraphicsPrimitive
{
    public static final String methodSignature = "BlitBg(...)".toString();
    public static final int primTypeID = makePrimTypeID();
    private static RenderCache blitcache = new RenderCache(20);
    public static BlitBg locate(SurfaceType srctype,
                                CompositeType comptype,
                                SurfaceType dsttype)
    {
        return (BlitBg)
            GraphicsPrimitiveMgr.locate(primTypeID,
                                        srctype, comptype, dsttype);
    }
    public static BlitBg getFromCache(SurfaceType src,
                                      CompositeType comp,
                                      SurfaceType dst)
    {
        Object o = blitcache.get(src, comp, dst);
        if (o != null) {
            return (BlitBg) o;
        }
        BlitBg blit = locate(src, comp, dst);
        if (blit == null) {
            System.out.println("blitbg loop not found for:");
            System.out.println("src:  "+src);
            System.out.println("comp: "+comp);
            System.out.println("dst:  "+dst);
        } else {
            blitcache.put(src, comp, dst, blit);
        }
        return blit;
    }
    protected BlitBg(SurfaceType srctype,
                     CompositeType comptype,
                     SurfaceType dsttype)
    {
        super(methodSignature, primTypeID, srctype, comptype, dsttype);
    }
    public BlitBg(long pNativePrim,
                  SurfaceType srctype,
                  CompositeType comptype,
                  SurfaceType dsttype)
    {
        super(pNativePrim, methodSignature, primTypeID, srctype, comptype, dsttype);
    }
    public native void BlitBg(SurfaceData src, SurfaceData dst,
                              Composite comp, Region clip,
                              int bgColor,
                              int srcx, int srcy,
                              int dstx, int dsty,
                              int width, int height);
    static {
        GraphicsPrimitiveMgr.registerGeneral(new BlitBg(null, null, null));
    }
    public GraphicsPrimitive makePrimitive(SurfaceType srctype,
                                           CompositeType comptype,
                                           SurfaceType dsttype)
    {
        return new General(srctype, comptype, dsttype);
    }
    private static class General extends BlitBg {
        CompositeType compositeType;
        public General(SurfaceType srctype,
                       CompositeType comptype,
                       SurfaceType dsttype)
        {
            super(srctype, comptype, dsttype);
            compositeType = comptype;
        }
        @Override
        public void BlitBg(SurfaceData srcData,
                           SurfaceData dstData,
                           Composite comp,
                           Region clip,
                           int bgArgb,
                           int srcx, int srcy,
                           int dstx, int dsty,
                           int width, int height)
        {
            ColorModel dstModel = dstData.getColorModel();
            boolean bgHasAlpha = (bgArgb >>> 24) != 0xff;
            if (!dstModel.hasAlpha() && bgHasAlpha) {
                dstModel = ColorModel.getRGBdefault();
            }
            WritableRaster wr =
                dstModel.createCompatibleWritableRaster(width, height);
            boolean isPremult = dstModel.isAlphaPremultiplied();
            BufferedImage bimg =
                new BufferedImage(dstModel, wr, isPremult, null);
            SurfaceData tmpData = BufImgSurfaceData.createData(bimg);
            Color bgColor = new Color(bgArgb, bgHasAlpha);
            SunGraphics2D sg2d = new SunGraphics2D(tmpData, bgColor, bgColor,
                                                   defaultFont);
            FillRect fillop = FillRect.locate(SurfaceType.AnyColor,
                                              CompositeType.SrcNoEa,
                                              tmpData.getSurfaceType());
            Blit combineop = Blit.getFromCache(srcData.getSurfaceType(),
                                               CompositeType.SrcOverNoEa,
                                               tmpData.getSurfaceType());
            Blit blitop = Blit.getFromCache(tmpData.getSurfaceType(), compositeType,
                                            dstData.getSurfaceType());
            fillop.FillRect(sg2d, tmpData, 0, 0, width, height);
            combineop.Blit(srcData, tmpData, AlphaComposite.SrcOver, null,
                           srcx, srcy, 0, 0, width, height);
            blitop.Blit(tmpData, dstData, comp, clip,
                        0, 0, dstx, dsty, width, height);
        }
        private static Font defaultFont = new Font("Dialog", Font.PLAIN, 12);
    }
    public GraphicsPrimitive traceWrap() {
        return new TraceBlitBg(this);
    }
    private static class TraceBlitBg extends BlitBg {
        BlitBg target;
        public TraceBlitBg(BlitBg target) {
            super(target.getSourceType(),
                  target.getCompositeType(),
                  target.getDestType());
            this.target = target;
        }
        public GraphicsPrimitive traceWrap() {
            return this;
        }
        @Override
        public void BlitBg(SurfaceData src, SurfaceData dst,
                           Composite comp, Region clip,
                           int bgColor,
                           int srcx, int srcy, int dstx, int dsty,
                           int width, int height)
        {
            tracePrimitive(target);
            target.BlitBg(src, dst, comp, clip, bgColor,
                          srcx, srcy, dstx, dsty, width, height);
        }
    }
}
