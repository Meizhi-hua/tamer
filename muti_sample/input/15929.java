abstract class TexturePaintContext implements PaintContext {
    public static ColorModel xrgbmodel =
        new DirectColorModel(24, 0xff0000, 0xff00, 0xff);
    public static ColorModel argbmodel = ColorModel.getRGBdefault();
    ColorModel colorModel;
    int bWidth;
    int bHeight;
    int maxWidth;
    WritableRaster outRas;
    double xOrg;
    double yOrg;
    double incXAcross;
    double incYAcross;
    double incXDown;
    double incYDown;
    int colincx;
    int colincy;
    int colincxerr;
    int colincyerr;
    int rowincx;
    int rowincy;
    int rowincxerr;
    int rowincyerr;
    public static PaintContext getContext(BufferedImage bufImg,
                                          AffineTransform xform,
                                          RenderingHints hints,
                                          Rectangle devBounds) {
        WritableRaster raster = bufImg.getRaster();
        ColorModel cm = bufImg.getColorModel();
        int maxw = devBounds.width;
        Object val = hints.get(hints.KEY_INTERPOLATION);
        boolean filter =
            (val == null
             ? (hints.get(hints.KEY_RENDERING) == hints.VALUE_RENDER_QUALITY)
             : (val != hints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR));
        if (raster instanceof IntegerInterleavedRaster &&
            (!filter || isFilterableDCM(cm)))
        {
            IntegerInterleavedRaster iir = (IntegerInterleavedRaster) raster;
            if (iir.getNumDataElements() == 1 && iir.getPixelStride() == 1) {
                return new Int(iir, cm, xform, maxw, filter);
            }
        } else if (raster instanceof ByteInterleavedRaster) {
            ByteInterleavedRaster bir = (ByteInterleavedRaster) raster;
            if (bir.getNumDataElements() == 1 && bir.getPixelStride() == 1) {
                if (filter) {
                    if (isFilterableICM(cm)) {
                        return new ByteFilter(bir, cm, xform, maxw);
                    }
                } else {
                    return new Byte(bir, cm, xform, maxw);
                }
            }
        }
        return new Any(raster, cm, xform, maxw, filter);
    }
    public static boolean isFilterableICM(ColorModel cm) {
        if (cm instanceof IndexColorModel) {
            IndexColorModel icm = (IndexColorModel) cm;
            if (icm.getMapSize() <= 256) {
                return true;
            }
        }
        return false;
    }
    public static boolean isFilterableDCM(ColorModel cm) {
        if (cm instanceof DirectColorModel) {
            DirectColorModel dcm = (DirectColorModel) cm;
            return (isMaskOK(dcm.getAlphaMask(), true) &&
                    isMaskOK(dcm.getRedMask(), false) &&
                    isMaskOK(dcm.getGreenMask(), false) &&
                    isMaskOK(dcm.getBlueMask(), false));
        }
        return false;
    }
    public static boolean isMaskOK(int mask, boolean canbezero) {
        if (canbezero && mask == 0) {
            return true;
        }
        return (mask == 0xff ||
                mask == 0xff00 ||
                mask == 0xff0000 ||
                mask == 0xff000000);
    }
    public static ColorModel getInternedColorModel(ColorModel cm) {
        if (xrgbmodel == cm || xrgbmodel.equals(cm)) {
            return xrgbmodel;
        }
        if (argbmodel == cm || argbmodel.equals(cm)) {
            return argbmodel;
        }
        return cm;
    }
    TexturePaintContext(ColorModel cm, AffineTransform xform,
                        int bWidth, int bHeight, int maxw) {
        this.colorModel = getInternedColorModel(cm);
        this.bWidth = bWidth;
        this.bHeight = bHeight;
        this.maxWidth = maxw;
        try {
            xform = xform.createInverse();
        } catch (NoninvertibleTransformException e) {
            xform.setToScale(0, 0);
        }
        this.incXAcross = mod(xform.getScaleX(), bWidth);
        this.incYAcross = mod(xform.getShearY(), bHeight);
        this.incXDown = mod(xform.getShearX(), bWidth);
        this.incYDown = mod(xform.getScaleY(), bHeight);
        this.xOrg = xform.getTranslateX();
        this.yOrg = xform.getTranslateY();
        this.colincx = (int) incXAcross;
        this.colincy = (int) incYAcross;
        this.colincxerr = fractAsInt(incXAcross);
        this.colincyerr = fractAsInt(incYAcross);
        this.rowincx = (int) incXDown;
        this.rowincy = (int) incYDown;
        this.rowincxerr = fractAsInt(incXDown);
        this.rowincyerr = fractAsInt(incYDown);
    }
    static int fractAsInt(double d) {
        return (int) ((d % 1.0) * Integer.MAX_VALUE);
    }
    static double mod(double num, double den) {
        num = num % den;
        if (num < 0) {
            num += den;
            if (num >= den) {
                num = 0;
            }
        }
        return num;
    }
    public void dispose() {
        dropRaster(colorModel, outRas);
    }
    public ColorModel getColorModel() {
        return colorModel;
    }
    public Raster getRaster(int x, int y, int w, int h) {
        if (outRas == null ||
            outRas.getWidth() < w ||
            outRas.getHeight() < h)
        {
            outRas = makeRaster((h == 1 ? Math.max(w, maxWidth) : w), h);
        }
        double X = mod(xOrg + x * incXAcross + y * incXDown, bWidth);
        double Y = mod(yOrg + x * incYAcross + y * incYDown, bHeight);
        setRaster((int) X, (int) Y, fractAsInt(X), fractAsInt(Y),
                  w, h, bWidth, bHeight,
                  colincx, colincxerr,
                  colincy, colincyerr,
                  rowincx, rowincxerr,
                  rowincy, rowincyerr);
        SunWritableRaster.markDirty(outRas);
        return outRas;
    }
    private static WeakReference xrgbRasRef;
    private static WeakReference argbRasRef;
    synchronized static WritableRaster makeRaster(ColorModel cm,
                                                  Raster srcRas,
                                                  int w, int h)
    {
        if (xrgbmodel == cm) {
            if (xrgbRasRef != null) {
                WritableRaster wr = (WritableRaster) xrgbRasRef.get();
                if (wr != null && wr.getWidth() >= w && wr.getHeight() >= h) {
                    xrgbRasRef = null;
                    return wr;
                }
            }
            if (w <= 32 && h <= 32) {
                w = h = 32;
            }
        } else if (argbmodel == cm) {
            if (argbRasRef != null) {
                WritableRaster wr = (WritableRaster) argbRasRef.get();
                if (wr != null && wr.getWidth() >= w && wr.getHeight() >= h) {
                    argbRasRef = null;
                    return wr;
                }
            }
            if (w <= 32 && h <= 32) {
                w = h = 32;
            }
        }
        if (srcRas != null) {
            return srcRas.createCompatibleWritableRaster(w, h);
        } else {
            return cm.createCompatibleWritableRaster(w, h);
        }
    }
    synchronized static void dropRaster(ColorModel cm, Raster outRas) {
        if (outRas == null) {
            return;
        }
        if (xrgbmodel == cm) {
            xrgbRasRef = new WeakReference(outRas);
        } else if (argbmodel == cm) {
            argbRasRef = new WeakReference(outRas);
        }
    }
    private static WeakReference byteRasRef;
    synchronized static WritableRaster makeByteRaster(Raster srcRas,
                                                      int w, int h)
    {
        if (byteRasRef != null) {
            WritableRaster wr = (WritableRaster) byteRasRef.get();
            if (wr != null && wr.getWidth() >= w && wr.getHeight() >= h) {
                byteRasRef = null;
                return wr;
            }
        }
        if (w <= 32 && h <= 32) {
            w = h = 32;
        }
        return srcRas.createCompatibleWritableRaster(w, h);
    }
    synchronized static void dropByteRaster(Raster outRas) {
        if (outRas == null) {
            return;
        }
        byteRasRef = new WeakReference(outRas);
    }
    public abstract WritableRaster makeRaster(int w, int h);
    public abstract void setRaster(int x, int y, int xerr, int yerr,
                                   int w, int h, int bWidth, int bHeight,
                                   int colincx, int colincxerr,
                                   int colincy, int colincyerr,
                                   int rowincx, int rowincxerr,
                                   int rowincy, int rowincyerr);
    public static int blend(int rgbs[], int xmul, int ymul) {
        xmul = (xmul >>> 19);
        ymul = (ymul >>> 19);
        int accumA, accumR, accumG, accumB;
        accumA = accumR = accumG = accumB = 0;
        for (int i = 0; i < 4; i++) {
            int rgb = rgbs[i];
            xmul = (1<<12) - xmul;
            if ((i & 1) == 0) {
                ymul = (1<<12) - ymul;
            }
            int factor = xmul * ymul;
            if (factor != 0) {
                accumA += (((rgb >>> 24)       ) * factor);
                accumR += (((rgb >>> 16) & 0xff) * factor);
                accumG += (((rgb >>>  8) & 0xff) * factor);
                accumB += (((rgb       ) & 0xff) * factor);
            }
        }
        return ((((accumA + (1<<23)) >>> 24) << 24) |
                (((accumR + (1<<23)) >>> 24) << 16) |
                (((accumG + (1<<23)) >>> 24) <<  8) |
                (((accumB + (1<<23)) >>> 24)      ));
    }
    static class Int extends TexturePaintContext {
        IntegerInterleavedRaster srcRas;
        int inData[];
        int inOff;
        int inSpan;
        int outData[];
        int outOff;
        int outSpan;
        boolean filter;
        public Int(IntegerInterleavedRaster srcRas, ColorModel cm,
                   AffineTransform xform, int maxw, boolean filter)
        {
            super(cm, xform, srcRas.getWidth(), srcRas.getHeight(), maxw);
            this.srcRas = srcRas;
            this.inData = srcRas.getDataStorage();
            this.inSpan = srcRas.getScanlineStride();
            this.inOff = srcRas.getDataOffset(0);
            this.filter = filter;
        }
        public WritableRaster makeRaster(int w, int h) {
            WritableRaster ras = makeRaster(colorModel, srcRas, w, h);
            IntegerInterleavedRaster iiRas = (IntegerInterleavedRaster) ras;
            outData = iiRas.getDataStorage();
            outSpan = iiRas.getScanlineStride();
            outOff = iiRas.getDataOffset(0);
            return ras;
        }
        public void setRaster(int x, int y, int xerr, int yerr,
                              int w, int h, int bWidth, int bHeight,
                              int colincx, int colincxerr,
                              int colincy, int colincyerr,
                              int rowincx, int rowincxerr,
                              int rowincy, int rowincyerr) {
            int[] inData = this.inData;
            int[] outData = this.outData;
            int out = outOff;
            int inSpan = this.inSpan;
            int inOff = this.inOff;
            int outSpan = this.outSpan;
            boolean filter = this.filter;
            boolean normalx = (colincx == 1 && colincxerr == 0 &&
                               colincy == 0 && colincyerr == 0) && !filter;
            int rowx = x;
            int rowy = y;
            int rowxerr = xerr;
            int rowyerr = yerr;
            if (normalx) {
                outSpan -= w;
            }
            int rgbs[] = filter ? new int[4] : null;
            for (int j = 0; j < h; j++) {
                if (normalx) {
                    int in = inOff + rowy * inSpan + bWidth;
                    x = bWidth - rowx;
                    out += w;
                    if (bWidth >= 32) {
                        int i = w;
                        while (i > 0) {
                            int copyw = (i < x) ? i : x;
                            System.arraycopy(inData, in - x,
                                             outData, out - i,
                                             copyw);
                            i -= copyw;
                            if ((x -= copyw) == 0) {
                                x = bWidth;
                            }
                        }
                    } else {
                        for (int i = w; i > 0; i--) {
                            outData[out - i] = inData[in - x];
                            if (--x == 0) {
                                x = bWidth;
                            }
                        }
                    }
                } else {
                    x = rowx;
                    y = rowy;
                    xerr = rowxerr;
                    yerr = rowyerr;
                    for (int i = 0; i < w; i++) {
                        if (filter) {
                            int nextx, nexty;
                            if ((nextx = x + 1) >= bWidth) {
                                nextx = 0;
                            }
                            if ((nexty = y + 1) >= bHeight) {
                                nexty = 0;
                            }
                            rgbs[0] = inData[inOff + y * inSpan + x];
                            rgbs[1] = inData[inOff + y * inSpan + nextx];
                            rgbs[2] = inData[inOff + nexty * inSpan + x];
                            rgbs[3] = inData[inOff + nexty * inSpan + nextx];
                            outData[out + i] =
                                TexturePaintContext.blend(rgbs, xerr, yerr);
                        } else {
                            outData[out + i] = inData[inOff + y * inSpan + x];
                        }
                        if ((xerr += colincxerr) < 0) {
                            xerr &= Integer.MAX_VALUE;
                            x++;
                        }
                        if ((x += colincx) >= bWidth) {
                            x -= bWidth;
                        }
                        if ((yerr += colincyerr) < 0) {
                            yerr &= Integer.MAX_VALUE;
                            y++;
                        }
                        if ((y += colincy) >= bHeight) {
                            y -= bHeight;
                        }
                    }
                }
                if ((rowxerr += rowincxerr) < 0) {
                    rowxerr &= Integer.MAX_VALUE;
                    rowx++;
                }
                if ((rowx += rowincx) >= bWidth) {
                    rowx -= bWidth;
                }
                if ((rowyerr += rowincyerr) < 0) {
                    rowyerr &= Integer.MAX_VALUE;
                    rowy++;
                }
                if ((rowy += rowincy) >= bHeight) {
                    rowy -= bHeight;
                }
                out += outSpan;
            }
        }
    }
    static class Byte extends TexturePaintContext {
        ByteInterleavedRaster srcRas;
        byte inData[];
        int inOff;
        int inSpan;
        byte outData[];
        int outOff;
        int outSpan;
        public Byte(ByteInterleavedRaster srcRas, ColorModel cm,
                    AffineTransform xform, int maxw)
        {
            super(cm, xform, srcRas.getWidth(), srcRas.getHeight(), maxw);
            this.srcRas = srcRas;
            this.inData = srcRas.getDataStorage();
            this.inSpan = srcRas.getScanlineStride();
            this.inOff = srcRas.getDataOffset(0);
        }
        public WritableRaster makeRaster(int w, int h) {
            WritableRaster ras = makeByteRaster(srcRas, w, h);
            ByteInterleavedRaster biRas = (ByteInterleavedRaster) ras;
            outData = biRas.getDataStorage();
            outSpan = biRas.getScanlineStride();
            outOff = biRas.getDataOffset(0);
            return ras;
        }
        public void dispose() {
            dropByteRaster(outRas);
        }
        public void setRaster(int x, int y, int xerr, int yerr,
                              int w, int h, int bWidth, int bHeight,
                              int colincx, int colincxerr,
                              int colincy, int colincyerr,
                              int rowincx, int rowincxerr,
                              int rowincy, int rowincyerr) {
            byte[] inData = this.inData;
            byte[] outData = this.outData;
            int out = outOff;
            int inSpan = this.inSpan;
            int inOff = this.inOff;
            int outSpan = this.outSpan;
            boolean normalx = (colincx == 1 && colincxerr == 0 &&
                               colincy == 0 && colincyerr == 0);
            int rowx = x;
            int rowy = y;
            int rowxerr = xerr;
            int rowyerr = yerr;
            if (normalx) {
                outSpan -= w;
            }
            for (int j = 0; j < h; j++) {
                if (normalx) {
                    int in = inOff + rowy * inSpan + bWidth;
                    x = bWidth - rowx;
                    out += w;
                    if (bWidth >= 32) {
                        int i = w;
                        while (i > 0) {
                            int copyw = (i < x) ? i : x;
                            System.arraycopy(inData, in - x,
                                             outData, out - i,
                                             copyw);
                            i -= copyw;
                            if ((x -= copyw) == 0) {
                                x = bWidth;
                            }
                        }
                    } else {
                        for (int i = w; i > 0; i--) {
                            outData[out - i] = inData[in - x];
                            if (--x == 0) {
                                x = bWidth;
                            }
                        }
                    }
                } else {
                    x = rowx;
                    y = rowy;
                    xerr = rowxerr;
                    yerr = rowyerr;
                    for (int i = 0; i < w; i++) {
                        outData[out + i] = inData[inOff + y * inSpan + x];
                        if ((xerr += colincxerr) < 0) {
                            xerr &= Integer.MAX_VALUE;
                            x++;
                        }
                        if ((x += colincx) >= bWidth) {
                            x -= bWidth;
                        }
                        if ((yerr += colincyerr) < 0) {
                            yerr &= Integer.MAX_VALUE;
                            y++;
                        }
                        if ((y += colincy) >= bHeight) {
                            y -= bHeight;
                        }
                    }
                }
                if ((rowxerr += rowincxerr) < 0) {
                    rowxerr &= Integer.MAX_VALUE;
                    rowx++;
                }
                if ((rowx += rowincx) >= bWidth) {
                    rowx -= bWidth;
                }
                if ((rowyerr += rowincyerr) < 0) {
                    rowyerr &= Integer.MAX_VALUE;
                    rowy++;
                }
                if ((rowy += rowincy) >= bHeight) {
                    rowy -= bHeight;
                }
                out += outSpan;
            }
        }
    }
    static class ByteFilter extends TexturePaintContext {
        ByteInterleavedRaster srcRas;
        int inPalette[];
        byte inData[];
        int inOff;
        int inSpan;
        int outData[];
        int outOff;
        int outSpan;
        public ByteFilter(ByteInterleavedRaster srcRas, ColorModel cm,
                          AffineTransform xform, int maxw)
        {
            super((cm.getTransparency() == Transparency.OPAQUE
                   ? xrgbmodel : argbmodel),
                  xform, srcRas.getWidth(), srcRas.getHeight(), maxw);
            this.inPalette = new int[256];
            ((IndexColorModel) cm).getRGBs(this.inPalette);
            this.srcRas = srcRas;
            this.inData = srcRas.getDataStorage();
            this.inSpan = srcRas.getScanlineStride();
            this.inOff = srcRas.getDataOffset(0);
        }
        public WritableRaster makeRaster(int w, int h) {
            WritableRaster ras = makeRaster(colorModel, null, w, h);
            IntegerInterleavedRaster iiRas = (IntegerInterleavedRaster) ras;
            outData = iiRas.getDataStorage();
            outSpan = iiRas.getScanlineStride();
            outOff = iiRas.getDataOffset(0);
            return ras;
        }
        public void setRaster(int x, int y, int xerr, int yerr,
                              int w, int h, int bWidth, int bHeight,
                              int colincx, int colincxerr,
                              int colincy, int colincyerr,
                              int rowincx, int rowincxerr,
                              int rowincy, int rowincyerr) {
            byte[] inData = this.inData;
            int[] outData = this.outData;
            int out = outOff;
            int inSpan = this.inSpan;
            int inOff = this.inOff;
            int outSpan = this.outSpan;
            int rowx = x;
            int rowy = y;
            int rowxerr = xerr;
            int rowyerr = yerr;
            int rgbs[] = new int[4];
            for (int j = 0; j < h; j++) {
                x = rowx;
                y = rowy;
                xerr = rowxerr;
                yerr = rowyerr;
                for (int i = 0; i < w; i++) {
                    int nextx, nexty;
                    if ((nextx = x + 1) >= bWidth) {
                        nextx = 0;
                    }
                    if ((nexty = y + 1) >= bHeight) {
                        nexty = 0;
                    }
                    rgbs[0] = inPalette[0xff & inData[inOff + x +
                                                      inSpan * y]];
                    rgbs[1] = inPalette[0xff & inData[inOff + nextx +
                                                      inSpan * y]];
                    rgbs[2] = inPalette[0xff & inData[inOff + x +
                                                      inSpan * nexty]];
                    rgbs[3] = inPalette[0xff & inData[inOff + nextx +
                                                      inSpan * nexty]];
                    outData[out + i] =
                        TexturePaintContext.blend(rgbs, xerr, yerr);
                    if ((xerr += colincxerr) < 0) {
                        xerr &= Integer.MAX_VALUE;
                        x++;
                    }
                    if ((x += colincx) >= bWidth) {
                        x -= bWidth;
                    }
                    if ((yerr += colincyerr) < 0) {
                        yerr &= Integer.MAX_VALUE;
                        y++;
                    }
                    if ((y += colincy) >= bHeight) {
                        y -= bHeight;
                    }
                }
                if ((rowxerr += rowincxerr) < 0) {
                    rowxerr &= Integer.MAX_VALUE;
                    rowx++;
                }
                if ((rowx += rowincx) >= bWidth) {
                    rowx -= bWidth;
                }
                if ((rowyerr += rowincyerr) < 0) {
                    rowyerr &= Integer.MAX_VALUE;
                    rowy++;
                }
                if ((rowy += rowincy) >= bHeight) {
                    rowy -= bHeight;
                }
                out += outSpan;
            }
        }
    }
    static class Any extends TexturePaintContext {
        WritableRaster srcRas;
        boolean filter;
        public Any(WritableRaster srcRas, ColorModel cm,
                   AffineTransform xform, int maxw, boolean filter)
        {
            super(cm, xform, srcRas.getWidth(), srcRas.getHeight(), maxw);
            this.srcRas = srcRas;
            this.filter = filter;
        }
        public WritableRaster makeRaster(int w, int h) {
            return makeRaster(colorModel, srcRas, w, h);
        }
        public void setRaster(int x, int y, int xerr, int yerr,
                              int w, int h, int bWidth, int bHeight,
                              int colincx, int colincxerr,
                              int colincy, int colincyerr,
                              int rowincx, int rowincxerr,
                              int rowincy, int rowincyerr) {
            Object data = null;
            int rowx = x;
            int rowy = y;
            int rowxerr = xerr;
            int rowyerr = yerr;
            WritableRaster srcRas = this.srcRas;
            WritableRaster outRas = this.outRas;
            int rgbs[] = filter ? new int[4] : null;
            for (int j = 0; j < h; j++) {
                x = rowx;
                y = rowy;
                xerr = rowxerr;
                yerr = rowyerr;
                for (int i = 0; i < w; i++) {
                    data = srcRas.getDataElements(x, y, data);
                    if (filter) {
                        int nextx, nexty;
                        if ((nextx = x + 1) >= bWidth) {
                            nextx = 0;
                        }
                        if ((nexty = y + 1) >= bHeight) {
                            nexty = 0;
                        }
                        rgbs[0] = colorModel.getRGB(data);
                        data = srcRas.getDataElements(nextx, y, data);
                        rgbs[1] = colorModel.getRGB(data);
                        data = srcRas.getDataElements(x, nexty, data);
                        rgbs[2] = colorModel.getRGB(data);
                        data = srcRas.getDataElements(nextx, nexty, data);
                        rgbs[3] = colorModel.getRGB(data);
                        int rgb =
                            TexturePaintContext.blend(rgbs, xerr, yerr);
                        data = colorModel.getDataElements(rgb, data);
                    }
                    outRas.setDataElements(i, j, data);
                    if ((xerr += colincxerr) < 0) {
                        xerr &= Integer.MAX_VALUE;
                        x++;
                    }
                    if ((x += colincx) >= bWidth) {
                        x -= bWidth;
                    }
                    if ((yerr += colincyerr) < 0) {
                        yerr &= Integer.MAX_VALUE;
                        y++;
                    }
                    if ((y += colincy) >= bHeight) {
                        y -= bHeight;
                    }
                }
                if ((rowxerr += rowincxerr) < 0) {
                    rowxerr &= Integer.MAX_VALUE;
                    rowx++;
                }
                if ((rowx += rowincx) >= bWidth) {
                    rowx -= bWidth;
                }
                if ((rowyerr += rowincyerr) < 0) {
                    rowyerr &= Integer.MAX_VALUE;
                    rowy++;
                }
                if ((rowy += rowincy) >= bHeight) {
                    rowy -= bHeight;
                }
            }
        }
    }
}
