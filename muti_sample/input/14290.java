class OGLBlitLoops {
    static void register() {
        Blit blitIntArgbPreToSurface =
            new OGLSwToSurfaceBlit(SurfaceType.IntArgbPre,
                                   OGLSurfaceData.PF_INT_ARGB_PRE);
        Blit blitIntArgbPreToTexture =
            new OGLSwToTextureBlit(SurfaceType.IntArgbPre,
                                   OGLSurfaceData.PF_INT_ARGB_PRE);
        GraphicsPrimitive[] primitives = {
            new OGLSurfaceToSurfaceBlit(),
            new OGLSurfaceToSurfaceScale(),
            new OGLSurfaceToSurfaceTransform(),
            new OGLRTTSurfaceToSurfaceBlit(),
            new OGLRTTSurfaceToSurfaceScale(),
            new OGLRTTSurfaceToSurfaceTransform(),
            new OGLSurfaceToSwBlit(SurfaceType.IntArgb,
                                   OGLSurfaceData.PF_INT_ARGB),
            blitIntArgbPreToSurface,
            new OGLSwToSurfaceBlit(SurfaceType.IntRgb,
                                   OGLSurfaceData.PF_INT_RGB),
            new OGLSwToSurfaceBlit(SurfaceType.IntRgbx,
                                   OGLSurfaceData.PF_INT_RGBX),
            new OGLSwToSurfaceBlit(SurfaceType.IntBgr,
                                   OGLSurfaceData.PF_INT_BGR),
            new OGLSwToSurfaceBlit(SurfaceType.IntBgrx,
                                   OGLSurfaceData.PF_INT_BGRX),
            new OGLSwToSurfaceBlit(SurfaceType.ThreeByteBgr,
                                   OGLSurfaceData.PF_3BYTE_BGR),
            new OGLSwToSurfaceBlit(SurfaceType.Ushort565Rgb,
                                   OGLSurfaceData.PF_USHORT_565_RGB),
            new OGLSwToSurfaceBlit(SurfaceType.Ushort555Rgb,
                                   OGLSurfaceData.PF_USHORT_555_RGB),
            new OGLSwToSurfaceBlit(SurfaceType.Ushort555Rgbx,
                                   OGLSurfaceData.PF_USHORT_555_RGBX),
            new OGLSwToSurfaceBlit(SurfaceType.ByteGray,
                                   OGLSurfaceData.PF_BYTE_GRAY),
            new OGLSwToSurfaceBlit(SurfaceType.UshortGray,
                                   OGLSurfaceData.PF_USHORT_GRAY),
            new OGLGeneralBlit(OGLSurfaceData.OpenGLSurface,
                               CompositeType.AnyAlpha,
                               blitIntArgbPreToSurface),
            new OGLSwToSurfaceScale(SurfaceType.IntRgb,
                                    OGLSurfaceData.PF_INT_RGB),
            new OGLSwToSurfaceScale(SurfaceType.IntRgbx,
                                    OGLSurfaceData.PF_INT_RGBX),
            new OGLSwToSurfaceScale(SurfaceType.IntBgr,
                                    OGLSurfaceData.PF_INT_BGR),
            new OGLSwToSurfaceScale(SurfaceType.IntBgrx,
                                    OGLSurfaceData.PF_INT_BGRX),
            new OGLSwToSurfaceScale(SurfaceType.ThreeByteBgr,
                                    OGLSurfaceData.PF_3BYTE_BGR),
            new OGLSwToSurfaceScale(SurfaceType.Ushort565Rgb,
                                    OGLSurfaceData.PF_USHORT_565_RGB),
            new OGLSwToSurfaceScale(SurfaceType.Ushort555Rgb,
                                    OGLSurfaceData.PF_USHORT_555_RGB),
            new OGLSwToSurfaceScale(SurfaceType.Ushort555Rgbx,
                                    OGLSurfaceData.PF_USHORT_555_RGBX),
            new OGLSwToSurfaceScale(SurfaceType.ByteGray,
                                    OGLSurfaceData.PF_BYTE_GRAY),
            new OGLSwToSurfaceScale(SurfaceType.UshortGray,
                                    OGLSurfaceData.PF_USHORT_GRAY),
            new OGLSwToSurfaceScale(SurfaceType.IntArgbPre,
                                    OGLSurfaceData.PF_INT_ARGB_PRE),
            new OGLSwToSurfaceTransform(SurfaceType.IntRgb,
                                        OGLSurfaceData.PF_INT_RGB),
            new OGLSwToSurfaceTransform(SurfaceType.IntRgbx,
                                        OGLSurfaceData.PF_INT_RGBX),
            new OGLSwToSurfaceTransform(SurfaceType.IntBgr,
                                        OGLSurfaceData.PF_INT_BGR),
            new OGLSwToSurfaceTransform(SurfaceType.IntBgrx,
                                        OGLSurfaceData.PF_INT_BGRX),
            new OGLSwToSurfaceTransform(SurfaceType.ThreeByteBgr,
                                        OGLSurfaceData.PF_3BYTE_BGR),
            new OGLSwToSurfaceTransform(SurfaceType.Ushort565Rgb,
                                        OGLSurfaceData.PF_USHORT_565_RGB),
            new OGLSwToSurfaceTransform(SurfaceType.Ushort555Rgb,
                                        OGLSurfaceData.PF_USHORT_555_RGB),
            new OGLSwToSurfaceTransform(SurfaceType.Ushort555Rgbx,
                                        OGLSurfaceData.PF_USHORT_555_RGBX),
            new OGLSwToSurfaceTransform(SurfaceType.ByteGray,
                                        OGLSurfaceData.PF_BYTE_GRAY),
            new OGLSwToSurfaceTransform(SurfaceType.UshortGray,
                                        OGLSurfaceData.PF_USHORT_GRAY),
            new OGLSwToSurfaceTransform(SurfaceType.IntArgbPre,
                                        OGLSurfaceData.PF_INT_ARGB_PRE),
            new OGLTextureToSurfaceBlit(),
            new OGLTextureToSurfaceScale(),
            new OGLTextureToSurfaceTransform(),
            blitIntArgbPreToTexture,
            new OGLSwToTextureBlit(SurfaceType.IntRgb,
                                   OGLSurfaceData.PF_INT_RGB),
            new OGLSwToTextureBlit(SurfaceType.IntRgbx,
                                   OGLSurfaceData.PF_INT_RGBX),
            new OGLSwToTextureBlit(SurfaceType.IntBgr,
                                   OGLSurfaceData.PF_INT_BGR),
            new OGLSwToTextureBlit(SurfaceType.IntBgrx,
                                   OGLSurfaceData.PF_INT_BGRX),
            new OGLSwToTextureBlit(SurfaceType.ThreeByteBgr,
                                   OGLSurfaceData.PF_3BYTE_BGR),
            new OGLSwToTextureBlit(SurfaceType.Ushort565Rgb,
                                   OGLSurfaceData.PF_USHORT_565_RGB),
            new OGLSwToTextureBlit(SurfaceType.Ushort555Rgb,
                                   OGLSurfaceData.PF_USHORT_555_RGB),
            new OGLSwToTextureBlit(SurfaceType.Ushort555Rgbx,
                                   OGLSurfaceData.PF_USHORT_555_RGBX),
            new OGLSwToTextureBlit(SurfaceType.ByteGray,
                                   OGLSurfaceData.PF_BYTE_GRAY),
            new OGLSwToTextureBlit(SurfaceType.UshortGray,
                                   OGLSurfaceData.PF_USHORT_GRAY),
            new OGLGeneralBlit(OGLSurfaceData.OpenGLTexture,
                               CompositeType.SrcNoEa,
                               blitIntArgbPreToTexture),
        };
        GraphicsPrimitiveMgr.register(primitives);
    }
    private static final int OFFSET_SRCTYPE = 16;
    private static final int OFFSET_HINT    =  8;
    private static final int OFFSET_TEXTURE =  3;
    private static final int OFFSET_RTT     =  2;
    private static final int OFFSET_XFORM   =  1;
    private static final int OFFSET_ISOBLIT =  0;
    private static int createPackedParams(boolean isoblit, boolean texture,
                                          boolean rtt, boolean xform,
                                          int hint, int srctype)
    {
        return
            ((srctype           << OFFSET_SRCTYPE) |
             (hint              << OFFSET_HINT   ) |
             ((texture ? 1 : 0) << OFFSET_TEXTURE) |
             ((rtt     ? 1 : 0) << OFFSET_RTT    ) |
             ((xform   ? 1 : 0) << OFFSET_XFORM  ) |
             ((isoblit ? 1 : 0) << OFFSET_ISOBLIT));
    }
    private static void enqueueBlit(RenderQueue rq,
                                    SurfaceData src, SurfaceData dst,
                                    int packedParams,
                                    int sx1, int sy1,
                                    int sx2, int sy2,
                                    double dx1, double dy1,
                                    double dx2, double dy2)
    {
        RenderBuffer buf = rq.getBuffer();
        rq.ensureCapacityAndAlignment(72, 24);
        buf.putInt(BLIT);
        buf.putInt(packedParams);
        buf.putInt(sx1).putInt(sy1);
        buf.putInt(sx2).putInt(sy2);
        buf.putDouble(dx1).putDouble(dy1);
        buf.putDouble(dx2).putDouble(dy2);
        buf.putLong(src.getNativeOps());
        buf.putLong(dst.getNativeOps());
    }
    static void Blit(SurfaceData srcData, SurfaceData dstData,
                     Composite comp, Region clip,
                     AffineTransform xform, int hint,
                     int sx1, int sy1,
                     int sx2, int sy2,
                     double dx1, double dy1,
                     double dx2, double dy2,
                     int srctype, boolean texture)
    {
        int ctxflags = 0;
        if (srcData.getTransparency() == Transparency.OPAQUE) {
            ctxflags |= OGLContext.SRC_IS_OPAQUE;
        }
        OGLRenderQueue rq = OGLRenderQueue.getInstance();
        rq.lock();
        try {
            rq.addReference(srcData);
            OGLSurfaceData oglDst = (OGLSurfaceData)dstData;
            if (texture) {
                OGLGraphicsConfig gc = oglDst.getOGLGraphicsConfig();
                OGLContext.setScratchSurface(gc);
            } else {
                OGLContext.validateContext(oglDst, oglDst,
                                           clip, comp, xform, null, null,
                                           ctxflags);
            }
            int packedParams = createPackedParams(false, texture,
                                                  false, xform != null,
                                                  hint, srctype);
            enqueueBlit(rq, srcData, dstData,
                        packedParams,
                        sx1, sy1, sx2, sy2,
                        dx1, dy1, dx2, dy2);
            rq.flushNow();
        } finally {
            rq.unlock();
        }
    }
    static void IsoBlit(SurfaceData srcData, SurfaceData dstData,
                        BufferedImage srcImg, BufferedImageOp biop,
                        Composite comp, Region clip,
                        AffineTransform xform, int hint,
                        int sx1, int sy1,
                        int sx2, int sy2,
                        double dx1, double dy1,
                        double dx2, double dy2,
                        boolean texture)
    {
        int ctxflags = 0;
        if (srcData.getTransparency() == Transparency.OPAQUE) {
            ctxflags |= OGLContext.SRC_IS_OPAQUE;
        }
        OGLRenderQueue rq = OGLRenderQueue.getInstance();
        rq.lock();
        try {
            OGLSurfaceData oglSrc = (OGLSurfaceData)srcData;
            OGLSurfaceData oglDst = (OGLSurfaceData)dstData;
            int srctype = oglSrc.getType();
            boolean rtt;
            OGLSurfaceData srcCtxData;
            if (srctype == OGLSurfaceData.TEXTURE) {
                rtt = false;
                srcCtxData = oglDst;
            } else {
                rtt = true;
                if (srctype == OGLSurfaceData.FBOBJECT) {
                    srcCtxData = oglDst;
                } else {
                    srcCtxData = oglSrc;
                }
            }
            OGLContext.validateContext(srcCtxData, oglDst,
                                       clip, comp, xform, null, null,
                                       ctxflags);
            if (biop != null) {
                OGLBufImgOps.enableBufImgOp(rq, oglSrc, srcImg, biop);
            }
            int packedParams = createPackedParams(true, texture,
                                                  rtt, xform != null,
                                                  hint, 0 );
            enqueueBlit(rq, srcData, dstData,
                        packedParams,
                        sx1, sy1, sx2, sy2,
                        dx1, dy1, dx2, dy2);
            if (biop != null) {
                OGLBufImgOps.disableBufImgOp(rq, biop);
            }
            if (rtt && (oglDst.getType() == OGLSurfaceData.WINDOW)) {
                rq.flushNow();
            }
        } finally {
            rq.unlock();
        }
    }
}
class OGLSurfaceToSurfaceBlit extends Blit {
    OGLSurfaceToSurfaceBlit() {
        super(OGLSurfaceData.OpenGLSurface,
              CompositeType.AnyAlpha,
              OGLSurfaceData.OpenGLSurface);
    }
    public void Blit(SurfaceData src, SurfaceData dst,
                     Composite comp, Region clip,
                     int sx, int sy, int dx, int dy, int w, int h)
    {
        OGLBlitLoops.IsoBlit(src, dst,
                             null, null,
                             comp, clip, null,
                             AffineTransformOp.TYPE_NEAREST_NEIGHBOR,
                             sx, sy, sx+w, sy+h,
                             dx, dy, dx+w, dy+h,
                             false);
    }
}
class OGLSurfaceToSurfaceScale extends ScaledBlit {
    OGLSurfaceToSurfaceScale() {
        super(OGLSurfaceData.OpenGLSurface,
              CompositeType.AnyAlpha,
              OGLSurfaceData.OpenGLSurface);
    }
    public void Scale(SurfaceData src, SurfaceData dst,
                      Composite comp, Region clip,
                      int sx1, int sy1,
                      int sx2, int sy2,
                      double dx1, double dy1,
                      double dx2, double dy2)
    {
        OGLBlitLoops.IsoBlit(src, dst,
                             null, null,
                             comp, clip, null,
                             AffineTransformOp.TYPE_NEAREST_NEIGHBOR,
                             sx1, sy1, sx2, sy2,
                             dx1, dy1, dx2, dy2,
                             false);
    }
}
class OGLSurfaceToSurfaceTransform extends TransformBlit {
    OGLSurfaceToSurfaceTransform() {
        super(OGLSurfaceData.OpenGLSurface,
              CompositeType.AnyAlpha,
              OGLSurfaceData.OpenGLSurface);
    }
    public void Transform(SurfaceData src, SurfaceData dst,
                          Composite comp, Region clip,
                          AffineTransform at, int hint,
                          int sx, int sy, int dx, int dy,
                          int w, int h)
    {
        OGLBlitLoops.IsoBlit(src, dst,
                             null, null,
                             comp, clip, at, hint,
                             sx, sy, sx+w, sy+h,
                             dx, dy, dx+w, dy+h,
                             false);
    }
}
class OGLRTTSurfaceToSurfaceBlit extends Blit {
    OGLRTTSurfaceToSurfaceBlit() {
        super(OGLSurfaceData.OpenGLSurfaceRTT,
              CompositeType.AnyAlpha,
              OGLSurfaceData.OpenGLSurface);
    }
    public void Blit(SurfaceData src, SurfaceData dst,
                     Composite comp, Region clip,
                     int sx, int sy, int dx, int dy, int w, int h)
    {
        OGLBlitLoops.IsoBlit(src, dst,
                             null, null,
                             comp, clip, null,
                             AffineTransformOp.TYPE_NEAREST_NEIGHBOR,
                             sx, sy, sx+w, sy+h,
                             dx, dy, dx+w, dy+h,
                             true);
    }
}
class OGLRTTSurfaceToSurfaceScale extends ScaledBlit {
    OGLRTTSurfaceToSurfaceScale() {
        super(OGLSurfaceData.OpenGLSurfaceRTT,
              CompositeType.AnyAlpha,
              OGLSurfaceData.OpenGLSurface);
    }
    public void Scale(SurfaceData src, SurfaceData dst,
                      Composite comp, Region clip,
                      int sx1, int sy1,
                      int sx2, int sy2,
                      double dx1, double dy1,
                      double dx2, double dy2)
    {
        OGLBlitLoops.IsoBlit(src, dst,
                             null, null,
                             comp, clip, null,
                             AffineTransformOp.TYPE_NEAREST_NEIGHBOR,
                             sx1, sy1, sx2, sy2,
                             dx1, dy1, dx2, dy2,
                             true);
    }
}
class OGLRTTSurfaceToSurfaceTransform extends TransformBlit {
    OGLRTTSurfaceToSurfaceTransform() {
        super(OGLSurfaceData.OpenGLSurfaceRTT,
              CompositeType.AnyAlpha,
              OGLSurfaceData.OpenGLSurface);
    }
    public void Transform(SurfaceData src, SurfaceData dst,
                          Composite comp, Region clip,
                          AffineTransform at, int hint,
                          int sx, int sy, int dx, int dy, int w, int h)
    {
        OGLBlitLoops.IsoBlit(src, dst,
                             null, null,
                             comp, clip, at, hint,
                             sx, sy, sx+w, sy+h,
                             dx, dy, dx+w, dy+h,
                             true);
    }
}
class OGLSurfaceToSwBlit extends Blit {
    private int typeval;
    OGLSurfaceToSwBlit(SurfaceType dstType, int typeval) {
        super(OGLSurfaceData.OpenGLSurface,
              CompositeType.SrcNoEa,
              dstType);
        this.typeval = typeval;
    }
    public void Blit(SurfaceData src, SurfaceData dst,
                     Composite comp, Region clip,
                     int sx, int sy, int dx, int dy,
                     int w, int h)
    {
        OGLRenderQueue rq = OGLRenderQueue.getInstance();
        rq.lock();
        try {
            rq.addReference(dst);
            RenderBuffer buf = rq.getBuffer();
            OGLContext.validateContext((OGLSurfaceData)src);
            rq.ensureCapacityAndAlignment(48, 32);
            buf.putInt(SURFACE_TO_SW_BLIT);
            buf.putInt(sx).putInt(sy);
            buf.putInt(dx).putInt(dy);
            buf.putInt(w).putInt(h);
            buf.putInt(typeval);
            buf.putLong(src.getNativeOps());
            buf.putLong(dst.getNativeOps());
            rq.flushNow();
        } finally {
            rq.unlock();
        }
    }
}
class OGLSwToSurfaceBlit extends Blit {
    private int typeval;
    OGLSwToSurfaceBlit(SurfaceType srcType, int typeval) {
        super(srcType,
              CompositeType.AnyAlpha,
              OGLSurfaceData.OpenGLSurface);
        this.typeval = typeval;
    }
    public void Blit(SurfaceData src, SurfaceData dst,
                     Composite comp, Region clip,
                     int sx, int sy, int dx, int dy, int w, int h)
    {
        OGLBlitLoops.Blit(src, dst,
                          comp, clip, null,
                          AffineTransformOp.TYPE_NEAREST_NEIGHBOR,
                          sx, sy, sx+w, sy+h,
                          dx, dy, dx+w, dy+h,
                          typeval, false);
    }
}
class OGLSwToSurfaceScale extends ScaledBlit {
    private int typeval;
    OGLSwToSurfaceScale(SurfaceType srcType, int typeval) {
        super(srcType,
              CompositeType.AnyAlpha,
              OGLSurfaceData.OpenGLSurface);
        this.typeval = typeval;
    }
    public void Scale(SurfaceData src, SurfaceData dst,
                      Composite comp, Region clip,
                      int sx1, int sy1,
                      int sx2, int sy2,
                      double dx1, double dy1,
                      double dx2, double dy2)
    {
        OGLBlitLoops.Blit(src, dst,
                          comp, clip, null,
                          AffineTransformOp.TYPE_NEAREST_NEIGHBOR,
                          sx1, sy1, sx2, sy2,
                          dx1, dy1, dx2, dy2,
                          typeval, false);
    }
}
class OGLSwToSurfaceTransform extends TransformBlit {
    private int typeval;
    OGLSwToSurfaceTransform(SurfaceType srcType, int typeval) {
        super(srcType,
              CompositeType.AnyAlpha,
              OGLSurfaceData.OpenGLSurface);
        this.typeval = typeval;
    }
    public void Transform(SurfaceData src, SurfaceData dst,
                          Composite comp, Region clip,
                          AffineTransform at, int hint,
                          int sx, int sy, int dx, int dy, int w, int h)
    {
        OGLBlitLoops.Blit(src, dst,
                          comp, clip, at, hint,
                          sx, sy, sx+w, sy+h,
                          dx, dy, dx+w, dy+h,
                          typeval, false);
    }
}
class OGLSwToTextureBlit extends Blit {
    private int typeval;
    OGLSwToTextureBlit(SurfaceType srcType, int typeval) {
        super(srcType,
              CompositeType.SrcNoEa,
              OGLSurfaceData.OpenGLTexture);
        this.typeval = typeval;
    }
    public void Blit(SurfaceData src, SurfaceData dst,
                     Composite comp, Region clip,
                     int sx, int sy, int dx, int dy, int w, int h)
    {
        OGLBlitLoops.Blit(src, dst,
                          comp, clip, null,
                          AffineTransformOp.TYPE_NEAREST_NEIGHBOR,
                          sx, sy, sx+w, sy+h,
                          dx, dy, dx+w, dy+h,
                          typeval, true);
    }
}
class OGLTextureToSurfaceBlit extends Blit {
    OGLTextureToSurfaceBlit() {
        super(OGLSurfaceData.OpenGLTexture,
              CompositeType.AnyAlpha,
              OGLSurfaceData.OpenGLSurface);
    }
    public void Blit(SurfaceData src, SurfaceData dst,
                     Composite comp, Region clip,
                     int sx, int sy, int dx, int dy, int w, int h)
    {
        OGLBlitLoops.IsoBlit(src, dst,
                             null, null,
                             comp, clip, null,
                             AffineTransformOp.TYPE_NEAREST_NEIGHBOR,
                             sx, sy, sx+w, sy+h,
                             dx, dy, dx+w, dy+h,
                             true);
    }
}
class OGLTextureToSurfaceScale extends ScaledBlit {
    OGLTextureToSurfaceScale() {
        super(OGLSurfaceData.OpenGLTexture,
              CompositeType.AnyAlpha,
              OGLSurfaceData.OpenGLSurface);
    }
    public void Scale(SurfaceData src, SurfaceData dst,
                      Composite comp, Region clip,
                      int sx1, int sy1,
                      int sx2, int sy2,
                      double dx1, double dy1,
                      double dx2, double dy2)
    {
        OGLBlitLoops.IsoBlit(src, dst,
                             null, null,
                             comp, clip, null,
                             AffineTransformOp.TYPE_NEAREST_NEIGHBOR,
                             sx1, sy1, sx2, sy2,
                             dx1, dy1, dx2, dy2,
                             true);
    }
}
class OGLTextureToSurfaceTransform extends TransformBlit {
    OGLTextureToSurfaceTransform() {
        super(OGLSurfaceData.OpenGLTexture,
              CompositeType.AnyAlpha,
              OGLSurfaceData.OpenGLSurface);
    }
    public void Transform(SurfaceData src, SurfaceData dst,
                          Composite comp, Region clip,
                          AffineTransform at, int hint,
                          int sx, int sy, int dx, int dy,
                          int w, int h)
    {
        OGLBlitLoops.IsoBlit(src, dst,
                             null, null,
                             comp, clip, at, hint,
                             sx, sy, sx+w, sy+h,
                             dx, dy, dx+w, dy+h,
                             true);
    }
}
class OGLGeneralBlit extends Blit {
    private Blit performop;
    private WeakReference srcTmp;
    OGLGeneralBlit(SurfaceType dstType,
                   CompositeType compType,
                   Blit performop)
    {
        super(SurfaceType.Any, compType, dstType);
        this.performop = performop;
    }
    public synchronized void Blit(SurfaceData src, SurfaceData dst,
                                  Composite comp, Region clip,
                                  int sx, int sy, int dx, int dy,
                                  int w, int h)
    {
        Blit convertsrc = Blit.getFromCache(src.getSurfaceType(),
                                            CompositeType.SrcNoEa,
                                            SurfaceType.IntArgbPre);
        SurfaceData cachedSrc = null;
        if (srcTmp != null) {
            cachedSrc = (SurfaceData)srcTmp.get();
        }
        src = convertFrom(convertsrc, src, sx, sy, w, h,
                          cachedSrc, BufferedImage.TYPE_INT_ARGB_PRE);
        performop.Blit(src, dst, comp, clip,
                       0, 0, dx, dy, w, h);
        if (src != cachedSrc) {
            srcTmp = new WeakReference(src);
        }
    }
}
