public class GifImageDecoder extends ImageDecoder {
    private static final boolean verbose = false;
    private static final int IMAGESEP           = 0x2c;
    private static final int EXBLOCK            = 0x21;
    private static final int EX_GRAPHICS_CONTROL= 0xf9;
    private static final int EX_COMMENT         = 0xfe;
    private static final int EX_APPLICATION     = 0xff;
    private static final int TERMINATOR         = 0x3b;
    private static final int TRANSPARENCYMASK   = 0x01;
    private static final int INTERLACEMASK      = 0x40;
    private static final int COLORMAPMASK       = 0x80;
    int num_global_colors;
    byte[] global_colormap;
    int trans_pixel = -1;
    IndexColorModel global_model;
    Hashtable props = new Hashtable();
    byte[] saved_image;
    IndexColorModel saved_model;
    int global_width;
    int global_height;
    int global_bgpixel;
    GifFrame curframe;
    public GifImageDecoder(InputStreamImageSource src, InputStream is) {
        super(src, is);
    }
    private static void error(String s1) throws ImageFormatException {
        throw new ImageFormatException(s1);
    }
    private int readBytes(byte buf[], int off, int len) {
        while (len > 0) {
            try {
                int n = input.read(buf, off, len);
                if (n < 0) {
                    break;
                }
                off += n;
                len -= n;
            } catch (IOException e) {
                break;
            }
        }
        return len;
    }
    private static final int ExtractByte(byte buf[], int off) {
        return (buf[off] & 0xFF);
    }
    private static final int ExtractWord(byte buf[], int off) {
        return (buf[off] & 0xFF) | ((buf[off + 1] & 0xFF) << 8);
    }
    public void produceImage() throws IOException, ImageFormatException {
        try {
            readHeader();
            int totalframes = 0;
            int frameno = 0;
            int nloops = -1;
            int disposal_method = 0;
            int delay = -1;
            boolean loopsRead = false;
            boolean isAnimation = false;
            while (!aborted) {
                int code;
                switch (code = input.read()) {
                  case EXBLOCK:
                    switch (code = input.read()) {
                      case EX_GRAPHICS_CONTROL: {
                        byte buf[] = new byte[6];
                        if (readBytes(buf, 0, 6) != 0) {
                            return;
                        }
                        if ((buf[0] != 4) || (buf[5] != 0)) {
                            return;
                        }
                        delay = ExtractWord(buf, 2) * 10;
                        if (delay > 0 && !isAnimation) {
                            isAnimation = true;
                            ImageFetcher.startingAnimation();
                        }
                        disposal_method = (buf[1] >> 2) & 7;
                        if ((buf[1] & TRANSPARENCYMASK) != 0) {
                            trans_pixel = ExtractByte(buf, 4);
                        } else {
                            trans_pixel = -1;
                        }
                        break;
                      }
                      case EX_COMMENT:
                      case EX_APPLICATION:
                      default:
                        boolean loop_tag = false;
                        String comment = "";
                        while (true) {
                            int n = input.read();
                            if (n <= 0) {
                                break;
                            }
                            byte buf[] = new byte[n];
                            if (readBytes(buf, 0, n) != 0) {
                                return;
                            }
                            if (code == EX_COMMENT) {
                                comment += new String(buf, 0);
                            } else if (code == EX_APPLICATION) {
                                if (loop_tag) {
                                    if (n == 3 && buf[0] == 1) {
                                        if (loopsRead) {
                                            ExtractWord(buf, 1);
                                        }
                                        else {
                                            nloops = ExtractWord(buf, 1);
                                            loopsRead = true;
                                        }
                                    } else {
                                        loop_tag = false;
                                    }
                                }
                                if ("NETSCAPE2.0".equals(new String(buf, 0))) {
                                    loop_tag = true;
                                }
                            }
                        }
                        if (code == EX_COMMENT) {
                            props.put("comment", comment);
                        }
                        if (loop_tag && !isAnimation) {
                            isAnimation = true;
                            ImageFetcher.startingAnimation();
                        }
                        break;
                      case -1:
                        return; 
                    }
                    break;
                  case IMAGESEP:
                    if (!isAnimation) {
                        input.mark(0); 
                    }
                    try {
                        if (!readImage(totalframes == 0,
                                       disposal_method,
                                       delay)) {
                            return;
                        }
                    } catch (Exception e) {
                        if (verbose) {
                            e.printStackTrace();
                        }
                        return;
                    }
                    frameno++;
                    totalframes++;
                    break;
                  default:
                  case -1:
                    if (verbose) {
                        if (code == -1) {
                            System.err.println("Premature EOF in GIF file," +
                                               " frame " + frameno);
                        } else {
                            System.err.println("corrupt GIF file (parse) ["
                                               + code + "].");
                        }
                    }
                    if (frameno == 0) {
                        return;
                    }
                  case TERMINATOR:
                    if (nloops == 0 || nloops-- >= 0) {
                        try {
                            if (curframe != null) {
                                curframe.dispose();
                                curframe = null;
                            }
                            input.reset();
                            saved_image = null;
                            saved_model = null;
                            frameno = 0;
                            break;
                        } catch (IOException e) {
                            return; 
                        }
                    }
                    if (verbose && frameno != 1) {
                        System.out.println("processing GIF terminator,"
                                           + " frames: " + frameno
                                           + " total: " + totalframes);
                    }
                    imageComplete(ImageConsumer.STATICIMAGEDONE, true);
                    return;
                }
            }
        } finally {
            close();
        }
    }
    private void readHeader() throws IOException, ImageFormatException {
        byte buf[] = new byte[13];
        if (readBytes(buf, 0, 13) != 0) {
            throw new IOException();
        }
        if ((buf[0] != 'G') || (buf[1] != 'I') || (buf[2] != 'F')) {
            error("not a GIF file.");
        }
        global_width = ExtractWord(buf, 6);
        global_height = ExtractWord(buf, 8);
        int ch = ExtractByte(buf, 10);
        if ((ch & COLORMAPMASK) == 0) {
            num_global_colors = 2;
            global_bgpixel = 0;
            global_colormap = new byte[2*3];
            global_colormap[0] = global_colormap[1] = global_colormap[2] = (byte)0;
            global_colormap[3] = global_colormap[4] = global_colormap[5] = (byte)255;
        }
        else {
            num_global_colors = 1 << ((ch & 0x7) + 1);
            global_bgpixel = ExtractByte(buf, 11);
            if (buf[12] != 0) {
                props.put("aspectratio", ""+((ExtractByte(buf, 12) + 15) / 64.0));
            }
            global_colormap = new byte[num_global_colors * 3];
            if (readBytes(global_colormap, 0, num_global_colors * 3) != 0) {
                throw new IOException();
            }
        }
        input.mark(Integer.MAX_VALUE); 
    }
    private static final int normalflags =
        ImageConsumer.TOPDOWNLEFTRIGHT | ImageConsumer.COMPLETESCANLINES |
        ImageConsumer.SINGLEPASS | ImageConsumer.SINGLEFRAME;
    private static final int interlaceflags =
        ImageConsumer.RANDOMPIXELORDER | ImageConsumer.COMPLETESCANLINES |
        ImageConsumer.SINGLEPASS | ImageConsumer.SINGLEFRAME;
    private short prefix[]  = new short[4096];
    private byte  suffix[]  = new byte[4096];
    private byte  outCode[] = new byte[4097];
    private static native void initIDs();
    static {
        NativeLibLoader.loadLibraries();
        initIDs();
    }
    private native boolean parseImage(int x, int y, int width, int height,
                                      boolean interlace, int initCodeSize,
                                      byte block[], byte rasline[],
                                      IndexColorModel model);
    private int sendPixels(int x, int y, int width, int height,
                           byte rasline[], ColorModel model) {
        int rasbeg, rasend, x2;
        if (y < 0) {
            height += y;
            y = 0;
        }
        if (y + height > global_height) {
            height = global_height - y;
        }
        if (height <= 0) {
            return 1;
        }
        if (x < 0) {
            rasbeg = -x;
            width += x;         
            x2 = 0;             
        } else {
            rasbeg = 0;
            x2 = x;             
        }
        if (x2 + width > global_width) {
            width = global_width - x2;
        }
        if (width <= 0) {
            return 1;
        }
        rasend = rasbeg + width;
        int off = y * global_width + x2;
        boolean save = (curframe.disposal_method == GifFrame.DISPOSAL_SAVE);
        if (trans_pixel >= 0 && !curframe.initialframe) {
            if (saved_image != null && model.equals(saved_model)) {
                for (int i = rasbeg; i < rasend; i++, off++) {
                    byte pixel = rasline[i];
                    if ((pixel & 0xff) == trans_pixel) {
                        rasline[i] = saved_image[off];
                    } else if (save) {
                        saved_image[off] = pixel;
                    }
                }
            } else {
                int runstart = -1;
                int count = 1;
                for (int i = rasbeg; i < rasend; i++, off++) {
                    byte pixel = rasline[i];
                    if ((pixel & 0xff) == trans_pixel) {
                        if (runstart >= 0) {
                            count = setPixels(x + runstart, y,
                                              i - runstart, 1,
                                              model, rasline,
                                              runstart, 0);
                            if (count == 0) {
                                break;
                            }
                        }
                        runstart = -1;
                    } else {
                        if (runstart < 0) {
                            runstart = i;
                        }
                        if (save) {
                            saved_image[off] = pixel;
                        }
                    }
                }
                if (runstart >= 0) {
                    count = setPixels(x + runstart, y,
                                      rasend - runstart, 1,
                                      model, rasline,
                                      runstart, 0);
                }
                return count;
            }
        } else if (save) {
            System.arraycopy(rasline, rasbeg, saved_image, off, width);
        }
        int count = setPixels(x2, y, width, height, model,
                              rasline, rasbeg, 0);
        return count;
    }
    private boolean readImage(boolean first, int disposal_method, int delay)
        throws IOException
    {
        if (curframe != null && !curframe.dispose()) {
            abort();
            return false;
        }
        long tm = 0;
        if (verbose) {
            tm = System.currentTimeMillis();
        }
        byte block[] = new byte[256 + 3];
        if (readBytes(block, 0, 10) != 0) {
            throw new IOException();
        }
        int x = ExtractWord(block, 0);
        int y = ExtractWord(block, 2);
        int width = ExtractWord(block, 4);
        int height = ExtractWord(block, 6);
        if (width == 0 && global_width != 0) {
            width = global_width - x;
        }
        if (height == 0 && global_height != 0) {
            height = global_height - y;
        }
        boolean interlace = (block[8] & INTERLACEMASK) != 0;
        IndexColorModel model = global_model;
        if ((block[8] & COLORMAPMASK) != 0) {
            int num_local_colors = 1 << ((block[8] & 0x7) + 1);
            byte[] local_colormap = new byte[num_local_colors * 3];
            local_colormap[0] = block[9];
            if (readBytes(local_colormap, 1, num_local_colors * 3 - 1) != 0) {
                throw new IOException();
            }
            if (readBytes(block, 9, 1) != 0) {
                throw new IOException();
            }
            if (trans_pixel >= num_local_colors) {
                num_local_colors = trans_pixel + 1;
                local_colormap = grow_colormap(local_colormap, num_local_colors);
            }
            model = new IndexColorModel(8, num_local_colors, local_colormap,
                                        0, false, trans_pixel);
        } else if (model == null
                   || trans_pixel != model.getTransparentPixel()) {
            if (trans_pixel >= num_global_colors) {
                num_global_colors = trans_pixel + 1;
                global_colormap = grow_colormap(global_colormap, num_global_colors);
            }
            model = new IndexColorModel(8, num_global_colors, global_colormap,
                                        0, false, trans_pixel);
            global_model = model;
        }
        if (first) {
            if (global_width == 0) global_width = width;
            if (global_height == 0) global_height = height;
            setDimensions(global_width, global_height);
            setProperties(props);
            setColorModel(model);
            headerComplete();
        }
        if (disposal_method == GifFrame.DISPOSAL_SAVE && saved_image == null) {
            saved_image = new byte[global_width * global_height];
            if ((height < global_height) && (model != null)) {
                byte tpix = (byte)model.getTransparentPixel();
                if (tpix >= 0) {
                    byte trans_rasline[] = new byte[global_width];
                    for (int i=0; i<global_width;i++) {
                        trans_rasline[i] = tpix;
                    }
                    setPixels(0, 0, global_width, y,
                              model, trans_rasline, 0, 0);
                    setPixels(0, y+height, global_width,
                              global_height-height-y, model, trans_rasline,
                              0, 0);
                }
            }
        }
        int hints = (interlace ? interlaceflags : normalflags);
        setHints(hints);
        curframe = new GifFrame(this, disposal_method, delay,
                                (curframe == null), model,
                                x, y, width, height);
        byte rasline[] = new byte[width];
        if (verbose) {
            System.out.print("Reading a " + width + " by " + height + " " +
                      (interlace ? "" : "non-") + "interlaced image...");
        }
        int initCodeSize = ExtractByte(block, 9);
        if (initCodeSize >= 12) {
            if (verbose) {
                System.out.println("Invalid initial code size: " +
                                   initCodeSize);
            }
            return false;
        }
        boolean ret = parseImage(x, y, width, height,
                                 interlace, initCodeSize,
                                 block, rasline, model);
        if (!ret) {
            abort();
        }
        if (verbose) {
            System.out.println("done in "
                               + (System.currentTimeMillis() - tm)
                               + "ms");
        }
        return ret;
    }
    public static byte[] grow_colormap(byte[] colormap, int newlen) {
        byte[] newcm = new byte[newlen * 3];
        System.arraycopy(colormap, 0, newcm, 0, colormap.length);
        return newcm;
    }
}
class GifFrame {
    private static final boolean verbose = false;
    private static IndexColorModel trans_model;
    static final int DISPOSAL_NONE      = 0x00;
    static final int DISPOSAL_SAVE      = 0x01;
    static final int DISPOSAL_BGCOLOR   = 0x02;
    static final int DISPOSAL_PREVIOUS  = 0x03;
    GifImageDecoder decoder;
    int disposal_method;
    int delay;
    IndexColorModel model;
    int x;
    int y;
    int width;
    int height;
    boolean initialframe;
    public GifFrame(GifImageDecoder id, int dm, int dl, boolean init,
                    IndexColorModel cm, int x, int y, int w, int h) {
        this.decoder = id;
        this.disposal_method = dm;
        this.delay = dl;
        this.model = cm;
        this.initialframe = init;
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
    }
    private void setPixels(int x, int y, int w, int h,
                           ColorModel cm, byte[] pix, int off, int scan) {
        decoder.setPixels(x, y, w, h, cm, pix, off, scan);
    }
    public boolean dispose() {
        if (decoder.imageComplete(ImageConsumer.SINGLEFRAMEDONE, false) == 0) {
            return false;
        } else {
            if (delay > 0) {
                try {
                    if (verbose) {
                        System.out.println("sleeping: "+delay);
                    }
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    return false;
                }
            } else {
                Thread.yield();
            }
            if (verbose && disposal_method != 0) {
                System.out.println("disposal method: "+disposal_method);
            }
            int global_width = decoder.global_width;
            int global_height = decoder.global_height;
            if (x < 0) {
                width += x;
                x = 0;
            }
            if (x + width > global_width) {
                width = global_width - x;
            }
            if (width <= 0) {
                disposal_method = DISPOSAL_NONE;
            } else {
                if (y < 0) {
                    height += y;
                    y = 0;
                }
                if (y + height > global_height) {
                    height = global_height - y;
                }
                if (height <= 0) {
                    disposal_method = DISPOSAL_NONE;
                }
            }
            switch (disposal_method) {
            case DISPOSAL_PREVIOUS:
                byte[] saved_image = decoder.saved_image;
                IndexColorModel saved_model = decoder.saved_model;
                if (saved_image != null) {
                    setPixels(x, y, width, height,
                              saved_model, saved_image,
                              y * global_width + x, global_width);
                }
                break;
            case DISPOSAL_BGCOLOR:
                byte tpix;
                if (model.getTransparentPixel() < 0) {
                    model = trans_model;
                    if (model == null) {
                        model = new IndexColorModel(8, 1,
                                                    new byte[4], 0, true);
                        trans_model = model;
                    }
                    tpix = 0;
                } else {
                    tpix = (byte) model.getTransparentPixel();
                }
                byte[] rasline = new byte[width];
                if (tpix != 0) {
                    for (int i = 0; i < width; i++) {
                        rasline[i] = tpix;
                    }
                }
                if( decoder.saved_image != null ) {
                    for( int i = 0; i < global_width * global_height; i ++ )
                        decoder.saved_image[i] = tpix;
                }
                setPixels(x, y, width, height, model, rasline, 0, 0);
                break;
            case DISPOSAL_SAVE:
                decoder.saved_model = model;
                break;
            }
        }
        return true;
    }
}
