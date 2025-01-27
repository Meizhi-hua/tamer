public class RescaleOp implements BufferedImageOp, RasterOp {
    private float scaleFactors[];
    private float offsets[];
    private RenderingHints hints;
    static {
    }
    public RescaleOp(float[] scaleFactors, float[] offsets, RenderingHints hints) {
        int numFactors = Math.min(scaleFactors.length, offsets.length);
        this.scaleFactors = new float[numFactors];
        this.offsets = new float[numFactors];
        System.arraycopy(scaleFactors, 0, this.scaleFactors, 0, numFactors);
        System.arraycopy(offsets, 0, this.offsets, 0, numFactors);
        this.hints = hints;
    }
    public RescaleOp(float scaleFactor, float offset, RenderingHints hints) {
        scaleFactors = new float[1];
        offsets = new float[1];
        scaleFactors[0] = scaleFactor;
        offsets[0] = offset;
        this.hints = hints;
    }
    public final int getNumFactors() {
        return scaleFactors.length;
    }
    public final RenderingHints getRenderingHints() {
        return hints;
    }
    public final float[] getScaleFactors(float[] scaleFactors) {
        if (scaleFactors == null) {
            scaleFactors = new float[this.scaleFactors.length];
        }
        int minLength = Math.min(scaleFactors.length, this.scaleFactors.length);
        System.arraycopy(this.scaleFactors, 0, scaleFactors, 0, minLength);
        return scaleFactors;
    }
    public final float[] getOffsets(float[] offsets) {
        if (offsets == null) {
            offsets = new float[this.offsets.length];
        }
        int minLength = Math.min(offsets.length, this.offsets.length);
        System.arraycopy(this.offsets, 0, offsets, 0, minLength);
        return offsets;
    }
    public final Point2D getPoint2D(Point2D srcPt, Point2D dstPt) {
        if (dstPt == null) {
            dstPt = new Point2D.Float();
        }
        dstPt.setLocation(srcPt);
        return dstPt;
    }
    public final Rectangle2D getBounds2D(Raster src) {
        return src.getBounds();
    }
    public final Rectangle2D getBounds2D(BufferedImage src) {
        return getBounds2D(src.getRaster());
    }
    public WritableRaster createCompatibleDestRaster(Raster src) {
        return src.createCompatibleWritableRaster();
    }
    public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel dstCM) {
        if (dstCM == null) {
            dstCM = src.getColorModel();
        }
        if (dstCM instanceof IndexColorModel) {
            dstCM = ColorModel.getRGBdefault();
        }
        WritableRaster r = dstCM.isCompatibleSampleModel(src.getSampleModel()) ? src.getRaster()
                .createCompatibleWritableRaster(src.getWidth(), src.getHeight()) : dstCM
                .createCompatibleWritableRaster(src.getWidth(), src.getHeight());
        return new BufferedImage(dstCM, r, dstCM.isAlphaPremultiplied(), null);
    }
    public final WritableRaster filter(Raster src, WritableRaster dst) {
        if (dst == null) {
            dst = createCompatibleDestRaster(src);
        } else {
            if (src.getNumBands() != dst.getNumBands()) {
                throw new IllegalArgumentException(Messages.getString("awt.21D", 
                        src.getNumBands(), dst.getNumBands()));
            }
        }
        if (this.scaleFactors.length != 1 && this.scaleFactors.length != src.getNumBands()) {
            throw new IllegalArgumentException(Messages.getString("awt.21E")); 
        }
        if (slowFilter(src, dst, false) != 0) {
            throw new ImagingOpException(Messages.getString("awt.21F")); 
        }
        return dst;
    }
    private final int slowFilter(Raster src, WritableRaster dst, boolean skipAlpha) {
        SampleModel sm = src.getSampleModel();
        int numBands = src.getNumBands();
        int srcHeight = src.getHeight();
        int srcWidth = src.getWidth();
        int srcMinX = src.getMinX();
        int srcMinY = src.getMinY();
        int dstMinX = dst.getMinX();
        int dstMinY = dst.getMinY();
        int[] maxValues = new int[numBands];
        int[] masks = new int[numBands];
        int[] sampleSizes = sm.getSampleSize();
        for (int i = 0; i < numBands; i++) {
            maxValues[i] = (1 << sampleSizes[i]) - 1;
            masks[i] = ~(maxValues[i]);
        }
        float[] pixels = null;
        pixels = src.getPixels(srcMinX, srcMinY, srcWidth, srcHeight, pixels);
        if (skipAlpha) { 
            if (scaleFactors.length > 1) {
                for (int i = 0; i < pixels.length;) {
                    for (int bandIdx = 0; bandIdx < numBands - 1; bandIdx++, i++) {
                        pixels[i] = pixels[i] * scaleFactors[bandIdx] + offsets[bandIdx];
                        if (((int)pixels[i] & masks[bandIdx]) != 0) {
                            if (pixels[i] < 0) {
                                pixels[i] = 0;
                            } else {
                                pixels[i] = maxValues[bandIdx];
                            }
                        }
                    }
                    i++;
                }
            } else {
                for (int i = 0; i < pixels.length;) {
                    for (int bandIdx = 0; bandIdx < numBands - 1; bandIdx++, i++) {
                        pixels[i] = pixels[i] * scaleFactors[0] + offsets[0];
                        if (((int)pixels[i] & masks[bandIdx]) != 0) {
                            if (pixels[i] < 0) {
                                pixels[i] = 0;
                            } else {
                                pixels[i] = maxValues[bandIdx];
                            }
                        }
                    }
                    i++;
                }
            }
        } else {
            if (scaleFactors.length > 1) {
                for (int i = 0; i < pixels.length;) {
                    for (int bandIdx = 0; bandIdx < numBands; bandIdx++, i++) {
                        pixels[i] = pixels[i] * scaleFactors[bandIdx] + offsets[bandIdx];
                        if (((int)pixels[i] & masks[bandIdx]) != 0) {
                            if (pixels[i] < 0) {
                                pixels[i] = 0;
                            } else {
                                pixels[i] = maxValues[bandIdx];
                            }
                        }
                    }
                }
            } else {
                for (int i = 0; i < pixels.length;) {
                    for (int bandIdx = 0; bandIdx < numBands; bandIdx++, i++) {
                        pixels[i] = pixels[i] * scaleFactors[0] + offsets[0];
                        if (((int)pixels[i] & masks[bandIdx]) != 0) {
                            if (pixels[i] < 0) {
                                pixels[i] = 0;
                            } else {
                                pixels[i] = maxValues[bandIdx];
                            }
                        }
                    }
                }
            }
        }
        dst.setPixels(dstMinX, dstMinY, srcWidth, srcHeight, pixels);
        return 0;
    }
    public final BufferedImage filter(BufferedImage src, BufferedImage dst) {
        ColorModel srcCM = src.getColorModel();
        if (srcCM instanceof IndexColorModel) {
            throw new IllegalArgumentException(Messages.getString("awt.220")); 
        }
        int nComponents = srcCM.getNumComponents();
        boolean skipAlpha;
        if (srcCM.hasAlpha()) {
            if (scaleFactors.length == 1 || scaleFactors.length == nComponents - 1) {
                skipAlpha = true;
            } else if (scaleFactors.length == nComponents) {
                skipAlpha = false;
            } else {
                throw new IllegalArgumentException(Messages.getString("awt.21E")); 
            }
        } else if (scaleFactors.length == 1 || scaleFactors.length == nComponents) {
            skipAlpha = false;
        } else {
            throw new IllegalArgumentException(Messages.getString("awt.21E")); 
        }
        BufferedImage finalDst = null;
        if (dst == null) {
            finalDst = dst;
            dst = createCompatibleDestImage(src, srcCM);
        } else if (!srcCM.equals(dst.getColorModel())) {
            if (!((src.getType() == BufferedImage.TYPE_INT_RGB || src.getType() == BufferedImage.TYPE_INT_ARGB) && (dst
                    .getType() == BufferedImage.TYPE_INT_RGB || dst.getType() == BufferedImage.TYPE_INT_ARGB))) {
                finalDst = dst;
                dst = createCompatibleDestImage(src, srcCM);
            }
        }
        if (slowFilter(src.getRaster(), dst.getRaster(), skipAlpha) != 0) {
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
    private final void createLevels(SampleModel sm, int numBands, boolean skipAlpha, int levels[],
            int values[], int channelsOrder[]) {
        int maxValue = (1 << sm.getSampleSize(0)) - 1;
        float extScaleFactors[] = new float[numBands];
        float extOffsets[] = new float[numBands];
        if (scaleFactors.length != 1) {
            System.arraycopy(scaleFactors, 0, extScaleFactors, 0, scaleFactors.length);
            System.arraycopy(offsets, 0, extOffsets, 0, scaleFactors.length);
        } else {
            for (int i = 0; i < numBands; i++) {
                extScaleFactors[i] = scaleFactors[0];
                extOffsets[i] = offsets[0];
            }
        }
        if (skipAlpha) {
            extScaleFactors[numBands - 1] = 1;
            extOffsets[numBands - 1] = 0;
        }
        for (int i = 0; i < numBands; i++) {
            if (extScaleFactors[i] == 0) {
                levels[i * 4] = 0;
                levels[i * 4 + 1] = 0;
                levels[i * 4 + 2] = maxValue + 1;
                levels[i * 4 + 3] = maxValue + 1;
            }
            float minLevel = -extOffsets[i] / extScaleFactors[i];
            float maxLevel = (maxValue - extOffsets[i]) / extScaleFactors[i];
            if (minLevel < 0) {
                minLevel = 0;
            } else if (minLevel > maxValue) {
                minLevel = maxValue;
            }
            if (maxLevel < 0) {
                maxLevel = 0;
            } else if (maxLevel > maxValue) {
                maxLevel = maxValue;
            }
            levels[i * 4] = 0;
            if (minLevel > maxLevel) {
                levels[i * 4 + 1] = (int)maxLevel;
                levels[i * 4 + 2] = (int)minLevel;
            } else {
                levels[i * 4 + 1] = (int)minLevel;
                levels[i * 4 + 2] = (int)maxLevel;
            }
            levels[i * 4 + 3] = maxValue + 1;
            for (int k = 0; k < 4; k++) {
                int idx = i * 4 + k;
                values[idx] = (int)(extScaleFactors[i] * levels[idx] + extOffsets[i]);
                if (values[idx] < 0) {
                    values[idx] = 0;
                } else if (values[idx] > maxValue) {
                    values[idx] = maxValue;
                }
            }
        }
        if (channelsOrder != null) {
            int len = numBands * 4;
            int savedLevels[] = new int[len];
            int savedValues[] = new int[len];
            System.arraycopy(levels, 0, savedLevels, 0, len);
            System.arraycopy(values, 0, savedValues, 0, len);
            for (int i = 0; i < channelsOrder.length; i++) {
                System.arraycopy(savedLevels, i * 4, levels, channelsOrder[i] * 4, 4);
                System.arraycopy(savedValues, i * 4, values, channelsOrder[i] * 4, 4);
            }
        }
    }
    @SuppressWarnings("unused")
    private final int ippFilter(Raster src, WritableRaster dst, int imageType, boolean skipAlpha) {
        int res;
        int srcStride, dstStride;
        int channels;
        int offsets[] = null;
        int channelsOrder[] = null;
        switch (imageType) {
            case BufferedImage.TYPE_INT_ARGB:
            case BufferedImage.TYPE_INT_ARGB_PRE:
            case BufferedImage.TYPE_INT_RGB: {
                channels = 4;
                srcStride = src.getWidth() * 4;
                dstStride = dst.getWidth() * 4;
                channelsOrder = new int[] {
                        2, 1, 0, 3
                };
                break;
            }
            case BufferedImage.TYPE_4BYTE_ABGR:
            case BufferedImage.TYPE_4BYTE_ABGR_PRE:
            case BufferedImage.TYPE_INT_BGR: {
                channels = 4;
                srcStride = src.getWidth() * 4;
                dstStride = dst.getWidth() * 4;
                break;
            }
            case BufferedImage.TYPE_BYTE_GRAY: {
                channels = 1;
                srcStride = src.getWidth();
                dstStride = dst.getWidth();
                break;
            }
            case BufferedImage.TYPE_3BYTE_BGR: {
                channels = 3;
                srcStride = src.getWidth() * 3;
                dstStride = dst.getWidth() * 3;
                channelsOrder = new int[] {
                        2, 1, 0
                };
                break;
            }
            case BufferedImage.TYPE_USHORT_GRAY:
            case BufferedImage.TYPE_USHORT_565_RGB:
            case BufferedImage.TYPE_USHORT_555_RGB:
            case BufferedImage.TYPE_BYTE_BINARY: {
                return slowFilter(src, dst, skipAlpha);
            }
            default: {
                SampleModel srcSM = src.getSampleModel();
                SampleModel dstSM = dst.getSampleModel();
                if (srcSM instanceof PixelInterleavedSampleModel
                        && dstSM instanceof PixelInterleavedSampleModel) {
                    if (srcSM.getDataType() != DataBuffer.TYPE_BYTE
                            || dstSM.getDataType() != DataBuffer.TYPE_BYTE) {
                        return slowFilter(src, dst, skipAlpha);
                    }
                    channels = srcSM.getNumBands(); 
                    if (!(channels == 1 || channels == 3 || channels == 4)) {
                        return slowFilter(src, dst, skipAlpha);
                    }
                    srcStride = ((ComponentSampleModel)srcSM).getScanlineStride();
                    dstStride = ((ComponentSampleModel)dstSM).getScanlineStride();
                    channelsOrder = ((ComponentSampleModel)srcSM).getBandOffsets();
                } else if (srcSM instanceof SinglePixelPackedSampleModel
                        && dstSM instanceof SinglePixelPackedSampleModel) {
                    SinglePixelPackedSampleModel sppsm1 = (SinglePixelPackedSampleModel)srcSM;
                    SinglePixelPackedSampleModel sppsm2 = (SinglePixelPackedSampleModel)dstSM;
                    channels = sppsm1.getNumBands();
                    if (sppsm1.getDataType() != DataBuffer.TYPE_INT
                            || sppsm2.getDataType() != DataBuffer.TYPE_INT
                            || !(channels == 3 || channels == 4)) {
                        return slowFilter(src, dst, skipAlpha);
                    }
                    if (!Arrays.equals(sppsm1.getBitOffsets(), sppsm2.getBitOffsets())
                            || !Arrays.equals(sppsm1.getBitMasks(), sppsm2.getBitMasks())) {
                        return slowFilter(src, dst, skipAlpha);
                    }
                    for (int i = 0; i < channels; i++) {
                        if (sppsm1.getSampleSize(i) != 8) {
                            return slowFilter(src, dst, skipAlpha);
                        }
                    }
                    channelsOrder = new int[channels];
                    int bitOffsets[] = sppsm1.getBitOffsets();
                    for (int i = 0; i < channels; i++) {
                        channelsOrder[i] = bitOffsets[i] / 8;
                    }
                    if (channels == 3) { 
                        channels = 4;
                    }
                    srcStride = sppsm1.getScanlineStride() * 4;
                    dstStride = sppsm2.getScanlineStride() * 4;
                } else {
                    return slowFilter(src, dst, skipAlpha);
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
        int levels[] = new int[4 * channels];
        int values[] = new int[4 * channels];
        createLevels(src.getSampleModel(), channels, skipAlpha, levels, values, channelsOrder);
        Object srcData, dstData;
        AwtImageBackdoorAccessor dbAccess = AwtImageBackdoorAccessor.getInstance();
        try {
            srcData = dbAccess.getData(src.getDataBuffer());
            dstData = dbAccess.getData(dst.getDataBuffer());
        } catch (IllegalArgumentException e) {
            return -1; 
        }
        res = LookupOp.ippLUT(srcData, src.getWidth(), src.getHeight(), srcStride, dstData, dst
                .getWidth(), dst.getHeight(), dstStride, levels, values, channels, offsets, true);
        return res;
    }
}
