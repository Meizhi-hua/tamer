public abstract class PackedColorModel extends ColorModel {
    int componentMasks[];
    int offsets[];
    float scales[];
    public PackedColorModel(ColorSpace space, int bits, int colorMaskArray[], int alphaMask,
            boolean isAlphaPremultiplied, int trans, int transferType) {
        super(bits, createBits(colorMaskArray, alphaMask), space, (alphaMask == 0 ? false : true),
                isAlphaPremultiplied, trans, validateTransferType(transferType));
        if (pixel_bits < 1 || pixel_bits > 32) {
            throw new IllegalArgumentException(Messages.getString("awt.236")); 
        }
        componentMasks = new int[numComponents];
        for (int i = 0; i < numColorComponents; i++) {
            componentMasks[i] = colorMaskArray[i];
        }
        if (hasAlpha) {
            componentMasks[numColorComponents] = alphaMask;
            if (this.bits[numColorComponents] == 1) {
                transparency = Transparency.BITMASK;
            }
        }
        parseComponents();
    }
    public PackedColorModel(ColorSpace space, int bits, int rmask, int gmask, int bmask, int amask,
            boolean isAlphaPremultiplied, int trans, int transferType) {
        super(bits, createBits(rmask, gmask, bmask, amask), space, (amask == 0 ? false : true),
                isAlphaPremultiplied, trans, validateTransferType(transferType));
        if (pixel_bits < 1 || pixel_bits > 32) {
            throw new IllegalArgumentException(Messages.getString("awt.236")); 
        }
        if (cs.getType() != ColorSpace.TYPE_RGB) {
            throw new IllegalArgumentException(Messages.getString("awt.239")); 
        }
        for (int i = 0; i < numColorComponents; i++) {
            if (cs.getMinValue(i) != 0.0f || cs.getMaxValue(i) != 1.0f) {
                throw new IllegalArgumentException(Messages.getString("awt.23A")); 
            }
        }
        componentMasks = new int[numComponents];
        componentMasks[0] = rmask;
        componentMasks[1] = gmask;
        componentMasks[2] = bmask;
        if (hasAlpha) {
            componentMasks[3] = amask;
            if (this.bits[3] == 1) {
                transparency = Transparency.BITMASK;
            }
        }
        parseComponents();
    }
    @Override
    public WritableRaster getAlphaRaster(WritableRaster raster) {
        if (!hasAlpha) {
            return null;
        }
        int x = raster.getMinX();
        int y = raster.getMinY();
        int w = raster.getWidth();
        int h = raster.getHeight();
        int band[] = new int[1];
        band[0] = raster.getNumBands() - 1;
        return raster.createWritableChild(x, y, w, h, x, y, band);
    }
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof PackedColorModel)) {
            return false;
        }
        PackedColorModel cm = (PackedColorModel)obj;
        return (pixel_bits == cm.getPixelSize() && transferType == cm.getTransferType()
                && cs.getType() == cm.getColorSpace().getType() && hasAlpha == cm.hasAlpha()
                && isAlphaPremultiplied == cm.isAlphaPremultiplied()
                && transparency == cm.getTransparency()
                && numColorComponents == cm.getNumColorComponents()
                && numComponents == cm.getNumComponents()
                && Arrays.equals(bits, cm.getComponentSize()) && Arrays.equals(componentMasks, cm
                .getMasks()));
    }
    @Override
    public boolean isCompatibleSampleModel(SampleModel sm) {
        if (sm == null) {
            return false;
        }
        if (!(sm instanceof SinglePixelPackedSampleModel)) {
            return false;
        }
        SinglePixelPackedSampleModel esm = (SinglePixelPackedSampleModel)sm;
        return ((esm.getNumBands() == numComponents) && (esm.getTransferType() == transferType) && Arrays
                .equals(esm.getBitMasks(), componentMasks));
    }
    @Override
    public SampleModel createCompatibleSampleModel(int w, int h) {
        return new SinglePixelPackedSampleModel(transferType, w, h, componentMasks);
    }
    public final int getMask(int index) {
        return componentMasks[index];
    }
    public final int[] getMasks() {
        return (componentMasks.clone());
    }
    private static int[] createBits(int colorMaskArray[], int alphaMask) {
        int bits[];
        int numComp;
        if (alphaMask == 0) {
            numComp = colorMaskArray.length;
        } else {
            numComp = colorMaskArray.length + 1;
        }
        bits = new int[numComp];
        int i = 0;
        for (; i < colorMaskArray.length; i++) {
            bits[i] = countCompBits(colorMaskArray[i]);
            if (bits[i] < 0) {
                throw new IllegalArgumentException(Messages.getString("awt.23B", i)); 
            }
        }
        if (i < numComp) {
            bits[i] = countCompBits(alphaMask);
            if (bits[i] < 0) {
                throw new IllegalArgumentException(Messages.getString("awt.23C")); 
            }
        }
        return bits;
    }
    private static int[] createBits(int rmask, int gmask, int bmask, int amask) {
        int numComp;
        if (amask == 0) {
            numComp = 3;
        } else {
            numComp = 4;
        }
        int bits[] = new int[numComp];
        bits[0] = countCompBits(rmask);
        if (bits[0] < 0) {
            throw new IllegalArgumentException(Messages.getString("awt.23D")); 
        }
        bits[1] = countCompBits(gmask);
        if (bits[1] < 0) {
            throw new IllegalArgumentException(Messages.getString("awt.23E")); 
        }
        bits[2] = countCompBits(bmask);
        if (bits[2] < 0) {
            throw new IllegalArgumentException(Messages.getString("awt.23F")); 
        }
        if (amask != 0) {
            bits[3] = countCompBits(amask);
            if (bits[3] < 0) {
                throw new IllegalArgumentException(Messages.getString("awt.23C")); 
            }
        }
        return bits;
    }
    private static int countCompBits(int compMask) {
        int bits = 0;
        if (compMask != 0) {
            while ((compMask & 1) == 0) {
                compMask >>>= 1;
            }
            while ((compMask & 1) == 1) {
                compMask >>>= 1;
                bits++;
            }
        }
        if (compMask != 0) {
            return -1;
        }
        return bits;
    }
    private static int validateTransferType(int transferType) {
        if (transferType != DataBuffer.TYPE_BYTE && transferType != DataBuffer.TYPE_USHORT
                && transferType != DataBuffer.TYPE_INT) {
            throw new IllegalArgumentException(Messages.getString("awt.240")); 
        }
        return transferType;
    }
    private void parseComponents() {
        offsets = new int[numComponents];
        scales = new float[numComponents];
        for (int i = 0; i < numComponents; i++) {
            int off = 0;
            int mask = componentMasks[i];
            while ((mask & 1) == 0) {
                mask >>>= 1;
                off++;
            }
            offsets[i] = off;
            if (bits[i] == 0) {
                scales[i] = 256.0f; 
            } else {
                scales[i] = 255.0f / maxValues[i];
            }
        }
    }
}
