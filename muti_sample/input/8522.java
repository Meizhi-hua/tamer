public class GIFImageReader extends ImageReader {
    ImageInputStream stream = null;
    boolean gotHeader = false;
    GIFStreamMetadata streamMetadata = null;
    int currIndex = -1;
    GIFImageMetadata imageMetadata = null;
    List imageStartPosition = new ArrayList();
    int imageMetadataLength;
    int numImages = -1;
    byte[] block = new byte[255];
    int blockLength = 0;
    int bitPos = 0;
    int nextByte = 0;
    int initCodeSize;
    int clearCode;
    int eofCode;
    int next32Bits = 0;
    boolean lastBlockFound = false;
    BufferedImage theImage = null;
    WritableRaster theTile = null;
    int width = -1, height = -1;
    int streamX = -1, streamY = -1;
    int rowsDone = 0;
    int interlacePass = 0;
    static final int[] interlaceIncrement = { 8, 8, 4, 2, -1 };
    static final int[] interlaceOffset = { 0, 4, 2, 1, -1 };
    public GIFImageReader(ImageReaderSpi originatingProvider) {
        super(originatingProvider);
    }
    public void setInput(Object input,
                         boolean seekForwardOnly,
                         boolean ignoreMetadata) {
        super.setInput(input, seekForwardOnly, ignoreMetadata);
        if (input != null) {
            if (!(input instanceof ImageInputStream)) {
                throw new IllegalArgumentException
                    ("input not an ImageInputStream!");
            }
            this.stream = (ImageInputStream)input;
        } else {
            this.stream = null;
        }
        resetStreamSettings();
    }
    public int getNumImages(boolean allowSearch) throws IIOException {
        if (stream == null) {
            throw new IllegalStateException("Input not set!");
        }
        if (seekForwardOnly && allowSearch) {
            throw new IllegalStateException
                ("seekForwardOnly and allowSearch can't both be true!");
        }
        if (numImages > 0) {
            return numImages;
        }
        if (allowSearch) {
            this.numImages = locateImage(Integer.MAX_VALUE) + 1;
        }
        return numImages;
    }
    private void checkIndex(int imageIndex) {
        if (imageIndex < minIndex) {
            throw new IndexOutOfBoundsException("imageIndex < minIndex!");
        }
        if (seekForwardOnly) {
            minIndex = imageIndex;
        }
    }
    public int getWidth(int imageIndex) throws IIOException {
        checkIndex(imageIndex);
        int index = locateImage(imageIndex);
        if (index != imageIndex) {
            throw new IndexOutOfBoundsException();
        }
        readMetadata();
        return imageMetadata.imageWidth;
    }
    public int getHeight(int imageIndex) throws IIOException {
        checkIndex(imageIndex);
        int index = locateImage(imageIndex);
        if (index != imageIndex) {
            throw new IndexOutOfBoundsException();
        }
        readMetadata();
        return imageMetadata.imageHeight;
    }
    public Iterator getImageTypes(int imageIndex) throws IIOException {
        checkIndex(imageIndex);
        int index = locateImage(imageIndex);
        if (index != imageIndex) {
            throw new IndexOutOfBoundsException();
        }
        readMetadata();
        List l = new ArrayList(1);
        byte[] colorTable;
        if (imageMetadata.localColorTable != null) {
            colorTable = imageMetadata.localColorTable;
        } else {
            colorTable = streamMetadata.globalColorTable;
        }
        int length = colorTable.length/3;
        int bits;
        if (length == 2) {
            bits = 1;
        } else if (length == 4) {
            bits = 2;
        } else if (length == 8 || length == 16) {
            bits = 4;
        } else {
            bits = 8;
        }
        int lutLength = 1 << bits;
        byte[] r = new byte[lutLength];
        byte[] g = new byte[lutLength];
        byte[] b = new byte[lutLength];
        int rgbIndex = 0;
        for (int i = 0; i < length; i++) {
            r[i] = colorTable[rgbIndex++];
            g[i] = colorTable[rgbIndex++];
            b[i] = colorTable[rgbIndex++];
        }
        byte[] a = null;
        if (imageMetadata.transparentColorFlag) {
            a = new byte[lutLength];
            Arrays.fill(a, (byte)255);
            int idx = Math.min(imageMetadata.transparentColorIndex,
                               lutLength - 1);
            a[idx] = (byte)0;
        }
        int[] bitsPerSample = new int[1];
        bitsPerSample[0] = bits;
        l.add(ImageTypeSpecifier.createIndexed(r, g, b, a, bits,
                                               DataBuffer.TYPE_BYTE));
        return l.iterator();
    }
    public ImageReadParam getDefaultReadParam() {
        return new ImageReadParam();
    }
    public IIOMetadata getStreamMetadata() throws IIOException {
        readHeader();
        return streamMetadata;
    }
    public IIOMetadata getImageMetadata(int imageIndex) throws IIOException {
        checkIndex(imageIndex);
        int index = locateImage(imageIndex);
        if (index != imageIndex) {
            throw new IndexOutOfBoundsException("Bad image index!");
        }
        readMetadata();
        return imageMetadata;
    }
    private void initNext32Bits() {
        next32Bits = block[0] & 0xff;
        next32Bits |= (block[1] & 0xff) << 8;
        next32Bits |= (block[2] & 0xff) << 16;
        next32Bits |= block[3] << 24;
        nextByte = 4;
    }
    private int getCode(int codeSize, int codeMask) throws IOException {
        if (bitPos + codeSize > 32) {
            return eofCode; 
        }
        int code = (next32Bits >> bitPos) & codeMask;
        bitPos += codeSize;
        while (bitPos >= 8 && !lastBlockFound) {
            next32Bits >>>= 8;
            bitPos -= 8;
            if (nextByte >= blockLength) {
                blockLength = stream.readUnsignedByte();
                if (blockLength == 0) {
                    lastBlockFound = true;
                    return code;
                } else {
                    int left = blockLength;
                    int off = 0;
                    while (left > 0) {
                        int nbytes = stream.read(block, off, left);
                        off += nbytes;
                        left -= nbytes;
                    }
                    nextByte = 0;
                }
            }
            next32Bits |= block[nextByte++] << 24;
        }
        return code;
    }
    public void initializeStringTable(int[] prefix,
                                      byte[] suffix,
                                      byte[] initial,
                                      int[] length) {
        int numEntries = 1 << initCodeSize;
        for (int i = 0; i < numEntries; i++) {
            prefix[i] = -1;
            suffix[i] = (byte)i;
            initial[i] = (byte)i;
            length[i] = 1;
        }
        for (int i = numEntries; i < 4096; i++) {
            prefix[i] = -1;
            length[i] = 1;
        }
    }
    Rectangle sourceRegion;
    int sourceXSubsampling;
    int sourceYSubsampling;
    int sourceMinProgressivePass;
    int sourceMaxProgressivePass;
    Point destinationOffset;
    Rectangle destinationRegion;
    int updateMinY;
    int updateYStep;
    boolean decodeThisRow = true;
    int destY = 0;
    byte[] rowBuf;
    private void outputRow() {
        int width = Math.min(sourceRegion.width,
                             destinationRegion.width*sourceXSubsampling);
        int destX = destinationRegion.x;
        if (sourceXSubsampling == 1) {
            theTile.setDataElements(destX, destY, width, 1, rowBuf);
        } else {
            for (int x = 0; x < width; x += sourceXSubsampling, destX++) {
                theTile.setSample(destX, destY, 0, rowBuf[x] & 0xff);
            }
        }
        if (updateListeners != null) {
            int[] bands = { 0 };
            processImageUpdate(theImage,
                               destX, destY,
                               width, 1, 1, updateYStep,
                               bands);
        }
    }
    private void computeDecodeThisRow() {
        this.decodeThisRow =
            (destY < destinationRegion.y + destinationRegion.height) &&
            (streamY >= sourceRegion.y) &&
            (streamY < sourceRegion.y + sourceRegion.height) &&
            (((streamY - sourceRegion.y) % sourceYSubsampling) == 0);
    }
    private void outputPixels(byte[] string, int len) {
        if (interlacePass < sourceMinProgressivePass ||
            interlacePass > sourceMaxProgressivePass) {
            return;
        }
        for (int i = 0; i < len; i++) {
            if (streamX >= sourceRegion.x) {
                rowBuf[streamX - sourceRegion.x] = string[i];
            }
            ++streamX;
            if (streamX == width) {
                ++rowsDone;
                processImageProgress(100.0F*rowsDone/height);
                if (decodeThisRow) {
                    outputRow();
                }
                streamX = 0;
                if (imageMetadata.interlaceFlag) {
                    streamY += interlaceIncrement[interlacePass];
                    if (streamY >= height) {
                        if (updateListeners != null) {
                            processPassComplete(theImage);
                        }
                        ++interlacePass;
                        if (interlacePass > sourceMaxProgressivePass) {
                            return;
                        }
                        streamY = interlaceOffset[interlacePass];
                        startPass(interlacePass);
                    }
                } else {
                    ++streamY;
                }
                this.destY = destinationRegion.y +
                    (streamY - sourceRegion.y)/sourceYSubsampling;
                computeDecodeThisRow();
            }
        }
    }
    private void readHeader() throws IIOException {
        if (gotHeader) {
            return;
        }
        if (stream == null) {
            throw new IllegalStateException("Input not set!");
        }
        this.streamMetadata = new GIFStreamMetadata();
        try {
            stream.setByteOrder(ByteOrder.LITTLE_ENDIAN);
            byte[] signature = new byte[6];
            stream.readFully(signature);
            StringBuffer version = new StringBuffer(3);
            version.append((char)signature[3]);
            version.append((char)signature[4]);
            version.append((char)signature[5]);
            streamMetadata.version = version.toString();
            streamMetadata.logicalScreenWidth = stream.readUnsignedShort();
            streamMetadata.logicalScreenHeight = stream.readUnsignedShort();
            int packedFields = stream.readUnsignedByte();
            boolean globalColorTableFlag = (packedFields & 0x80) != 0;
            streamMetadata.colorResolution = ((packedFields >> 4) & 0x7) + 1;
            streamMetadata.sortFlag = (packedFields & 0x8) != 0;
            int numGCTEntries = 1 << ((packedFields & 0x7) + 1);
            streamMetadata.backgroundColorIndex = stream.readUnsignedByte();
            streamMetadata.pixelAspectRatio = stream.readUnsignedByte();
            if (globalColorTableFlag) {
                streamMetadata.globalColorTable = new byte[3*numGCTEntries];
                stream.readFully(streamMetadata.globalColorTable);
            } else {
                streamMetadata.globalColorTable = null;
            }
            imageStartPosition.add(Long.valueOf(stream.getStreamPosition()));
        } catch (IOException e) {
            throw new IIOException("I/O error reading header!", e);
        }
        gotHeader = true;
    }
    private boolean skipImage() throws IIOException {
        try {
            while (true) {
                int blockType = stream.readUnsignedByte();
                if (blockType == 0x2c) {
                    stream.skipBytes(8);
                    int packedFields = stream.readUnsignedByte();
                    if ((packedFields & 0x80) != 0) {
                        int bits = (packedFields & 0x7) + 1;
                        stream.skipBytes(3*(1 << bits));
                    }
                    stream.skipBytes(1);
                    int length = 0;
                    do {
                        length = stream.readUnsignedByte();
                        stream.skipBytes(length);
                    } while (length > 0);
                    return true;
                } else if (blockType == 0x3b) {
                    return false;
                } else if (blockType == 0x21) {
                    int label = stream.readUnsignedByte();
                    int length = 0;
                    do {
                        length = stream.readUnsignedByte();
                        stream.skipBytes(length);
                    } while (length > 0);
                } else if (blockType == 0x0) {
                    return false;
                } else {
                    int length = 0;
                    do {
                        length = stream.readUnsignedByte();
                        stream.skipBytes(length);
                    } while (length > 0);
                }
            }
        } catch (EOFException e) {
            return false;
        } catch (IOException e) {
            throw new IIOException("I/O error locating image!", e);
        }
    }
    private int locateImage(int imageIndex) throws IIOException {
        readHeader();
        try {
            int index = Math.min(imageIndex, imageStartPosition.size() - 1);
            Long l = (Long)imageStartPosition.get(index);
            stream.seek(l.longValue());
            while (index < imageIndex) {
                if (!skipImage()) {
                    --index;
                    return index;
                }
                Long l1 = new Long(stream.getStreamPosition());
                imageStartPosition.add(l1);
                ++index;
            }
        } catch (IOException e) {
            throw new IIOException("Couldn't seek!", e);
        }
        if (currIndex != imageIndex) {
            imageMetadata = null;
        }
        currIndex = imageIndex;
        return imageIndex;
    }
    private byte[] concatenateBlocks() throws IOException {
        byte[] data = new byte[0];
        while (true) {
            int length = stream.readUnsignedByte();
            if (length == 0) {
                break;
            }
            byte[] newData = new byte[data.length + length];
            System.arraycopy(data, 0, newData, 0, data.length);
            stream.readFully(newData, data.length, length);
            data = newData;
        }
        return data;
    }
    private void readMetadata() throws IIOException {
        if (stream == null) {
            throw new IllegalStateException("Input not set!");
        }
        try {
            this.imageMetadata = new GIFImageMetadata();
            long startPosition = stream.getStreamPosition();
            while (true) {
                int blockType = stream.readUnsignedByte();
                if (blockType == 0x2c) { 
                    imageMetadata.imageLeftPosition =
                        stream.readUnsignedShort();
                    imageMetadata.imageTopPosition =
                        stream.readUnsignedShort();
                    imageMetadata.imageWidth = stream.readUnsignedShort();
                    imageMetadata.imageHeight = stream.readUnsignedShort();
                    int idPackedFields = stream.readUnsignedByte();
                    boolean localColorTableFlag =
                        (idPackedFields & 0x80) != 0;
                    imageMetadata.interlaceFlag = (idPackedFields & 0x40) != 0;
                    imageMetadata.sortFlag = (idPackedFields & 0x20) != 0;
                    int numLCTEntries = 1 << ((idPackedFields & 0x7) + 1);
                    if (localColorTableFlag) {
                        imageMetadata.localColorTable =
                            new byte[3*numLCTEntries];
                        stream.readFully(imageMetadata.localColorTable);
                    } else {
                        imageMetadata.localColorTable = null;
                    }
                    this.imageMetadataLength =
                        (int)(stream.getStreamPosition() - startPosition);
                    return;
                } else if (blockType == 0x21) { 
                    int label = stream.readUnsignedByte();
                    if (label == 0xf9) { 
                        int gceLength = stream.readUnsignedByte(); 
                        int gcePackedFields = stream.readUnsignedByte();
                        imageMetadata.disposalMethod =
                            (gcePackedFields >> 2) & 0x3;
                        imageMetadata.userInputFlag =
                            (gcePackedFields & 0x2) != 0;
                        imageMetadata.transparentColorFlag =
                            (gcePackedFields & 0x1) != 0;
                        imageMetadata.delayTime = stream.readUnsignedShort();
                        imageMetadata.transparentColorIndex
                            = stream.readUnsignedByte();
                        int terminator = stream.readUnsignedByte();
                    } else if (label == 0x1) { 
                        int length = stream.readUnsignedByte();
                        imageMetadata.hasPlainTextExtension = true;
                        imageMetadata.textGridLeft =
                            stream.readUnsignedShort();
                        imageMetadata.textGridTop =
                            stream.readUnsignedShort();
                        imageMetadata.textGridWidth =
                            stream.readUnsignedShort();
                        imageMetadata.textGridHeight =
                            stream.readUnsignedShort();
                        imageMetadata.characterCellWidth =
                            stream.readUnsignedByte();
                        imageMetadata.characterCellHeight =
                            stream.readUnsignedByte();
                        imageMetadata.textForegroundColor =
                            stream.readUnsignedByte();
                        imageMetadata.textBackgroundColor =
                            stream.readUnsignedByte();
                        imageMetadata.text = concatenateBlocks();
                    } else if (label == 0xfe) { 
                        byte[] comment = concatenateBlocks();
                        if (imageMetadata.comments == null) {
                            imageMetadata.comments = new ArrayList();
                        }
                        imageMetadata.comments.add(comment);
                    } else if (label == 0xff) { 
                        int blockSize = stream.readUnsignedByte();
                        byte[] applicationID = new byte[8];
                        byte[] authCode = new byte[3];
                        byte[] blockData = new byte[blockSize];
                        stream.readFully(blockData);
                        int offset = copyData(blockData, 0, applicationID);
                        offset = copyData(blockData, offset, authCode);
                        byte[] applicationData = concatenateBlocks();
                        if (offset < blockSize) {
                            int len = blockSize - offset;
                            byte[] data =
                                new byte[len + applicationData.length];
                            System.arraycopy(blockData, offset, data, 0, len);
                            System.arraycopy(applicationData, 0, data, len,
                                             applicationData.length);
                            applicationData = data;
                        }
                        if (imageMetadata.applicationIDs == null) {
                            imageMetadata.applicationIDs = new ArrayList();
                            imageMetadata.authenticationCodes =
                                new ArrayList();
                            imageMetadata.applicationData = new ArrayList();
                        }
                        imageMetadata.applicationIDs.add(applicationID);
                        imageMetadata.authenticationCodes.add(authCode);
                        imageMetadata.applicationData.add(applicationData);
                    } else {
                        int length = 0;
                        do {
                            length = stream.readUnsignedByte();
                            stream.skipBytes(length);
                        } while (length > 0);
                    }
                } else if (blockType == 0x3b) { 
                    throw new IndexOutOfBoundsException
                        ("Attempt to read past end of image sequence!");
                } else {
                    throw new IIOException("Unexpected block type " +
                                           blockType + "!");
                }
            }
        } catch (IIOException iioe) {
            throw iioe;
        } catch (IOException ioe) {
            throw new IIOException("I/O error reading image metadata!", ioe);
        }
    }
    private int copyData(byte[] src, int offset, byte[] dst) {
        int len = dst.length;
        int rest = src.length - offset;
        if (len > rest) {
            len = rest;
        }
        System.arraycopy(src, offset, dst, 0, len);
        return offset + len;
    }
    private void startPass(int pass) {
        if (updateListeners == null) {
            return;
        }
        int y = 0;
        int yStep = 1;
        if (imageMetadata.interlaceFlag) {
            y = interlaceOffset[interlacePass];
            yStep = interlaceIncrement[interlacePass];
        }
        int[] vals = ReaderUtil.
            computeUpdatedPixels(sourceRegion,
                                 destinationOffset,
                                 destinationRegion.x,
                                 destinationRegion.y,
                                 destinationRegion.x +
                                 destinationRegion.width - 1,
                                 destinationRegion.y +
                                 destinationRegion.height - 1,
                                 sourceXSubsampling,
                                 sourceYSubsampling,
                                 0,
                                 y,
                                 destinationRegion.width,
                                 (destinationRegion.height + yStep - 1)/yStep,
                                 1,
                                 yStep);
        this.updateMinY = vals[1];
        this.updateYStep = vals[5];
        int[] bands = { 0 };
        processPassStarted(theImage,
                           interlacePass,
                           sourceMinProgressivePass,
                           sourceMaxProgressivePass,
                           0,
                           updateMinY,
                           1,
                           updateYStep,
                           bands);
    }
    public BufferedImage read(int imageIndex, ImageReadParam param)
        throws IIOException {
        if (stream == null) {
            throw new IllegalStateException("Input not set!");
        }
        checkIndex(imageIndex);
        int index = locateImage(imageIndex);
        if (index != imageIndex) {
            throw new IndexOutOfBoundsException("imageIndex out of bounds!");
        }
        clearAbortRequest();
        readMetadata();
        if (param == null) {
            param = getDefaultReadParam();
        }
        Iterator imageTypes = getImageTypes(imageIndex);
        this.theImage = getDestination(param,
                                       imageTypes,
                                       imageMetadata.imageWidth,
                                       imageMetadata.imageHeight);
        this.theTile = theImage.getWritableTile(0, 0);
        this.width = imageMetadata.imageWidth;
        this.height = imageMetadata.imageHeight;
        this.streamX = 0;
        this.streamY = 0;
        this.rowsDone = 0;
        this.interlacePass = 0;
        this.sourceRegion = new Rectangle(0, 0, 0, 0);
        this.destinationRegion = new Rectangle(0, 0, 0, 0);
        computeRegions(param, width, height, theImage,
                       sourceRegion, destinationRegion);
        this.destinationOffset = new Point(destinationRegion.x,
                                           destinationRegion.y);
        this.sourceXSubsampling = param.getSourceXSubsampling();
        this.sourceYSubsampling = param.getSourceYSubsampling();
        this.sourceMinProgressivePass =
            Math.max(param.getSourceMinProgressivePass(), 0);
        this.sourceMaxProgressivePass =
            Math.min(param.getSourceMaxProgressivePass(), 3);
        this.destY = destinationRegion.y +
            (streamY - sourceRegion.y)/sourceYSubsampling;
        computeDecodeThisRow();
        processImageStarted(imageIndex);
        startPass(0);
        this.rowBuf = new byte[width];
        try {
            this.initCodeSize = stream.readUnsignedByte();
            this.blockLength = stream.readUnsignedByte();
            int left = blockLength;
            int off = 0;
            while (left > 0) {
                int nbytes = stream.read(block, off, left);
                left -= nbytes;
                off += nbytes;
            }
            this.bitPos = 0;
            this.nextByte = 0;
            this.lastBlockFound = false;
            this.interlacePass = 0;
            initNext32Bits();
            this.clearCode = 1 << initCodeSize;
            this.eofCode = clearCode + 1;
            int code, oldCode = 0;
            int[] prefix = new int[4096];
            byte[] suffix = new byte[4096];
            byte[] initial = new byte[4096];
            int[] length = new int[4096];
            byte[] string = new byte[4096];
            initializeStringTable(prefix, suffix, initial, length);
            int tableIndex = (1 << initCodeSize) + 2;
            int codeSize = initCodeSize + 1;
            int codeMask = (1 << codeSize) - 1;
            while (!abortRequested()) {
                code = getCode(codeSize, codeMask);
                if (code == clearCode) {
                    initializeStringTable(prefix, suffix, initial, length);
                    tableIndex = (1 << initCodeSize) + 2;
                    codeSize = initCodeSize + 1;
                    codeMask = (1 << codeSize) - 1;
                    code = getCode(codeSize, codeMask);
                    if (code == eofCode) {
                        processImageComplete();
                        return theImage;
                    }
                } else if (code == eofCode) {
                    processImageComplete();
                    return theImage;
                } else {
                    int newSuffixIndex;
                    if (code < tableIndex) {
                        newSuffixIndex = code;
                    } else { 
                        newSuffixIndex = oldCode;
                        if (code != tableIndex) {
                            processWarningOccurred("Out-of-sequence code!");
                        }
                    }
                    int ti = tableIndex;
                    int oc = oldCode;
                    prefix[ti] = oc;
                    suffix[ti] = initial[newSuffixIndex];
                    initial[ti] = initial[oc];
                    length[ti] = length[oc] + 1;
                    ++tableIndex;
                    if ((tableIndex == (1 << codeSize)) &&
                        (tableIndex < 4096)) {
                        ++codeSize;
                        codeMask = (1 << codeSize) - 1;
                    }
                }
                int c = code;
                int len = length[c];
                for (int i = len - 1; i >= 0; i--) {
                    string[i] = suffix[c];
                    c = prefix[c];
                }
                outputPixels(string, len);
                oldCode = code;
            }
            processReadAborted();
            return theImage;
        } catch (IOException e) {
            e.printStackTrace();
            throw new IIOException("I/O error reading image!", e);
        }
    }
    public void reset() {
        super.reset();
        resetStreamSettings();
    }
    private void resetStreamSettings() {
        gotHeader = false;
        streamMetadata = null;
        currIndex = -1;
        imageMetadata = null;
        imageStartPosition = new ArrayList();
        numImages = -1;
        blockLength = 0;
        bitPos = 0;
        nextByte = 0;
        next32Bits = 0;
        lastBlockFound = false;
        theImage = null;
        theTile = null;
        width = -1;
        height = -1;
        streamX = -1;
        streamY = -1;
        rowsDone = 0;
        interlacePass = 0;
    }
}
