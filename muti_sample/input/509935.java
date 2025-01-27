public class OffscreenImage extends Image implements ImageConsumer {
    static final ColorModel rgbCM = ColorModel.getRGBdefault();
    ImageProducer src;
    BufferedImage image;
    ColorModel cm;
    WritableRaster raster;
    boolean isIntRGB;
    Hashtable<?, ?> properties;
    Vector<ImageObserver> observers;
    int width;
    int height;
    int imageState;
    int hints;
    private boolean producing;
    private ImageSurface imageSurf;
    public OffscreenImage(ImageProducer ip){
        imageState = 0;
        src = ip;
        width = -1;
        height = -1;
        observers = new Vector<ImageObserver>();
        producing = false;
    }
    @Override
    public Object getProperty(String name, ImageObserver observer) {
        if(name == null) {
            throw new NullPointerException(Messages.getString("awt.38")); 
        }
        if(properties == null){
            addObserver(observer);
            startProduction();
            if(properties == null) {
                return null;
            }
        }
        Object prop = properties.get(name);
        if(prop == null) {
            prop = UndefinedProperty;
        }
        return prop;
    }
    @Override
    public ImageProducer getSource() {
        return src;
    }
    @Override
    public int getWidth(ImageObserver observer) {
        if((imageState & ImageObserver.WIDTH) == 0){
            addObserver(observer);
            startProduction();
            if((imageState & ImageObserver.WIDTH) == 0) {
                return -1;
            }
        }
        return width;
    }
    @Override
    public int getHeight(ImageObserver observer) {
        if((imageState & ImageObserver.HEIGHT) == 0){
            addObserver(observer);
            startProduction();
            if((imageState & ImageObserver.HEIGHT) == 0) {
                return -1;
            }
        }
        return height;
    }
    @Override
    public Graphics getGraphics() {
        throw new UnsupportedOperationException(Messages.getString("awt.39")); 
    }
    @Override
    public void flush() {
        stopProduction();
        imageUpdate(this, ImageObserver.ABORT, -1, -1, -1, -1);
        imageState &= ~ImageObserver.ERROR;
        imageState = 0;
        image = null;
        cm = null;
        raster = null;
        hints = 0;
        width = -1;
        height = -1;
    }
    public void setProperties(Hashtable<?, ?> properties) {
        this.properties = properties;
        imageUpdate(this, ImageObserver.PROPERTIES, 0, 0, width, height);
    }
    public void setColorModel(ColorModel cm) {
        this.cm = cm;
    }
    public void setPixels(int x, int y, int w, int h, ColorModel model,
            int[] pixels, int off, int scansize) {
        if(raster == null){
            if(cm == null){
                if(model == null) {
                    throw new NullPointerException(Messages.getString("awt.3A")); 
                }
                cm = model;
            }
            createRaster();
        }
        if(model == null) {
            model = cm;
        }
        if(cm != model){
            forceToIntARGB();
        }
        if(cm == model && model.getTransferType() == DataBuffer.TYPE_INT &&
                raster.getNumDataElements() == 1){
            DataBufferInt dbi = (DataBufferInt) raster.getDataBuffer();
            int data[] = dbi.getData();
            int scanline = raster.getWidth();
            int rof = dbi.getOffset() + y * scanline + x;
            for(int lineOff = off, line = y; line < y + h;
                line++, lineOff += scansize, rof += scanline){
                System.arraycopy(pixels, lineOff, data, rof, w);
            }
        }else if(isIntRGB){
            int buff[] = new int[w];
            DataBufferInt dbi = (DataBufferInt) raster.getDataBuffer();
            int data[] = dbi.getData();
            int scanline = raster.getWidth();
            int rof = dbi.getOffset() + y * scanline + x;
            for (int sy = y, sOff = off; sy < y + h; sy++, sOff += scansize,
                rof += scanline) {
                for (int sx = x, idx = 0; sx < x + w; sx++, idx++) {
                    buff[idx] = model.getRGB(pixels[sOff + idx]);
                }
                System.arraycopy(buff, 0, data, rof, w);
            }
        }else{
            Object buf = null;
            for (int sy = y, sOff = off; sy < y + h; sy++, sOff += scansize) {
                for (int sx = x, idx = 0; sx < x + w; sx++, idx++) {
                    int rgb = model.getRGB(pixels[sOff + idx]);
                    buf = cm.getDataElements(rgb, buf);
                    raster.setDataElements(sx, sy, buf);
                }
            }
        }
        if (imageSurf != null) {
            imageSurf.invalidate();
        }
        imageUpdate(this, ImageObserver.SOMEBITS, 0, 0, width, height);
    }
    public void setPixels(int x, int y, int w, int h, ColorModel model,
            byte[] pixels, int off, int scansize) {
        if(raster == null){
            if(cm == null){
                if(model == null) {
                    throw new NullPointerException(Messages.getString("awt.3A")); 
                }
                cm = model;
            }
            createRaster();
        }
        if(model == null) {
            model = cm;
        }
        if(model != cm){
            forceToIntARGB();
        }
        if(isIntRGB){
            int buff[] = new int[w];
            IndexColorModel icm = (IndexColorModel) model;
            int colorMap[] = new int[icm.getMapSize()];
            icm.getRGBs(colorMap);
            DataBufferInt dbi = (DataBufferInt) raster.getDataBuffer();
            int data[] = dbi.getData();
            int scanline = raster.getWidth();
            int rof = dbi.getOffset() + y * scanline + x;
            if(model instanceof IndexColorModel){
                for (int sy = y, sOff = off; sy < y + h; sy++, sOff += scansize,
                    rof += scanline) {
                    for (int sx = x, idx = 0; sx < x + w; sx++, idx++) {
                        buff[idx] = colorMap[pixels[sOff + idx] & 0xff];
                    }
                    System.arraycopy(buff, 0, data, rof, w);
                }
            }else{
                for (int sy = y, sOff = off; sy < y + h; sy++, sOff += scansize,
                    rof += scanline) {
                    for (int sx = x, idx = 0; sx < x + w; sx++, idx++) {
                        buff[idx] = model.getRGB(pixels[sOff + idx] & 0xff);
                    }
                    System.arraycopy(buff, 0, data, rof, w);
                }
            }
        }else if(model == cm && model.getTransferType() == DataBuffer.TYPE_BYTE &&
                raster.getNumDataElements() == 1){
            DataBufferByte dbb = (DataBufferByte)raster.getDataBuffer();
            byte data[] = dbb.getData();
            int scanline = raster.getWidth();
            int rof = dbb.getOffset() + y * scanline + x;
            for(int lineOff = off, line = y; line < y + h;
                line++, lineOff += scansize, rof += scanline){
                System.arraycopy(pixels, lineOff, data, rof, w);
            }
        }else if(model == cm && model.getTransferType() == DataBuffer.TYPE_BYTE &&
                cm instanceof ComponentColorModel){
            int nc = cm.getNumComponents();
            byte stride[] = new byte[scansize];
            for (int sy = y, sOff = off; sy < y + h; sy++, sOff += scansize) {
                System.arraycopy(pixels, sOff, stride, 0, scansize);
                raster.setDataElements(x, sy, w, 1, stride);
            }
        }else {
            for (int sy = y, sOff = off; sy < y + h; sy++, sOff += scansize) {
                for (int sx = x, idx = 0; sx < x + w; sx++, idx++) {
                    int rgb = model.getRGB(pixels[sOff + idx] & 0xff);
                    raster.setDataElements(sx, sy, cm.getDataElements(rgb, null));
                }
            }
        }
        if (imageSurf != null) {
            imageSurf.invalidate();
        }
        imageUpdate(this, ImageObserver.SOMEBITS, 0, 0, width, height);
    }
    public void setDimensions(int width, int height) {
        if(width <= 0 || height <= 0){
            imageComplete(ImageObserver.ERROR);
            return;
        }
        this.width = width;
        this.height = height;
        imageUpdate(this, (ImageObserver.HEIGHT | ImageObserver.WIDTH),
                0, 0, width, height);
    }
    public void setHints(int hints) {
        this.hints = hints;
    }
    public void imageComplete(int state) {
        int flag;
        switch(state){
        case IMAGEABORTED:
            flag = ImageObserver.ABORT;
            break;
        case IMAGEERROR:
            flag = ImageObserver.ERROR | ImageObserver.ABORT;
            break;
        case SINGLEFRAMEDONE:
            flag = ImageObserver.FRAMEBITS;
            break;
        case STATICIMAGEDONE:
            flag = ImageObserver.ALLBITS;
            break;
        default:
            throw new IllegalArgumentException(Messages.getString("awt.3B")); 
        }
        imageUpdate(this, flag, 0, 0, width, height);
        if((flag & (ImageObserver.ERROR | ImageObserver.ABORT |
                ImageObserver.ALLBITS)) != 0 ) {
            stopProduction();
            observers.removeAllElements();
        }
    }
    public  BufferedImage getBufferedImage(){
        if(image == null){
            ColorModel model = getColorModel();
            WritableRaster wr = getRaster();
            if(model != null && wr != null) {
                image = new BufferedImage(model, wr, model.isAlphaPremultiplied(), null);
            }
        }
        return image;
    }
    public  int checkImage(ImageObserver observer){
        addObserver(observer);
        return imageState;
    }
    public  boolean prepareImage(ImageObserver observer){
        if((imageState & ImageObserver.ERROR) != 0){
            if(observer != null){
                observer.imageUpdate(this, ImageObserver.ERROR |
                        ImageObserver.ABORT, -1, -1, -1, -1);
            }
            return false;
        }
        if((imageState & ImageObserver.ALLBITS) != 0) {
            return true;
        }
        addObserver(observer);
        startProduction();
        return ((imageState & ImageObserver.ALLBITS) != 0);
    }
    public  ColorModel getColorModel(){
        if(cm == null) {
            startProduction();
        }
        return cm;
    }
    public  WritableRaster getRaster(){
        if(raster == null) {
            startProduction();
        }
        return raster;
    }
    public int getState(){
        return imageState;
    }
    private  void addObserver(ImageObserver observer){
        if(observer != null){
          if(observers.contains(observer)) {
            return;
        }
          if((imageState & ImageObserver.ERROR) != 0){
              observer.imageUpdate(this, ImageObserver.ERROR |
                      ImageObserver.ABORT, -1, -1, -1, -1);
              return;
          }
          if((imageState & ImageObserver.ALLBITS) != 0){
              observer.imageUpdate(this, imageState, 0, 0, width, height);
              return;
          }
          observers.addElement(observer);
        }
    }
    private synchronized void startProduction(){
        if(!producing){
            imageState &= ~ImageObserver.ABORT;
            producing = true;
            src.startProduction(this);
        }
    }
    private synchronized void stopProduction(){
        producing = false;
        src.removeConsumer(this);
    }
    private void createRaster(){
        try{
            raster = cm.createCompatibleWritableRaster(width, height);
            isIntRGB = false;
            if(cm instanceof DirectColorModel){
                DirectColorModel dcm = (DirectColorModel) cm;
                if(dcm.getTransferType() == DataBuffer.TYPE_INT &&
                        dcm.getRedMask() == 0xff0000 &&
                        dcm.getGreenMask() == 0xff00 &&
                        dcm.getBlueMask() == 0xff){
                    isIntRGB = true;
                }
            }
        }catch(Exception e){
            cm = ColorModel.getRGBdefault();
            raster = cm.createCompatibleWritableRaster(width, height);
            isIntRGB = true;
        }
    }
    private  void imageUpdate(Image img, int infoflags, int x, int y,
            int width, int height){
        imageState |= infoflags;
        for (ImageObserver observer : observers) {
            observer.imageUpdate(this, infoflags, x, y, width, height);
        }
    }
    private void forceToIntARGB(){
        int w = raster.getWidth();
        int h = raster.getHeight();
        WritableRaster destRaster = rgbCM.createCompatibleWritableRaster(w, h);
        Object obj = null;
        int pixels[] = new int[w];
        if(cm instanceof IndexColorModel){
            IndexColorModel icm = (IndexColorModel) cm;
            int colorMap[] = new int[icm.getMapSize()];
            icm.getRGBs(colorMap);
            for (int y = 0; y < h; y++) {
                obj = raster.getDataElements(0, y, w, 1, obj);
                byte ba[] = (byte[]) obj;
                for (int x = 0; x < ba.length; x++) {
                    pixels[x] = colorMap[ba[x] & 0xff];
                }
                destRaster.setDataElements(0, y, w, 1, pixels);
            }
        }else{
            for(int y = 0; y < h; y++){
                for(int x = 0; x < w; x++){
                    obj = raster.getDataElements(x, y, obj);
                    pixels[x] = cm.getRGB(obj);
                }
                destRaster.setDataElements(0, y, w, 1, pixels);
            }
        }
        synchronized(this){
            if(imageSurf != null){
                imageSurf.dispose();
                imageSurf = null;
            }
            if(image != null){
                image.flush();
                image = null;
            }
            cm = rgbCM;
            raster = destRaster;
            isIntRGB = true;
        }
    }
    public ImageSurface getImageSurface() {
        if (imageSurf == null) {
            ColorModel model = getColorModel();
            WritableRaster wr = getRaster();
            if(model != null && wr != null) {
                imageSurf = new ImageSurface(model, wr);
            }
        }
        return imageSurf;
    }
}
