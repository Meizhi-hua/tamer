public class AffineTransformOp implements BufferedImageOp, RasterOp {
    public static final int TYPE_NEAREST_NEIGHBOR = 1;
    public static final int TYPE_BILINEAR = 2;
    public static final int TYPE_BICUBIC = 3;
    private int iType; 
    private AffineTransform at;
    private RenderingHints hints;
    static {
    }
    public AffineTransformOp(AffineTransform xform, RenderingHints hints) {
        this(xform, TYPE_NEAREST_NEIGHBOR);
        this.hints = hints;
        if (hints != null) {
            Object hint = hints.get(RenderingHints.KEY_INTERPOLATION);
            if (hint != null) {
                if (hint == RenderingHints.VALUE_INTERPOLATION_BILINEAR) {
                    this.iType = TYPE_BILINEAR;
                } else if (hint == RenderingHints.VALUE_INTERPOLATION_BICUBIC) {
                    this.iType = TYPE_BICUBIC;
                }
            } else {
                hint = hints.get(RenderingHints.KEY_RENDERING);
                if (hint == RenderingHints.VALUE_RENDER_QUALITY) {
                    this.iType = TYPE_BILINEAR;
                }
            }
        }
    }
    public AffineTransformOp(AffineTransform xform, int interp) {
        if (Math.abs(xform.getDeterminant()) <= Double.MIN_VALUE) {
            throw new ImagingOpException(Messages.getString("awt.24F", xform)); 
        }
        this.at = (AffineTransform)xform.clone();
        if (interp != TYPE_NEAREST_NEIGHBOR && interp != TYPE_BILINEAR && interp != TYPE_BICUBIC) {
            throw new IllegalArgumentException(Messages.getString("awt.250", interp)); 
        }
        this.iType = interp;
    }
    public final int getInterpolationType() {
        return iType;
    }
    public final RenderingHints getRenderingHints() {
        if (hints == null) {
            Object value = null;
            switch (iType) {
                case TYPE_NEAREST_NEIGHBOR:
                    value = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
                    break;
                case TYPE_BILINEAR:
                    value = RenderingHints.VALUE_INTERPOLATION_BILINEAR;
                    break;
                case TYPE_BICUBIC:
                    value = RenderingHints.VALUE_INTERPOLATION_BICUBIC;
                    break;
                default:
                    value = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
            }
            hints = new RenderingHints(RenderingHints.KEY_INTERPOLATION, value);
        }
        return hints;
    }
    public final AffineTransform getTransform() {
        return (AffineTransform)at.clone();
    }
    public final Point2D getPoint2D(Point2D srcPt, Point2D dstPt) {
        return at.transform(srcPt, dstPt);
    }
    public final Rectangle2D getBounds2D(BufferedImage src) {
        return getBounds2D(src.getRaster());
    }
    public final Rectangle2D getBounds2D(Raster src) {
        int width = src.getWidth();
        int height = src.getHeight();
        float[] corners = {
                0, 0, width, 0, width, height, 0, height
        };
        at.transform(corners, 0, corners, 0, 4);
        Rectangle2D.Float bounds = new Rectangle2D.Float(corners[0], corners[1], 0, 0);
        bounds.add(corners[2], corners[3]);
        bounds.add(corners[4], corners[5]);
        bounds.add(corners[6], corners[7]);
        return bounds;
    }
    public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel destCM) {
        Rectangle2D newBounds = getBounds2D(src);
        double dstWidth = newBounds.getX() + newBounds.getWidth();
        double dstHeight = newBounds.getY() + newBounds.getHeight();
        if (dstWidth <= 0 || dstHeight <= 0) {
            throw new RasterFormatException(Messages.getString("awt.251", dstWidth, dstHeight)); 
        }
        if (destCM != null) {
            return new BufferedImage(destCM, destCM.createCompatibleWritableRaster((int)dstWidth,
                    (int)dstHeight), destCM.isAlphaPremultiplied(), null);
        }
        ColorModel cm = src.getColorModel();
        if (iType != TYPE_NEAREST_NEIGHBOR && cm instanceof IndexColorModel) {
            return new BufferedImage((int)dstWidth, (int)dstHeight, BufferedImage.TYPE_INT_ARGB);
        }
        return new BufferedImage(cm, src.getRaster().createCompatibleWritableRaster((int)dstWidth,
                (int)dstHeight), cm.isAlphaPremultiplied(), null);
    }
    public WritableRaster createCompatibleDestRaster(Raster src) {
        Rectangle2D newBounds = getBounds2D(src);
        return src.createCompatibleWritableRaster((int)newBounds.getX(), (int)newBounds.getY(),
                (int)newBounds.getWidth(), (int)newBounds.getHeight());
    }
    public final BufferedImage filter(BufferedImage src, BufferedImage dst) {
        if (src == dst) {
            throw new IllegalArgumentException(Messages.getString("awt.252")); 
        }
        ColorModel srcCM = src.getColorModel();
        BufferedImage finalDst = null;
        if (srcCM instanceof IndexColorModel
                && (iType != TYPE_NEAREST_NEIGHBOR || srcCM.getPixelSize() % 8 != 0)) {
            src = ((IndexColorModel)srcCM).convertToIntDiscrete(src.getRaster(), true);
            srcCM = src.getColorModel();
        }
        if (dst == null) {
            dst = createCompatibleDestImage(src, srcCM);
        } else {
            if (!srcCM.equals(dst.getColorModel())) {
                if (!((src.getType() == BufferedImage.TYPE_INT_RGB || src.getType() == BufferedImage.TYPE_INT_ARGB) && (dst
                        .getType() == BufferedImage.TYPE_INT_RGB || dst.getType() == BufferedImage.TYPE_INT_ARGB))) {
                    finalDst = dst;
                    dst = createCompatibleDestImage(src, srcCM);
                }
            }
        }
        if (slowFilter(src.getRaster(), dst.getRaster()) != 0) {
            throw new ImagingOpException(Messages.getString("awt.21F")); 
        }
        if (finalDst != null) {
            Graphics2D g = finalDst.createGraphics();
            g.setComposite(AlphaComposite.Src);
            g.drawImage(dst, 0, 0, null);
        } else {
            finalDst = dst;
        }
        return finalDst;
    }
    public final WritableRaster filter(Raster src, WritableRaster dst) {
        if (src == dst) {
            throw new IllegalArgumentException(Messages.getString("awt.252")); 
        }
        if (dst == null) {
            dst = createCompatibleDestRaster(src);
        } else if (src.getNumBands() != dst.getNumBands()) {
            throw new IllegalArgumentException(Messages.getString("awt.253")); 
        }
        if (slowFilter(src, dst) != 0) {
            throw new ImagingOpException(Messages.getString("awt.21F")); 
        }
        return dst;
    }
    @SuppressWarnings("unused")
    private int ippFilter(Raster src, WritableRaster dst, int imageType) {
        int srcStride, dstStride;
        boolean skipChannel = false;
        int channels;
        int offsets[] = null;
        switch (imageType) {
            case BufferedImage.TYPE_INT_RGB:
            case BufferedImage.TYPE_INT_BGR: {
                channels = 4;
                srcStride = src.getWidth() * 4;
                dstStride = dst.getWidth() * 4;
                skipChannel = true;
                break;
            }
            case BufferedImage.TYPE_INT_ARGB:
            case BufferedImage.TYPE_INT_ARGB_PRE:
            case BufferedImage.TYPE_4BYTE_ABGR:
            case BufferedImage.TYPE_4BYTE_ABGR_PRE: {
                channels = 4;
                srcStride = src.getWidth() * 4;
                dstStride = dst.getWidth() * 4;
                break;
            }
            case BufferedImage.TYPE_BYTE_GRAY:
            case BufferedImage.TYPE_BYTE_INDEXED: {
                channels = 1;
                srcStride = src.getWidth();
                dstStride = dst.getWidth();
                break;
            }
            case BufferedImage.TYPE_3BYTE_BGR: {
                channels = 3;
                srcStride = src.getWidth() * 3;
                dstStride = dst.getWidth() * 3;
                break;
            }
            case BufferedImage.TYPE_USHORT_GRAY: 
            case BufferedImage.TYPE_USHORT_565_RGB:
            case BufferedImage.TYPE_USHORT_555_RGB:
            case BufferedImage.TYPE_BYTE_BINARY: {
                return slowFilter(src, dst);
            }
            default: {
                SampleModel srcSM = src.getSampleModel();
                SampleModel dstSM = dst.getSampleModel();
                if (srcSM instanceof PixelInterleavedSampleModel
                        && dstSM instanceof PixelInterleavedSampleModel) {
                    if (srcSM.getDataType() != DataBuffer.TYPE_BYTE
                            || dstSM.getDataType() != DataBuffer.TYPE_BYTE) {
                        return slowFilter(src, dst);
                    }
                    channels = srcSM.getNumBands(); 
                    if (channels != 1 && channels != 3 && channels != 4) {
                        return slowFilter(src, dst);
                    }
                    int dataTypeSize = DataBuffer.getDataTypeSize(srcSM.getDataType()) / 8;
                    srcStride = ((ComponentSampleModel)srcSM).getScanlineStride() * dataTypeSize;
                    dstStride = ((ComponentSampleModel)dstSM).getScanlineStride() * dataTypeSize;
                } else if (srcSM instanceof SinglePixelPackedSampleModel
                        && dstSM instanceof SinglePixelPackedSampleModel) {
                    SinglePixelPackedSampleModel sppsm1 = (SinglePixelPackedSampleModel)srcSM;
                    SinglePixelPackedSampleModel sppsm2 = (SinglePixelPackedSampleModel)dstSM;
                    if (sppsm1.getDataType() == DataBuffer.TYPE_USHORT) {
                        return slowFilter(src, dst);
                    }
                    channels = sppsm1.getNumBands();
                    if (channels != 1 && channels != 3 && channels != 4) {
                        return slowFilter(src, dst);
                    }
                    if (sppsm1.getDataType() != sppsm2.getDataType()
                            || !Arrays.equals(sppsm1.getBitOffsets(), sppsm2.getBitOffsets())
                            || !Arrays.equals(sppsm1.getBitMasks(), sppsm2.getBitMasks())) {
                        return slowFilter(src, dst);
                    }
                    for (int i = 0; i < channels; i++) {
                        if (sppsm1.getSampleSize(i) != 8) {
                            return slowFilter(src, dst);
                        }
                    }
                    if (channels == 3) {
                        channels = 4;
                    }
                    int dataTypeSize = DataBuffer.getDataTypeSize(sppsm1.getDataType()) / 8;
                    srcStride = sppsm1.getScanlineStride() * dataTypeSize;
                    dstStride = sppsm2.getScanlineStride() * dataTypeSize;
                } else {
                    return slowFilter(src, dst);
                }
                if (src.getParent() != null || dst.getParent() != null) {
                    if (src.getSampleModelTranslateX() != 0 || src.getSampleModelTranslateY() != 0
                            || dst.getSampleModelTranslateX() != 0
                            || dst.getSampleModelTranslateY() != 0) {
                        offsets = new int[4];
                        offsets[0] = -src.getSampleModelTranslateX() + src.getMinX();
                        offsets[1] = -src.getSampleModelTranslateY() + src.getMinY();
                        offsets[2] = -dst.getSampleModelTranslateX() + dst.getMinX();
                        offsets[3] = -dst.getSampleModelTranslateY() + dst.getMinY();
                    }
                }
            }
        }
        double m00 = at.getScaleX();
        double m01 = at.getShearX();
        double m02 = at.getTranslateX();
        double m10 = at.getShearY();
        double m11 = at.getScaleY();
        double m12 = at.getTranslateY();
        Object srcData, dstData;
        AwtImageBackdoorAccessor dbAccess = AwtImageBackdoorAccessor.getInstance();
        try {
            srcData = dbAccess.getData(src.getDataBuffer());
            dstData = dbAccess.getData(dst.getDataBuffer());
        } catch (IllegalArgumentException e) {
            return -1; 
        }
        return ippAffineTransform(m00, m01, m02, m10, m11, m12, srcData, src.getWidth(), src
                .getHeight(), srcStride, dstData, dst.getWidth(), dst.getHeight(), dstStride,
                iType, channels, skipChannel, offsets);
    }
    private int slowFilter(Raster src, WritableRaster dst) {
        Rectangle srcBounds = src.getBounds();
        Rectangle dstBounds = dst.getBounds();
        Rectangle normDstBounds = new Rectangle(0, 0, dstBounds.width, dstBounds.height);
        Rectangle bounds = getBounds2D(src).getBounds().intersection(normDstBounds);
        AffineTransform inv = null;
        try {
            inv = at.createInverse();
        } catch (NoninvertibleTransformException e) {
            return -1;
        }
        double[] m = new double[6];
        inv.getMatrix(m);
        int minSrcX = srcBounds.x;
        int minSrcY = srcBounds.y;
        int maxSrcX = srcBounds.x + srcBounds.width;
        int maxSrcY = srcBounds.y + srcBounds.height;
        int minX = bounds.x + dstBounds.x;
        int minY = bounds.y + dstBounds.y;
        int maxX = minX + bounds.width;
        int maxY = minY + bounds.height;
        int hx = (int)(m[0] * 256);
        int hy = (int)(m[1] * 256);
        int vx = (int)(m[2] * 256);
        int vy = (int)(m[3] * 256);
        int sx = (int)(m[4] * 256) + hx * bounds.x + vx * bounds.y + (srcBounds.x) * 256;
        int sy = (int)(m[5] * 256) + hy * bounds.x + vy * bounds.y + (srcBounds.y) * 256;
        vx -= hx * bounds.width;
        vy -= hy * bounds.width;
        if (src.getTransferType() == dst.getTransferType()) {
            for (int y = minY; y < maxY; y++) {
                for (int x = minX; x < maxX; x++) {
                    int px = sx >> 8;
                    int py = sy >> 8;
                    if (px >= minSrcX && py >= minSrcY && px < maxSrcX && py < maxSrcY) {
                        Object val = src.getDataElements(px, py, null);
                        dst.setDataElements(x, y, val);
                    }
                    sx += hx;
                    sy += hy;
                }
                sx += vx;
                sy += vy;
            }
        } else {
            float pixel[] = null;
            for (int y = minY; y < maxY; y++) {
                for (int x = minX; x < maxX; x++) {
                    int px = sx >> 8;
                    int py = sy >> 8;
                    if (px >= minSrcX && py >= minSrcY && px < maxSrcX && py < maxSrcY) {
                        pixel = src.getPixel(px, py, pixel);
                        dst.setPixel(x, y, pixel);
                    }
                    sx += hx;
                    sy += hy;
                }
                sx += vx;
                sy += vy;
            }
        }
        return 0;
    }
    private native int ippAffineTransform(double m00, double m01, double m02, double m10,
            double m11, double m12, Object src, int srcWidth, int srcHeight, int srcStride,
            Object dst, int dstWidth, int dstHeight, int dstStride, int iType, int channels,
            boolean skipChannel, int offsets[]);
}