public class BufferedImageFilter extends ImageFilter implements Cloneable {
    private static final AwtImageBackdoorAccessor accessor = AwtImageBackdoorAccessor.getInstance();
    private BufferedImageOp op;
    private WritableRaster raster;
    private int iData[];
    private byte bData[];
    private int width;
    private int height;
    private ColorModel cm;
    private boolean forcedRGB = false;
    private int transferType = DataBuffer.TYPE_UNDEFINED;
    public BufferedImageFilter(BufferedImageOp op) {
        if (op == null) {
            throw new NullPointerException(Messages.getString("awt.05")); 
        }
        this.op = op;
    }
    public BufferedImageOp getBufferedImageOp() {
        return op;
    }
    @Override
    public void setDimensions(int width, int height) {
        this.width = width;
        this.height = height;
        if (width <= 0 || height <= 0) {
            consumer.imageComplete(ImageConsumer.STATICIMAGEDONE);
            reset();
        }
    }
    @Override
    public void setColorModel(ColorModel model) {
        if (this.cm != null && this.cm != model && raster != null) {
            forceRGB();
        } else {
            this.cm = model;
        }
    }
    @Override
    public void setPixels(int x, int y, int w, int h, ColorModel model, byte[] pixels, int off,
            int scansize) {
        setPixels(x, y, w, h, model, pixels, off, scansize, true);
    }
    @Override
    public void setPixels(int x, int y, int w, int h, ColorModel model, int[] pixels, int off,
            int scansize) {
        setPixels(x, y, w, h, model, pixels, off, scansize, false);
    }
    @Override
    public void imageComplete(int status) {
        if (status == STATICIMAGEDONE || status == SINGLEFRAMEDONE) {
            BufferedImage bim = new BufferedImage(cm, raster, cm.isAlphaPremultiplied, null);
            bim = op.filter(bim, null);
            DataBuffer dstDb = bim.getRaster().getDataBuffer();
            ColorModel dstCm = bim.getColorModel();
            int dstW = bim.getWidth();
            int dstH = bim.getHeight();
            consumer.setDimensions(dstW, dstH);
            if (dstDb.getDataType() == DataBuffer.TYPE_INT) {
                consumer.setColorModel(dstCm);
                consumer.setPixels(0, 0, dstW, dstH, dstCm, accessor.getDataInt(dstDb), 0, dstW);
            } else if (dstDb.getDataType() == DataBuffer.TYPE_BYTE) {
                consumer.setColorModel(dstCm);
                consumer.setPixels(0, 0, dstW, dstH, dstCm, accessor.getDataByte(dstDb), 0, dstW);
            } else {
                int dstData[] = bim.getRGB(0, 0, dstW, dstH, null, 0, dstW);
                dstCm = ColorModel.getRGBdefault();
                consumer.setColorModel(dstCm);
                consumer.setPixels(0, 0, dstW, dstH, dstCm, dstData, 0, dstW);
            }
        } else if (status == IMAGEERROR || status == IMAGEABORTED) {
            reset();
        }
        consumer.imageComplete(status);
    }
    private void setPixels(int x, int y, int w, int h, ColorModel model, Object pixels, int off,
            int scansize, boolean isByteData) {
        if (x < 0) {
            w -= x;
            off += x;
            x = 0;
        }
        if (y < 0) {
            h -= y;
            off += y * scansize;
            y = 0;
        }
        if (x + w > width) {
            w = width - x;
        }
        if (y + h > height) {
            h = height - y;
        }
        if (w <= 0 || h <= 0) {
            return;
        }
        if (this.cm == null) {
            setColorModel(model);
        } else if (model == null) {
            model = this.cm;
        } else if (!model.equals(this.cm)) {
            forceRGB();
        }
        boolean canArraycopy;
        switch (transferType) {
            case DataBuffer.TYPE_UNDEFINED: {
                if (isByteData) {
                    transferType = DataBuffer.TYPE_BYTE;
                    createRaster(transferType);
                    canArraycopy = !forcedRGB;
                    break;
                }
                transferType = DataBuffer.TYPE_INT;
                createRaster(transferType);
                canArraycopy = !forcedRGB || model.equals(ColorModel.getRGBdefault());
                break;
            } 
            case DataBuffer.TYPE_INT: {
                if (isByteData) { 
                    forceRGB();
                    canArraycopy = false;
                    break;
                } else if (!forcedRGB || model.equals(ColorModel.getRGBdefault())) {
                    canArraycopy = true;
                    break;
                } 
            }
            case DataBuffer.TYPE_BYTE: {
                if (isByteData && !forcedRGB) {
                    canArraycopy = true;
                    break;
                }
                canArraycopy = false;
                break;
            }
            default: {
                throw new IllegalStateException(Messages.getString("awt.06")); 
            }
        }
        off += x;
        int maxOffset = off + h * scansize;
        int dstOffset = x + y * width;
        if (canArraycopy) {
            Object dstArray = isByteData ? (Object)bData : (Object)iData;
            for (; off < maxOffset; off += scansize, dstOffset += width) {
                System.arraycopy(pixels, off, dstArray, dstOffset, w);
            }
        } else {
            for (; off < maxOffset; off += scansize, dstOffset += width) {
                int srcPos = off;
                int dstPos = dstOffset;
                int maxDstPos = dstOffset + w;
                for (; dstPos < maxDstPos; dstPos++, srcPos++) {
                    iData[dstPos] = model.getRGB(isByteData ? ((byte[])pixels)[srcPos]
                            : ((int[])pixels)[srcPos]);
                }
            }
        }
    }
    private void forceRGB() {
        if (!forcedRGB) {
            forcedRGB = true;
            int size = width * height;
            int rgbData[] = new int[size];
            if (bData != null) {
                for (int i = 0; i < size; i++) {
                    rgbData[i] = cm.getRGB(bData[i]);
                }
            } else if (iData != null) {
                for (int i = 0; i < size; i++) {
                    rgbData[i] = cm.getRGB(iData[i]);
                }
            }
            cm = ColorModel.getRGBdefault();
            DataBufferInt db = new DataBufferInt(rgbData, size);
            int masks[] = new int[] {
                    0x00ff0000, 0x0000ff00, 0x000000ff, 0xff000000
            };
            raster = Raster.createPackedRaster(db, width, height, width, masks, null);
            iData = accessor.getDataInt(db);
            bData = null;
            transferType = DataBuffer.TYPE_INT;
        }
    }
    private void reset() {
        width = 0;
        height = 0;
        forcedRGB = false;
        cm = null;
        iData = null;
        bData = null;
        transferType = DataBuffer.TYPE_UNDEFINED;
        raster = null;
    }
    private void createRaster(int dataType) {
        boolean createdValidBuffer = false;
        try {
            raster = cm.createCompatibleWritableRaster(width, height);
            int rasterType = raster.getDataBuffer().getDataType();
            if (rasterType == dataType) {
                switch (rasterType) {
                    case DataBuffer.TYPE_INT: {
                        iData = accessor.getDataInt(raster.getDataBuffer());
                        if (iData != null) {
                            createdValidBuffer = true;
                        }
                        break;
                    }
                    case DataBuffer.TYPE_BYTE: {
                        bData = accessor.getDataByte(raster.getDataBuffer());
                        if (bData != null) {
                            createdValidBuffer = true;
                        }
                        break;
                    }
                    default:
                        createdValidBuffer = false;
                }
                if (cm == ColorModel.getRGBdefault()) {
                    forcedRGB = true;
                }
            } else {
                createdValidBuffer = false;
            }
        } catch (Exception e) {
            createdValidBuffer = false;
        }
        if (createdValidBuffer == false) {
            cm = ColorModel.getRGBdefault();
            raster = cm.createCompatibleWritableRaster(width, height);
            iData = accessor.getDataInt(raster.getDataBuffer());
            bData = null;
            forcedRGB = true;
        }
    }
}