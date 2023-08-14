public class MaskFill extends GraphicsPrimitive
{
    public static final String methodSignature = "MaskFill(...)".toString();
    public static final String fillPgramSignature =
        "FillAAPgram(...)".toString();
    public static final String drawPgramSignature =
        "DrawAAPgram(...)".toString();
    public static final int primTypeID = makePrimTypeID();
    private static RenderCache fillcache = new RenderCache(10);
    public static MaskFill locate(SurfaceType srctype,
                                  CompositeType comptype,
                                  SurfaceType dsttype)
    {
        return (MaskFill)
            GraphicsPrimitiveMgr.locate(primTypeID,
                                        srctype, comptype, dsttype);
    }
    public static MaskFill locatePrim(SurfaceType srctype,
                                      CompositeType comptype,
                                      SurfaceType dsttype)
    {
        return (MaskFill)
            GraphicsPrimitiveMgr.locatePrim(primTypeID,
                                            srctype, comptype, dsttype);
    }
    public static MaskFill getFromCache(SurfaceType src,
                                        CompositeType comp,
                                        SurfaceType dst)
    {
        Object o = fillcache.get(src, comp, dst);
        if (o != null) {
            return (MaskFill) o;
        }
        MaskFill fill = locatePrim(src, comp, dst);
        if (fill != null) {
            fillcache.put(src, comp, dst, fill);
        }
        return fill;
    }
    protected MaskFill(String alternateSignature,
                       SurfaceType srctype,
                       CompositeType comptype,
                       SurfaceType dsttype)
    {
        super(alternateSignature, primTypeID, srctype, comptype, dsttype);
    }
    protected MaskFill(SurfaceType srctype,
                       CompositeType comptype,
                       SurfaceType dsttype)
    {
        super(methodSignature, primTypeID, srctype, comptype, dsttype);
    }
    public MaskFill(long pNativePrim,
                    SurfaceType srctype,
                    CompositeType comptype,
                    SurfaceType dsttype)
    {
        super(pNativePrim, methodSignature, primTypeID, srctype, comptype, dsttype);
    }
    public native void MaskFill(SunGraphics2D sg2d, SurfaceData sData,
                                Composite comp,
                                int x, int y, int w, int h,
                                byte[] mask, int maskoff, int maskscan);
    public native void FillAAPgram(SunGraphics2D sg2d, SurfaceData sData,
                                   Composite comp,
                                   double x, double y,
                                   double dx1, double dy1,
                                   double dx2, double dy2);
    public native void DrawAAPgram(SunGraphics2D sg2d, SurfaceData sData,
                                   Composite comp,
                                   double x, double y,
                                   double dx1, double dy1,
                                   double dx2, double dy2,
                                   double lw1, double lw2);
    public boolean canDoParallelograms() {
        return (getNativePrim() != 0);
    }
    static {
        GraphicsPrimitiveMgr.registerGeneral(new MaskFill(null, null, null));
    }
    public GraphicsPrimitive makePrimitive(SurfaceType srctype,
                                           CompositeType comptype,
                                           SurfaceType dsttype)
    {
        if (SurfaceType.OpaqueColor.equals(srctype) ||
            SurfaceType.AnyColor.equals(srctype))
        {
            if (CompositeType.Xor.equals(comptype)) {
                throw new InternalError("Cannot construct MaskFill for " +
                                        "XOR mode");
            } else {
                return new General(srctype, comptype, dsttype);
            }
        } else {
            throw new InternalError("MaskFill can only fill with colors");
        }
    }
    private static class General extends MaskFill {
        FillRect fillop;
        MaskBlit maskop;
        public General(SurfaceType srctype,
                       CompositeType comptype,
                       SurfaceType dsttype)
        {
            super(srctype, comptype, dsttype);
            fillop = FillRect.locate(srctype,
                                     CompositeType.SrcNoEa,
                                     SurfaceType.IntArgb);
            maskop = MaskBlit.locate(SurfaceType.IntArgb, comptype, dsttype);
        }
        public void MaskFill(SunGraphics2D sg2d,
                             SurfaceData sData,
                             Composite comp,
                             int x, int y, int w, int h,
                             byte mask[], int offset, int scan)
        {
            BufferedImage dstBI =
                new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            SurfaceData tmpData = BufImgSurfaceData.createData(dstBI);
            int pixel = sg2d.pixel;
            sg2d.pixel = tmpData.pixelFor(sg2d.getColor());
            fillop.FillRect(sg2d, tmpData, 0, 0, w, h);
            sg2d.pixel = pixel;
            maskop.MaskBlit(tmpData, sData, comp, null,
                            0, 0, x, y, w, h,
                            mask, offset, scan);
        }
    }
    public GraphicsPrimitive traceWrap() {
        return new TraceMaskFill(this);
    }
    private static class TraceMaskFill extends MaskFill {
        MaskFill target;
        MaskFill fillPgramTarget;
        MaskFill drawPgramTarget;
        public TraceMaskFill(MaskFill target) {
            super(target.getSourceType(),
                  target.getCompositeType(),
                  target.getDestType());
            this.target = target;
            this.fillPgramTarget = new MaskFill(fillPgramSignature,
                                                target.getSourceType(),
                                                target.getCompositeType(),
                                                target.getDestType());
            this.drawPgramTarget = new MaskFill(drawPgramSignature,
                                                target.getSourceType(),
                                                target.getCompositeType(),
                                                target.getDestType());
        }
        public GraphicsPrimitive traceWrap() {
            return this;
        }
        public void MaskFill(SunGraphics2D sg2d, SurfaceData sData,
                             Composite comp,
                             int x, int y, int w, int h,
                             byte[] mask, int maskoff, int maskscan)
        {
            tracePrimitive(target);
            target.MaskFill(sg2d, sData, comp, x, y, w, h,
                            mask, maskoff, maskscan);
        }
        public void FillAAPgram(SunGraphics2D sg2d, SurfaceData sData,
                                Composite comp,
                                double x, double y,
                                double dx1, double dy1,
                                double dx2, double dy2)
        {
            tracePrimitive(fillPgramTarget);
            target.FillAAPgram(sg2d, sData, comp,
                               x, y, dx1, dy1, dx2, dy2);
        }
        public void DrawAAPgram(SunGraphics2D sg2d, SurfaceData sData,
                                Composite comp,
                                double x, double y,
                                double dx1, double dy1,
                                double dx2, double dy2,
                                double lw1, double lw2)
        {
            tracePrimitive(drawPgramTarget);
            target.DrawAAPgram(sg2d, sData, comp,
                               x, y, dx1, dy1, dx2, dy2, lw1, lw2);
        }
        public boolean canDoParallelograms() {
            return target.canDoParallelograms();
        }
    }
}