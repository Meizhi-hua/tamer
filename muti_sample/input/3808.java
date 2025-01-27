public class WBMPImageWriter extends ImageWriter {
    private ImageOutputStream stream = null;
    private static int getNumBits(int intValue) {
        int numBits = 32;
        int mask = 0x80000000;
        while(mask != 0 && (intValue & mask) == 0) {
            numBits--;
            mask >>>= 1;
        }
        return numBits;
    }
    private static byte[] intToMultiByte(int intValue) {
        int numBitsLeft = getNumBits(intValue);
        byte[] multiBytes = new byte[(numBitsLeft + 6)/7];
        int maxIndex = multiBytes.length - 1;
        for(int b = 0; b <= maxIndex; b++) {
            multiBytes[b] = (byte)((intValue >>> ((maxIndex - b)*7))&0x7f);
            if(b != maxIndex) {
                multiBytes[b] |= (byte)0x80;
            }
        }
        return multiBytes;
    }
    public WBMPImageWriter(ImageWriterSpi originator) {
        super(originator);
    }
    public void setOutput(Object output) {
        super.setOutput(output); 
        if (output != null) {
            if (!(output instanceof ImageOutputStream))
                throw new IllegalArgumentException(I18N.getString("WBMPImageWriter"));
            this.stream = (ImageOutputStream)output;
        } else
            this.stream = null;
    }
    public IIOMetadata getDefaultStreamMetadata(ImageWriteParam param) {
        return null;
    }
    public IIOMetadata getDefaultImageMetadata(ImageTypeSpecifier imageType,
                                               ImageWriteParam param) {
        WBMPMetadata meta = new WBMPMetadata();
        meta.wbmpType = 0; 
        return meta;
    }
    public IIOMetadata convertStreamMetadata(IIOMetadata inData,
                                             ImageWriteParam param) {
        return null;
    }
    public IIOMetadata convertImageMetadata(IIOMetadata metadata,
                                            ImageTypeSpecifier type,
                                            ImageWriteParam param) {
        return null;
    }
    public boolean canWriteRasters() {
        return true;
    }
    public void write(IIOMetadata streamMetadata,
                      IIOImage image,
                      ImageWriteParam param) throws IOException {
        if (stream == null) {
            throw new IllegalStateException(I18N.getString("WBMPImageWriter3"));
        }
        if (image == null) {
            throw new IllegalArgumentException(I18N.getString("WBMPImageWriter4"));
        }
        clearAbortRequest();
        processImageStarted(0);
        if (param == null)
            param = getDefaultWriteParam();
        RenderedImage input = null;
        Raster inputRaster = null;
        boolean writeRaster = image.hasRaster();
        Rectangle sourceRegion = param.getSourceRegion();
        SampleModel sampleModel = null;
        if (writeRaster) {
            inputRaster = image.getRaster();
            sampleModel = inputRaster.getSampleModel();
        } else {
            input = image.getRenderedImage();
            sampleModel = input.getSampleModel();
            inputRaster = input.getData();
        }
        checkSampleModel(sampleModel);
        if (sourceRegion == null)
            sourceRegion = inputRaster.getBounds();
        else
            sourceRegion = sourceRegion.intersection(inputRaster.getBounds());
        if (sourceRegion.isEmpty())
            throw new RuntimeException(I18N.getString("WBMPImageWriter1"));
        int scaleX = param.getSourceXSubsampling();
        int scaleY = param.getSourceYSubsampling();
        int xOffset = param.getSubsamplingXOffset();
        int yOffset = param.getSubsamplingYOffset();
        sourceRegion.translate(xOffset, yOffset);
        sourceRegion.width -= xOffset;
        sourceRegion.height -= yOffset;
        int minX = sourceRegion.x / scaleX;
        int minY = sourceRegion.y / scaleY;
        int w = (sourceRegion.width + scaleX - 1) / scaleX;
        int h = (sourceRegion.height + scaleY - 1) / scaleY;
        Rectangle destinationRegion = new Rectangle(minX, minY, w, h);
        sampleModel = sampleModel.createCompatibleSampleModel(w, h);
        SampleModel destSM= sampleModel;
        if(sampleModel.getDataType() != DataBuffer.TYPE_BYTE ||
           !(sampleModel instanceof MultiPixelPackedSampleModel) ||
           ((MultiPixelPackedSampleModel)sampleModel).getDataBitOffset() != 0) {
           destSM =
                new MultiPixelPackedSampleModel(DataBuffer.TYPE_BYTE,
                                                w, h, 1,
                                                w + 7 >> 3, 0);
        }
        if (!destinationRegion.equals(sourceRegion)) {
            if (scaleX == 1 && scaleY == 1)
                inputRaster = inputRaster.createChild(inputRaster.getMinX(),
                                                      inputRaster.getMinY(),
                                                      w, h, minX, minY, null);
            else {
                WritableRaster ras = Raster.createWritableRaster(destSM,
                                                                 new Point(minX, minY));
                byte[] data = ((DataBufferByte)ras.getDataBuffer()).getData();
                for(int j = minY, y = sourceRegion.y, k = 0;
                    j < minY + h; j++, y += scaleY) {
                    for (int i = 0, x = sourceRegion.x;
                        i <w; i++, x +=scaleX) {
                        int v = inputRaster.getSample(x, y, 0);
                        data[k + (i >> 3)] |= v << (7 - (i & 7));
                    }
                    k += w + 7 >> 3;
                }
                inputRaster = ras;
            }
        }
        if(!destSM.equals(inputRaster.getSampleModel())) {
            WritableRaster raster =
                Raster.createWritableRaster(destSM,
                                            new Point(inputRaster.getMinX(),
                                                      inputRaster.getMinY()));
            raster.setRect(inputRaster);
            inputRaster = raster;
        }
        boolean isWhiteZero = false;
        if(!writeRaster && input.getColorModel() instanceof IndexColorModel) {
            IndexColorModel icm = (IndexColorModel)input.getColorModel();
            isWhiteZero = icm.getRed(0) > icm.getRed(1);
        }
        int lineStride =
            ((MultiPixelPackedSampleModel)destSM).getScanlineStride();
        int bytesPerRow = (w + 7)/8;
        byte[] bdata = ((DataBufferByte)inputRaster.getDataBuffer()).getData();
        stream.write(0); 
        stream.write(0); 
        stream.write(intToMultiByte(w)); 
        stream.write(intToMultiByte(h)); 
        if(!isWhiteZero && lineStride == bytesPerRow) {
            stream.write(bdata, 0, h * bytesPerRow);
            processImageProgress(100.0F);
        } else {
            int offset = 0;
            if(!isWhiteZero) {
                for(int row = 0; row < h; row++) {
                    if (abortRequested())
                        break;
                    stream.write(bdata, offset, bytesPerRow);
                    offset += lineStride;
                    processImageProgress(100.0F * row / h);
                }
            } else {
                byte[] inverted = new byte[bytesPerRow];
                for(int row = 0; row < h; row++) {
                    if (abortRequested())
                        break;
                    for(int col = 0; col < bytesPerRow; col++) {
                        inverted[col] = (byte)(~(bdata[col+offset]));
                    }
                    stream.write(inverted, 0, bytesPerRow);
                    offset += lineStride;
                    processImageProgress(100.0F * row / h);
                }
            }
        }
        if (abortRequested())
            processWriteAborted();
        else {
            processImageComplete();
            stream.flushBefore(stream.getStreamPosition());
        }
    }
    public void reset() {
        super.reset();
        stream = null;
    }
    private void checkSampleModel(SampleModel sm) {
        int type = sm.getDataType();
        if (type < DataBuffer.TYPE_BYTE || type > DataBuffer.TYPE_INT
            || sm.getNumBands() != 1 || sm.getSampleSize(0) != 1)
            throw new IllegalArgumentException(I18N.getString("WBMPImageWriter2"));
    }
}
