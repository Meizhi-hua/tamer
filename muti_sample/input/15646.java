public class XRBackendNative implements XRBackend {
    static {
        initIDs();
    }
    private static long FMTPTR_A8;
    private static long FMTPTR_ARGB32;
    private static long MASK_XIMG;
    private static native void initIDs();
    public native long createGC(int drawable);
    public native void freeGC(long gc);
    public native int createPixmap(int drawable, int depth,
                                   int width, int height);
    private native int createPictureNative(int drawable, long formatID);
    public native void freePicture(int picture);
    public native void freePixmap(int pixmap);
    public native void setGCExposures(long gc, boolean exposure);
    public native void setGCForeground(long gc, int pixel);
    public native void setPictureRepeat(int picture, int repeat);
    public native void copyArea(int src, int dst, long gc,
                                int srcx, int srcy, int width, int height,
                                 int dstx, int dsty);
    public native void setGCMode(long gc, boolean copy);
    private static native void GCRectanglesNative(int drawable, long gc,
                                                  int[] rectArray, int rectCnt);
    public native void renderComposite(byte op, int src, int mask,
                                       int dst, int srcX, int srcY,
                                       int maskX, int maskY, int dstX, int dstY,
                                       int width, int height);
    private native void renderRectangle(int dst, byte op,
                                        short red, short green,
                                        short blue, short alpha,
                                        int x, int y, int width, int height);
    private static native void
         XRenderRectanglesNative(int dst, byte op,
                                 short red, short green,
                                 short blue, short alpha,
                                 int[] rects, int rectCnt);
    private native void XRSetTransformNative(int pic,
                                             int m00, int m01, int m02,
                                             int m10, int m11, int m12);
    private static native int
        XRCreateLinearGradientPaintNative(float[] fractionsArray,
                                          short[] pixelsArray,
                                          int x1, int y1, int x2, int y2,
                                          int numStops, int repeat,
                                          int m00, int m01, int m02,
                                           int m10, int m11, int m12);
    private native static int
        XRCreateRadialGradientPaintNative(float[] fractionsArray,
                                          short[] pixelsArray, int numStops,
                                          int innerRadius, int outerRadius,
                                          int repeat,
                                          int m00, int m01, int m02,
                                          int m10, int m11, int m12);
    public native void setFilter(int picture, int filter);
    private static native void XRSetClipNative(long dst,
                                               int x1, int y1, int x2, int y2,
                                               Region clip, boolean isGC);
    public void GCRectangles(int drawable, long gc, GrowableRectArray rects) {
        GCRectanglesNative(drawable, gc, rects.getArray(), rects.getSize());
    }
    public int createPicture(int drawable, int formatID) {
        return createPictureNative(drawable, getFormatPtr(formatID));
    }
    public void setPictureTransform(int picture, AffineTransform transform) {
        XRSetTransformNative(picture,
                             XDoubleToFixed(transform.getScaleX()),
                             XDoubleToFixed(transform.getShearX()),
                             XDoubleToFixed(transform.getTranslateX()),
                             XDoubleToFixed(transform.getShearY()),
                             XDoubleToFixed(transform.getScaleY()),
                             XDoubleToFixed(transform.getTranslateY()));
    }
    public void renderRectangle(int dst, byte op, XRColor color,
                                int x, int y, int width, int height) {
        renderRectangle(dst, op, (short)color.red, (short)color.green,
                       (short)color.blue, (short)color.alpha,
                        x, y, width, height);
    }
    private short[] getRenderColors(int[] pixels) {
        short[] renderColors = new short[pixels.length * 4];
        XRColor c = new XRColor();
        for (int i = 0; i < pixels.length; i++) {
            c.setColorValues(pixels[i], true);
            renderColors[i * 4 + 0] = (short) c.alpha;
            renderColors[i * 4 + 1] = (short) c.red;
            renderColors[i * 4 + 2] = (short) c.green;
            renderColors[i * 4 + 3] = (short) c.blue;
        }
        return renderColors;
    }
    private static long getFormatPtr(int formatID) {
        switch (formatID) {
        case XRUtils.PictStandardA8:
            return FMTPTR_A8;
        case XRUtils.PictStandardARGB32:
            return FMTPTR_ARGB32;
        }
        return 0L;
    }
    public int createLinearGradient(Point2D p1, Point2D p2, float[] fractions,
                              int[] pixels,  int repeat, AffineTransform trx) {
        short[] colorValues = getRenderColors(pixels);
        int gradient =
           XRCreateLinearGradientPaintNative(fractions, colorValues,
                XDoubleToFixed(p1.getX()), XDoubleToFixed(p1.getY()),
                XDoubleToFixed(p2.getX()), XDoubleToFixed(p2.getY()),
                fractions.length, repeat,
                XDoubleToFixed(trx.getScaleX()),
                XDoubleToFixed(trx.getShearX()),
                XDoubleToFixed(trx.getTranslateX()),
                XDoubleToFixed(trx.getShearY()),
                XDoubleToFixed(trx.getScaleY()),
                XDoubleToFixed(trx.getTranslateY()));
        return gradient;
    }
    public int createRadialGradient(Point2D inner, Point2D outer,
                                   float innerRadius, float outerRadius,
                                   float[] fractions, int[] pixels, int repeat,
                                   AffineTransform trx) {
        short[] colorValues = getRenderColors(pixels);
        return XRCreateRadialGradientPaintNative
             (fractions, colorValues, fractions.length,
              XDoubleToFixed(innerRadius),
              XDoubleToFixed(outerRadius),
              repeat,
              XDoubleToFixed(trx.getScaleX()),
              XDoubleToFixed(trx.getShearX()),
              XDoubleToFixed(trx.getTranslateX()),
              XDoubleToFixed(trx.getShearY()),
              XDoubleToFixed(trx.getScaleY()),
              XDoubleToFixed(trx.getTranslateY()));
    }
    public void setGCClipRectangles(long gc, Region clip) {
        XRSetClipNative(gc, clip.getLoX(), clip.getLoY(),
                        clip.getHiX(), clip.getHiY(),
                        clip.isRectangular() ? null : clip, true);
    }
    public void setClipRectangles(int picture, Region clip) {
        if (clip != null) {
            XRSetClipNative(picture, clip.getLoX(), clip.getLoY(),
                            clip.getHiX(), clip.getHiY(),
                            clip.isRectangular() ? null : clip, false);
        } else {
            XRSetClipNative(picture, 0, 0, 32767, 32767, null, false);
        }
    }
    public void renderRectangles(int dst, byte op, XRColor color,
                                 GrowableRectArray rects) {
        XRenderRectanglesNative(dst, op,
                                (short) color.red, (short) color.green,
                                (short) color.blue, (short) color.alpha,
                                rects.getArray(), rects
                .getSize());
    }
    private static long[] getGlyphInfoPtrs(List<XRGlyphCacheEntry> cacheEntries) {
        long[] glyphInfoPtrs = new long[cacheEntries.size()];
        for (int i = 0; i < cacheEntries.size(); i++) {
            glyphInfoPtrs[i] = cacheEntries.get(i).getGlyphInfoPtr();
        }
        return glyphInfoPtrs;
    }
    public void XRenderAddGlyphs(int glyphSet, GlyphList gl,
                                 List<XRGlyphCacheEntry> cacheEntries,
                                 byte[] pixelData) {
        long[] glyphInfoPtrs = getGlyphInfoPtrs(cacheEntries);
        XRAddGlyphsNative(glyphSet, glyphInfoPtrs,
                          glyphInfoPtrs.length, pixelData, pixelData.length);
    }
    public void XRenderFreeGlyphs(int glyphSet, int[] gids) {
        XRFreeGlyphsNative(glyphSet, gids, gids.length);
    }
    private static native void XRAddGlyphsNative(int glyphSet,
                                                 long[] glyphInfoPtrs,
                                                 int glyphCnt,
                                                 byte[] pixelData,
                                                 int pixelDataLength);
    private static native void XRFreeGlyphsNative(int glyphSet,
                                                  int[] gids, int idCnt);
    private static native void
        XRenderCompositeTextNative(int op, int src, int dst,
                                   long maskFormat, int[] eltArray,
                                   int[] glyphIDs, int eltCnt, int glyphCnt);
    public int XRenderCreateGlyphSet(int formatID) {
        return XRenderCreateGlyphSetNative(getFormatPtr(formatID));
    }
    private static native int XRenderCreateGlyphSetNative(long format);
    public void XRenderCompositeText(byte op, int src, int dst,
                                     int maskFormatID,
                                     int src2, int src3, int dst2, int dst3,
                                     int glyphset, GrowableEltArray elts) {
        GrowableIntArray glyphs = elts.getGlyphs();
        XRenderCompositeTextNative(op, src, dst, 0, elts.getArray(),
                                   glyphs.getArray(), elts.getSize(),
                                   glyphs.getSize());
    }
    public void putMaskImage(int drawable, long gc, byte[] imageData,
                             int sx, int sy, int dx, int dy,
                             int width, int height, int maskOff,
                             int maskScan, float ea) {
        putMaskNative(drawable, gc, imageData, sx, sy, dx, dy,
                      width, height, maskOff, maskScan, ea, MASK_XIMG);
    }
    private static native void putMaskNative(int drawable, long gc,
                                             byte[] imageData,
                                             int sx, int sy, int dx, int dy,
                                             int width, int height,
                                             int maskOff, int maskScan,
                                             float ea, long xImg);
    public void padBlit(byte op, int srcPict, int maskPict, int dstPict,
                        AffineTransform maskTrx, int maskWidth, int maskHeight,
                        int lastMaskWidth, int lastMaskHeight,
                        int sx, int sy, int dx, int dy, int w, int h) {
        padBlitNative(op, srcPict, maskPict, dstPict,
                      XDoubleToFixed(maskTrx.getScaleX()),
                      XDoubleToFixed(maskTrx.getShearX()),
                      XDoubleToFixed(maskTrx.getTranslateX()),
                      XDoubleToFixed(maskTrx.getShearY()),
                      XDoubleToFixed(maskTrx.getScaleY()),
                      XDoubleToFixed(maskTrx.getTranslateY()),
                      maskWidth, maskHeight, lastMaskWidth, lastMaskHeight,
                      sx, sy, dx, dy, w, h);
    }
    private static native void padBlitNative(byte op, int srcPict,
                                             int maskPict, int dstPict,
                                             int m00, int m01, int m02,
                                             int m10, int m11, int m12,
                                             int maskWidth, int maskHeight,
                                             int lastMaskWidth,
                                             int lastMaskHeight,
                                             int sx, int sy, int dx, int dy,
                                             int w, int h);
    public void renderCompositeTrapezoids(byte op, int src, int maskFormat,
                                          int dst, int srcX, int srcY,
                                          TrapezoidList trapList) {
        renderCompositeTrapezoidsNative(op, src, getFormatPtr(maskFormat),
                                        dst, srcX, srcY,
                                        trapList.getTrapArray());
    }
    private static native void
        renderCompositeTrapezoidsNative(byte op, int src, long maskFormat,
                                        int dst, int srcX, int srcY,
                                        int[] trapezoids);
}
